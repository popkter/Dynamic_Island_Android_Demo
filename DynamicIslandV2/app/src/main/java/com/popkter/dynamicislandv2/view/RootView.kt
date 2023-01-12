package com.popkter.dynamicislandv2.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.core.view.marginTop

/**
 * 该自定义ViewGroup仅支持一个ChildView
 */
class RootView constructor(context: Context?, attrs: AttributeSet? = null) :
    ViewGroup(context, attrs) {

    companion object {
        private const val TAG = "RootView"
    }

    private val mScreenWidth: Int
    private val mScreenHeight: Int

    init {
        val wm = getContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            mScreenWidth = wm.defaultDisplay.width
            mScreenHeight = wm.defaultDisplay.height
        } else {
            mScreenWidth = wm.currentWindowMetrics.bounds.width()
            mScreenHeight = wm.currentWindowMetrics.bounds.height()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val childCount = childCount
        for (i in 0 until childCount) {
            val childView: View = getChildAt(i)

            measureChild(childView, widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val width = r - l
        val height = b - t
        for (i in 0 until childCount) {
            val childView: View = getChildAt(i)
            if (childView.isVisible) {
                childView.layout(
                    (width - childView.measuredWidth) / 2,
                    t + paddingTop,
                    (width + childView.measuredWidth) / 2,
                    t + paddingTop + childView.measuredHeight
                )
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        for (i in 0 until childCount) {
            val childView = getChildAt(i)
            if (childView.isVisible) {
                val inWidthRange =
                    ev?.x!! > childView.left.toFloat() && ev.x < (childView.left + childView.measuredWidth).toFloat()
                Log.e(
                    TAG,
                    "dispatchTouchEvent Width: ${ev.x} ${childView.left}  ${childView.left + childView.measuredHeight} $inWidthRange"
                )

                val inHeightRange =
                    ev.y > childView.top.toFloat() && ev.y < (childView.top + childView.measuredHeight).toFloat()
                Log.e(
                    TAG,
                    "dispatchTouchEvent Height: ${ev.y} ${childView.top}  ${childView.top + childView.measuredHeight} $inHeightRange"
                )

                if (inHeightRange && inWidthRange) {
                    super.dispatchTouchEvent(ev)
                }
            }
        }
        Log.e(TAG, "dispatchTouchEvent")
        return true
    }
}