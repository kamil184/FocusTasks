package com.kamil184.focustasks.ui.tasks

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kamil184.focustasks.data.model.Task
import com.kamil184.focustasks.data.model.TaskListName
import com.kamil184.focustasks.databinding.TasksViewPagerItemBinding
import kotlinx.coroutines.flow.MutableSharedFlow


class TasksViewPagerAdapter(private val updatedTasksFlow: MutableSharedFlow<Task>) :
    RecyclerView.Adapter<TasksViewPagerAdapter.ViewHolder>() {
    private var taskListNames: List<TaskListName>? = null
    private var tasks: List<Task>? = null

    @SuppressLint("NotifyDataSetChanged")
    fun submitTaskListNames(taskListNames: List<TaskListName>) {
        this.taskListNames = taskListNames
        if (tasks != null) {
            notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitTasks(tasks: List<Task>) {
        this.tasks = tasks
        if (taskListNames != null) {
            notifyDataSetChanged()
        }
    }

    inner class ViewHolder(private val binding: TasksViewPagerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.tasksRecyclerView.layoutManager = LinearLayoutManager(binding.root.context)
            binding.tasksRecyclerView.adapter = TasksListAdapter(updatedTasksFlow)
            val itemTouchHelperCallback = TasksListAdapterItemTouchHelperCallback(binding.tasksRecyclerView.adapter as TasksListAdapter)
            val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
            itemTouchHelper.attachToRecyclerView(binding.tasksRecyclerView)
        }

        fun bind(tasks: List<Task>) {
            if (tasks.isEmpty()) {
                binding.tasksRecyclerView.visibility = View.GONE
                binding.tasksOnEmptyListImage.visibility = View.VISIBLE
                binding.tasksOnEmptyListTitle.visibility = View.VISIBLE
                binding.tasksOnEmptyListText.visibility = View.VISIBLE
            } else {
                binding.tasksRecyclerView.visibility = View.VISIBLE
                binding.tasksOnEmptyListImage.visibility = View.GONE
                binding.tasksOnEmptyListTitle.visibility = View.GONE
                binding.tasksOnEmptyListText.visibility = View.GONE

                (binding.tasksRecyclerView.adapter as TasksListAdapter).submitTasks(tasks)

            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = TasksViewPagerItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (tasks != null && taskListNames != null) {
            val list = taskListNames!![position]
            val tasks = tasks!!.filter { it.list == list.uuid.uuid}
            holder.bind(tasks)
        }
    }

    override fun getItemCount() = taskListNames?.size ?: 0
}