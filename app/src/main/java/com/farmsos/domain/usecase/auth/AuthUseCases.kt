package com.farmsos.domain.usecase.auth

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthUseCases @Inject constructor(
    val login: LoginUseCase,
    val signUp: SignUpUseCase,
    val logout: LogoutUseCase,
    val resetPassword: ResetPasswordUseCase,
    val observeAuthState: ObserveAuthStateUseCase,
    val restoreSession: RestoreSessionUseCase
)
