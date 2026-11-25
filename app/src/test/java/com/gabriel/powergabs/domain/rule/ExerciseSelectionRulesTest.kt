package com.gabriel.powergabs.domain.rule

import com.gabriel.powergabs.domain.model.Goal
import com.gabriel.powergabs.domain.model.MuscleGroup
import com.gabriel.powergabs.testutil.BARBELL
import com.gabriel.powergabs.testutil.BODYWEIGHT
import com.gabriel.powergabs.testutil.DUMBBELL
import com.gabriel.powergabs.testutil.KETTLEBELL
import com.gabriel.powergabs.testutil.SZ_BAR
import com.gabriel.powergabs.testutil.exercise
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** RN02–RN05 — seleção de exercícios por objetivo. */
class ExerciseSelectionRulesTest {

    private val barbell = exercise(id = 1, equipment = listOf(BARBELL))
    private val dumbbell = exercise(id = 2, equipment = listOf(DUMBBELL))
    private val kettlebell = exercise(id = 3, equipment = listOf(KETTLEBELL))
    private val bodyweight = exercise(id = 4, equipment = listOf(BODYWEIGHT))
    private val noEquipment = exercise(id = 5, equipment = emptyList())
    private val szBar = exercise(id = 6, equipment = listOf(SZ_BAR))
    private val all = listOf(barbell, dumbbell, kettlebell, bodyweight, noEquipment, szBar)

    @Test
    fun `strength goal keeps only barbell and dumbbell exercises`() {
        // Act
        val selected = ExerciseSelectionRules.select(all, Goal.STRENGTH)

        // Assert
        assertEquals(listOf(barbell, dumbbell), selected)
    }

    @Test
    fun `strength goal accepts exercise that combines barbell with other equipment`() {
        val mixed = exercise(id = 7, equipment = listOf(KETTLEBELL, BARBELL))

        assertTrue(ExerciseSelectionRules.matches(mixed, Goal.STRENGTH))
    }

    @Test
    fun `hypertrophy goal keeps exercises with any equipment`() {
        val selected = ExerciseSelectionRules.select(all, Goal.HYPERTROPHY)

        assertEquals(all, selected)
    }

    @Test
    fun `endurance goal includes bodyweight exercises`() {
        val selected = ExerciseSelectionRules.select(all, Goal.ENDURANCE)

        assertTrue(bodyweight in selected)
        assertTrue(noEquipment in selected)
        assertEquals(all.size, selected.size)
    }

    @Test
    fun `conditioning goal needs the chosen group plus cardio`() {
        val groups = ExerciseSelectionRules.groupsFor(Goal.CONDITIONING, MuscleGroup.CHEST)

        assertEquals(listOf(MuscleGroup.CHEST, MuscleGroup.CARDIO), groups)
    }

    @Test
    fun `conditioning on cardio group does not repeat cardio`() {
        val groups = ExerciseSelectionRules.groupsFor(Goal.CONDITIONING, MuscleGroup.CARDIO)

        assertEquals(listOf(MuscleGroup.CARDIO), groups)
    }

    @Test
    fun `other goals need only the chosen group`() {
        listOf(Goal.HYPERTROPHY, Goal.STRENGTH, Goal.ENDURANCE).forEach { goal ->
            assertEquals(listOf(MuscleGroup.LEGS), ExerciseSelectionRules.groupsFor(goal, MuscleGroup.LEGS))
        }
    }

    @Test
    fun `exercise without equipment counts as bodyweight`() {
        assertTrue(ExerciseSelectionRules.isBodyweight(noEquipment))
        assertTrue(ExerciseSelectionRules.isBodyweight(bodyweight))
        assertFalse(ExerciseSelectionRules.isBodyweight(barbell))
    }
}
