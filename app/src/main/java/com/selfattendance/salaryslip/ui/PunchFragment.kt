package com.selfattendance.salaryslip.ui

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.selfattendance.salaryslip.R
import java.text.SimpleDateFormat
import java.util.*
import android.os.Handler
import android.os.Looper

class PunchFragment : Fragment() {
    private val vm: PunchViewModel by viewModels()
    override fun onCreateView(i: android.view.LayoutInflater,c:android.view.ViewGroup?,s:Bundle?):View {
        val scroll=Ui.scroll(requireContext()); val col=Ui.column(requireContext());scroll.addView(col)
        col.addView(Ui.title(requireContext(),"Punch")); col.addView(Ui.subtitle(requireContext(),"Offline-first attendance clock"))
        val time=Ui.card(requireContext()); val inner=Ui.column(requireContext(),0); time.addView(inner)
        val clock=Ui.value(requireContext(),"—"); val date=Ui.subtitle(requireContext(),"—"); inner.addView(Ui.label(requireContext(),"CURRENT DATE & TIME"));inner.addView(clock);inner.addView(date);col.addView(time)
        val status=Ui.card(requireContext()); val si=Ui.column(requireContext(),0);status.addView(si);si.addView(Ui.label(requireContext(),"TODAY'S STATUS"));val statusV=Ui.value(requireContext(),"Not punched");si.addView(statusV);val inV=Ui.value(requireContext(),"In: —");val outV=Ui.value(requireContext(),"Out: —");val workV=Ui.value(requireContext(),"Working: —");si.addView(inV);si.addView(outV);si.addView(workV);col.addView(status)
        val btn=Ui.button(requireContext(),"Punch In / Punch Out");col.addView(btn)
        fun refresh(){val now=Date();clock.text=SimpleDateFormat("hh:mm:ss a",Locale.getDefault()).format(now);date.text=SimpleDateFormat("EEEE, dd MMM yyyy",Locale.getDefault()).format(now)}
        refresh(); val handler=Handler(Looper.getMainLooper()); val tick=object:Runnable{override fun run(){refresh();handler.postDelayed(this,1000)}}; handler.post(tick); viewLifecycleOwner.lifecycle.addObserver(object:androidx.lifecycle.DefaultLifecycleObserver{override fun onDestroy(owner:androidx.lifecycle.LifecycleOwner){handler.removeCallbacks(tick)}})
        vm.today.observe(viewLifecycleOwner){a-> if(a!=null){statusV.text=if(a.outTime!=null)"Completed" else "Checked in";inV.text="In: "+SimpleDateFormat("hh:mm a",Locale.getDefault()).format(Date(a.inTime!!));outV.text="Out: "+(a.outTime?.let{SimpleDateFormat("hh:mm a",Locale.getDefault()).format(Date(it))}?:"—");workV.text="Working: "+(a.workingMinutes/60)+"h "+(a.workingMinutes%60)+"m"} }
        btn.setOnClickListener{vm.punch{r->r.onFailure{Toast.makeText(requireContext(),it.message,Toast.LENGTH_SHORT).show()};r.onSuccess{Toast.makeText(requireContext(),"Punch saved offline",Toast.LENGTH_SHORT).show()}}}
        return scroll
    }
    override fun onDestroyView(){super.onDestroyView()}
}
