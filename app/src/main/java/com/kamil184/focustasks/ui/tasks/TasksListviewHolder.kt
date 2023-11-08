package com.kamil184.focustasks.ui.tasks

import android.graphics.Paint
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.kamil184.focustasks.R
import com.kamil184.focustasks.data.model.Task
import com.kamil184.focustasks.databinding.ItemCompletedTasksHeaderBinding
import com.kamil184.focustasks.databinding.ItemTaskBinding
import kotlinx.coroutines.flow.MutableSharedFlow

sealed class TasksListViewHolder(binding: ViewBinding) :
    RecyclerView.ViewHolder(binding.root) {

    class HeaderViewHolder(
        private val binding: ItemCompletedTasksHeaderBinding,
        private var areCompletedTasksVisible: Boolean,
        private val onExpandImageClicked: (Boolean) -> Unit,
    ) :
        TasksListViewHolder(binding) {

        init {
            binding.itemCompletedTasksHeaderSelectableLayout.setOnClickListener {
                areCompletedTasksVisible = areCompletedTasksVisible.not()
                onExpandImageClicked.invoke(areCompletedTasksVisible)
                val rotationAngle = if (areCompletedTasksVisible) 180F else 0F
                binding.itemCompletedTasksHeaderExpandImage.animate().apply {
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
                    val isEmitted =
                        task?.copy(isCompleted = itemTaskCheckbox.isChecked)
                            ?.let { it1 -> updatedTasksFlow.tryEmit(it1) }

                    if (isEmitted == true) {
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