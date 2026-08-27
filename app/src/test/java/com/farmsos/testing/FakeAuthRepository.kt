package com.farmsos.testing

import android.content.Intent
import com.farmsos.domain.model.AuthState
import com.farmsos.domain.model.User
import com.farmsos.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeAuthRepository : AuthRepository {
    private val state = MutableStateFlow<AuthState>(AuthState.Unknown)
    private val users = mutableMapOf<String, Pair<String, User>>()
    var lastResetEmail: String? = null
    var restoreCalled = false

    override val authState: Flow<AuthState> = state
    override val currentUser: Flow<User?> = state.map { (it as? AuthState.Authenticated)?.user }

    fun seedUser(password: String, user: User) {
        users[user.email] = password to user
    }

    fun emit(authState: AuthState) {
        state.value = authState
    }

    override suspend fun login(email: String, password: String): Result<User> {
        val record = users[email] ?: return Result.failure(IllegalArgumentException("Invalid email or password"))
        if (record.first != password) {
            return Result.failure(IllegalArgumentException("Invalid email or password"))
        }
        state.value = AuthState.Authenticated(record.second)
        return Result.success(record.second)
    }

    override suspend fun signUp(name: String, email: String, password: String): Result<User> {
        if (users.containsKey(email)) {
            return Result.failure(IllegalStateException("Email already registered"))
        }
        val user = User(
            id = "user-${users.size + 1}",
            name = name,
            email = email,
            createdAt = 1L,
            updatedAt = 1L
        )
        users[email] = password to user
        state.value = AuthState.Authenticated(user)
        return Result.success(user)
    }

    override suspend fun logout(): Result<Unit> {
        state.value = AuthState.Unauthenticated
        return Result.success(Unit)
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        lastResetEmail = email
        return Result.success(Unit)
    }

    override suspend fun restoreSession(): User? {
        restoreCalled = true
        return (state.value as? AuthState.Authenticated)?.user
    }

    override suspend fun getSession(): User? = (state.value as? AuthState.Authenticated)?.user

    override fun handleAuthIntent(intent: Intent) = Unit
}
