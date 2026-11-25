package com.gabriel.powergabs.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.gabriel.powergabs.data.local.entity.SessionEntity
import com.gabriel.powergabs.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/** Contas locais e sessão. */
@Dao
interface UserDao {

    /** `ABORT`: se o e-mail já existir, o índice único faz o Room lançar uma exceção de restrição. */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?

    /** Usuário da sessão atual (ou `null`), observado para decidir a tela inicial. */
    @Query("SELECT users.* FROM session INNER JOIN users ON users.id = session.userId LIMIT 1")
    fun observeSessionUser(): Flow<UserEntity?>

    @Query("SELECT users.* FROM session INNER JOIN users ON users.id = session.userId LIMIT 1")
    suspend fun getSessionUser(): UserEntity?

    @Upsert
    suspend fun saveSession(session: SessionEntity)

    @Query("DELETE FROM session")
    suspend fun clearSession()
}
