package com.catto.scanfighter.utils.viewmodels

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.catto.scanfighter.data.Fighter
import com.catto.scanfighter.data.FighterRepository
import com.catto.scanfighter.ui.theme.Fighter1Color
import com.catto.scanfighter.ui.theme.Fighter2Color
import com.catto.scanfighter.ui.theme.ScanFighterYellow
import com.catto.scanfighter.utils.BattleSoundPlayer
import com.catto.scanfighter.utils.FighterStatsGenerator
import com.catto.scanfighter.utils.MusicUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class BattleViewModel(
    private val repository: FighterRepository,
    private val fighter1Id: Int,
    private val fighter2Id: Int
) : ViewModel() {

    private val battleSoundPlayer = BattleSoundPlayer()

    data class BattleLogEntry(
        val message: String,
        val color: Color,
        val fontWeight: FontWeight = FontWeight.Normal,
        val fontStyle: FontStyle? = null
    )

    data class BattleFighter(
        val fighter: Fighter,
        var currentHp: Int,
        var isStunned: Boolean = false
    )

    data class BattleUiState(
        val isLoading: Boolean = true,
        val fighter1: BattleFighter? = null,
        val fighter2: BattleFighter? = null,
        val battleLog: List<BattleLogEntry> = emptyList(),
        val isBattleOver: Boolean = false,
        val winner: Fighter? = null,
        val isFighter1Turn: Boolean = true
    )

    private val _uiState = MutableStateFlow(BattleUiState())
    val uiState: StateFlow<BattleUiState> = _uiState.asStateFlow()

    init {
        loadFighters()
    }

    private fun loadFighters() {
        viewModelScope.launch {
            val f1 = repository.getFighterById(fighter1Id)
            val f2 = repository.getFighterById(fighter2Id)

            if (f1 != null && f2 != null) {
                val battleFighter1 = BattleFighter(f1, f1.health)
                val battleFighter2 = BattleFighter(f2, f2.health)
                val isFighter1Turn = f1.speed >= f2.speed
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        fighter1 = battleFighter1,
                        fighter2 = battleFighter2,
                        isFighter1Turn = isFighter1Turn,
                        battleLog = listOf(
                            BattleLogEntry(
                                "The battle between ${f1.name} and ${f2.name} begins!",
                                Color.White
                            )
                        )
                    )
                }
            }
        }
    }

    fun runFullBattle() {
        viewModelScope.launch {
            while (!_uiState.value.isBattleOver) {
                nextTurn()
                delay(2000) // Delay between turns for readability
            }
            val winner = _uiState.value.winner
            if (winner != null) {
                val winnerName = winner.name
                val finalMessage = "--- Battle Over! $winnerName is victorious! ---"
                _uiState.update {
                    it.copy(battleLog = it.battleLog + BattleLogEntry(finalMessage, ScanFighterYellow, FontWeight.Bold))
                }
                val colors = FighterStatsGenerator.generateColorSignature(winner.barcode)
                val musicalSignature = MusicUtils.generateMusicalSignature(colors)
                battleSoundPlayer.playVictorySignature(musicalSignature)
            }
        }
    }

    private fun saveBattleResult(winner: Fighter, loser: Fighter) {
        viewModelScope.launch {
            val updatedWinner = winner.copy(wins = winner.wins + 1)
            repository.updateFighter(updatedWinner)
            val updatedLoser = loser.copy(losses = loser.losses + 1)
            repository.updateFighter(updatedLoser)
        }
    }

    private fun nextTurn() {
        val currentState = _uiState.value
        if (currentState.isBattleOver || currentState.isLoading) return

        val attacker = if (currentState.isFighter1Turn) currentState.fighter1!! else currentState.fighter2!!
        val defender = if (currentState.isFighter1Turn) currentState.fighter2!! else currentState.fighter1!!
        val attackerColor = if (currentState.isFighter1Turn) Fighter1Color else Fighter2Color
        val defenderColor = if (currentState.isFighter1Turn) Fighter2Color else Fighter1Color

        val newLog = mutableListOf<BattleLogEntry>()

        if (attacker.isStunned) {
            val stunMessages = listOf(
                "${attacker.fighter.name} is stunned and can't move!",
                "${attacker.fighter.name} is seeing stars and skips a turn!",
                "${attacker.fighter.name} is immobilized by the last hit!"
            )
            newLog.add(BattleLogEntry(stunMessages.random(), attackerColor, fontWeight = FontWeight.Bold))
            battleSoundPlayer.playStunSound()
            attacker.isStunned = false
        } else {
            val hitChance = (attacker.fighter.speed.toFloat() / (attacker.fighter.speed + defender.fighter.speed)) * 100 + 20
            if (Random.nextInt(100) < hitChance) {
                val isCritical = Random.nextInt(100) < (attacker.fighter.skill)
                val damageMultiplier = if (isCritical) 1.5 else 1.0
                val baseDamage = attacker.fighter.attack
                val defenseReduction = defender.fighter.defense * 0.5
                var damage = (baseDamage * damageMultiplier - defenseReduction).toInt().coerceAtLeast(1)

                if (isCritical) {
                    val criticalHitMessages = listOf(
                        "CRITICAL HIT! ${attacker.fighter.name} attacks with fury!",
                        "A devastating blow from ${attacker.fighter.name}!",
                        "Right on the mark! ${attacker.fighter.name} lands a critical strike!"
                    )
                    newLog.add(BattleLogEntry(criticalHitMessages.random(), attackerColor, FontWeight.Bold))
                    battleSoundPlayer.playCriticalHitSound()
                }

                val blockChance = defender.fighter.defense / 2
                if (Random.nextInt(100) < blockChance) {
                    damage /= 2
                    val blockMessages = listOf(
                        "${defender.fighter.name} blocks part of the attack!",
                        "${defender.fighter.name} weathers the storm, taking reduced damage!",
                        "A solid defense from ${defender.fighter.name}!"
                    )
                    newLog.add(BattleLogEntry(blockMessages.random(), defenderColor))
                    battleSoundPlayer.playBlockSound()
                } else {
                    battleSoundPlayer.playHitSound()
                }

                defender.currentHp = (defender.currentHp - damage).coerceAtLeast(0)
                val attackMessages = listOf(
                    "${attacker.fighter.name} hits ${defender.fighter.name} for $damage damage.",
                    "${attacker.fighter.name} strikes true, dealing $damage damage to ${defender.fighter.name}!",
                    "${defender.fighter.name} reels from a solid hit from ${attacker.fighter.name} for $damage damage!"
                )
                newLog.add(BattleLogEntry(attackMessages.random(), attackerColor))

                if (isCritical && Random.nextInt(100) < 25) {
                    defender.isStunned = true
                    val stunMessages = listOf(
                        "${defender.fighter.name} is stunned by the powerful blow!",
                        "The critical hit leaves ${defender.fighter.name} dazed and confused!",
                        "${defender.fighter.name} is staggered and seeing double!"
                    )
                    newLog.add(BattleLogEntry(stunMessages.random(), defenderColor, fontWeight = FontWeight.Bold))
                    battleSoundPlayer.playStunSound()
                }
            } else {
                val dodgeMessages = listOf(
                    "${attacker.fighter.name} attacks, but ${defender.fighter.name} dodges!",
                    "A swing and a miss by ${attacker.fighter.name}!",
                    "${defender.fighter.name} is too quick for ${attacker.fighter.name}'s attack!"
                )
                newLog.add(BattleLogEntry(dodgeMessages.random(), attackerColor, fontStyle = FontStyle.Italic))
            }
        }

        val isBattleOver = defender.currentHp <= 0 || attacker.currentHp <= 0
        var winnerFighter: Fighter? = null

        if (isBattleOver) {
            winnerFighter = if (defender.currentHp <= 0) {
                saveBattleResult(attacker.fighter, defender.fighter)
                attacker.fighter
            } else {
                saveBattleResult(defender.fighter, attacker.fighter)
                defender.fighter
            }
        }

        _uiState.update {
            it.copy(
                battleLog = it.battleLog + newLog,
                isFighter1Turn = !it.isFighter1Turn,
                isBattleOver = isBattleOver,
                winner = winnerFighter
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    class BattleViewModelFactory(
        private val repository: FighterRepository,
        private val fighter1Id: Int,
        private val fighter2Id: Int
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BattleViewModel::class.java)) {
                return BattleViewModel(repository, fighter1Id, fighter2Id) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
