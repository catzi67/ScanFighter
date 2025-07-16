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
import androidx.compose.ui.draw.clip
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
import com.catto.scanfighter.ui.theme.Purple40
import com.catto.scanfighter.ui.theme.Silver
import com.catto.scanfighter.utils.FighterStatsGenerator
import com.catto.scanfighter.utils.MusicUtils
import com.catto.scanfighter.utils.SoundPlayer
import com.catto.scanfighter.utils.viewmodels.FighterViewModel

@Composable
fun LeaderboardScreen(navController: NavController, viewModel: FighterViewModel) {
    val fighters by viewModel.allFighters.collectAsState(initial = emptyList())
    var selectedFighters by remember { mutableStateOf<List<Fighter>>(emptyList()) }
    val soundPlayer = remember { SoundPlayer() }

    // State for the management dialogs
    var fighterToManage by remember { mutableStateOf<Fighter?>(null) }
    var showOptionsDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

    fun selectFighterForBattle(fighter: Fighter) {
        if (selectedFighters.any { it.id == fighter.id }) {
            selectedFighters = selectedFighters.filter { it.id != fighter.id }
        } else {
            if (selectedFighters.size < 2) {
                selectedFighters = selectedFighters + fighter
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
                text = "Select two fighters to battle",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
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
                    val isSelected = selectedFighters.any { it.id == fighter.id }
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
                    if (selectedFighters.size == 2) {
                        navController.navigate(
                            Screen.Battle.createRoute(
                                selectedFighters[0].id,
                                selectedFighters[1].id
                            )
                        )
                    }
                },
                enabled = selectedFighters.size == 2
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
    val musicalSignature = remember(fighter.barcode) {
        MusicUtils.generateMusicalSignature(fighter.barcode)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Purple40.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(
            width = if (isSelected) 3.dp else 1.dp,
            color = medalColor ?: if (isSelected) Purple40 else Color.Gray.copy(alpha = 0.5f)
        )
    ) {
        // The main content area is clickable and long-clickable
        Column(
            modifier = Modifier
                .clip(CardDefaults.shape)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .padding(16.dp)
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
        // The ColorSignature is outside the clickable area
        ColorSignature(
            colors = colors,
            onColorBarClick = { index ->
                onNotePlayed(musicalSignature[index])
            }
        )
    }
}
