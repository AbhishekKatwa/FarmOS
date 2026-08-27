package com.farmsos.domain.usecase.auth

import com.farmsos.domain.model.AuthState
import com.farmsos.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAuthStateUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<AuthState> = authRepository.authState
}
