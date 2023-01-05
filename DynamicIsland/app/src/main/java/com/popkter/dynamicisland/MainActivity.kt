package com.popkter.dynamicisland

import CustomizedAdapter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintProperties.WRAP_CONTENT
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import androidx.transition.TransitionManager
import kotlin.math.floor


class MainActivity : AppCompatActivity(), View.OnClickListener {
    companion object {
        const val TAG = "MainActivity"
    }

    private val isHide = MutableLiveData(0)
    private val isLongAsr = MutableLiveData(false)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.btn).setOnClickListener {
            isHide.postValue(floor(Math.random() * 1000).toInt())
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
                .inflateTransition(R.transition.island_animator)


        isLongAsr.observe(this) {
            TransitionManager.beginDelayedTransition(sceneRoot, transition)
            if (it) {
                toast.layoutParams.apply {
                    width = Utils.dip2px(this@MainActivity, 200f)
                    height = Utils.dip2px(this@MainActivity, 40f)
                }.let { params ->
                    toast.layoutParams = params
                }
                asr.text = "This is Dynamic Island!"
            } else {
                toast.layoutParams.apply {
                    width = Utils.dip2px(this@MainActivity, 200f)
                    height = Utils.dip2px(this@MainActivity, 80f)
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
                        width = Utils.dip2px(this@MainActivity, 200f)
                        height = Utils.dip2px(this@MainActivity, 40f)
                    }.let { param ->
                        toast.visibility = View.VISIBLE
                        imageView.visibility = View.GONE
                        recyclerView.visibility = View.GONE
                        asr.layoutParams = param
                    }
                }
                1 -> {
                    imageView.layoutParams.apply {
                        width = Utils.dip2px(this@MainActivity, 200f)
                        height = Utils.dip2px(this@MainActivity, 200f)
                    }.let { param ->
                        toast.visibility = View.GONE
                        imageView.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                        imageView.layoutParams = param
                    }
                }
                else -> {
                    recyclerView.layoutParams.apply {
                        width = Utils.dip2px(this@MainActivity, 300f)
                        height = Utils.dip2px(this@MainActivity, 304f)
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
}