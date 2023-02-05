package com.example.notekeeper.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.Typeface
import android.graphics.fonts.Font
import android.view.View
import android.widget.TextView

fun setFadeAnimation(view: View, visibility: String)
{
    if(visibility == "gone") {
        view.animate().alpha(0f).setDuration(300).setListener(object : AnimatorListenerAdapter(){
            override fun onAnimationEnd(animation: Animator?) {
                view.visibility = View.GONE }
        })
    }
    else if(visibility == "visible") {
        view.animate().alpha(1f).setDuration(300).setListener(object : AnimatorListenerAdapter(){
            override fun onAnimationStart(animation: Animator?) {
                view.visibility = View.VISIBLE }
        })
    }
}


fun TextView.setCustomFont(font: Int, context: Context)
{
    this.typeface = context.resources.getFont(font)
}