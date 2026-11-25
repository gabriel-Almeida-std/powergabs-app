package com.gabriel.powergabs.data.mapper

import com.gabriel.powergabs.data.local.entity.UserEntity
import com.gabriel.powergabs.data.local.entity.WorkoutItemEntity
import com.gabriel.powergabs.domain.model.Goal
import com.gabriel.powergabs.domain.model.MuscleGroup
import com.gabriel.powergabs.domain.model.PlannedExercise
import com.gabriel.powergabs.domain.model.Prescription
import com.gabriel.powergabs.domain.model.User
import com.gabriel.powergabs.domain.model.WorkoutStatus
import java.time.Instant

/** Domain → entidade. Enums são gravados pelo nome (texto), que é estável e legível no banco. */
fun PlannedExercise.toEntity(userId: Long) = WorkoutItemEntity(
    id = id,
    userId = userId,
    exerciseId = exerciseId,
    name = name,
    categoryId = muscleGroup.categoryId,
    thumbnailUrl = thumbnailUrl,
    goal = goal.name,
    setsMin = prescription.sets.first,
    setsMax = prescription.sets.last,
    repsMin = prescription.reps.first,
    repsMax = prescription.reps.last,
    restMin = prescription.restSeconds.first,
    restMax = prescription.restSeconds.last,
    status = status.name,
    addedAt = addedAt.toEpochMilli(),
    doneAt = doneAt?.toEpochMilli(),
    effort = effort,
)

/** Entidade → domain. Devolve `null` se algum valor gravado não for reconhecido (dado corrompido). */
fun WorkoutItemEntity.toDomain(): PlannedExercise? {
    val group = MuscleGroup.fromCategoryId(categoryId) ?: return null
    val goal = Goal.entries.firstOrNull { it.name == goal } ?: return null
    val status = WorkoutStatus.entries.firstOrNull { it.name == status } ?: return null
    return PlannedExercise(
        id = id,
        exerciseId = exerciseId,
        name = name,
        muscleGroup = group,
        thumbnailUrl = thumbnailUrl,
        goal = goal,
        prescription = Prescription(setsMin..setsMax, repsMin..repsMax, restMin..restMax),
        status = status,
        addedAt = Instant.ofEpochMilli(addedAt),
        doneAt = doneAt?.let(Instant::ofEpochMilli),
        effort = effort,
    )
}

fun UserEntity.toDomain() = User(id = id, email = email)
