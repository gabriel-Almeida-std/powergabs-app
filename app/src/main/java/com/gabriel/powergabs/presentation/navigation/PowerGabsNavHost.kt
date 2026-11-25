package com.gabriel.powergabs.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gabriel.powergabs.presentation.components.EmptyState
import com.gabriel.powergabs.presentation.screens.auth.LoginRoute as LoginScreenRoute
import com.gabriel.powergabs.presentation.screens.auth.RegisterRoute as RegisterScreenRoute
import com.gabriel.powergabs.presentation.screens.goal.GoalRoute as GoalScreenRoute

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
        composable<MuscleGroupRoute> { EmptyState(message = "Grupo muscular") }
        composable<WorkoutRoute> { EmptyState(message = "Meu treino") }
        composable<AboutRoute> { EmptyState(message = "Sobre") }
    }
}
