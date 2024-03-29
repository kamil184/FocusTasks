package com.kamil184.focustasks.ui.tasks

import android.os.ParcelUuid
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kamil184.focustasks.App
import com.kamil184.focustasks.data.manager.TaskListNamesManager
import com.kamil184.focustasks.data.model.Task
import com.kamil184.focustasks.data.model.TaskListName
import com.kamil184.focustasks.data.repo.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class TasksViewModel(
    private val taskListNamesManager: TaskListNamesManager,
    private val taskRepository: TaskRepository,
) : ViewModel() {

    val taskListNames = MutableStateFlow(listOf<TaskListName>())

    private var _tasks: MutableStateFlow<List<Task>> = MutableStateFlow(listOf())
    val tasks:StateFlow<List<Task>> get() = _tasks


    init {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.allTasks.collect{
                _tasks.value = it
            }
        }
    }

    val updatedTasksFlow = MutableSharedFlow<Task>(extraBufferCapacity = 64)

    fun findCurrentTaskListNameUUID(listName: String): ParcelUuid {
        taskListNames.value.forEach {
            if (it.listName == listName) return it.uuid
        }
        throw IllegalArgumentException("taskListNames doesn't contain passed variable listName")
    }

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

    fun fetchTaskListNames(defaultName: String) = viewModelScope.launch(Dispatchers.IO) {
        taskListNamesManager.taskListNamesFlow.collect {
            if (it.size == 1 && it[0].listName.isEmpty()) it[0].listName = defaultName
            taskListNames.value = it
        }
    }

    fun saveTaskListNames() = viewModelScope.launch(Dispatchers.IO) {
        taskListNamesManager.saveTaskListNames(taskListNames.value)
    }

    /**
     * @return true if update was successful and did make sense (there is no equal element to parameter s)
     */
    fun addTaskListName(s: String): Boolean {
        taskListNames.value.forEach {
            if (it.listName == s) return false
        }
        val mutableList = mutableListOf<TaskListName>()
        mutableList.addAll(taskListNames.value)
        mutableList.add(TaskListName(s))
        taskListNames.value = mutableList
        return true
    }

    /**
     * @return true if remove was successful
     */
    fun removeTaskListName(s: String): Boolean {
        val mutableList = mutableListOf<TaskListName>()
        mutableList.addAll(taskListNames.value)

        // @RequiresApi(Build.VERSION_CODES.N)
        // mutableList.removeIf { it.listName == s}

        for (i in mutableList.indices)
            if (mutableList[i].listName == s) {
                val listName = TaskListName.getUUID(s, mutableList)
                viewModelScope.launch(Dispatchers.IO) {
                    taskRepository.deleteAllFromList(listName)
                }
                mutableList.removeAt(i)
                taskListNames.value = mutableList
                //_taskListNames.compareAndSet(_taskListNames.value, mutableList)
                return true
            }
        return false
    }


    /**
     * @return true if rename was successful
     */
    fun setTaskListName(id: Int, s: String): Boolean {
        taskListNames.value.forEach {
            if (it.listName == s) return false
        }
        if (!taskListNames.value.indices.contains(id))
            return false
        val mutableList = mutableListOf<TaskListName>()
        mutableList.addAll(taskListNames.value)
        mutableList[id] = mutableList[id].copy(listName = s)
        taskListNames.compareAndSet(taskListNames.value, mutableList)
        return true
    }

    fun deleteCompletedTasksFromList(list: UUID): Boolean {
        if (containsCompletedTasksInList(list)) {
            viewModelScope.launch(Dispatchers.IO) {
                taskRepository.deleteCompletedTasksFromList(list)
            }
            return true
        }
        return false
    }

    fun containsCompletedTasksInList(list: UUID) =
        !tasks.value.none { it.list?.equals(list) == true && it.isCompleted }

    fun containsTasksInList(list: UUID) =
        !tasks.value.none { it.list?.equals(list) == true }

    fun containsTasksInList(list: String) = containsTasksInList(TaskListName.getUUID(list, taskListNames.value))


    suspend fun updateTask(task: Task) {
        taskRepository.update(task)
    }

    fun insertAllTasks(tasks:List<Task>){
        viewModelScope.launch {
            taskRepository.insertAll(tasks)
        }
    }

    override fun onCleared() {
        super.onCleared()
        saveTaskListNames()
    }
}