package com.gabriel.powergabs.data.remote

import com.gabriel.powergabs.data.remote.dto.ExerciseInfoDto
import com.gabriel.powergabs.data.remote.dto.PageDto
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Desserialização de um trecho real (reduzido) de `exerciseinfo/?category=11`, consultado em 24/11/2026. */
class ExerciseInfoDtoTest {

    private val json = NetworkFactory.json

    private val page = """
        {
          "count": 90,
          "next": "https://wger.de/api/v2/exerciseinfo/?category=11&limit=1&offset=1",
          "previous": null,
          "results": [
            {
              "id": 73,
              "uuid": "3717d144-7815-4a97-9a56-956fb889c996",
              "category": {"id": 11, "name": "Chest"},
              "muscles": [{"id": 4, "name": "Pectoralis major", "name_en": "Chest", "is_front": true}],
              "muscles_secondary": [{"id": 5, "name": "Triceps brachii", "name_en": "Triceps", "is_front": false}],
              "equipment": [{"id": 1, "name": "Barbell"}, {"id": 8, "name": "Bench"}],
              "license": {"id": 2, "short_name": "CC-BY-SA 4"},
              "images": [{
                "id": 68,
                "image": "https://wger.de/media/exercise-images/192/Bench-press-1.png",
                "thumbnails": {
                  "small": "https://wger.de/media/exercise-images/192/Bench-press-1.png.200x200_q85.png",
                  "medium": "https://wger.de/media/exercise-images/192/Bench-press-1.png.400x400_q85.png"
                },
                "is_main": true,
                "style": "1"
              }],
              "translations": [
                {"id": 1, "name": "Supino reto", "exercise": 73, "description": "<p>Deite no banco.</p>", "language": 7},
                {"id": 2, "name": "Bench Press", "exercise": 73, "description": "<p>Lay down.</p>", "language": 2}
              ],
              "videos": [{"id": 21, "video": "https://wger.de/media/exercise-video/73/a.MOV", "is_main": false, "duration": "15.93"}]
            }
          ]
        }
    """.trimIndent()

    @Test
    fun `exerciseinfo page is parsed ignoring unknown fields`() {
        // Act
        val dto = json.decodeFromString<PageDto<ExerciseInfoDto>>(page)

        // Assert
        assertEquals(90, dto.count)
        assertTrue(dto.next!!.contains("offset=1"))
        val exercise = dto.results.single()
        assertEquals(73, exercise.id)
        assertEquals(11, exercise.category.id)
        assertEquals(listOf(1, 8), exercise.equipment.map { it.id })
        assertEquals("Chest", exercise.muscles.single().nameEn)
        assertEquals(5, exercise.musclesSecondary.single().id)
        assertTrue(exercise.images.single().isMain)
        assertEquals(
            "https://wger.de/media/exercise-images/192/Bench-press-1.png.400x400_q85.png",
            exercise.images.single().thumbnails?.medium,
        )
        assertEquals(listOf(7, 2), exercise.translations.map { it.language })
        assertEquals("https://wger.de/media/exercise-video/73/a.MOV", exercise.videos.single().video)
    }

    @Test
    fun `missing optional lists default to empty`() {
        val minimal = """{"count": 1, "next": null, "results": [{"id": 1, "category": {"id": 15}}]}"""

        val dto = json.decodeFromString<PageDto<ExerciseInfoDto>>(minimal)

        val exercise = dto.results.single()
        assertNull(dto.next)
        assertTrue(exercise.images.isEmpty())
        assertTrue(exercise.translations.isEmpty())
        assertTrue(exercise.equipment.isEmpty())
    }

    @Test(expected = SerializationException::class)
    fun `malformed json throws serialization exception`() {
        json.decodeFromString<PageDto<ExerciseInfoDto>>("""{"results": [{"id": "abc"}]}""")
    }
}
