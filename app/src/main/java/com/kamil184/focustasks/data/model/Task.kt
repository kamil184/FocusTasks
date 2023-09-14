package com.kamil184.focustasks.data.model

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.os.Parcelable
import android.text.format.DateFormat
import androidx.core.content.ContextCompat
import androidx.room.*
import com.kamil184.focustasks.R
import com.kamil184.focustasks.data.model.Task.Companion.TABLE_NAME
import com.kamil184.focustasks.util.getColorFromAttr
import kotlinx.parcelize.Parcelize
import java.util.*

@Entity(tableName = TABLE_NAME)
@Parcelize
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var title: String = "",
    var description: String? = null,
    var list: String? = null,
    var calendar: Calendar? = null,
    @ColumnInfo(name = "is_there_a_time") var isThereATime: Boolean = false,
    var priority: Priority = Priority.NO,
    @Embedded var repeat: Repeat? = null,
    @ColumnInfo(name = "is_completed") var isCompleted: Boolean = false,
    @ColumnInfo(name = "position_in_list") var positionInList: Int = 0,
) : Parcelable {

    fun getChipText(resources: Resources, isSystem24Hour: Boolean): CharSequence? =
        if (repeat == null) {
            if (isThereATime) {
                if (isSystem24Hour) DateFormat.format("dd MMMM yyyy, HH:mm", calendar)
                else DateFormat.format("dd MMMM yyyy, h:mm a", calendar)
            } else DateFormat.format("dd MMMM yyyy", calendar)
        } else {
            repeat!!.getText(resources) + " ${
                if (isThereATime) {
                    resources.getString(R.string.at_time) + " " +
                            if (isSystem24Hour) DateFormat.format("HH:mm", calendar)
                            else DateFormat.format("h:mm a", calendar)
                } else ""
            }"
        }

    fun getChipText(context: Context): CharSequence? =
        getChipText(context.resources, DateFormat.is24HourFormat(context))

    enum class Priority {
        NO, LOW, MEDIUM, HIGH
    }

    fun getPriorityColorList(context: Context): ColorStateList = when (priority) {
        Priority.HIGH -> ColorStateList(arrayOf(intArrayOf(android.R.attr.state_enabled)),
            intArrayOf(ContextCompat.getColor(context, R.color.priority_high_3)))
        Priority.MEDIUM -> ColorStateList(arrayOf(intArrayOf(android.R.attr.state_enabled)),
            intArrayOf(ContextCompat.getColor(context, R.color.priority_high_2)))
        Priority.LOW -> ColorStateList(arrayOf(intArrayOf(android.R.attr.state_enabled)),
            intArrayOf(ContextCompat.getColor(context, R.color.priority_high_1)))
        Priority.NO -> ColorStateList(arrayOf(intArrayOf(android.R.attr.state_enabled)),
            intArrayOf(getColorFromAttr(context, R.attr.colorControlActivated)))
    }

    companion object {
        const val TABLE_NAME = "tasks"
    }
}