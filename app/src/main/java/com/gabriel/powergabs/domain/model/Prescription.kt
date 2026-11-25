package com.gabriel.powergabs.domain.model

/**
 * Prescrição de referência de um objetivo (RN01): faixas de séries, repetições e descanso.
 * `IntRange` deixa explícito que são intervalos (ex.: 8..12 repetições), não valores únicos.
 */
data class Prescription(
    val sets: IntRange,
    val reps: IntRange,
    val restSeconds: IntRange,
)
