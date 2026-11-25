package com.gabriel.powergabs.domain.model

import java.time.Instant

/** Status de um exercício do "Meu treino" (RN13): só avança de PLANNED para DONE. */
enum class WorkoutStatus { PLANNED, DONE }

/** Filtro da tela "Meu treino" (RF13). */
enum class WorkoutFilter { ALL, PLANNED, DONE }

/**
 * Exercício salvo no "Meu treino".
 * Guarda uma cópia do nome, do grupo e da miniatura, e a prescrição do objetivo no momento
 * em que foi adicionado (RN12), para não depender do cache de exercícios.
 */
data class PlannedExercise(
    val id: Long,
    val exerciseId: Int,
    val name: String,
    val muscleGroup: MuscleGroup,
    val thumbnailUrl: String?,
    val goal: Goal,
    val prescription: Prescription,
    val status: WorkoutStatus,
    val addedAt: Instant,
    val doneAt: Instant?,
    val effort: Int?,
)
