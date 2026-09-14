package com.selfattendance.salaryslip.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.selfattendance.salaryslip.R
import com.selfattendance.salaryslip.data.local.EmployeeProfileEntity
import com.google.android.material.textfield.TextInputLayout

class ProfileFragment : Fragment() {
    private val vm: ProfileViewModel by viewModels()
    private lateinit var fields: MutableMap<String, TextInputLayout>

    override fun onCreateView(inflater: android.view.LayoutInflater, container: android.view.ViewGroup?, state: Bundle?): View {
        val scroll = Ui.scroll(requireContext())
        val col = Ui.column(requireContext())
        scroll.addView(col)

        col.addView(Ui.title(requireContext(), "Profile"))
        col.addView(Ui.subtitle(requireContext(), "Your employee details and salary configuration"))

        fields = linkedMapOf()
        addSection(col, "EMPLOYEE DETAILS", listOf(
            "name" to "Full name",
            "employeeId" to "Employee ID",
            "mobile" to "Mobile number",
            "email" to "Email",
            "department" to "Department",
            "designation" to "Designation",
            "joiningDate" to "Joining date (YYYY-MM-DD)"
        ))
        addSection(col, "COMPANY DETAILS", listOf(
            "companyName" to "Company name",
            "companyAddress" to "Company address"
        ))
        addSection(col, "SALARY CONFIGURATION", listOf(
            "basicSalary" to "Basic salary",
            "hra" to "HRA",
            "allowance" to "Allowances",
            "overtimeRate" to "Overtime rate / hour",
            "deduction" to "Deductions"
        ))

        val save = Ui.button(requireContext(), "Save Profile")
        col.addView(save)
        col.addView(Ui.muted(requireContext(), "Profile data stays on this device and is saved offline."))

        vm.profile.observe(viewLifecycleOwner) { p -> if (p != null) fill(p) }
        save.setOnClickListener {
            val p = EmployeeProfileEntity(
                name = text("name"), employeeId = text("employeeId"), mobile = text("mobile"), email = text("email"),
                department = text("department"), designation = text("designation"), joiningDate = text("joiningDate"),
                companyName = text("companyName"), companyAddress = text("companyAddress"), basicSalary = num("basicSalary"),
                hra = num("hra"), allowance = num("allowance"), overtimeRate = num("overtimeRate"), deduction = num("deduction")
            )
            vm.save(p) { Toast.makeText(requireContext(), "Profile saved offline", Toast.LENGTH_SHORT).show() }
        }
        return scroll
    }

    private fun addSection(col: android.widget.LinearLayout, title: String, specs: List<Pair<String, String>>) {
        val card = Ui.card(requireContext())
        val inner = Ui.cardInner(requireContext())
        card.addView(inner)
        inner.addView(Ui.sectionLabel(requireContext(), title))
        specs.forEach { (key, hint) ->
            val field = Ui.input(requireContext(), hint)
            fields[key] = field
            inner.addView(field)
        }
        col.addView(card)
    }

    private fun text(key: String) = Ui.textFrom(fields[key]!!)
    private fun num(key: String) = text(key).toDoubleOrNull() ?: 0.0

    private fun fill(p: EmployeeProfileEntity) {
        val values = mapOf(
            "name" to p.name, "employeeId" to p.employeeId, "mobile" to p.mobile, "email" to p.email,
            "department" to p.department, "designation" to p.designation, "joiningDate" to p.joiningDate,
            "companyName" to p.companyName, "companyAddress" to p.companyAddress, "basicSalary" to p.basicSalary.toString(),
            "hra" to p.hra.toString(), "allowance" to p.allowance.toString(), "overtimeRate" to p.overtimeRate.toString(),
            "deduction" to p.deduction.toString()
        )
        values.forEach { (key, value) -> fields[key]?.editText?.setText(value) }
    }
}
