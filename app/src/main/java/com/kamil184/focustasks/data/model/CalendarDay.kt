package com.kamil184.focustasks.data.model

import java.util.*

data class CalendarDay(
    val calendar: Calendar,
    val isToday: Boolean,
    var isSelected: Boolean,
) {
    fun getText() = calendar.get(Calendar.DAY_OF_MONTH).toString()
}