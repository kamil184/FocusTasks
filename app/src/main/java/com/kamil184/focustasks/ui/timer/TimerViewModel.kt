package com.kamil184.focustasks.ui.timer

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kamil184.focustasks.manager.TimerManager
import com.kamil184.focustasks.model.Timer
import com.kamil184.focustasks.model.TimerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class TimerViewModel(application: Application) : AndroidViewModel(application) {

    private val timerManager: TimerManager = TimerManager(application)
    private var _timer: Timer? = null
    val timer get() = _timer!!

    init {
        Log.d("TimerViewModel", "init")
        if (_timer == null)
            viewModelScope.launch(Dispatchers.IO) {
                timerManager.timerFlow.collect {
                    Log.d("TimerViewModel", "collect")
                    _timer = it
                }
            }
    }


    override fun onCleared() {
        super.onCleared()
        Log.d("TimerViewModel", "onCleared")
    }

    fun saveTimerState() = viewModelScope.launch(Dispatchers.IO) {
        timerManager.updateTimerState(timer)
    }

    fun onTimerFinished() {
        if (timer.state.value != TimerState.Stopped) timer.state.postValue(TimerState.Stopped)

        timer.timeRemaining.postValue(timer.length.value)
    }

    fun updateTimeRemaining(millisUntilFinished: Long) =
        timer.timeRemaining.postValue(millisUntilFinished.toInt())

}