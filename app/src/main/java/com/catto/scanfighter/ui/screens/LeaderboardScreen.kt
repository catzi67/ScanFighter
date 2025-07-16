package com.catto.scanfighter.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.navigation.NavController
import com.catto.scanfighter.data.Fighter
import com.catto.scanfighter.ui.components.ColorSignature
import com.catto.scanfighter.ui.components.GameButton
import com.catto.scanfighter.ui.components.GameDialog
import com.catto.scanfighter.ui.components.GameTextField
import com.catto.scanfighter.ui.navigation.Screen
import com.catto.scanfighter.ui.theme.Bronze
import com.catto.scanfighter.ui.theme.Gold
import com.catto.scanfighter.ui.theme.Silver
import com.catto.scanfighter.utils.FighterStatsGenerator
import com.catto.scanfighter.utils.MusicUtils
import com.catto.scanfighter.utils.SoundPlayer
import com.catto.scanfighter.utils.viewmodels.FighterViewModel

@Composable
fun LeaderboardScreen(navController: NavController, viewModel: FighterViewModel) {
    val fighters by viewModel.allFighters.collectAsState(initial = emptyList())
    var selectedFightersForBattle by remember { mutableStateOf<List<Fighter>>(emptyList()) }
    var fighterToManage by remember { mutableStateOf<Fighter?>(null) }
    var showOptionsDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    val soundPlayer = remember { SoundPlayer() }

    fun selectFighterForBattle(fighter: Fighter) {
        if (selectedFightersForBattle.contains(fighter)) {
            selectedFightersForBattle = selectedFightersForBattle - fighter
        } else {
            if (selectedFightersForBattle.size < 2) {
                selectedFightersForBattle = selectedFightersForBattle + fighter
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Leaderboard",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Choose two fighters to battle",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            LazyColumn(modifier = Modifier.weight(1f)) {
                itemsIndexed(items = fighters) { index, fighter ->
                    val medalColor = when (index) {
                        0 -> Gold
                        1 -> Silver
                        2 -> Bronze
                        else -> null
                    }
                    val isSelected = selectedFightersForBattle.contains(fighter)
                    FighterCard(
                        fighter = fighter,
                        medalColor = medalColor,
                        isSelected = isSelected,
                        onClick = { selectFighterForBattle(fighter) },
                        onLongClick = {
                            fighterToManage = fighter
                            showOptionsDialog = true
                        },
                        onNotePlayed = { frequency ->
                            soundPlayer.playNote(frequency)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            GameButton(
                text = "Start Battle",
                onClick = {
                    if (selectedFightersForBattle.size == 2) {
                        navController.navigate(
                            Screen.Battle.createRoute(
                                selectedFightersForBattle[0].id,
                                selectedFightersForBattle[1].id
                            )
                        )
                    }
                },
                enabled = selectedFightersForBattle.size == 2
            )
        }
    }

    // Fighter Management Options Dialog
    if (showOptionsDialog && fighterToManage != null) {
        GameDialog(
            title = "Manage ${fighterToManage!!.name}",
            onDismissRequest = { showOptionsDialog = false },
            content = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    GameButton(text = "Rename", onClick = {
                        newName = fighterToManage!!.name
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
    if (showRenameDialog && fighterToManage != null) {
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
                        viewModel.updateFighter(fighterToManage!!.copy(name = newName))
                        showRenameDialog = false
                    }
                })
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog && fighterToManage != null) {
        GameDialog(
            title = "Delete ${fighterToManage!!.name}?",
            onDismissRequest = { showDeleteConfirmDialog = false },
            content = { Text("This action cannot be undone.") },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    GameButton(text = "Cancel", onClick = { showDeleteConfirmDialog = false })
                    Spacer(modifier = Modifier.width(16.dp))
                    GameButton(text = "Delete", onClick = {
                        viewModel.deleteFighter(fighterToManage!!)
                        showDeleteConfirmDialog = false
                    })
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FighterCard(
    fighter: Fighter,
    medalColor: Color?,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onNotePlayed: (Float) -> Unit
) {
    val colors = remember(fighter.barcode) {
        FighterStatsGenerator.generateColorSignature(fighter.barcode)
    }
    val musicalSignature = remember(colors) {
        MusicUtils.generateMusicalSignature(colors)
    }

    val border = when {
        isSelected -> BorderStroke(4.dp, MaterialTheme.colorScheme.primary)
        medalColor != null -> BorderStroke(4.dp, medalColor)
        else -> null
    }

    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = border
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // This inner Column is now the only clickable area for management/selection.
            Column(
                modifier = Modifier.combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
            ) {
                Text(
                    text = fighter.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "HP: ${fighter.health}")
                    Text(text = "ATK: ${fighter.attack}")
                    Text(text = "DEF: ${fighter.defense}")
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "SPD: ${fighter.speed}")
                    Text(text = "SKL: ${fighter.skill}")
                    Text(text = "LUK: ${fighter.luck}")
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
