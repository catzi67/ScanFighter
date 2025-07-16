package com.catto.scanfighter.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.catto.scanfighter.data.FighterRepository
import com.catto.scanfighter.ui.components.ColorSignature
import com.catto.scanfighter.ui.navigation.Screen
import com.catto.scanfighter.ui.components.GameButton
import com.catto.scanfighter.ui.theme.Gold
import com.catto.scanfighter.ui.theme.Purple40
import com.catto.scanfighter.utils.FighterStatsGenerator
import com.catto.scanfighter.utils.MusicUtils
import com.catto.scanfighter.utils.SoundPlayer
import com.catto.scanfighter.utils.viewmodels.BattleViewModel
import kotlinx.coroutines.launch

@Composable
fun BattleScreen(
    navController: NavController,
    repository: FighterRepository,
    fighter1Id: Int,
    fighter2Id: Int
) {
    val battleViewModel: BattleViewModel = viewModel(
        factory = BattleViewModel.BattleViewModelFactory(
            repository,
            fighter1Id,
            fighter2Id
        )
    )

    val battleState by battleViewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(battleState.isLoading, battleState.isBattleOver) {
        if (!battleState.isLoading && !battleState.isBattleOver) {
            battleViewModel.runFullBattle()
        }
    }

    LaunchedEffect(battleState.battleLog.size) {
        if (battleState.battleLog.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(battleState.battleLog.size - 1)
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (battleState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Preparing for battle...", fontSize = 20.sp)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.Top
                ) {
                    val isFighter1Winner = battleState.isBattleOver && battleState.winner?.id == battleState.fighter1?.fighter?.id
                    val isFighter2Winner = battleState.isBattleOver && battleState.winner?.id == battleState.fighter2?.fighter?.id

                    FighterStatus(fighter = battleState.fighter1, isWinner = isFighter1Winner)
                    Text(
                        text = "VS",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    FighterStatus(fighter = battleState.fighter2, isWinner = isFighter2Winner)
                }
                Spacer(modifier = Modifier.height(24.dp))

                BattleLog(log = battleState.battleLog, modifier = Modifier.weight(1f))

                Spacer(modifier = Modifier.height(16.dp))

                if (battleState.isBattleOver) {
                    GameButton(
                        text = "Back to Menu",
                        onClick = {
                            navController.navigate(Screen.MainMenu.route) {
                                popUpTo(Screen.MainMenu.route) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FighterStatus(fighter: BattleViewModel.BattleFighter?, isWinner: Boolean) {
    val soundPlayer = remember { SoundPlayer() }

    fighter?.let {
        val colors = remember(it.fighter.barcode) {
            FighterStatsGenerator.generateColorSignature(it.fighter.barcode)
        }
        val musicalSignature = remember(it.fighter.barcode) {
            MusicUtils.generateMusicalSignature(it.fighter.barcode)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(150.dp)
                .fillMaxHeight()
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.medium
                )
                .border(
                    width = 2.dp,
                    color = it.color,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(8.dp)
        ) {
            Box(
                modifier = Modifier.height(60.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = it.fighter.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    color = it.color
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "HP: ${it.currentHp}/${it.fighter.health}", fontSize = 14.sp)
            HealthBar(currentHp = it.currentHp, maxHp = it.fighter.health)
            Spacer(modifier = Modifier.height(8.dp))
            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                Text(text = "ATK: ${it.fighter.attack + it.attackModifier}", fontSize = 12.sp)
                Text(text = "DEF: ${it.fighter.defense + it.defenseModifier}", fontSize = 12.sp)
                Text(text = "SPD: ${it.fighter.speed}", fontSize = 12.sp)
                Text(text = "SKL: ${it.fighter.skill}", fontSize = 12.sp)
                Text(text = "LUK: ${it.fighter.luck}", fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Status effects display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.height(80.dp)
            ) {
                if (it.currentHp <= 0) {
                    Text(text = "DEFEATED", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                } else if (isWinner) {
                    Text(text = "VICTORIOUS!", color = Gold, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                else {
                    if (it.stunnedForRounds > 0) {
                        Text(text = "STUNNED (${it.stunnedForRounds})", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                    if (it.isEnraged) {
                        Text(text = "ENRAGED!", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                    if (it.isFocused) {
                        Text(text = "FOCUSED!", color = Color.Cyan, fontWeight = FontWeight.Bold)
                    }
                    if (it.bleedRounds > 0) {
                        Text(text = "BLEEDING (${it.bleedRounds})", color = Color(0xFF880808), fontWeight = FontWeight.Bold)
                    }
                    if (it.attackModifier < 0) {
                        Text(text = "ATK DOWN", color = Color.Yellow, fontWeight = FontWeight.Bold)
                    }
                    if (it.defenseModifier < 0) {
                        Text(text = "DEF DOWN", color = Color.Yellow, fontWeight = FontWeight.Bold)
                    }
                }
            }


            Spacer(Modifier.weight(1f))

            ColorSignature(
                colors = colors,
                onColorBarClick = { index ->
                    soundPlayer.playNote(musicalSignature[index])
                }
            )
        }
    }
}

@Composable
fun HealthBar(currentHp: Int, maxHp: Int) {
    val progress = (currentHp.toFloat() / maxHp.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500)
    )

    val barColor = when {
        progress > 0.5f -> Color.Green
        progress > 0.2f -> Color.Yellow
        else -> Color.Red
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
            .border(1.dp, Purple40, MaterialTheme.shapes.small)
    ) {
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxSize(),
            color = barColor,
            trackColor = Color.Transparent
        )
    }
}

@Composable
fun BattleLog(log: List<Pair<String, Color>>, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()
    LaunchedEffect(log.size) {
        if (log.isNotEmpty()) {
            listState.animateScrollToItem(log.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black, shape = MaterialTheme.shapes.medium)
            .border(1.dp, Purple40, MaterialTheme.shapes.medium)
            .padding(8.dp)
    ) {
        LazyColumn(state = listState) {
            items(log) { (message, color) ->
                Text(
                    text = message,
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                    fontSize = 14.sp,
                    color = color
                )
            }
        }
    }
}
