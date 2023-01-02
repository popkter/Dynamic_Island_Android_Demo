package com.example.customizeview

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.util.DisplayMetrics
import android.view.*
import android.view.accessibility.AccessibilityEvent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.example.customizeview.Utils.isNull

/**
 * @功能:利用无障碍打开悬浮窗口 无局限性 任何界面可以显示
 * @User Lmy
 * @Creat 4/15/21 5:57 PM
 * @Compony 永远相信美好的事情即将发生
 */
class WorkAccessibilityService : AccessibilityService(), LifecycleOwner {
    private lateinit var windowManager: WindowManager
    private var floatRootView: View? = null//悬浮窗View
    private val mLifecycleRegistry = LifecycleRegistry(this)
    override fun onCreate() {
        super.onCreate()
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);
        initObserve()
    }

    /**
     * 打开关闭的订阅
     */
    private fun initObserve() {
        ViewModleMain.isShowWindow.observe(this) {
            if (it) {
                showWindow()
            } else {
                if (!isNull(floatRootView)) {
                    if (!isNull(floatRootView?.windowToken)) {
                        if (!isNull(windowManager)) {
                            windowManager.removeView(floatRootView)
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showWindow() {
        // 设置LayoutParam
        // 获取WindowManager服务
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val outMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(outMetrics)
        var layoutParam = WindowManager.LayoutParams()
        layoutParam.apply {
            //显示的位置
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
                //刘海屏延伸到刘海里面
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    layoutInDisplayCutoutMode =
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            } else {
                type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            }
            flags =
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            format = PixelFormat.TRANSPARENT
        }
        floatRootView = LayoutInflater.from(this).inflate(R.layout.activity_main, null)
        floatRootView?.setOnTouchListener(ItemViewTouchListener(layoutParam, windowManager))
        windowManager.addView(floatRootView, layoutParam)
    }


    override fun onServiceConnected() {
        super.onServiceConnected()
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    override fun getLifecycle(): Lifecycle = mLifecycleRegistry
    override fun onStart(intent: Intent?, startId: Int) {
        super.onStart(intent, startId)
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
    }
}