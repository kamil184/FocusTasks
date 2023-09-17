package com.kamil184.focustasks.data.model

import android.content.res.Resources
import android.os.Parcelable
import com.kamil184.focustasks.R
import com.kamil184.focustasks.ui.dialogs.RepeatDialogViewModel
import com.kamil184.focustasks.util.getTodayLocalIndex
import com.kamil184.focustasks.util.monthInfoToString
import com.kamil184.focustasks.util.stringToMonthInfo
import com.kamil184.focustasks.util.stringToWeekInfo
import com.kamil184.focustasks.util.weekInfoToString
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

//TODO: make russia text normal (просколнять все)
abstract class Repeat:Parcelable {
    abstract var count: Int
    abstract var info: String

    abstract fun getText(resources: Resources): String

    abstract fun getNameRes(): Int

    /**
     * has to be invoked before saving into database
     */
    abstract fun updateInfo()

    companion object {
        fun getBasicInstance(): Repeat {
            val instance = Week()
            instance.info = weekInfoToString(Week.getTodayWeekInfo())
            return instance
        }
    }
}

@Parcelize
data class Day(override var count: Int = 1, override var info: String = "") : Repeat() {
    override fun getText(resources: Resources): String {
        return if (count == 1) resources.getString(R.string.daily)
        else
            resources.getString(R.string.every) + " $count " + resources.getString(R.string.days)
    }

    override fun getNameRes() = R.string.day
    override fun updateInfo() {
        info = ""
    }
}

@Parcelize
data class Week(override var count: Int = 1, override var info: String = "") : Repeat()  {
    @IgnoredOnParcel
    var weekInfo: BooleanArray = stringToWeekInfo(info)
    constructor(weekInfo: BooleanArray):this(info = weekInfoToString(weekInfo))
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
    override fun updateInfo() {
        info = weekInfoToString(weekInfo)
    }

    companion object {
        fun getTodayWeekInfo(): BooleanArray {
            val weekInfo = BooleanArray(7)
            weekInfo[getTodayLocalIndex()] = true
            return weekInfo
        }
    }

}

@Parcelize
data class Month(override var count: Int = 1, override var info: String = "") : Repeat()  {
    /**
     * it has to be in 1.32 (32 is last days index)
     */
    @IgnoredOnParcel
    private var _monthInfoInt: Int? = null
    var monthInfoInt
        get() = _monthInfoInt
        set(value) {
            _monthInfoInt = value
            _monthInfoPair = null
        }

    /**
     * first has to be in 1..5, second - in 1..7
     */

    @IgnoredOnParcel
    private var _monthInfoPair: Pair<Int, Int>? = null
    var monthInfoPair
        get() = _monthInfoPair
        set(value) {
            _monthInfoPair = value
            _monthInfoInt = null
        }

    init {
        val monthInfo = stringToMonthInfo(info)
        if (monthInfo is Int) monthInfoInt = monthInfo
        else if (monthInfo is Pair<*, *>) monthInfoPair = _monthInfoPair
    }

    override fun getText(resources: Resources): String {
        if (monthInfoPair == null && monthInfoInt == null) throw IllegalArgumentException("one of the fields has to be initialized")
        if (monthInfoPair != null && monthInfoInt != null) throw IllegalArgumentException("only one of the fields has to be initialized")

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
    override fun updateInfo() {
        info = monthInfoToString(monthInfoInt, monthInfoPair)
    }

}

@Parcelize
data class Year(override var count: Int = 1, override var info: String = "") : Repeat()  {
    override fun getText(resources: Resources): String {
        return if (count == 1) resources.getString(R.string.yearly)
        else resources.getString(R.string.every) + " $count " + resources.getString(R.string.year)
    }

    override fun getNameRes() = R.string.year
    override fun updateInfo() {
        info = ""
    }
}
