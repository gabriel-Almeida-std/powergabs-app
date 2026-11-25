package com.gabriel.powergabs.testutil

import com.gabriel.powergabs.core.time.TimeProvider
import com.gabriel.powergabs.domain.model.Equipment
import com.gabriel.powergabs.domain.model.Exercise
import com.gabriel.powergabs.domain.model.Goal
import com.gabriel.powergabs.domain.model.Muscle
import com.gabriel.powergabs.domain.model.MuscleGroup
import com.gabriel.powergabs.domain.model.PlannedExercise
import com.gabriel.powergabs.domain.model.WorkoutStatus
import com.gabriel.powergabs.domain.rule.PrescriptionRules
import java.time.Instant
import java.time.ZoneId

/** Fuso fixo dos testes: o resultado não depende do fuso da máquina que roda os testes. */
val TEST_ZONE: ZoneId = ZoneId.of("America/Manaus")

/** "Agora" padrão dos testes: 24/11/2026 às 10:00 em Manaus. */
val TEST_NOW: Instant = Instant.parse("2026-11-24T14:00:00Z")

/** Relógio controlado pelo teste (RN17). */
class FixedTimeProvider(
    var instant: Instant = TEST_NOW,
    private val zoneId: ZoneId = TEST_ZONE,
) : TimeProvider {
    override fun now(): Instant = instant
    override fun zone(): ZoneId = zoneId
}

val BARBELL = Equipment(Equipment.BARBELL, "Barbell")
val DUMBBELL = Equipment(Equipment.DUMBBELL, "Dumbbell")
val BODYWEIGHT = Equipment(Equipment.BODYWEIGHT, "none (bodyweight exercise)")
val KETTLEBELL = Equipment(10, "Kettlebell")
val SZ_BAR = Equipment(2, "SZ-Bar")

fun exercise(
    id: Int = 1,
    name: String = "Exercise $id",
    group: MuscleGroup = MuscleGroup.CHEST,
    imageUrl: String? = null,
    videoUrl: String? = null,
    equipment: List<Equipment> = emptyList(),
) = Exercise(
    id = id,
    muscleGroup = group,
    name = name,
    description = "Description of $name",
    imageUrl = imageUrl,
    thumbnailUrl = imageUrl?.let { "$it.thumb" },
    videoUrl = videoUrl,
    muscles = listOf(Muscle(4, "Pectoralis major")),
    secondaryMuscles = emptyList(),
    equipment = equipment,
)

fun plannedExercise(
    id: Long = 1,
    exerciseId: Int = 10,
    group: MuscleGroup = MuscleGroup.CHEST,
    goal: Goal = Goal.HYPERTROPHY,
    status: WorkoutStatus = WorkoutStatus.PLANNED,
    doneAt: Instant? = null,
    effort: Int? = null,
) = PlannedExercise(
    id = id,
    exerciseId = exerciseId,
    name = "Exercise $exerciseId",
    muscleGroup = group,
    thumbnailUrl = null,
    goal = goal,
    prescription = PrescriptionRules.forGoal(goal),
    status = status,
    addedAt = TEST_NOW,
    doneAt = doneAt,
    effort = effort,
)
