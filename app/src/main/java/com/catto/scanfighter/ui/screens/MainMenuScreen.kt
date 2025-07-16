package com.catto.scanfighter.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.catto.scanfighter.ui.navigation.Screen
import com.catto.scanfighter.ui.components.GameButton

@Composable
fun MainMenuScreen(
    navController: NavController
) {
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
            Text(
                text = "Scan Fighter",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(64.dp))
            GameButton(
                text = "Create Fighter",
                onClick = {
                    navController.navigate(Screen.CreateFighter.route)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            GameButton(
                text = "Local Battle",
                onClick = {
                    navController.navigate(Screen.Leaderboard.route)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            GameButton(
                text = "Leaderboard",
                onClick = {
                    navController.navigate(Screen.Leaderboard.route)
                }
            )
        }
    }
}
