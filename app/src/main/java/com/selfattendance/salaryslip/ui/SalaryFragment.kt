package com.selfattendance.salaryslip.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.selfattendance.salaryslip.data.local.EmployeeProfileEntity
import com.selfattendance.salaryslip.data.local.SalarySlipEntity
import java.io.File
import java.io.FileOutputStream
import java.time.YearMonth
import java.util.Locale

class SalaryFragment : Fragment() {
    private val vm: SalaryViewModel by viewModels()
    private var lastPdf: File? = null

    override fun onCreateView(i: android.view.LayoutInflater, c: android.view.ViewGroup?, s: Bundle?): View {
        val scroll = Ui.scroll(requireContext())
        val col = Ui.column(requireContext())
        scroll.addView(col)
        col.addView(Ui.title(requireContext(), "Salary Slip"))
        col.addView(Ui.subtitle(requireContext(), "Monthly payroll, premium preview and offline PDF"))

        val ym = YearMonth.now()
        val info = Ui.card(requireContext())
        val ii = Ui.column(requireContext(), 0)
        info.addView(ii)
        ii.addView(Ui.label(requireContext(), "PAY PERIOD"))
        ii.addView(Ui.value(requireContext(), "${ym.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${ym.year}"))
        col.addView(info)

        val overtime = Ui.input(requireContext(), "Overtime hours", "0")
        col.addView(overtime)

        val preview = Ui.card(requireContext())
        val pv = Ui.column(requireContext(), 0)
        preview.addView(pv)
        pv.addView(Ui.label(requireContext(), "PREMIUM PREVIEW"))
        val details = Ui.value(requireContext(), "Generate a slip to calculate salary.")
        pv.addView(details)
        col.addView(preview)

        val generate = Ui.button(requireContext(), "Generate / Save PDF")
        val share = Ui.button(requireContext(), "Share PDF", false)
        col.addView(generate)
        col.addView(share)
        share.isEnabled = false

        vm.observe(ym.year, ym.month).observe(viewLifecycleOwner) { slip ->
            if (slip != null) {
                details.text = "Basic: ₹%.2f\nHRA: ₹%.2f\nAllowances: ₹%.2f\nOvertime: ₹%.2f\nDeductions: ₹%.2f\nGross: ₹%.2f\nNet Salary: ₹%.2f".format(
                    Locale.US, slip.basic, slip.hra, slip.allowance, slip.overtime,
                    slip.deductions, slip.gross, slip.net
                )
            }
        }

        generate.setOnClickListener {
            val hours = Ui.textFrom(overtime).toDoubleOrNull() ?: 0.0
            if (hours < 0.0) {
                Toast.makeText(requireContext(), "Overtime hours cannot be negative", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            vm.generate(ym.year, ym.month, hours, { slip, profile ->
                if (isAdded) {
                    Toast.makeText(requireContext(), "Net salary: ₹%.2f".format(Locale.US, slip.net), Toast.LENGTH_SHORT).show()
                    details.text = "Basic: ₹%.2f\nHRA: ₹%.2f\nAllowances: ₹%.2f\nOvertime: ₹%.2f\nDeductions: ₹%.2f\nGross: ₹%.2f\nNet Salary: ₹%.2f".format(Locale.US, slip.basic, slip.hra, slip.allowance, slip.overtime, slip.deductions, slip.gross, slip.net)
                    createPdf(slip, profile) { success -> share.isEnabled = success }
                }
            }, { error ->
                if (isAdded) Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            })
        }

        share.setOnClickListener {
            val file = lastPdf ?: return@setOnClickListener
            val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)
            startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }, "Share salary slip"))
        }
        return scroll
    }

    private fun createPdf(slip: SalarySlipEntity, profile: EmployeeProfileEntity?, onComplete: (Boolean) -> Unit) {
        val slipFile = File(requireContext().filesDir, "salary-slip-${slip.year}-${slip.month}.pdf")
        val doc = PdfDocument()
        val page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val canvas = page.canvas
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.color = Color.rgb(49, 87, 213)
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, 0f, 595f, 105f, paint)
        paint.color = Color.WHITE
        paint.textSize = 25f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("SALARY SLIP", 40f, 48f, paint)
        paint.textSize = 13f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Self Attendance & Salary Slip", 40f, 75f, paint)
        canvas.drawText("Period: ${slip.month.toString().padStart(2, '0')}/${slip.year}", 40f, 94f, paint)

        paint.color = Color.rgb(30, 38, 58)
        paint.textSize = 15f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("Employee", 40f, 145f, paint)
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 13f
        canvas.drawText(profile?.name?.ifBlank { "Employee" } ?: "Employee", 40f, 168f, paint)
        canvas.drawText("ID: ${profile?.employeeId?.ifBlank { "—" } ?: "—"}", 40f, 188f, paint)
        canvas.drawText(profile?.companyName?.ifBlank { "Company" } ?: "Company", 330f, 168f, paint)
        canvas.drawText(profile?.department?.ifBlank { "" } ?: "", 330f, 188f, paint)

        var y = 235f
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textSize = 14f
        canvas.drawText("Earnings", 40f, y, paint)
        y += 28f
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 13f
        val earnings = listOf(
            "Basic salary" to slip.basic,
            "HRA" to slip.hra,
            "Allowances" to slip.allowance,
            "Overtime" to slip.overtime
        )
        earnings.forEach { (label, amount) ->
            canvas.drawText(label, 40f, y, paint)
            canvas.drawText("₹%.2f".format(Locale.US, amount), 420f, y, paint)
            y += 25f
        }
        y += 10f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("Gross salary", 40f, y, paint)
        canvas.drawText("₹%.2f".format(Locale.US, slip.gross), 420f, y, paint)
        y += 45f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Deductions", 40f, y, paint)
        canvas.drawText("₹%.2f".format(Locale.US, slip.deductions), 420f, y, paint)
        y += 45f
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textSize = 18f
        canvas.drawText("NET SALARY", 40f, y, paint)
        canvas.drawText("₹%.2f".format(Locale.US, slip.net), 390f, y, paint)
        paint.textSize = 11f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Generated offline • Local data first", 40f, 790f, paint)

        doc.finishPage(page)
        return try {
            FileOutputStream(slipFile).use { doc.writeTo(it) }
            doc.close()
            lastPdf = slipFile
            onComplete(true)
            true
        } catch (e: Exception) {
            doc.close()
            Toast.makeText(requireContext(), e.message ?: "Unable to create PDF", Toast.LENGTH_SHORT).show()
            onComplete(false)
            false
        }
    }

}
