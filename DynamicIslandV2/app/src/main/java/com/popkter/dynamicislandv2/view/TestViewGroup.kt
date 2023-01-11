package com.popkter.dynamicislandv2.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Scroller

class TestViewGroup @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null) :
    ViewGroup(context, attrs) {
    private val mScreenHeight: Int
    private var mStartY = 0
    private var mEnd = 0
    private val mScroller: Scroller
    private var mLastY = 0
    private var childCounts = 0

    init {
        //初始化一些需要的属性
        val wm = getContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mScreenHeight = wm.defaultDisplay.height
        mScroller = Scroller(getContext())
    }

    //在onMeasure中测量子view
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val childCount = getChildCount()
        for (i in 0 until childCount) {
            val childView: View = getChildAt(i)
            measureChild(childView, widthMeasureSpec, heightMeasureSpec)
        }
    }

    //确定子View的位子
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        childCounts = childCount
        //设置这个ViewGroup的高度
        val lp = layoutParams as MarginLayoutParams
        lp.height = mScreenHeight * childCount
        layoutParams = lp
        //绘制子view的位置
        for (i in 0 until childCount) {
            val childView: View = getChildAt(i)
            if (childView.visibility !== View.GONE) {
                childView.layout(l, i * mScreenHeight, r, (i + 1) * mScreenHeight)
            }
        }
    }

    //step3：增添我们需要的触摸响应事件
    override fun onTouchEvent(event: MotionEvent): Boolean {
        //在这个触摸事件中，需要判断两个距离，一个是手指移动的距离一个是view滚动的距离
        //这是随着手指的移动会发送改变的量
        val y = event.y.toInt()
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mLastY = y
                mStartY = scrollY
            }
            MotionEvent.ACTION_MOVE -> {
                //当我们再次触碰屏幕时，如果之前的滚动动画还没有停止，我们也让他立即停止
                if (!mScroller.isFinished) {
                    mScroller.abortAnimation()
                }
                var dY = mLastY - y
                //滚动触碰到上边缘时一给个下拉反弹的效果
                if (scrollY < 0) {
                    dY /= 3
                }
                //判断滚动的
                if (scrollY > mScreenHeight * getChildCount() - mScreenHeight) {
                    dY = 0
                }
                //让我们的view滚动相应的dy距离
                scrollBy(0, dY)
                mLastY = y
            }
            MotionEvent.ACTION_UP -> {
                mEnd = scrollY
                val dScrollY = mEnd - mStartY
                if (dScrollY > 0) { //向上滚动的情况
                    if (scrollY < 0) {
                        mScroller.startScroll(0, scrollY, 0, -dScrollY)
                    } else {
                        if (dScrollY < mScreenHeight / 3) {
                            mScroller.startScroll(0, scrollY, 0, -dScrollY)
                        } else {
                            mScroller.startScroll(0, scrollY, 0, mScreenHeight - dScrollY)
                        }
                    }
                } else { //向下滚动的情况
                    if (scrollY > mScreenHeight * getChildCount() - mScreenHeight) {
                        mScroller.startScroll(0, scrollY, 0, -dScrollY)
                    } else {
                        if (-dScrollY < mScreenHeight / 3) {
                            mScroller.startScroll(0, scrollY, 0, -dScrollY)
                        } else {
                            mScroller.startScroll(0, scrollY, 0, -mScreenHeight - dScrollY)
                        }
                    }
                }
            }
        }
        //重绘界面
        postInvalidate()
        return true
    }

    //控制滑屏控制
    override fun computeScroll() {
        super.computeScroll()
        if (mScroller.computeScrollOffset()) {
            scrollTo(0, mScroller.currY)
            postInvalidate()
        }
    }
}