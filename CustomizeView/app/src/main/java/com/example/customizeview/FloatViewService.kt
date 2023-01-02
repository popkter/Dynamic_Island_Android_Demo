package com.example.customizeview

import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleService

class FloatViewService : LifecycleService() {

    companion object {
        const val TAG = "FloatViewService"
    }

    override fun onCreate() {
        Log.e(TAG, "onCreate: ")
        super.onCreate()
    }

    override fun onStart(intent: Intent?, startId: Int) {
        super.onStart(intent, startId)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }

    override fun getLifecycle(): Lifecycle {
        return super.getLifecycle()
    }
}