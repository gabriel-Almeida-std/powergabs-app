package com.gabriel.powergabs.domain.repository

import com.gabriel.powergabs.core.result.AppResult
import com.gabriel.powergabs.domain.model.MuscleGroup
import com.gabriel.powergabs.domain.model.PlannedExercise
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/** "Meu treino" do usuário, totalmente local (RF12–RF15). */
interface WorkoutRepository {
    fun observeWorkout(userId: Long): Flow<List<PlannedExercise>>
    fun observeIsInWorkout(userId: Long, exerciseId: Int): Flow<Boolean>

    /** Datas de conclusão dos exercícios `DONE` do grupo, usadas no aviso de descanso (RN10). */
    fun observeDoneDates(userId: Long, group: MuscleGroup): Flow<List<Instant>>

    suspend fun find(userId: Long, itemId: Long): PlannedExercise?
    suspend fun isInWorkout(userId: Long, exerciseId: Int): Boolean
    suspend fun add(userId: Long, item: PlannedExercise): AppResult<Unit>
    suspend fun update(userId: Long, item: PlannedExercise): AppResult<Unit>
}
