package com.kamil184.focustasks.ui.dialogs

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kamil184.focustasks.App
import com.kamil184.focustasks.data.model.Task
import com.kamil184.focustasks.data.model.TaskListName
import com.kamil184.focustasks.data.model.TaskListName.Companion.getListName
import com.kamil184.focustasks.data.repo.TaskRepository
import kotlinx.coroutines.launch
import java.util.UUID

class TaskCreateViewModel(val taskRepository: TaskRepository) : ViewModel() {
    val task = MutableLiveData<Task>()
    var taskListNames: List<TaskListName>? = null

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val taskRepository =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as App).taskRepository
                TaskCreateViewModel(
                    taskRepository = taskRepository
                )
            }
        }
    }

    fun saveTask() = viewModelScope.launch {
        task.value?.let {
            taskRepository.insert(it)
        }
    }

    fun getCurrentListName() = getListName(task.value?.list, taskListNames)
}