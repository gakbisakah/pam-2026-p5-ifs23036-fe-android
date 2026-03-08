package org.delcom.pam_p5_ifs23036.ui.viewmodels

import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.delcom.pam_p5_ifs23036.network.todos.data.*
import org.delcom.pam_p5_ifs23036.network.todos.service.ITodoRepository
import org.delcom.pam_p5_ifs23036.prefs.AuthTokenPref
import javax.inject.Inject

sealed interface AuthUIState {
    data class Success(val data: ResponseAuthLogin) : AuthUIState
    data class Error(val message: String) : AuthUIState
    object Loading : AuthUIState
    object Idle : AuthUIState
}

sealed interface AuthActionUIState {
    data class Success(val message: String) : AuthActionUIState
    data class Error(val message: String) : AuthActionUIState
    object Loading : AuthActionUIState
    object Idle : AuthActionUIState
}

sealed interface AuthLogoutUIState {
    data class Success(val message: String) : AuthLogoutUIState
    data class Error(val message: String) : AuthLogoutUIState
    object Loading : AuthLogoutUIState
    object Idle : AuthLogoutUIState
}

data class UIStateAuth(
    val auth: AuthUIState = AuthUIState.Idle,
    val authRegister: AuthActionUIState = AuthActionUIState.Idle,
    val authLogout: AuthLogoutUIState = AuthLogoutUIState.Idle,
    val authRefreshToken: AuthActionUIState = AuthActionUIState.Idle,
)

@HiltViewModel
@Keep
class AuthViewModel @Inject constructor(
    private val repository: ITodoRepository,
    private val authTokenPref: AuthTokenPref
) : ViewModel() {
    private val _uiState = MutableStateFlow(UIStateAuth())
    val uiState = _uiState.asStateFlow()

    fun register(name: String, username: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(authRegister = AuthActionUIState.Loading) }
            val result = runCatching {
                repository.postRegister(RequestAuthRegister(name, username, password))
            }.fold(
                onSuccess = {
                    if (it.status == "success") AuthActionUIState.Success(it.data!!.userId)
                    else AuthActionUIState.Error(it.message)
                },
                onFailure = { AuthActionUIState.Error(it.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(authRegister = result) }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(auth = AuthUIState.Loading) }
            val result = runCatching {
                repository.postLogin(RequestAuthLogin(username, password))
            }.fold(
                onSuccess = {
                    if (it.status == "success" && it.data != null) {
                        authTokenPref.saveAuthToken(it.data.authToken)
                        authTokenPref.saveRefreshToken(it.data.refreshToken)
                        authTokenPref.saveUserId(it.data.userId)
                        AuthUIState.Success(it.data)
                    } else {
                        AuthUIState.Error(it.message)
                    }
                },
                onFailure = { AuthUIState.Error(it.message ?: "Koneksi gagal") }
            )
            _uiState.update { it.copy(auth = result) }
        }
    }

    fun logout(authToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(authLogout = AuthLogoutUIState.Loading) }
            authTokenPref.clearAuthToken()
            authTokenPref.clearRefreshToken()
            authTokenPref.clearUserId()
            
            runCatching {
                repository.postLogout(RequestAuthLogout(authToken = authToken))
            }
            
            _uiState.update { it.copy(
                authLogout = AuthLogoutUIState.Success("Berhasil keluar"),
                auth = AuthUIState.Idle
            ) }
        }
    }

    fun refreshToken(authToken: String, refreshToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(authRefreshToken = AuthActionUIState.Loading) }
            runCatching {
                repository.postRefreshToken(RequestAuthRefreshToken(authToken, refreshToken))
            }.onSuccess {
                if (it.status == "success" && it.data != null) {
                    authTokenPref.saveAuthToken(it.data.authToken)
                    authTokenPref.saveRefreshToken(it.data.refreshToken)
                    authTokenPref.saveUserId(it.data.userId)
                    _uiState.update { state -> state.copy(
                        auth = AuthUIState.Success(it.data),
                        authRefreshToken = AuthActionUIState.Success(it.message)
                    ) }
                } else {
                    _uiState.update { state -> state.copy(
                        auth = AuthUIState.Error("Token expired"),
                        authRefreshToken = AuthActionUIState.Error(it.message)
                    ) }
                }
            }.onFailure { e ->
                _uiState.update { state -> state.copy(
                    auth = AuthUIState.Error("Token expired"),
                    authRefreshToken = AuthActionUIState.Error(e.message ?: "Unknown error")
                ) }
            }
        }
    }

    fun loadTokenFromPreferences() {
        viewModelScope.launch {
            val authToken = authTokenPref.getAuthToken()
            val refreshToken = authTokenPref.getRefreshToken()
            val userId = authTokenPref.getUserId()

            if (authToken.isNullOrEmpty() || refreshToken.isNullOrEmpty() || userId.isNullOrEmpty()) {
                _uiState.update { it.copy(auth = AuthUIState.Idle) }
            } else {
                _uiState.update { it.copy(
                    auth = AuthUIState.Success(ResponseAuthLogin(authToken, refreshToken, userId))
                ) }
            }
        }
    }

    fun resetActionStates() {
        _uiState.update { it.copy(
            authRegister = AuthActionUIState.Idle,
            authLogout = AuthLogoutUIState.Idle,
            authRefreshToken = AuthActionUIState.Idle
        )}
    }
}