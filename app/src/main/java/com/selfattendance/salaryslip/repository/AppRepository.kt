package com.selfattendance.salaryslip.repository

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.selfattendance.salaryslip.data.local.AppDatabase
import com.selfattendance.salaryslip.data.local.AttendanceEntity
import com.selfattendance.salaryslip.data.local.EmployeeProfileEntity
import com.selfattendance.salaryslip.data.local.SalarySlipEntity
import com.selfattendance.salaryslip.data.local.SyncStateEntity
import com.selfattendance.salaryslip.domain.SalaryBreakdown
import kotlinx.coroutines.flow.Flow
import java.time.Duration
import java.time.YearMonth
import java.util.UUID

class AppRepository(
    private val context: Context,
    private val db: AppDatabase = AppDatabase.get(context)
) {
    private val employeeDao = db.employeeDao()
    private val attendanceDao = db.attendanceDao()
    private val salaryDao = db.salaryDao()

    fun observeProfile() = employeeDao.observe()
    fun observeMonth(yearMonth: YearMonth): Flow<List<AttendanceEntity>> =
        attendanceDao.observeRange(yearMonth.atDay(1).toString(), yearMonth.atEndOfMonth().toString())
    fun observeSalary(year: Int, month: Int) = salaryDao.observe(year, month)

    suspend fun saveProfile(profile: EmployeeProfileEntity) {
        val now = System.currentTimeMillis()
        employeeDao.upsert(profile.copy(id = "profile", updatedAt = now, syncState = "PENDING"))
        db.syncDao().upsert(SyncStateEntity("profile", "PROFILE", updatedAt = now))
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
        db.syncDao().upsert(SyncStateEntity(result.id, "ATTENDANCE", updatedAt = result.updatedAt))
        scheduleSync()
        return Result.success(result)
    }

    suspend fun getToday(date: String) = attendanceDao.getByDate(date)

    suspend fun setAttendanceStatus(date: String, status: String) {
        val now = System.currentTimeMillis()
        val old = attendanceDao.getByDate(date)
        val item = old ?: AttendanceEntity(UUID.randomUUID().toString(), date, status = status, createdAt = now, updatedAt = now)
        val updated = item.copy(status = status, updatedAt = now, syncState = "PENDING")
        attendanceDao.upsert(updated)
        db.syncDao().upsert(SyncStateEntity(updated.id, "ATTENDANCE", updatedAt = now))
        scheduleSync()
    }

    suspend fun createSalary(year: Int, month: Int, overtimeHours: Double = 0.0): SalarySlipEntity {
        require(overtimeHours >= 0.0) { "Overtime hours cannot be negative" }
        val p = employeeDao.get() ?: EmployeeProfileEntity()
        val breakdown = SalaryBreakdown(
            basic = p.basicSalary,
            hra = p.hra,
            allowance = p.allowance,
            overtime = overtimeHours * p.overtimeRate,
            deductions = p.deduction
        )
        val now = System.currentTimeMillis()
        val item = SalarySlipEntity(
            id = "$year-$month",
            year = year,
            month = month,
            basic = breakdown.basic,
            hra = breakdown.hra,
            allowance = breakdown.allowance,
            overtime = breakdown.overtime,
            deductions = breakdown.deductions,
            gross = breakdown.gross,
            net = breakdown.net,
            createdAt = salaryDao.get(year, month)?.createdAt ?: now,
            updatedAt = now,
            syncState = "PENDING"
        )
        salaryDao.upsert(item)
        db.syncDao().upsert(SyncStateEntity(item.id, "SALARY", updatedAt = now))
        scheduleSync()
        return item
    }

    fun scheduleSync() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork("offline-sync", ExistingWorkPolicy.KEEP, request)
    }
}
