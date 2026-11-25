package com.gabriel.powergabs.domain.model

/** Nome e descrição de um exercício em um idioma da wger (RN06). */
data class LocalizedText(
    val languageId: Int,
    val name: String,
    val description: String,
) {
    companion object {
        /** IDs confirmados em `language/` (docs/01-especificacao.md, seção 8.3). */
        const val PORTUGUESE = 7
        const val ENGLISH = 2
    }
}
