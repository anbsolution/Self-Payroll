package com.selfattendance.salaryslip.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.selfattendance.salaryslip.data.local.AttendanceEntity
import com.selfattendance.salaryslip.data.local.EmployeeProfileEntity
import com.selfattendance.salaryslip.data.local.SalarySlipEntity
import com.selfattendance.salaryslip.repository.AppRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class PunchViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AppRepository(app)
    val today = repo.observeToday(LocalDate.now().toString()).asLiveData()

    fun punch(onResult: (Boolean, String) -> Unit) = viewModelScope.launch {
        val result = repo.punch(System.currentTimeMillis(), LocalDate.now().toString())
        result.fold(
            onSuccess = { onResult(true, "Punch saved offline") },
            onFailure = { onResult(false, it.message ?: "Punch failed") }
        )
    }
}

class AttendanceViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AppRepository(app)
    fun month(ym: YearMonth) = repo.observeMonth(ym).asLiveData()
    fun setStatus(date: String, status: String, onDone: () -> Unit) = viewModelScope.launch {
        repo.setAttendanceStatus(date, status)
        onDone()
    }
}

class SalaryViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AppRepository(app)
    fun observe(year: Int, month: Int) = repo.observeSalary(year, month).asLiveData()

    fun generate(
        year: Int,
        month: Int,
        overtime: Double,
        onDone: (SalarySlipEntity, EmployeeProfileEntity?) -> Unit,
        onError: (String) -> Unit
    ) = viewModelScope.launch {
        try {
            val slip = repo.createSalary(year, month, overtime)
            onDone(slip, repo.getProfile())
        } catch (e: Exception) {
            onError(e.message ?: "Unable to generate salary slip")
        }
    }
}

class ProfileViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AppRepository(app)
    val profile = repo.observeProfile().asLiveData()

    fun save(profile: EmployeeProfileEntity, onDone: () -> Unit) = viewModelScope.launch {
        repo.saveProfile(profile)
        onDone()
    }
}
