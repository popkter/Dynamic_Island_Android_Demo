package com.popkter.dynamicislandv2.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.util.concurrent.ConcurrentHashMap

class WindowManagerUtil(private val context: Context) {

    private val viewMap = ConcurrentHashMap<View, LayoutParams>(1)
    private var wm: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val defaultLp = LayoutParams().apply {
        type = LayoutParams.TYPE_APPLICATION_OVERLAY
        format = PixelFormat.TRANSPARENT
        flags =
            LayoutParams.FLAG_NOT_TOUCH_MODAL or LayoutParams.FLAG_NOT_FOCUSABLE
        width = LayoutParams.MATCH_PARENT
        height = LayoutParams.MATCH_PARENT
    }

    private val customizedLp = LayoutParams().apply {
        type = LayoutParams.TYPE_APPLICATION_OVERLAY
        format = PixelFormat.TRANSPARENT
        flags =
            LayoutParams.FLAG_NOT_TOUCH_MODAL or LayoutParams.FLAG_NOT_FOCUSABLE
        width = LayoutParams.WRAP_CONTENT
        height = LayoutParams.WRAP_CONTENT
    }

    /**
     * Add View without layoutParams,and use default layoutParams
     */
    fun addView(view: View): WindowManager {
        viewMap.let {
            it.clear()
            it[view] = defaultLp
        }
        return wm
    }

    /**
     * Add View with customized layoutParams
     */
    fun addView(view: View, layoutParams: LayoutParams): WindowManager {
        viewMap.let {
            it.clear()
            it[view] = layoutParams
        }
        return wm
    }

    fun getCustomizedLp(): LayoutParams {
        return customizedLp
    }

    fun showView() {
        checkSuspendedWindowPermission {
            viewMap.iterator().forEach {
                wm.addView(it.key, it.value)
            }
        }
    }


    /**
     * 检查悬浮窗权限是否开启
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkSuspendedWindowPermission(block: () -> Unit?) {
        if (commonROMPermissionCheck(context)) {
            block()
        } else {
            Toast.makeText(context, "请开启悬浮窗权限", Toast.LENGTH_SHORT).show()
            (context as Activity).startActivityForResult(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = Uri.parse("package:${context.packageName}")
            }, CommonUtils.REQUEST_FLOAT_CODE)
        }
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
}