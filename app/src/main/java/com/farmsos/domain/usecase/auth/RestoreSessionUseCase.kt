package com.farmsos.domain.usecase.auth

import com.farmsos.domain.model.User
import com.farmsos.domain.repository.AuthRepository
import javax.inject.Inject

class RestoreSessionUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): User? = authRepository.restoreSession()
}
