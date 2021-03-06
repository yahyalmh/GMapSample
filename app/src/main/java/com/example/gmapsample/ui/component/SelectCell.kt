package com.example.gmapsample.ui.component

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setMargins
import com.example.gmapsample.util.Utils

class SelectCell : LinearLayout {
    var imageView: ImageView
    var textView: TextView

    constructor(context: Context) : super(context) {
        orientation = VERTICAL
        setBackgroundDrawable(RoundedDrawable(intArrayOf(
            Color.RED,
            Color.GREEN,
            Color.YELLOW,
            Color.CYAN
        ), 25f))

        imageView = ImageView(context)
        val layoutParams = LayoutParams(Utils.dp(100f), Utils.dp(100f))
        layoutParams.gravity = Gravity.CENTER
        layoutParams.setMargins(Utils.dp(15f))
        addView(imageView, layoutParams)

        textView = TextView(context)
        textView.textSize = 14f
        val layoutParams1 = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        layoutParams1.gravity = Gravity.CENTER
        addView(textView, layoutParams1)
    }

}