package com.kamil184.focustasks.util

import com.kamil184.focustasks.data.model.Week

fun weekInfoToString(weekInfo: BooleanArray): String {
    val stringBuilder = StringBuilder()
    weekInfo.forEach { stringBuilder.append(if (it) '1' else '0') }
    return stringBuilder.toString()
}

fun stringToWeekInfo(string: String): BooleanArray {
    if(string.isEmpty()) return Week.getTodayWeekInfo()
    val weekInfo = BooleanArray(7)
    for (i in 0..6)
        weekInfo[i] = string[i] == '1'
    return weekInfo
}

fun monthInfoToString(monthInfoInt: Int?, monthInfoPair: Pair<Int, Int>?): String {
    if (monthInfoPair == null && monthInfoInt == null) throw IllegalArgumentException("one of the fields has to be initialized")
    if (monthInfoPair != null && monthInfoInt != null) throw IllegalArgumentException("only one of the fields has to be initialized")
    if (monthInfoInt != null) return monthInfoInt.toString()
    return monthInfoPair!!.first.toString() + ',' + monthInfoPair.second.toString()
}

fun stringToMonthInfo(string: String): Any {
    if (!string.contains(',')) return Integer.parseInt(string)
    val first = Integer.parseInt(string.substringBefore(','))
    val second = Integer.parseInt(string.substringAfter(','))
    return Pair(first, second)
}