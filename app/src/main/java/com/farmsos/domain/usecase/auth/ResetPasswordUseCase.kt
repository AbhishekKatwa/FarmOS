package com.farmsos.domain.usecase.auth

import com.farmsos.domain.repository.AuthRepository
import javax.inject.Inject

class ResetPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        val normalized = email.trim().lowercase()
        if (normalized.isBlank() || !normalized.contains("@")) {
            return Result.failure(IllegalArgumentException("Please enter a valid email"))
        }
        return authRepository.resetPassword(normalized)
    }
}
