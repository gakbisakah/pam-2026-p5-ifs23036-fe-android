package org.delcom.pam_p5_ifs23036.network.todos.service

import okhttp3.MultipartBody
import org.delcom.pam_p5_ifs23036.helper.SuspendHelper
import org.delcom.pam_p5_ifs23036.network.data.ResponseMessage
import org.delcom.pam_p5_ifs23036.network.todos.data.*
import org.json.JSONObject
import android.util.Base64

class TodoRepository(
    private val apiService: TodoApiService
) : ITodoRepository {

    override fun getUserIdFromToken(authToken: String): String {
        return try {
            if (authToken.startsWith("mock-token-")) {
                return authToken.substringAfter("mock-token-")
            }

            val parts = authToken.split(".")
            if (parts.size == 3) {
                val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
                val json = JSONObject(payload)
                when {
                    json.has("id") -> json.getString("id")
                    json.has("userId") -> json.getString("userId")
                    json.has("sub") -> json.getString("sub")
                    json.has("uid") -> json.getString("uid")
                    else -> ""
                }
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    override suspend fun postRegister(request: RequestAuthRegister) = SuspendHelper.safeApiCall { apiService.postRegister(request) }
    override suspend fun postLogin(request: RequestAuthLogin) = SuspendHelper.safeApiCall { apiService.postLogin(request) }
    override suspend fun postLogout(request: RequestAuthLogout) = SuspendHelper.safeApiCall { apiService.postLogout(request) }
    override suspend fun postRefreshToken(request: RequestAuthRefreshToken) = SuspendHelper.safeApiCall { apiService.postRefreshToken(request) }

    override suspend fun getUserMe(authToken: String): ResponseMessage<ResponseUserData?> {
        val userId = getUserIdFromToken(authToken)
        return SuspendHelper.safeApiCall { apiService.getUserMe("Bearer $authToken", userId) }
    }

    override suspend fun putUserMe(authToken: String, request: RequestUserChange): ResponseMessage<String?> {
        val userId = getUserIdFromToken(authToken)
        return SuspendHelper.safeApiCall { apiService.putUserMe("Bearer $authToken", userId, request) }
    }

    override suspend fun putUserMePassword(authToken: String, request: RequestUserChangePassword): ResponseMessage<String?> {
        val userId = getUserIdFromToken(authToken)
        return SuspendHelper.safeApiCall { apiService.putUserMePassword("Bearer $authToken", userId, request) }
    }

    override suspend fun putUserMePhoto(authToken: String, file: MultipartBody.Part): ResponseMessage<String?> {
        val userId = getUserIdFromToken(authToken)
        return SuspendHelper.safeApiCall { apiService.putUserMePhoto("Bearer $authToken", userId, file) }
    }

    override suspend fun getTodos(authToken: String, page: Int, perPage: Int, status: String?, urgency: String?): ResponseMessage<ResponseTodos?> {
        val userId = getUserIdFromToken(authToken)
        return SuspendHelper.safeApiCall { apiService.getTodos("Bearer $authToken", userId, page, perPage, status, urgency) }
    }

    override suspend fun getTodoStats(authToken: String): ResponseMessage<ResponseTodoStats?> {
        val userId = getUserIdFromToken(authToken)
        return SuspendHelper.safeApiCall { apiService.getTodoStats("Bearer $authToken", userId) }
    }

    override suspend fun postTodo(authToken: String, request: RequestTodo): ResponseMessage<ResponseTodoAdd?> {
        val userId = getUserIdFromToken(authToken)
        val requestAdd = RequestTodoAdd(
            title = request.title,
            description = request.description,
            urgency = request.urgency
        )
        return SuspendHelper.safeApiCall { 
            apiService.postTodo("Bearer $authToken", userId, requestAdd)
        }
    }

    override suspend fun getTodoById(authToken: String, todoId: String): ResponseMessage<ResponseTodo?> {
        val userId = getUserIdFromToken(authToken)
        return SuspendHelper.safeApiCall { apiService.getTodoById("Bearer $authToken", todoId, userId) }
    }
    
    override suspend fun putTodo(authToken: String, todoId: String, request: RequestTodo): ResponseMessage<String?> {
        val userId = getUserIdFromToken(authToken)
        return SuspendHelper.safeApiCall {
            apiService.putTodo("Bearer $authToken", todoId, userId, request)
        }
    }

    override suspend fun putTodoCover(authToken: String, todoId: String, file: MultipartBody.Part): ResponseMessage<String?> {
        val userId = getUserIdFromToken(authToken)
        return SuspendHelper.safeApiCall { apiService.putTodoCover("Bearer $authToken", todoId, userId, file) }
    }

    override suspend fun deleteTodo(authToken: String, todoId: String): ResponseMessage<String?> {
        val userId = getUserIdFromToken(authToken)
        return SuspendHelper.safeApiCall { apiService.deleteTodo("Bearer $authToken", todoId, userId) }
    }
}
