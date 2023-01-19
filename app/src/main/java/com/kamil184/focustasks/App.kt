package com.kamil184.focustasks

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.kamil184.focustasks.data.manager.TaskListNamesManager
import com.kamil184.focustasks.data.manager.TimerManager
import com.kamil184.focustasks.data.repo.TaskRepository

class App : Application() {
    private val database by lazy { AppDatabase.getInstance(applicationContext) }
    val taskRepository by lazy { TaskRepository(database.taskDao()) }
    val taskListNamesManager by lazy { TaskListNamesManager(this) }
    val timerManager by lazy { TimerManager(this) }
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}