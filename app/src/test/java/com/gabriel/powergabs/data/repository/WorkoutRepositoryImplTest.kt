package com.gabriel.powergabs.data.repository

import app.cash.turbine.test
import com.gabriel.powergabs.core.dispatcher.AppDispatchers
import com.gabriel.powergabs.core.result.AppError
import com.gabriel.powergabs.core.result.AppResult
import com.gabriel.powergabs.data.local.dao.WorkoutDao
import com.gabriel.powergabs.data.mapper.toEntity
import com.gabriel.powergabs.domain.model.MuscleGroup
import com.gabriel.powergabs.testutil.TEST_NOW
import com.gabriel.powergabs.testutil.plannedExercise
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutRepositoryImplTest {

    private val dao = mockk<WorkoutDao>(relaxUnitFun = true)

    private fun TestScope.repository(): WorkoutRepositoryImpl {
        val dispatcher = StandardTestDispatcher(testScheduler)
        return WorkoutRepositoryImpl(dao, AppDispatchers(io = dispatcher, default = dispatcher))
    }

    @Test
    fun `adding inserts the entity of the logged user`() = runTest {
        val item = plannedExercise(exerciseId = 7)
        coEvery { dao.insert(any()) } returns 1L

        val result = repository().add(userId = 3, item = item)

        assertEquals(AppResult.Success(Unit), result)
        coVerify(exactly = 1) { dao.insert(item.toEntity(3)) }
    }

    @Test
    fun `duplicate rejected by the unique index becomes a database error`() = runTest {
        coEvery { dao.insert(any()) } throws IllegalStateException("UNIQUE constraint failed")

        val result = repository().add(userId = 3, item = plannedExercise())

        assertEquals(AppResult.Failure(AppError.Database), result)
    }

    @Test
    fun `workout is observed as domain objects`() = runTest {
        val item = plannedExercise(id = 4, exerciseId = 7)
        every { dao.observeByUser(3) } returns flowOf(listOf(item.toEntity(3)))

        repository().observeWorkout(3).test {
            assertEquals(listOf(item), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `done dates of a group are converted to instants`() = runTest {
        every { dao.observeDoneDates(3, MuscleGroup.LEGS.categoryId) } returns flowOf(listOf(TEST_NOW.toEpochMilli()))

        repository().observeDoneDates(3, MuscleGroup.LEGS).test {
            assertEquals(listOf(TEST_NOW), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `update failure becomes a database error`() = runTest {
        coEvery { dao.update(any()) } throws IllegalStateException("io")

        assertEquals(AppResult.Failure(AppError.Database), repository().update(3, plannedExercise()))
    }
}
