package com.popkter.dynamicislandv2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.popkter.dynamicislandv2.databinding.ActivityMainBinding
import com.popkter.dynamicislandv2.service.SuspendedService
import com.popkter.dynamicislandv2.view.DialogActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var activityMainBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
        supportActionBar?.hide()
    }

    override fun onResume() {
        super.onResume()

        startService(Intent(this, SuspendedService::class.java))


        /* val suspendedWindowUtil = SuspendedWindowUtil(this,this)
         supportActionBar?.hide()
         suspendedWindowUtil.initWindow(
             R.layout.suspend_window,
             R.id.scene_root
         )
         lifecycleScope.launch {
             suspendedWindowUtil.showWindow()
         }*/
    }
}