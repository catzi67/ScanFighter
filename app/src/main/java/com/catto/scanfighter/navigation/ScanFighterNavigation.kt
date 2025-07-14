package com.catto.scanfighter.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.catto.scanfighter.ScanFighterApplication
import com.catto.scanfighter.ui.screens.BattleScreen
import com.catto.scanfighter.ui.screens.CreateFighterScreen
import com.catto.scanfighter.ui.screens.LeaderboardScreen
import com.catto.scanfighter.ui.screens.MainMenuScreen
import com.catto.scanfighter.ui.screens.SplashScreen
import com.catto.scanfighter.ui.viewmodels.FighterViewModel
import com.catto.scanfighter.ui.viewmodels.FighterViewModelFactory

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object MainMenu : Screen("main_menu")
    object CreateFighter : Screen("create_fighter")
    object Leaderboard : Screen("leaderboard")
    object Battle : Screen("battle")
}

@Composable
fun ScanFighterNavigation() {
    val navController = rememberNavController()
    val application = LocalContext.current.applicationContext as ScanFighterApplication
    val fighterViewModel: FighterViewModel = viewModel(
        factory = FighterViewModelFactory(application.database.fighterDao())
    )

    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        composable(Screen.MainMenu.route) {
            MainMenuScreen(navController = navController)
        }
        composable(Screen.CreateFighter.route) {
            CreateFighterScreen(navController = navController, viewModel = fighterViewModel)
        }
        composable(Screen.Leaderboard.route) {
            LeaderboardScreen(viewModel = fighterViewModel)
        }
        composable(Screen.Battle.route) {
            BattleScreen()
        }
    }
}
