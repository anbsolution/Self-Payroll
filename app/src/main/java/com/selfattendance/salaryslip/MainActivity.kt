package com.selfattendance.salaryslip

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.selfattendance.salaryslip.ui.*

class MainActivity : AppCompatActivity() {
    private val containerId = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, R.color.background)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.surface)
        window.decorView.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
            android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

        val root = FrameLayout(this).apply {
            setBackgroundColor(ContextCompat.getColor(context, R.color.background))
        }

        val navHeight = Ui.dp(this, 88)
        val container = FrameLayout(this).apply { id = containerId }
        root.addView(container, FrameLayout.LayoutParams(-1, -1).apply {
            bottomMargin = navHeight
        })

        val nav = BottomNavigationView(this).apply {
            id = 1002
            inflateMenu(R.menu.bottom_nav)
            labelVisibilityMode = BottomNavigationView.LABEL_VISIBILITY_LABELED
            itemIconSize = Ui.dp(this@MainActivity, 23)
            itemPaddingTop = Ui.dp(this@MainActivity, 7)
            itemPaddingBottom = Ui.dp(this@MainActivity, 3)
            itemIconTintList = ContextCompat.getColorStateList(this@MainActivity, R.color.nav_item_colors)
            itemTextColor = ContextCompat.getColorStateList(this@MainActivity, R.color.nav_item_colors)
            setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.surface))
            elevation = Ui.dp(this@MainActivity, 10).toFloat()
        }
        root.addView(nav, FrameLayout.LayoutParams(-1, navHeight).apply {
            gravity = Gravity.BOTTOM
        })

        setContentView(root)

        nav.setOnItemSelectedListener { item ->
            val f: Fragment = when (item.itemId) {
                R.id.nav_punch -> PunchFragment()
                R.id.nav_attendance -> AttendanceFragment()
                R.id.nav_salary -> SalaryFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> PunchFragment()
            }
            supportFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .replace(containerId, f)
                .commit()
            true
        }
        nav.selectedItemId = R.id.nav_punch
    }
}
