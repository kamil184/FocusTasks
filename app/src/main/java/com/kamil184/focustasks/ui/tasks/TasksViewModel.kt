package com.kamil184.focustasks.ui.tasks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kamil184.focustasks.manager.TaskListNamesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TasksViewModel(application: Application) : AndroidViewModel(application) {

    private val taskListManager: TaskListNamesManager = TaskListNamesManager(application)
    val taskListNames = MutableLiveData<MutableList<String>>()

    fun fetchTaskListNames() = viewModelScope.launch(Dispatchers.IO) {
        taskListManager.taskListNamesPreferencesFlow.collect {
            taskListNames.postValue(it)
        }
    }

    fun saveTaskListNames() = viewModelScope.launch(Dispatchers.IO) {
        taskListNames.value?.let {
            taskListManager.saveTaskListNames(it)
        }
    }

    /**
     * @return true if update was successful and did make sense (there is no equal element to parameter s)
     */
    fun addIntoTaskListNames(s: String): Boolean {
        if (taskListNames.value?.contains(s) == true) return false
        taskListNames.value?.add(s)
        return true
    }

    /**
     * @return true if remove was successful
     */
    fun removeFromTaskListNames(s: String): Boolean = taskListNames.value?.remove(s) == true


    /**
     * @return true if rename was successful
     */
    fun renameFromTaskListNames(id:Int, s: String): Boolean {
        if(taskListNames.value?.indices?.contains(id) == false || taskListNames.value?.contains(s) == true)
            return false
        taskListNames.value?.set(id, s)
        return true
    }

    override fun onCleared() {
        super.onCleared()
        saveTaskListNames()
    }
}