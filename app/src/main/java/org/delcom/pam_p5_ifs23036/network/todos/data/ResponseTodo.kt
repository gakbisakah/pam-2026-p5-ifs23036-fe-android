package org.delcom.pam_p5_ifs23036.network.todos.data

import kotlinx.serialization.Serializable

@Serializable
data class ResponseTodos (
    val todos: List<ResponseTodoData>
)

@Serializable
data class ResponseTodo (
    val todo: ResponseTodoData
)

@Serializable
data class ResponseTodoData(
    val id: String = "",
    val userId: String = "",
    val title: String? = null,
    val description: String? = null,
    val isDone: Boolean = false,
    val urgency: String? = "Low",
    val coverPath: String? = null,
    val createdAt: String? = "",
    var updatedAt: String? = ""
)

@Serializable
data class ResponseTodoAdd (
    val todoId: String
)

@Serializable
data class ResponseTodoStats(
    val total: Long? = 0,
    val completed: Long? = 0,
    val pending: Long? = 0
)
