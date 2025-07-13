package com.catto.scanfighter.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.catto.scanfighter.R
import com.catto.scanfighter.ui.components.GameButton
import com.catto.scanfighter.ui.navigation.Screen

@Composable
fun SplashScreen(navController: NavController) {
    // Use a Surface to set the background color, which will be visible
    // if the image doesn't fill the entire screen (letterboxing).
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.scan_fighter_banner),
                contentDescription = "Scan Fighter Banner",
                modifier = Modifier.fillMaxSize(),
                // Change ContentScale.Crop to ContentScale.Fit
                // to ensure the entire image is visible.
                contentScale = ContentScale.Fit
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GameButton(
                    text = "Start Game",
                    onClick = { navController.navigate(Screen.MainMenu.route) }
                )
            }
        }
    }
}
