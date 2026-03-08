package org.delcom.pam_p5_ifs23036.network.todos.data

import kotlinx.serialization.Serializable

@Serializable
data class RequestTodo (
    val title: String,
    val description: String,
    val isDone: Boolean = false,
    val urgency: String = "Low",
    val userId: String? = null
)

@Serializable
data class RequestTodoAdd (
    val title: String,
    val description: String,
    val urgency: String = "Low"
)
