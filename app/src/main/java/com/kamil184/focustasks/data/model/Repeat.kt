package com.kamil184.focustasks.data.model

import android.content.res.Resources
import android.os.Parcelable
import androidx.annotation.StringRes
import com.kamil184.focustasks.R
import com.kamil184.focustasks.data.model.CalendarMonthsHelper.Companion.today
import com.kamil184.focustasks.ui.dialogs.RepeatDialogViewModel
import com.kamil184.focustasks.util.getTodayLocalIndex
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
//TODO: make russia text normal (просколнять все)
enum class Repeat(
    var count: Int = 1,
    var weekInfo: BooleanArray? = null,
    private var _monthInfoInt: Int? = null,
    private var _monthInfoPair: Pair<Int, Int>? = null,
) : Parcelable {
    DAY {
        override fun getText(resources: Resources): String {
            return if (count == 1) resources.getString(R.string.daily)
            else
                resources.getString(R.string.every) + " $count " + resources.getString(R.string.days)
        }
    },
    WEEK {
        override fun getText(resources: Resources): String {
            if (weekInfo == null) throw IllegalArgumentException("variable weekInfo isn't initialised!")

            val days = weekInfo!!
            var isDaily = true
            if (count != 1) isDaily = false
            var endText = " ("
            for (i in days.indices) {
                if (!days[i]) isDaily = false
                else endText += RepeatDialogViewModel.localeDays2LettersArray[i] + ", "
            }
            endText = endText.dropLast(2) + ")"
            if (isDaily) return resources.getString(R.string.daily)
            if (count == 1) return resources.getString(R.string.weekly) + endText
            return resources.getString(R.string.every) + " $count " + resources.getString(R.string.weeks) + endText
        }
    },
    MONTH {
        /**
         * if monthInfoInt, then it must be 1.32 (32 is last days index)
         * if monthInfoPair, then first has to be in 1..5, second - in 1..7
         */
        override fun getText(resources: Resources): String {
            if (monthInfoPair == null && monthInfoInt == null)
                throw IllegalArgumentException("one of the variables monthInfoInt or monthInfoPair has to be initialised!")

            val textBegin =
                if (count == 1) resources.getString(R.string.monthly) + " "
                else resources.getString(R.string.every) + " $count " + resources.getString(R.string.month) + " "
            if (monthInfoInt != null) {
                if (monthInfoInt !in 1..32)
                    throw IllegalArgumentException("variable info must be in 1..32")

                return if (monthInfoInt in 1..31)
                    textBegin + resources.getString(R.string.on_the_xx_rd, monthInfoInt)
                else textBegin + resources.getString(R.string.on_the_last_day)

            } else {
                val pair = monthInfoPair!!
                val textMiddle =
                    "${resources.getText(R.string.every)} " + when (pair.first) {
                        1 -> resources.getText(R.string.first)
                        2 -> resources.getText(R.string.second)
                        3 -> resources.getText(R.string.third)
                        4 -> resources.getText(R.string.fourth)
                        5 -> resources.getText(R.string.last)
                        else -> throw IllegalArgumentException("first variable in info must be in 1..5")
                    }

                if (pair.second !in 1..7) throw IllegalArgumentException("second variable in info must be in 1..7")
                val normalDaysStringArray =
                    resources.getStringArray(R.array.calendar_days_2_letters)
                val textEnd = normalDaysStringArray[pair.second]

                return "$textBegin$textMiddle $textEnd"
            }
        }
    },
    YEAR {
        override fun getText(resources: Resources): String {
            return if (count == 1) resources.getString(R.string.yearly)
            else resources.getString(R.string.every) + " $count " + resources.getString(R.string.year)
        }
    };

    var monthInfoInt
        get() = _monthInfoInt
        set(value) {
            _monthInfoInt = value
            _monthInfoPair = null
        }

    var monthInfoPair
        get() = _monthInfoPair
        set(value) {
            _monthInfoPair = value
            _monthInfoInt = null
        }

    abstract fun getText(resources: Resources): String

    fun getNameRes() = when (ordinal) {
        DAY.ordinal -> R.string.day
        WEEK.ordinal -> R.string.week
        MONTH.ordinal -> R.string.month
        YEAR.ordinal -> R.string.year
        else -> throw IllegalArgumentException("Repeat text must be day, week, month or year")
    }

    companion object {
        fun getBasicInstance(): Repeat {
            val repeat = WEEK
            val info = BooleanArray(7)
            info[getTodayLocalIndex()] = true
            repeat.weekInfo = info
            return repeat
        }

        @StringRes
        fun getNumberOfTheWeekInMonthStringRes() = when (today.get(Calendar.WEEK_OF_MONTH)) {
            1 -> R.string.first
            2 -> R.string.second
            3 -> R.string.third
            4 -> R.string.fourth
            else -> R.string.last
        }
    }
}
