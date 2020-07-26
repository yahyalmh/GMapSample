package com.example.gmapsample.ui.component

import android.graphics.Color
import android.graphics.drawable.GradientDrawable

class RoundedDrawable : GradientDrawable {
    private var radius = 0f

    constructor(
        orientation: Orientation?,
        colors: IntArray,
        radius: Float
    ) : super(orientation, colors) {
        this.radius = radius
        this.colors = colors
        this.orientation = orientation

        gradientType = GradientDrawable.LINEAR_GRADIENT
        cornerRadii = floatArrayOf(
            radius,
            radius,
            radius,
            radius,
            radius,
            radius,
            radius,
            radius
        )
    }

    constructor(colors: IntArray, radius: Float) :this(GradientDrawable.Orientation.BR_TL, colors, radius) { }
    constructor( radius: Float) :this(GradientDrawable.Orientation.BR_TL, intArrayOf(Color.GRAY, Color.DKGRAY), radius) { }

}