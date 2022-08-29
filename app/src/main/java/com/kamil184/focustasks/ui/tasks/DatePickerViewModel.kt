package com.kamil184.focustasks.ui.tasks

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kamil184.focustasks.model.Repeat

class DatePickerViewModel : ViewModel() {

    val repeat = MutableLiveData<Repeat?>()

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