package com.kamil184.focustasks.model

import java.util.*

data class Task(
    var title: String = "",
    var note: String? = null,
    var list: String? = null,
    var calendar: Calendar? = null,
    var priority: Priority = Priority.NO,
    var repeat: Repeat? = null,
    var isCompleted: Boolean = false
    ) {
    enum class Priority {
        NO, LOW, MEDIUM, HIGH
    }
}