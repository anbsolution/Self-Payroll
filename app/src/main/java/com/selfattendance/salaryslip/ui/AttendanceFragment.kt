package com.selfattendance.salaryslip.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.selfattendance.salaryslip.data.local.AttendanceEntity
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

class AttendanceFragment : Fragment() {
    private val vm: AttendanceViewModel by viewModels()
    private val dateFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM")

    override fun onCreateView(inflater: android.view.LayoutInflater, container: android.view.ViewGroup?, state: Bundle?): View {
        val scroll = Ui.scroll(requireContext())
        val col = Ui.column(requireContext())
        scroll.addView(col)

        col.addView(Ui.title(requireContext(), "Attendance"))
        col.addView(Ui.subtitle(requireContext(), "Monthly record and daily working hours"))

        val ym = YearMonth.now()
        val header = Ui.card(requireContext())
        val inner = Ui.cardInner(requireContext())
        header.addView(inner)
        inner.addView(Ui.sectionLabel(requireContext(), "THIS MONTH"))
        inner.addView(Ui.value(requireContext(), "${ym.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${ym.year}"))
        val summary = Ui.muted(requireContext(), "Loading summary…")
        inner.addView(summary)
        col.addView(header)

        val listCard = Ui.card(requireContext())
        val list = Ui.cardInner(requireContext())
        listCard.addView(list)
        list.addView(Ui.sectionLabel(requireContext(), "DAILY RECORDS"))
        col.addView(listCard)

        vm.month(ym).observe(viewLifecycleOwner) { items ->
            render(list, summary, ym, items)
        }
        return scroll
    }

    private fun render(list: LinearLayout, summary: TextView, ym: YearMonth, items: List<AttendanceEntity>) {
        while (list.childCount > 1) list.removeViewAt(1)
        val byDate = items.associateBy { it.date }
        val present = items.count { it.status == "PRESENT" }
        val absent = items.count { it.status == "ABSENT" }
        val half = items.count { it.status == "HALF_DAY" }
        val leave = items.count { it.status == "LEAVE" }
        val holiday = items.count { it.status == "HOLIDAY" }
        val minutes = items.sumOf { it.workingMinutes }
        summary.text = "Present $present  •  Absent $absent  •  Half $half  •  Leave $leave  •  ${minutes / 60}h ${minutes % 60}m"

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        for (day in 1..ym.lengthOfMonth()) {
            val localDate = ym.atDay(day)
            val item = byDate[localDate.toString()]
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, Ui.dp(requireContext(), 8), 0, Ui.dp(requireContext(), 8))
            }
            val top = Ui.row(requireContext())
            val dateText = Ui.value(requireContext(), localDate.format(dateFormatter))
            val statusText = Ui.muted(requireContext(), item?.status?.replace('_', ' ') ?: if (localDate.isAfter(LocalDate.now())) "—" else "Not marked")
            top.addView(dateText)
            top.addView(statusText)
            row.addView(top)
            val inText = item?.inTime?.let { timeFormat.format(Date(it)) } ?: "—"
            val outText = item?.outTime?.let { timeFormat.format(Date(it)) } ?: "—"
            row.addView(Ui.muted(requireContext(), "In $inText   •   Out $outText   •   ${item?.workingMinutes?.div(60) ?: 0}h ${item?.workingMinutes?.rem(60) ?: 0}m"))
            val edit = Ui.button(requireContext(), "Change status", false)
            row.addView(edit)
            edit.setOnClickListener { showStatusDialog(localDate.toString()) }
            list.addView(row)
            if (day < ym.lengthOfMonth()) list.addView(Ui.divider(requireContext()))
        }
    }

    private fun showStatusDialog(date: String) {
        val options = arrayOf("PRESENT", "ABSENT", "HALF_DAY", "LEAVE", "HOLIDAY")
        AlertDialog.Builder(requireContext())
            .setTitle("Status • $date")
            .setItems(options) { _, which ->
                vm.setStatus(date, options[which]) {
                    if (isAdded) Toast.makeText(requireContext(), "Attendance saved offline", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }
}
