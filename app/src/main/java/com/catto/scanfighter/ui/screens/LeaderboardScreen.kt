package com.catto.scanfighter.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catto.scanfighter.data.Fighter
import com.catto.scanfighter.ui.components.ColorSignature
import com.catto.scanfighter.ui.components.GameButton
import com.catto.scanfighter.ui.components.GameDialog
import com.catto.scanfighter.ui.components.GameTextField
import com.catto.scanfighter.ui.theme.Bronze
import com.catto.scanfighter.ui.theme.Gold
import com.catto.scanfighter.ui.theme.Silver
import com.catto.scanfighter.ui.viewmodels.FighterViewModel
import com.catto.scanfighter.utils.FighterStatsGenerator
import com.catto.scanfighter.utils.MusicUtils
import com.catto.scanfighter.utils.SoundPlayer

@Composable
fun LeaderboardScreen(viewModel: FighterViewModel) {
    val fighters by viewModel.allFighters.collectAsState(initial = emptyList())
    var selectedFighter by remember { mutableStateOf<Fighter?>(null) }
    var showOptionsDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    val soundPlayer = remember { SoundPlayer() }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(16.dp)
        ) {
            Text(
                text = "Leaderboard",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            LazyColumn {
                itemsIndexed(items = fighters) { index, fighter ->
                    val medalColor = when (index) {
                        0 -> Gold
                        1 -> Silver
                        2 -> Bronze
                        else -> null
                    }
                    FighterCard(
                        fighter = fighter,
                        medalColor = medalColor,
                        onClick = {
                            selectedFighter = fighter
                            showOptionsDialog = true
                        },
                        onNotePlayed = { frequency ->
                            soundPlayer.playNote(frequency)
                        }
                    )
                }
            }
        }
    }

    // Fighter Management Options Dialog
    if (showOptionsDialog && selectedFighter != null) {
        GameDialog(
            title = "Manage ${selectedFighter!!.name}",
            onDismissRequest = { showOptionsDialog = false },
            content = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    GameButton(text = "Rename", onClick = {
                        newName = selectedFighter!!.name
                        showRenameDialog = true
                        showOptionsDialog = false
                    })
                    Spacer(modifier = Modifier.height(16.dp))
                    GameButton(text = "Delete", onClick = {
                        showDeleteConfirmDialog = true
                        showOptionsDialog = false
                    })
                }
            }
        )
    }

    // Rename Dialog
    if (showRenameDialog && selectedFighter != null) {
        GameDialog(
            title = "Rename Fighter",
            onDismissRequest = { showRenameDialog = false },
            content = {
                GameTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = "New Name"
                )
            },
            confirmButton = {
                GameButton(text = "Save", onClick = {
                    if (newName.isNotBlank()) {
                        viewModel.updateFighter(selectedFighter!!.copy(name = newName))
                        showRenameDialog = false
                    }
                })
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog && selectedFighter != null) {
        GameDialog(
            title = "Delete ${selectedFighter!!.name}?",
            onDismissRequest = { showDeleteConfirmDialog = false },
            content = { Text("This action cannot be undone.") },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    GameButton(text = "Cancel", onClick = { showDeleteConfirmDialog = false })
                    Spacer(modifier = Modifier.width(16.dp))
                    GameButton(text = "Delete", onClick = {
                        viewModel.deleteFighter(selectedFighter!!)
                        showDeleteConfirmDialog = false
                    })
                }
            }
        )
    }
}

@Composable
fun FighterCard(fighter: Fighter, medalColor: Color?, onClick: () -> Unit, onNotePlayed: (Float) -> Unit) {
    val colors = remember(fighter.barcode) {
        FighterStatsGenerator.generateColorSignature(fighter.barcode)
    }
    val musicalSignature = remember(fighter.barcode) {
        MusicUtils.generateMusicalSignature(fighter.barcode)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = medalColor?.let { BorderStroke(4.dp, it) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // This inner Column now contains the clickable area for editing.
            Column(modifier = Modifier.clickable(onClick = onClick)) {
                Text(
                    text = fighter.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Stats are now in a single Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "HP: ${fighter.hp}")
                    Text(text = "AT: ${fighter.attack}")
                    Text(text = "DE: ${fighter.defense}")
                    Text(text = "SP: ${fighter.speed}")
                    Text(text = "LU: ${fighter.luck}")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Text(text = "Wins: ${fighter.wins}", fontWeight = FontWeight.Bold, color = Gold)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "Losses: ${fighter.losses}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // The ColorSignature is now outside the clickable area.
            ColorSignature(
                colors = colors,
                onColorBarClick = { index ->
                    onNotePlayed(musicalSignature[index])
                }
            )
        }
    }
}
