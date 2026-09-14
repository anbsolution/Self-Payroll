package com.selfattendance.salaryslip.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.selfattendance.salaryslip.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PunchFragment : Fragment() {
    private val vm: PunchViewModel by viewModels()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var clock: android.widget.TextView
    private lateinit var date: android.widget.TextView

    override fun onCreateView(inflater: android.view.LayoutInflater, container: android.view.ViewGroup?, state: Bundle?): View {
        val scroll = Ui.scroll(requireContext())
        val col = Ui.column(requireContext())
        scroll.addView(col)
        col.addView(Ui.title(requireContext(), "Punch"))
        col.addView(Ui.subtitle(requireContext(), "Offline-first attendance clock"))

        val timeCard = Ui.card(requireContext())
        val timeInner = Ui.column(requireContext(), 0)
        timeCard.addView(timeInner)
        timeInner.addView(Ui.label(requireContext(), "CURRENT DATE & TIME"))
        clock = Ui.value(requireContext(), "—")
        date = Ui.subtitle(requireContext(), "—")
        timeInner.addView(clock)
        timeInner.addView(date)
        col.addView(timeCard)

        val statusCard = Ui.card(requireContext())
        val statusInner = Ui.column(requireContext(), 0)
        statusCard.addView(statusInner)
        statusInner.addView(Ui.label(requireContext(), "TODAY'S STATUS"))
        val status = Ui.value(requireContext(), "Not punched")
        val inTime = Ui.value(requireContext(), "In: —")
        val outTime = Ui.value(requireContext(), "Out: —")
        val working = Ui.value(requireContext(), "Working: —")
        statusInner.addView(status)
        statusInner.addView(inTime)
        statusInner.addView(outTime)
        statusInner.addView(working)
        col.addView(statusCard)

        val button = Ui.button(requireContext(), "Punch In / Punch Out")
        col.addView(button)

        vm.today.observe(viewLifecycleOwner) { a ->
            if (a == null) {
                status.text = "Not punched"
                inTime.text = "In: —"
                outTime.text = "Out: —"
                working.text = "Working: —"
            } else {
                status.text = if (a.outTime != null) "Completed" else "Checked in"
                inTime.text = "In: ${a.inTime?.let { formatTime(it) } ?: "—"}"
                outTime.text = "Out: ${a.outTime?.let { formatTime(it) } ?: "—"}"
                working.text = "Working: ${a.workingMinutes / 60}h ${a.workingMinutes % 60}m"
            }
        }

        button.setOnClickListener {
            button.isEnabled = false
            vm.punch { ok, message ->
                if (isAdded) {
                    button.isEnabled = true
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            }
        }
        return scroll
    }

    override fun onStart() {
        super.onStart()
        tickClock.run()
    }

    override fun onStop() {
        handler.removeCallbacks(tickClock)
        super.onStop()
    }

    private val tickClock = object : Runnable {
        override fun run() {
            if (!isAdded) return
            val now = Date()
            clock.text = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(now)
            date.text = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()).format(now)
            handler.postDelayed(this, 1000L)
        }
    }

    private fun formatTime(value: Long): String =
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(value))
}
