package com.gabriel.powergabs.data.auth

import com.gabriel.powergabs.core.dispatcher.AppDispatchers
import com.gabriel.powergabs.core.result.AppError
import com.gabriel.powergabs.core.result.AppResult
import com.gabriel.powergabs.core.time.TimeProvider
import com.gabriel.powergabs.data.local.dao.UserDao
import com.gabriel.powergabs.data.local.entity.SessionEntity
import com.gabriel.powergabs.data.local.entity.UserEntity
import com.gabriel.powergabs.data.mapper.toDomain
import com.gabriel.powergabs.data.repository.safeDbCall
import com.gabriel.powergabs.domain.model.User
import com.gabriel.powergabs.domain.repository.AuthRepository
import com.gabriel.powergabs.domain.repository.AuthResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Autenticação local (RF01–RF04): contas e sessão no Room, senha com hash PBKDF2.
 *
 * O cálculo do hash é pesado de propósito, por isso roda no dispatcher `default`
 * (CPU), e o acesso ao banco no `io`. Nada disso acontece na thread principal.
 */
class AuthRepositoryImpl(
    private val userDao: UserDao,
    private val hasher: PasswordHasher,
    private val timeProvider: TimeProvider,
    private val dispatchers: AppDispatchers,
) : AuthRepository {

    override fun observeCurrentUser(): Flow<User?> =
        userDao.observeSessionUser().map { it?.toDomain() }

    override suspend fun currentUser(): User? = withContext(dispatchers.io) {
        userDao.getSessionUser()?.toDomain()
    }

    override suspend fun register(email: String, password: String): AuthResult = guarded {
        if (withContext(dispatchers.io) { userDao.findByEmail(email) } != null) {
            return@guarded AuthResult.EmailAlreadyRegistered
        }
        val salt = hasher.newSalt()
        val hash = withContext(dispatchers.default) { hasher.hash(password, salt) }
        val id = withContext(dispatchers.io) {
            val newId = userDao.insert(
                UserEntity(
                    email = email,
                    passwordHash = hash,
                    salt = salt,
                    createdAt = timeProvider.now().toEpochMilli(),
                ),
            )
            userDao.saveSession(SessionEntity(userId = newId))
            newId
        }
        AuthResult.Success(User(id = id, email = email))
    }

    override suspend fun login(email: String, password: String): AuthResult = guarded {
        // Mesma resposta para "e-mail não existe" e "senha errada": não revela quais e-mails têm conta (CA04).
        val user = withContext(dispatchers.io) { userDao.findByEmail(email) }
            ?: return@guarded AuthResult.InvalidCredentials
        val matches = withContext(dispatchers.default) { hasher.verify(password, user.salt, user.passwordHash) }
        if (!matches) return@guarded AuthResult.InvalidCredentials

        withContext(dispatchers.io) { userDao.saveSession(SessionEntity(userId = user.id)) }
        AuthResult.Success(user.toDomain())
    }

    override suspend fun logout(): AppResult<Unit> = withContext(dispatchers.io) {
        safeDbCall { userDao.clearSession() }
    }

    /** Converte falhas inesperadas (banco, criptografia) em [AuthResult.Failure]. */
    private suspend fun guarded(block: suspend () -> AuthResult): AuthResult =
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AuthResult.Failure(AppError.Database)
        }
}
