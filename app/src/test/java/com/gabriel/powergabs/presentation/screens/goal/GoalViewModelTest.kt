package com.gabriel.powergabs.presentation.screens.goal

import app.cash.turbine.test
import com.gabriel.powergabs.core.result.AppResult
import com.gabriel.powergabs.domain.model.Goal
import com.gabriel.powergabs.domain.model.User
import com.gabriel.powergabs.domain.usecase.LogoutUseCase
import com.gabriel.powergabs.domain.usecase.ObserveSessionUseCase
import com.gabriel.powergabs.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/** RF04, RF05 — seleção de objetivo e logout. */
class GoalViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observeSession = mockk<ObserveSessionUseCase> {
        every { this@mockk.invoke() } returns flowOf(User(1, "g@mail.com"))
    }
    private val logout = mockk<LogoutUseCase>()

    @Test
    fun `state shows the logged user and no goal selected at first`() = runTest {
        val viewModel = GoalViewModel(observeSession, logout)

        viewModel.uiState.test {
            awaitItem() // valor inicial do stateIn
            val state = awaitItem()
            assertEquals("g@mail.com", state.userEmail)
            assertNull(state.selectedGoal)
            assertEquals(Goal.entries.toList(), state.goals)
        }
    }

    @Test
    fun `selecting a goal updates the state`() = runTest {
        val viewModel = GoalViewModel(observeSession, logout)

        viewModel.uiState.test {
            skipItems(2)
            viewModel.onGoalSelected(Goal.ENDURANCE)
            assertEquals(Goal.ENDURANCE, awaitItem().selectedGoal)
        }
    }

    @Test
    fun `logout calls the use case`() = runTest {
        coEvery { logout() } returns AppResult.Success(Unit)
        val viewModel = GoalViewModel(observeSession, logout)

        viewModel.onLogout()
        advanceUntilIdle()

        coVerify(exactly = 1) { logout() }
    }
}
