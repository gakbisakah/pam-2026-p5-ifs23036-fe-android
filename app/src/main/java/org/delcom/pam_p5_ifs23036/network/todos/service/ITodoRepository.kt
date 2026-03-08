package org.delcom.pam_p5_ifs23036.network.todos.service

import okhttp3.MultipartBody
import org.delcom.pam_p5_ifs23036.network.data.ResponseMessage
import org.delcom.pam_p5_ifs23036.network.todos.data.*

interface ITodoRepository {

    fun getUserIdFromToken(authToken: String): String

    // ----------------------------------
    // Auth
    // ----------------------------------

    suspend fun postRegister(
        request: RequestAuthRegister
    ): ResponseMessage<ResponseAuthRegister?>

    suspend fun postLogin(
        request: RequestAuthLogin
    ): ResponseMessage<ResponseAuthLogin?>

    suspend fun postLogout(
        request: RequestAuthLogout
    ): ResponseMessage<String?>

    suspend fun postRefreshToken(
        request: RequestAuthRefreshToken
    ): ResponseMessage<ResponseAuthLogin?>

    // ----------------------------------
    // Users
    // ----------------------------------

    suspend fun getUserMe(
        authToken: String
    ): ResponseMessage<ResponseUserData?>

    suspend fun putUserMe(
        authToken: String,
        request: RequestUserChange
    ): ResponseMessage<String?>

    suspend fun putUserMePassword(
        authToken: String,
        request: RequestUserChangePassword
    ): ResponseMessage<String?>

    suspend fun putUserMePhoto(
        authToken: String,
        file: MultipartBody.Part
    ): ResponseMessage<String?>

    // ----------------------------------
    // Todos
    // ----------------------------------

    suspend fun getTodos(
        authToken: String,
        page: Int = 1,
        perPage: Int = 10,
        status: String? = null,
        urgency: String? = null
    ): ResponseMessage<ResponseTodos?>

    suspend fun getTodoStats(
        authToken: String
    ): ResponseMessage<ResponseTodoStats?>

    suspend fun postTodo(
        authToken: String,
        request: RequestTodo
    ): ResponseMessage<ResponseTodoAdd?>

    suspend fun getTodoById(
        authToken: String,
        todoId: String
    ): ResponseMessage<ResponseTodo?>

    suspend fun putTodo(
        authToken: String,
        todoId: String,
        request: RequestTodo
    ): ResponseMessage<String?>

    suspend fun putTodoCover(
        authToken: String,
        todoId: String,
        file: MultipartBody.Part
    ): ResponseMessage<String?>

    suspend fun deleteTodo(
        authToken: String,
        todoId: String
    ): ResponseMessage<String?>
}
