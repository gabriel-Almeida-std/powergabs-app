package com.gabriel.powergabs.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gabriel.powergabs.presentation.components.EmptyState

/**
 * Grafo de navegação type-safe. Cada `composable<Rota>` liga uma rota à sua tela;
 * os argumentos chegam tipados com `backStackEntry.toRoute<Rota>()`.
 */
@Composable
fun PowerGabsNavHost(startDestination: Any) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable<LoginRoute> { EmptyState(message = "Login") }
        composable<GoalRoute> { EmptyState(message = "Objetivo") }
    }
}
