package com.selfattendance.salaryslip.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
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

    override fun onCreateView(inflater: android.view.LayoutInflater, container: android.view.ViewGroup?, state: Bundle?): View {
        val scroll = Ui.scroll(requireContext())
        val col = Ui.column(requireContext())
        scroll.addView(col)

        col.addView(Ui.title(requireContext(), "Salary Slip"))
        col.addView(Ui.subtitle(requireContext(), "Calculate, preview and share your monthly salary"))

        val ym = YearMonth.now()
        val period = Ui.card(requireContext())
        val periodInner = Ui.cardInner(requireContext())
        period.addView(periodInner)
        periodInner.addView(Ui.sectionLabel(requireContext(), "PAY PERIOD"))
        periodInner.addView(Ui.value(requireContext(), "${ym.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${ym.year}"))
        col.addView(period)

        val inputCard = Ui.card(requireContext())
        val inputInner = Ui.cardInner(requireContext())
        inputCard.addView(inputInner)
        inputInner.addView(Ui.sectionLabel(requireContext(), "PAYROLL INPUT"))
        val overtime = Ui.input(requireContext(), "Overtime hours", "0")
        inputInner.addView(overtime)
        val generate = Ui.button(requireContext(), "Generate / Save PDF")
        inputInner.addView(generate)
        col.addView(inputCard)

        val preview = Ui.card(requireContext())
        val previewInner = Ui.cardInner(requireContext())
        preview.addView(previewInner)
        previewInner.addView(Ui.sectionLabel(requireContext(), "PREMIUM PREVIEW"))
        val details = Ui.value(requireContext(), "Generate a slip to calculate salary.")
        details.textSize = 16f
        previewInner.addView(details)
        col.addView(preview)

        val share = Ui.button(requireContext(), "Share PDF", false)
        share.isEnabled = false
        col.addView(share)
        col.addView(Ui.muted(requireContext(), "PDF is generated locally. No salary data is required online."))

        vm.observe(ym.year, ym.monthValue).observe(viewLifecycleOwner) { slip ->
            if (slip != null) details.text = formatSlip(slip)
        }

        generate.setOnClickListener {
            val hours = Ui.textFrom(overtime).toDoubleOrNull()
            if (hours == null || hours < 0.0) {
                Toast.makeText(requireContext(), "Enter valid overtime hours", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            generate.isEnabled = false
            vm.generate(ym.year, ym.monthValue, hours,
                onDone = { slip, profile ->
                    if (isAdded) {
                        generate.isEnabled = true
                        details.text = formatSlip(slip)
                        Toast.makeText(requireContext(), "Net salary: ₹%.2f".format(Locale.US, slip.net), Toast.LENGTH_SHORT).show()
                        createPdf(slip, profile) { success -> share.isEnabled = success }
                    }
                },
                onError = { error ->
                    if (isAdded) {
                        generate.isEnabled = true
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        share.setOnClickListener {
            val file = lastPdf ?: return@setOnClickListener
            val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Share salary slip"))
        }
        return scroll
    }

    private fun formatSlip(slip: SalarySlipEntity): String =
        "Basic: ₹%.2f\nHRA: ₹%.2f\nAllowances: ₹%.2f\nOvertime: ₹%.2f\nDeductions: ₹%.2f\nGross: ₹%.2f\nNet Salary: ₹%.2f".format(
            Locale.US, slip.basic, slip.hra, slip.allowance, slip.overtime, slip.deductions, slip.gross, slip.net
        )

    private fun createPdf(slip: SalarySlipEntity, profile: EmployeeProfileEntity?, onComplete: (Boolean) -> Unit) {
        val file = File(requireContext().filesDir, "salary-slip-${slip.year}-${slip.month}.pdf")
        val doc = PdfDocument()
        try {
            val page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
            val canvas = page.canvas
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.color = Color.rgb(49, 87, 213)
            canvas.drawRect(0f, 0f, 595f, 105f, paint)
            paint.color = Color.WHITE
            paint.textSize = 25f
            paint.typeface = Typeface.DEFAULT_BOLD
            canvas.drawText("SALARY SLIP", 40f, 48f, paint)
            paint.textSize = 13f
            paint.typeface = Typeface.DEFAULT
            canvas.drawText("Self Attendance & Salary Slip", 40f, 75f, paint)
            canvas.drawText("Period: %02d/%d".format(Locale.US, slip.month, slip.year), 40f, 94f, paint)

            paint.color = Color.rgb(30, 38, 58)
            paint.textSize = 15f
            paint.typeface = Typeface.DEFAULT_BOLD
            canvas.drawText("Employee", 40f, 145f, paint)
            paint.typeface = Typeface.DEFAULT
            paint.textSize = 13f
            canvas.drawText(profile?.name?.ifBlank { "Employee" } ?: "Employee", 40f, 168f, paint)
            canvas.drawText("ID: ${profile?.employeeId?.ifBlank { "—" } ?: "—"}", 40f, 188f, paint)
            canvas.drawText(profile?.companyName?.ifBlank { "Company" } ?: "Company", 330f, 168f, paint)
            canvas.drawText(profile?.department.orEmpty(), 330f, 188f, paint)

            var y = 235f
            paint.typeface = Typeface.DEFAULT_BOLD
            paint.textSize = 14f
            canvas.drawText("Earnings", 40f, y, paint)
            y += 28f
            paint.typeface = Typeface.DEFAULT
            paint.textSize = 13f
            listOf("Basic salary" to slip.basic, "HRA" to slip.hra, "Allowances" to slip.allowance, "Overtime" to slip.overtime).forEach { (label, amount) ->
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
            FileOutputStream(file).use { doc.writeTo(it) }
            lastPdf = file
            onComplete(true)
        } catch (e: Exception) {
            onComplete(false)
            if (isAdded) Toast.makeText(requireContext(), e.message ?: "Unable to create PDF", Toast.LENGTH_SHORT).show()
        } finally {
            doc.close()
        }
    }
}
