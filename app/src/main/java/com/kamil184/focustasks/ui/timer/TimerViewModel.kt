package com.kamil184.focustasks.ui.timer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kamil184.focustasks.App
import com.kamil184.focustasks.data.manager.TimerManager
import com.kamil184.focustasks.data.model.Timer
import com.kamil184.focustasks.data.model.TimerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TimerViewModel(private val timerManager: TimerManager) : ViewModel() {

    val timer = MutableLiveData<Timer>()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val timerManager = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as App).timerManager
                TimerViewModel(
                    timerManager = timerManager
                )
            }
        }
    }

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