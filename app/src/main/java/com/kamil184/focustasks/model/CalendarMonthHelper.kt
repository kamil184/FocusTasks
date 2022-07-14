package com.kamil184.focustasks.model

import android.content.Context
import com.kamil184.focustasks.R
import java.util.*

class CalendarMonthHelper {
    companion object {
        private val calendar2000 = Calendar.getInstance()
        val today: Calendar = Calendar.getInstance()

        init {
            removeTimeFromCalendar(today)

            calendar2000.set(Calendar.YEAR, 2000)
            calendar2000.set(Calendar.DAY_OF_YEAR, 1)
            calendar2000.set(Calendar.WEEK_OF_YEAR, 1)
            calendar2000.set(Calendar.MONTH, Calendar.JANUARY)
            calendar2000.set(Calendar.DAY_OF_MONTH, 1)
            removeTimeFromCalendar(calendar2000)
        }

        val allMonths = getMonths()

        fun getMonthTitle(context: Context, c: Calendar): String {
            val months = context.resources.getStringArray(R.array.months)
            val month = months[c.get(Calendar.MONTH)]
            return "$month ${c.get(Calendar.YEAR)}"
        }

        fun getDaysHeadersList(context: Context): Array<String> {
            val daysStringArray = context.resources.getStringArray(R.array.calendar_days)
            val days = Array(7) { "" }
            for (i in 0..6) {
                var dayId = i + (today.firstDayOfWeek - 1)
                if (dayId > 6) dayId -= 7
                days[i] = daysStringArray[dayId]
            }
            return days
        }

        private fun getMonths(): Array<Calendar> {
            val c = calendar2000.clone() as Calendar
            c.add(Calendar.MONTH, -1)
            val months: Array<Calendar> = Array(1200) {
                c.add(Calendar.MONTH, 1)
                c.clone() as Calendar
            } // 1200 = 12 * 100
            return months
        }

        fun getMonthId(calendar: Calendar): Int {
            val yearsDiff = calendar.get(Calendar.YEAR) - calendar2000.get(Calendar.YEAR)
            val monthDiff = calendar.get(Calendar.MONTH) - calendar2000.get(Calendar.MONTH)
            return yearsDiff * 12 + monthDiff
        }

        /**
         * calculate what day of the week the first day of the month is
         */
        private fun getFirstDayOfWeekOfMonth(calendar: Calendar): Int {
            val copy = calendar.clone() as Calendar
            copy.set(Calendar.DAY_OF_MONTH, 1)
            var firstDayOfWeekOfMonth = copy.get(Calendar.DAY_OF_WEEK) - copy.firstDayOfWeek + 1
            if (firstDayOfWeekOfMonth < 1) firstDayOfWeekOfMonth += 7
            return firstDayOfWeekOfMonth
        }

        private fun removeTimeFromCalendar(calendar: Calendar) {
            calendar[Calendar.HOUR_OF_DAY] = 0
            calendar[Calendar.MINUTE] = 0
            calendar[Calendar.SECOND] = 0
            calendar[Calendar.MILLISECOND] = 0
        }

        fun getDayId(calendar: Calendar) =
            getFirstDayOfWeekOfMonth(calendar) + calendar.get(Calendar.DAY_OF_MONTH) - 2

        fun isDatesEquals(c1: Calendar, c2: Calendar) =
            c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                    c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH) &&
                    c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH)

    }

    val selectedDay = SelectedDay(today)

    //TODO: RTL support
    fun getDaysOfMonth(c: Calendar): Array<CalendarDay?> {
        val days: Array<CalendarDay?> = Array(42) { null } // 42 = 6*7
        val calendar: Calendar = c.clone() as Calendar

        val firstDayOfWeekOfMonth = getFirstDayOfWeekOfMonth(calendar)

        // fill the array, leaving empty fields that are not included in the current month
        for (i in firstDayOfWeekOfMonth - 1 until calendar.getActualMaximum(Calendar.DAY_OF_MONTH) + firstDayOfWeekOfMonth - 1) {
            days[i] = getDay(calendar)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return days
    }
    private fun getDay(calendar: Calendar): CalendarDay {
        val day = calendar.clone() as Calendar

        val isToday = isDatesEquals(calendar, today)

        val isSelected = isDatesEquals(calendar, selectedDay.calendar)

        return CalendarDay(day, isToday, isSelected)
    }
}