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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.MutableLiveData
import androidx.transition.Scene
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import androidx.transition.TransitionManager
import com.popkter.dynamicislandv2.R

class SuspendedWindowUtil(private val context: Context) : View.OnTouchListener,
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
        fvRootView = LayoutInflater.from(context).inflate(SourceLayoutId, null)
        sceneRoot = fvRootView.findViewById(ViewContainerId)
        sceneOne = Scene.getSceneForLayout(sceneRoot, R.layout.single_asr, context)
        sceneTwo = Scene.getSceneForLayout(sceneRoot, R.layout.asr_with_image, context)
        fvRootView.setOnTouchListener(this)
        sceneRoot.addOnLayoutChangeListener(this)
        animation = TransitionInflater.from(context).inflateTransition(R.transition.fade_transition)
        sceneRoot.background?.mutate()?.alpha = 255
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

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        val inWidthRange = event?.x?.toInt() in lastLeft until lastRight
        val inHeightRange = event?.y?.toInt() in lastTop until lastBottom
        if (event?.action == MotionEvent.ACTION_DOWN) {
            Log.e(TAG, "onTouch: ${inWidthRange && inHeightRange}")
            return if (inWidthRange && inHeightRange) {
                currentIsOne = if (currentIsOne) {
                    TransitionManager.go(sceneTwo, animation)
                    false
                } else {
                    TransitionManager.go(sceneOne, animation)
                    true
                }
                false
            } else {
                true
            }
        } else {
            return true
        }
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
    }


}