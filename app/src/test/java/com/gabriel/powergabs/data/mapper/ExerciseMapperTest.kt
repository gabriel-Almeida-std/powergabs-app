package com.gabriel.powergabs.data.mapper

import com.gabriel.powergabs.data.local.entity.EquipmentRef
import com.gabriel.powergabs.data.local.entity.MuscleRef
import com.gabriel.powergabs.data.remote.dto.CategoryDto
import com.gabriel.powergabs.data.remote.dto.EquipmentDto
import com.gabriel.powergabs.data.remote.dto.ExerciseInfoDto
import com.gabriel.powergabs.data.remote.dto.ImageDto
import com.gabriel.powergabs.data.remote.dto.MuscleDto
import com.gabriel.powergabs.data.remote.dto.ThumbnailsDto
import com.gabriel.powergabs.data.remote.dto.TranslationDto
import com.gabriel.powergabs.data.remote.dto.VideoDto
import com.gabriel.powergabs.domain.model.MuscleGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExerciseMapperTest {

    private fun dto(
        translations: List<TranslationDto> = listOf(
            TranslationDto(language = 2, name = "Bench Press", description = "<p>Lay down.</p>"),
            TranslationDto(language = 7, name = " Supino reto ", description = "<p>Deite no banco.</p>"),
        ),
        images: List<ImageDto> = emptyList(),
        videos: List<VideoDto> = emptyList(),
        categoryId: Int = 11,
    ) = ExerciseInfoDto(
        id = 73,
        category = CategoryDto(categoryId, "Chest"),
        muscles = listOf(MuscleDto(4, "Pectoralis major", "Chest"), MuscleDto(3, "Serratus anterior", "")),
        musclesSecondary = listOf(MuscleDto(5, "Triceps brachii", "Triceps")),
        equipment = listOf(EquipmentDto(1, "Barbell")),
        images = images,
        translations = translations,
        videos = videos,
    )

    @Test
    fun `portuguese name and plain text description are used`() {
        // Act
        val entity = dto().toEntity()!!

        // Assert
        assertEquals("Supino reto", entity.name)
        assertEquals("Deite no banco.", entity.description)
        assertEquals(11, entity.categoryId)
        assertEquals(listOf(EquipmentRef(1, "Barbell")), entity.equipment)
    }

    @Test
    fun `english is used when portuguese is missing`() {
        val entity = dto(translations = listOf(TranslationDto(2, "Bench Press", "<p>Lay down.</p>"))).toEntity()!!

        assertEquals("Bench Press", entity.name)
    }

    @Test
    fun `exercise without portuguese or english name is discarded`() {
        val entity = dto(translations = listOf(TranslationDto(1, "Bankdrücken", ""))).toEntity()

        assertNull(entity)
    }

    @Test
    fun `muscle uses popular english name and falls back to scientific name`() {
        val entity = dto().toEntity()!!

        assertEquals(listOf(MuscleRef(4, "Chest"), MuscleRef(3, "Serratus anterior")), entity.muscles)
        assertEquals(listOf(MuscleRef(5, "Triceps")), entity.secondaryMuscles)
    }

    @Test
    fun `main image and medium thumbnail are chosen`() {
        // Arrange
        val images = listOf(
            ImageDto("https://img/side.png", isMain = false),
            ImageDto("https://img/main.png", isMain = true, thumbnails = ThumbnailsDto(small = "s.png", medium = "m.png")),
        )

        // Act
        val entity = dto(images = images).toEntity()!!

        // Assert
        assertEquals("https://img/main.png", entity.imageUrl)
        assertEquals("m.png", entity.thumbnailUrl)
    }

    @Test
    fun `first image is used when none is main and thumbnail falls back to the image`() {
        val entity = dto(images = listOf(ImageDto("https://img/a.png"), ImageDto("https://img/b.png"))).toEntity()!!

        assertEquals("https://img/a.png", entity.imageUrl)
        assertEquals("https://img/a.png", entity.thumbnailUrl)
    }

    @Test
    fun `exercise without media has null urls`() {
        val entity = dto().toEntity()!!

        assertNull(entity.imageUrl)
        assertNull(entity.thumbnailUrl)
        assertNull(entity.videoUrl)
    }

    @Test
    fun `first video is used when none is main`() {
        val entity = dto(videos = listOf(VideoDto("https://v/1.MOV"), VideoDto("https://v/2.MOV"))).toEntity()!!

        assertEquals("https://v/1.MOV", entity.videoUrl)
    }

    @Test
    fun `entity is mapped to domain with its muscle group`() {
        val exercise = dto(images = listOf(ImageDto("https://img/a.png"))).toEntity()!!.toDomain()!!

        assertEquals(MuscleGroup.CHEST, exercise.muscleGroup)
        assertEquals("Supino reto", exercise.name)
        assertEquals(listOf(4, 3), exercise.muscles.map { it.id })
        assertEquals(listOf(1), exercise.equipment.map { it.id })
        assertEquals(true, exercise.hasImage)
    }

    @Test
    fun `entity with unknown category is not mapped`() {
        val entity = dto(categoryId = 999).toEntity()!!

        assertNull(entity.toDomain())
    }
}
