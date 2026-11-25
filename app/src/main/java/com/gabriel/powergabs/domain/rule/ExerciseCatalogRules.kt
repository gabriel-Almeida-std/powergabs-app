package com.gabriel.powergabs.domain.rule

import com.gabriel.powergabs.domain.model.Exercise
import com.gabriel.powergabs.domain.model.LocalizedText
import java.text.Normalizer

/** Regras do catálogo de exercícios: idioma (RN06), ordenação (RN07) e duplicados (RN08). */
object ExerciseCatalogRules {

    private val DIACRITICS = Regex("\\p{Mn}+")
    private val WHITESPACE = Regex("\\s+")

    /**
     * Escolhe o texto a exibir (RN06): português se houver nome em português;
     * senão, inglês; senão, `null` (o exercício deve ser descartado).
     */
    fun pickTranslation(texts: List<LocalizedText>): LocalizedText? =
        texts.firstOrNull { it.languageId == LocalizedText.PORTUGUESE && hasValidName(it.name) }
            ?: texts.firstOrNull { it.languageId == LocalizedText.ENGLISH && hasValidName(it.name) }

    fun hasValidName(name: String?): Boolean = !name.isNullOrBlank()

    /** Chave de comparação de nomes: minúsculas, sem acentos e com espaços normalizados (RN08). */
    fun normalizeName(name: String): String =
        Normalizer.normalize(name, Normalizer.Form.NFD)
            .replace(DIACRITICS, "")
            .lowercase()
            .replace(WHITESPACE, " ")
            .trim()

    /**
     * Prepara a lista para exibição:
     * 1. descarta exercícios sem nome (RN06);
     * 2. ordena com imagem primeiro e, dentro de cada bloco, por nome (RN07);
     * 3. remove duplicados por ID e por nome normalizado, mantendo o primeiro (RN08).
     */
    fun prepare(exercises: List<Exercise>): List<Exercise> =
        exercises
            .filter { hasValidName(it.name) }
            .sortedWith(compareByDescending<Exercise> { it.hasImage }.thenBy { normalizeName(it.name) })
            .distinctBy { it.id }
            .distinctBy { normalizeName(it.name) }
}
