package com.popkter.dynamicisland

import CustomizedAdapter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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

    private var isHide = MutableLiveData<Int>(0)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.btn).setOnClickListener {
            isHide.postValue(floor(Math.random() * 100).toInt())
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        initSecondRecyclerView(recyclerView)

        val asr = findViewById<TextView>(R.id.asr)

        val imageView = findViewById<ImageView>(R.id.image_view)

        val transition =
            TransitionInflater.from(this)
                .inflateTransition(R.transition.island_animator)

        isHide.observe(this) {
            val sceneRoot: ViewGroup = findViewById(R.id.scene_root)
            TransitionManager.beginDelayedTransition(sceneRoot, transition)
            Log.e(TAG, "isHide: $it")
            when (it % 3) {
                0 -> {
                    recyclerView.layoutParams.apply {
                        width = Utils.dip2px(this@MainActivity, 200f)
                        height = Utils.dip2px(this@MainActivity, 50f)
                    }.let { param ->
                        asr.visibility = View.VISIBLE
                        imageView.visibility = View.GONE
                        recyclerView.visibility = View.GONE
                        recyclerView.layoutParams = param
                    }
                }
                1 -> {

                    recyclerView.layoutParams.apply {
                        width = Utils.dip2px(this@MainActivity, 200f)
                        height = Utils.dip2px(this@MainActivity, 200f)
                    }.let { param ->
                        asr.visibility = View.GONE
                        imageView.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                        recyclerView.layoutParams = param
                    }
                }
                else -> {
                    recyclerView.layoutParams.apply {
                        width = Utils.dip2px(this@MainActivity, 300f)
                        height = Utils.dip2px(this@MainActivity, 300f)
                    }.let { param ->
                        asr.visibility = View.GONE
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