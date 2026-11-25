package com.gabriel.powergabs.domain.rule

import com.gabriel.powergabs.domain.model.Goal
import com.gabriel.powergabs.domain.model.WorkoutFilter
import com.gabriel.powergabs.domain.model.WorkoutStatus
import com.gabriel.powergabs.testutil.TEST_NOW
import com.gabriel.powergabs.testutil.exercise
import com.gabriel.powergabs.testutil.plannedExercise
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** RN11–RN14 — regras do "Meu treino". */
class WorkoutRulesTest {

    @Test
    fun `exercise already in the workout is a duplicate`() {
        val workout = listOf(plannedExercise(exerciseId = 10))

        assertTrue(WorkoutRules.isDuplicate(workout, exerciseId = 10))
        assertFalse(WorkoutRules.isDuplicate(workout, exerciseId = 11))
    }

    @Test
    fun `new planned exercise saves the prescription of the current goal`() {
        // Arrange
        val exercise = exercise(id = 5, imageUrl = "https://img/5.png")

        // Act
        val planned = WorkoutRules.newPlannedExercise(exercise, Goal.STRENGTH, TEST_NOW)

        // Assert
        assertEquals(PrescriptionRules.forGoal(Goal.STRENGTH), planned.prescription)
        assertEquals(Goal.STRENGTH, planned.goal)
        assertEquals(WorkoutStatus.PLANNED, planned.status)
        assertEquals(5, planned.exerciseId)
        assertEquals(TEST_NOW, planned.addedAt)
        assertNull(planned.doneAt)
        assertNull(planned.effort)
    }

    @Test
    fun `marking a planned exercise as done records the completion time`() {
        val planned = plannedExercise(status = WorkoutStatus.PLANNED)

        val result = WorkoutRules.markDone(planned, TEST_NOW)

        val done = (result as MarkDoneResult.Done).item
        assertEquals(WorkoutStatus.DONE, done.status)
        assertEquals(TEST_NOW, done.doneAt)
    }

    @Test
    fun `done exercise cannot be marked done again`() {
        val done = plannedExercise(status = WorkoutStatus.DONE, doneAt = TEST_NOW)

        assertEquals(MarkDoneResult.AlreadyDone, WorkoutRules.markDone(done, TEST_NOW.plusSeconds(60)))
    }

    @Test
    fun `effort from 1 to 5 is accepted for done exercise`() {
        val done = plannedExercise(status = WorkoutStatus.DONE, doneAt = TEST_NOW)

        (1..5).forEach { effort ->
            val result = WorkoutRules.rateEffort(done, effort)
            assertEquals(effort, (result as RateEffortResult.Rated).item.effort)
        }
    }

    @Test
    fun `effort outside 1 to 5 is rejected`() {
        val done = plannedExercise(status = WorkoutStatus.DONE, doneAt = TEST_NOW)

        assertEquals(RateEffortResult.OutOfRange, WorkoutRules.rateEffort(done, 0))
        assertEquals(RateEffortResult.OutOfRange, WorkoutRules.rateEffort(done, 6))
    }

    @Test
    fun `effort is rejected for planned exercise`() {
        val planned = plannedExercise(status = WorkoutStatus.PLANNED)

        assertEquals(RateEffortResult.NotDone, WorkoutRules.rateEffort(planned, 3))
    }

    @Test
    fun `filter keeps items by status`() {
        // Arrange
        val planned = plannedExercise(id = 1, status = WorkoutStatus.PLANNED)
        val done = plannedExercise(id = 2, exerciseId = 11, status = WorkoutStatus.DONE, doneAt = TEST_NOW)
        val items = listOf(planned, done)

        // Act + Assert
        assertEquals(items, WorkoutRules.filter(items, WorkoutFilter.ALL))
        assertEquals(listOf(planned), WorkoutRules.filter(items, WorkoutFilter.PLANNED))
        assertEquals(listOf(done), WorkoutRules.filter(items, WorkoutFilter.DONE))
    }
}
