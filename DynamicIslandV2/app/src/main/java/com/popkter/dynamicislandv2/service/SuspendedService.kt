package com.popkter.dynamicislandv2.service

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.popkter.dynamicislandv2.view.DialogActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SuspendedService : LifecycleService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        lifecycleScope.launch {
            delay(100)
            val dialogIntent = Intent(
                this@SuspendedService,
                DialogActivity::class.java
            )
            dialogIntent.flags = FLAG_ACTIVITY_NEW_TASK
            startActivity(dialogIntent)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    val binder = object : Binder() {
        fun showDialog() {

        }
    }


}