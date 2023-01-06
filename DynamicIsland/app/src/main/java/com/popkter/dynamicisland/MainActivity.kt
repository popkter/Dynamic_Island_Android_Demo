package com.popkter.dynamicisland

import CustomizedAdapter
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import androidx.transition.TransitionManager
import com.popkter.dynamicisland.service.DynamicIslandService
import com.popkter.dynamicisland.utils.CommonUtils


class MainActivity : AppCompatActivity(), View.OnClickListener {
    companion object {
        const val TAG = "MainActivity"
    }

    private val isHide = MutableLiveData(0)
    private val isLongAsr = MutableLiveData(false)
    private lateinit var viewBinder: DynamicIslandService.ViewBinder

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        findViewById<Button>(R.id.btn).setOnClickListener {
//            isHide.postValue(kotlin.math.floor(Math.random() * 1000).toInt())
            CommonUtils.checkSuspendedWindowPermission(this) {
                if (CommonUtils.isVisible()) {
                   viewBinder.removeView()
                } else {
                    bindService(
                        Intent(this@MainActivity, DynamicIslandService::class.java),
                        object : ServiceConnection {
                            override fun onServiceConnected(
                                name: ComponentName?,
                                service: IBinder?
                            ) {
                                viewBinder = service as DynamicIslandService.ViewBinder
                                if (viewBinder.initialized()) {
                                    viewBinder.initView()
                                    viewBinder.showView()
                                }
                            }

                            override fun onServiceDisconnected(name: ComponentName?) {
                                TODO("Not yet implemented")
                            }

                        },
                        BIND_AUTO_CREATE
                    )
                }
            }
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        initSecondRecyclerView(recyclerView)

        val asr = findViewById<TextView>(R.id.asr)
        asr.text = "This is Dynamic Island!"
        asr.setOnClickListener {
            isLongAsr.postValue(!isLongAsr.value!!)
        }

        val toast = findViewById<View>(R.id.toast)

        val tips = findViewById<View>(R.id.tips)

        val sceneRoot: ViewGroup = findViewById(R.id.scene_root)

        val transition =
            TransitionInflater.from(this)
                .inflateTransition(R.transition.island_animator_expand)


        isLongAsr.observe(this) {
            TransitionManager.beginDelayedTransition(sceneRoot, transition)
            if (it) {
                toast.layoutParams.apply {
                    width = CommonUtils.dip2px(this@MainActivity, 200)
                    height = CommonUtils.dip2px(this@MainActivity, 40)
                }.let { params ->
                    toast.layoutParams = params
                }
                asr.text = "This is Dynamic Island!"
            } else {
                toast.layoutParams.apply {
                    width = CommonUtils.dip2px(this@MainActivity, 200)
                    height = CommonUtils.dip2px(this@MainActivity, 80)
                }.let { params ->
                    toast.layoutParams = params
                }
            }
        }

        val imageView = findViewById<ImageView>(R.id.image_view)

        isHide.observe(this) {
            TransitionManager.beginDelayedTransition(sceneRoot, transition)
            Log.e(TAG, "isHide: $it")
            when (it % 3) {
                0 -> {
                    toast.layoutParams.apply {
                        width = CommonUtils.dip2px(this@MainActivity, 200)
                        height = CommonUtils.dip2px(this@MainActivity, 40)
                    }.let { param ->
                        toast.visibility = View.VISIBLE
                        imageView.visibility = View.GONE
                        recyclerView.visibility = View.GONE
                        asr.layoutParams = param
                    }
                }
                1 -> {
                    imageView.layoutParams.apply {
                        width = CommonUtils.dip2px(this@MainActivity, 200)
                        height = CommonUtils.dip2px(this@MainActivity, 200)
                    }.let { param ->
                        toast.visibility = View.GONE
                        imageView.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                        imageView.layoutParams = param
                    }
                }
                else -> {
                    recyclerView.layoutParams.apply {
                        width = CommonUtils.dip2px(this@MainActivity, 300)
                        height = CommonUtils.dip2px(this@MainActivity, 304)
                    }.let { param ->
                        toast.visibility = View.GONE
                        imageView.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        recyclerView.layoutParams = param
                    }
                }
            }

        }


    }

    private fun initSecondRecyclerView(recyclerView: RecyclerView) {
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

    override fun onClick(v: View?) {
        Log.e(TAG, "onClick: ")
    }

    private fun <T> T.initialized(): Boolean {
        return this != null
    }
}