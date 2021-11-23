package com.kamil184.focustasks.model

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
    constructor() : this(
        length = MutableLiveData(0),
        state = MutableLiveData(TimerState.Stopped),
        timeRemaining = MutableLiveData(0)
    )

    fun startTimer() = state.postValue(TimerState.Running)

    fun stopTimer() = state.postValue(TimerState.Stopped)

    fun pauseTimer() = state.postValue(TimerState.Paused)

    /*override fun toString(): String {
        return "Timer(length=${length.value}, state=${state.value}, timeRemaining=${timeRemaining.value})"
    }*/
}

enum class TimerState {
    Stopped, Paused, Running
}