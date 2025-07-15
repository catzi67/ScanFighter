// app/src/main/java/com/catto/scanfighter/ui/screens/FighterSelectionScreen.kt
package com.catto.scanfighter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.catto.scanfighter.data.Fighter
import com.catto.scanfighter.navigation.Screen
import com.catto.scanfighter.ui.components.GameButton
import com.catto.scanfighter.ui.theme.Purple40
import com.catto.scanfighter.ui.theme.PurpleGrey40
import com.catto.scanfighter.utils.viewmodels.FighterViewModel

@Composable
fun FighterSelectionScreen(
    navController: NavController,
    fighterViewModel: FighterViewModel
) {
    val fighters by fighterViewModel.allFighters.collectAsState(initial = emptyList())
    var selectedFighters by remember { mutableStateOf<List<Fighter>>(emptyList()) }

    fun selectFighter(fighter: Fighter) {
        if (selectedFighters.contains(fighter)) {
            selectedFighters = selectedFighters.filter { it != fighter }
        } else {
            if (selectedFighters.size < 2) {
                selectedFighters = selectedFighters + fighter
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Select Two Fighters",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(fighters) { fighter ->
                val isSelected = selectedFighters.contains(fighter)
                FighterRow(
                    fighter = fighter,
                    isSelected = isSelected,
                    onFighterSelected = { selectFighter(it) }
                )
                Spacer(modifier = Modifier.height(8.dp))
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
        Spacer(modifier = Modifier.height(8.dp))
        GameButton(
            text = "Back",
            onClick = { navController.popBackStack() }
        )
    }
}

@Composable
fun FighterRow(
    fighter: Fighter,
    isSelected: Boolean,
    onFighterSelected: (Fighter) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onFighterSelected(fighter) }
            .background(
                if (isSelected) Purple40.copy(alpha = 0.5f) else PurpleGrey40.copy(alpha = 0.2f),
                shape = MaterialTheme.shapes.medium
            )
            .border(
                width = 2.dp,
                color = if (isSelected) Purple40 else Color.Transparent,
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = fighter.name,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "ATK: ${fighter.attack}", fontSize = 14.sp)
                Text(text = "DEF: ${fighter.defense}", fontSize = 14.sp)
                Text(text = "SPD: ${fighter.speed}", fontSize = 14.sp)
            }
        }
    }
}
