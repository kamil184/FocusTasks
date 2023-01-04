package com.kamil184.focustasks.ui.timer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kamil184.focustasks.manager.TimerManager
import com.kamil184.focustasks.model.Timer
import com.kamil184.focustasks.model.TimerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TimerViewModel(application: Application) : AndroidViewModel(application) {

    private val timerManager: TimerManager = TimerManager(application)
    val timer = MutableLiveData<Timer>()

    fun fetchTimer() = viewModelScope.launch(Dispatchers.IO) {
        timerManager.timerFlow.collect {
            timer.postValue(it)
        }
    }

    fun saveTimer() = viewModelScope.launch(Dispatchers.IO) {
        timer.value?.let {
            timerManager.saveTimer(it)
        }
    }

    fun onTimerFinished() {
        if (timer.value?.state?.value != TimerState.Stopped) timer.value?.state?.postValue(
            TimerState.Stopped)

        timer.value?.timeRemaining?.postValue(timer.value?.length?.value)
    }

    fun updateTimeRemaining(millisUntilFinished: Long) =
        timer.value?.timeRemaining?.postValue(millisUntilFinished.toInt())

}