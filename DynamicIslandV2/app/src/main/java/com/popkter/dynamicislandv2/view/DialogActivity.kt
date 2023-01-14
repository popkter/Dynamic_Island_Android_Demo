package com.popkter.dynamicislandv2.view

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.popkter.dynamicislandv2.R
import com.popkter.dynamicislandv2.utils.CommonUtils

class DialogActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialog)
      /*  val lp = window.attributes.apply {
            width = CommonUtils.dip2px(this@DialogActivity, 200F)
            height = CommonUtils.dip2px(this@DialogActivity, 200F)
        }
        window.attributes = lp*/
        supportActionBar?.hide()
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}