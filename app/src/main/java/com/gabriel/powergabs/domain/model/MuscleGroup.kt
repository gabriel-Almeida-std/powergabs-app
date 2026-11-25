package com.gabriel.powergabs.domain.model

/**
 * Grupo muscular do dia (RF06). Cada grupo corresponde a uma categoria da wger;
 * os IDs foram confirmados na API real (docs/01-especificacao.md, seção 8.1).
 */
enum class MuscleGroup(val categoryId: Int) {
    ABS(10),
    ARMS(8),
    BACK(12),
    CALVES(14),
    CARDIO(15),
    CHEST(11),
    LEGS(9),
    SHOULDERS(13),
    ;

    companion object {
        /** Devolve o grupo da categoria informada, ou `null` se a categoria não for conhecida. */
        fun fromCategoryId(categoryId: Int): MuscleGroup? = entries.firstOrNull { it.categoryId == categoryId }
    }
}
