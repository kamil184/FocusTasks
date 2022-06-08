package com.kamil184.focustasks.model

import android.util.Log
import java.util.*

class CalendarMonthHelper {
    private var currentMonth = Calendar.getInstance() // DAY_OF_MONTH is 1
    private val today = Calendar.getInstance()
    private var selectedDay = SelectedDay(Calendar.getInstance())

    init {
        setCurrentMonthToday()
        removeTimeFromCalendar(today)
        removeTimeFromCalendar(selectedDay.calendar)
    }

    fun getDaysOfMonth(): Array<CalendarDay?> {
        val days: Array<CalendarDay?> = Array(42) { null } // 42 = 6*7
        val calendar: Calendar = currentMonth.clone() as Calendar

        val firstDayOfWeekOfMonth = getFirstDayOfWeekOfMonth(currentMonth)

        // fill the array, leaving empty fields that are not included in the current month
        for (i in firstDayOfWeekOfMonth - 1 until calendar.getActualMaximum(Calendar.DAY_OF_MONTH) + firstDayOfWeekOfMonth - 1) {
            days[i] = getDay(calendar)
            if (days[i]!!.isSelected) selectedDay =
                SelectedDay(days[i]!!.calendar.clone() as Calendar, i)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return days
    }

    // calculate what day of the week the first day of the month is
    private fun getFirstDayOfWeekOfMonth(calendar: Calendar): Int {
        val copy = calendar.clone() as Calendar
        copy.set(Calendar.DAY_OF_MONTH, 1)
        var firstDayOfWeekOfMonth = copy.get(Calendar.DAY_OF_WEEK) - copy.firstDayOfWeek + 1
        if (firstDayOfWeekOfMonth < 1) firstDayOfWeekOfMonth += 7
        return firstDayOfWeekOfMonth
    }

    private fun getDay(calendar: Calendar): CalendarDay {
        val day = calendar.clone() as Calendar

        var isSelected = false
        val type: CalendarDay.Type
        when {
            calendar.before(today) || calendar.after(today) -> type = CalendarDay.Type.NORMAL
            else -> {
                isSelected = true
                type = CalendarDay.Type.TODAY
            }
        }

        return CalendarDay(day, type, isSelected)
    }

    private fun removeTimeFromCalendar(calendar: Calendar) {
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
    }

    fun swipeRight() {
        currentMonth.add(Calendar.MONTH, 1)
    }

    fun swipeLeft() {
        currentMonth.add(Calendar.MONTH, -1)
    }

    fun setCurrentMonthToday() {
        currentMonth = Calendar.getInstance()
        removeTimeFromCalendar(currentMonth)
        currentMonth.set(Calendar.DAY_OF_MONTH, 1)
    }

    fun onSelectedDayChange(calendar: Calendar): Int {
        val lastSelectedId = selectedDay.id
        val selectedId = getFirstDayOfWeekOfMonth(calendar) + calendar.get(Calendar.DAY_OF_MONTH) - 2
        selectedDay = SelectedDay(calendar, selectedId)
        return lastSelectedId
    }
}

data class SelectedDay(var calendar: Calendar, var id: Int = -1)