package com.gabriel.powergabs.domain.usecase

import com.gabriel.powergabs.core.network.NetworkMonitor
import com.gabriel.powergabs.core.result.AppError
import com.gabriel.powergabs.core.result.AppResult
import com.gabriel.powergabs.core.time.TimeProvider
import com.gabriel.powergabs.domain.model.ExerciseDetails
import com.gabriel.powergabs.domain.model.Goal
import com.gabriel.powergabs.domain.model.MuscleGroup
import com.gabriel.powergabs.domain.model.SuggestedExercise
import com.gabriel.powergabs.domain.model.SuggestedExercises
import com.gabriel.powergabs.domain.repository.AuthRepository
import com.gabriel.powergabs.domain.repository.ExerciseRepository
import com.gabriel.powergabs.domain.repository.WorkoutRepository
import com.gabriel.powergabs.domain.rule.CachePolicy
import com.gabriel.powergabs.domain.rule.ExerciseCatalogRules
import com.gabriel.powergabs.domain.rule.ExerciseSelectionRules
import com.gabriel.powergabs.domain.rule.PrescriptionRules
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

/**
 * Lista de exercícios sugeridos (RF07). Observa o cache dos grupos necessários
 * (o grupo escolhido e, no Condicionamento, Cardio) e aplica, nesta ordem:
 * seleção por objetivo (RN02–RN05) → preparo do catálogo (RN06–RN08) → prescrição (RN01).
 *
 * Como tudo é calculado sobre o cache, trocar de objetivo não faz nova requisição.
 */
class ObserveSuggestedExercisesUseCase(private val repository: ExerciseRepository) {

    operator fun invoke(goal: Goal, group: MuscleGroup): Flow<SuggestedExercises> {
        val groups = ExerciseSelectionRules.groupsFor(goal, group)
        val exerciseFlows = groups.map { repository.observeExercises(it) }
        val allExercises = combine(exerciseFlows) { lists -> lists.toList().flatten() }
        val prescription = PrescriptionRules.forGoal(goal)

        return combine(allExercises, repository.observeLastUpdated(group)) { exercises, lastUpdated ->
            val items = ExerciseCatalogRules.prepare(ExerciseSelectionRules.select(exercises, goal))
                .map { SuggestedExercise(it, prescription) }
            SuggestedExercises(items = items, lastUpdated = lastUpdated)
        }
    }
}

/**
 * Atualiza o cache dos grupos necessários (RF08, RN09, RNF01).
 *
 * - Sem `force`: só baixa os grupos com cache expirado (24 h).
 * - Com `force` (pull-to-refresh): baixa todos, ignorando a validade.
 * - Sem conexão: não tenta a rede e devolve [AppError.Network].
 */
class RefreshExercisesUseCase(
    private val repository: ExerciseRepository,
    private val networkMonitor: NetworkMonitor,
    private val timeProvider: TimeProvider,
) {

    suspend operator fun invoke(goal: Goal, group: MuscleGroup, force: Boolean): AppResult<Unit> {
        val now = timeProvider.now()
        val toRefresh = ExerciseSelectionRules.groupsFor(goal, group).filter {
            force || CachePolicy.isExpired(repository.lastUpdated(it), now)
        }
        if (toRefresh.isEmpty()) return AppResult.Success(Unit)
        if (!networkMonitor.isOnline.first()) return AppResult.Failure(AppError.Network)

        for (target in toRefresh) {
            val result = repository.refresh(target)
            if (result is AppResult.Failure) return result
        }
        return AppResult.Success(Unit)
    }
}

/** Detalhes do exercício com a prescrição do objetivo e se ele já está no treino (RF10–RF12). */
class ObserveExerciseDetailsUseCase(
    private val exerciseRepository: ExerciseRepository,
    private val workoutRepository: WorkoutRepository,
    private val authRepository: AuthRepository,
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(exerciseId: Int, goal: Goal): Flow<ExerciseDetails?> {
        val isInWorkout = authRepository.observeCurrentUser().flatMapLatest { user ->
            if (user == null) flowOf(false) else workoutRepository.observeIsInWorkout(user.id, exerciseId)
        }
        val prescription = PrescriptionRules.forGoal(goal)
        return combine(exerciseRepository.observeExercise(exerciseId), isInWorkout) { exercise, inWorkout ->
            exercise?.let { ExerciseDetails(it, prescription, inWorkout) }
        }
    }
}

/** Estado da conexão para a faixa "sem internet" (RF16). */
class ObserveConnectivityUseCase(private val networkMonitor: NetworkMonitor) {
    operator fun invoke(): Flow<Boolean> = networkMonitor.isOnline
}
