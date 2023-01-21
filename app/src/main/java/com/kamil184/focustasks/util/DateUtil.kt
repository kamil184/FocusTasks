package com.kamil184.focustasks.util

import com.kamil184.focustasks.data.model.CalendarMonthsHelper
import java.util.Calendar

/**
 * @param localDayId is an index of a local day; it has to be in [0;6]
 * For example, Wednesday in the US is the fourth day of the week, while in Germany it is the third.
 * So if you are in Germany normalDayId = 3, localDayId = 2
 */
fun convertToNormalDay(localDayId: Int): Int {
    if (localDayId !in 0..6) throw IllegalArgumentException("day id must be in 0..6")
    val diff = CalendarMonthsHelper.today.firstDayOfWeek - 1
    val normalDayId = localDayId + diff
    if (normalDayId !in 0..6) return normalDayId - 7
    return normalDayId
}

/**
 * @param normalDayId is an index of a normal day (USA: first day is Sunday); it has to be in [0;6]
 * For example, Wednesday in the US is the fourth day of the week, while in Germany it is the third.
 * So if you are in Germany normalDayId = 3, localDayId = 2
 */
fun convertToLocalDay(normalDayId: Int): Int {
    if (normalDayId !in 0..6) throw IllegalArgumentException("day id must be in 0..6")
    val diff = CalendarMonthsHelper.today.firstDayOfWeek - 1
    val localDayId = normalDayId - diff
    Calendar.SUNDAY
    if (localDayId !in 0..6) return localDayId + 7
    return localDayId
}

fun getTodayLocalIndex() =
    convertToLocalDay(CalendarMonthsHelper.today.get(Calendar.DAY_OF_WEEK) - 1)
