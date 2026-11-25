package com.gabriel.powergabs.data.mapper

import com.gabriel.powergabs.data.local.entity.EquipmentRef
import com.gabriel.powergabs.data.local.entity.ExerciseEntity
import com.gabriel.powergabs.data.local.entity.MuscleRef
import com.gabriel.powergabs.data.remote.dto.ExerciseInfoDto
import com.gabriel.powergabs.data.remote.dto.MuscleDto
import com.gabriel.powergabs.domain.model.Equipment
import com.gabriel.powergabs.domain.model.Exercise
import com.gabriel.powergabs.domain.model.LocalizedText
import com.gabriel.powergabs.domain.model.Muscle
import com.gabriel.powergabs.domain.model.MuscleGroup
import com.gabriel.powergabs.domain.rule.ExerciseCatalogRules

/**
 * DTO da API → entidade do Room.
 *
 * Usa a regra de idioma do domain (RN06): português, senão inglês.
 * Devolve `null` para exercício sem nome em nenhum dos dois idiomas, que não entra no cache.
 */
fun ExerciseInfoDto.toEntity(): ExerciseEntity? {
    val texts = translations.map { LocalizedText(it.language, it.name.trim(), it.description) }
    val text = ExerciseCatalogRules.pickTranslation(texts) ?: return null

    // Imagem principal (is_main) ou, se nenhuma for marcada, a primeira.
    val image = images.firstOrNull { it.isMain } ?: images.firstOrNull()
    val video = videos.firstOrNull { it.isMain } ?: videos.firstOrNull()

    return ExerciseEntity(
        id = id,
        categoryId = category.id,
        name = text.name,
        description = HtmlText.toPlainText(text.description),
        imageUrl = image?.image,
        thumbnailUrl = image?.let { it.thumbnails?.medium ?: it.thumbnails?.small ?: it.image },
        videoUrl = video?.video,
        muscles = muscles.map { it.toRef() },
        secondaryMuscles = musclesSecondary.map { it.toRef() },
        equipment = equipment.map { EquipmentRef(it.id, it.name) },
    )
}

/** Nome popular em inglês quando existir (ex.: "Chest"); senão, o nome científico. */
private fun MuscleDto.toRef() = MuscleRef(id = id, name = nameEn.ifBlank { name })

/** Entidade → domain. Devolve `null` se a categoria não for um grupo conhecido. */
fun ExerciseEntity.toDomain(): Exercise? {
    val group = MuscleGroup.fromCategoryId(categoryId) ?: return null
    return Exercise(
        id = id,
        muscleGroup = group,
        name = name,
        description = description,
        imageUrl = imageUrl,
        thumbnailUrl = thumbnailUrl,
        videoUrl = videoUrl,
        muscles = muscles.map { Muscle(it.id, it.name) },
        secondaryMuscles = secondaryMuscles.map { Muscle(it.id, it.name) },
        equipment = equipment.map { Equipment(it.id, it.name) },
    )
}
