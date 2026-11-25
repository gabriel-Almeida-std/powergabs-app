package com.gabriel.powergabs.presentation.screens.workout

import app.cash.turbine.test
import com.gabriel.powergabs.R
import com.gabriel.powergabs.core.result.AppError
import com.gabriel.powergabs.domain.model.WorkoutFilter
import com.gabriel.powergabs.domain.model.WorkoutStatus
import com.gabriel.powergabs.domain.usecase.MarkDoneOutcome
import com.gabriel.powergabs.domain.usecase.MarkExerciseDoneUseCase
import com.gabriel.powergabs.domain.usecase.ObserveWorkoutUseCase
import com.gabriel.powergabs.domain.usecase.RateEffortOutcome
import com.gabriel.powergabs.domain.usecase.RateEffortUseCase
import com.gabriel.powergabs.testutil.MainDispatcherRule
import com.gabriel.powergabs.testutil.TEST_NOW
import com.gabriel.powergabs.testutil.plannedExercise
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** RF13–RF15 — CA20 a CA22. */
class WorkoutViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val planned = plannedExercise(id = 1, exerciseId = 1)
    private val done = plannedExercise(id = 2, exerciseId = 2, status = WorkoutStatus.DONE, doneAt = TEST_NOW, effort = 3)

    private val observeWorkout = mockk<ObserveWorkoutUseCase> {
        every { this@mockk.invoke(WorkoutFilter.ALL) } returns flowOf(listOf(planned, done))
        every { this@mockk.invoke(WorkoutFilter.DONE) } returns flowOf(listOf(done))
        every { this@mockk.invoke(WorkoutFilter.PLANNED) } returns flowOf(emptyList())
    }
    private val markDone = mockk<MarkExerciseDoneUseCase>()
    private val rateEffort = mockk<RateEffortUseCase>()

    private fun viewModel() = WorkoutViewModel(observeWorkout, markDone, rateEffort)

    @Test
    fun `all items are shown by default`() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertEquals(WorkoutFilter.ALL, state.filter)
            assertEquals(WorkoutContent.Success(listOf(planned, done)), state.content)
        }
    }

    @Test
    fun `done filter shows only done exercises`() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            advanceUntilIdle()
            viewModel.onFilterSelected(WorkoutFilter.DONE)
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertEquals(WorkoutFilter.DONE, state.filter)
            assertEquals(WorkoutContent.Success(listOf(done)), state.content)
        }
    }

    @Test
    fun `filter without items shows the empty state`() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            viewModel.onFilterSelected(WorkoutFilter.PLANNED)
            advanceUntilIdle()
            assertEquals(WorkoutContent.Empty, expectMostRecentItem().content)
        }
    }

    @Test
    fun `mark done calls the use case`() = runTest {
        coEvery { markDone(1) } returns MarkDoneOutcome.Done
        val viewModel = viewModel()

        viewModel.onMarkDone(1)
        advanceUntilIdle()

        coVerify(exactly = 1) { markDone(1) }
    }

    @Test
    fun `failure to mark done shows a message`() = runTest {
        coEvery { markDone(1) } returns MarkDoneOutcome.Failure(AppError.Database)
        val viewModel = viewModel()

        viewModel.messages.test {
            viewModel.onMarkDone(1)
            assertEquals(R.string.error_database, awaitItem())
        }
    }

    @Test
    fun `effort is sent to the use case`() = runTest {
        coEvery { rateEffort(2, 5) } returns RateEffortOutcome.Saved
        val viewModel = viewModel()

        viewModel.onRateEffort(2, 5)
        advanceUntilIdle()

        coVerify(exactly = 1) { rateEffort(2, 5) }
    }

    @Test
    fun `rejected effort shows a message`() = runTest {
        coEvery { rateEffort(1, 3) } returns RateEffortOutcome.NotDone
        val viewModel = viewModel()

        viewModel.messages.test {
            viewModel.onRateEffort(1, 3)
            assertEquals(R.string.error_unknown, awaitItem())
        }
    }
}
