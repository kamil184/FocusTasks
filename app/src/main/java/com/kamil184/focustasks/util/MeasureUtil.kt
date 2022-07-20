package com.kamil184.focustasks.util

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.util.TypedValue
import kotlin.math.roundToInt

fun dpToPx(context: Context?, dp: Int): Int {
    context?.let {
        val displayMetrics = it.resources.displayMetrics
        return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }
    return 0
}

fun getColorFromAttr(context: Context, attrId: Int): Int {
    val typedValue = TypedValue()
    context.theme.resolveAttribute(attrId, typedValue, true)
    val arr: TypedArray =
        context.obtainStyledAttributes(typedValue.data, intArrayOf(attrId))
    val primaryColor = arr.getColor(0, -1)
    arr.recycle()
    return primaryColor
}