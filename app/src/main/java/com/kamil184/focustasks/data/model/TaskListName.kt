package com.kamil184.focustasks.data.model

import android.os.ParcelUuid
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Arrays
import java.util.UUID

@Parcelize
data class TaskListName(val uuid: ParcelUuid, var listName: String) : Parcelable {
    constructor(listName: String) : this(UUID.randomUUID(), listName)
    constructor(uuid: UUID, listName: String) : this(ParcelUuid(uuid), listName)
    companion object{
        fun getListName(uuid: UUID?, taskListNames:List<TaskListName>?):String{
            taskListNames?.forEach {
                if(it.uuid.uuid.equals(uuid)) return it.listName
            }
            throw IllegalArgumentException("taskListNames: ${Arrays.toString(taskListNames?.toTypedArray())} doesn't contain uuid: $uuid")
        }

        fun getUUID(listName:String, taskListNames:List<TaskListName>?):UUID{
            taskListNames?.forEach {
                if(it.listName == listName) return it.uuid.uuid
            }
            throw IllegalArgumentException("taskListNames doesn't contain listName: $listName")
        }
    }
}