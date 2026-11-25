package com.gabriel.powergabs.domain.usecase

import app.cash.turbine.test
import com.gabriel.powergabs.core.result.AppError
import com.gabriel.powergabs.core.result.AppResult
import com.gabriel.powergabs.domain.model.Goal
import com.gabriel.powergabs.domain.model.MuscleGroup
import com.gabriel.powergabs.domain.model.PlannedExercise
import com.gabriel.powergabs.domain.model.User
import com.gabriel.powergabs.domain.model.WorkoutFilter
import com.gabriel.powergabs.domain.model.WorkoutStatus
import com.gabriel.powergabs.domain.repository.AuthRepository
import com.gabriel.powergabs.domain.repository.WorkoutRepository
import com.gabriel.powergabs.domain.rule.PrescriptionRules
import com.gabriel.powergabs.testutil.FixedTimeProvider
import com.gabriel.powergabs.testutil.TEST_NOW
import com.gabriel.powergabs.testutil.exercise
import com.gabriel.powergabs.testutil.plannedExercise
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration

class WorkoutUseCasesTest {

    private val workoutRepository = mockk<WorkoutRepository>()
    private val authRepository = mockk<AuthRepository>()
    private val time = FixedTimeProvider()
    private val user = User(id = 1, email = "g@mail.com")

    init {
        coEvery { authRepository.currentUser() } returns user
        every { authRepository.observeCurrentUser() } returns flowOf(user)
    }

    // ---------- AddToWorkoutUseCase (RF12, RN11, RN12) ----------

    @Test
    fun `adding saves the prescription of the current goal`() = runTest {
        // Arrange
        val saved = slot<PlannedExercise>()
        coEvery { workoutRepository.isInWorkout(1, 5) } returns false
        coEvery { workoutRepository.add(1, capture(saved)) } returns AppResult.Success(Unit)

        // Act
        val result = AddToWorkoutUseCase(workoutRepository, authRepository, time)(exercise(id = 5), Goal.STRENGTH)

        // Assert
        assertEquals(AddToWorkoutResult.Added, result)
        assertEquals(PrescriptionRules.forGoal(Goal.STRENGTH), saved.captured.prescription)
        assertEquals(WorkoutStatus.PLANNED, saved.captured.status)
        assertEquals(TEST_NOW, saved.captured.addedAt)
    }

    @Test
    fun `an exercise cannot be added twice`() = runTest {
        coEvery { workoutRepository.isInWorkout(1, 5) } returns true

        val result = AddToWorkoutUseCase(workoutRepository, authRepository, time)(exercise(id = 5), Goal.STRENGTH)

        assertEquals(AddToWorkoutResult.AlreadyInWorkout, result)
        coVerify(exactly = 0) { workoutRepository.add(any(), any()) }
    }

    @Test
    fun `adding without session is refused`() = runTest {
        coEvery { authRepository.currentUser() } returns null

        val result = AddToWorkoutUseCase(workoutRepository, authRepository, time)(exercise(id = 5), Goal.STRENGTH)

        assertEquals(AddToWorkoutResult.NotLoggedIn, result)
    }

    @Test
    fun `database failure while adding is reported`() = runTest {
        coEvery { workoutRepository.isInWorkout(any(), any()) } returns false
        coEvery { workoutRepository.add(any(), any()) } returns AppResult.Failure(AppError.Database)

        val result = AddToWorkoutUseCase(workoutRepository, authRepository, time)(exercise(id = 5), Goal.STRENGTH)

        assertEquals(AddToWorkoutResult.Failure(AppError.Database), result)
    }

    // ---------- ObserveWorkoutUseCase (RF13) ----------

    @Test
    fun `workout is filtered by status`() = runTest {
        val planned = plannedExercise(id = 1, exerciseId = 1)
        val done = plannedExercise(id = 2, exerciseId = 2, status = WorkoutStatus.DONE, doneAt = TEST_NOW)
        every { workoutRepository.observeWorkout(1) } returns flowOf(listOf(planned, done))

        ObserveWorkoutUseCase(workoutRepository, authRepository)(WorkoutFilter.DONE).test {
            assertEquals(listOf(done), awaitItem())
            awaitComplete()
        }
    }

    // ---------- MarkExerciseDoneUseCase (RF14, RN13) ----------

    @Test
    fun `marking as done updates status and completion time`() = runTest {
        // Arrange
        val updated = slot<PlannedExercise>()
        coEvery { workoutRepository.find(1, 3) } returns plannedExercise(id = 3)
        coEvery { workoutRepository.update(1, capture(updated)) } returns AppResult.Success(Unit)

        // Act
        val outcome = MarkExerciseDoneUseCase(workoutRepository, authRepository, time)(3)

        // Assert
        assertEquals(MarkDoneOutcome.Done, outcome)
        assertEquals(WorkoutStatus.DONE, updated.captured.status)
        assertEquals(TEST_NOW, updated.captured.doneAt)
    }

    @Test
    fun `done exercise is not marked again`() = runTest {
        coEvery { workoutRepository.find(1, 3) } returns plannedExercise(id = 3, status = WorkoutStatus.DONE, doneAt = TEST_NOW)

        val outcome = MarkExerciseDoneUseCase(workoutRepository, authRepository, time)(3)

        assertEquals(MarkDoneOutcome.AlreadyDone, outcome)
        coVerify(exactly = 0) { workoutRepository.update(any(), any()) }
    }

    @Test
    fun `marking unknown item reports not found`() = runTest {
        coEvery { workoutRepository.find(1, 99) } returns null

        assertEquals(MarkDoneOutcome.NotFound, MarkExerciseDoneUseCase(workoutRepository, authRepository, time)(99))
    }

    // ---------- RateEffortUseCase (RF15, RN14) ----------

    @Test
    fun `effort is saved for done exercise`() = runTest {
        val updated = slot<PlannedExercise>()
        coEvery { workoutRepository.find(1, 3) } returns plannedExercise(id = 3, status = WorkoutStatus.DONE, doneAt = TEST_NOW)
        coEvery { workoutRepository.update(1, capture(updated)) } returns AppResult.Success(Unit)

        val outcome = RateEffortUseCase(workoutRepository, authRepository)(3, 4)

        assertEquals(RateEffortOutcome.Saved, outcome)
        assertEquals(4, updated.captured.effort)
    }

    @Test
    fun `effort for planned exercise is rejected`() = runTest {
        coEvery { workoutRepository.find(1, 3) } returns plannedExercise(id = 3)

        assertEquals(RateEffortOutcome.NotDone, RateEffortUseCase(workoutRepository, authRepository)(3, 4))
        coVerify(exactly = 0) { workoutRepository.update(any(), any()) }
    }

    @Test
    fun `effort out of range is rejected`() = runTest {
        coEvery { workoutRepository.find(1, 3) } returns plannedExercise(id = 3, status = WorkoutStatus.DONE, doneAt = TEST_NOW)

        assertEquals(RateEffortOutcome.OutOfRange, RateEffortUseCase(workoutRepository, authRepository)(3, 7))
    }

    // ---------- ObserveRestWarningUseCase (RF09, RN10) ----------

    @Test
    fun `rest warning appears when the group was trained yesterday`() = runTest {
        every { workoutRepository.observeDoneDates(1, MuscleGroup.LEGS) } returns flowOf(listOf(TEST_NOW.minus(Duration.ofDays(1))))

        ObserveRestWarningUseCase(workoutRepository, authRepository, time)(MuscleGroup.LEGS).test {
            assertTrue(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `no rest warning without session`() = runTest {
        every { authRepository.observeCurrentUser() } returns flowOf(null)

        ObserveRestWarningUseCase(workoutRepository, authRepository, time)(MuscleGroup.LEGS).test {
            assertFalse(awaitItem())
            awaitComplete()
        }
    }
}
