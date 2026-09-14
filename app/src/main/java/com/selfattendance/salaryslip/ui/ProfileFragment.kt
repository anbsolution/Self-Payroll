package com.selfattendance.salaryslip.ui

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.selfattendance.salaryslip.data.local.EmployeeProfileEntity
import com.selfattendance.salaryslip.R

class ProfileFragment : Fragment() {
    private val vm: ProfileViewModel by viewModels()
    private lateinit var fields: Map<String, com.google.android.material.textfield.TextInputLayout>
    override fun onCreateView(i:android.view.LayoutInflater,c:android.view.ViewGroup?,s:Bundle?):View {
        val scroll=Ui.scroll(requireContext());val col=Ui.column(requireContext());scroll.addView(col)
        col.addView(Ui.title(requireContext(),"Profile"));col.addView(Ui.subtitle(requireContext(),"Employee and company information"))
        val keys=listOf("name","employeeId","mobile","email","department","designation","joiningDate","companyName","companyAddress","basicSalary","hra","allowance","overtimeRate","deduction")
        fields=keys.associateWith{Ui.input(requireContext(),when(it){"employeeId"->"Employee ID";"joiningDate"->"Joining date (YYYY-MM-DD)";"companyName"->"Company name";"companyAddress"->"Company address";"basicSalary"->"Basic salary";"hra"->"HRA";"allowance"->"Allowances";"overtimeRate"->"Overtime rate / hour";"deduction"->"Deductions";else->it.replaceFirstChar{c->c.uppercase()}})}
        keys.forEach{col.addView(fields[it])}
        val save=Ui.button(requireContext(),"Save Profile");col.addView(save)
        vm.profile.observe(viewLifecycleOwner){p->if(p!=null)fill(p)}
        save.setOnClickListener{val p=EmployeeProfileEntity(name=Ui.textFrom(fields["name"]!!),employeeId=Ui.textFrom(fields["employeeId"]!!),mobile=Ui.textFrom(fields["mobile"]!!),email=Ui.textFrom(fields["email"]!!),department=Ui.textFrom(fields["department"]!!),designation=Ui.textFrom(fields["designation"]!!),joiningDate=Ui.textFrom(fields["joiningDate"]!!),companyName=Ui.textFrom(fields["companyName"]!!),companyAddress=Ui.textFrom(fields["companyAddress"]!!),basicSalary=num("basicSalary"),hra=num("hra"),allowance=num("allowance"),overtimeRate=num("overtimeRate"),deduction=num("deduction"));vm.save(p){Toast.makeText(requireContext(),"Profile saved offline",Toast.LENGTH_SHORT).show()}}
        return scroll
    }
    private fun num(k:String)=Ui.textFrom(fields[k]!!).toDoubleOrNull()?:0.0
    private fun fill(p:EmployeeProfileEntity){val m=mapOf("name" to p.name,"employeeId" to p.employeeId,"mobile" to p.mobile,"email" to p.email,"department" to p.department,"designation" to p.designation,"joiningDate" to p.joiningDate,"companyName" to p.companyName,"companyAddress" to p.companyAddress,"basicSalary" to p.basicSalary.toString(),"hra" to p.hra.toString(),"allowance" to p.allowance.toString(),"overtimeRate" to p.overtimeRate.toString(),"deduction" to p.deduction.toString());m.forEach{(k,v)->fields[k]?.editText?.setText(v)}}
}
