package com.farmsos.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmsos.domain.model.AuthState
import com.farmsos.domain.usecase.auth.AuthUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authUseCases: AuthUseCases
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unknown)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authUseCases.restoreSession()
            authUseCases.observeAuthState().collect { state ->
                _authState.value = state
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            authUseCases.login(email, password).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Login failed")
                    }
                }
            )
        }
    }

    fun signUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            authUseCases.signUp(name, email, password).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Sign-up failed")
                    }
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authUseCases.logout().fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false) } },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Logout failed")
                    }
                }
            )
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            authUseCases.resetPassword(email).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            infoMessage = "Password reset email sent. Check your inbox."
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Could not send reset email"
                        )
                    }
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, infoMessage = null) }
    }
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)
