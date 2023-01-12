package com.popkter.dynamicislandv2.view

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.transition.*
import androidx.transition.TransitionSet.ORDERING_TOGETHER
import com.popkter.dynamicislandv2.R
import com.popkter.dynamicislandv2.common.EaseCubicInterpolator

class SuspendedDialog : DialogFragment() {

    private lateinit var rootView: View
    private var isSceneOne = true

    /** The system calls this to get the DialogFragment's layout, regardless
    of whether it's being displayed as a dialog or an embedded fragment. */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        rootView = inflater.inflate(R.layout.suspend_window_2, container, false)
        return rootView
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        getDialog()?.window?.attributes?.windowAnimations = R.transition.fade_transition
        return dialog
    }

    override fun onStart() {
        super.onStart()
        val window = dialog?.window
        //window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val dm = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(dm)
        val lp = window?.attributes
        lp?.gravity = Gravity.TOP
        lp?.width = MATCH_PARENT
        lp?.height = MATCH_PARENT
        lp?.dimAmount = 0F
        lp?.alpha = 0F
        window?.attributes = lp
    }

    override fun onResume() {
        super.onResume()
        val sceneRoot = rootView.findViewById<ViewGroup>(R.id.scene_root)
        rootView.background.alpha = 255
        val sceneOne = Scene.getSceneForLayout(sceneRoot, R.layout.single_asr, requireContext())
        val singleAsrText = rootView.findViewById<TextView>(R.id.single_asr_asr)
        val sceneTwo = Scene.getSceneForLayout(sceneRoot, R.layout.asr_with_image, requireContext())
        val imageAsrText = rootView.findViewById<TextView>(R.id.asr_with_image_asr)
        val ani = EaseCubicInterpolator(.45, .55, .5, 1.21)
        val animation =
            TransitionSet().apply {
                addTransition(ChangeImageTransform())
                addTransition(ChangeBounds().apply { interpolator = OvershootInterpolator(1F) })
                addTransition(AutoTransition())
                ordering = ORDERING_TOGETHER
                duration = 300
                addListener(
                    object : TransitionListenerAdapter() {
                        override fun onTransitionStart(transition: Transition) {
                            rootView.findViewById<TextView>(R.id.asr_with_image_asr)?.text =
                                "Halo World!"
                            rootView.findViewById<TextView>(R.id.single_asr_asr)?.text =
                                "This is DynamicIsland V2 Demo"
                        }
                    }
                )
            }

        TransitionManager.beginDelayedTransition(sceneRoot, animation)
        rootView.setOnClickListener {
            isSceneOne = if (isSceneOne) {
                TransitionManager.go(sceneTwo, animation)
                false
            } else {
                TransitionManager.go(sceneOne, animation)
                true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}