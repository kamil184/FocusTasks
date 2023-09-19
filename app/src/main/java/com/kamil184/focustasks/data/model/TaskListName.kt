package com.kamil184.focustasks.data.model

import android.os.ParcelUuid
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class TaskListName(val uuid: ParcelUuid, var listName:String):Parcelable{
    constructor(listName: String):this(UUID.randomUUID(), listName)
    constructor(uuid: UUID, listName: String):this(ParcelUuid(uuid), listName)
}