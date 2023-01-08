package com.example.customizeview

import CustomizedAdapter
import android.annotation.SuppressLint
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
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.GravityCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Scene
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import androidx.transition.TransitionManager
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.properties.Delegates


/**
 * @功能:应用外打开Service 有局限性 特殊界面无法显示
 *
 */
class SuspendWindowService : LifecycleService(), View.OnClickListener, View.OnTouchListener {

    companion object {
        const val limit = 5
        const val TAG = "SuspendWindowService"
    }

    private var isContinue = true
    private var flag = 0
    private var isVisible = AtomicBoolean(false)
    private lateinit var floatRootView: View
    private lateinit var windowManager: WindowManager
    private lateinit var layoutParam: WindowManager.LayoutParams
    private var viewHeight by Delegates.notNull<Int>()
    private lateinit var sceneRoot: ViewGroup
    private lateinit var sceneOne: Scene
    private lateinit var sceneTwo: Scene
    private lateinit var sceneThree: Scene
    private lateinit var sceneFour: Scene
    private lateinit var sceneFive: Scene
    private lateinit var animation: Transition
    private lateinit var currentScene: Scene

    override fun onCreate() {
        super.onCreate()
        viewHeight = Utils.dip2px(this, 260f)
    }

    override fun onBind(intent: Intent): IBinder {
        Log.e(TAG, "onBind: ")
        super.onBind(intent)
        return ViewBinder()
    }

    inner class ViewBinder : Binder() {

        fun isVisible(): Boolean {
            return isVisible.get()
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun initView() {
            initWindow()
        }

        @RequiresApi(Build.VERSION_CODES.R)
        fun addView() {
            showWindow()
        }

        fun removeView() {

        }
    }

    /**
     * 初始化窗口
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun initWindow() {
        windowManager = application.getSystemService(WINDOW_SERVICE) as WindowManager
        layoutParam = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metrics)
            format = PixelFormat.RGBA_8888
            flags =
                FLAG_NOT_TOUCH_MODAL or FLAG_NOT_FOCUSABLE or FLAG_HARDWARE_ACCELERATED or FLAG_SHOW_WALLPAPER
            width = MATCH_PARENT
            height = WRAP_CONTENT
            gravity = Gravity.TOP or Gravity.CENTER
            windowAnimations = 0
        }
        floatRootView = LayoutInflater.from(this).inflate(R.layout.float_view, null)
        windowManager.addView(floatRootView, layoutParam)

        floatRootView.setOnTouchListener { _, _ -> true }
        floatRootView.rootView.setOnTouchListener { _, _ -> false }
    }


    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("ClickableViewAccessibility")
    private fun showWindow() {
        isVisible.set(true)

        sceneRoot = floatRootView.findViewById(R.id.scene_root_float)
        sceneOne = Scene.getSceneForLayout(sceneRoot, R.layout.a_scene, this)
        sceneTwo = Scene.getSceneForLayout(sceneRoot, R.layout.b_scene, this)
        sceneThree = Scene.getSceneForLayout(sceneRoot, R.layout.c_scene, this)
        sceneFour = Scene.getSceneForLayout(sceneRoot, R.layout.d_scene, this)
        sceneFive = Scene.getSceneForLayout(sceneRoot, R.layout.e_scene, this)

        currentScene = sceneOne
        animation =
            TransitionInflater.from(this).inflateTransition(R.transition.fade_transition)

        initAsr()
        floatRootView.rootView.setOnClickListener(this)
        floatRootView.rootView.setOnTouchListener(this)
    }

    private fun initAsr() {
        val textView = floatRootView.findViewById<TextView>(R.id.a_text_view)
        textView.text = "Hello World!"
    }

    private fun initLongAsr() {
        val textView = floatRootView.findViewById<TextView>(R.id.b_text_view)
        textView.text = "This is Dynamic Island Demo"
    }

    private fun initFirstRecyclerView() {
        val recyclerView = floatRootView.findViewById<RecyclerView>(R.id.recyclerview)
        val stringArr: ArrayList<String> = arrayListOf(
            "北京烤鸭",
            "周黑鸭",
            "茅台",
            "老乡鸡",
            "巴比馒头",
            "巴比馒头",
            "麦当劳",
            "肯德基",
            "汉堡王",
            "德克士",
            "华莱士"
        )
        val customizedAdapter = CustomizedAdapter(stringArr,
            object : ShowToast {
                override fun show(string: String) {
                    Toast.makeText(applicationContext, string, Toast.LENGTH_SHORT)
                        .show()
                }
            })
        recyclerView.adapter = customizedAdapter
        val layoutManager =
            LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
            )
        recyclerView.layoutManager = layoutManager
    }

    private fun initSecondRecyclerView() {
        val recyclerView = floatRootView.findViewById<RecyclerView>(R.id.recyclerview)
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
        val customizedAdapter = CustomizedAdapter(stringArr,
            object : ShowToast {
                override fun show(string: String) {
                    Toast.makeText(applicationContext, string, Toast.LENGTH_SHORT)
                        .show()
                }
            })
        recyclerView.adapter = customizedAdapter
        val layoutManager =
            LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
            )
        recyclerView.layoutManager = layoutManager
    }

    //定时器，定时变化View
    private fun initTimer() {
        CoroutineScope(Job() + Dispatchers.Main).launch {
            delay(200)
            while (isContinue) {
                delay(1200)
                floatRootView.rootView.findViewById<FrameLayout>(R.id.scene_root_float)
                    .callOnClick()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    override fun onClick(v: View?) {
        resetLayoutParams()
        when (v?.id) {
            R.id.scene_root_float -> {
                when (flag % limit) {
                    0 -> {
                        TransitionManager.go(sceneOne, animation).run {
                            initAsr()
                            updateHeight(300)
                            currentScene = sceneOne
                        }
                    }
                    1 -> {
                        TransitionManager.go(sceneTwo, animation).run {
                            initLongAsr()
                            updateHeight()
                            currentScene = sceneTwo
                        }
                    }
                    2 -> {
                        TransitionManager.go(sceneThree, animation).run {
                            updateHeight()
                            currentScene = sceneThree
                        }
                    }
                    3 -> {
                        TransitionManager.go(sceneFour, animation).run {
                            initFirstRecyclerView()
                            updateHeight()
                            currentScene = sceneFour
                        }
                    }
                    4 -> {
                        TransitionManager.go(sceneThree, animation).run {
                            updateHeight()
                            currentScene = sceneThree
                        }
                    }
                }
                flag++
            }
            else -> {
                Log.e(TAG, "onClick: ")
            }
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return if (event != null) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    resetLayoutParams()
                    val x = event.rawX.toInt()
                    val y = event.rawY.toInt()
                    var nextScene: Scene = sceneOne
                    val width = when (currentScene) {
                        sceneOne -> {
                            nextScene = sceneTwo
                            currentScene.sceneRoot.findViewById<TextView>(R.id.a_text_view).width
                        }
                        sceneTwo -> {
                            nextScene = sceneThree
                            currentScene.sceneRoot.findViewById<TextView>(R.id.b_text_view).width
                        }
                        sceneThree -> {
                            nextScene = sceneFour
                            currentScene.sceneRoot.findViewById<ImageView>(R.id.image_view).width
                        }
                        sceneFour -> {
                            nextScene = sceneFive
                            currentScene.sceneRoot.findViewById<RecyclerView>(R.id.recyclerview).width
                        }
                        sceneFive -> {
                            nextScene = sceneOne
                            currentScene.sceneRoot.findViewById<RecyclerView>(R.id.recyclerview).width
                        }
                        else -> 0
                    }

                    val startX = (1080 - width) / 2
                    val endX = (1080 + width) / 2
                    Log.e(
                        TAG,
                        "onTouch: $x $startX ${x in startX until endX}"
                    )

                    if (x in startX until endX) {
                        TransitionManager.go(nextScene, animation).run {
                            val timeout: Long = when (nextScene) {
                                sceneOne -> {
                                    initAsr()
                                    300L
                                }
                                sceneTwo -> {
                                    initLongAsr()
                                    0
                                }
                                sceneThree -> {
                                    0
                                }
                                sceneFour -> {
                                    initFirstRecyclerView()
                                    0
                                }
                                sceneFive -> {
                                    initSecondRecyclerView()
                                    0
                                }
                                else -> {
                                    initAsr()
                                    0
                                }
                            }
                            updateHeight(timeout)
                            currentScene = nextScene
                        }
                    }
                    x in (endX + 1) until startX
                }
                else -> {
                    true
                }
            }
        } else {
            true
        }
    }

    private fun resetLayoutParams() {
        layoutParam.apply {
            width = MATCH_PARENT
            height = MATCH_PARENT
        }.let {
            windowManager.updateViewLayout(floatRootView, it)
        }
    }

    private fun updateHeight() {
        updateHeight(0)
    }

    private fun updateHeight(timeout: Long) {
        lifecycleScope.launch(Dispatchers.Main) {
            delay(timeout)
            layoutParam.apply {
                height = WRAP_CONTENT
            }.let {
                windowManager.updateViewLayout(floatRootView, it)
            }
            cancel()
        }
    }


}