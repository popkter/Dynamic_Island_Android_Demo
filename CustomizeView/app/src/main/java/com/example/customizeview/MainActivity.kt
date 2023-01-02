package com.example.customizeview

import CustomizedAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.setPadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var flag = 0
    private lateinit var sceneOne: Scene
    private lateinit var sceneTwo: Scene
    private lateinit var sceneThree: Scene
    private lateinit var sceneFour: Scene
    private lateinit var sceneFive: Scene
    private lateinit var animation: Transition

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        window.sharedElementsUseOverlay = true
        val sceneRoot: ViewGroup = findViewById(R.id.scene_root)
        sceneOne = Scene.getSceneForLayout(sceneRoot, R.layout.a_scene, this)
        sceneTwo = Scene.getSceneForLayout(sceneRoot, R.layout.b_scene, this)
        sceneThree = Scene.getSceneForLayout(sceneRoot, R.layout.c_scene, this)
        sceneFour = Scene.getSceneForLayout(sceneRoot, R.layout.d_scene, this)
        sceneFive = Scene.getSceneForLayout(sceneRoot, R.layout.e_scene, this)
        animation =
            TransitionInflater.from(this).inflateTransition(R.transition.fade_transition)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_1 -> {
                if (flag == 1) {
                    return
                }
                flag = 1
                TransitionManager.go(sceneOne, animation)
                initAsr()
            }

            R.id.btn_2 -> {
                if (flag == 2) {
                    return
                }
                flag = 2
                TransitionManager.go(sceneTwo, animation)
                initLongAsr()
            }

            R.id.btn_3 -> {
                if (flag == 3) {
                    return
                }
                flag = 3
                TransitionManager.go(sceneThree, animation)
            }

            R.id.btn_4 -> {
                if (flag == 4) {
                    return
                }
                flag = 4
                TransitionManager.go(sceneFour, animation)
                initRecyclerView()
            }
            R.id.btn_5 -> {
                if (flag == 5) {
                    return
                }
                flag = 5
                TransitionManager.go(sceneFive, animation)
                initRecyclerView_2()
            }
            R.id.btn_6 -> {
                if (ViewModleMain.isVisible.value == true) {
                  return
                  //  ViewModleMain.isShowSuspendWindow.postValue(false)
                } else {
                    startService(Intent(this, SuspendWindowService::class.java))
                    Utils.checkSuspendedWindowPermission(this) {
                        ViewModleMain.isShowSuspendWindow.postValue(true)
                    }
                }
            }

            else -> {
                if (flag == 0) {
                    return
                }
                flag = 0
                TransitionManager.go(sceneOne, animation)
                findViewById<TextView>(R.id.a_text_view).setPadding(0)
            }
        }
    }

    private fun initAsr() {
        val textView = findViewById<TextView>(R.id.a_text_view)
        textView.text = "Hello World!"
    }

    private fun initLongAsr() {
        val textView = findViewById<TextView>(R.id.b_text_view)
        textView.text = "This is Dynamic Island Demo"
    }

    private fun initRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
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
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
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

    override fun onResume() {
        super.onResume()

    }

}


