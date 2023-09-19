package com.kamil184.focustasks.ui.tasks

import android.annotation.SuppressLint
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.kamil184.focustasks.R
import com.kamil184.focustasks.data.model.Task
import com.kamil184.focustasks.databinding.ItemCompletedTasksHeaderBinding
import com.kamil184.focustasks.databinding.ItemTaskBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.*

class TasksListAdapter(private val updatedTasksFlow: MutableSharedFlow<Task>) :
    RecyclerView.Adapter<TasksListAdapter.TasksListViewHolder>() {
    private var uncompletedTasks: MutableList<Task> = mutableListOf()
    private var completedTasks: MutableList<Task> = mutableListOf()

    private val differ = AsyncListDiffer(this, DiffCallback)

    private var completedTasksVisible = false

    init {
        submitListDiffer()
    }

    private fun filterTasks(tasks: List<Task>) {
        completedTasks = mutableListOf()
        uncompletedTasks = mutableListOf()
        tasks.forEach {
            if (it.isCompleted) completedTasks.add(it)
            else uncompletedTasks.add(it)
        }
    }

    fun submitTasks(tasks: List<Task>) {
        filterTasks(tasks)
        submitListDiffer()
    }

    private fun submitListDiffer() {
        val tasksSize = completedTasks.size + uncompletedTasks.size
        val list = when {
            tasksSize == 0 -> listOf()
            completedTasks.isEmpty() -> uncompletedTasks
            uncompletedTasks.isEmpty() -> {
                if (completedTasksVisible)
                    getListFromListsAndObjects(completedTasks.size, completedTasks)
                else listOf(completedTasks.size)
            }
            else -> {
                if (completedTasksVisible)
                    getListFromListsAndObjects(uncompletedTasks,
                        completedTasks.size,
                        completedTasks)
                else getListFromListsAndObjects(uncompletedTasks, completedTasks.size)
            }
        }
        differ.submitList(list)
    }

    private fun getListFromListsAndObjects(vararg objects: Any): List<Any> {
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
                    submitListDiffer()
                }
            }
            else -> throw IllegalArgumentException("Invalid ViewType Provided")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (differ.currentList[position]) {
            is Task -> R.layout.item_task
            is Int -> R.layout.item_completed_tasks_header
            else -> throw IllegalArgumentException("Invalid Class Provided: ${differ.currentList[position].javaClass.simpleName}")
        }
    }

    override fun onBindViewHolder(holder: TasksListViewHolder, position: Int) {
        when (val element = differ.currentList[position]) {
            is Int -> (holder as TasksListViewHolder.HeaderViewHolder).bind(completedTasks.size) //differ.currentList.size - 1 - position
            is Task -> {
                element.positionInList = position
                (holder as TasksListViewHolder.TaskViewHolder).bind(element)
            }
            else -> throw IllegalArgumentException("Invalid Type. It must be Task or Int") // Int is type for header
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    sealed class TasksListViewHolder(binding: ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        class HeaderViewHolder(
            private val binding: ItemCompletedTasksHeaderBinding,
            private var areCompletedTasksVisible: Boolean,
            private val onExpandImageClicked: (Boolean) -> Unit,
        ) :
            TasksListViewHolder(binding) {

            init {
                binding.itemCompletedTasksHeaderExpandImage.setOnClickListener {
                    areCompletedTasksVisible = areCompletedTasksVisible.not()
                    onExpandImageClicked.invoke(areCompletedTasksVisible)
                    val rotationAngle = if (areCompletedTasksVisible) 180F else 0F
                    it.animate().apply {
                        rotation(rotationAngle)
                        duration = 200
                        start()
                    }
                }
            }

            fun bind(size: Int) {
                binding.itemCompletedTasksHeaderText.text =
                    binding.root.context.getString(R.string.completed, size)
            }
        }

        class TaskViewHolder(
            private val binding: ItemTaskBinding,
            private val updatedTasksFlow: MutableSharedFlow<Task>,
        ) :
            TasksListViewHolder(binding) {
            private var task: Task? = null

            init {
                binding.apply {
                    itemTaskCheckbox.setOnClickListener {
                        var isEmitted = false
                        task?.apply {
                            isCompleted = itemTaskCheckbox.isChecked
                            isEmitted = updatedTasksFlow.tryEmit(this)
                        }
                        if (isEmitted) {
                            if (itemTaskCheckbox.isChecked) {
                                itemTaskTitle.paintFlags =
                                    itemTaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                            } else {
                                itemTaskTitle.paintFlags =
                                    itemTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                            }
                        }
                    }
                }
            }

            fun bind(task: Task) {
                this.task = task
                binding.apply {
                    itemTaskTitle.text = task.title

                    if (task.description.isNullOrEmpty()) itemTaskDescription.visibility = View.GONE
                    else {
                        itemTaskDescription.text = task.description
                        itemTaskDescription.visibility = View.VISIBLE
                    }

                    if (task.calendar != null) {
                        itemTaskChip.text = task.getChipText(binding.root.context)
                        itemTaskChip.visibility = View.VISIBLE
                    } else itemTaskChip.visibility = View.GONE

                    itemTaskChip.isChipIconVisible = task.repeat != null
                    itemTaskCheckbox.isChecked = task.isCompleted
                    itemTaskCheckbox.buttonTintList =
                        task.getPriorityColorList(binding.root.context)

                    if (task.isCompleted) {
                        itemTaskTitle.paintFlags =
                            itemTaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    } else {
                        itemTaskTitle.paintFlags =
                            itemTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

                    }
                }
            }
        }
    }

    fun onItemMove(from: Int, to: Int): Boolean {
        val list:List<Any> = differ.currentList
        val taskFrom:Task = list.find { it is Task && it.positionInList == from } as Task
        val taskTo:Task = list.find { it is Task && it.positionInList == to } as Task
        taskFrom.positionInList = to
        updatedTasksFlow.tryEmit(taskFrom)
        taskTo.positionInList = from
        updatedTasksFlow.tryEmit(taskTo)
        return true
    }
    private object DiffCallback : ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            val res = when (oldItem) {
                is Int -> newItem is Int
                is Task -> if (newItem is Task) oldItem.id == newItem.id else false
                else -> throw IllegalArgumentException("Invalid Type. It must be Task or Int") // Int is type for header
            }
            return res
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            val res = when (oldItem) {
                is Int -> if (newItem is Int) oldItem == newItem else false
                is Task -> if (newItem is Task) oldItem == newItem else false
                else -> throw IllegalArgumentException("Invalid Type. It must be Task or Int") // Int is type for header
            }
            return res
        }
    }
}