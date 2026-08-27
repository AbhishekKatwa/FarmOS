package com.farmsos.data.repository

import android.content.Intent
import com.farmsos.core.logging.AppLogger
import com.farmsos.data.mapper.toDomain
import com.farmsos.data.mapper.toDomainUser
import com.farmsos.data.remote.dto.ProfileDto
import com.farmsos.domain.model.AuthState
import com.farmsos.domain.model.User
import com.farmsos.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.handleDeeplinks
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val auth: Auth,
    private val postgrest: Postgrest,
    private val logger: AppLogger
) : AuthRepository {

    override val authState: Flow<AuthState> = auth.sessionStatus
        .mapLatest { status -> status.toAuthState() }
        .distinctUntilChanged()

    override val currentUser: Flow<User?> = authState.map { state ->
        (state as? AuthState.Authenticated)?.user
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return runCatching {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            val userInfo = auth.currentUserOrNull()
                ?: error("Login succeeded but no session was established")
            resolveUser(userInfo)
        }.onFailure { logger.e("Login failed: ${it.message}", it) }
    }

    override suspend fun signUp(name: String, email: String, password: String): Result<User> {
        return runCatching {
            val created = auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject { put("full_name", name) }
            }
            val userInfo = auth.currentUserOrNull() ?: created
            if (userInfo == null) {
                error("Account created. Confirm your email before signing in.")
            }
            resolveUser(userInfo)
        }.onFailure { logger.e("Sign-up failed: ${it.message}", it) }
    }

    override suspend fun logout(): Result<Unit> {
        return runCatching {
            auth.signOut()
        }.onFailure { logger.e("Logout failed: ${it.message}", it) }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return runCatching {
            auth.resetPasswordForEmail(email)
        }.onFailure { logger.e("Password reset failed: ${it.message}", it) }
    }

    override suspend fun restoreSession(): User? {
        auth.awaitInitialization()
        val userInfo = auth.currentUserOrNull() ?: return null
        return resolveUser(userInfo)
    }

    override suspend fun getSession(): User? {
        val userInfo = auth.currentUserOrNull() ?: return null
        return resolveUser(userInfo)
    }

    override fun handleAuthIntent(intent: Intent) {
        supabaseClient.handleDeeplinks(intent)
    }

    private suspend fun SessionStatus.toAuthState(): AuthState = when (this) {
        is SessionStatus.Initializing -> AuthState.Unknown
        is SessionStatus.NotAuthenticated -> AuthState.Unauthenticated
        is SessionStatus.RefreshFailure -> {
            val userInfo = auth.currentUserOrNull()
            if (userInfo != null) AuthState.Authenticated(resolveUser(userInfo)) else AuthState.Unknown
        }
        is SessionStatus.Authenticated -> {
            val userInfo = session.user
            if (userInfo == null) AuthState.Unauthenticated else AuthState.Authenticated(resolveUser(userInfo))
        }
    }

    private suspend fun resolveUser(userInfo: UserInfo): User {
        val profile = runCatching {
            postgrest["profiles"].select {
                ProfileDto::id eq userInfo.id
            }.decodeSingleOrNull<ProfileDto>()?.toDomain()
        }.onFailure { logger.w("Profile lookup failed: ${it.message}", it) }.getOrNull()
        return userInfo.toDomainUser(profile)
    }
}
