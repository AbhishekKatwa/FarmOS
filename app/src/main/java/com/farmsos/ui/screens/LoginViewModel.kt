package com.farmsos.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmsos.core.auth.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authManager: AuthManager
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || !email.contains("@")) {
            _state.value = _state.value.copy(errorMessage = "Please enter a valid email")
            return
        }
        if (password.isBlank() || password.length < 6) {
            _state.value = _state.value.copy(errorMessage = "Password must be at least 6 characters")
            return
        }

        _state.value = _state.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = authManager.login(email, password)
            _state.value = result.fold(
                onSuccess = { _state.value.copy(isLoading = false, isSuccess = true, userId = it.id) },
                onFailure = { _state.value.copy(isLoading = false, errorMessage = it.message ?: "Login failed") }
            )
        }
    }

    fun signUp(name: String, email: String, password: String) {
        if (name.isBlank()) {
            _state.value = _state.value.copy(errorMessage = "Name is required")
            return
        }
        if (email.isBlank() || !email.contains("@")) {
            _state.value = _state.value.copy(errorMessage = "Please enter a valid email")
            return
        }
        if (password.isBlank() || password.length < 6) {
            _state.value = _state.value.copy(errorMessage = "Password must be at least 6 characters")
            return
        }

        _state.value = _state.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = authManager.signUp(name, email, password)
            _state.value = result.fold(
                onSuccess = { _state.value.copy(isLoading = false, isSuccess = true, userId = it.id) },
                onFailure = { _state.value.copy(isLoading = false, errorMessage = it.message ?: "Sign-up failed") }
            )
        }
    }

    fun resetSuccess() {
        _state.value = _state.value.copy(isSuccess = false)
    }
}

data class LoginUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val userId: String? = null,
    val errorMessage: String? = null
)
