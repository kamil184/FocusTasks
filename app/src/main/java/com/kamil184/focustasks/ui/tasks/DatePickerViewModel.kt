package com.kamil184.focustasks.ui.tasks

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kamil184.focustasks.R
import com.kamil184.focustasks.model.CalendarMonthsHelper
import com.kamil184.focustasks.model.Task
import java.util.*

class DatePickerViewModel : ViewModel() {

    val task = MutableLiveData<Task>()
    val taskValue: Task get() = task.value!!

    fun getDaysHeadersList(context: Context): Array<String> {
        val daysStringArray = context.resources.getStringArray(R.array.calendar_days_1_letter)
        val days = Array(7) { "" }
        for (i in 0..6) {
            var dayId = i + (CalendarMonthsHelper.today.firstDayOfWeek - 1)
            if (dayId > 6) dayId -= 7
            days[i] = daysStringArray[dayId]
        }
        return days
    }

    fun getIdOfMonthIn21Century(calendar: Calendar): Int {
        val yearsDiff = calendar.get(Calendar.YEAR) - 2000
        return yearsDiff * 12 + calendar.get(Calendar.MONTH)
    }

    fun getMonthTitle(context: Context, c: Calendar): String {
        val months = context.resources.getStringArray(R.array.months)
        val month = months[c.get(Calendar.MONTH)]
        return "$month ${c.get(Calendar.YEAR)}"
    }

    fun getTimeText(hour: Int, minute: Int, isSystem24Hour: Boolean): String {
        val m =
            if (minute < 10) "0$minute" else "$minute"

        return if (isSystem24Hour) {
            val h =
                if (hour < 10) "0$hour" else "$hour"
            "$h:$m"
        } else {
            val isAm = hour < 12
            var hAmPm = if (isAm) hour
            else hour - 12
            if (hAmPm == 0) hAmPm = 12
            val h =
                if (hAmPm < 10) "0$hAmPm" else "$hAmPm"
            val amPmText = if (isAm) "AM" else "PM"
            "$amPmText $h:$m"
        }
    }
}