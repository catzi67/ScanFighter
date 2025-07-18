package com.catto.scanfighter.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.catto.scanfighter.ui.theme.Fighter1Color
import com.catto.scanfighter.ui.theme.Fighter2Color
import com.catto.scanfighter.ui.theme.Gold
import com.catto.scanfighter.ui.theme.Purple40
import com.catto.scanfighter.ui.theme.ScanFighterRed
import com.catto.scanfighter.utils.FighterStatsGenerator
import com.catto.scanfighter.utils.MusicUtils
import com.catto.scanfighter.utils.SoundPlayer
import com.catto.scanfighter.utils.viewmodels.BattleViewModel
import com.catto.scanfighter.utils.viewmodels.MessageSource
import kotlinx.coroutines.launch

@Composable
fun BattleScreen(
    navController: NavController,
    repository: FighterRepository,
    fighter1Id: Int,
    fighter2Id: Int
) {
    val context = LocalContext.current.applicationContext
    val battleViewModel: BattleViewModel = viewModel(
        factory = BattleViewModel.BattleViewModelFactory(
            repository,
            fighter1Id,
            fighter2Id,
            context
        )
    )

    val battleState by battleViewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Screen shake animation
    val shakeController = remember { Animatable(0f) }
    LaunchedEffect(battleState.screenShakeTrigger) {
        if (battleState.screenShakeTrigger > 0) {
            coroutineScope.launch {
                shakeController.animateTo(
                    targetValue = 0f,
                    animationSpec = keyframes {
                        durationMillis = 500
                        -10f at 50
                        10f at 100
                        -10f at 150
                        10f at 200
                        -5f at 250
                        5f at 300
                        0f at 350
                    }
                )
            }
        }
    }

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
                    .offset(x = shakeController.value.dp, y = 0.dp)
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
                    val fighter1Status = when {
                        battleState.isBattleOver && battleState.winner?.id == battleState.fighter1?.fighter?.id -> "Victorious"
                        battleState.isBattleOver && battleState.winner?.id != battleState.fighter1?.fighter?.id -> "Defeated"
                        else -> null
                    }
                    FighterStatus(fighter = battleState.fighter1, titleColor = Fighter1Color, battleOutcome = fighter1Status)
                    Text(
                        text = "VS",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    val fighter2Status = when {
                        battleState.isBattleOver && battleState.winner?.id == battleState.fighter2?.fighter?.id -> "Victorious"
                        battleState.isBattleOver && battleState.winner?.id != battleState.fighter2?.fighter?.id -> "Defeated"
                        else -> null
                    }
                    FighterStatus(fighter = battleState.fighter2, titleColor = Fighter2Color, battleOutcome = fighter2Status)
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
fun FighterStatus(fighter: BattleViewModel.BattleFighter?, titleColor: Color, battleOutcome: String?) {
    val soundPlayer = remember { SoundPlayer() }

    fighter?.let { battleFighter ->
        val colors = remember(battleFighter.fighter.barcode) {
            FighterStatsGenerator.generateColorSignature(battleFighter.fighter.barcode)
        }
        val musicalSignature = remember(colors) {
            MusicUtils.generateMusicalSignature(colors)
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
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(8.dp)
        ) {
            Box(
                modifier = Modifier.height(60.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = battleFighter.fighter.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    color = titleColor
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "HP: ${battleFighter.currentHp}/${battleFighter.fighter.health}", fontSize = 14.sp)
            HealthBar(currentHp = battleFighter.currentHp, maxHp = battleFighter.fighter.health)
            Spacer(modifier = Modifier.height(8.dp))
            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                Text(text = "ATK: ${battleFighter.fighter.attack}", fontSize = 12.sp)
                Text(text = "DEF: ${battleFighter.fighter.defense}", fontSize = 12.sp)
                Text(text = "SPD: ${battleFighter.fighter.speed}", fontSize = 12.sp)
                Text(text = "SKL: ${battleFighter.fighter.skill}", fontSize = 12.sp)
                Text(text = "LUK: ${battleFighter.fighter.luck}", fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Status Effects Display Box
            Box(modifier = Modifier.height(40.dp), contentAlignment = Alignment.Center) {
                when {
                    battleOutcome != null -> {
                        Text(
                            text = battleOutcome,
                            color = if (battleOutcome == "Victorious") Gold else ScanFighterRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                    battleFighter.isStunned -> {
                        Text(text = "STUNNED", color = ScanFighterRed, fontWeight = FontWeight.Bold)
                    }
                    battleFighter.isPoisoned -> {
                        Text(text = "POISONED", color = Color.Magenta, fontWeight = FontWeight.Bold)
                    }
                    else -> {
                        Text(text = "", fontWeight = FontWeight.Bold) // Placeholder for alignment
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
        animationSpec = tween(durationMillis = 500),
        label = "HealthBarProgress"
    )

    val barColor = when {
        progress > 0.5f -> Color.Green
        progress > 0.2f -> Color.Yellow
        else -> ScanFighterRed
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
fun BattleLog(log: List<BattleViewModel.BattleLogEntry>, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()
    LaunchedEffect(log.size) {
        if (log.isNotEmpty()) {
            listState.animateScrollToItem(log.size - 1)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        items(log) { entry ->
            val arrangement = when (entry.source) {
                MessageSource.FIGHTER1 -> Arrangement.Start
                MessageSource.FIGHTER2 -> Arrangement.End
                MessageSource.SYSTEM -> Arrangement.Center
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = arrangement
            ) {
                val cardModifier = Modifier.fillMaxWidth(if (entry.source == MessageSource.SYSTEM) 1f else 0.8f)
                val border = when (entry.source) {
                    MessageSource.FIGHTER1 -> BorderStroke(2.dp, Fighter1Color)
                    MessageSource.FIGHTER2 -> BorderStroke(2.dp, Fighter2Color)
                    else -> null
                }

                Card(
                    modifier = cardModifier,
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (entry.source) {
                            MessageSource.FIGHTER1, MessageSource.FIGHTER2 -> Color.Black
                            MessageSource.SYSTEM -> MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = border
                ) {
                    Text(
                        text = entry.message,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 14.sp,
                        color = entry.color,
                        fontWeight = entry.fontWeight,
                        fontStyle = entry.fontStyle,
                        textAlign = if (entry.source == MessageSource.SYSTEM) TextAlign.Center else TextAlign.Start
                    )
                }
            }
        }
    }
}
