package com.kamil184.focustasks.ui.tasks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kamil184.focustasks.model.Task

class TaskCreateViewModel : ViewModel() {

    private val _task = MutableLiveData<Task>().apply {
        value = Task()
    }
    val task: LiveData<Task> = _task


}