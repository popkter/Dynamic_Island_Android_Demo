package com.popkter.dynamicislandv2.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.graphics.PixelFormat
import android.util.Log
import android.view.*
import android.view.WindowManager.LayoutParams
import android.view.WindowManager.LayoutParams.*
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.transition.*
import androidx.transition.TransitionSet.ORDERING_TOGETHER
import com.popkter.dynamicislandv2.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class SuspendedWindowUtil(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) :
    View.OnLayoutChangeListener {

    companion object {
        const val TAG = "SuspendedWindowUtil"
    }

    private val isSingleAsrVisible = MutableLiveData(false)
    private val isAsrWithToastVisible = MutableLiveData(false)
    private val isAsrWithImageVisible = MutableLiveData(false)
    private val isEmptyScene = MutableLiveData(true)

    private var lastLeft = 0
    private var lastTop = 0
    private var lastRight = 0
    private var lastBottom = 0
    private var lastHeight = 0
    private var lastWidth = 0
    private var currentIsOne = true

    private lateinit var wM: WindowManager
    private lateinit var fvRootView: View
    private lateinit var lpRootView: LayoutParams
    private lateinit var sceneRoot: ViewGroup
    private lateinit var sceneEmpty: Scene
    private lateinit var sceneOne: Scene
    private lateinit var sceneTwo: Scene
    private lateinit var sceneThree: Scene
    private lateinit var animation: Transition

    fun initWindow(SourceLayoutId: Int, ViewContainerId: Int) {
        wM = context.getSystemService(WINDOW_SERVICE) as WindowManager
        lpRootView = LayoutParams().apply {
            type = TYPE_APPLICATION_OVERLAY
            format = PixelFormat.TRANSPARENT
            flags = FLAG_NOT_TOUCH_MODAL or FLAG_NOT_FOCUSABLE
            width = MATCH_PARENT
            height = MATCH_PARENT
        }
        fvRootView = LayoutInflater.from(context).inflate(R.layout.suspend_window, null)
        sceneRoot = fvRootView.findViewById(ViewContainerId)
        sceneEmpty = Scene.getSceneForLayout(sceneRoot, R.layout.empty_scene, context)
        sceneOne = Scene.getSceneForLayout(sceneRoot, R.layout.single_asr, context)
        sceneTwo = Scene.getSceneForLayout(sceneRoot, R.layout.asr_with_image, context)
        sceneThree = Scene.getSceneForLayout(sceneRoot, R.layout.asr_with_toast, context)
        //animation = TransitionInflater.from(context).inflateTransition(R.transition.fade_transition)

        animation = TransitionSet().apply {
            addTransition(ChangeImageTransform())
            addTransition(ChangeBounds().apply { interpolator = OvershootInterpolator(1F) })
            ordering = ORDERING_TOGETHER
            duration = 300
        }

        sceneRoot.addOnLayoutChangeListener(this)
        sceneRoot.setOnClickListener {
            when (Random.nextInt(100) % 4) {
                0 -> isSingleAsrVisible.postValue(true)
                1 -> isAsrWithToastVisible.postValue(true)
                2 -> isAsrWithImageVisible.postValue(true)
                3 -> {
                    isEmptyScene.postValue(true)
                    lifecycleOwner.lifecycleScope.launch {
                        delay(1000)
                        isSingleAsrVisible.postValue(true)
                    }
                }
                else -> {}
            }
        }

        isEmptyScene.observe(lifecycleOwner) {
            it?.let {
                TransitionManager.go(sceneEmpty, animation)
            }
        }

        isSingleAsrVisible.observe(lifecycleOwner) {
            it?.let {
                TransitionManager.go(sceneOne, animation)
                fvRootView.findViewById<TextView>(R.id.single_asr_asr)?.text = "Halo World!"
            }
        }

        isAsrWithToastVisible.observe(lifecycleOwner) {
            it?.let {
                TransitionManager.go(sceneThree, animation)
                fvRootView.findViewById<TextView>(R.id.asr_with_toast_asr)?.text = "Halo World!"
                fvRootView.findViewById<TextView>(R.id.asr_with_toast_tip)?.text = "Android"
            }
        }

        isAsrWithImageVisible.observe(lifecycleOwner) {
            it?.let {
                TransitionManager.go(sceneTwo, animation.apply { duration = 500 })
                fvRootView.findViewById<TextView>(R.id.asr_with_image_asr)?.text = "Halo World!"
            }
        }
    }

    fun showWindow() {
        CommonUtils.checkSuspendedWindowPermission(context as Activity) {
            wM.addView(fvRootView, lpRootView)
            lifecycleOwner.lifecycleScope.launch{
                delay(1000)
                isSingleAsrVisible.postValue(true)
            }
        }
    }

    fun removeWindow() {
        wM.removeView(fvRootView)
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
        lastHeight = v?.height!!
        lastWidth = v.width
        lastLeft = left
        lastTop = top
        lastRight = right
        lastBottom = bottom
/*        Log.e(TAG, "onLayoutChange: $lastLeft,$lastTop,$lastRight,$lastBottom")
        Log.e(TAG, "showWindow: ${sceneRoot.measuredWidth} ${sceneRoot.measuredHeight}")*/
    }
}