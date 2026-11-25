package com.gabriel.powergabs.data.repository

import app.cash.turbine.test
import com.gabriel.powergabs.core.dispatcher.AppDispatchers
import com.gabriel.powergabs.core.result.AppError
import com.gabriel.powergabs.core.result.AppResult
import com.gabriel.powergabs.data.local.dao.ExerciseDao
import com.gabriel.powergabs.data.local.entity.ExerciseEntity
import com.gabriel.powergabs.data.remote.WgerApi
import com.gabriel.powergabs.data.remote.dto.CategoryDto
import com.gabriel.powergabs.data.remote.dto.ExerciseInfoDto
import com.gabriel.powergabs.data.remote.dto.PageDto
import com.gabriel.powergabs.data.remote.dto.TranslationDto
import com.gabriel.powergabs.domain.model.MuscleGroup
import com.gabriel.powergabs.testutil.FixedTimeProvider
import com.gabriel.powergabs.testutil.TEST_NOW
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/** RNF01, RNF06 — cache Offline First e conversão de erros. API e DAO falsos com MockK. */
class ExerciseRepositoryImplTest {

    private val api = mockk<WgerApi>()
    private val dao = mockk<ExerciseDao>(relaxUnitFun = true)
    private val time = FixedTimeProvider()

    private fun TestScope.repository(): ExerciseRepositoryImpl {
        val dispatcher = StandardTestDispatcher(testScheduler)
        return ExerciseRepositoryImpl(api, dao, time, AppDispatchers(io = dispatcher, default = dispatcher))
    }

    private fun dto(id: Int, name: String = "Exercise $id") = ExerciseInfoDto(
        id = id,
        category = CategoryDto(MuscleGroup.LEGS.categoryId),
        translations = listOf(TranslationDto(language = 2, name = name)),
    )

    private fun page(vararg items: ExerciseInfoDto, next: String? = null) =
        PageDto(count = items.size, next = next, results = items.toList())

    @Test
    fun `refresh downloads every page and replaces the group cache in one transaction`() = runTest {
        // Arrange
        coEvery { api.getExercises(9, 100, 0) } returns page(dto(1), dto(2), next = "page2")
        coEvery { api.getExercises(9, 100, 100) } returns page(dto(3))
        val saved = slot<List<ExerciseEntity>>()
        coEvery { dao.replaceCategory(9, capture(saved), any()) } returns Unit

        // Act
        val result = repository().refresh(MuscleGroup.LEGS)

        // Assert
        assertEquals(AppResult.Success(Unit), result)
        assertEquals(listOf(1, 2, 3), saved.captured.map { it.id })
        coVerify(exactly = 1) { dao.replaceCategory(9, any(), TEST_NOW.toEpochMilli()) }
    }

    @Test
    fun `exercises without name are not cached and duplicated ids are saved once`() = runTest {
        coEvery { api.getExercises(9, 100, 0) } returns page(dto(1), dto(1), dto(2, name = " "))
        val saved = slot<List<ExerciseEntity>>()
        coEvery { dao.replaceCategory(9, capture(saved), any()) } returns Unit

        repository().refresh(MuscleGroup.LEGS)

        assertEquals(listOf(1), saved.captured.map { it.id })
    }

    @Test
    fun `failure on a later page keeps the old cache untouched`() = runTest {
        coEvery { api.getExercises(9, 100, 0) } returns page(dto(1), next = "page2")
        coEvery { api.getExercises(9, 100, 100) } throws IOException("timeout")

        val result = repository().refresh(MuscleGroup.LEGS)

        assertEquals(AppResult.Failure(AppError.Network), result)
        coVerify(exactly = 0) { dao.replaceCategory(any(), any(), any()) }
    }

    @Test
    fun `http error is converted to AppError Http with its code`() = runTest {
        coEvery { api.getExercises(any(), any(), any()) } throws
            HttpException(Response.error<Any>(503, "".toResponseBody()))

        val result = repository().refresh(MuscleGroup.LEGS)

        assertEquals(AppResult.Failure(AppError.Http(503)), result)
    }

    @Test
    fun `invalid json is converted to AppError Serialization`() = runTest {
        coEvery { api.getExercises(any(), any(), any()) } throws SerializationException("bad json")

        assertEquals(AppResult.Failure(AppError.Serialization), repository().refresh(MuscleGroup.LEGS))
    }

    @Test
    fun `database failure while saving is converted to AppError Database`() = runTest {
        coEvery { api.getExercises(any(), any(), any()) } returns page(dto(1))
        coEvery { dao.replaceCategory(any(), any(), any()) } throws IllegalStateException("disk full")

        assertEquals(AppResult.Failure(AppError.Database), repository().refresh(MuscleGroup.LEGS))
    }

    @Test
    fun `cached exercises are observed from the database`() = runTest {
        // Arrange
        val entity = ExerciseEntity(
            id = 1, categoryId = 9, name = "Agachamento", description = "", imageUrl = null,
            thumbnailUrl = null, videoUrl = null, muscles = emptyList(), secondaryMuscles = emptyList(),
            equipment = emptyList(),
        )
        every { dao.observeByCategory(9) } returns flowOf(listOf(entity))

        // Act + Assert
        repository().observeExercises(MuscleGroup.LEGS).test {
            assertEquals(listOf("Agachamento"), awaitItem().map { it.name })
            awaitComplete()
        }
    }

    @Test
    fun `last updated is converted from epoch millis`() = runTest {
        coEvery { dao.getLastUpdated(9) } returns TEST_NOW.toEpochMilli()
        coEvery { dao.getLastUpdated(10) } returns null

        assertEquals(TEST_NOW, repository().lastUpdated(MuscleGroup.LEGS))
        assertNull(repository().lastUpdated(MuscleGroup.ABS))
    }
}
