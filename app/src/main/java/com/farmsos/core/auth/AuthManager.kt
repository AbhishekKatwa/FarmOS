package com.farmsos.core.auth

import com.farmsos.core.logging.AppLogger
import com.farmsos.domain.model.User
import com.farmsos.domain.model.UserRole
import com.farmsos.domain.repository.UserRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-app authentication backend.
 *
 * For now this is a local-only auth service backed by the existing
 * UserRepository (Room). The `apiClient` field on the repository impls
 * is already wired for an eventual remote backend swap.
 */
@Singleton
class AuthManager @Inject constructor(
    private val userRepository: UserRepository,
    private val logger: AppLogger
) {
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val emailResult = userRepository.getUserByEmail(email.trim().lowercase())
            emailResult.fold(
                onSuccess = { user ->
                    if (user.password == password) {
                        if (!user.isActive) {
                            Result.failure(IllegalStateException("Account is inactive"))
                        } else {
                            logger.i("Login success for ${user.email}")
                            Result.success(user)
                        }
                    } else {
                        Result.failure(IllegalArgumentException("Invalid email or password"))
                    }
                },
                onFailure = {
                    Result.failure(IllegalArgumentException("Invalid email or password"))
                }
            )
        } catch (e: Exception) {
            logger.e("Login error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun signUp(name: String, email: String, password: String): Result<User> {
        return try {
            val normalizedEmail = email.trim().lowercase()
            val existing = userRepository.getUserByEmail(normalizedEmail)
            if (existing.isSuccess) {
                return Result.failure(IllegalStateException("Email already registered"))
            }

            val now = System.currentTimeMillis()
            val user = User(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                email = normalizedEmail,
                password = password,
                createdAt = now,
                updatedAt = now,
                isActive = true,
                role = UserRole.FARMER
            )
            userRepository.insertUser(user).fold(
                onSuccess = {
                    logger.i("Sign-up success for ${user.email}")
                    Result.success(user)
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            logger.e("Sign-up error: ${e.message}", e)
            Result.failure(e)
        }
    }
}
