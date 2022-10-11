package com.bpandof.appdeporte

import android.animation.ObjectAnimator
import android.animation.ValueAnimator.ofFloat
import android.view.View
import android.widget.LinearLayout

object Objects {

    /* FUNCIONES DE ANIMACION Y CAMBIOS DE ATRIBUTOS */
    fun setHeightLinearLayout(ly: LinearLayout, value: Int){
        val params:LinearLayout.LayoutParams = ly.layoutParams as LinearLayout.LayoutParams
        params.height = value
        ly.layoutParams = params
    }

    fun animateViewofInt(v: View, attr:String, value: Int, time:Long){

        ObjectAnimator.ofInt(v,attr,value).apply {
            duration = time
            start()
        }
    }

    fun animateViewofFloat(v: View, attr:String, value: Float, time:Long){

        ObjectAnimator.ofFloat(v,attr,value).apply {
            duration = time
            start()
        }
    }

}