package com.popkter.idot

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.annotation.RequiresApi
import com.popkter.idot.util.CommonUtils
import com.popkter.idot.util.WindowManagerUtil

@RequiresApi(Build.VERSION_CODES.R)
class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()

        val windowManagerUtil = WindowManagerUtil(this)
        val view = LayoutInflater.from(this).inflate(R.layout.i_dot, null)
        windowManagerUtil.addView(view, WindowManagerUtil.CUSTOM_LP)
        CommonUtils.checkSuspendedWindowPermission(this) {
            windowManagerUtil.showView()
        }
    }
}