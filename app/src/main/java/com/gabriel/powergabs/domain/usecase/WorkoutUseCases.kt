package com.gabriel.powergabs.domain.usecase

import com.gabriel.powergabs.core.result.AppError
import com.gabriel.powergabs.core.result.AppResult
import com.gabriel.powergabs.core.time.TimeProvider
import com.gabriel.powergabs.domain.model.Exercise
import com.gabriel.powergabs.domain.model.Goal
import com.gabriel.powergabs.domain.model.MuscleGroup
import com.gabriel.powergabs.domain.model.PlannedExercise
import com.gabriel.powergabs.domain.model.WorkoutFilter
import com.gabriel.powergabs.domain.repository.AuthRepository
import com.gabriel.powergabs.domain.repository.WorkoutRepository
import com.gabriel.powergabs.domain.rule.MarkDoneResult
import com.gabriel.powergabs.domain.rule.RateEffortResult
import com.gabriel.powergabs.domain.rule.RestWarningRules
import com.gabriel.powergabs.domain.rule.WorkoutRules
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

sealed interface AddToWorkoutResult {
    data object Added : AddToWorkoutResult
    data object AlreadyInWorkout : AddToWorkoutResult
    data object NotLoggedIn : AddToWorkoutResult
    data class Failure(val error: AppError) : AddToWorkoutResult
}

sealed interface MarkDoneOutcome {
    data object Done : MarkDoneOutcome
    data object AlreadyDone : MarkDoneOutcome
    data object NotFound : MarkDoneOutcome
    data class Failure(val error: AppError) : MarkDoneOutcome
}

sealed interface RateEffortOutcome {
    data object Saved : RateEffortOutcome
    data object NotDone : RateEffortOutcome
    data object OutOfRange : RateEffortOutcome
    data object NotFound : RateEffortOutcome
    data class Failure(val error: AppError) : RateEffortOutcome
}

/** Adiciona ao "Meu treino" com a prescrição do objetivo atual (RF12, RN11, RN12). */
class AddToWorkoutUseCase(
    private val workoutRepository: WorkoutRepository,
    private val authRepository: AuthRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(exercise: Exercise, goal: Goal): AddToWorkoutResult {
        val user = authRepository.currentUser() ?: return AddToWorkoutResult.NotLoggedIn
        if (workoutRepository.isInWorkout(user.id, exercise.id)) return AddToWorkoutResult.AlreadyInWorkout

        val item = WorkoutRules.newPlannedExercise(exercise, goal, timeProvider.now())
        return when (val result = workoutRepository.add(user.id, item)) {
            is AppResult.Success -> AddToWorkoutResult.Added
            is AppResult.Failure -> AddToWorkoutResult.Failure(result.error)
        }
    }
}

/** "Meu treino" do usuário logado, filtrado por status (RF13). */
class ObserveWorkoutUseCase(
    private val workoutRepository: WorkoutRepository,
    private val authRepository: AuthRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(filter: WorkoutFilter): Flow<List<PlannedExercise>> =
        authRepository.observeCurrentUser().flatMapLatest { user ->
            if (user == null) {
                flowOf(emptyList())
            } else {
                workoutRepository.observeWorkout(user.id).map { WorkoutRules.filter(it, filter) }
            }
        }
}

/** Marca como feito (RF14, RN13). */
class MarkExerciseDoneUseCase(
    private val workoutRepository: WorkoutRepository,
    private val authRepository: AuthRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(itemId: Long): MarkDoneOutcome {
        val user = authRepository.currentUser() ?: return MarkDoneOutcome.NotFound
        val item = workoutRepository.find(user.id, itemId) ?: return MarkDoneOutcome.NotFound
        return when (val rule = WorkoutRules.markDone(item, timeProvider.now())) {
            MarkDoneResult.AlreadyDone -> MarkDoneOutcome.AlreadyDone
            is MarkDoneResult.Done -> when (val saved = workoutRepository.update(user.id, rule.item)) {
                is AppResult.Success -> MarkDoneOutcome.Done
                is AppResult.Failure -> MarkDoneOutcome.Failure(saved.error)
            }
        }
    }
}

/** Registra o esforço percebido (RF15, RN14). */
class RateEffortUseCase(
    private val workoutRepository: WorkoutRepository,
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(itemId: Long, effort: Int): RateEffortOutcome {
        val user = authRepository.currentUser() ?: return RateEffortOutcome.NotFound
        val item = workoutRepository.find(user.id, itemId) ?: return RateEffortOutcome.NotFound
        return when (val rule = WorkoutRules.rateEffort(item, effort)) {
            RateEffortResult.NotDone -> RateEffortOutcome.NotDone
            RateEffortResult.OutOfRange -> RateEffortOutcome.OutOfRange
            is RateEffortResult.Rated -> when (val saved = workoutRepository.update(user.id, rule.item)) {
                is AppResult.Success -> RateEffortOutcome.Saved
                is AppResult.Failure -> RateEffortOutcome.Failure(saved.error)
            }
        }
    }
}

/** Aviso de descanso para um grupo (RF09, RN10). */
class ObserveRestWarningUseCase(
    private val workoutRepository: WorkoutRepository,
    private val authRepository: AuthRepository,
    private val timeProvider: TimeProvider,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(group: MuscleGroup): Flow<Boolean> =
        authRepository.observeCurrentUser().flatMapLatest { user ->
            if (user == null) {
                flowOf(false)
            } else {
                workoutRepository.observeDoneDates(user.id, group).map { dates ->
                    RestWarningRules.shouldWarn(dates, timeProvider.now(), timeProvider.zone())
                }
            }
        }
}
