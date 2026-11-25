package com.gabriel.powergabs.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/** Músculo guardado dentro do exercício (lista gravada como JSON pelo `Converters`). */
@Serializable
data class MuscleRef(val id: Int, val name: String)

/** Equipamento guardado dentro do exercício (lista gravada como JSON pelo `Converters`). */
@Serializable
data class EquipmentRef(val id: Int, val name: String)

/**
 * Exercício em cache, já com o idioma resolvido (RN06).
 * O índice em `categoryId` acelera a consulta "exercícios do grupo".
 */
@Entity(tableName = "exercises", indices = [Index("categoryId")])
data class ExerciseEntity(
    @PrimaryKey val id: Int,
    val categoryId: Int,
    val name: String,
    val description: String,
    val imageUrl: String?,
    val thumbnailUrl: String?,
    val videoUrl: String?,
    val muscles: List<MuscleRef>,
    val secondaryMuscles: List<MuscleRef>,
    val equipment: List<EquipmentRef>,
)

/** Data da última atualização bem-sucedida de cada grupo (RN09 e "atualizado há…"). */
@Entity(tableName = "category_sync")
data class CategorySyncEntity(
    @PrimaryKey val categoryId: Int,
    val lastUpdated: Long, // epoch em milissegundos
)

/**
 * Conta local. Guarda só o hash da senha e o sal (RNF03); a senha em texto puro nunca é gravada.
 * O índice único em `email` impede duas contas com o mesmo e-mail (RN15).
 */
@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val passwordHash: String,
    val salt: String,
    val createdAt: Long,
)

/** Sessão atual: uma única linha (id = 0). Login grava; logout apaga (RF03, RF04). */
@Entity(tableName = "session")
data class SessionEntity(
    @PrimaryKey val id: Int = SINGLE_ROW_ID,
    val userId: Long,
) {
    companion object {
        const val SINGLE_ROW_ID = 0
    }
}

/**
 * Item do "Meu treino". Guarda cópia de nome, grupo e miniatura, e a prescrição congelada (RN12).
 * O índice único `(userId, exerciseId)` reforça no próprio banco a regra "sem repetição" (RN11).
 */
@Entity(
    tableName = "workout_items",
    indices = [Index(value = ["userId", "exerciseId"], unique = true)],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class WorkoutItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val exerciseId: Int,
    val name: String,
    val categoryId: Int,
    val thumbnailUrl: String?,
    val goal: String,
    val setsMin: Int,
    val setsMax: Int,
    val repsMin: Int,
    val repsMax: Int,
    val restMin: Int,
    val restMax: Int,
    val status: String,
    val addedAt: Long,
    val doneAt: Long?,
    val effort: Int?,
)
