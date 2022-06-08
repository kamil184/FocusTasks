package com.kamil184.focustasks.model

import java.util.*

data class CalendarDay(
    val calendar: Calendar,
    val type: Type,
    var isSelected: Boolean,
) {
    fun getText() = calendar.get(Calendar.DAY_OF_MONTH).toString()
    enum class Type {
        TODAY, NORMAL
    }
}