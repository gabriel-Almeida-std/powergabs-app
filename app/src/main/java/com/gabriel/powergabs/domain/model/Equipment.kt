package com.gabriel.powergabs.domain.model

/** Equipamento usado no exercício. IDs confirmados na API (docs/01-especificacao.md, seção 8.2). */
data class Equipment(val id: Int, val name: String) {
    companion object {
        const val BARBELL = 1
        const val DUMBBELL = 3
        const val BODYWEIGHT = 7
    }
}
