package com.gabriel.powergabs.domain.model

import java.time.Instant

/** Exercício sugerido com a prescrição do objetivo aplicada (RF07). */
data class SuggestedExercise(
    val exercise: Exercise,
    val prescription: Prescription,
)

/**
 * Resultado da lista de sugestões: os itens e a data da última atualização do grupo
 * escolhido (`null` = o grupo nunca foi baixado), usada no "atualizado há…" (RF08).
 */
data class SuggestedExercises(
    val items: List<SuggestedExercise>,
    val lastUpdated: Instant?,
)
