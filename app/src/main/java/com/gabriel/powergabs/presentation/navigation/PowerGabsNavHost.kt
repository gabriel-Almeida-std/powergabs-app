package com.gabriel.powergabs.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.gabriel.powergabs.presentation.screens.about.AboutScreen
import com.gabriel.powergabs.presentation.screens.auth.LoginRoute as LoginScreenRoute
import com.gabriel.powergabs.presentation.screens.auth.RegisterRoute as RegisterScreenRoute
import com.gabriel.powergabs.presentation.screens.details.ExerciseDetailsRoute as ExerciseDetailsScreenRoute
import com.gabriel.powergabs.presentation.screens.exercises.ExerciseListRoute as ExerciseListScreenRoute
import com.gabriel.powergabs.presentation.screens.goal.GoalRoute as GoalScreenRoute
import com.gabriel.powergabs.presentation.screens.musclegroup.MuscleGroupRoute as MuscleGroupScreenRoute
import com.gabriel.powergabs.presentation.screens.workout.WorkoutRoute as WorkoutScreenRoute

/**
 * Grafo de navegação type-safe. Cada `composable<Rota>` liga uma rota à sua tela;
 * os argumentos chegam tipados com `backStackEntry.toRoute<Rota>()`.
 */
@Composable
fun PowerGabsNavHost(startDestination: Any) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable<LoginRoute> {
            LoginScreenRoute(onGoToRegister = { navController.navigate(RegisterRoute) })
        }
        composable<RegisterRoute> {
            RegisterScreenRoute(onGoToLogin = { navController.popBackStack() })
        }
        composable<GoalRoute> {
            GoalScreenRoute(
                onContinue = { goal -> navController.navigate(MuscleGroupRoute(goal)) },
                onOpenWorkout = { navController.navigate(WorkoutRoute) },
                onOpenAbout = { navController.navigate(AboutRoute) },
            )
        }
        composable<MuscleGroupRoute> { entry ->
            val route = entry.toRoute<MuscleGroupRoute>()
            MuscleGroupScreenRoute(
                goal = route.goal,
                onBack = { navController.popBackStack() },
                onBuildWorkout = { goal, group -> navController.navigate(ExerciseListRoute(goal, group)) },
            )
        }
        composable<ExerciseListRoute> { entry ->
            val route = entry.toRoute<ExerciseListRoute>()
            ExerciseListScreenRoute(
                goal = route.goal,
                group = route.group,
                onBack = { navController.popBackStack() },
                onOpenExercise = { id, goal -> navController.navigate(ExerciseDetailsRoute(id, goal)) },
            )
        }
        composable<ExerciseDetailsRoute> { entry ->
            val route = entry.toRoute<ExerciseDetailsRoute>()
            ExerciseDetailsScreenRoute(
                exerciseId = route.exerciseId,
                goal = route.goal,
                onBack = { navController.popBackStack() },
            )
        }
        composable<WorkoutRoute> {
            WorkoutScreenRoute(
                onBack = { navController.popBackStack() },
                // Volta para a tela de objetivo (início do grafo do usuário logado).
                onChooseGoal = { navController.popBackStack(GoalRoute, inclusive = false) },
            )
        }
        composable<AboutRoute> { AboutScreen(onBack = { navController.popBackStack() }) }
    }
}
