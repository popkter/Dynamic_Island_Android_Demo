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
import androidx.transition.*
import com.popkter.dynamicislandv2.R

class SuspendedWindowUtil(private val context: Context) :
    View.OnLayoutChangeListener {

    companion object {
        const val TAG = "SuspendedWindowUtil"
    }

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
    private lateinit var sceneOne: Scene
    private lateinit var sceneTwo: Scene
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
        sceneOne = Scene.getSceneForLayout(sceneRoot, R.layout.single_asr, context)
        sceneTwo = Scene.getSceneForLayout(sceneRoot, R.layout.asr_with_image, context)
        animation = TransitionInflater.from(context).inflateTransition(R.transition.fade_transition)
        sceneRoot.addOnLayoutChangeListener(this)
        fvRootView.setOnClickListener {
            if (currentIsOne) {
                currentIsOne = false
                TransitionManager.go(sceneTwo, animation)
            } else {
                currentIsOne = true
                TransitionManager.go(sceneOne, animation)
            }
        }
    }

    fun showWindow() {
        CommonUtils.checkSuspendedWindowPermission(context as Activity) {
            wM.addView(fvRootView, lpRootView)
            TransitionManager.go(sceneOne)
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
        Log.e(TAG, "onLayoutChange: $lastLeft,$lastTop,$lastRight,$lastBottom")
        Log.e(TAG, "showWindow: ${sceneRoot.measuredWidth} ${sceneRoot.measuredHeight}", )

    }


}