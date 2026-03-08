package org.delcom.pam_p5_ifs23036.network.todos.data

import kotlinx.serialization.Serializable

@Serializable
data class RequestUserChange (
    val name: String,
    val username: String,
    val about: String? = null
)

@Serializable
data class RequestUserChangePassword (
    val old: String,
    val new: String
)
