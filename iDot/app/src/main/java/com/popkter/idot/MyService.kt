package com.popkter.idot

import CustomizedAdapter
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.popkter.idot.databinding.*
import com.popkter.idotsdk.PopWindowManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MyService : LifecycleService() {


    private var isAdd = false
    private var imageSrc = -1

    private lateinit var windowManager: PopWindowManager

    private lateinit var activityMainBinding: ActivityMainBinding

    private val suspendViewVisible = MutableLiveData(false)

    /**
     * image View
     */
    private lateinit var imageSceneBinding: ImageSceneBinding

    private val imageSceneVisible = MutableLiveData(false)

    /**
     * tips View
     */
    private lateinit var tipSceneBinding: TipSceneBinding

    private val tipSceneVisible = MutableLiveData(false)

    /**
     * recyclerList View
     */
    private lateinit var listSceneBinding: RecyclerviewSceneBinding

    private val listSceneVisible = MutableLiveData(false)

    override fun onCreate() {
        super.onCreate()
        val layoutInflater =
            this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        windowManager = PopWindowManager(this, this)
        imageSceneBinding = ImageSceneBinding.inflate(layoutInflater)
        tipSceneBinding = TipSceneBinding.inflate(layoutInflater)
        listSceneBinding = RecyclerviewSceneBinding.inflate(layoutInflater)
        //initRecyclerView(listSceneBinding.recyclerView)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        windowManager.init()
        lifecycleScope.launch {
            delay(2000)
            windowManager.showView(tipSceneBinding.root)
            delay(2000)
            windowManager.showView(imageSceneBinding.root)
            delay(2000)
            windowManager.showView(listSceneBinding.root)
            delay(2000)
            windowManager.showView(tipSceneBinding.root)
            delay(2000)
            windowManager.showView(imageSceneBinding.root)
            /*delay(2000)
            windowManager.dismiss()*/
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun initObserver() {
    }

    private fun initRecyclerView(recyclerView: RecyclerView) {
        val stringArr: ArrayList<String> = arrayListOf(
            "iPhone 14 Pro",
            "HUAWEI Mate 50 Pro",
            "Xiaomi 12s Ultra",
            "OPPO Find x5 Pro",
            "Honor Magic 4 Pro +",
            "Samsung Galaxy S22 Ultra"
        )
        val customizedAdapter = CustomizedAdapter(stringArr) {
            imageSrc = when (it) {
                "iPhone 14 Pro" -> R.drawable.iphone14pro
                "HUAWEI Mate 50 Pro" -> R.drawable.huaweimate50pro
                "Xiaomi 12s Ultra" -> R.drawable.xiaomi12su
                "OPPO Find x5 Pro" -> R.drawable.oppofind5pro
                "Honor Magic 4 Pro +" -> R.drawable.honormagic4pro
                "Samsung Galaxy S22 Ultra" -> R.drawable.samsunggalaxys22ultra
                else -> R.drawable.iphone14pro
            }
            imageSceneVisible.postValue(true)
            listSceneVisible.postValue(false)
        }
        recyclerView.adapter = customizedAdapter

        val layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.VERTICAL, false
        )
        recyclerView.layoutManager = layoutManager
        recyclerView.setItemViewCacheSize(10);
        recyclerView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
    }

    private fun updateView(height: Int?, width: Int?, view: View, block: (() -> Unit)?) {
        //windowManager.updateViewSize(height!!, width!!, null) {}
    }

    private fun removeView(height: Int?, width: Int?) {
        if (tipSceneVisible.value == false && imageSceneVisible.value == false && listSceneVisible.value == false) {
            //windowManager.updateViewSize(height ?: 0, width ?: 0, null) {}
            imageSceneVisible.postValue(false)
            tipSceneVisible.postValue(false)
        }
    }
}