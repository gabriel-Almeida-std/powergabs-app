package com.gabriel.powergabs.domain.model

/**
 * Exercício já com o idioma resolvido (português ou inglês, RN06).
 * Traz todos os dados dos detalhes (RF10), porque o `exerciseinfo` da wger já entrega tudo
 * de uma vez — por isso os detalhes funcionam offline.
 */
data class Exercise(
    val id: Int,
    val muscleGroup: MuscleGroup,
    val name: String,
    val description: String,
    val imageUrl: String?,
    val thumbnailUrl: String?,
    val videoUrl: String?,
    val muscles: List<Muscle>,
    val secondaryMuscles: List<Muscle>,
    val equipment: List<Equipment>,
) {
    val hasImage: Boolean get() = imageUrl != null
    val hasVideo: Boolean get() = videoUrl != null
}
