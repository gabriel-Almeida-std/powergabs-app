package com.gabriel.powergabs.presentation.screens.musclegroup

import app.cash.turbine.test
import com.gabriel.powergabs.domain.model.Goal
import com.gabriel.powergabs.domain.model.MuscleGroup
import com.gabriel.powergabs.domain.usecase.ObserveRestWarningUseCase
import com.gabriel.powergabs.testutil.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** RF06, RF09 — CA16. */
class MuscleGroupViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observeRestWarning = mockk<ObserveRestWarningUseCase>()

    @Test
    fun `no group is selected at first and all eight groups are offered`() = runTest {
        val viewModel = MuscleGroupViewModel(Goal.STRENGTH, observeRestWarning)

        val state = viewModel.uiState.value

        assertNull(state.selectedGroup)
        assertEquals(8, state.groups.size)
        assertEquals(Goal.STRENGTH, state.goal)
    }

    @Test
    fun `selecting a group trained yesterday shows the rest warning`() = runTest {
        // Arrange
        every { observeRestWarning(MuscleGroup.LEGS) } returns flowOf(true)
        val viewModel = MuscleGroupViewModel(Goal.HYPERTROPHY, observeRestWarning)

        viewModel.uiState.test {
            awaitItem() // inicial

            // Act
            viewModel.onGroupSelected(MuscleGroup.LEGS)

            // Assert
            val state = awaitItem()
            assertEquals(MuscleGroup.LEGS, state.selectedGroup)
            assertTrue(state.showRestWarning)
        }
    }

    @Test
    fun `changing to a rested group hides the warning`() = runTest {
        every { observeRestWarning(MuscleGroup.LEGS) } returns flowOf(true)
        every { observeRestWarning(MuscleGroup.CHEST) } returns flowOf(false)
        val viewModel = MuscleGroupViewModel(Goal.HYPERTROPHY, observeRestWarning)

        viewModel.uiState.test {
            awaitItem()
            viewModel.onGroupSelected(MuscleGroup.LEGS)
            assertTrue(awaitItem().showRestWarning)

            viewModel.onGroupSelected(MuscleGroup.CHEST)
            val state = awaitItem()
            assertEquals(MuscleGroup.CHEST, state.selectedGroup)
            assertFalse(state.showRestWarning)
        }
    }

    @Test
    fun `failure reading the history does not block the choice`() = runTest {
        every { observeRestWarning(MuscleGroup.BACK) } returns flow { throw IllegalStateException("db") }
        val viewModel = MuscleGroupViewModel(Goal.HYPERTROPHY, observeRestWarning)

        viewModel.uiState.test {
            awaitItem()
            viewModel.onGroupSelected(MuscleGroup.BACK)
            val state = awaitItem()
            assertEquals(MuscleGroup.BACK, state.selectedGroup)
            assertFalse(state.showRestWarning)
        }
    }
}
