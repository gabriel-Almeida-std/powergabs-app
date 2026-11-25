package com.gabriel.powergabs.domain.rule

import com.gabriel.powergabs.domain.model.Goal
import com.gabriel.powergabs.domain.model.Prescription

/**
 * Prescrição por objetivo (RN01). Faixas gerais de referência, exibidas como sugestão;
 * o app não faz prescrição individual.
 */
object PrescriptionRules {

    fun forGoal(goal: Goal): Prescription = when (goal) {
        Goal.HYPERTROPHY -> Prescription(sets = 3..4, reps = 8..12, restSeconds = 60..90)
        Goal.STRENGTH -> Prescription(sets = 3..5, reps = 3..6, restSeconds = 120..180)
        Goal.ENDURANCE -> Prescription(sets = 2..3, reps = 15..20, restSeconds = 30..60)
        Goal.CONDITIONING -> Prescription(sets = 2..3, reps = 12..15, restSeconds = 30..30)
    }
}
