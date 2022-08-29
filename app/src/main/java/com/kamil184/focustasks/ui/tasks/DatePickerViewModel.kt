package com.kamil184.focustasks.ui.tasks

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kamil184.focustasks.model.Repeat

class DatePickerViewModel : ViewModel() {

    val repeat = MutableLiveData<Repeat?>()

    fun getTimeText(hour: Int, minute: Int, isSystem24Hour: Boolean) =
        if (isSystem24Hour) {
            "${hour}:${minute}"
        } else {
            val isAm = hour < 12
            var h = if (isAm) hour
            else hour - 12
            if (h == 0) h = 12
            val amPmText = if (isAm) "AM" else "PM"
            val m =
                if (minute < 10) "0${minute}" else "${minute}"
            "$amPmText $h:$m"
        }
}