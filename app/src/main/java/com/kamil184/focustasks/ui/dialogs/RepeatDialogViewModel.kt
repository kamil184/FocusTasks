package com.kamil184.focustasks.ui.dialogs

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kamil184.focustasks.R
import com.kamil184.focustasks.data.model.CalendarMonthsHelper
import com.kamil184.focustasks.data.model.Repeat
import java.util.*

class RepeatDialogViewModel : ViewModel() {

    val repeat = MutableLiveData<Repeat?>()

    companion object {
        lateinit var days2LettersArray: Array<String>

        private fun isDays2LettersArrayInitialized() = this::days2LettersArray.isInitialized

        /**
         * Before call getter, you should init days2LettersArray
         */
        val localeDays2LettersArray: Array<String> by lazy {
            val arr = Array(7) { "" }
            val diff = CalendarMonthsHelper.today.firstDayOfWeek - 1
            for (i in arr.indices) {
                if (i + diff > 6)
                    arr[i] = days2LettersArray[i + diff - 7]
                else
                    arr[i] = days2LettersArray[i + diff]
            }
            arr
        }
    }

    fun fillLocaleDays2LettersList(context: Context) {
        if (!isDays2LettersArrayInitialized()) {
            days2LettersArray = context.resources.getStringArray(R.array.calendar_days_2_letters)
        }
    }

    fun getLocaleDaysArrayTodayIndex() =
        getLocaleDaysArrayIndex(CalendarMonthsHelper.today.get(Calendar.DAY_OF_WEEK) - 1)

    /**
     * @param i is index of normal array (USA: first day is Sunday); i is in [0;6]
     */
    fun getLocaleDaysArrayIndex(i: Int): Int {
        if (i !in 0..6) throw IllegalArgumentException("index should be [0;6]")
        val diff = CalendarMonthsHelper.today.firstDayOfWeek - 1
        var id = i - 1 - diff
        if (id < 0) id += 7
        return id
    }
}