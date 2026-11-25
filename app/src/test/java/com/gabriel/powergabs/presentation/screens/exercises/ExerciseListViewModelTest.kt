package com.gabriel.powergabs.presentation.screens.exercises

import app.cash.turbine.test
import com.gabriel.powergabs.R
import com.gabriel.powergabs.core.result.AppError
import com.gabriel.powergabs.core.result.AppResult
import com.gabriel.powergabs.domain.model.Goal
import com.gabriel.powergabs.domain.model.MuscleGroup
import com.gabriel.powergabs.domain.model.SuggestedExercise
import com.gabriel.powergabs.domain.model.SuggestedExercises
import com.gabriel.powergabs.domain.rule.PrescriptionRules
import com.gabriel.powergabs.domain.usecase.ObserveRestWarningUseCase
import com.gabriel.powergabs.domain.usecase.ObserveSuggestedExercisesUseCase
import com.gabriel.powergabs.domain.usecase.RefreshExercisesUseCase
import com.gabriel.powergabs.testutil.MainDispatcherRule
import com.gabriel.powergabs.testutil.TEST_NOW
import com.gabriel.powergabs.testutil.exercise
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** RF07–RF09, RNF01 — CA12 a CA16 na camada de apresentação. */
class ExerciseListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val cache = MutableStateFlow(SuggestedExercises(emptyList(), lastUpdated = null))
    private val observeSuggested = mockk<ObserveSuggestedExercisesUseCase> {
        every { this@mockk.invoke(Goal.STRENGTH, MuscleGroup.CHEST) } returns cache
    }
    private val refresh = mockk<RefreshExercisesUseCase>()
    private val observeRestWarning = mockk<ObserveRestWarningUseCase> {
        every { this@mockk.invoke(MuscleGroup.CHEST) } returns flowOf(false)
    }

    private val cachedItem = SuggestedExercise(exercise(id = 1, name = "Supino"), PrescriptionRules.forGoal(Goal.STRENGTH))

    private fun viewModel() = ExerciseListViewModel(Goal.STRENGTH, MuscleGroup.CHEST, observeSuggested, refresh, observeRestWarning)

    @Test
    fun `cached exercises are shown and the first refresh is not forced`() = runTest {
        // Arrange
        cache.value = SuggestedExercises(listOf(cachedItem), TEST_NOW)
        coEvery { refresh(Goal.STRENGTH, MuscleGroup.CHEST, false) } returns AppResult.Success(Unit)

        // Act
        val viewModel = viewModel()

        // Assert
        viewModel.uiState.test {
            awaitItem() // valor inicial
            val state = awaitItem()
            assertEquals(ExerciseListContent.Success(listOf(cachedItem)), state.content)
            assertEquals(TEST_NOW, state.lastUpdated)
        }
        coVerify(exactly = 1) { refresh(Goal.STRENGTH, MuscleGroup.CHEST, false) }
    }

    @Test
    fun `without cache the screen loads until the data arrives`() = runTest {
        val gate = CompletableDeferred<AppResult<Unit>>()
        coEvery { refresh(any(), any(), any()) } coAnswers { gate.await() }
        val viewModel = viewModel()

        viewModel.uiState.test {
            assertEquals(ExerciseListContent.Loading, awaitItem().content)

            cache.value = SuggestedExercises(listOf(cachedItem), TEST_NOW) // o Room recebeu os dados
            gate.complete(AppResult.Success(Unit))
            advanceUntilIdle()

            assertEquals(ExerciseListContent.Success(listOf(cachedItem)), expectMostRecentItem().content)
        }
    }

    @Test
    fun `failure without cache shows the error state with retry`() = runTest {
        coEvery { refresh(any(), any(), any()) } returns AppResult.Failure(AppError.Network)
        val viewModel = viewModel()

        viewModel.uiState.test {
            advanceUntilIdle()
            assertEquals(ExerciseListContent.Error(R.string.error_network), expectMostRecentItem().content)
        }
    }

    @Test
    fun `failure with cache keeps the data and sends a snackbar message`() = runTest {
        // Arrange
        cache.value = SuggestedExercises(listOf(cachedItem), TEST_NOW.minusSeconds(100_000))
        coEvery { refresh(any(), any(), any()) } returns AppResult.Failure(AppError.Http(503))
        val viewModel = viewModel()

        // Act + Assert
        viewModel.messages.test {
            assertEquals(R.string.error_http, awaitItem())
        }
        viewModel.uiState.test {
            advanceUntilIdle()
            assertEquals(ExerciseListContent.Success(listOf(cachedItem)), expectMostRecentItem().content)
        }
    }

    @Test
    fun `pull to refresh forces the request and reports refreshing`() = runTest {
        cache.value = SuggestedExercises(listOf(cachedItem), TEST_NOW)
        coEvery { refresh(any(), any(), false) } returns AppResult.Success(Unit)
        val gate = CompletableDeferred<AppResult<Unit>>()
        coEvery { refresh(any(), any(), true) } coAnswers { gate.await() }
        val viewModel = viewModel()

        viewModel.uiState.test {
            advanceUntilIdle()
            expectMostRecentItem()

            viewModel.onRefresh()
            assertTrue(awaitItem().isRefreshing)

            gate.complete(AppResult.Success(Unit))
            assertFalse(awaitItem().isRefreshing)
        }
        coVerify(exactly = 1) { refresh(Goal.STRENGTH, MuscleGroup.CHEST, true) }
    }

    @Test
    fun `pull to refresh without internet keeps cache and says it could not update`() = runTest {
        cache.value = SuggestedExercises(listOf(cachedItem), TEST_NOW)
        coEvery { refresh(any(), any(), false) } returns AppResult.Success(Unit)
        coEvery { refresh(any(), any(), true) } returns AppResult.Failure(AppError.Network)
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.messages.test {
            viewModel.onRefresh()
            assertEquals(R.string.error_refresh_offline, awaitItem())
        }
    }

    @Test
    fun `downloaded group with nothing for the goal shows the empty state`() = runTest {
        cache.value = SuggestedExercises(emptyList(), TEST_NOW)
        coEvery { refresh(any(), any(), any()) } returns AppResult.Success(Unit)
        val viewModel = viewModel()

        viewModel.uiState.test {
            advanceUntilIdle()
            assertEquals(ExerciseListContent.Empty, expectMostRecentItem().content)
        }
    }

    @Test
    fun `rest warning is exposed in the list`() = runTest {
        every { observeRestWarning(MuscleGroup.CHEST) } returns flowOf(true)
        coEvery { refresh(any(), any(), any()) } returns AppResult.Success(Unit)
        val viewModel = viewModel()

        viewModel.uiState.test {
            advanceUntilIdle()
            assertTrue(expectMostRecentItem().showRestWarning)
        }
    }
}
