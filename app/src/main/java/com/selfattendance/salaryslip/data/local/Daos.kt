package com.selfattendance.salaryslip.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employee_profile WHERE id = 'profile' LIMIT 1") fun observe(): Flow<EmployeeProfileEntity?>
    @Query("SELECT * FROM employee_profile WHERE id = 'profile' LIMIT 1") suspend fun get(): EmployeeProfileEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(profile: EmployeeProfileEntity)
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance WHERE date = :date LIMIT 1") suspend fun getByDate(date: String): AttendanceEntity?
    @Query("SELECT * FROM attendance WHERE date BETWEEN :from AND :to ORDER BY date ASC") fun observeRange(from: String, to: String): Flow<List<AttendanceEntity>>
    @Query("SELECT * FROM attendance WHERE syncState IN ('PENDING','FAILED')") suspend fun pending(): List<AttendanceEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(item: AttendanceEntity)
    @Query("UPDATE attendance SET syncState = :state, updatedAt = :updatedAt WHERE id = :id") suspend fun setSyncState(id: String, state: String, updatedAt: Long)
}

@Dao
interface SalaryDao {
    @Query("SELECT * FROM salary_slip WHERE year = :year AND month = :month LIMIT 1") suspend fun get(year: Int, month: Int): SalarySlipEntity?
    @Query("SELECT * FROM salary_slip WHERE year = :year AND month = :month LIMIT 1") fun observe(year: Int, month: Int): Flow<SalarySlipEntity?>
    @Query("SELECT * FROM salary_slip WHERE syncState IN ('PENDING','FAILED')") suspend fun pending(): List<SalarySlipEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(item: SalarySlipEntity)
    @Query("UPDATE salary_slip SET syncState = :state, updatedAt = :updatedAt WHERE id = :id") suspend fun setSyncState(id: String, state: String, updatedAt: Long)
}

@Dao
interface SyncDao {
    @Query("SELECT * FROM sync_state WHERE state != 'SYNCED'") suspend fun pending(): List<SyncStateEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(item: SyncStateEntity)
    @Query("UPDATE sync_state SET state = :state, attempts = attempts + 1, lastAttemptAt = :time, lastError = :error, updatedAt = :time WHERE recordId = :id") suspend fun update(id: String, state: String, time: Long, error: String?)
}
