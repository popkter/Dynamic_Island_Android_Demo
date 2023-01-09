package com.popkter.dynamicisland.service

import CustomizedAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.Image
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager.LayoutParams.*
import android.view.animation.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.animation.addListener
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Scene
import androidx.transition.TransitionInflater
import androidx.transition.TransitionManager
import com.popkter.dynamicisland.R
import com.popkter.dynamicisland.utils.CommonUtils
import kotlinx.coroutines.launch

class DynamicIslandService : LifecycleService(), View.OnLayoutChangeListener {

    companion object {
        const val TAG = "SuspendWindowService"
        const val WIDTH_ANIMATOR = true
        const val HEIGHT_ANIMATOR = false
        const val EMPTY_VIEW = ""
        const val TOAST_WITHOUT_TIP = "toast_without_tip"
        const val TOAST_WITH_TIP = "toast_with_tip"
        const val IMAGE_VIEW = "image_view"
        const val RECYCLER_VIEW = "recycler_view"
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private val viewBinder = lazy(LazyThreadSafetyMode.SYNCHRONIZED) { ViewBinder() }
    private val currentViewFlag = MutableLiveData(EMPTY_VIEW)
    private val isLongAsr = MutableLiveData(false)

    /**
     * 其变化开始和结束速率较慢，中间加速
     */
    private val accelerateDecelerateInterpolator = AccelerateDecelerateInterpolator()

    /**
     * 其变化开始速率较慢，后面加速
     */
    private val accelerateInterpolator = AccelerateDecelerateInterpolator()

    /**
     * 其变化开始速率较快，后面减速
     */
    private val decelerateInterpolator = DecelerateInterpolator()

    /**
     * 结束后顺着结束的运行规律让然运行一段时间
     */
    private val overshootInterpolator = OvershootInterpolator()

    /**
     * 沿着开始相反的方向先运行
     */
    private val anticipateInterpolator = AnticipateInterpolator()

    /**
     * AnticipateInterpolator 和 OvershootInterpolator 的结合
     */
    private val anticipateOvershootInterpolator = AnticipateOvershootInterpolator()

    /**
     * 其变化速率恒定
     */
    private val linearInterpolator = LinearInterpolator()

    /**
     * 其速率为正弦曲线
     */
    private val cycleInterpolator = CycleInterpolator(2.5f)

    /**
     * 其变化先匀速再减速
     */
    private val linearOutSlowInInterpolator = LinearOutSlowInInterpolator()

    /**
     * 其变化是先加速，然后减速
     */
    private val fastOutSlowInInterpolator = FastOutSlowInInterpolator()

    /**
     * 其变化先加速然后匀速，本质还是加速运动
     */
    private val fastOutLinearInInterpolator = FastOutLinearInInterpolator()

    /**
     * 自由落体规律运动
     */
    private val bounceInterpolator = BounceInterpolator()

    private var isVisibleImageView = MutableLiveData(false)
    private var isVisibleToast = MutableLiveData(false)
    private var isVisibleRecyclerView = MutableLiveData(false)
    private var isInit = false

    private val lastHeight = MutableLiveData(0)
    private val lastWidth = MutableLiveData(0)

    private lateinit var floatRootView: View
    private lateinit var windowManager: WindowManager
    private lateinit var layoutParam: WindowManager.LayoutParams


    /**
     * 属性变化的动画
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private fun dynamicViewChange(
        context: Context,
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
        animatorType: Interpolator,
        /**
         * 指定动画结束执行的操作，偶发问题，非必须
         */
        function: (() -> Unit)?
    ) {
        ValueAnimator.ofInt(
            CommonUtils.dip2px(context, startValue),
            CommonUtils.dip2px(context, endValue),
        ).apply {
            addUpdateListener {
//                Log.e(TAG, "dynamicViewChange: ${it.animatedValue}")
                layoutParam.apply {
                    if (isWidthAnimator) {
                        width = it?.animatedValue as Int
                    } else {
                        height = it?.animatedValue as Int
                    }
                }.let { params ->
                    windowManager.updateViewLayout(floatRootView, params)
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


    override fun onCreate() {
        super.onCreate()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return viewBinder.value
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun initWindows() {
        windowManager = application.getSystemService(WINDOW_SERVICE) as WindowManager
        layoutParam = WindowManager.LayoutParams().apply {
            type = TYPE_APPLICATION_OVERLAY
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metrics)
            format = PixelFormat.RGBA_8888
            flags = FLAG_NOT_TOUCH_MODAL or FLAG_NOT_FOCUSABLE or FLAG_WATCH_OUTSIDE_TOUCH
            width = CommonUtils.dip2px(this@DynamicIslandService, 240)
            height = WRAP_CONTENT
            gravity = Gravity.CENTER or Gravity.TOP
            y = 20
        }

        floatRootView =
            LayoutInflater.from(this@DynamicIslandService).inflate(R.layout.dynamic_island, null)
        val asr = floatRootView.rootView.findViewById<TextView>(R.id.asr)
        val imageView = floatRootView.rootView.findViewById<ImageView>(R.id.image_view)
        val recyclerView = floatRootView.rootView.findViewById<RecyclerView>(R.id.recycler_view)
        val tips = floatRootView.rootView.findViewById<ViewGroup>(R.id.tips)
        asr.text = "This is Dynamic Island Demo!"

        floatRootView.setOnTouchListener { _, event ->
            if (event?.action == MotionEvent.ACTION_OUTSIDE) {
                if (CommonUtils.isVisible()) {
                    removeWindows()
                }
                true
            } else
                false
        }

        floatRootView.addOnLayoutChangeListener(this)

        asr.setOnClickListener {
            if (isVisibleRecyclerView.value == true) {
                dynamicViewChange(
                    this@DynamicIslandService,
                    startValue = 280,
                    endValue = 40,
                    isWidthAnimator = HEIGHT_ANIMATOR,
                    animatorType = OvershootInterpolator(1f),
                    timeout = 600,
                    function = null
                )
                dynamicViewChange(
                    this@DynamicIslandService,
                    startValue = 280,
                    endValue = 240,
                    isWidthAnimator = WIDTH_ANIMATOR,
                    animatorType = OvershootInterpolator(1f),
                    timeout = 300,
                    function = null
                )
                isLongAsr.postValue(false)
                isVisibleRecyclerView.postValue(false)
                isVisibleImageView.postValue(false)
            } else if (isVisibleImageView.value == true) {
                dynamicViewChange(
                    this@DynamicIslandService,
                    startValue = 280,
                    endValue = 40,
                    isWidthAnimator = HEIGHT_ANIMATOR,
                    animatorType = OvershootInterpolator(1f),
                    timeout = 600,
                    function = null
                )
                dynamicViewChange(
                    this@DynamicIslandService,
                    startValue = 260,
                    endValue = 240,
                    isWidthAnimator = WIDTH_ANIMATOR,
                    animatorType = OvershootInterpolator(1f),
                    timeout = 300,
                    function = null
                )
                isLongAsr.postValue(false)
                isVisibleRecyclerView.postValue(false)
                isVisibleImageView.postValue(false)
            } else {
                isLongAsr.value.let { state ->
                    dynamicViewChange(
                        this@DynamicIslandService,
                        startValue = if (state == false) 40 else 80,
                        endValue = if (state == false) 80 else 40,
                        isWidthAnimator = HEIGHT_ANIMATOR,
                        animatorType = overshootInterpolator,
                        timeout = if (state == false) 600 else 300,
                    ) {
                        isLongAsr.postValue(!state!!)
                    }
                }
            }
        }

        isLongAsr.observe(this) { state ->
            tips.visibility = if (state) View.VISIBLE else View.GONE
            if (state) {
                floatRootView.rootView.findViewById<Button>(R.id.description).setOnClickListener {
                    isVisibleRecyclerView.postValue(true)
                    isLongAsr.postValue(false)
                }
            }
        }

        //ImageView显示状态的观察者
        isVisibleImageView.observe(this) {
            if (isInit) {
                imageView.visibility = if (it) View.VISIBLE else View.GONE
                if (it) {
                    dynamicViewChange(
                        this,
                        startValue = 280,
                        endValue = 240,
                        isWidthAnimator = HEIGHT_ANIMATOR,
                        animatorType = OvershootInterpolator(1f),
                        timeout = 500,
                        function = null
                    )

                    dynamicViewChange(
                        this,
                        startValue = 280,
                        endValue = 260,
                        isWidthAnimator = WIDTH_ANIMATOR,
                        animatorType = OvershootInterpolator(1f),
                        timeout = 500,
                        function = null
                    )
                    imageView.setImageResource(R.drawable.iphone_14_pro)
                }
            }
        }


        isVisibleRecyclerView.observe(this) {
            if (isInit) {
                recyclerView.visibility = if (it) View.VISIBLE else View.GONE
                if (it) {
                    dynamicViewChange(
                        this,
                        startValue = 80,
                        endValue = 280,
                        isWidthAnimator = HEIGHT_ANIMATOR,
                        animatorType = OvershootInterpolator(1f),
                        timeout = 500,
                        function = null
                    )

                    dynamicViewChange(
                        this,
                        startValue = 240,
                        endValue = 280,
                        isWidthAnimator = WIDTH_ANIMATOR,
                        animatorType = OvershootInterpolator(1f),
                        timeout = 500,
                        function = null
                    )
                    initRecyclerView(recyclerView)
                }
            }
        }

    }

    private fun showWindows() {
        windowManager.addView(floatRootView, layoutParam)
        isInit = true
        CommonUtils.setVisible(true)
        isVisibleToast.postValue(true)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private fun removeWindows() {
        lastWidth.value?.let {
            ValueAnimator.ofInt(
                it,
                0
            ).apply {
                addUpdateListener {
                    layoutParam.apply {
                        width = it?.animatedValue as Int
                    }.let { params ->
                        windowManager.updateViewLayout(floatRootView, params)
                    }
                }
                duration = 300
                interpolator = AnticipateInterpolator(1f)


                addListener(
                    onStart = {
                        false.let { state ->
                            isVisibleImageView.postValue(state)
                            isVisibleToast.postValue(state)
                            isVisibleRecyclerView.postValue(state)
                            isVisibleRecyclerView.postValue(state)
                        }
                    },

                    onEnd = {
                        CommonUtils.setVisible(false)
                        windowManager.removeView(floatRootView)
                    }
                )
            }.start()
        }

        lastHeight.value?.let {
            ValueAnimator.ofInt(
                it,
                1
            ).apply {
                addUpdateListener {
                    layoutParam.apply {
                        height = it?.animatedValue as Int
                    }.let { params ->
                        windowManager.updateViewLayout(floatRootView, params)
                    }
                }
                duration = 300
                interpolator = AnticipateInterpolator(1f)
            }.start()
        }
    }

    private fun initRecyclerView(recyclerView: RecyclerView) {
        val stringArr: ArrayList<String> = arrayListOf(
            "iPhone 14 Pro",
            "HUAWEI Mate 50 Pro",
            "Xiaomi 12s Ultra",
            "vivo X90 Pro",
            "OPPO Find x6 Pro",
            "Meizu 18 Pro",
            "Realme 10 Pro",
            "OnePlus 10 Pro",
            "Redmi K60 Pro",
            "Honor 80 Pro +",
            "Samsung Galaxy S22 Ultra"
        )
        val customizedAdapter = CustomizedAdapter(stringArr)
        val detail = object : CustomizedAdapter.IDetail {
            override fun show(string: String) {
                isVisibleRecyclerView.postValue(false)
                isVisibleImageView.postValue(true)
                isLongAsr.postValue(false)
            }
        }
        customizedAdapter.detail = detail
        recyclerView.adapter = customizedAdapter
        val layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.VERTICAL, false
        )
        recyclerView.layoutManager = layoutManager
    }


    @RequiresApi(Build.VERSION_CODES.R)
    inner class ViewBinder : Binder() {
        fun initView() {
            initWindows()
        }

        fun showView() {
            showWindows()
        }

        fun removeView() {
            removeWindows()
        }
    }

    interface IViewController {
        fun execute()
    }

    override fun onLayoutChange(
        v: View?,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        oldLeft: Int,
        oldTop: Int,
        oldRight: Int,
        oldBottom: Int
    ) {
        lastHeight.postValue(v?.height!!)
        lastWidth.postValue(v.width)
    }


}