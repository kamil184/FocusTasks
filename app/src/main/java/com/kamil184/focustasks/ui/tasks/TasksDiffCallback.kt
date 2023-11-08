package com.kamil184.focustasks.ui.tasks

import androidx.recyclerview.widget.DiffUtil
import com.kamil184.focustasks.data.model.Task

class TasksDiffCallback(private var old: List<Any>, private var new: List<Any>) : DiffUtil.Callback() {
    override fun getOldListSize() = old.size

    override fun getNewListSize() = new.size

    fun updateData(new:List<Any>){
        old = this.new
        this.new = new
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = old[oldItemPosition]
        val newItem = new[newItemPosition]
        val res = when (oldItem) {
            is Int -> newItem is Int
            is Task -> if (newItem is Task) oldItem.id == newItem.id else false
            else -> throw IllegalArgumentException("Invalid Type. It must be Task or Int") // Int is type for header
        }
        return res
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = old[oldItemPosition]
        val newItem = new[newItemPosition]
        val res = when (oldItem) {
            is Int -> if (newItem is Int) oldItem == newItem else false
            is Task -> if (newItem is Task) oldItem == newItem else false
            else -> throw IllegalArgumentException("Invalid Type. It must be Task or Int") // Int is type for header
        }
        return res
    }


}