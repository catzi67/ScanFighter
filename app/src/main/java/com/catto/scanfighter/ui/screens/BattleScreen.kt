// app/src/main/java/com/catto/scanfighter/ui/screens/BattleScreen.kt
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
import com.catto.scanfighter.data.Fighter
import com.catto.scanfighter.navigation.Screen
import com.catto.scanfighter.ui.components.GameButton
import com.catto.scanfighter.ui.components.GameDialog
import com.catto.scanfighter.ui.theme.Purple40
import com.catto.scanfighter.ui.theme.PurpleGrey40
import com.catto.scanfighter.utils.viewmodels.BattleViewModel
import com.catto.scanfighter.utils.viewmodels.FighterViewModel
import kotlinx.coroutines.launch

@Composable
fun BattleScreen(
    navController: NavController,
    fighterViewModel: FighterViewModel,
    fighter1Id: Int,
    fighter2Id: Int
) {
    val battleViewModel: BattleViewModel = viewModel(
        factory = BattleViewModel.BattleViewModelFactory(
            fighterViewModel.repository,
            fighter1Id,
            fighter2Id
        )
    )

    val battleState by battleViewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(battleState.battleLog.size) {
        if (battleState.battleLog.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(battleState.battleLog.size - 1)
            }
        }
    }

    if (battleState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Preparing for battle...", fontSize = 20.sp)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Top
            ) {
                FighterStatus(fighter = battleState.fighter1)
                Text(
                    text = "VS",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                FighterStatus(fighter = battleState.fighter2)
            }
            Spacer(modifier = Modifier.height(24.dp))

            BattleLog(log = battleState.battleLog, modifier = Modifier.weight(1f))

            Spacer(modifier = Modifier.height(16.dp))

            if (!battleState.isBattleOver) {
                GameButton(
                    text = "Next Turn",
                    onClick = { battleViewModel.nextTurn() }
                )
            }

            if (battleState.isBattleOver) {
                GameDialog(
                    title = "Battle Over!",
                    message = "${battleState.winner?.name ?: "No one"} is victorious!",
                    onDismiss = { /* Can't dismiss */ },
                    onConfirm = {
                        navController.navigate(Screen.MainMenu.route) {
                            popUpTo(Screen.MainMenu.route) { inclusive = true }
                        }
                    },
                    confirmText = "Back to Menu"
                )
            }
        }
    }
}

@Composable
fun FighterStatus(fighter: BattleViewModel.BattleFighter?) {
    fighter?.let {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(150.dp)
        ) {
            Text(
                text = it.fighter.name,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "HP: ${it.currentHp}/${it.fighter.health}", fontSize = 14.sp)
            HealthBar(currentHp = it.currentHp, maxHp = it.fighter.health)
            Spacer(modifier = Modifier.height(8.dp))
            Column(horizontalAlignment = Alignment.Start) {
                Text(text = "ATK: ${it.fighter.attack}", fontSize = 12.sp)
                Text(text = "DEF: ${it.fighter.defense}", fontSize = 12.sp)
                Text(text = "SPD: ${it.fighter.speed}", fontSize = 12.sp)
            }
            if (it.isStunned) {
                Text(text = "STUNNED", color = Color.Red, fontWeight = FontWeight.Bold)
            }
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
            .background(PurpleGrey40)
            .border(1.dp, Purple40, MaterialTheme.shapes.small)
    ) {
        LinearProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier.fillMaxSize(),
            color = barColor,
            trackColor = Color.Transparent
        )
    }
}

@Composable
fun BattleLog(log: List<String>, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()
    LaunchedEffect(log.size) {
        if (log.isNotEmpty()) {
            listState.animateScrollToItem(log.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(PurpleGrey40.copy(alpha = 0.2f), shape = MaterialTheme.shapes.medium)
            .border(1.dp, Purple40, MaterialTheme.shapes.medium)
            .padding(8.dp)
    ) {
        LazyColumn(state = listState) {
            items(log) { message ->
                Text(
                    text = message,
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                    fontSize = 14.sp
                )
            }
        }
    }
}
