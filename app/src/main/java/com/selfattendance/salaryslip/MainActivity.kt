package com.selfattendance.salaryslip

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.selfattendance.salaryslip.ui.*

class MainActivity : AppCompatActivity() {
    private val containerId = 1001
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = FrameLayout(this).apply { setBackgroundColor(getColor(R.color.background)) }
        val container = FrameLayout(this).apply { id=containerId }
        val nav = BottomNavigationView(this).apply { id=1002; inflateMenu(R.menu.bottom_nav); itemIconTintList=null }
        root.addView(container, FrameLayout.LayoutParams(-1,0).apply{bottomMargin=Ui.dp(this@MainActivity,72)})
        root.addView(nav, FrameLayout.LayoutParams(-1,Ui.dp(this,72)).apply{gravity=android.view.Gravity.BOTTOM})
        setContentView(root)
        nav.setOnItemSelectedListener { item ->
            val f: Fragment = when(item.itemId){R.id.nav_punch->PunchFragment();R.id.nav_attendance->AttendanceFragment();R.id.nav_salary->SalaryFragment();else->ProfileFragment()}
            supportFragmentManager.beginTransaction().replace(containerId,f).commit(); true
        }
        nav.selectedItemId = R.id.nav_punch
    }
}
