package com.popkter.dynamicislandv2.service

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.LifecycleService

class SuspendedService : LifecycleService() {

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    val binder = object : Binder() {

    }


}