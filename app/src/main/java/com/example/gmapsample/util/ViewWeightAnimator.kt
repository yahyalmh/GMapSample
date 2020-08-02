package com.example.gmapsample.util

import android.view.View
import android.widget.LinearLayout


class ViewWeightAnimator {
    private lateinit var view: View

    constructor(view: View) {
        if (view.layoutParams is LinearLayout.LayoutParams) {
            this.view = view
        } else {
            throw IllegalArgumentException("The view should have LinearLayout as parent")
        }
    }

    fun setWeight(weight: Float) {
        val params = view.layoutParams as LinearLayout.LayoutParams
        params.weight = weight
        view.parent.requestLayout()
    }

    fun getWeight(): Float {
        return (view.layoutParams as LinearLayout.LayoutParams).weight
    }
    /*
    // Sample usage:

    val mapAnimationWrapper = ViewWeightAnimator(view)
        val animation = ObjectAnimator.ofFloat(mapAnimationWrapper,
                "weight",
                50,
                100)

        animation.duration = 800
        animation.start()*/
}