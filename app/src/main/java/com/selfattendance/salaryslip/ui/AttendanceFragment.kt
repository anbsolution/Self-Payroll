package com.selfattendance.salaryslip.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.selfattendance.salaryslip.data.local.AttendanceEntity
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.util.Date
import java.util.Locale

class AttendanceFragment : Fragment() {
    private val vm: AttendanceViewModel by viewModels()

    override fun onCreateView(inflater: android.view.LayoutInflater, container: android.view.ViewGroup?, state: Bundle?): View {
        val scroll = Ui.scroll(requireContext())
        val col = Ui.column(requireContext())
        scroll.addView(col)
        col.addView(Ui.title(requireContext(), "Attendance"))
        col.addView(Ui.subtitle(requireContext(), "Monthly attendance calendar and daily details"))

        val ym = YearMonth.now()
        val header = Ui.card(requireContext())
        val row = Ui.row(requireContext())
        header.addView(row)
        row.addView(Ui.value(requireContext(), "${ym.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${ym.year}"))
        val summary = Ui.value(requireContext(), "Loading…")
        row.addView(summary)
        col.addView(header)

        val list = LinearLayout(requireContext()).apply { orientation = LinearLayout.VERTICAL }
        col.addView(list)

        vm.month(ym).observe(viewLifecycleOwner) { items ->
            render(list, summary, ym, items)
        }
        return scroll
    }

    private fun render(list: LinearLayout, summary: android.widget.TextView, ym: YearMonth, items: List<AttendanceEntity>) {
        list.removeAllViews()
        val byDate = items.associateBy { it.date }
        val present = items.count { it.status == "PRESENT" }
        val absent = items.count { it.status == "ABSENT" }
        val half = items.count { it.status == "HALF_DAY" }
        val leave = items.count { it.status == "LEAVE" }
        val holiday = items.count { it.status == "HOLIDAY" }
        val minutes = items.sumOf { it.workingMinutes }
        summary.text = "P $present  A $absent  H $half  L $leave  Hol $holiday • ${minutes / 60}h ${minutes % 60}m"

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        for (day in 1..ym.lengthOfMonth()) {
            val date = ym.atDay(day)
            val item = byDate[date.toString()]
            val card = Ui.card(requireContext())
            val box = Ui.column(requireContext(), 0)
            card.addView(box)
            box.addView(Ui.label(requireContext(), date.toString()))
            box.addView(Ui.value(requireContext(), item?.status ?: if (date.isAfter(LocalDate.now())) "—" else "Not marked"))
            val inText = item?.inTime?.let { timeFormat.format(Date(it)) } ?: "—"
            val outText = item?.outTime?.let { timeFormat.format(Date(it)) } ?: "—"
            box.addView(Ui.subtitle(requireContext(), "In: $inText   Out: $outText   Hours: ${item?.workingMinutes?.div(60) ?: 0}h ${item?.workingMinutes?.rem(60) ?: 0}m"))
            val edit = Ui.button(requireContext(), "Mark status", false)
            box.addView(edit)
            edit.setOnClickListener { showStatusDialog(date.toString()) }
            list.addView(card)
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
