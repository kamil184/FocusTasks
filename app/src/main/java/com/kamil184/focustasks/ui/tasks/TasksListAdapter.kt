package com.kamil184.focustasks.ui.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.kamil184.focustasks.R
import com.kamil184.focustasks.data.model.Task
import com.kamil184.focustasks.databinding.ItemCompletedTasksHeaderBinding
import com.kamil184.focustasks.databinding.ItemTaskBinding
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.Collections

class TasksListAdapter(private val updatedTasksFlow: MutableSharedFlow<Task>, private val insertTasks: (List<Task>) -> Unit) :
    RecyclerView.Adapter<TasksListViewHolder>() {
    private var uncompletedTasks: MutableList<Task> = mutableListOf()
    private var completedTasks: MutableList<Task> = mutableListOf()

    private var list: MutableList<Any> = mutableListOf()

    private val diffCallback = TasksDiffCallback(list, listOf())

    private var completedTasksVisible = false

    init {
        submitList()
    }

    private fun filterTasksByCompleteness(tasks: List<Task>) {
        completedTasks = mutableListOf()
        uncompletedTasks = mutableListOf()
        tasks.forEach {
            if (it.isCompleted) completedTasks.add(it)
            else uncompletedTasks.add(it)
        }
    }

    fun submitTasks(tasks: List<Task>) {
        filterTasksByCompleteness(tasks)
        submitList()
        diffCallback.updateData(list)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        diffResult.dispatchUpdatesTo(this)
    }

    private fun onCompletedTasksVisibleChange() {
        submitList()
        diffCallback.updateData(list)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        diffResult.dispatchUpdatesTo(this)
    }

    private fun submitList() {
        val tasksSize = completedTasks.size + uncompletedTasks.size
        list =
            when {
                tasksSize == 0 -> mutableListOf()
                completedTasks.isEmpty() -> uncompletedTasks.toMutableList()
                uncompletedTasks.isEmpty() -> {
                    if (completedTasksVisible)
                        getListFromListsAndObjects(completedTasks.size, completedTasks)
                    else mutableListOf(completedTasks.size)
                }
                else -> {
                    if (completedTasksVisible)
                        getListFromListsAndObjects(uncompletedTasks,
                            completedTasks.size,
                            completedTasks)
                    else getListFromListsAndObjects(uncompletedTasks, completedTasks.size)
                }
            }

    }

    private fun getListFromListsAndObjects(vararg objects: Any): MutableList<Any> {
        val mutableList = mutableListOf<Any>()
        objects.forEach { obj ->
            if (obj is Collection<*>)
                obj.forEach {
                    if (it != null) {
                        mutableList.add(it)
                    }
                }
            else mutableList.add(obj)
        }
        return mutableList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasksListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.item_task -> {
                val binding = ItemTaskBinding.inflate(inflater, parent, false)
                TasksListViewHolder.TaskViewHolder(binding, updatedTasksFlow)
            }
            R.layout.item_completed_tasks_header -> {
                val binding = ItemCompletedTasksHeaderBinding.inflate(inflater, parent, false)
                TasksListViewHolder.HeaderViewHolder(binding,
                    completedTasksVisible) { completedTasksVisible ->
                    this.completedTasksVisible = completedTasksVisible
                    onCompletedTasksVisibleChange()
                }
            }
            else -> throw IllegalArgumentException("Invalid ViewType Provided")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (list[position]) {
            is Task -> R.layout.item_task
            is Int -> R.layout.item_completed_tasks_header
            else -> throw IllegalArgumentException("Invalid Class Provided: ${list[position].javaClass.simpleName}")
        }
    }

    override fun onBindViewHolder(holder: TasksListViewHolder, position: Int) {
        when (val element = list[position]) {
            is Int -> (holder as TasksListViewHolder.HeaderViewHolder).bind(completedTasks.size) //differ.currentList.size - 1 - position
            is Task -> {
                element.positionInList = position
                (holder as TasksListViewHolder.TaskViewHolder).bind(element)
            }
            else -> throw IllegalArgumentException("Invalid Type. It must be Task or Int") // Int is type for header
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun onItemMove(from: Int, to: Int): Boolean {
        val temporaryList = list.toMutableList()
        val headerId = temporaryList.indexOf(temporaryList.find { it is Int })
        if (headerId in (from + 1)..to) return false
        if (headerId in to until from) return false

        val taskFrom = (temporaryList[from] as Task)
        taskFrom.positionInList = to
        val taskTo = (temporaryList[to] as Task)
        taskTo.positionInList = from

        Collections.swap(temporaryList, from, to)
        notifyItemMoved(from, to)

        list.clear()
        list.addAll(temporaryList)
        return true
    }

    fun onClearView(position: Int) {
        val headerId = list.indexOf(list.find { it is Int })
        if (position < headerId)
            insertTasks(list.subList(0, headerId).map { it as Task })
        else for (i in headerId + 1 until list.size) {
            insertTasks(list.subList(headerId + 1, list.size).map { it as Task })
        }
    }
}