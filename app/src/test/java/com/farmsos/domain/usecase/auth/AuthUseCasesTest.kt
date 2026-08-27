package com.farmsos.domain.usecase.auth

import com.farmsos.domain.model.AuthState
import com.farmsos.domain.model.User
import com.farmsos.testing.FakeAuthRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthUseCasesTest {

    private lateinit var repository: FakeAuthRepository
    private lateinit var useCases: AuthUseCases

    private val user = User(
        id = "u1",
        name = "Ada",
        email = "ada@farm.test",
        createdAt = 1L,
        updatedAt = 1L
    )

    @Before
    fun setUp() {
        repository = FakeAuthRepository()
        repository.seedUser("secret1", user)
        useCases = AuthUseCases(
            login = LoginUseCase(repository),
            signUp = SignUpUseCase(repository),
            logout = LogoutUseCase(repository),
            resetPassword = ResetPasswordUseCase(repository),
            observeAuthState = ObserveAuthStateUseCase(repository),
            restoreSession = RestoreSessionUseCase(repository)
        )
    }

    @Test
    fun loginSucceedsAndExposesAuthenticatedState() = runTest {
        val result = useCases.login("ada@farm.test", "secret1")
        assertTrue(result.isSuccess)
        assertEquals(user.id, result.getOrThrow().id)
        assertEquals(AuthState.Authenticated(user), useCases.observeAuthState().first())
    }

    @Test
    fun loginRejectsInvalidEmail() = runTest {
        val result = useCases.login("not-an-email", "secret1")
        assertTrue(result.isFailure)
    }

    @Test
    fun loginRejectsWrongPassword() = runTest {
        val result = useCases.login("ada@farm.test", "wrong-password")
        assertTrue(result.isFailure)
    }

    @Test
    fun logoutClearsSession() = runTest {
        useCases.login("ada@farm.test", "secret1")
        val result = useCases.logout()
        assertTrue(result.isSuccess)
        assertEquals(AuthState.Unauthenticated, useCases.observeAuthState().first())
    }

    @Test
    fun restoreSessionReturnsPersistedUser() = runTest {
        repository.emit(AuthState.Authenticated(user))
        val restored = useCases.restoreSession()
        assertEquals(user, restored)
        assertTrue(repository.restoreCalled)
    }

    @Test
    fun resetPasswordNormalizesEmail() = runTest {
        val result = useCases.resetPassword("  Ada@Farm.TEST  ")
        assertTrue(result.isSuccess)
        assertEquals("ada@farm.test", repository.lastResetEmail)
    }
}
