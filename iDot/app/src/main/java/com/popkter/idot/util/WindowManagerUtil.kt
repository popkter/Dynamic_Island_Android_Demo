package com.popkter.idot.util

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import android.view.animation.Interpolator
import androidx.annotation.RequiresApi
import com.popkter.idot.MainActivity
import java.util.concurrent.ConcurrentHashMap

@RequiresApi(Build.VERSION_CODES.R)
class WindowManagerUtil(private val context: Context) {

    companion object {
        const val WIDTH_ANIMATOR = true
        const val HEIGHT_ANIMATOR = false
        private const val TAG = "WindowManagerUtil"
        private val DEFAULT_LP = LayoutParams().apply {
            type = LayoutParams.TYPE_APPLICATION_OVERLAY
            format = PixelFormat.TRANSPARENT
            flags =
                LayoutParams.FLAG_NOT_TOUCH_MODAL or LayoutParams.FLAG_NOT_FOCUSABLE
            width = LayoutParams.MATCH_PARENT
            height = LayoutParams.MATCH_PARENT
        }

        val CUSTOM_LP = LayoutParams().apply {
            type = LayoutParams.TYPE_APPLICATION_OVERLAY
            format = PixelFormat.TRANSPARENT
            flags =
                LayoutParams.FLAG_NOT_TOUCH_MODAL or LayoutParams.FLAG_NOT_FOCUSABLE
            gravity = Gravity.TOP or Gravity.CENTER
            width = LayoutParams.WRAP_CONTENT
            height = LayoutParams.WRAP_CONTENT
        }
    }

    private val viewMap = ConcurrentHashMap<View, LayoutParams>(1)

    private val wm: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    /**
     * the Width of last View
     */
    private var lastWidth = 0

    /**
     * the Height of last View
     */
    private var lastHeight = 0


    /**
     * Add View without layoutParams,and use default layoutParams
     */
    fun addView(view: View): WindowManager {
        viewMap.let {
            it.clear()
            it[view] = DEFAULT_LP
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

    fun showView() {
        wm.addView(viewMap.toList()[0].first, viewMap.toList()[0].second)
        viewMap.toList()[0].first.viewTreeObserver.addOnPreDrawListener(viewTreeObserver)
    }

    fun updateViewHeight() {
        viewMap.toList()[0].first.viewTreeObserver.addOnPreDrawListener(viewTreeObserver)
    }


    private val viewTreeObserver = object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            val newWidth = viewMap.toList()[0].first.width
            val newHeight = viewMap.toList()[0].first.height
            lastWidth = if (newWidth == lastWidth) lastWidth else newWidth
            lastHeight = if (newHeight == lastHeight) lastHeight else newHeight
            viewMap.toList()[0].first.viewTreeObserver.removeOnPreDrawListener(this)
            if (lastWidth != newWidth) {
                updateViewAnimatorWidth(lastWidth, newWidth)
                lastWidth = newWidth
            }
            if (lastHeight != newHeight) {
                updateViewAnimatorHeight(lastHeight, newHeight)
                lastHeight = newHeight
            }

            return true
        }

    }

    private fun updateViewAnimatorWidth(lastValue: Int, nextValue: Int) {
        ValueAnimator.ofInt(
            lastValue,
            nextValue
        ).apply {
            val currentView = viewMap.toList()[0].first
            val currentViewLayoutParams = viewMap.toList()[0].second
            addUpdateListener {
                currentViewLayoutParams.apply {
                    width = it?.animatedValue as Int
                    wm.updateViewLayout(currentView, this)
                }
            }
        }.start()
    }

    private fun updateViewAnimatorHeight(lastValue: Int, nextValue: Int) {
        ValueAnimator.ofInt(
            lastValue,
            nextValue
        ).apply {
            val currentView = viewMap.toList()[0].first
            val currentViewLayoutParams = viewMap.toList()[0].second
            addUpdateListener {
                currentViewLayoutParams.apply {
                    height = it?.animatedValue as Int
                    wm.updateViewLayout(currentView, this)
                }
            }
        }.start()
    }


}