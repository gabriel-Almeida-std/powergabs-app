package com.gabriel.powergabs.domain.rule

import com.gabriel.powergabs.domain.model.Equipment
import com.gabriel.powergabs.domain.model.Exercise
import com.gabriel.powergabs.domain.model.Goal
import com.gabriel.powergabs.domain.model.MuscleGroup

/** Regras de seleção de exercícios por objetivo (RN02–RN05). */
object ExerciseSelectionRules {

    /** Equipamentos aceitos no objetivo Força (RN03): Barra e Halteres. */
    val STRENGTH_EQUIPMENT: Set<Int> = setOf(Equipment.BARBELL, Equipment.DUMBBELL)

    /**
     * Grupos cujo cache é necessário para montar a lista.
     * Condicionamento acrescenta a categoria Cardio (RN05); os demais usam só o grupo escolhido.
     */
    fun groupsFor(goal: Goal, group: MuscleGroup): List<MuscleGroup> =
        if (goal == Goal.CONDITIONING) listOf(group, MuscleGroup.CARDIO).distinct() else listOf(group)

    /**
     * Diz se o exercício serve para o objetivo.
     * - Hipertrofia (RN02): qualquer equipamento.
     * - Força (RN03): precisa usar Barra ou Halteres.
     * - Resistência (RN04): qualquer equipamento, incluindo peso corporal.
     * - Condicionamento (RN05): qualquer exercício do grupo ou de Cardio.
     */
    fun matches(exercise: Exercise, goal: Goal): Boolean = when (goal) {
        Goal.STRENGTH -> exercise.equipment.any { it.id in STRENGTH_EQUIPMENT }
        Goal.HYPERTROPHY, Goal.ENDURANCE, Goal.CONDITIONING -> true
    }

    /** Exercício de peso corporal: equipamento "none" (ID 7) ou nenhum equipamento informado. */
    fun isBodyweight(exercise: Exercise): Boolean =
        exercise.equipment.isEmpty() || exercise.equipment.any { it.id == Equipment.BODYWEIGHT }

    fun select(exercises: List<Exercise>, goal: Goal): List<Exercise> = exercises.filter { matches(it, goal) }
}
