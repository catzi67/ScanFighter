package com.catto.scanfighter.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catto.scanfighter.data.Fighter
import com.catto.scanfighter.ui.viewmodels.FighterViewModel

@Composable
fun LeaderboardScreen(viewModel: FighterViewModel) {
    val fighters by viewModel.allFighters.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Leaderboard", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        LazyColumn {
            // Explicitly naming the 'items' parameter to resolve the overload ambiguity.
            // This ensures the compiler uses the correct 'items' function that takes a list.
            items(items = fighters) { fighter ->
                FighterCard(fighter = fighter)
            }
        }
    }
}

@Composable
fun FighterCard(fighter: Fighter) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = fighter.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Row {
                Text(text = "HP: ${fighter.hp}")
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "Attack: ${fighter.attack}")
            }
            Row {
                Text(text = "Defense: ${fighter.defense}")
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "Speed: ${fighter.speed}")
            }
            Text(text = "Luck: ${fighter.luck}")
        }
    }
}
