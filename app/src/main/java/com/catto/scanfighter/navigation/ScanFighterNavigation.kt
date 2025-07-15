// app/src/main/java/com/catto/scanfighter/navigation/ScanFighterNavigation.kt
package com.catto.scanfighter.ui.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.catto.scanfighter.ScanFighterApplication
import com.catto.scanfighter.ui.screens.*
import com.catto.scanfighter.utils.viewmodels.FighterViewModel

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object MainMenu : Screen("main_menu")
    object CreateFighter : Screen("create_fighter")
    object Leaderboard : Screen("leaderboard")
    object FighterSelection : Screen("fighter_selection")
    object Battle : Screen("battle/{fighter1Id}/{fighter2Id}") {
        fun createRoute(fighter1Id: Int, fighter2Id: Int) = "battle/$fighter1Id/$fighter2Id"
    }
}

@Composable
fun ScanFighterNavigation() {
    val navController = rememberNavController()

    // Get the application context to access the repository
    val application = LocalContext.current.applicationContext as ScanFighterApplication

    // Create the ViewModel using the factory and the application's repository
    val fighterViewModel: FighterViewModel = viewModel(
        factory = FighterViewModel.FighterViewModelFactory(application.repository)
    )

    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        composable(Screen.MainMenu.route) {
            MainMenuScreen(navController = navController)
        }
        composable(Screen.CreateFighter.route) {
            // Pass the viewModel correctly
            CreateFighterScreen(navController = navController, viewModel = fighterViewModel)
        }
        composable(Screen.Leaderboard.route) {
            // Pass the viewModel correctly
            LeaderboardScreen(navController = navController, viewModel = fighterViewModel)
        }
        composable(Screen.FighterSelection.route) {
            // Pass the viewModel correctly
            FighterSelectionScreen(navController = navController, fighterViewModel = fighterViewModel)
        }
        composable(
            route = Screen.Battle.route,
            arguments = listOf(
                navArgument("fighter1Id") { type = NavType.IntType },
                navArgument("fighter2Id") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val fighter1Id = backStackEntry.arguments?.getInt("fighter1Id") ?: -1
            val fighter2Id = backStackEntry.arguments?.getInt("fighter2Id") ?: -1

            // Pass the repository to the BattleScreen
            BattleScreen(
                navController = navController,
                repository = application.repository, // Pass repository here
                fighter1Id = fighter1Id,
                fighter2Id = fighter2Id
            )
        }
    }
}
