package com.gabriel.powergabs.data.auth

import com.gabriel.powergabs.core.dispatcher.AppDispatchers
import com.gabriel.powergabs.core.result.AppError
import com.gabriel.powergabs.core.result.AppResult
import com.gabriel.powergabs.data.local.dao.UserDao
import com.gabriel.powergabs.data.local.entity.SessionEntity
import com.gabriel.powergabs.data.local.entity.UserEntity
import com.gabriel.powergabs.domain.model.User
import com.gabriel.powergabs.domain.repository.AuthResult
import com.gabriel.powergabs.testutil.FixedTimeProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/** RF01–RF04 — cadastro, login, sessão e logout locais. */
class AuthRepositoryImplTest {

    private val userDao = mockk<UserDao>(relaxUnitFun = true)
    private val hasher = PasswordHasher(iterations = 1_000)

    private fun TestScope.repository(): AuthRepositoryImpl {
        val dispatcher = StandardTestDispatcher(testScheduler)
        return AuthRepositoryImpl(userDao, hasher, FixedTimeProvider(), AppDispatchers(dispatcher, dispatcher))
    }

    private fun storedUser(password: String): UserEntity {
        val salt = hasher.newSalt()
        return UserEntity(id = 7, email = "g@mail.com", passwordHash = hasher.hash(password, salt), salt = salt, createdAt = 0)
    }

    @Test
    fun `register stores a hash and opens a session`() = runTest {
        // Arrange
        val inserted = slot<UserEntity>()
        coEvery { userDao.findByEmail("g@mail.com") } returns null
        coEvery { userDao.insert(capture(inserted)) } returns 7L

        // Act
        val result = repository().register("g@mail.com", "segredo123")

        // Assert
        assertEquals(AuthResult.Success(User(7, "g@mail.com")), result)
        assertNotEquals("segredo123", inserted.captured.passwordHash)
        coVerify(exactly = 1) { userDao.saveSession(SessionEntity(userId = 7)) }
    }

    @Test
    fun `register with an existing email is refused`() = runTest {
        coEvery { userDao.findByEmail("g@mail.com") } returns storedUser("segredo123")

        val result = repository().register("g@mail.com", "outra123")

        assertEquals(AuthResult.EmailAlreadyRegistered, result)
        coVerify(exactly = 0) { userDao.insert(any()) }
    }

    @Test
    fun `login with the right password opens a session`() = runTest {
        coEvery { userDao.findByEmail("g@mail.com") } returns storedUser("segredo123")

        val result = repository().login("g@mail.com", "segredo123")

        assertEquals(AuthResult.Success(User(7, "g@mail.com")), result)
        coVerify(exactly = 1) { userDao.saveSession(SessionEntity(userId = 7)) }
    }

    @Test
    fun `login with wrong password is refused without session`() = runTest {
        coEvery { userDao.findByEmail("g@mail.com") } returns storedUser("segredo123")

        val result = repository().login("g@mail.com", "errada123")

        assertEquals(AuthResult.InvalidCredentials, result)
        coVerify(exactly = 0) { userDao.saveSession(any()) }
    }

    @Test
    fun `login with unknown email gives the same answer as wrong password`() = runTest {
        coEvery { userDao.findByEmail(any()) } returns null

        assertEquals(AuthResult.InvalidCredentials, repository().login("x@mail.com", "segredo123"))
    }

    @Test
    fun `database failure becomes an auth failure`() = runTest {
        coEvery { userDao.findByEmail(any()) } throws IllegalStateException("db closed")

        assertEquals(AuthResult.Failure(AppError.Database), repository().login("g@mail.com", "segredo123"))
    }

    @Test
    fun `logout clears the session`() = runTest {
        val result = repository().logout()

        assertEquals(AppResult.Success(Unit), result)
        coVerify(exactly = 1) { userDao.clearSession() }
    }
}
