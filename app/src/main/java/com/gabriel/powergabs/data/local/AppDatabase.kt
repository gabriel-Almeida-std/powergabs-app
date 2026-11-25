package com.gabriel.powergabs.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gabriel.powergabs.data.local.dao.ExerciseDao
import com.gabriel.powergabs.data.local.dao.UserDao
import com.gabriel.powergabs.data.local.dao.WorkoutDao
import com.gabriel.powergabs.data.local.entity.CategorySyncEntity
import com.gabriel.powergabs.data.local.entity.ExerciseEntity
import com.gabriel.powergabs.data.local.entity.SessionEntity
import com.gabriel.powergabs.data.local.entity.UserEntity
import com.gabriel.powergabs.data.local.entity.WorkoutItemEntity

/**
 * Banco local: a única fonte de verdade do app (RNF01).
 * O esquema é exportado para `app/schemas/` e serve de base para migrações futuras.
 */
@Database(
    entities = [
        ExerciseEntity::class,
        CategorySyncEntity::class,
        UserEntity::class,
        SessionEntity::class,
        WorkoutItemEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun userDao(): UserDao
    abstract fun workoutDao(): WorkoutDao

    companion object {
        private const val NAME = "powergabs.db"

        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, NAME).build()
    }
}
