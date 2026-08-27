package com.farmsos.domain.repository

import android.content.Intent
import com.farmsos.domain.model.AuthState
import com.farmsos.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val authState: Flow<AuthState>
    val currentUser: Flow<User?>

    suspend fun login(email: String, password: String): Result<User>
    suspend fun signUp(name: String, email: String, password: String): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun resetPassword(email: String): Result<Unit>
    suspend fun restoreSession(): User?
    suspend fun getSession(): User?
    fun handleAuthIntent(intent: Intent)
}
