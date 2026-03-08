package org.delcom.pam_p5_ifs23036.network.todos.service

import okhttp3.MultipartBody
import org.delcom.pam_p5_ifs23036.network.data.ResponseMessage
import org.delcom.pam_p5_ifs23036.network.todos.data.*
import retrofit2.http.*

interface TodoApiService {
    // ----------------------------------
    // Auth
    // ----------------------------------

    @POST("auth/register")
    suspend fun postRegister(
        @Body request: RequestAuthRegister
    ): ResponseMessage<ResponseAuthRegister?>

    @POST("auth/login")
    suspend fun postLogin(
        @Body request: RequestAuthLogin
    ): ResponseMessage<ResponseAuthLogin?>

    @POST("auth/logout")
    suspend fun postLogout(
        @Body request: RequestAuthLogout
    ): ResponseMessage<String?>

    @POST("auth/refresh-token")
    suspend fun postRefreshToken(
        @Body request: RequestAuthRefreshToken
    ): ResponseMessage<ResponseAuthLogin?>

    // ----------------------------------
    // Users
    // ----------------------------------

    @GET("profile/{id}")
    suspend fun getUserMe(
        @Header("Authorization") authToken: String,
        @Path("id") id: String
    ): ResponseMessage<ResponseUserData?>

    @PUT("profile/{id}")
    suspend fun putUserMe(
        @Header("Authorization") authToken: String,
        @Path("id") id: String,
        @Body request: RequestUserChange,
    ): ResponseMessage<String?>

    @PATCH("profile/{id}/password")
    suspend fun putUserMePassword(
        @Header("Authorization") authToken: String,
        @Path("id") id: String,
        @Body request: RequestUserChangePassword,
    ): ResponseMessage<String?>

    @Multipart
    @PUT("profile/{id}/photo")
    suspend fun putUserMePhoto(
        @Header("Authorization") authToken: String,
        @Path("id") id: String,
        @Part file: MultipartBody.Part
    ): ResponseMessage<String?>

    @GET("todos")
    suspend fun getTodos(
        @Header("Authorization") authToken: String,
        @Query("userId") userId: String,
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 10,
        @Query("status") status: String? = null,
        @Query("urgency") urgency: String? = null
    ): ResponseMessage<ResponseTodos?>

    @GET("todos/stats/{userId}")
    suspend fun getTodoStats(
        @Header("Authorization") authToken: String,
        @Path("userId") userId: String
    ): ResponseMessage<ResponseTodoStats?>

    @POST("todos")
    suspend fun postTodo(
        @Header("Authorization") authToken: String,
        @Query("userId") userId: String,
        @Body request: RequestTodoAdd
    ): ResponseMessage<ResponseTodoAdd?>

    @GET("todos/{todoId}")
    suspend fun getTodoById(
        @Header("Authorization") authToken: String,
        @Path("todoId") todoId: String,
        @Query("userId") userId: String
    ): ResponseMessage<ResponseTodo?>

    @PUT("todos/{todoId}")
    suspend fun putTodo(
        @Header("Authorization") authToken: String,
        @Path("todoId") todoId: String,
        @Query("userId") userId: String,
        @Body request: RequestTodo
    ): ResponseMessage<String?>

    @Multipart
    @PUT("todos/{todoId}/cover")
    suspend fun putTodoCover(
        @Header("Authorization") authToken: String,
        @Path("todoId") todoId: String,
        @Query("userId") userId: String,
        @Part file: MultipartBody.Part
    ): ResponseMessage<String?>

    @DELETE("todos/{todoId}")
    suspend fun deleteTodo(
        @Header("Authorization") authToken: String,
        @Path("todoId") todoId: String,
        @Query("userId") userId: String
    ): ResponseMessage<String?>
}
