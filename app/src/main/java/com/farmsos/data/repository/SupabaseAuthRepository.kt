package com.farmsos.data.repository

import com.farmsos.domain.model.User
import com.farmsos.domain.model.UserRole
import com.farmsos.domain.repository.AuthRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseAuthRepository @Inject constructor(
    private val auth: Auth,
    private val postgrest: Postgrest
) : AuthRepository {

    override val currentUser: Flow<User?> = auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Authenticated -> {
                status.session.user?.toDomainUser()
            }
            else -> null
        }
    }

    override suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun logout(): Result<Unit> = runCatching {
        auth.signOut()
    }

    override suspend fun resetPassword(email: String): Result<Unit> = runCatching {
        auth.resetPasswordForEmail(email)
    }

    override suspend fun getSession(): User? {
        return auth.currentUserOrNull()?.toDomainUser()
    }

    private fun UserInfo.toDomainUser(): User {
        // In a real app, we would fetch the role from a profile table or user metadata
        // For now, we'll try to get it from metadata or default to WORKER
        val roleStr = userMetadata?.get("role")?.toString()?.removeSurrounding("\"")
        val role = when (roleStr) {
            "OWNER" -> UserRole.OWNER
            "MANAGER" -> UserRole.MANAGER
            else -> UserRole.WORKER
        }

        return User(
            id = id,
            name = userMetadata?.get("full_name")?.toString()?.removeSurrounding("\"") ?: email ?: "User",
            email = email ?: "",
            createdAt = System.currentTimeMillis(), // Supabase provides created_at as string/timestamp
            updatedAt = System.currentTimeMillis(),
            isActive = true,
            role = role
        )
    }
}
