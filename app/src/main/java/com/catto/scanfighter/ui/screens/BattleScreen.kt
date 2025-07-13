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
import com.catto.scanfighter.ui.components.GameButton

@Composable
fun BattleScreen() {
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
                text = "Local Battle",
                onClick = { /* TODO: Implement Local Battle */ }
            )
            Spacer(modifier = Modifier.height(24.dp))
            GameButton(
                text = "WiFi P2P Battle",
                onClick = { /* TODO: Implement WiFi P2P Battle */ }
            )
        }
    }
}
