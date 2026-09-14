package com.selfattendance.salaryslip

import com.selfattendance.salaryslip.domain.SalaryBreakdown
import org.junit.Assert.assertEquals
import org.junit.Test

class SalaryCalculationTest {
    @Test fun calculatesGrossAndNet() {
        val s=SalaryBreakdown(20000.0,5000.0,3000.0,1000.0,2000.0)
        assertEquals(29000.0,s.gross,0.001);assertEquals(27000.0,s.net,0.001)
    }
}
