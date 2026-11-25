package com.gabriel.powergabs.data.mapper

import com.gabriel.powergabs.data.local.entity.UserEntity
import com.gabriel.powergabs.domain.model.Goal
import com.gabriel.powergabs.domain.model.MuscleGroup
import com.gabriel.powergabs.domain.model.User
import com.gabriel.powergabs.domain.model.WorkoutStatus
import com.gabriel.powergabs.testutil.TEST_NOW
import com.gabriel.powergabs.testutil.plannedExercise
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WorkoutMapperTest {

    @Test
    fun `planned exercise survives a round trip through the entity`() {
        // Arrange
        val original = plannedExercise(
            id = 3,
            exerciseId = 42,
            group = MuscleGroup.LEGS,
            goal = Goal.STRENGTH,
            status = WorkoutStatus.DONE,
            doneAt = TEST_NOW,
            effort = 4,
        )

        // Act
        val entity = original.toEntity(userId = 9)
        val restored = entity.toDomain()

        // Assert
        assertEquals(9, entity.userId)
        assertEquals(MuscleGroup.LEGS.categoryId, entity.categoryId)
        assertEquals("STRENGTH", entity.goal)
        assertEquals("DONE", entity.status)
        assertEquals(original, restored)
    }

    @Test
    fun `entity with unknown status is ignored`() {
        val entity = plannedExercise().toEntity(userId = 1).copy(status = "CANCELLED")

        assertNull(entity.toDomain())
    }

    @Test
    fun `user entity is mapped without password data`() {
        val entity = UserEntity(id = 5, email = "g@mail.com", passwordHash = "hash", salt = "salt", createdAt = 0)

        assertEquals(User(5, "g@mail.com"), entity.toDomain())
    }
}
