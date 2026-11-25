package com.gabriel.powergabs.domain.usecase

import com.gabriel.powergabs.core.result.AppError
import com.gabriel.powergabs.domain.model.User
import com.gabriel.powergabs.domain.repository.AuthRepository
import com.gabriel.powergabs.domain.repository.AuthResult
import com.gabriel.powergabs.domain.rule.CredentialsError
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** RF01, RF02, RN15 — cadastro e login. */
class AuthUseCasesTest {

    private val repository = mockk<AuthRepository>()
    private val user = User(id = 1, email = "gabriel@mail.com")

    @Test
    fun `register normalizes email before calling repository`() = runTest {
        // Arrange
        coEvery { repository.register("gabriel@mail.com", "123456") } returns AuthResult.Success(user)

        // Act
        val outcome = RegisterUseCase(repository)("  Gabriel@Mail.com ", "123456", "123456")

        // Assert
        assertEquals(AuthOutcome.Success(user), outcome)
        coVerify(exactly = 1) { repository.register("gabriel@mail.com", "123456") }
    }

    @Test
    fun `invalid registration does not reach the repository`() = runTest {
        val outcome = RegisterUseCase(repository)("gabriel@mail.com", "123", "123")

        assertTrue(outcome is AuthOutcome.InvalidInput)
        assertEquals(
            CredentialsError.PASSWORD_TOO_SHORT,
            (outcome as AuthOutcome.InvalidInput).validation.passwordError,
        )
        coVerify(exactly = 0) { repository.register(any(), any()) }
    }

    @Test
    fun `register with existing email reports it`() = runTest {
        coEvery { repository.register(any(), any()) } returns AuthResult.EmailAlreadyRegistered

        val outcome = RegisterUseCase(repository)("gabriel@mail.com", "123456", "123456")

        assertEquals(AuthOutcome.EmailAlreadyRegistered, outcome)
    }

    @Test
    fun `login with wrong password reports invalid credentials`() = runTest {
        coEvery { repository.login("gabriel@mail.com", "wrong1") } returns AuthResult.InvalidCredentials

        val outcome = LoginUseCase(repository)("gabriel@mail.com", "wrong1")

        assertEquals(AuthOutcome.InvalidCredentials, outcome)
    }

    @Test
    fun `login technical failure is passed through`() = runTest {
        coEvery { repository.login(any(), any()) } returns AuthResult.Failure(AppError.Database)

        val outcome = LoginUseCase(repository)("gabriel@mail.com", "123456")

        assertEquals(AuthOutcome.Failure(AppError.Database), outcome)
    }

    @Test
    fun `login with empty email does not reach the repository`() = runTest {
        val outcome = LoginUseCase(repository)("", "123456")

        assertTrue(outcome is AuthOutcome.InvalidInput)
        coVerify(exactly = 0) { repository.login(any(), any()) }
    }
}
