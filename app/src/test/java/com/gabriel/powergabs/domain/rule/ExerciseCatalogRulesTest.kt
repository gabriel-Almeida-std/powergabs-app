package com.gabriel.powergabs.domain.rule

import com.gabriel.powergabs.domain.model.LocalizedText
import com.gabriel.powergabs.testutil.exercise
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** RN06–RN08 — idioma, ordenação e duplicados. */
class ExerciseCatalogRulesTest {

    private val pt = LocalizedText(LocalizedText.PORTUGUESE, "Supino reto", "Descrição")
    private val en = LocalizedText(LocalizedText.ENGLISH, "Bench press", "Description")
    private val de = LocalizedText(1, "Bankdrücken", "Beschreibung")

    @Test
    fun `portuguese translation is preferred over english`() {
        val picked = ExerciseCatalogRules.pickTranslation(listOf(de, en, pt))

        assertEquals(pt, picked)
    }

    @Test
    fun `english is used when there is no portuguese translation`() {
        val picked = ExerciseCatalogRules.pickTranslation(listOf(de, en))

        assertEquals(en, picked)
    }

    @Test
    fun `blank portuguese name falls back to english`() {
        val blankPt = pt.copy(name = "   ")

        val picked = ExerciseCatalogRules.pickTranslation(listOf(blankPt, en))

        assertEquals(en, picked)
    }

    @Test
    fun `exercise without portuguese or english name has no translation`() {
        val picked = ExerciseCatalogRules.pickTranslation(listOf(de))

        assertNull(picked)
    }

    @Test
    fun `prepare discards exercises without name`() {
        val valid = exercise(id = 1, name = "Supino")
        val blank = exercise(id = 2, name = " ")

        val prepared = ExerciseCatalogRules.prepare(listOf(blank, valid))

        assertEquals(listOf(valid), prepared)
    }

    @Test
    fun `exercises with image come before exercises without image`() {
        // Arrange
        val noImageA = exercise(id = 1, name = "A sem imagem")
        val withImageZ = exercise(id = 2, name = "Z com imagem", imageUrl = "https://img/z.png")
        val withImageB = exercise(id = 3, name = "B com imagem", imageUrl = "https://img/b.png")

        // Act
        val prepared = ExerciseCatalogRules.prepare(listOf(noImageA, withImageZ, withImageB))

        // Assert
        assertEquals(listOf(withImageB, withImageZ, noImageA), prepared)
    }

    @Test
    fun `duplicated ids appear only once`() {
        val first = exercise(id = 1, name = "Supino")
        val sameId = exercise(id = 1, name = "Supino")

        val prepared = ExerciseCatalogRules.prepare(listOf(first, sameId))

        assertEquals(1, prepared.size)
    }

    @Test
    fun `names that differ only by case accents and spaces are duplicates and the one with image wins`() {
        // Arrange
        val withoutImage = exercise(id = 1, name = "Flexão  de braço")
        val withImage = exercise(id = 2, name = "flexao de BRACO", imageUrl = "https://img/f.png")

        // Act
        val prepared = ExerciseCatalogRules.prepare(listOf(withoutImage, withImage))

        // Assert
        assertEquals(listOf(withImage), prepared)
    }

    @Test
    fun `normalize name removes accents lowercases and collapses spaces`() {
        assertEquals("elevacao lateral", ExerciseCatalogRules.normalizeName("  Elevação   LATERAL "))
    }
}
