package com.farmsos.domain.usecase.auth

import com.farmsos.domain.model.User
import com.farmsos.domain.repository.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(name: String, email: String, password: String): Result<User> {
        val trimmedName = name.trim()
        val normalized = email.trim().lowercase()
        if (trimmedName.isBlank()) {
            return Result.failure(IllegalArgumentException("Name is required"))
        }
        if (normalized.isBlank() || !normalized.contains("@")) {
            return Result.failure(IllegalArgumentException("Please enter a valid email"))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
        }
        return authRepository.signUp(trimmedName, normalized, password)
    }
}
