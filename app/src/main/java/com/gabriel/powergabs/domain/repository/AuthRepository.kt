package com.gabriel.powergabs.domain.repository

import com.gabriel.powergabs.core.result.AppError
import com.gabriel.powergabs.core.result.AppResult
import com.gabriel.powergabs.domain.model.User
import kotlinx.coroutines.flow.Flow

/** Resultado de cadastro/login no repositório. Recusas de negócio ficam separadas de falhas técnicas. */
sealed interface AuthResult {
    data class Success(val user: User) : AuthResult
    data object EmailAlreadyRegistered : AuthResult
    data object InvalidCredentials : AuthResult
    data class Failure(val error: AppError) : AuthResult
}

/**
 * Autenticação local (RF01–RF04). O domain não sabe como as senhas são guardadas:
 * a implementação (hash PBKDF2 + Room) fica no data.
 */
interface AuthRepository {
    /** Usuário da sessão atual; `null` quando ninguém está logado. */
    fun observeCurrentUser(): Flow<User?>
    suspend fun currentUser(): User?

    /** Recebe o e-mail já normalizado e a senha em texto puro (que nunca é gravada assim). */
    suspend fun register(email: String, password: String): AuthResult
    suspend fun login(email: String, password: String): AuthResult
    suspend fun logout(): AppResult<Unit>
}
