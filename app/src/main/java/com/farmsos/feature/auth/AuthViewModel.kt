package com.farmsos.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmsos.domain.model.User
import com.farmsos.domain.model.AuthState as DomainAuthState
import com.farmsos.domain.usecase.auth.AuthUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authUseCases: AuthUseCases
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authUseCases.observeAuthState().collect { state ->
                _currentUser.value = when (state) {
                    is DomainAuthState.Authenticated -> state.user
                    else -> null
                }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            authUseCases.login(email, password)
                .onSuccess {
                    _authState.value = AuthUiState.Success
                }
                .onFailure { error ->
                    _authState.value = AuthUiState.Error(error.message ?: "Login failed")
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authUseCases.logout()
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            authUseCases.resetPassword(email)
                .onSuccess {
                    _authState.value = AuthUiState.PasswordResetSent
                }
                .onFailure { error ->
                    _authState.value = AuthUiState.Error(error.message ?: "Failed to send reset email")
                }
        }
    }

    fun clearState() {
        _authState.value = AuthUiState.Idle
    }
}

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    object PasswordResetSent : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
