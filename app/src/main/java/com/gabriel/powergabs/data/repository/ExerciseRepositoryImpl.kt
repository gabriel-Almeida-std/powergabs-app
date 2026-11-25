package com.gabriel.powergabs.data.repository

import com.gabriel.powergabs.core.dispatcher.AppDispatchers
import com.gabriel.powergabs.core.result.AppResult
import com.gabriel.powergabs.core.time.TimeProvider
import com.gabriel.powergabs.data.local.dao.ExerciseDao
import com.gabriel.powergabs.data.mapper.toDomain
import com.gabriel.powergabs.data.mapper.toEntity
import com.gabriel.powergabs.data.remote.WgerApi
import com.gabriel.powergabs.data.remote.dto.ExerciseInfoDto
import com.gabriel.powergabs.domain.model.Exercise
import com.gabriel.powergabs.domain.model.MuscleGroup
import com.gabriel.powergabs.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant

/**
 * Offline First (RNF01): a UI só lê do Room; a rede só escreve no Room.
 *
 * O [refresh] baixa todas as páginas do grupo e só grava se TODAS chegarem (tudo ou nada),
 * substituindo o cache do grupo numa transação.
 */
class ExerciseRepositoryImpl(
    private val api: WgerApi,
    private val dao: ExerciseDao,
    private val timeProvider: TimeProvider,
    private val dispatchers: AppDispatchers,
) : ExerciseRepository {

    override fun observeExercises(group: MuscleGroup): Flow<List<Exercise>> =
        dao.observeByCategory(group.categoryId).map { entities -> entities.mapNotNull { it.toDomain() } }

    override fun observeLastUpdated(group: MuscleGroup): Flow<Instant?> =
        dao.observeLastUpdated(group.categoryId).map { it?.let(Instant::ofEpochMilli) }

    override fun observeExercise(id: Int): Flow<Exercise?> =
        dao.observeById(id).map { it?.toDomain() }

    override suspend fun lastUpdated(group: MuscleGroup): Instant? = withContext(dispatchers.io) {
        dao.getLastUpdated(group.categoryId)?.let(Instant::ofEpochMilli)
    }

    override suspend fun refresh(group: MuscleGroup): AppResult<Unit> = withContext(dispatchers.io) {
        val collected = mutableListOf<ExerciseInfoDto>()
        var offset = 0
        for (page in 1..WgerApi.MAX_PAGES) {
            val result = safeApiCall { api.getExercises(group.categoryId, WgerApi.PAGE_SIZE, offset) }
            when (result) {
                is AppResult.Failure -> return@withContext result // nada é gravado
                is AppResult.Success -> {
                    collected += result.data.results
                    if (result.data.next == null) break
                    offset += WgerApi.PAGE_SIZE
                }
            }
        }
        val entities = collected.distinctBy { it.id }.mapNotNull { it.toEntity() }
        safeDbCall {
            dao.replaceCategory(group.categoryId, entities, timeProvider.now().toEpochMilli())
        }
    }
}
