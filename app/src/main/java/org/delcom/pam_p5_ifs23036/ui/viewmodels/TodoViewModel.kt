package org.delcom.pam_p5_ifs23036.ui.viewmodels

import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import org.delcom.pam_p5_ifs23036.network.todos.data.*
import org.delcom.pam_p5_ifs23036.network.todos.service.ITodoRepository
import javax.inject.Inject

sealed interface ProfileUIState {
    data class Success(val data: ResponseUserData) : ProfileUIState
    data class Error(val message: String) : ProfileUIState
    object Loading : ProfileUIState
}

sealed interface TodosUIState {
    data class Success(val data: List<ResponseTodoData>) : TodosUIState
    data class Error(val message: String) : TodosUIState
    object Loading : TodosUIState
}

sealed interface TodoStatsUIState {
    data class Success(val data: ResponseTodoStats) : TodoStatsUIState
    data class Error(val message: String) : TodoStatsUIState
    object Loading : TodoStatsUIState
}

sealed interface TodoUIState {
    data class Success(val data: ResponseTodoData) : TodoUIState
    data class Error(val message: String) : TodoUIState
    object Loading : TodoUIState
}

sealed interface TodoActionUIState {
    data class Success(val message: String) : TodoActionUIState
    data class Error(val message: String) : TodoActionUIState
    object Loading : TodoActionUIState
    object Idle : TodoActionUIState
}

data class UIStateTodo(
    val profile: ProfileUIState = ProfileUIState.Loading,
    val todos: TodosUIState = TodosUIState.Loading,
    val stats: TodoStatsUIState = TodoStatsUIState.Loading,
    val todo: TodoUIState = TodoUIState.Loading,
    val todoAdd: TodoActionUIState = TodoActionUIState.Idle,
    val todoChange: TodoActionUIState = TodoActionUIState.Idle,
    val todoDelete: TodoActionUIState = TodoActionUIState.Idle,
    val todoChangeCover: TodoActionUIState = TodoActionUIState.Idle,
    val profileUpdate: TodoActionUIState = TodoActionUIState.Idle
)

@HiltViewModel
@Keep
class TodoViewModel @Inject constructor(
    private val repository: ITodoRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(UIStateTodo())
    val uiState = _uiState.asStateFlow()

    fun getProfile(authToken: String) {
        if (authToken.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(profile = ProfileUIState.Loading) }
            val result = runCatching { repository.getUserMe(authToken) }
            _uiState.update { it.copy(profile = result.fold(
                onSuccess = { res -> 
                    val user = res.data
                    if (res.status == "success" && user != null) {
                        ProfileUIState.Success(user)
                    } else {
                        ProfileUIState.Error(res.message ?: "Gagal memuat profil")
                    }
                },
                onFailure = { e -> ProfileUIState.Error(e.message ?: "Gagal memuat profil") }
            ))}
        }
    }

    fun getTodoStats(authToken: String) {
        if (authToken.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(stats = TodoStatsUIState.Loading) }
            val result = runCatching { repository.getTodoStats(authToken) }
            _uiState.update { it.copy(stats = result.fold(
                onSuccess = { res -> 
                    if (res.status == "success" && res.data != null) TodoStatsUIState.Success(res.data) 
                    else TodoStatsUIState.Error(res.message ?: "Gagal memuat statistik") 
                },
                onFailure = { e -> TodoStatsUIState.Error(e.message ?: "Gagal memuat statistik") }
            ))}
        }
    }

    fun getAllTodos(authToken: String, page: Int = 1, perPage: Int = 10, status: String? = null, urgency: String? = null) {
        if (authToken.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(todos = TodosUIState.Loading) }
            val result = runCatching { repository.getTodos(authToken, page, perPage, status, urgency) }
            _uiState.update { it.copy(todos = result.fold(
                onSuccess = { res -> 
                    val todos = res.data?.todos
                    if (res.status == "success" && todos != null) TodosUIState.Success(todos) 
                    else TodosUIState.Error(res.message ?: "Gagal memuat daftar tugas") 
                },
                onFailure = { e -> TodosUIState.Error(e.message ?: "Gagal memuat daftar tugas") }
            ))}
        }
    }

    fun postTodo(authToken: String, title: String, description: String, urgency: String = "Low") {
        viewModelScope.launch {
            _uiState.update { it.copy(todoAdd = TodoActionUIState.Loading) }
            val result = runCatching { repository.postTodo(authToken, RequestTodo(title, description, false, urgency)) }
            _uiState.update { it.copy(todoAdd = result.fold(
                onSuccess = { res -> 
                    if (res.status == "success") TodoActionUIState.Success(res.message ?: "Berhasil menambah tugas") 
                    else TodoActionUIState.Error(res.message ?: "Gagal menambah tugas") 
                },
                onFailure = { e -> TodoActionUIState.Error(e.message ?: "Gagal menambah tugas") }
            ))}
        }
    }

    fun getTodoById(authToken: String, todoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(todo = TodoUIState.Loading) }
            val result = runCatching { repository.getTodoById(authToken, todoId) }
            _uiState.update { it.copy(todo = result.fold(
                onSuccess = { res -> 
                    // Perbaikan parsing: Backend mengirim objek todo langsung di dalam data
                    // Sesuai log: "data": { "id": "...", "title": "...", ... }
                    val todo = res.data?.todo ?: (res.data as? ResponseTodoData)
                    if (res.status == "success" && todo != null) TodoUIState.Success(todo) 
                    else TodoUIState.Error(res.message ?: "Gagal memuat detail tugas") 
                },
                onFailure = { e -> TodoUIState.Error(e.message ?: "Gagal memuat detail tugas") }
            ))}
        }
    }

    fun putTodo(authToken: String, todoId: String, title: String, description: String, isDone: Boolean, urgency: String = "Low") {
        viewModelScope.launch {
            _uiState.update { it.copy(todoChange = TodoActionUIState.Loading) }
            val result = runCatching { repository.putTodo(authToken, todoId, RequestTodo(title, description, isDone, urgency)) }
            _uiState.update { it.copy(todoChange = result.fold(
                onSuccess = { res -> 
                    if (res.status == "success") TodoActionUIState.Success(res.message ?: "Berhasil memperbarui tugas") 
                    else TodoActionUIState.Error(res.message ?: "Gagal memperbarui tugas") 
                },
                onFailure = { e -> TodoActionUIState.Error(e.message ?: "Gagal memperbarui tugas") }
            ))}
        }
    }

    fun putTodoCover(authToken: String, todoId: String, file: MultipartBody.Part) {
        viewModelScope.launch {
            _uiState.update { it.copy(todoChangeCover = TodoActionUIState.Loading) }
            val result = runCatching { repository.putTodoCover(authToken, todoId, file) }
            _uiState.update { it.copy(todoChangeCover = result.fold(
                onSuccess = { res -> 
                    if (res.status == "success") TodoActionUIState.Success(res.message ?: "Berhasil memperbarui cover") 
                    else TodoActionUIState.Error(res.message ?: "Gagal memperbarui cover") 
                },
                onFailure = { e -> TodoActionUIState.Error(e.message ?: "Gagal memperbarui cover") }
            ))}
        }
    }

    fun deleteTodo(authToken: String, todoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(todoDelete = TodoActionUIState.Loading) }
            val result = runCatching { repository.deleteTodo(authToken, todoId) }
            _uiState.update { it.copy(todoDelete = result.fold(
                onSuccess = { res -> 
                    if (res.status == "success") TodoActionUIState.Success(res.message ?: "Berhasil menghapus tugas") 
                    else TodoActionUIState.Error(res.message ?: "Gagal menghapus tugas") 
                },
                onFailure = { e -> TodoActionUIState.Error(e.message ?: "Gagal menghapus tugas") }
            ))}
        }
    }

    fun putUserMe(authToken: String, name: String, username: String, about: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(profileUpdate = TodoActionUIState.Loading) }
            val result = runCatching { repository.putUserMe(authToken, RequestUserChange(name, username, about)) }
            _uiState.update { it.copy(profileUpdate = result.fold(
                onSuccess = { res -> 
                    if (res.status == "success") TodoActionUIState.Success(res.message ?: "Berhasil memperbarui profil") 
                    else TodoActionUIState.Error(res.message ?: "Gagal memperbarui profil") 
                },
                onFailure = { e -> TodoActionUIState.Error(e.message ?: "Gagal memperbarui profil") }
            ))}
        }
    }

    fun putUserMePassword(authToken: String, old: String, new: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(profileUpdate = TodoActionUIState.Loading) }
            val result = runCatching { repository.putUserMePassword(authToken, RequestUserChangePassword(old, new)) }
            _uiState.update { it.copy(profileUpdate = result.fold(
                onSuccess = { res -> 
                    if (res.status == "success") TodoActionUIState.Success(res.message ?: "Berhasil memperbarui password") 
                    else TodoActionUIState.Error(res.message ?: "Gagal memperbarui password") 
                },
                onFailure = { e -> TodoActionUIState.Error(e.message ?: "Gagal memperbarui password") }
            ))}
        }
    }

    fun putUserMePhoto(authToken: String, file: MultipartBody.Part) {
        viewModelScope.launch {
            _uiState.update { it.copy(profileUpdate = TodoActionUIState.Loading) }
            val result = runCatching { repository.putUserMePhoto(authToken, file) }
            _uiState.update { it.copy(profileUpdate = result.fold(
                onSuccess = { res -> 
                    if (res.status == "success") TodoActionUIState.Success(res.message ?: "Berhasil memperbarui foto") 
                    else TodoActionUIState.Error(res.message ?: "Gagal memperbarui foto") 
                },
                onFailure = { e -> TodoActionUIState.Error(e.message ?: "Gagal memperbarui foto") }
            ))}
        }
    }
    
    fun resetActionStates() {
        _uiState.update { it.copy(
            todoAdd = TodoActionUIState.Idle,
            todoChange = TodoActionUIState.Idle,
            todoDelete = TodoActionUIState.Idle,
            todoChangeCover = TodoActionUIState.Idle,
            profileUpdate = TodoActionUIState.Idle
        )}
    }
}
