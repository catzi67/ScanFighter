package com.catto.scanfighter.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.catto.scanfighter.ui.components.GameButton
import com.catto.scanfighter.ui.navigation.Screen

@Composable
fun MainMenuScreen(navController: NavController) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GameButton(
                text = "Scan New Fighter",
                onClick = { navController.navigate(Screen.CreateFighter.route) }
            )
            Spacer(modifier = Modifier.height(24.dp))
            GameButton(
                text = "Leaderboard",
                onClick = { navController.navigate(Screen.Leaderboard.route) }
            )
            Spacer(modifier = Modifier.height(24.dp))
            GameButton(
                text = "Battle",
                onClick = { navController.navigate(Screen.Battle.route) }
            )
        }
    }
}
