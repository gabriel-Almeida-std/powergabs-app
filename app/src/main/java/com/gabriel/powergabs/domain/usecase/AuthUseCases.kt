package com.gabriel.powergabs.domain.usecase

import com.gabriel.powergabs.core.result.AppError
import com.gabriel.powergabs.core.result.AppResult
import com.gabriel.powergabs.domain.model.User
import com.gabriel.powergabs.domain.repository.AuthRepository
import com.gabriel.powergabs.domain.repository.AuthResult
import com.gabriel.powergabs.domain.rule.CredentialsRules
import com.gabriel.powergabs.domain.rule.CredentialsValidation
import kotlinx.coroutines.flow.Flow

/** O que a tela de login/cadastro precisa saber depois de uma tentativa. */
sealed interface AuthOutcome {
    data class Success(val user: User) : AuthOutcome

    /** Algum campo não passou na validação (RN15); nada foi enviado ao repositório. */
    data class InvalidInput(val validation: CredentialsValidation) : AuthOutcome
    data object EmailAlreadyRegistered : AuthOutcome
    data object InvalidCredentials : AuthOutcome
    data class Failure(val error: AppError) : AuthOutcome
}

private fun AuthResult.toOutcome(): AuthOutcome = when (this) {
    is AuthResult.Success -> AuthOutcome.Success(user)
    AuthResult.EmailAlreadyRegistered -> AuthOutcome.EmailAlreadyRegistered
    AuthResult.InvalidCredentials -> AuthOutcome.InvalidCredentials
    is AuthResult.Failure -> AuthOutcome.Failure(error)
}

/** Cria uma conta local (RF01): valida (RN15), normaliza o e-mail e delega ao repositório. */
class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String, confirmation: String): AuthOutcome {
        val validation = CredentialsRules.validateRegistration(email, password, confirmation)
        if (!validation.isValid) return AuthOutcome.InvalidInput(validation)
        return repository.register(CredentialsRules.normalizeEmail(email), password).toOutcome()
    }
}

/** Entra com uma conta local existente (RF02). */
class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): AuthOutcome {
        val validation = CredentialsRules.validateLogin(email, password)
        if (!validation.isValid) return AuthOutcome.InvalidInput(validation)
        return repository.login(CredentialsRules.normalizeEmail(email), password).toOutcome()
    }
}

/** Encerra a sessão (RF04). */
class LogoutUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): AppResult<Unit> = repository.logout()
}

/** Usuário logado, ou `null` (RF03). Decide se o app abre no login ou no objetivo. */
class ObserveSessionUseCase(private val repository: AuthRepository) {
    operator fun invoke(): Flow<User?> = repository.observeCurrentUser()
}
