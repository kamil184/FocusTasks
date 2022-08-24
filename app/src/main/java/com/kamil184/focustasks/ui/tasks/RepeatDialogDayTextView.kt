package com.kamil184.focustasks.ui.tasks

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.kamil184.focustasks.R
import com.kamil184.focustasks.util.getColorFromAttr

class RepeatDialogDayTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) :
    AppCompatTextView(context, attrs, defStyleAttr) {

    var isClicked = false

    var stateOnClickListener: StateOnClickListener? = null
        set(value) {
            field = value
            if (value != null)
                setOnClickListener {
                    if (value.isMoreThanOne() || !this.isClicked) {
                        if (isClicked) setUnClickedState() else setClickedState()
                    }
                }
        }

    fun setClickedState() {
        isClicked = true
        val states = arrayOf(intArrayOf(android.R.attr.state_enabled))
        val colors = intArrayOf(getColorFromAttr(context, R.attr.colorPrimary))
        backgroundTintList = ColorStateList(states, colors)

        setTextColor(getColorFromAttr(context, R.attr.colorOnPrimary))
    }

    fun setUnClickedState(){
        isClicked = false
        val states = arrayOf(intArrayOf(android.R.attr.state_enabled))
        val colors =
            intArrayOf(getColorFromAttr(context, R.attr.colorSurfaceVariant))
        backgroundTintList = ColorStateList(states, colors)

        setTextColor(getColorFromAttr(context, R.attr.colorOnSurfaceVariant))
    }


    interface StateOnClickListener {
        fun isMoreThanOne(): Boolean
    }
}