package com.kamil184.focustasks.model

import com.kamil184.focustasks.TimerPreferences

// A timer for TimerService
data class NonLiveDataTimer(
    var length: Int,
    var state: TimerState,
    var timeRemaining: Int,
){
    constructor(length: Int, state: TimerPreferences.TimerState?, timeRemaining: Int) : this(
        length = length,
        state = TimerState.values()[state!!.ordinal],
        timeRemaining = timeRemaining
    )

    fun getTimeRemainingText(): String =
        "${(timeRemaining / 60000)}:${(if (timeRemaining % 60000 / 1000 > 9) timeRemaining % 60000 / 1000 else "0" + timeRemaining % 60000 / 1000)}"

    override fun toString(): String {
        return "NonLiveDataTimer(length=${length}, state=${state}, timeRemaining=${timeRemaining})"
    }
}