package com.example.gmapsample.ui.component

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import com.example.gmapsample.Utils

class SelectCell : LinearLayout {
    var imageView: ImageView
    var textView: TextView

    constructor(context: Context) : super(context) {
        orientation = VERTICAL
        setBackgroundDrawable(Utils.getDrawableWithRadius())

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