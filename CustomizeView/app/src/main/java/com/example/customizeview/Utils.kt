package com.example.customizeview

import android.animation.ValueAnimator
import android.app.Activity
import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.BaseInterpolator
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.animation.addListener
import androidx.core.content.getSystemService
import androidx.lifecycle.LifecycleService
import java.util.*

/**
 * @功能: 工具类
 * @User Lmy
 * @Creat 4/16/21 8:33 AM
 * @Compony 永远相信美好的事情即将发生
 */
object Utils {
    const val REQUEST_FLOAT_CODE = 1001

    /**
     * 跳转到设置页面申请打开无障碍辅助功能
     */
    private fun accessibilityToSettingPage(context: Context) {
        //开启辅助功能页面
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            e.printStackTrace()
        }
    }

    /**
     * 判断Service是否开启
     *
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
     * 属性变化的动画
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    fun dynamicViewChange(
        context: Context,
        floatRootView: View,
        /**
         * 视图想要变化的属性的初始值
         */
        startValue: Int,
        /**
         * 视图想要变化的属性的目标值
         */
        endValue: Int,
        /**
         * 动画执行时长
         */
        timeout: Long,
        /**
         * 指定想要变化的属性
         * [WIDTH_ANIMATOR] 宽
         * [HEIGHT_ANIMATOR] 高
         */
        isWidthAnimator: Boolean,
        /**
         * 指定插值器
         */
        animatorType: BaseInterpolator,
        /**
         * 指定动画结束执行的操作，偶发问题，非必须
         */
        function: (() -> Unit)?
    ) {
        ValueAnimator.ofInt(
            dip2px(context, startValue.toFloat()),
            dip2px(context, endValue.toFloat())
        ).apply {
            addUpdateListener {
//                Log.e(TAG, "dynamicViewChange: ${it.animatedValue}")
                floatRootView.layoutParams.apply {
                    if (isWidthAnimator) {
                        width = it?.animatedValue as Int
                    } else {
                        height = it?.animatedValue as Int
                    }
                }.let { params ->
                    // val wm = context.getSystemService(WINDOW_SERVICE) as WindowManager
                    // wm.updateViewLayout(floatRootView, params)
                    floatRootView.layoutParams = params
                }

            }

            addListener(
                onStart = {
                    if (startValue < endValue) {
                        if (function != null) {
                            function()
                        }
                    }
                },

                onEnd = {
                    if (startValue > endValue) {
                        if (function != null) {
                            function()
                        }
                    }
                }
            )
            interpolator = animatorType
            duration = timeout
        }.start()
    }


    fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }
}