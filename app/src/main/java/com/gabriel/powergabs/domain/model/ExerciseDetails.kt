package com.gabriel.powergabs.domain.model

/** Dados da tela de detalhes (RF10–RF12). */
data class ExerciseDetails(
    val exercise: Exercise,
    val prescription: Prescription,
    val isInWorkout: Boolean,
)
