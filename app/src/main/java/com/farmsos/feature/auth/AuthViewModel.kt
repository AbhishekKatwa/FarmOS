package com.farmsos.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmsos.domain.model.User
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

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authUseCases.observeAuthState().collect { user ->
                _currentUser.value = user
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authUseCases.login(email, password)
                .onSuccess {
                    _authState.value = AuthState.Success
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Login failed")
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
            _authState.value = AuthState.Loading
            authUseCases.resetPassword(email)
                .onSuccess {
                    _authState.value = AuthState.PasswordResetSent
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Failed to send reset email")
                }
        }
    }

    fun clearState() {
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    object PasswordResetSent : AuthState()
    data class Error(val message: String) : AuthState()
}
