package com.selfattendance.salaryslip.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [EmployeeProfileEntity::class, AttendanceEntity::class, SalarySlipEntity::class, SyncStateEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun employeeDao(): EmployeeDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun salaryDao(): SalaryDao
    abstract fun syncDao(): SyncDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "self_attendance.db").fallbackToDestructiveMigration().build().also { INSTANCE = it }
        }
    }
}
