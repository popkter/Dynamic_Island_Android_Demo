package com.example.customizeview

import CustomizedAdapter
import android.annotation.SuppressLint
import android.graphics.PixelFormat
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.setPadding
import androidx.lifecycle.LifecycleService
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Scene
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import androidx.transition.TransitionManager
import kotlinx.coroutines.*

/**
 * @功能:应用外打开Service 有局限性 特殊界面无法显示
 * @User Lmy
 * @Creat 4/15/21 5:28 PM
 * @Compony 永远相信美好的事情即将发生
 */
class SuspendWindowService : LifecycleService() {

    companion object {
        const val TAG = "SuspendWindowService"
    }

    private lateinit var windowManager: WindowManager
    private var floatRootView: View? = null
    private var isContinue = true
    private lateinit var animation: Transition
    private var flag = 0
    private lateinit var sceneOne: Scene
    private lateinit var sceneTwo: Scene
    private lateinit var sceneThree: Scene
    private lateinit var sceneFour: Scene
    private lateinit var sceneFive: Scene

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate() {
        Log.e(TAG, "onCreate: ")
        super.onCreate()
        animation = TransitionInflater.from(this).inflateTransition(R.transition.fade_transition)
        initObserve()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun initObserve() {
        Log.e(TAG, "initObserve: ")
        ViewModleMain.apply {
            isShowSuspendWindow.observe(this@SuspendWindowService) {
                Log.e(TAG, "initObserve: $it")
                if (it) {
                    showWindow()
                } else {
                    isVisible.postValue(false)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("ClickableViewAccessibility")
    private fun showWindow() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val layoutParam = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metrics)
            format = PixelFormat.RGBA_8888
            flags =
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            //位置大小设置
            width = MATCH_PARENT
            height = WRAP_CONTENT
            gravity = Gravity.START or Gravity.TOP or Gravity.CENTER

            x = metrics.widthPixels - width / 2
            //  (windowManager.currentWindowMetrics.bounds.width() - width) / 2
            y = 10
        }
        floatRootView = LayoutInflater.from(this).inflate(R.layout.float_view, null)
        floatRootView?.setOnTouchListener(ItemViewTouchListener(layoutParam, windowManager))
        windowManager.addView(floatRootView, layoutParam)
        ViewModleMain.isVisible.postValue(true)
        val sceneRoot: ViewGroup = floatRootView!!.findViewById(R.id.scene_root_float)
        sceneOne = Scene.getSceneForLayout(sceneRoot, R.layout.a_scene, this)
        sceneTwo = Scene.getSceneForLayout(sceneRoot, R.layout.b_scene, this)
        sceneThree = Scene.getSceneForLayout(sceneRoot, R.layout.c_scene, this)
        sceneFour = Scene.getSceneForLayout(sceneRoot, R.layout.d_scene, this)
        sceneFive = Scene.getSceneForLayout(sceneRoot, R.layout.e_scene, this)
        val animation2 =
            TransitionInflater.from(this).inflateTransition(R.transition.fade_transition_2)
        val animation3 =
            TransitionInflater.from(this).inflateTransition(R.transition.fade_transition_2)

        TransitionManager.beginDelayedTransition(sceneRoot, animation)

        floatRootView!!.rootView.setOnClickListener {
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
                    initRecyclerView()
                }
                4 -> {
                    TransitionManager.go(sceneFive, animation)
                    initRecyclerView_2()
                }
                5 -> {
                    TransitionManager.go(sceneThree, animation)
                }
                6 -> {
                    TransitionManager.go(sceneOne, animation)
                    floatRootView!!.findViewById<TextView>(R.id.a_text_view)
                        .setPadding(0)
                    isContinue = false
                }
            }
            flag++
        }

        initTimer()
    }


    private fun initAsr() {
        val textView = floatRootView!!.findViewById<TextView>(R.id.a_text_view)
        textView.text = "Hello World!"
    }


    private fun initLongAsr() {
        val textView = floatRootView!!.findViewById<TextView>(R.id.b_text_view)
        textView.text = "This is Dynamic Island Demo"
    }

    private fun initRecyclerView() {
        val recyclerView = floatRootView!!.findViewById<RecyclerView>(R.id.recyclerview)
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

    fun initRecyclerView_2() {
        val recyclerView = floatRootView!!.findViewById<RecyclerView>(R.id.recyclerview)
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


    private fun initTimer() {
        CoroutineScope(Job() + Dispatchers.Main).launch {
            delay(200)
            while (isContinue) {
                delay(1200)
                floatRootView!!.callOnClick()
            }

        }
    }

}