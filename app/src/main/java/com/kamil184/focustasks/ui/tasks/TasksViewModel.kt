package com.kamil184.focustasks.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kamil184.focustasks.App
import com.kamil184.focustasks.data.manager.TaskListNamesManager
import com.kamil184.focustasks.data.repo.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class TasksViewModel(
    private val taskListNamesManager: TaskListNamesManager,
    private val taskRepository: TaskRepository,
) : ViewModel() {

    private val _taskListNames = MutableStateFlow(listOf<String>())
    val taskListNames get() = _taskListNames

    val tasks = taskRepository.allTasks

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val taskListManager = (this[APPLICATION_KEY] as App).taskListNamesManager
                val taskRepository = (this[APPLICATION_KEY] as App).taskRepository
                TasksViewModel(
                    taskListNamesManager = taskListManager,
                    taskRepository = taskRepository
                )
            }
        }
    }

    fun fetchTaskListNames() = viewModelScope.launch(Dispatchers.IO) {
        taskListNamesManager.taskListNamesPreferencesFlow.collect {
            _taskListNames.value = it
        }
    }

    fun saveTaskListNames() = viewModelScope.launch(Dispatchers.IO) {
        taskListNamesManager.saveTaskListNames(taskListNames.value)
    }

    /**
     * @return true if update was successful and did make sense (there is no equal element to parameter s)
     */
    fun addTaskListName(s: String): Boolean {
        if (taskListNames.value.contains(s)) return false
        val mutableList = mutableListOf<String>()
        mutableList.addAll(taskListNames.value)
        mutableList.add(s)
        _taskListNames.value = mutableList
        return true
    }

    /**
     * @return true if remove was successful
     */
    fun removeTaskListName(s: String): Boolean {
        val mutableList = mutableListOf<String>()
        mutableList.addAll(taskListNames.value)
        if (!mutableList.remove(s)) return false
        _taskListNames.value = mutableList
        return true
    }


    /**
     * @return true if rename was successful
     */
    fun setTaskListName(id: Int, s: String): Boolean {
        if (!taskListNames.value.indices.contains(id) || taskListNames.value.contains(s))
            return false
        val mutableList = mutableListOf<String>()
        mutableList.addAll(taskListNames.value)
        mutableList[id] = s
        _taskListNames.value = mutableList
        return true
    }

    override fun onCleared() {
        super.onCleared()
        saveTaskListNames()
    }
}