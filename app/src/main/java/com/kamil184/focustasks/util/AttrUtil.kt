package com.kamil184.focustasks.util

import android.content.Context
import android.content.res.TypedArray
import android.util.TypedValue

fun getColorFromAttr(context: Context, attrId: Int): Int {
    val typedValue = TypedValue()
    context.theme.resolveAttribute(attrId, typedValue, true)
    val arr: TypedArray =
        context.obtainStyledAttributes(typedValue.data, intArrayOf(attrId))
    val primaryColor = arr.getColor(0, -1)
    arr.recycle()
    return primaryColor
}