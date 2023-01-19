package com.kamil184.focustasks.data.model

import androidx.lifecycle.MutableLiveData
import com.kamil184.focustasks.TimerPreferences

data class Timer(
    val length: MutableLiveData<Int>,
    val state: MutableLiveData<TimerState>,
    val timeRemaining: MutableLiveData<Int>,
) {
    constructor(length: Int, state: TimerPreferences.TimerState?, timeRemaining: Int) : this(
        length = MutableLiveData(length),
        state = MutableLiveData(TimerState.values()[state!!.ordinal]),
        timeRemaining = MutableLiveData(timeRemaining)
    )

    fun changeState(timerState: TimerState) = state.postValue(timerState)

    override fun toString(): String {
        return "Timer(length=${length.value}, state=${state.value}, timeRemaining=${timeRemaining.value})"
    }
}

enum class TimerState {
    Stopped, Paused, Running
}