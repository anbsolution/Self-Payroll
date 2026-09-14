package com.selfattendance.salaryslip.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "employee_profile")
data class EmployeeProfileEntity(
    @PrimaryKey val id: String = "profile",
    val name: String = "",
    val employeeId: String = "",
    val mobile: String = "",
    val email: String = "",
    val department: String = "",
    val designation: String = "",
    val joiningDate: String = "",
    val companyName: String = "",
    val companyAddress: String = "",
    val basicSalary: Double = 0.0,
    val hra: Double = 0.0,
    val allowance: Double = 0.0,
    val overtimeRate: Double = 0.0,
    val deduction: Double = 0.0,
    val updatedAt: Long = System.currentTimeMillis(),
    val syncState: String = "PENDING"
)

@Entity(tableName = "attendance", indices = [Index(value = ["date"], unique = true)])
data class AttendanceEntity(
    @PrimaryKey val id: String,
    val date: String,
    val status: String = "PRESENT",
    val inTime: Long? = null,
    val outTime: Long? = null,
    val workingMinutes: Long = 0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncState: String = "PENDING"
)

@Entity(tableName = "salary_slip", indices = [Index(value = ["year", "month"], unique = true)])
data class SalarySlipEntity(
    @PrimaryKey val id: String,
    val year: Int,
    val month: Int,
    val basic: Double,
    val hra: Double,
    val allowance: Double,
    val overtime: Double,
    val deductions: Double,
    val gross: Double,
    val net: Double,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncState: String = "PENDING"
)

@Entity(tableName = "sync_state")
data class SyncStateEntity(
    @PrimaryKey val recordId: String,
    val entityType: String,
    val state: String = "PENDING",
    val attempts: Int = 0,
    val lastAttemptAt: Long? = null,
    val lastError: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
