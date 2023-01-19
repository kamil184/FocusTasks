package com.kamil184.focustasks.data.manager

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kamil184.focustasks.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.taskListNamesPreferences by preferencesDataStore(
    name = "taskListNames.preferences"
)

class TaskListNamesManager(private val context: Context) {
    val taskListNamesPreferencesFlow: Flow<MutableList<String>> =
        context.taskListNamesPreferences.data.map { preferences ->
            preferences[PreferencesKeys.TASK_LIST_NAMES]?.toMutableList() ?: mutableListOf(context.getString(R.string.base_list_tab))
        }


    suspend fun saveTaskListNames(strings: List<String>) {
        context.taskListNamesPreferences.edit { preferences ->
            preferences[PreferencesKeys.TASK_LIST_NAMES] = strings.toSet()
        }
    }

    private object PreferencesKeys {
        val TASK_LIST_NAMES = stringSetPreferencesKey("task_list_names")
    }
}