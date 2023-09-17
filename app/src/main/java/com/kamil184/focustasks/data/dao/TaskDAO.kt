package com.kamil184.focustasks.data.dao

import androidx.room.*
import com.kamil184.focustasks.data.model.Task
import com.kamil184.focustasks.data.model.Task.Companion.TABLE_NAME
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDAO {
    @Query("SELECT * FROM $TABLE_NAME WHERE id=:id")
    fun get(id: Int): Flow<Task>

    @Query("SELECT * FROM $TABLE_NAME WHERE list=:list ORDER BY position_in_list ASC")
    fun getAllFromList(list: String): Flow<List<Task>>

    @Query("SELECT * FROM $TABLE_NAME ORDER BY position_in_list ASC")
    fun getAll(): Flow<List<Task>>

    @Query("DELETE FROM $TABLE_NAME WHERE list=:list")
    fun deleteAllFromList(list: String)

    @Update
    suspend fun update(task: Task)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Delete
    suspend fun delete(task: Task)
}