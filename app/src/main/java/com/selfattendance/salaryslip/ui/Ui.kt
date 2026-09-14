package com.selfattendance.salaryslip.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.selfattendance.salaryslip.R

object Ui {
    fun scroll(context: Context): ScrollView = ScrollView(context).apply { setBackgroundColor(ContextCompat.getColor(context, R.color.background)); isFillViewport = true }
    fun column(context: Context, padding: Int = 20): LinearLayout = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(context,padding), dp(context, padding), dp(context,padding), dp(context,padding)); layoutParams = LinearLayout.LayoutParams(-1,-2) }
    fun title(context: Context, text: String): TextView = TextView(context).apply { this.text=text; textSize=28f; setTextColor(ContextCompat.getColor(context,R.color.text_primary)); typeface=Typeface.DEFAULT_BOLD; setPadding(0,0,0,dp(context,8)) }
    fun subtitle(context: Context, text: String): TextView = TextView(context).apply { this.text=text; textSize=14f; setTextColor(ContextCompat.getColor(context,R.color.text_secondary)); setPadding(0,0,0,dp(context,14)) }
    fun card(context: Context): MaterialCardView = MaterialCardView(context).apply { radius=dp(context,18).toFloat(); cardElevation=dp(context,2).toFloat(); setCardBackgroundColor(Color.WHITE); useCompatPadding=true; layoutParams=LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=dp(context,14)} }
    fun label(context: Context,text:String)=TextView(context).apply{this.text=text;textSize=13f;setTextColor(ContextCompat.getColor(context,R.color.text_secondary))}
    fun value(context: Context,text:String)=TextView(context).apply{this.text=text;textSize=19f;typeface=Typeface.DEFAULT_BOLD;setTextColor(ContextCompat.getColor(context,R.color.text_primary));setPadding(0,4,0,4)}
    fun button(context: Context,text:String, primary:Boolean=true)=Button(context).apply{this.text=text;textSize=14f;isAllCaps=false;minHeight=dp(context,52);background=ContextCompat.getDrawable(context,if(primary)R.drawable.bg_primary_button else R.drawable.bg_outline_button);setTextColor(if(primary)Color.WHITE else ContextCompat.getColor(context,R.color.primary))}
    fun input(context: Context,hint:String,value:String=""): TextInputLayout { val box=TextInputLayout(context); box.hint=hint; box.boxCornerRadiusTopStart=14f;box.boxCornerRadiusTopEnd=14f;box.boxCornerRadiusBottomStart=14f;box.boxCornerRadiusBottomEnd=14f; val e=TextInputEditText(context);e.setText(value);box.addView(e);box.layoutParams=LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=dp(context,10)};return box }
    fun textFrom(box:TextInputLayout)=box.editText?.text?.toString()?.trim().orEmpty()
    fun row(context:Context,vararg views:View):LinearLayout=LinearLayout(context).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;views.forEach{addView(it,LinearLayout.LayoutParams(0,-2,1f).apply{rightMargin=dp(context,8)})}}
    fun dp(c:Context,v:Int)= (v*c.resources.displayMetrics.density).toInt()
}
