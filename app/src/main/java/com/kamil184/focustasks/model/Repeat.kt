package com.kamil184.focustasks.model

import android.content.Context
import android.os.Parcelable
import com.kamil184.focustasks.R
import com.kamil184.focustasks.model.CalendarMonthHelper.Companion.localeDays2LettersArray
import com.kamil184.focustasks.model.CalendarMonthHelper.Companion.today
import kotlinx.parcelize.Parcelize

//TODO: make russia text normal (просколнять все)
@Parcelize
enum class Repeat: Parcelable {
    DAY {
        override fun getText(context: Context): String {
            checkCount()

            return if (count == 1) context.getString(R.string.daily)
            else
                context.getString(R.string.every) + " $count " + context.getString(R.string.days)
        }
    },
    WEEK {

        /**
         * @param info must be initialised as Array<Boolean> before calling getText()
         */
        override fun getText(context: Context): String {
            checkCount()
            if (info == null) throw IllegalArgumentException("variable info isn't initialised!")
            if (info !is Array<*>) throw IllegalArgumentException("variable info isn't right datatype (Array<Boolean>)!")

            val days = (info as Array<*>).filterIsInstance<Boolean>()
            var isDaily = true
            if (count != 1) isDaily = false
            var endText = " ("
            for (i in days.indices) {
                if (!days[i]) isDaily = false
                else endText += localeDays2LettersArray[i].toString() + ", "
            }
            endText = endText.dropLast(2) + ")"
            if (isDaily) return context.getString(R.string.daily)
            if (count == 1) return context.getString(R.string.weekly) + endText
            return context.getString(R.string.every) + " $count " + context.getString(R.string.weeks) + endText
        }
    },
    MONTH {

        /**
         * @param info must be initialised as Int or Pair<Int,Int> before calling getText()
         * if info is Int, then it must be 1.32 (32 is last days index)
         * if info is Pair<Int,Int>, then first in 1..5, second in 1..7
         */
        override fun getText(context: Context): String {
            checkCount()
            if (info == null) throw IllegalArgumentException("variable info isn't initialised!")

            val textBegin =
                if (count == 1) context.getString(R.string.monthly) + " "
                else context.getString(R.string.every) + " $count " + context.getString(R.string.month) + " "
            when (info) {
                is Int -> {
                    val intInfo = info as Int
                    if (intInfo !in 1..32)
                        throw IllegalArgumentException("variable info must be in 1..32")

                    return if (intInfo in 1..31)
                        textBegin + "on the ${intInfo}rd"
                    else textBegin + "on the last day"

                }
                is Pair<*, *> -> {
                    val pairInfo = info as Pair<*, *>
                    val textMiddle =
                        "${context.getText(R.string.every)} " + when (pairInfo.first as Int) {
                            1 -> context.getText(R.string.first)
                            2 -> context.getText(R.string.second)
                            3 -> context.getText(R.string.third)
                            4 -> context.getText(R.string.fourth)
                            5 -> context.getText(R.string.last)
                            else -> throw IllegalArgumentException("first variable in info must be in 1..5")
                        }

                    if (pairInfo.second as Int !in 1..7) throw IllegalArgumentException("second variable in info must be in 1..7")
                    val normalDaysStringArray =
                        context.resources.getStringArray(R.array.calendar_days_2_letters)
                    val textEnd = normalDaysStringArray[pairInfo.second as Int - 1]

                    return "$textBegin$textMiddle $textEnd"

                }
                else -> throw IllegalArgumentException("variable info isn't right datatype (Int or Pair<Int, Int>)!")
            }
        }
    },
    YEAR {
        override fun getText(context: Context): String {
            checkCount()

            return if (count == 1) context.getString(R.string.yearly)
            else context.getString(R.string.every) + " $count " + context.getString(R.string.year)
        }
    };

    var count: Int? = null

    //normal - USA (first day - sunday), local - all others
    /**
     * if it's WEEK it can be Array<Boolean>
     *
     * if it's MONTH it can be Int in 1.32 (32 is last days index)
     * or Pair<Int,Int>, then first in 1..5, second in 1..7 (normal id: 1 - sunday)
     */
    var info: Any? = null

    abstract fun getText(context: Context): String

    protected fun checkCount() {
        if (count == null) throw IllegalArgumentException("variable count isn't initialised!")
        if (count!! < 1) throw IllegalArgumentException("variable count must be > 0")
    }

    fun getNameRes() = when(ordinal){
        DAY.ordinal -> R.string.day
        WEEK.ordinal -> R.string.week
        MONTH.ordinal -> R.string.month
        YEAR.ordinal -> R.string.year
        else -> throw IllegalArgumentException("Repeat text must be day, week, month or year")
    }
}
