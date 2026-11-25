package com.gabriel.powergabs.presentation.screens.auth

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabriel.powergabs.R
import com.gabriel.powergabs.domain.usecase.AuthOutcome
import com.gabriel.powergabs.domain.usecase.LoginUseCase
import com.gabriel.powergabs.domain.usecase.RegisterUseCase
import com.gabriel.powergabs.presentation.util.ErrorMessageMapper
import com.gabriel.powergabs.presentation.util.messageRes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado do formulário de login/cadastro. Imutável: cada mudança gera uma cópia (`copy`),
 * o que permite ao Compose saber exatamente quando recompor.
 * Os erros são IDs de `strings.xml` (`@StringRes`), sem `Context` no ViewModel.
 */
data class AuthFormUiState(
    val email: String = "",
    val password: String = "",
    val confirmation: String = "",
    @StringRes val emailError: Int? = null,
    @StringRes val passwordError: Int? = null,
    @StringRes val confirmationError: Int? = null,
    @StringRes val generalError: Int? = null,
    val isSubmitting: Boolean = false,
)

/** Converte o resultado do caso de uso em erros de campo ou erro geral. */
private fun AuthFormUiState.withOutcome(outcome: AuthOutcome): AuthFormUiState = when (outcome) {
    // Sucesso: a sessão muda e a raiz troca para o grafo do usuário logado (não há navegação aqui).
    is AuthOutcome.Success -> copy(isSubmitting = false)
    is AuthOutcome.InvalidInput -> copy(
        isSubmitting = false,
        emailError = outcome.validation.emailError?.messageRes(),
        passwordError = outcome.validation.passwordError?.messageRes(),
        confirmationError = outcome.validation.confirmationError?.messageRes(),
    )
    AuthOutcome.InvalidCredentials -> copy(isSubmitting = false, generalError = R.string.error_invalid_credentials)
    AuthOutcome.EmailAlreadyRegistered -> copy(isSubmitting = false, emailError = R.string.error_email_already_registered)
    is AuthOutcome.Failure -> copy(isSubmitting = false, generalError = ErrorMessageMapper.messageFor(outcome.error))
}

/** Tela de login (RF02). */
class LoginViewModel(private val login: LoginUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthFormUiState())
    val uiState: StateFlow<AuthFormUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, emailError = null, generalError = null) }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, passwordError = null, generalError = null) }

    fun onSubmit() {
        val current = _uiState.value
        if (current.isSubmitting) return // evita duplo toque
        _uiState.update { it.copy(isSubmitting = true, generalError = null) }
        viewModelScope.launch {
            val outcome = login(current.email, current.password)
            _uiState.update { it.withOutcome(outcome) }
        }
    }
}

/** Tela de cadastro (RF01). */
class RegisterViewModel(private val register: RegisterUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthFormUiState())
    val uiState: StateFlow<AuthFormUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, emailError = null, generalError = null) }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, passwordError = null, generalError = null) }

    fun onConfirmationChange(value: String) =
        _uiState.update { it.copy(confirmation = value, confirmationError = null, generalError = null) }

    fun onSubmit() {
        val current = _uiState.value
        if (current.isSubmitting) return
        _uiState.update { it.copy(isSubmitting = true, generalError = null) }
        viewModelScope.launch {
            val outcome = register(current.email, current.password, current.confirmation)
            _uiState.update { it.withOutcome(outcome) }
        }
    }
}
