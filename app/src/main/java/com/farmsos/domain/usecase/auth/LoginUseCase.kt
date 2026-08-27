package com.farmsos.domain.usecase.auth

import com.farmsos.domain.model.User
import com.farmsos.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        val normalized = email.trim().lowercase()
        if (normalized.isBlank() || !normalized.contains("@")) {
            return Result.failure(IllegalArgumentException("Please enter a valid email"))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
        }
        return authRepository.login(normalized, password)
    }
}
