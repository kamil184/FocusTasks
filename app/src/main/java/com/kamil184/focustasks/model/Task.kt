package com.kamil184.focustasks.model

import java.util.*

data class Task(
    val title: String,
    val note: String,
    val date: Calendar,
    var priority: Priority,
    ) {
    enum class Priority {
        NO, LOW, MEDIUM, HIGH
    }
}