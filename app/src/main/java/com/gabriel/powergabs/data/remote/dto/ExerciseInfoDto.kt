package com.gabriel.powergabs.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTOs do endpoint `exerciseinfo/` da wger. Espelham o JSON da API e nunca saem da camada data:
 * os mappers os convertem em entidades do Room.
 *
 * Todos os campos têm valor padrão: se a API omitir algum, a leitura não quebra.
 * Campos que o app não usa são ignorados (`ignoreUnknownKeys` no `Json`).
 */
@Serializable
data class PageDto<T>(
    val count: Int = 0,
    val next: String? = null,
    val results: List<T> = emptyList(),
)

@Serializable
data class ExerciseInfoDto(
    val id: Int,
    val category: CategoryDto,
    val muscles: List<MuscleDto> = emptyList(),
    @SerialName("muscles_secondary") val musclesSecondary: List<MuscleDto> = emptyList(),
    val equipment: List<EquipmentDto> = emptyList(),
    val images: List<ImageDto> = emptyList(),
    val translations: List<TranslationDto> = emptyList(),
    val videos: List<VideoDto> = emptyList(),
)

@Serializable
data class CategoryDto(val id: Int, val name: String = "")

@Serializable
data class MuscleDto(
    val id: Int,
    val name: String = "",
    @SerialName("name_en") val nameEn: String = "",
)

@Serializable
data class EquipmentDto(val id: Int, val name: String = "")

@Serializable
data class ImageDto(
    val image: String,
    @SerialName("is_main") val isMain: Boolean = false,
    val thumbnails: ThumbnailsDto? = null,
)

@Serializable
data class ThumbnailsDto(
    val small: String? = null,
    val medium: String? = null,
)

@Serializable
data class TranslationDto(
    val language: Int,
    val name: String = "",
    val description: String = "",
)

@Serializable
data class VideoDto(
    val video: String,
    @SerialName("is_main") val isMain: Boolean = false,
)
