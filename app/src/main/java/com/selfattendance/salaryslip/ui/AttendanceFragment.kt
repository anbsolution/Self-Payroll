package com.selfattendance.salaryslip.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.selfattendance.salaryslip.R
import java.time.LocalDate
import java.time.YearMonth

class AttendanceFragment : Fragment() {
    private val vm: AttendanceViewModel by viewModels()
    override fun onCreateView(i:android.view.LayoutInflater,c:android.view.ViewGroup?,s:Bundle?):View {
        val scroll=Ui.scroll(requireContext());val col=Ui.column(requireContext());scroll.addView(col)
        col.addView(Ui.title(requireContext(),"Attendance"));col.addView(Ui.subtitle(requireContext(),"Monthly calendar/list with editable attendance status"))
        val ym=YearMonth.now();val header=Ui.card(requireContext());val h=Ui.row(requireContext());header.addView(h);h.addView(Ui.value(requireContext(),ym.month.name.lowercase().replaceFirstChar{it.uppercase()}+" "+ym.year));val summary=Ui.value(requireContext(),"Loading…");h.addView(summary);col.addView(header)
        val list=LinearLayout(requireContext()).apply{orientation=LinearLayout.VERTICAL};col.addView(list)
        vm.month(ym).observe(viewLifecycleOwner){items->
            list.removeAllViews(); val byDate=items.associateBy{it.date}; val present=items.count{it.status=="PRESENT"};val absent=items.count{it.status=="ABSENT"};val half=items.count{it.status=="HALF_DAY"};val leave=items.count{it.status=="LEAVE"};val holiday=items.count{it.status=="HOLIDAY"};summary.text="P $present  A $absent  H $half  L $leave  Hol $holiday"
            for(day in 1..ym.lengthOfMonth()){
                val date=ym.atDay(day); val a=byDate[date.toString()]; val card=Ui.card(requireContext());val box=Ui.column(requireContext(),0);card.addView(box);box.addView(Ui.label(requireContext(),date.toString()));box.addView(Ui.value(requireContext(),a?.status ?: if(date.isAfter(LocalDate.now())) "—" else "Not marked"));box.addView(Ui.subtitle(requireContext(),"In: ${a?.inTime?.let{java.text.SimpleDateFormat("hh:mm a",java.util.Locale.getDefault()).format(java.util.Date(it))}?:"—"}   Out: ${a?.outTime?.let{java.text.SimpleDateFormat("hh:mm a",java.util.Locale.getDefault()).format(java.util.Date(it))}?:"—"}   Hours: ${a?.workingMinutes?.div(60) ?: 0}h ${a?.workingMinutes?.rem(60) ?: 0}m"));val edit=Ui.button(requireContext(),"Mark status",false);box.addView(edit);edit.setOnClickListener{showStatusDialog(date.toString())};list.addView(card)
            }
        }
        return scroll
    }
    private fun showStatusDialog(date:String){val options=arrayOf("PRESENT","ABSENT","HALF_DAY","LEAVE","HOLIDAY");AlertDialog.Builder(requireContext()).setTitle("Status • $date").setItems(options){_,which->vm.setStatus(date,options[which]){Toast.makeText(requireContext(),"Attendance saved offline",Toast.LENGTH_SHORT).show()}}.show()}
}
