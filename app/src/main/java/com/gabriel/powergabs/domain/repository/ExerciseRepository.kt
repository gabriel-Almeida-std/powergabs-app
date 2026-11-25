package com.gabriel.powergabs.domain.repository

import com.gabriel.powergabs.core.result.AppResult
import com.gabriel.powergabs.domain.model.Exercise
import com.gabriel.powergabs.domain.model.MuscleGroup
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Exercícios em cache (Offline First, RNF01).
 * Os `observe*` leem do banco local (a única fonte de verdade); [refresh] apenas atualiza o banco
 * a partir da API, e os Flows emitem os dados novos sozinhos.
 */
interface ExerciseRepository {
    fun observeExercises(group: MuscleGroup): Flow<List<Exercise>>
    fun observeLastUpdated(group: MuscleGroup): Flow<Instant?>
    fun observeExercise(id: Int): Flow<Exercise?>
    suspend fun lastUpdated(group: MuscleGroup): Instant?

    /** Baixa todos os exercícios do grupo e substitui o cache dele numa transação (tudo ou nada). */
    suspend fun refresh(group: MuscleGroup): AppResult<Unit>
}
