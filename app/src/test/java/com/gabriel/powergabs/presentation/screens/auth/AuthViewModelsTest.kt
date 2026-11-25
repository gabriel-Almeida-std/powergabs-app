package com.gabriel.powergabs.presentation.screens.auth

import app.cash.turbine.test
import com.gabriel.powergabs.R
import com.gabriel.powergabs.core.result.AppError
import com.gabriel.powergabs.domain.model.User
import com.gabriel.powergabs.domain.rule.CredentialsError
import com.gabriel.powergabs.domain.rule.CredentialsValidation
import com.gabriel.powergabs.domain.usecase.AuthOutcome
import com.gabriel.powergabs.domain.usecase.LoginUseCase
import com.gabriel.powergabs.domain.usecase.RegisterUseCase
import com.gabriel.powergabs.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** RF01, RF02 — CA01 a CA04. */
class AuthViewModelsTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val login = mockk<LoginUseCase>()
    private val register = mockk<RegisterUseCase>()

    @Test
    fun `login shows submitting and then field errors for invalid input`() = runTest {
        // Arrange
        coEvery { login("", "") } returns AuthOutcome.InvalidInput(
            CredentialsValidation(emailError = CredentialsError.EMAIL_EMPTY, passwordError = CredentialsError.PASSWORD_EMPTY),
        )
        val viewModel = LoginViewModel(login)

        viewModel.uiState.test {
            assertFalse(awaitItem().isSubmitting)

            // Act
            viewModel.onSubmit()

            // Assert
            assertTrue(awaitItem().isSubmitting)
            val result = awaitItem()
            assertFalse(result.isSubmitting)
            assertEquals(R.string.error_email_empty, result.emailError)
            assertEquals(R.string.error_password_empty, result.passwordError)
        }
    }

    @Test
    fun `wrong credentials show a general error that does not say which field is wrong`() = runTest {
        coEvery { login("g@mail.com", "errada") } returns AuthOutcome.InvalidCredentials
        val viewModel = LoginViewModel(login)
        viewModel.onEmailChange("g@mail.com")
        viewModel.onPasswordChange("errada")

        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(R.string.error_invalid_credentials, viewModel.uiState.value.generalError)
        assertNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun `typing again clears the field error`() = runTest {
        coEvery { login(any(), any()) } returns AuthOutcome.InvalidCredentials
        val viewModel = LoginViewModel(login)
        viewModel.onSubmit()
        advanceUntilIdle()

        viewModel.onPasswordChange("nova")

        assertNull(viewModel.uiState.value.generalError)
    }

    @Test
    fun `successful login ends the submission without errors`() = runTest {
        coEvery { login(any(), any()) } returns AuthOutcome.Success(User(1, "g@mail.com"))
        val viewModel = LoginViewModel(login)

        viewModel.onSubmit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSubmitting)
        assertNull(state.generalError)
    }

    @Test
    fun `technical failure shows the mapped message`() = runTest {
        coEvery { login(any(), any()) } returns AuthOutcome.Failure(AppError.Database)
        val viewModel = LoginViewModel(login)

        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(R.string.error_database, viewModel.uiState.value.generalError)
    }

    @Test
    fun `register sends the three fields and shows mismatch error`() = runTest {
        // Arrange
        coEvery { register("g@mail.com", "123456", "654321") } returns AuthOutcome.InvalidInput(
            CredentialsValidation(confirmationError = CredentialsError.CONFIRMATION_MISMATCH),
        )
        val viewModel = RegisterViewModel(register)
        viewModel.onEmailChange("g@mail.com")
        viewModel.onPasswordChange("123456")
        viewModel.onConfirmationChange("654321")

        // Act
        viewModel.onSubmit()
        advanceUntilIdle()

        // Assert
        assertEquals(R.string.error_confirmation_mismatch, viewModel.uiState.value.confirmationError)
        coVerify(exactly = 1) { register("g@mail.com", "123456", "654321") }
    }

    @Test
    fun `register with existing email marks the email field`() = runTest {
        coEvery { register(any(), any(), any()) } returns AuthOutcome.EmailAlreadyRegistered
        val viewModel = RegisterViewModel(register)

        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(R.string.error_email_already_registered, viewModel.uiState.value.emailError)
    }
}
