package com.kamil184.focustasks.util

import android.content.Context
import android.util.DisplayMetrics
import kotlin.math.roundToInt

fun dpToPx(context: Context?, dp: Int): Int {
    context?.let {
        val displayMetrics = it.resources.displayMetrics
        return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }
    return 0
}