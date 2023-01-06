package com.popkter.dynamicisland.service

import CustomizedAdapter
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.PixelFormat
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Scene
import androidx.transition.TransitionInflater
import androidx.transition.TransitionManager
import com.popkter.dynamicisland.R
import com.popkter.dynamicisland.utils.CommonUtils

class DynamicIslandService : LifecycleService() {

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
    private val cycleInterpolator = CycleInterpolator(1F)

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

    private lateinit var floatRootView: View
    private lateinit var windowManager: WindowManager
    private lateinit var layoutParam: WindowManager.LayoutParams
    private lateinit var toast: ViewGroup
    private lateinit var asr: TextView
    private lateinit var tips: View


    /**
     * 属性变化的动画
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private fun dynamicViewChange(
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
            startValue,
            endValue
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

    override fun onDestroy() {
        super.onDestroy()
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
            width = MATCH_PARENT
            height = WRAP_CONTENT
            gravity = Gravity.CENTER or Gravity.TOP
            y = 20
        }

        floatRootView =
            LayoutInflater.from(this@DynamicIslandService).inflate(R.layout.dynamic_island, null)

        floatRootView.setOnTouchListener { _, event ->
            if (CommonUtils.canDismiss()) {
                if (event?.action == MotionEvent.ACTION_OUTSIDE) {
                    if (CommonUtils.isVisible()) {
                        removeWindows()
                    }
                    true
                } else {
                    false
                }
            } else {
                false
            }
        }

        val sceneRoot: ViewGroup = floatRootView.findViewById(R.id.scene_root)

        //val recyclerView = floatRootView.findViewById<RecyclerView>(R.id.recycler_view)
        //initRecyclerView(recyclerView)

        val imageView = floatRootView.findViewById<ImageView>(R.id.image_view)

        val sceneView = Scene.getSceneForLayout(sceneRoot, R.layout.imageview_scene, this)

        val sceneToast = Scene.getSceneForLayout(sceneRoot, R.layout.toast_scene, this)

        val sceneRecyclerView =
            Scene.getSceneForLayout(sceneRoot, R.layout.recyclerview_scene, this)

        val transitionExpand =
            TransitionInflater.from(this).inflateTransition(R.transition.island_animator_expand)

        val transitionFold =
            TransitionInflater.from(this).inflateTransition(R.transition.island_animator_fold)

        //Toast显示状态的观察者
        isVisibleToast.observe(this) {
            if (it) {
                floatRootView.rootView.findViewById<Button>(R.id.description).setOnClickListener {
                    TransitionManager.go(sceneView, transitionExpand)
                    layoutParam.apply {
                        width = MATCH_PARENT
                        height = WRAP_CONTENT
                    }.let { params ->
                        windowManager.updateViewLayout(floatRootView, params)
                        isVisibleImageView.postValue(true)
                        isVisibleToast.postValue(false)
                        isLongAsr.postValue(false)
                    }
                }

                toast = floatRootView.findViewById(R.id.toast_main)
                asr = floatRootView.findViewById(R.id.asr)
                asr.text = "This is Dynamic Island!"

                tips = floatRootView.findViewById(R.id.tips)
                asr.setOnClickListener {
                    isLongAsr.postValue(isLongAsr.value == false)
                }
            }
        }

        isLongAsr.observe(this) {
            if (CommonUtils.isVisible() && isVisibleToast.value == true) {
                Log.e(TAG, "isLongAsr.observe: $it")
                dynamicViewChange(
                    CommonUtils.dip2px(this@DynamicIslandService, if (it) 60 else 100),
                    CommonUtils.dip2px(this@DynamicIslandService, if (it) 100 else 60),
                    if (it) 300 else 200,
                    HEIGHT_ANIMATOR,
                    if (it) anticipateOvershootInterpolator else linearInterpolator, null
                )
                val icon = floatRootView.rootView.findViewById<ImageView>(R.id.icon)
                val description =
                    floatRootView.rootView.findViewById<TextView>(R.id.description)
                icon.setImageResource(R.drawable.ic_launcher_foreground)
                description.text = "open"
                tips.visibility = if (it) View.VISIBLE else View.GONE
            }
        }

        //ImageView显示状态的观察者
        isVisibleImageView.observe(this) {
            if (it) {
                floatRootView.rootView.findViewById<ImageView>(R.id.image_view_scene)
                    .setOnClickListener {
                        /*dynamicViewChange(
                            CommonUtils.dip2px(this@DynamicIslandService, 220f),
                            CommonUtils.dip2px(this@DynamicIslandService, 60f),
                            300, HEIGHT_ANIMATOR,
                            linearInterpolator,
                            null
                        )
                        TransitionManager.go(sceneToast, transitionFold)
                        isVisibleImageView.postValue(false)
                        isVisibleToast.postValue(true)*/

                        TransitionManager.go(sceneRecyclerView, transitionExpand)
                        val recyclerView =
                            floatRootView.rootView.findViewById<TextView>(R.id.recyclerview_scene)
                        recyclerView.setOnClickListener {
                            layoutParam.apply {
                                width = MATCH_PARENT
                                height = WRAP_CONTENT
                            }.let { params ->
                                windowManager.updateViewLayout(floatRootView, params)
                                isVisibleImageView.postValue(false)
                                isVisibleToast.postValue(false)
                                isLongAsr.postValue(false)
                                isVisibleRecyclerView.postValue(true)
                            }
                        }
                        //initRecyclerView(recyclerView)
                    }

            }
        }

        isVisibleRecyclerView.observe(this) {
            if (it) {
                dynamicViewChange(
                    CommonUtils.dip2px(this@DynamicIslandService, 320),
                    CommonUtils.dip2px(this@DynamicIslandService, 60),
                    300,
                    HEIGHT_ANIMATOR,
                    linearInterpolator,
                    null
                )
                TransitionManager.go(sceneToast, transitionFold)
                isVisibleImageView.postValue(false)
                isVisibleToast.postValue(true)
                isVisibleRecyclerView.postValue(false)
            }
        }

/*
        if (isSceneImage) {

            floatRootView.rootView.findViewById<ImageView>(R.id.image_view_scene).setOnClickListener {
                Log.e(TAG, "floatRootView:setOnClickListener ")
*/
/*                dynamicViewChange(
                    CommonUtils.dip2px(this@DynamicIslandService, 220f),
                    CommonUtils.dip2px(this@DynamicIslandService, 60f),
                    200, HEIGHT_ANIMATOR
                ) {
                    imageView.visibility = View.GONE
                    tips.visibility = View.GONE
                    toast.visibility = View.VISIBLE
                }*//*

                TransitionManager.go(sceneToast, transition)
                isSceneImage.postValue(false)
            }
        }
*/

        /*      recyclerView.setOnClickListener {
                  dynamicViewChange(
                      CommonUtils.dip2px(this@DynamicIslandService, 60f),
                      CommonUtils.dip2px(this@DynamicIslandService, 220f),
                      500, HEIGHT_ANIMATOR
                  ) {}
                  dynamicViewChange(
                      CommonUtils.dip2px(this@DynamicIslandService, 320f),
                      CommonUtils.dip2px(this@DynamicIslandService, 220f), 200, WIDTH_ANIMATOR
                  ) {
                      recyclerView.visibility = View.GONE
                      toast.visibility = View.VISIBLE
                  }
              }*/
    }

    private fun showWindows() {
        windowManager.addView(floatRootView, layoutParam)
        CommonUtils.setVisible(true)
        isVisibleToast.postValue(true)
    }

    private fun removeWindows() {
        CommonUtils.setVisible(false)
        isVisibleImageView.postValue(false)
        isVisibleToast.postValue(false)
        windowManager.removeView(floatRootView)
    }

    private fun initRecyclerView(recyclerView: RecyclerView) {
        val stringArr: ArrayList<String> = arrayListOf(
            "iPhone 13 Pro Max",
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
}