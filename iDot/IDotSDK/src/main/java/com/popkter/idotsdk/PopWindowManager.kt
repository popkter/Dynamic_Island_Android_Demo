package com.popkter.idotsdk

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.provider.Settings
import android.transition.Scene
import android.util.Log
import android.view.*
import android.view.View.MeasureSpec
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager.LayoutParams
import android.view.animation.AnticipateInterpolator
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.core.animation.addListener
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.popkter.idotsdk.databinding.IDotBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PopWindowManager(private val context: Context, private val lifecycleOwner: LifecycleOwner) {

    companion object {
        private const val TAG = "com.popkter.idot.WindowManagerUtil"

        val CUSTOM_LP = LayoutParams().apply {
            type = LayoutParams.TYPE_APPLICATION_OVERLAY
            format = PixelFormat.TRANSPARENT
            flags =
                LayoutParams.FLAG_NOT_TOUCH_MODAL or LayoutParams.FLAG_NOT_FOCUSABLE or LayoutParams.FLAG_HARDWARE_ACCELERATED or LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            gravity = Gravity.TOP or Gravity.CENTER
            width = WRAP_CONTENT
            height = WRAP_CONTENT
            windowAnimations = androidx.constraintlayout.widget.R.anim.abc_grow_fade_in_from_bottom
        }
    }

    private val windowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private val layoutInflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    /**
     * the view last come in
     */
    private lateinit var lastView: View

    private lateinit var currentView: View

    /**
     *  whether display ASR
     */
    private var isShowAsr = false

    /**
     * the Width of ASR
     */
    private var initWidth = 0

    /**
     * the Height of ASR
     */
    private var initHeight = 0

    /**
     * the Width of last View
     */
    private var lastWidth = 10

    /**
     * the Height of last View
     */
    private var lastHeight = 10

    /**
     * suspension window rootView Binding
     */
    private lateinit var iDotBinding: IDotBinding

    private lateinit var viewTreeObserver: ViewTreeObserver

    private lateinit var sceneRoot: ViewGroup

    /**
     * 1. please run [PopWindowManager.Utils.checkSuspensionWindowPermission] before init !!!
     * 2. Add root-view to suspension window
     */
    fun init() {
        init(false, CUSTOM_LP)
    }

    /**
     * 1. please run [PopWindowManager.Utils.checkSuspensionWindowPermission] before init !!!
     * 2. Add root-view to suspension window, And Set Asr Status meanwhile
     * @param showASR whether ASR display is enabled
     */
    fun init(showASR: Boolean) {
        init(false, CUSTOM_LP)
    }

    /**
     * 1. please run [PopWindowManager.Utils.checkSuspensionWindowPermission] before init !!!
     * 2. Add root-view to suspension window With Customized LayoutParams
     * @param layoutParams root view layout-params
     */
    fun init(layoutParams: LayoutParams) {
        init(false, layoutParams)
    }

    /**
     * 1. please run [PopWindowManager.Utils.checkSuspensionWindowPermission] before init !!!
     * 2. Add root-view to suspension window With Customized LayoutParams And Set Asr Status meanwhile
     * @param showASR display ASR or dismiss it
     * @param layoutParams root view layout-params
     */
    fun init(showASR: Boolean, layoutParams: LayoutParams) {
        isShowAsr = showASR
        iDotBinding = IDotBinding.inflate(layoutInflater)
        windowManager.addView(iDotBinding.root, layoutParams)
        iDotBinding.root.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_OUTSIDE){
                dismiss()
            }
            true
        }
        sceneRoot = iDotBinding.viewContainer
        val scnen = Scene.getSceneForLayout()
        iDotBinding.root.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        initHeight = iDotBinding.asr.measuredHeight
        initWidth = iDotBinding.asr.measuredWidth
    }

    /**
     * Disappear suspension windows
     */
    fun dismiss() {
        updateViewAnimatorRectangle(0, 0, LinearInterpolator())
        iDotBinding.viewContainer.removeAllViews()
    }

    /**
     * Add a childView to [iDotBinding.viewContainer](FrameLayout)
     * @param childView a ChildView
     */
    fun showView(childView: View) {
        showView(childView,null)
    }

    /**
     * Add a childView to [iDotBinding.viewContainer](FrameLayout) with Interpolator
     * @param childView a ChildView
     */
    fun showView(childView: View, interpolatorType: Interpolator?) {
        if (!this::viewTreeObserver.isInitialized) {
            viewTreeObserver = iDotBinding.root.viewTreeObserver
        }
        iDotBinding.viewContainer.addView(childView)
        if (this::currentView.isInitialized) {
            iDotBinding.viewContainer.removeView(currentView)
        }
        currentView = childView
        childView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        updateViewRectangle(
            childView.measuredHeight,
            childView.measuredWidth,
            interpolatorType
        ) {
        }
    }

    /**
     * Remove root-view From WindowManager
     */
    fun destory() {
        dismiss()
        windowManager.removeView(iDotBinding.root)
    }

    private fun updateViewRectangle(
        height: Int,
        width: Int,
        interpolatorType: Interpolator?,
        block: () -> Unit
    ) {
        updateViewAnimatorRectangle(height, width, interpolatorType)
    }

    private fun updateViewAnimatorRectangle(
        nextHeight: Int,
        nextWidth: Int,
        interpolatorType: Interpolator?
    ) {

        val heightAnimator = if (lastHeight != nextHeight) {
//            Log.e(TAG, "updateViewAnimatorRectangleHeight: $lastHeight $nextHeight")
            ValueAnimator.ofInt(lastHeight, nextHeight).apply {
                addUpdateListener {
                    iDotBinding.root.layoutParams.apply {
                        height = it?.animatedValue as Int
                        windowManager.updateViewLayout(
                            iDotBinding.root, iDotBinding.root.layoutParams
                        )
                    }
                }
                interpolator = interpolatorType ?: OvershootInterpolator(1F)
            }
        } else null

        val widthAnimator = if (lastWidth != nextWidth) {
//            Log.e(TAG, "updateViewAnimatorRectangleWidth: $lastWidth $nextWidth")
            ValueAnimator.ofInt(lastHeight, nextWidth).apply {
                addUpdateListener {
                    iDotBinding.root.layoutParams.apply {
                        width = it.animatedValue as Int
                        lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                            windowManager.updateViewLayout(
                                iDotBinding.root, iDotBinding.root.layoutParams
                            )
                        }
                    }
                }
                interpolator = interpolatorType ?: OvershootInterpolator(1F)
            }
        } else null

        val animatorSet = AnimatorSet()

        if (heightAnimator != null) {
            if (widthAnimator != null) {
                animatorSet.playTogether(heightAnimator, widthAnimator)
            } else {
                animatorSet.playTogether(heightAnimator)
            }
        } else {
            if (widthAnimator != null) {
                animatorSet.playTogether(widthAnimator)
            }
        }
        animatorSet.duration = 400
        animatorSet.start()

        lastHeight = nextHeight
        lastWidth = nextWidth

        if (this::lastView.isInitialized) {
            iDotBinding.viewContainer.removeView(lastView)
        }
        if (this::currentView.isInitialized) {
            lastView = currentView
        }
    }


    /**
     * Calculate how long the animation should run
     * @param startValue
     * @param endValue
     */
    private fun countDuration(startValue: Int, endValue: Int) {

    }

    object Utils {
        /**
         * 判断悬浮窗权限权限
         */
        private fun commonROMPermissionCheck(context: Context?): Boolean {
            var result = true
            try {
                val clazz: Class<*> = Settings::class.java
                val canDrawOverlays =
                    clazz.getDeclaredMethod("canDrawOverlays", Context::class.java)
                result = canDrawOverlays.invoke(null, context) as Boolean
            } catch (e: Exception) {
                Log.e("ServiceUtils", Log.getStackTraceString(e))
            }
            return result
        }

        /**
         * Check whether the suspension window permission is enabled
         */
        fun checkSuspensionWindowPermission(context: Activity, block: () -> Unit) {
            if (commonROMPermissionCheck(context)) {
                block()
            } else {
                Toast.makeText(context, "请开启悬浮窗权限", Toast.LENGTH_SHORT).show()
                context.startActivityForResult(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                    data = Uri.parse("package:${context.packageName}")
                }, 1001)
            }
        }

        /**
         * DP转PX
         */
        fun dip2px(context: Context, dpValue: Int): Int {
            val scale = context.resources.displayMetrics.density
            return (dpValue * scale + 0.5F).toInt()
        }
    }
}