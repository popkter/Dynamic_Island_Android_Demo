package com.popkter.dynamicislandv2.common

import android.graphics.PointF
import android.view.animation.Interpolator

/**
 * Author：青蛙要fly
 * url：https://juejin.cn/post/6844903465525739527
 * 缓动三次方曲线插值器.(基于三次方贝塞尔曲线)
 */
class EaseCubicInterpolator() : Interpolator {

    constructor(x1: Double, y1: Double, x2: Double, y2: Double) : this() {
        mControlPoint1.x = x1.toFloat()
        mControlPoint1.y = y1.toFloat()
        mControlPoint2.x = x2.toFloat()
        mControlPoint2.y = y2.toFloat()
    }

    private var mLastI = 0
    private val mControlPoint1 = PointF().apply {
        x = 0.17F
        y = 0.67F
    }

    private val mControlPoint2 = PointF().apply {
        x = 0.83F
        y = 0.67F
    }

    override fun getInterpolation(input: Float): Float {
        var t = input

        // 近似求解t的值[0,1]
        for (i in mLastI until ACCURACY) {
            t = 1.0f * i / ACCURACY
            val x = cubicCurves(
                t.toDouble(),
                0.0,
                mControlPoint1.x.toDouble(),
                mControlPoint2.x.toDouble(),
                1.0
            )
            if (x >= input) {
                mLastI = i
                break
            }
        }
        var value = cubicCurves(
            t.toDouble(),
            0.0,
            mControlPoint1.y.toDouble(),
            mControlPoint2.y.toDouble(),
            1.0
        )
        if (value > 0.999) {
            value = 1.0
            mLastI = 0
        }
        return value.toFloat()
    }

    companion object {
        private const val ACCURACY = 4096

        /**
         * 求三次贝塞尔曲线(四个控制点)一个点某个维度的值.<br></br>
         *
         *
         * 参考资料: * http://devmag.org.za/2011/04/05/bzier-curves-a-tutorial/ *
         *
         * @param t      取值[0, 1]
         * @param value0
         * @param value1
         * @param value2
         * @param value3
         * @return
         */
        fun cubicCurves(
            t: Double, value0: Double, value1: Double,
            value2: Double, value3: Double
        ): Double {
            var value: Double
            val u = 1 - t
            val tt = t * t
            val uu = u * u
            val uuu = uu * u
            val ttt = tt * t
            value = uuu * value0
            value += 3 * uu * t * value1
            value += 3 * u * tt * value2
            value += ttt * value3
            return value
        }
    }
}