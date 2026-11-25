package com.gabriel.powergabs.domain.rule

import com.gabriel.powergabs.domain.model.Exercise
import com.gabriel.powergabs.domain.model.Goal
import com.gabriel.powergabs.domain.model.PlannedExercise
import com.gabriel.powergabs.domain.model.WorkoutFilter
import com.gabriel.powergabs.domain.model.WorkoutStatus
import java.time.Instant

/** Resultado de marcar um exercício como feito (RN13). */
sealed interface MarkDoneResult {
    data class Done(val item: PlannedExercise) : MarkDoneResult
    data object AlreadyDone : MarkDoneResult
}

/** Resultado de registrar o esforço percebido (RN14). */
sealed interface RateEffortResult {
    data class Rated(val item: PlannedExercise) : RateEffortResult
    data object NotDone : RateEffortResult
    data object OutOfRange : RateEffortResult
}

/** Regras do "Meu treino" (RN11–RN14). */
object WorkoutRules {

    val EFFORT_RANGE: IntRange = 1..5

    /** RN11: o exercício já está no treino? */
    fun isDuplicate(workout: List<PlannedExercise>, exerciseId: Int): Boolean =
        workout.any { it.exerciseId == exerciseId }

    /** RN12: cria o item com a prescrição do objetivo atual, congelada a partir daqui. */
    fun newPlannedExercise(exercise: Exercise, goal: Goal, now: Instant): PlannedExercise =
        PlannedExercise(
            id = 0,
            exerciseId = exercise.id,
            name = exercise.name,
            muscleGroup = exercise.muscleGroup,
            thumbnailUrl = exercise.thumbnailUrl ?: exercise.imageUrl,
            goal = goal,
            prescription = PrescriptionRules.forGoal(goal),
            status = WorkoutStatus.PLANNED,
            addedAt = now,
            doneAt = null,
            effort = null,
        )

    /** RN13: PLANNED → DONE, registrando quando. Não existe o caminho de volta. */
    fun markDone(item: PlannedExercise, now: Instant): MarkDoneResult =
        if (item.status == WorkoutStatus.DONE) {
            MarkDoneResult.AlreadyDone
        } else {
            MarkDoneResult.Done(item.copy(status = WorkoutStatus.DONE, doneAt = now))
        }

    /** RN14: esforço de 1 a 5, só para exercício feito. */
    fun rateEffort(item: PlannedExercise, effort: Int): RateEffortResult = when {
        item.status != WorkoutStatus.DONE -> RateEffortResult.NotDone
        effort !in EFFORT_RANGE -> RateEffortResult.OutOfRange
        else -> RateEffortResult.Rated(item.copy(effort = effort))
    }

    /** Filtro por status da tela "Meu treino" (RF13). */
    fun filter(items: List<PlannedExercise>, filter: WorkoutFilter): List<PlannedExercise> = when (filter) {
        WorkoutFilter.ALL -> items
        WorkoutFilter.PLANNED -> items.filter { it.status == WorkoutStatus.PLANNED }
        WorkoutFilter.DONE -> items.filter { it.status == WorkoutStatus.DONE }
    }
}
