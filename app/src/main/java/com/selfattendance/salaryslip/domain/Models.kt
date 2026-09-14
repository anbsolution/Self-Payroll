package com.selfattendance.salaryslip.domain

data class SalaryBreakdown(val basic: Double, val hra: Double, val allowance: Double, val overtime: Double, val deductions: Double) {
    val gross: Double get() = basic + hra + allowance + overtime
    val net: Double get() = gross - deductions
}
