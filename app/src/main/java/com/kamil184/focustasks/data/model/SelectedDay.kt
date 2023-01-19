package com.kamil184.focustasks.data.model

import java.util.*

class SelectedDay(calendar: Calendar, id: Int = -1) {

    var calendar = calendar
        private set
    var id = id
        private set
    private var listener: OnSelectedDayChangedListener? = null

    fun update(calendar: Calendar, id: Int, listener: OnSelectedDayChangedListener) {
        this.calendar = calendar
        this.id = id
        this.listener?.onSelectedDayChanged()
        this.listener = listener
    }
}

interface OnSelectedDayChangedListener {
    fun onSelectedDayChanged()
}