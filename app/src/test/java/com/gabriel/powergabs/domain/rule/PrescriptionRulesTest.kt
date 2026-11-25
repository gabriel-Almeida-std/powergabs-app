package com.gabriel.powergabs.domain.rule

import com.gabriel.powergabs.domain.model.Goal
import com.gabriel.powergabs.domain.model.Prescription
import org.junit.Assert.assertEquals
import org.junit.Test

/** RN01 — prescrição por objetivo. */
class PrescriptionRulesTest {

    @Test
    fun `hypertrophy prescribes 3-4 sets of 8-12 reps with 60-90 s rest`() {
        // Act
        val prescription = PrescriptionRules.forGoal(Goal.HYPERTROPHY)

        // Assert
        assertEquals(Prescription(3..4, 8..12, 60..90), prescription)
    }

    @Test
    fun `strength prescribes 3-5 sets of 3-6 reps with 120-180 s rest`() {
        val prescription = PrescriptionRules.forGoal(Goal.STRENGTH)

        assertEquals(Prescription(3..5, 3..6, 120..180), prescription)
    }

    @Test
    fun `endurance prescribes 2-3 sets of 15-20 reps with 30-60 s rest`() {
        val prescription = PrescriptionRules.forGoal(Goal.ENDURANCE)

        assertEquals(Prescription(2..3, 15..20, 30..60), prescription)
    }

    @Test
    fun `conditioning prescribes 2-3 sets of 12-15 reps with fixed 30 s rest`() {
        val prescription = PrescriptionRules.forGoal(Goal.CONDITIONING)

        assertEquals(Prescription(2..3, 12..15, 30..30), prescription)
    }
}
