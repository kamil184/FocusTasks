package com.kamil184.focustasks.data.model

import android.content.res.Resources
import android.os.Parcelable
import androidx.room.ColumnInfo
import com.kamil184.focustasks.R
import com.kamil184.focustasks.ui.dialogs.RepeatDialogViewModel
import com.kamil184.focustasks.util.getTodayLocalIndex
import kotlinx.parcelize.Parcelize

//TODO: make russia text normal (просколнять все)
sealed class Repeat(
    var count: Int = 1
) : Parcelable {
    @Parcelize
    class Day : Repeat() {
        override fun getText(resources: Resources): String {
            return if (count == 1) resources.getString(R.string.daily)
            else
                resources.getString(R.string.every) + " $count " + resources.getString(R.string.days)
        }

        override fun getNameRes() = R.string.day
    }

    @Parcelize
    class Week(
        @ColumnInfo(name = "week_info")
        var weekInfo: BooleanArray = booleanArrayOf()
    ) : Repeat() {
        override fun getText(resources: Resources): String {
            val days = weekInfo
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

        override fun getNameRes() = R.string.week

    }

    @Parcelize
    class Month(
        @ColumnInfo(name = "month_info_int")
        private var _monthInfoInt: Int? = null,
        @ColumnInfo(name = "month_info_pair")
        private var _monthInfoPair: Pair<Int, Int>? = null,
    ) : Repeat() {
        /**
         * it must be 1.32 (32 is last days index)
         */
        var monthInfoInt
            get() = _monthInfoInt
            set(value) {
                _monthInfoInt = value
                _monthInfoPair = null
            }

        /**
         * first has to be in 1..5, second - in 1..7
         */
        var monthInfoPair
            get() = _monthInfoPair
            set(value) {
                _monthInfoPair = value
                _monthInfoInt = null
            }

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

        override fun getNameRes() = R.string.month

    }

    @Parcelize
    class Year : Repeat() {
        override fun getText(resources: Resources): String {
            return if (count == 1) resources.getString(R.string.yearly)
            else resources.getString(R.string.every) + " $count " + resources.getString(R.string.year)
        }

        override fun getNameRes() = R.string.year

    }

    abstract fun getText(resources: Resources): String

    abstract fun getNameRes(): Int

    companion object {
        fun getBasicInstance(): Repeat {
            val info = BooleanArray(7)
            info[getTodayLocalIndex()] = true
            return Week(info)
        }
    }
}
