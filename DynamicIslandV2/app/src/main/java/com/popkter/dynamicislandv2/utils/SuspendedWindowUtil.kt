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
import android.view.animation.AnticipateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.transition.*
import androidx.transition.TransitionSet.ORDERING_TOGETHER
import com.popkter.dynamicislandv2.R
import com.popkter.dynamicislandv2.common.EaseCubicInterpolator
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
    private var clickNum = 0

    private lateinit var wM: WindowManager
    private lateinit var fvRootView: View
    private lateinit var lpRootView: LayoutParams
    private lateinit var sceneRoot: ViewGroup
    private lateinit var sceneEmpty: SceneWithId
    private lateinit var sceneOne: SceneWithId
    private lateinit var sceneTwo: SceneWithId
    private lateinit var sceneThree: SceneWithId
    private lateinit var lastScene: SceneWithId
    private lateinit var currentScene: SceneWithId

    private lateinit var animationEnlarge: Transition
    private lateinit var animationShrink: Transition

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
        sceneEmpty =
            SceneWithId(Scene.getSceneForLayout(sceneRoot, R.layout.empty_scene, context), 0)
        sceneOne = SceneWithId(Scene.getSceneForLayout(sceneRoot, R.layout.single_asr, context), 1)
        sceneTwo =
            SceneWithId(Scene.getSceneForLayout(sceneRoot, R.layout.asr_with_toast, context), 2)
        sceneThree =
            SceneWithId(Scene.getSceneForLayout(sceneRoot, R.layout.asr_with_image, context), 3)
        lastScene = sceneEmpty
        currentScene = sceneOne
        //animation = TransitionInflater.from(context).inflateTransition(R.transition.fade_transition)

        animationEnlarge = TransitionSet().apply {
            addTransition(ChangeImageTransform())
            addTransition(ChangeBounds().apply { interpolator = OvershootInterpolator(1F) })
            addTransition(ChangeClipBounds())
        }

        animationShrink = TransitionSet().apply {
            addTransition(ChangeImageTransform())
            addTransition(ChangeBounds().apply { interpolator = EaseCubicInterpolator() })
            addTransition(Fade())
        }

        sceneRoot.addOnLayoutChangeListener(this)
        sceneRoot.setOnClickListener {
//            when (Random.nextInt(3) % 3) {
            when((clickNum++)%3){
                0 -> isSingleAsrVisible.postValue(true)
                1 -> isAsrWithToastVisible.postValue(true)
                2 -> isAsrWithImageVisible.postValue(true)
                3 -> {
                    /*isEmptyScene.postValue(true)
                    lifecycleOwner.lifecycleScope.launch {
                        delay(1000)
                        isSingleAsrVisible.postValue(true)
                    }*/
                }
                else -> {}
            }
        }

        isEmptyScene.observe(lifecycleOwner) { it ->
            lastScene = currentScene
            it?.let {
                currentScene = sceneEmpty
                animate()
            }
        }

        isSingleAsrVisible.observe(lifecycleOwner) {
            lastScene = currentScene
            it?.let {
                currentScene = sceneOne
                animate()
            }
        }

        isAsrWithToastVisible.observe(lifecycleOwner) {
            lastScene = currentScene
            it?.let {
                currentScene = sceneTwo
                animate()
            }
        }

        isAsrWithImageVisible.observe(lifecycleOwner) {
            lastScene = currentScene
            it?.let {
                currentScene = sceneThree
                animate()
            }
        }

    }

    private fun animate() {
        currentScene.scene.let { scene ->
            TransitionManager.go(
                scene,
                if (currentScene > lastScene)
                    animationEnlarge.apply { duration = 300 }
                else
                    animationShrink.apply { duration = 300 })
        }
        when (currentScene.index) {
            1 -> fvRootView.findViewById<TextView>(R.id.single_asr_asr)?.text = "Halo World!"
            2 -> {
                fvRootView.findViewById<TextView>(R.id.asr_with_toast_asr)?.text = "Halo World!"
                fvRootView.findViewById<TextView>(R.id.asr_with_toast_tip)?.text = "Android"
            }
            3 -> fvRootView.findViewById<TextView>(R.id.asr_with_image_asr)?.text = "Halo World!"

            else -> {}

        }
    }

    fun showWindow() {
        CommonUtils.checkSuspendedWindowPermission(context as Activity) {
            wM.addView(fvRootView, lpRootView)
            lifecycleOwner.lifecycleScope.launch {
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

    data class SceneWithId(val scene: Scene, val index: Int) {

        operator fun compareTo(sceneWithId: SuspendedWindowUtil.SceneWithId): Int {
            return this.index - sceneWithId.index
        }
    }

}