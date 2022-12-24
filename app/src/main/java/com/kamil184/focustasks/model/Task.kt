package com.kamil184.focustasks.model

import android.os.Parcelable
import com.kamil184.focustasks.model.CalendarMonthsHelper.Companion.today
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Task(
    var title: String = "",
    var description: String? = null,
    var list: String? = null,
    var calendar: Calendar? = null,
    var isThereATime: Boolean = false,
    var priority: Priority = Priority.NO,
    var repeat: Repeat? = null,
    var isCompleted: Boolean = false,
) : Parcelable {
    enum class Priority {
        NO, LOW, MEDIUM, HIGH
    }
}