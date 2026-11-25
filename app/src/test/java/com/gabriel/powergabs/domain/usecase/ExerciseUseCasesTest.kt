package com.gabriel.powergabs.domain.usecase

import app.cash.turbine.test
import com.gabriel.powergabs.core.network.NetworkMonitor
import com.gabriel.powergabs.core.result.AppError
import com.gabriel.powergabs.core.result.AppResult
import com.gabriel.powergabs.domain.model.Goal
import com.gabriel.powergabs.domain.model.MuscleGroup
import com.gabriel.powergabs.domain.model.User
import com.gabriel.powergabs.domain.repository.AuthRepository
import com.gabriel.powergabs.domain.repository.ExerciseRepository
import com.gabriel.powergabs.domain.repository.WorkoutRepository
import com.gabriel.powergabs.domain.rule.PrescriptionRules
import com.gabriel.powergabs.testutil.BARBELL
import com.gabriel.powergabs.testutil.FixedTimeProvider
import com.gabriel.powergabs.testutil.KETTLEBELL
import com.gabriel.powergabs.testutil.TEST_NOW
import com.gabriel.powergabs.testutil.exercise
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration

class ExerciseUseCasesTest {

    private val exerciseRepository = mockk<ExerciseRepository>()
    private val online = MutableStateFlow(true)
    private val networkMonitor = object : NetworkMonitor {
        override val isOnline = online
    }
    private val time = FixedTimeProvider()

    // ---------- ObserveSuggestedExercisesUseCase (RF07) ----------

    @Test
    fun `suggestions apply goal selection catalog rules and prescription`() = runTest {
        // Arrange
        val barbell = exercise(id = 1, name = "Supino", equipment = listOf(BARBELL))
        val kettlebell = exercise(id = 2, name = "Swing", equipment = listOf(KETTLEBELL))
        every { exerciseRepository.observeExercises(MuscleGroup.CHEST) } returns flowOf(listOf(kettlebell, barbell))
        every { exerciseRepository.observeLastUpdated(MuscleGroup.CHEST) } returns flowOf(TEST_NOW)

        // Act
        val useCase = ObserveSuggestedExercisesUseCase(exerciseRepository)

        // Assert
        useCase(Goal.STRENGTH, MuscleGroup.CHEST).test {
            val result = awaitItem()
            assertEquals(listOf(barbell), result.items.map { it.exercise })
            assertEquals(PrescriptionRules.forGoal(Goal.STRENGTH), result.items.single().prescription)
            assertEquals(TEST_NOW, result.lastUpdated)
            awaitComplete()
        }
    }

    @Test
    fun `conditioning suggestions include cardio exercises`() = runTest {
        // Arrange
        val chest = exercise(id = 1, name = "Flexão", group = MuscleGroup.CHEST)
        val cardio = exercise(id = 2, name = "Burpee", group = MuscleGroup.CARDIO)
        every { exerciseRepository.observeExercises(MuscleGroup.CHEST) } returns flowOf(listOf(chest))
        every { exerciseRepository.observeExercises(MuscleGroup.CARDIO) } returns flowOf(listOf(cardio))
        every { exerciseRepository.observeLastUpdated(MuscleGroup.CHEST) } returns flowOf(TEST_NOW)

        // Act + Assert
        ObserveSuggestedExercisesUseCase(exerciseRepository)(Goal.CONDITIONING, MuscleGroup.CHEST).test {
            val names = awaitItem().items.map { it.exercise.name }
            assertEquals(listOf("Burpee", "Flexão"), names)
            awaitComplete()
        }
    }

    // ---------- RefreshExercisesUseCase (RF08, RN09) ----------

    @Test
    fun `valid cache does not trigger a request`() = runTest {
        coEvery { exerciseRepository.lastUpdated(MuscleGroup.LEGS) } returns TEST_NOW.minus(Duration.ofHours(1))

        val result = RefreshExercisesUseCase(exerciseRepository, networkMonitor, time)(Goal.HYPERTROPHY, MuscleGroup.LEGS, force = false)

        assertEquals(AppResult.Success(Unit), result)
        coVerify(exactly = 0) { exerciseRepository.refresh(any()) }
    }

    @Test
    fun `expired cache is refreshed when online`() = runTest {
        coEvery { exerciseRepository.lastUpdated(MuscleGroup.LEGS) } returns TEST_NOW.minus(Duration.ofHours(25))
        coEvery { exerciseRepository.refresh(MuscleGroup.LEGS) } returns AppResult.Success(Unit)

        val result = RefreshExercisesUseCase(exerciseRepository, networkMonitor, time)(Goal.HYPERTROPHY, MuscleGroup.LEGS, force = false)

        assertEquals(AppResult.Success(Unit), result)
        coVerify(exactly = 1) { exerciseRepository.refresh(MuscleGroup.LEGS) }
    }

    @Test
    fun `forced refresh ignores the 24 hour validity`() = runTest {
        coEvery { exerciseRepository.lastUpdated(any()) } returns TEST_NOW
        coEvery { exerciseRepository.refresh(any()) } returns AppResult.Success(Unit)

        RefreshExercisesUseCase(exerciseRepository, networkMonitor, time)(Goal.CONDITIONING, MuscleGroup.CHEST, force = true)

        coVerify(exactly = 1) { exerciseRepository.refresh(MuscleGroup.CHEST) }
        coVerify(exactly = 1) { exerciseRepository.refresh(MuscleGroup.CARDIO) }
    }

    @Test
    fun `refresh without connection returns network error without calling the api`() = runTest {
        online.value = false
        coEvery { exerciseRepository.lastUpdated(any()) } returns null

        val result = RefreshExercisesUseCase(exerciseRepository, networkMonitor, time)(Goal.HYPERTROPHY, MuscleGroup.LEGS, force = true)

        assertEquals(AppResult.Failure(AppError.Network), result)
        coVerify(exactly = 0) { exerciseRepository.refresh(any()) }
    }

    @Test
    fun `refresh failure is returned`() = runTest {
        coEvery { exerciseRepository.lastUpdated(any()) } returns null
        coEvery { exerciseRepository.refresh(any()) } returns AppResult.Failure(AppError.Http(500))

        val result = RefreshExercisesUseCase(exerciseRepository, networkMonitor, time)(Goal.HYPERTROPHY, MuscleGroup.LEGS, force = false)

        assertEquals(AppResult.Failure(AppError.Http(500)), result)
    }

    // ---------- ObserveExerciseDetailsUseCase (RF10) ----------

    @Test
    fun `details combine exercise prescription and workout membership`() = runTest {
        // Arrange
        val workoutRepository = mockk<WorkoutRepository>()
        val authRepository = mockk<AuthRepository>()
        val supino = exercise(id = 7, name = "Supino")
        every { exerciseRepository.observeExercise(7) } returns flowOf(supino)
        every { authRepository.observeCurrentUser() } returns flowOf(User(1, "g@mail.com"))
        every { workoutRepository.observeIsInWorkout(1, 7) } returns flowOf(true)

        // Act + Assert
        ObserveExerciseDetailsUseCase(exerciseRepository, workoutRepository, authRepository)(7, Goal.ENDURANCE).test {
            val details = awaitItem()!!
            assertEquals(supino, details.exercise)
            assertEquals(PrescriptionRules.forGoal(Goal.ENDURANCE), details.prescription)
            assertTrue(details.isInWorkout)
            awaitComplete()
        }
    }

    @Test
    fun `details are null when the exercise is not cached`() = runTest {
        val workoutRepository = mockk<WorkoutRepository>()
        val authRepository = mockk<AuthRepository>()
        every { exerciseRepository.observeExercise(99) } returns flowOf(null)
        every { authRepository.observeCurrentUser() } returns flowOf(null)

        ObserveExerciseDetailsUseCase(exerciseRepository, workoutRepository, authRepository)(99, Goal.ENDURANCE).test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }
}
