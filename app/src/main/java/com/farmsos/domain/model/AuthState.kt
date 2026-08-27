package com.farmsos.domain.model

sealed class AuthState {
    data object Unknown : AuthState()
    data object Unauthenticated : AuthState()
    data class Authenticated(val user: User) : AuthState()
}
