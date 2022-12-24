package com.kamil184.focustasks.model

import java.util.*

class CalendarMonthsHelper {
    companion object {
        private val calendar2000: Calendar = Calendar.getInstance()
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

        /**
         * months from 2000 to 2099 (1200 elements)
         */
        val allMonthsIn21Century = getMonths()

        private fun getMonths(): Array<Calendar> {
            val c = calendar2000.clone() as Calendar
            c.add(Calendar.MONTH, -1)
            val months: Array<Calendar> = Array(1200) {
                c.add(Calendar.MONTH, 1)
                c.clone() as Calendar
            } // 1200 = 12 * 100
            return months
        }

        private fun removeTimeFromCalendar(calendar: Calendar) {
            calendar[Calendar.HOUR_OF_DAY] = 0
            calendar[Calendar.MINUTE] = 0
            calendar[Calendar.SECOND] = 0
            calendar[Calendar.MILLISECOND] = 0
        }
    }
}