package com.kamil184.focustasks.data.repo

import androidx.annotation.WorkerThread
import com.kamil184.focustasks.data.dao.TaskDAO
import com.kamil184.focustasks.data.model.Task
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class TaskRepository(private val taskDAO: TaskDAO) {

    @WorkerThread
    suspend fun delete(task: Task) {
        taskDAO.delete(task)
    }

    @WorkerThread
    suspend fun deleteAllFromList(list: UUID) {
        taskDAO.deleteAllFromList(list)
    }

    @WorkerThread
    suspend fun deleteCompletedTasksFromList(list: UUID) {
        taskDAO.deleteCompletedTasksFromList(list)
    }

    @WorkerThread
    suspend fun isThereCompletedTasksInList(list: UUID) =
        taskDAO.isThereCompletedTasksInList(list)


    @WorkerThread
    fun getById(id: Int) = taskDAO.get(id)

    @WorkerThread
    fun getAllFromList(list: UUID) = taskDAO.getAllFromList(list)

    @WorkerThread
    fun getAll() = taskDAO.getAll()

    val allTasks: Flow<List<Task>> = taskDAO.getAll()


    @WorkerThread
    suspend fun update(task: Task) {
        taskDAO.update(task)
    }

    @WorkerThread
    suspend fun insert(task: Task) {
        taskDAO.insert(task)
    }
}