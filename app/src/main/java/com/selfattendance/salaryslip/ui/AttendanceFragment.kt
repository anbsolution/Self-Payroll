package com.selfattendance.salaryslip.ui

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import com.selfattendance.salaryslip.data.local.AttendanceEntity
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class AttendanceFragment : Fragment() {
    private val vm: AttendanceViewModel by viewModels()

    private var selectedMonth = YearMonth.now()
    private var monthLiveData: LiveData<List<AttendanceEntity>>? = null

    private lateinit var monthTitle: TextView
    private lateinit var summaryText: TextView
    private lateinit var calendar: GridLayout
    private lateinit var recordsList: LinearLayout

    private val dayFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM")
    private val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        state: Bundle?
    ): View {
        val scroll = Ui.scroll(requireContext())
        val col = Ui.column(requireContext())
        scroll.addView(col)

        col.addView(Ui.title(requireContext(), "Attendance"))
        col.addView(Ui.subtitle(requireContext(), "Calendar, status and daily working hours"))

        val monthCard = Ui.card(requireContext())
        val monthInner = Ui.cardInner(requireContext())
        monthCard.addView(monthInner)

        monthTitle = Ui.value(requireContext(), "")
        monthInner.addView(Ui.sectionLabel(requireContext(), "ATTENDANCE MONTH"))
        monthInner.addView(monthTitle)

        val nav = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val previous = navButton("‹ Previous")
        val next = navButton("Next ›")

        nav.addView(previous, LinearLayout.LayoutParams(0, Ui.dp(requireContext(), 48), 1f).apply {
            rightMargin = Ui.dp(requireContext(), 6)
        })
        nav.addView(next, LinearLayout.LayoutParams(0, Ui.dp(requireContext(), 48), 1f).apply {
            leftMargin = Ui.dp(requireContext(), 6)
        })
        monthInner.addView(nav)

        previous.setOnClickListener {
            selectedMonth = selectedMonth.minusMonths(1)
            loadMonth()
        }
        next.setOnClickListener {
            selectedMonth = selectedMonth.plusMonths(1)
            loadMonth()
        }

        col.addView(monthCard)

        val summaryCard = Ui.card(requireContext())
        val summaryInner = Ui.cardInner(requireContext())
        summaryCard.addView(summaryInner)
        summaryInner.addView(Ui.sectionLabel(requireContext(), "MONTHLY SUMMARY"))
        summaryText = Ui.muted(requireContext(), "Loading…")
        summaryInner.addView(summaryText)
        col.addView(summaryCard)

        val calendarCard = Ui.card(requireContext())
        val calendarInner = Ui.cardInner(requireContext())
        calendarCard.addView(calendarInner)
        calendarInner.addView(Ui.sectionLabel(requireContext(), "MONTHLY CALENDAR"))

        val weekdays = GridLayout(requireContext()).apply {
            columnCount = 7
            rowCount = 1
            useDefaultMargins = false
        }
        listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { name ->
            weekdays.addView(dayHeader(name))
        }
        calendarInner.addView(weekdays)

        calendar = GridLayout(requireContext()).apply {
            columnCount = 7
            useDefaultMargins = false
        }
        calendarInner.addView(calendar)
        col.addView(calendarCard)

        val legendCard = Ui.card(requireContext())
        val legendInner = Ui.cardInner(requireContext())
        legendCard.addView(legendInner)
        legendInner.addView(Ui.sectionLabel(requireContext(), "STATUS LEGEND"))
        legendInner.addView(Ui.muted(
            requireContext(),
            "● Present    ● Absent    ● Half Day    ● Leave    ● Holiday    ● Weekly Off"
        ))
        legendInner.addView(Ui.muted(requireContext(), "Tap any day to view details or change its status."))
        col.addView(legendCard)

        val recordsCard = Ui.card(requireContext())
        val recordsInner = Ui.cardInner(requireContext())
        recordsCard.addView(recordsInner)
        recordsInner.addView(Ui.sectionLabel(requireContext(), "DAILY DETAILS"))
        recordsList = recordsInner
        col.addView(recordsCard)

        loadMonth()
        return scroll
    }

    private fun navButton(text: String) = Ui.button(requireContext(), text, false).apply {
        minHeight = Ui.dp(requireContext(), 48)
        minimumHeight = Ui.dp(requireContext(), 48)
        setPadding(Ui.dp(requireContext(), 8), 0, Ui.dp(requireContext(), 8), 0)
    }

    private fun dayHeader(text: String): TextView = TextView(requireContext()).apply {
        this.text = text
        gravity = Gravity.CENTER
        textSize = 11f
        setTextColor(Color.DKGRAY)
        setPadding(0, Ui.dp(requireContext(), 7), 0, Ui.dp(requireContext(), 7))
        layoutParams = GridLayout.LayoutParams().apply {
            width = 0
            height = Ui.dp(requireContext(), 30)
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        }
    }

    private fun loadMonth() {
        monthTitle.text = "${selectedMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${selectedMonth.year}"

        monthLiveData?.removeObservers(viewLifecycleOwner)
        monthLiveData = vm.month(selectedMonth)
        monthLiveData?.observe(viewLifecycleOwner) { items ->
            render(items)
        }
    }

    private fun render(items: List<AttendanceEntity>) {
        val byDate = items.associateBy { it.date }

        var present = 0
        var absent = 0
        var halfDay = 0
        var leave = 0
        var holiday = 0
        var weeklyOff = 0
        var workingMinutes = 0L

        for (day in 1..selectedMonth.lengthOfMonth()) {
            val date = selectedMonth.atDay(day)
            val item = byDate[date.toString()]
            val status = item?.status ?: if (date.dayOfWeek == DayOfWeek.SUNDAY) "WEEKLY_OFF"
            else if (date.isAfter(LocalDate.now())) "" else "NOT_MARKED"

            when (status) {
                "PRESENT" -> present++
                "ABSENT" -> absent++
                "HALF_DAY" -> halfDay++
                "LEAVE" -> leave++
                "HOLIDAY" -> holiday++
                "WEEKLY_OFF" -> weeklyOff++
            }
            workingMinutes += item?.workingMinutes ?: 0L
        }

        summaryText.text =
            "Present $present  •  Absent $absent  •  Half Day $halfDay  •  Leave $leave\n" +
            "Holiday $holiday  •  Weekly Off $weeklyOff  •  Working ${workingMinutes / 60}h ${workingMinutes % 60}m"

        renderCalendar(byDate)
        renderDailyDetails(byDate)
    }

    private fun renderCalendar(byDate: Map<String, AttendanceEntity>) {
        calendar.removeAllViews()

        val firstDay = selectedMonth.atDay(1)
        val offset = firstDay.dayOfWeek.value % 7

        repeat(offset) {
            calendar.addView(emptyDayCell())
        }

        for (day in 1..selectedMonth.lengthOfMonth()) {
            val date = selectedMonth.atDay(day)
            val item = byDate[date.toString()]
            val status = item?.status ?: if (date.dayOfWeek == DayOfWeek.SUNDAY) "WEEKLY_OFF"
            else if (date.isAfter(LocalDate.now())) "" else "NOT_MARKED"

            calendar.addView(dayCell(date, status, item))
        }
    }

    private fun emptyDayCell(): View = View(requireContext()).apply {
        layoutParams = GridLayout.LayoutParams().apply {
            width = 0
            height = Ui.dp(requireContext(), 68)
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        }
    }

    private fun dayCell(
        date: LocalDate,
        status: String,
        item: AttendanceEntity?
    ): TextView = TextView(requireContext()).apply {
        text = date.dayOfMonth.toString()
        gravity = Gravity.CENTER
        textSize = 15f
        setTextColor(Color.DKGRAY)
        setTypeface(typeface, android.graphics.Typeface.BOLD)
        setPadding(0, Ui.dp(requireContext(), 4), 0, Ui.dp(requireContext(), 4))
        background = roundedStatusBackground(status)
        layoutParams = GridLayout.LayoutParams().apply {
            width = 0
            height = Ui.dp(requireContext(), 58)
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            setMargins(
                Ui.dp(requireContext(), 3),
                Ui.dp(requireContext(), 3),
                Ui.dp(requireContext(), 3),
                Ui.dp(requireContext(), 3)
            )
        }
        contentDescription = "${date.format(dayFormatter)}: ${displayStatus(status)}"
        setOnClickListener { showDayDialog(date, item, status) }
    }

    private fun roundedStatusBackground(status: String): GradientDrawable {
        val fill = when (status) {
            "PRESENT" -> 0xFFDFF7E7.toInt()
            "ABSENT" -> 0xFFFFE2E2.toInt()
            "HALF_DAY" -> 0xFFFFF0C7.toInt()
            "LEAVE" -> 0xFFE7E0FF.toInt()
            "HOLIDAY" -> 0xFFDDEEFF.toInt()
            "WEEKLY_OFF" -> 0xFFECEFF3.toInt()
            else -> 0xFFF7F8FA.toInt()
        }
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = Ui.dp(requireContext(), 12).toFloat()
            setColor(fill)
        }
    }

    private fun renderDailyDetails(byDate: Map<String, AttendanceEntity>) {
        while (recordsList.childCount > 1) recordsList.removeViewAt(1)

        for (day in 1..selectedMonth.lengthOfMonth()) {
            val date = selectedMonth.atDay(day)
            val item = byDate[date.toString()]
            val status = item?.status ?: if (date.dayOfWeek == DayOfWeek.SUNDAY) "WEEKLY_OFF"
            else if (date.isAfter(LocalDate.now())) "" else "NOT_MARKED"

            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, Ui.dp(requireContext(), 7), 0, Ui.dp(requireContext(), 7))
                setOnClickListener { showDayDialog(date, item, status) }
            }

            row.addView(Ui.value(requireContext(), date.format(dayFormatter)))
            row.addView(Ui.muted(
                requireContext(),
                "${displayStatus(status)}   •   In ${formatTime(item?.inTime)}   •   Out ${formatTime(item?.outTime)}"
            ))
            row.addView(Ui.muted(
                requireContext(),
                "Working ${item?.workingMinutes?.div(60) ?: 0}h ${item?.workingMinutes?.rem(60) ?: 0}m"
            ))
            recordsList.addView(row)

            if (day < selectedMonth.lengthOfMonth()) {
                recordsList.addView(Ui.divider(requireContext()))
            }
        }
    }

    private fun showDayDialog(date: LocalDate, item: AttendanceEntity?, currentStatus: String) {
        val details =
            "${date.format(dayFormatter)}\n\n" +
            "Status: ${displayStatus(currentStatus)}\n" +
            "IN: ${formatTime(item?.inTime)}\n" +
            "OUT: ${formatTime(item?.outTime)}\n" +
            "Working: ${item?.workingMinutes?.div(60) ?: 0}h ${item?.workingMinutes?.rem(60) ?: 0}m"

        AlertDialog.Builder(requireContext())
            .setTitle("Daily Attendance")
            .setMessage(details)
            .setNegativeButton("Close", null)
            .setPositiveButton("Change Status") { _, _ ->
                showStatusDialog(date.toString())
            }
            .show()
    }

    private fun showStatusDialog(date: String) {
        val options = arrayOf(
            "PRESENT",
            "ABSENT",
            "HALF_DAY",
            "LEAVE",
            "HOLIDAY",
            "WEEKLY_OFF"
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Change status • $date")
            .setItems(options) { _, which ->
                vm.setStatus(date, options[which]) {
                    if (isAdded) {
                        Toast.makeText(
                            requireContext(),
                            "Attendance saved offline",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .show()
    }

    private fun formatTime(time: Long?): String =
        time?.let { timeFormatter.format(Date(it)) } ?: "—"

    private fun displayStatus(status: String): String = when (status) {
        "PRESENT" -> "Present"
        "ABSENT" -> "Absent"
        "HALF_DAY" -> "Half Day"
        "LEAVE" -> "Leave"
        "HOLIDAY" -> "Holiday"
        "WEEKLY_OFF" -> "Weekly Off"
        "NOT_MARKED" -> "Not marked"
        "" -> "Upcoming"
        else -> status.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
    }
}
