package com.popkter.dynamicislandv2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.popkter.dynamicislandv2.utils.SuspendedWindowUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        val suspendedWindowUtil = SuspendedWindowUtil(this)
        supportActionBar?.hide()
        suspendedWindowUtil.initWindow(
            R.layout.suspend_window,
            R.id.root_view
        )
        lifecycleScope.launch {
            suspendedWindowUtil.showWindow()
        }
    }
}