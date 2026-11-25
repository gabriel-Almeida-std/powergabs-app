package com.gabriel.powergabs.data.local

import androidx.room.TypeConverter
import com.gabriel.powergabs.data.local.entity.EquipmentRef
import com.gabriel.powergabs.data.local.entity.MuscleRef
import kotlinx.serialization.json.Json

/**
 * O SQLite não tem coluna do tipo "lista". Estes conversores gravam as listas como texto JSON
 * (com kotlinx.serialization, sem Gson) e as reconstroem na leitura.
 */
class Converters {

    @TypeConverter
    fun fromMuscles(value: List<MuscleRef>): String = json.encodeToString(value)

    @TypeConverter
    fun toMuscles(value: String): List<MuscleRef> = json.decodeFromString(value)

    @TypeConverter
    fun fromEquipment(value: List<EquipmentRef>): String = json.encodeToString(value)

    @TypeConverter
    fun toEquipment(value: String): List<EquipmentRef> = json.decodeFromString(value)

    private companion object {
        val json = Json { ignoreUnknownKeys = true }
    }
}
