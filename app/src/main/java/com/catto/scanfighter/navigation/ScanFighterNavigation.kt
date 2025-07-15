// app/src/main/java/com/catto/scanfighter/navigation/ScanFighterNavigation.kt
package com.catto.scanfighter.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.catto.scanfighter.ui.screens.BattleScreen
import com.catto.scanfighter.ui.screens.CreateFighterScreen
import com.catto.scanfighter.ui.screens.FighterSelectionScreen
import com.catto.scanfighter.ui.screens.LeaderboardScreen
import com.catto.scanfighter.ui.screens.MainMenuScreen
import com.catto.scanfighter.ui.screens.SplashScreen
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
    val fighterViewModel: FighterViewModel = viewModel(factory = FighterViewModel.FighterViewModelFactory())

    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        composable(Screen.MainMenu.route) {
            MainMenuScreen(navController = navController)
        }
        composable(Screen.CreateFighter.route) {
            CreateFighterScreen(navController = navController, fighterViewModel = fighterViewModel)
        }
        composable(Screen.Leaderboard.route) {
            LeaderboardScreen(navController = navController, fighterViewModel = fighterViewModel)
        }
        composable(Screen.FighterSelection.route) {
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
            BattleScreen(
                navController = navController,
                fighterViewModel = fighterViewModel,
                fighter1Id = fighter1Id,
                fighter2Id = fighter2Id
            )
        }
    }
}