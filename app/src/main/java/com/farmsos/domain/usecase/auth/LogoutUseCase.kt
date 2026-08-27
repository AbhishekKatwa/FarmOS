package com.farmsos.domain.usecase.auth

import com.farmsos.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> = authRepository.logout()
}
