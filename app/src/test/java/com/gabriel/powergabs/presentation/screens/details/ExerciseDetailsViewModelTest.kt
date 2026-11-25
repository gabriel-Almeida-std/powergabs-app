package com.gabriel.powergabs.presentation.screens.details

import app.cash.turbine.test
import com.gabriel.powergabs.R
import com.gabriel.powergabs.core.result.AppError
import com.gabriel.powergabs.domain.model.ExerciseDetails
import com.gabriel.powergabs.domain.model.Goal
import com.gabriel.powergabs.domain.rule.PrescriptionRules
import com.gabriel.powergabs.domain.usecase.AddToWorkoutResult
import com.gabriel.powergabs.domain.usecase.AddToWorkoutUseCase
import com.gabriel.powergabs.domain.usecase.ObserveConnectivityUseCase
import com.gabriel.powergabs.domain.usecase.ObserveExerciseDetailsUseCase
import com.gabriel.powergabs.testutil.MainDispatcherRule
import com.gabriel.powergabs.testutil.exercise
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

/** RF10–RF12 — CA17 a CA19. */
class ExerciseDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val supino = exercise(id = 7, name = "Supino", videoUrl = "https://wger.de/v.MOV")
    private val details = MutableStateFlow<ExerciseDetails?>(
        ExerciseDetails(supino, PrescriptionRules.forGoal(Goal.HYPERTROPHY), isInWorkout = false),
    )
    private val online = MutableStateFlow(true)
    private val observeDetails = mockk<ObserveExerciseDetailsUseCase> {
        every { this@mockk.invoke(7, Goal.HYPERTROPHY) } returns details
    }
    private val observeConnectivity = mockk<ObserveConnectivityUseCase> { every { this@mockk.invoke() } returns online }
    private val addToWorkout = mockk<AddToWorkoutUseCase>()

    /** Cria o ViewModel e mantém o `uiState` sendo coletado em segundo plano, como a tela faria. */
    private fun TestScope.viewModel(): ExerciseDetailsViewModel {
        val viewModel = ExerciseDetailsViewModel(7, Goal.HYPERTROPHY, observeDetails, addToWorkout, observeConnectivity)
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        return viewModel
    }

    @Test
    fun `cached details are shown with the goal prescription`() = runTest {
        val viewModel = viewModel()

        val content = viewModel.uiState.value.content as DetailsContent.Success

        assertEquals(supino, content.details.exercise)
        assertEquals(PrescriptionRules.forGoal(Goal.HYPERTROPHY), content.details.prescription)
    }

    @Test
    fun `exercise missing from cache shows not found`() = runTest {
        details.value = null

        val viewModel = viewModel()

        assertEquals(DetailsContent.NotFound, viewModel.uiState.value.content)
    }

    @Test
    fun `offline state is exposed for the video area`() = runTest {
        online.value = false

        val viewModel = viewModel()

        assertFalse(viewModel.uiState.value.isOnline)
    }

    @Test
    fun `adding to workout confirms with a message`() = runTest {
        // Arrange
        coEvery { addToWorkout(supino, Goal.HYPERTROPHY) } returns AddToWorkoutResult.Added
        val viewModel = viewModel()

        viewModel.messages.test {
            // Act
            viewModel.onAddToWorkout()

            // Assert
            assertEquals(R.string.added_to_workout, awaitItem())
        }
        coVerify(exactly = 1) { addToWorkout(supino, Goal.HYPERTROPHY) }
    }

    @Test
    fun `exercise already in workout is not added again`() = runTest {
        details.value = details.value!!.copy(isInWorkout = true)
        val viewModel = viewModel()

        viewModel.onAddToWorkout()
        advanceUntilIdle()

        coVerify(exactly = 0) { addToWorkout(any(), any()) }
    }

    @Test
    fun `database failure while adding shows the error message`() = runTest {
        coEvery { addToWorkout(any(), any()) } returns AddToWorkoutResult.Failure(AppError.Database)
        val viewModel = viewModel()

        viewModel.messages.test {
            viewModel.onAddToWorkout()
            assertEquals(R.string.error_database, awaitItem())
        }
    }
}
