package com.gabriel.powergabs.presentation.navigation

import com.gabriel.powergabs.domain.model.Goal
import com.gabriel.powergabs.domain.model.MuscleGroup
import kotlinx.serialization.Serializable

/*
 * Rotas type-safe do Navigation Compose: cada destino é uma classe @Serializable,
 * e os argumentos são propriedades com tipo. O compilador impede, por exemplo,
 * abrir a lista de exercícios sem informar o objetivo e o grupo.
 */

@Serializable
data object LoginRoute

@Serializable
data object RegisterRoute

@Serializable
data object GoalRoute

@Serializable
data class MuscleGroupRoute(val goal: Goal)

@Serializable
data class ExerciseListRoute(val goal: Goal, val group: MuscleGroup)

@Serializable
data class ExerciseDetailsRoute(val exerciseId: Int, val goal: Goal)

@Serializable
data object WorkoutRoute

@Serializable
data object AboutRoute
