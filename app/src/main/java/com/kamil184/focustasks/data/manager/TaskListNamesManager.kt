package com.kamil184.focustasks.data.manager

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.google.protobuf.InvalidProtocolBufferException
import com.kamil184.focustasks.TaskListNamePreference
import com.kamil184.focustasks.TaskListNamesPreference
import com.kamil184.focustasks.TimerPreferences
import com.kamil184.focustasks.data.model.Task
import com.kamil184.focustasks.data.model.TaskListName
import kotlinx.coroutines.flow.map
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

private val Context.taskListNamesProto: DataStore<TaskListNamesPreference> by dataStore(
    fileName = "task_list_names.proto",
    serializer = TaskListNamesSerializer
)

class TaskListNamesManager(private val context: Context) {
    val taskListNamesFlow = context.taskListNamesProto.data.map {
        it.taskListNamePreferenceList.map { taskListNamePreference ->
            TaskListName(UUID.fromString(taskListNamePreference.uuid), taskListNamePreference.name)
        }.toMutableList()
    }

    suspend fun addTaskListName(name: String) {
        context.taskListNamesProto.updateData {
            val list = it.taskListNamePreferenceList
            val taskListNamePreference = TaskListNamePreference.newBuilder()
                .setUuid(UUID.randomUUID().toString())
                .setName(name).build()
            it.toBuilder()
                .setTaskListNamePreference(
                    list.size,
                    taskListNamePreference
                )
                .build()
        }
    }

    suspend fun addTaskListName(taskListName: TaskListName) {
        context.taskListNamesProto.updateData {
            val list = it.taskListNamePreferenceList
            val taskListNamePreference = TaskListNamePreference.newBuilder()
                .setUuid(taskListName.uuid.toString())
                .setName(taskListName.listName)
                .build()
            it.toBuilder()
                .setTaskListNamePreference(
                    list.size,
                    taskListNamePreference
                )
                .build()
        }
    }

    suspend fun updateTaskListName(taskListName: TaskListName) {
        context.taskListNamesProto.updateData {
            val list = it.taskListNamePreferenceList

            var index = -1
            for (i in list.indices)
                if (list[i].uuid == taskListName.uuid.toString())
                    index = i

            it.toBuilder()
                .setTaskListNamePreference(
                    index,
                    TaskListNamePreference.newBuilder()
                        .setUuid(taskListName.uuid.toString())
                        .setName(taskListName.listName)
                        .build()
                )
                .build()
        }
    }

    suspend fun updateTaskListName(previousName: String, name: String) {
        context.taskListNamesProto.updateData {
            val list = it.taskListNamePreferenceList

            var index = -1
            var uuid = ""
            for (i in list.indices)
                if (list[i].name == previousName) {
                    index = i
                    uuid = list[i].uuid
                }

            it.toBuilder()
                .setTaskListNamePreference(
                    index,
                    TaskListNamePreference.newBuilder()
                        .setUuid(uuid)
                        .setName(name)
                        .build()
                )
                .build()
        }
    }

    suspend fun saveTaskListNames(taskListNames: List<TaskListName>) {
        context.taskListNamesProto.updateData {
            val builder = TaskListNamesPreference.newBuilder()
            taskListNames.forEach { taskListName->
                val taskListNamePreference =
                    TaskListNamePreference.newBuilder()
                        .setUuid(taskListName.uuid.toString())
                        .setName(taskListName.listName)
                        .build()

                builder.addTaskListNamePreference(taskListNamePreference)
            }
            builder.build()
        }
    }
}


object TaskListNameSerializer : Serializer<TaskListNamePreference> {
    override val defaultValue: TaskListNamePreference =
        TaskListNamePreference.getDefaultInstance().toBuilder()
            .setUuid(UUID.randomUUID().toString())
            .setName("")
            .build()

    override suspend fun readFrom(input: InputStream): TaskListNamePreference {
        try {
            return TaskListNamePreference.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: TaskListNamePreference, output: OutputStream) =
        t.writeTo(output)
}

object TaskListNamesSerializer : Serializer<TaskListNamesPreference> {
    // Name will be changed at first launch to default name depending on language settings
    private val defaultTaskListName: TaskListNamePreference =
        TaskListNamePreference.getDefaultInstance().toBuilder()
            .setUuid(UUID.randomUUID().toString())
            .setName("")
            .build()

    override val defaultValue: TaskListNamesPreference =
        TaskListNamesPreference.getDefaultInstance().toBuilder()
            .addTaskListNamePreference(defaultTaskListName)
            .build()

    override suspend fun readFrom(input: InputStream): TaskListNamesPreference {
        try {
            return TaskListNamesPreference.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: TaskListNamesPreference, output: OutputStream) =
        t.writeTo(output)
}