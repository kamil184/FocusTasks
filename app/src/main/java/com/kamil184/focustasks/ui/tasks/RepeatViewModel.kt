package com.kamil184.focustasks.ui.tasks

import android.content.Context
import androidx.lifecycle.*
import com.kamil184.focustasks.R
import com.kamil184.focustasks.model.CalendarMonthHelper
import com.kamil184.focustasks.model.CalendarMonthHelper.Companion.days2LettersArray
import com.kamil184.focustasks.model.Repeat

class RepeatViewModel : ViewModel() {

    val repeat = MutableLiveData<Repeat?>()

    fun fillLocaleDays2LettersList(context: Context) {
        if (!CalendarMonthHelper.isDays2LettersArrayInitialized()) {
            days2LettersArray = context.resources.getStringArray(R.array.calendar_days_2_letters)
        }
    }

    val firstRadioBtnMenuItems: Array<CharSequence> = Array(32) { "" }

    fun fillFirstRadioBtnMenuItems(context: Context) {
        val dayString = context.getString(R.string.day)
        for (i in 1..31)
            firstRadioBtnMenuItems[i - 1] = "$i $dayString"
        firstRadioBtnMenuItems[31] = context.getString(R.string.last_day)
    }
}