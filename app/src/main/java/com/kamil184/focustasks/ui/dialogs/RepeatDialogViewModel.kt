package com.kamil184.focustasks.ui.dialogs

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kamil184.focustasks.R
import com.kamil184.focustasks.data.model.CalendarMonthsHelper
import com.kamil184.focustasks.data.model.Repeat
import com.kamil184.focustasks.util.convertToNormalDay
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
            for (i in arr.indices)
                arr[i] = days2LettersArray[convertToNormalDay(i)]
            arr
        }
    }

    fun fillLocaleDays2LettersList(context: Context) {
        if (!isDays2LettersArrayInitialized()) {
            days2LettersArray = context.resources.getStringArray(R.array.calendar_days_2_letters)
        }
    }

    @StringRes
    fun getNumberOfTheWeekInMonthStringRes() = when (CalendarMonthsHelper.today.get(Calendar.WEEK_OF_MONTH)) {
        1 -> R.string.first
        2 -> R.string.second
        3 -> R.string.third
        4 -> R.string.fourth
        else -> R.string.last
    }
}