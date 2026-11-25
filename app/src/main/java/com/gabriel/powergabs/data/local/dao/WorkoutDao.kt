package com.gabriel.powergabs.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gabriel.powergabs.data.local.entity.WorkoutItemEntity
import kotlinx.coroutines.flow.Flow

/** "Meu treino": sempre filtrado pelo usuário dono dos itens. */
@Dao
interface WorkoutDao {

    @Query("SELECT * FROM workout_items WHERE userId = :userId ORDER BY addedAt DESC, id DESC")
    fun observeByUser(userId: Long): Flow<List<WorkoutItemEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM workout_items WHERE userId = :userId AND exerciseId = :exerciseId)")
    fun observeExists(userId: Long, exerciseId: Int): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM workout_items WHERE userId = :userId AND exerciseId = :exerciseId)")
    suspend fun exists(userId: Long, exerciseId: Int): Boolean

    @Query("SELECT * FROM workout_items WHERE userId = :userId AND id = :id")
    suspend fun find(userId: Long, id: Long): WorkoutItemEntity?

    /** Datas de conclusão do grupo, para o aviso de descanso (RN10). */
    @Query(
        """
        SELECT doneAt FROM workout_items
        WHERE userId = :userId AND categoryId = :categoryId AND status = 'DONE' AND doneAt IS NOT NULL
        """,
    )
    fun observeDoneDates(userId: Long, categoryId: Int): Flow<List<Long>>

    /** `ABORT`: o índice único (userId, exerciseId) recusa duplicados (RN11). */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(item: WorkoutItemEntity): Long

    @Update
    suspend fun update(item: WorkoutItemEntity)
}
