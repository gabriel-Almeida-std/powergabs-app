package com.gabriel.powergabs.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.gabriel.powergabs.data.local.entity.CategorySyncEntity
import com.gabriel.powergabs.data.local.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

/**
 * Cache de exercícios. As consultas que devolvem `Flow` são observadas pela UI:
 * o Room emite de novo sempre que a tabela muda (por isso a rede só precisa gravar).
 *
 * É uma `abstract class` (e não `interface`) para poder ter o método [replaceCategory] com corpo.
 */
@Dao
abstract class ExerciseDao {

    @Query("SELECT * FROM exercises WHERE categoryId = :categoryId")
    abstract fun observeByCategory(categoryId: Int): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    abstract fun observeById(id: Int): Flow<ExerciseEntity?>

    @Query("SELECT lastUpdated FROM category_sync WHERE categoryId = :categoryId")
    abstract fun observeLastUpdated(categoryId: Int): Flow<Long?>

    @Query("SELECT lastUpdated FROM category_sync WHERE categoryId = :categoryId")
    abstract suspend fun getLastUpdated(categoryId: Int): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(exercises: List<ExerciseEntity>)

    @Query("DELETE FROM exercises WHERE categoryId = :categoryId")
    abstract suspend fun deleteByCategory(categoryId: Int)

    @Upsert
    abstract suspend fun upsertSync(sync: CategorySyncEntity)

    /**
     * Troca o cache de um grupo inteiro numa única transação: quem observa nunca vê
     * a lista pela metade, e se algo falhar no meio nada é gravado.
     */
    @Transaction
    open suspend fun replaceCategory(categoryId: Int, exercises: List<ExerciseEntity>, updatedAt: Long) {
        deleteByCategory(categoryId)
        insertAll(exercises)
        upsertSync(CategorySyncEntity(categoryId, updatedAt))
    }
}
