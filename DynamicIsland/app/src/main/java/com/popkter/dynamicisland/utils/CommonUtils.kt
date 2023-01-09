package com.popkter.dynamicisland.utils

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import java.util.*

/**
 * @功能: 工具类
 */
object CommonUtils {
    private const val TAG = "CommonUtils"

    private const val REQUEST_FLOAT_CODE = 1001
    private val isVisible = MutableLiveData(false)
    private var canDismiss = false

    /**
     * 判断Service是否开启
     */
    fun isServiceRunning(context: Context, ServiceName: String): Boolean {
        if (TextUtils.isEmpty(ServiceName)) {
            return false
        }
        val myManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningService =
            myManager.getRunningServices(1000) as ArrayList<ActivityManager.RunningServiceInfo>
        for (i in runningService.indices) {
            if (runningService[i].service.className == ServiceName) {
                return true
            }
        }
        return false
    }

    /**
     * 判断悬浮窗权限权限
     */
    private fun commonROMPermissionCheck(context: Context?): Boolean {
        var result = true
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                val clazz: Class<*> = Settings::class.java
                val canDrawOverlays =
                    clazz.getDeclaredMethod("canDrawOverlays", Context::class.java)
                result = canDrawOverlays.invoke(null, context) as Boolean
            } catch (e: Exception) {
                Log.e("ServiceUtils", Log.getStackTraceString(e))
            }
        }
        return result
    }

    /**
     * 检查悬浮窗权限是否开启
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun checkSuspendedWindowPermission(context: Activity, block: () -> Unit) {
        if (commonROMPermissionCheck(context)) {
            block()
        } else {
            Toast.makeText(context, "请开启悬浮窗权限", Toast.LENGTH_SHORT).show()
            context.startActivityForResult(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = Uri.parse("package:${context.packageName}")
            }, REQUEST_FLOAT_CODE)
        }
    }

    /**
     * dp转px
     */
    fun dip2px(context: Context, dpValue: Int): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }


    /**
     * 记录悬浮窗是否开启
     */
    fun isVisible(): Boolean {
        return isVisible.value == true
    }


    fun setVisible(state: Boolean) {
        Log.e(TAG, "setVisible: $state")
        isVisible.postValue(state)
    }

    fun canDismiss(): Boolean {
        return canDismiss
    }

    fun setDismiss(state: Boolean) {
        canDismiss = state
    }

}