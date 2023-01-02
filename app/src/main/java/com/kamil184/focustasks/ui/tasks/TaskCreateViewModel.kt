package com.kamil184.focustasks.ui.tasks

import android.content.Context
import android.text.format.DateFormat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kamil184.focustasks.R
import com.kamil184.focustasks.model.Task

class TaskCreateViewModel : ViewModel() {
    fun getCalendarChipText(context: Context, isSystem24Hour: Boolean): CharSequence? =
        if (task.value?.repeat == null) {
            if (task.value?.isThereATime == true) {
                if (isSystem24Hour) DateFormat.format("dd MMMM yyyy, HH:mm", task.value?.calendar)
                else DateFormat.format("dd MMMM yyyy, h:mm a", task.value?.calendar)
            } else DateFormat.format("dd MMMM yyyy", task.value?.calendar)
        } else {
            task.value!!.repeat!!.getText(context) + " ${
                if (task.value?.isThereATime == true) {
                    context.getString(R.string.at_time) +
                            if (isSystem24Hour) DateFormat.format("HH:mm", task.value?.calendar)
                            else DateFormat.format("h:mm a", task.value?.calendar)
                } else ""
            }"
        }

    val task = MutableLiveData<Task>()
    var taskListNames : List<String>? = null
}