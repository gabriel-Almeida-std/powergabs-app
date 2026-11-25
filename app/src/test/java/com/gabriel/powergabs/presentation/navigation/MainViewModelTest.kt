package com.gabriel.powergabs.presentation.navigation

import app.cash.turbine.test
import com.gabriel.powergabs.domain.model.User
import com.gabriel.powergabs.domain.usecase.ObserveConnectivityUseCase
import com.gabriel.powergabs.domain.usecase.ObserveSessionUseCase
import com.gabriel.powergabs.testutil.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** RF03, RF04, RF16 — sessão e conectividade da raiz do app. */
class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val session = MutableStateFlow<User?>(null)
    private val online = MutableStateFlow(true)
    private val observeSession = mockk<ObserveSessionUseCase> { every { this@mockk.invoke() } returns session }
    private val observeConnectivity = mockk<ObserveConnectivityUseCase> { every { this@mockk.invoke() } returns online }

    private fun viewModel() = MainViewModel(observeSession, observeConnectivity)

    @Test
    fun `session starts loading and then follows the logged user`() = runTest {
        val viewModel = viewModel()

        viewModel.sessionState.test {
            assertEquals(SessionState.Loading, awaitItem())
            assertEquals(SessionState.LoggedOut, awaitItem())

            session.value = User(1, "g@mail.com")
            assertEquals(SessionState.LoggedIn(User(1, "g@mail.com")), awaitItem())

            session.value = null // logout
            assertEquals(SessionState.LoggedOut, awaitItem())
        }
    }

    @Test
    fun `connectivity changes are exposed`() = runTest {
        val viewModel = viewModel()

        viewModel.isOnline.test {
            assertEquals(true, awaitItem())
            online.value = false
            assertEquals(false, awaitItem())
        }
    }
}
