package com.popkter.dynamicislandv2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.popkter.dynamicislandv2.utils.SuspendedWindowUtil
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        val suspendedWindowUtil = SuspendedWindowUtil(this,this)
        supportActionBar?.hide()
        suspendedWindowUtil.initWindow(
            R.layout.suspend_window,
            R.id.scene_root
        )
        lifecycleScope.launch {
            suspendedWindowUtil.showWindow()
        }
    }
}