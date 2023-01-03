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
import android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
import android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleService
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Scene
import androidx.transition.TransitionInflater
import androidx.transition.TransitionManager
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.properties.Delegates


/**
 * @功能:应用外打开Service 有局限性 特殊界面无法显示
 *
 */
class SuspendWindowService : LifecycleService() {

    companion object {
        const val TAG = "SuspendWindowService"
    }

    private var isContinue = true
    private var flag = 0
    private var isVisible = AtomicBoolean(false)
    private lateinit var floatRootView: View
    private lateinit var windowManager: WindowManager
    private lateinit var layoutParam: WindowManager.LayoutParams
    private var viewHeight by Delegates.notNull<Int>()

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
            flags = FLAG_NOT_TOUCH_MODAL or FLAG_NOT_FOCUSABLE
            //位置大小设置
            width = MATCH_PARENT
            height = viewHeight
            gravity = Gravity.START or Gravity.TOP

            x = metrics.widthPixels
            y = 10
        }
    }


    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("ClickableViewAccessibility")
    private fun showWindow() {
        isVisible.set(true)
        floatRootView = LayoutInflater.from(this).inflate(R.layout.float_view, null)

        //拖动以修改悬浮窗位置
        //floatRootView?.setOnTouchListener(ItemViewTouchListener(layoutParam, windowManager))

        windowManager.addView(floatRootView, layoutParam)
        val sceneRoot: ViewGroup = floatRootView.findViewById(R.id.scene_root_float)
        val sceneOne = Scene.getSceneForLayout(sceneRoot, R.layout.a_scene, this)
        val sceneTwo = Scene.getSceneForLayout(sceneRoot, R.layout.b_scene, this)
        val sceneThree = Scene.getSceneForLayout(sceneRoot, R.layout.c_scene, this)
        val sceneFour = Scene.getSceneForLayout(sceneRoot, R.layout.d_scene, this)
        val sceneFive = Scene.getSceneForLayout(sceneRoot, R.layout.e_scene, this)
        val animation =
            TransitionInflater.from(this).inflateTransition(R.transition.fade_transition)

        floatRootView.rootView.findViewById<FrameLayout>(R.id.scene_root_float).setOnClickListener {
            when (flag % 7) {
                0 -> {
                    TransitionManager.go(sceneOne, animation)
                    initAsr()
                    isContinue = true
                }
                1 -> {
                    TransitionManager.go(sceneTwo, animation)
                    initLongAsr()
                }
                2 -> {
                    TransitionManager.go(sceneThree, animation)
                }
                3 -> {
                    TransitionManager.go(sceneFour, animation)
                    initFirstRecyclerView()
                }
                4 -> {
                    TransitionManager.go(sceneFive, animation)
                    initSecondRecyclerView()
                }
                5 -> {
                    TransitionManager.go(sceneThree, animation)
                }
                6 -> {
                    TransitionManager.go(sceneOne, animation)
                    initAsr()
                    isContinue = false
                }
            }
            flag++
        }

        initTimer()
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

}