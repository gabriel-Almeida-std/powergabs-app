package com.gabriel.powergabs.data.repository

import com.gabriel.powergabs.core.dispatcher.AppDispatchers
import com.gabriel.powergabs.core.result.AppResult
import com.gabriel.powergabs.data.local.dao.WorkoutDao
import com.gabriel.powergabs.data.mapper.toDomain
import com.gabriel.powergabs.data.mapper.toEntity
import com.gabriel.powergabs.domain.model.MuscleGroup
import com.gabriel.powergabs.domain.model.PlannedExercise
import com.gabriel.powergabs.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant

/** "Meu treino" totalmente local: funciona sem internet (RNF01). */
class WorkoutRepositoryImpl(
    private val dao: WorkoutDao,
    private val dispatchers: AppDispatchers,
) : WorkoutRepository {

    override fun observeWorkout(userId: Long): Flow<List<PlannedExercise>> =
        dao.observeByUser(userId).map { entities -> entities.mapNotNull { it.toDomain() } }

    override fun observeIsInWorkout(userId: Long, exerciseId: Int): Flow<Boolean> =
        dao.observeExists(userId, exerciseId)

    override fun observeDoneDates(userId: Long, group: MuscleGroup): Flow<List<Instant>> =
        dao.observeDoneDates(userId, group.categoryId).map { dates -> dates.map(Instant::ofEpochMilli) }

    override suspend fun find(userId: Long, itemId: Long): PlannedExercise? = withContext(dispatchers.io) {
        dao.find(userId, itemId)?.toDomain()
    }

    override suspend fun isInWorkout(userId: Long, exerciseId: Int): Boolean = withContext(dispatchers.io) {
        dao.exists(userId, exerciseId)
    }

    override suspend fun add(userId: Long, item: PlannedExercise): AppResult<Unit> = withContext(dispatchers.io) {
        safeDbCall { dao.insert(item.toEntity(userId)); Unit }
    }

    override suspend fun update(userId: Long, item: PlannedExercise): AppResult<Unit> = withContext(dispatchers.io) {
        safeDbCall { dao.update(item.toEntity(userId)) }
    }
}
