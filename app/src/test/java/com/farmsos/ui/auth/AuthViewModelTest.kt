package com.farmsos.ui.auth

import com.farmsos.domain.model.AuthState
import com.farmsos.domain.model.User
import com.farmsos.domain.usecase.auth.AuthUseCases
import com.farmsos.domain.usecase.auth.LoginUseCase
import com.farmsos.domain.usecase.auth.LogoutUseCase
import com.farmsos.domain.usecase.auth.ObserveAuthStateUseCase
import com.farmsos.domain.usecase.auth.ResetPasswordUseCase
import com.farmsos.domain.usecase.auth.RestoreSessionUseCase
import com.farmsos.domain.usecase.auth.SignUpUseCase
import com.farmsos.testing.FakeAuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeAuthRepository
    private lateinit var viewModel: AuthViewModel

    private val user = User(
        id = "u1",
        name = "Ada",
        email = "ada@farm.test",
        createdAt = 1L,
        updatedAt = 1L
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = FakeAuthRepository()
        repository.seedUser("secret1", user)
        viewModel = AuthViewModel(
            AuthUseCases(
                login = LoginUseCase(repository),
                signUp = SignUpUseCase(repository),
                logout = LogoutUseCase(repository),
                resetPassword = ResetPasswordUseCase(repository),
                observeAuthState = ObserveAuthStateUseCase(repository),
                restoreSession = RestoreSessionUseCase(repository)
            )
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loginUpdatesAuthenticationState() = runTest(dispatcher) {
        viewModel.login("ada@farm.test", "secret1")
        advanceUntilIdle()
        assertEquals(AuthState.Authenticated(user), viewModel.authState.value)
        assertTrue(!viewModel.uiState.value.isLoading)
    }

    @Test
    fun logoutReturnsToUnauthenticated() = runTest(dispatcher) {
        viewModel.login("ada@farm.test", "secret1")
        advanceUntilIdle()
        viewModel.logout()
        advanceUntilIdle()
        assertEquals(AuthState.Unauthenticated, viewModel.authState.value)
    }

    @Test
    fun resetPasswordSurfacesInfoMessage() = runTest(dispatcher) {
        viewModel.resetPassword("ada@farm.test")
        advanceUntilIdle()
        assertEquals("ada@farm.test", repository.lastResetEmail)
        assertTrue(viewModel.uiState.value.infoMessage!!.contains("Password reset"))
    }
}
