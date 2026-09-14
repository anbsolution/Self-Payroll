package com.selfattendance.salaryslip.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Space
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.selfattendance.salaryslip.R

object Ui {
    fun scroll(context: Context): ScrollView = ScrollView(context).apply {
        setBackgroundColor(ContextCompat.getColor(context, R.color.background))
        isFillViewport = true
        clipToPadding = false
        setPadding(0, 0, 0, dp(context, 20))
        overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS
    }

    fun column(context: Context, padding: Int = 20): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(context, padding), dp(context, 18), dp(context, padding), dp(context, 18))
        layoutParams = LinearLayout.LayoutParams(-1, -2)
    }

    fun title(context: Context, text: String): TextView = TextView(context).apply {
        this.text = text
        textSize = 29f
        setTextColor(ContextCompat.getColor(context, R.color.text_primary))
        typeface = Typeface.create("sans", Typeface.BOLD)
        setPadding(0, dp(context, 4), 0, dp(context, 3))
    }

    fun subtitle(context: Context, text: String): TextView = TextView(context).apply {
        this.text = text
        textSize = 14f
        setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
        setPadding(0, 0, 0, dp(context, 18))
        includeFontPadding = false
    }

    fun sectionLabel(context: Context, text: String): TextView = TextView(context).apply {
        this.text = text
        textSize = 12f
        letterSpacing = 0.08f
        setTextColor(ContextCompat.getColor(context, R.color.primary))
        typeface = Typeface.create("sans", Typeface.BOLD)
        setPadding(0, 0, 0, dp(context, 8))
    }

    fun card(context: Context): MaterialCardView = MaterialCardView(context).apply {
        radius = dp(context, 20).toFloat()
        cardElevation = dp(context, 1).toFloat()
        setCardBackgroundColor(ContextCompat.getColor(context, R.color.surface))
        strokeWidth = dp(context, 1)
        strokeColor = ContextCompat.getColor(context, R.color.card_stroke)
        preventCornerOverlap = true
        layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(context, 14) }
    }

    fun cardInner(context: Context, padding: Int = 18): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(context, padding), dp(context, padding), dp(context, padding), dp(context, padding))
        layoutParams = LinearLayout.LayoutParams(-1, -2)
    }

    fun label(context: Context, text: String): TextView = TextView(context).apply {
        this.text = text
        textSize = 12f
        letterSpacing = 0.05f
        typeface = Typeface.create("sans", Typeface.BOLD)
        setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
        includeFontPadding = false
    }

    fun value(context: Context, text: String): TextView = TextView(context).apply {
        this.text = text
        textSize = 19f
        typeface = Typeface.create("sans", Typeface.BOLD)
        setTextColor(ContextCompat.getColor(context, R.color.text_primary))
        setPadding(0, dp(context, 6), 0, dp(context, 4))
        includeFontPadding = false
    }

    fun bigValue(context: Context, text: String): TextView = TextView(context).apply {
        this.text = text
        textSize = 32f
        typeface = Typeface.create("sans", Typeface.BOLD)
        setTextColor(ContextCompat.getColor(context, R.color.primary))
        setPadding(0, dp(context, 5), 0, dp(context, 2))
        includeFontPadding = false
    }

    fun muted(context: Context, text: String): TextView = TextView(context).apply {
        this.text = text
        textSize = 13f
        setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
        setPadding(0, dp(context, 3), 0, dp(context, 3))
        includeFontPadding = false
    }

    fun button(context: Context, text: String, primary: Boolean = true): Button = Button(context).apply {
        this.text = text
        textSize = 14f
        isAllCaps = false
        minHeight = dp(context, 52)
        minimumHeight = dp(context, 52)
        stateListAnimator = null
        typeface = Typeface.create("sans", Typeface.BOLD)
        setPadding(dp(context, 18), 0, dp(context, 18), 0)
        setBackgroundResource(if (primary) R.drawable.bg_primary_button else R.drawable.bg_outline_button)
        setTextColor(if (primary) Color.WHITE else ContextCompat.getColor(context, R.color.primary))
        layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(context, 10) }
    }

    fun input(context: Context, hint: String, value: String = ""): TextInputLayout {
        val box = TextInputLayout(context).apply {
            this.hint = hint
            setBoxCornerRadii(dp(context, 14).toFloat(), dp(context, 14).toFloat(), dp(context, 14).toFloat(), dp(context, 14).toFloat())
            boxStrokeWidth = dp(context, 1)
            boxStrokeWidthFocused = dp(context, 2)
            setBoxBackgroundColor(ContextCompat.getColor(context, R.color.surface))
            setBoxStrokeColor(ContextCompat.getColor(context, R.color.card_stroke))
        }
        val edit = TextInputEditText(context).apply {
            setText(value)
            textSize = 16f
            setTextColor(ContextCompat.getColor(context, R.color.text_primary))
        }
        box.addView(edit)
        box.layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(context, 10) }
        return box
    }

    fun textFrom(box: TextInputLayout): String = box.editText?.text?.toString()?.trim().orEmpty()

    fun row(context: Context, vararg views: View): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        views.forEachIndexed { index, view ->
            addView(view, LinearLayout.LayoutParams(0, -2, 1f).apply {
                if (index < views.lastIndex) rightMargin = dp(context, 8)
            })
        }
    }

    fun divider(context: Context): View = View(context).apply {
        setBackgroundColor(ContextCompat.getColor(context, R.color.card_stroke))
        layoutParams = LinearLayout.LayoutParams(-1, dp(context, 1)).apply {
            topMargin = dp(context, 10)
            bottomMargin = dp(context, 10)
        }
    }

    fun spacer(context: Context, height: Int): Space = Space(context).apply {
        layoutParams = LinearLayout.LayoutParams(1, dp(context, height))
    }

    fun dp(context: Context, value: Int): Int = (value * context.resources.displayMetrics.density).toInt()
}
