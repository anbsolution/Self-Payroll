package com.selfattendance.salaryslip.repository

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.selfattendance.salaryslip.data.local.*
import com.selfattendance.salaryslip.domain.SalaryBreakdown
import kotlinx.coroutines.flow.Flow
import java.time.Duration
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

class AppRepository(private val context: Context, private val db: AppDatabase = AppDatabase.get(context)) {
    private val employeeDao = db.employeeDao(); private val attendanceDao = db.attendanceDao(); private val salaryDao = db.salaryDao()

    fun observeProfile() = employeeDao.observe()
    fun observeMonth(yearMonth: YearMonth): Flow<List<AttendanceEntity>> = attendanceDao.observeRange(yearMonth.atDay(1).toString(), yearMonth.atEndOfMonth().toString())
    fun observeSalary(year: Int, month: Int) = salaryDao.observe(year, month)

    suspend fun saveProfile(p: EmployeeProfileEntity) {
        employeeDao.upsert(p.copy(updatedAt = System.currentTimeMillis(), syncState = "PENDING"))
        db.syncDao().upsert(SyncStateEntity("profile", "PROFILE"))
        scheduleSync()
    }

    suspend fun punch(now: Long, date: String): Result<AttendanceEntity> {
        val existing = attendanceDao.getByDate(date)
        val result = when {
            existing == null -> AttendanceEntity(UUID.randomUUID().toString(), date, inTime = now)
            existing.inTime == null -> existing.copy(inTime = now, updatedAt = now, syncState = "PENDING")
            existing.outTime == null && now > existing.inTime -> {
                val minutes = Duration.ofMillis(now - existing.inTime).toMinutes()
                existing.copy(outTime = now, workingMinutes = minutes, updatedAt = now, syncState = "PENDING")
            }
            existing.outTime != null -> return Result.failure(IllegalStateException("Today's punch is already complete."))
            else -> return Result.failure(IllegalArgumentException("Invalid punch time."))
        }
        attendanceDao.upsert(result)
        db.syncDao().upsert(SyncStateEntity(result.id, "ATTENDANCE"))
        scheduleSync()
        return Result.success(result)
    }

    fun observeToday(date: String) = attendanceDao.observeByDate(date)
    suspend fun getToday(date: String) = attendanceDao.getByDate(date)
    suspend fun getProfile() = employeeDao.get()
    suspend fun setAttendanceStatus(date: String, status: String) {
        val old = attendanceDao.getByDate(date)
        val item = old ?: AttendanceEntity(UUID.randomUUID().toString(), date, status = status)
        attendanceDao.upsert(item.copy(status = status, updatedAt = System.currentTimeMillis(), syncState = "PENDING"))
        db.syncDao().upsert(SyncStateEntity(item.id, "ATTENDANCE"))
        scheduleSync()
    }
    suspend fun getProfile() = employeeDao.get()
    suspend fun getSalary(year: Int, month: Int) = salaryDao.get(year, month)

    suspend fun createSalary(year: Int, month: Int, overtimeHours: Double = 0.0): SalarySlipEntity {
        val p = employeeDao.get() ?: EmployeeProfileEntity()
        val breakdown = SalaryBreakdown(p.basicSalary, p.hra, p.allowance, overtimeHours * p.overtimeRate, p.deduction)
        val item = SalarySlipEntity("$year-$month", year, month, breakdown.basic, breakdown.hra, breakdown.allowance, breakdown.overtime, breakdown.deductions, breakdown.gross, breakdown.net)
        salaryDao.upsert(item); db.syncDao().upsert(SyncStateEntity(item.id, "SALARY")); scheduleSync(); return item
    }

    fun scheduleSync() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>().setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build()
        WorkManager.getInstance(context).enqueueUniqueWork("offline-sync", ExistingWorkPolicy.KEEP, request)
    }
}
