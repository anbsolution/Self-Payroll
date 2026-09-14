package com.selfattendance.salaryslip.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.selfattendance.salaryslip.data.local.EmployeeProfileEntity
import com.selfattendance.salaryslip.data.local.SalarySlipEntity
import com.selfattendance.salaryslip.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class PunchViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AppRepository(app)
    private val _today = MutableStateFlow<com.selfattendance.salaryslip.data.local.AttendanceEntity?>(null)
    val today = _today.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    init { refresh() }
    fun refresh() = viewModelScope.launch { _today.value = repo.getToday(LocalDate.now().toString()) }
    fun punch(onResult: (Result<*>) -> Unit) = viewModelScope.launch { val r=repo.punch(System.currentTimeMillis(), LocalDate.now().toString()); _today.value=r.getOrNull() ?: _today.value; onResult(r) }
}

class AttendanceViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AppRepository(app)
    fun month(ym: YearMonth) = repo.observeMonth(ym)
    fun setStatus(date:String,status:String,onDone:()->Unit)=viewModelScope.launch{repo.setAttendanceStatus(date,status);onDone()}
}

class SalaryViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AppRepository(app)
    fun observe(year: Int, month: Int) = repo.observeSalary(year, month)
    fun generate(year: Int, month: Int, overtime: Double, onDone: (SalarySlipEntity, EmployeeProfileEntity?) -> Unit, onError: (String) -> Unit) = viewModelScope.launch {
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
    val profile = repo.observeProfile().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    fun save(p: EmployeeProfileEntity, onDone: () -> Unit) = viewModelScope.launch { repo.saveProfile(p); onDone() }
}
