package com.selfattendance.salaryslip.repository

import com.selfattendance.salaryslip.data.local.AttendanceEntity
import com.selfattendance.salaryslip.data.local.EmployeeProfileEntity
import com.selfattendance.salaryslip.data.local.SalarySlipEntity

/** Secure backend boundary. A later server implementation must use authenticated HTTPS. */
interface SyncApi {
    suspend fun uploadProfile(profile: EmployeeProfileEntity)
    suspend fun uploadAttendance(attendance: AttendanceEntity)
    suspend fun uploadSalary(salary: SalarySlipEntity)
}

/** Safe default: never pretends that remote synchronization succeeded. */
class NoOpSecureApi : SyncApi {
    override suspend fun uploadProfile(profile: EmployeeProfileEntity) =
        throw java.io.IOException("Remote API endpoint is not configured")
    override suspend fun uploadAttendance(attendance: AttendanceEntity) =
        throw java.io.IOException("Remote API endpoint is not configured")
    override suspend fun uploadSalary(salary: SalarySlipEntity) =
        throw java.io.IOException("Remote API endpoint is not configured")
}
