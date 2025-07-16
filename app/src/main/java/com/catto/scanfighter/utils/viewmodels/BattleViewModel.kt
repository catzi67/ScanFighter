package com.catto.scanfighter.utils.viewmodels

import android.content.Context
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

enum class MessageSource {
    FIGHTER1,
    FIGHTER2,
    SYSTEM
}

class BattleViewModel(
    private val repository: FighterRepository,
    private val fighter1Id: Int,
    private val fighter2Id: Int,
    context: Context // Added context to pass to BattleSoundPlayer
) : ViewModel() {

    private val battleSoundPlayer: BattleSoundPlayer

    data class BattleLogEntry(
        val message: String,
        val color: Color,
        val fontWeight: FontWeight = FontWeight.Normal,
        val fontStyle: FontStyle? = null,
        val source: MessageSource
    )

    data class BattleFighter(
        val fighter: Fighter,
        var currentHp: Int,
        var isStunned: Boolean = false,
        var isPoisoned: Boolean = false,
        var poisonTurns: Int = 0,
        var attackModifier: Int = 0,
        var defenseModifier: Int = 0,
        var specialMoveCooldown: Int = 0,
        var turnsUntilRegenEnds: Int = 0
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
        // Instantiate BattleSoundPlayer with the provided context
        battleSoundPlayer = BattleSoundPlayer(context)
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
                                message = "The battle between ${f1.name} and ${f2.name} begins!",
                                color = Color.White,
                                source = MessageSource.SYSTEM
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
                delay(3000) // Delay between turns for readability
            }
            val winner = _uiState.value.winner
            if (winner != null) {
                val winnerName = winner.name
                val finalMessage = "--- Battle Over! $winnerName is victorious! ---"
                _uiState.update {
                    it.copy(battleLog = it.battleLog + BattleLogEntry(message = finalMessage, color = ScanFighterYellow, fontWeight = FontWeight.Bold, source = MessageSource.SYSTEM))
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

        val (attacker, defender) = if (currentState.isFighter1Turn) {
            currentState.fighter1!! to currentState.fighter2!!
        } else {
            currentState.fighter2!! to currentState.fighter1!!
        }
        val attackerColor = if (currentState.isFighter1Turn) Fighter1Color else Fighter2Color
        val defenderColor = if (currentState.isFighter1Turn) Fighter2Color else Fighter1Color
        val attackerSource = if (currentState.isFighter1Turn) MessageSource.FIGHTER1 else MessageSource.FIGHTER2


        val newLog = mutableListOf<BattleLogEntry>()

        // --- Pre-turn effects ---
        if (attacker.specialMoveCooldown > 0) attacker.specialMoveCooldown--

        if (attacker.isPoisoned) {
            val poisonDamage = (attacker.fighter.health * 0.05).toInt().coerceAtLeast(1)
            attacker.currentHp -= poisonDamage
            newLog.add(BattleLogEntry(message = "${attacker.fighter.name} takes $poisonDamage damage from poison!", color = attackerColor, fontStyle = FontStyle.Italic, source = attackerSource))
            battleSoundPlayer.playDebuffSound()
            attacker.poisonTurns--
            if (attacker.poisonTurns <= 0) {
                attacker.isPoisoned = false
                newLog.add(BattleLogEntry(message = "${attacker.fighter.name} is no longer poisoned.", color = attackerColor, source = attackerSource))
            }
        }

        if (attacker.turnsUntilRegenEnds > 0) {
            if (attacker.currentHp < attacker.fighter.health) {
                val healAmount = (attacker.fighter.health * 0.07).toInt().coerceAtLeast(1)
                attacker.currentHp = (attacker.currentHp + healAmount).coerceAtMost(attacker.fighter.health)
                newLog.add(BattleLogEntry(message = "${attacker.fighter.name} regenerates $healAmount HP!", color = attackerColor, source = attackerSource))
                battleSoundPlayer.playHealSound()
            }
            attacker.turnsUntilRegenEnds--
        }


        if (attacker.currentHp <= 0) {
            endBattle(defender, attacker, newLog)
            return
        }

        if (attacker.isStunned) {
            newLog.add(BattleLogEntry(message = "${attacker.fighter.name} is stunned and can't move!", color = attackerColor, fontWeight = FontWeight.Bold, source = attackerSource))
            battleSoundPlayer.playStunSound()
            attacker.isStunned = false
        } else {
            // --- Action Phase: AI decides to use special move or normal attack ---
            val useSpecial = attacker.specialMoveCooldown == 0 && Random.nextInt(100) < 40 // 40% chance
            if (useSpecial) {
                executeSpecialMove(attacker, defender, attackerColor, defenderColor, newLog)
            } else {
                executeNormalAttack(attacker, defender, attackerColor, defenderColor, newLog)
            }
        }

        if (defender.currentHp <= 0) {
            if (Random.nextInt(100) < defender.fighter.luck) {
                defender.currentHp = 1
                newLog.add(BattleLogEntry(message = "By a stroke of luck, ${defender.fighter.name} hangs on with 1 HP!", color = defenderColor, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic, source = MessageSource.SYSTEM))
                battleSoundPlayer.playFocusSound()
            } else {
                endBattle(attacker, defender, newLog)
                return
            }
        }

        val (updatedFighter1, updatedFighter2) = if (currentState.isFighter1Turn) {
            attacker to defender
        } else {
            defender to attacker
        }

        _uiState.update {
            it.copy(
                fighter1 = updatedFighter1.copy(),
                fighter2 = updatedFighter2.copy(),
                battleLog = it.battleLog + newLog,
                isFighter1Turn = !it.isFighter1Turn
            )
        }
    }

    private fun executeNormalAttack(attacker: BattleFighter, defender: BattleFighter, attackerColor: Color, defenderColor: Color, newLog: MutableList<BattleLogEntry>) {
        val attackerSource = if (_uiState.value.isFighter1Turn) MessageSource.FIGHTER1 else MessageSource.FIGHTER2
        val defenderSource = if (_uiState.value.isFighter1Turn) MessageSource.FIGHTER2 else MessageSource.FIGHTER1

        val hitChance = (attacker.fighter.speed.toFloat() / (attacker.fighter.speed + defender.fighter.speed)) * 100 + 20
        if (Random.nextInt(100) > hitChance && Random.nextInt(100) > attacker.fighter.luck) {
            newLog.add(BattleLogEntry(message = "${attacker.fighter.name} attacks, but ${defender.fighter.name} dodges!", color = attackerColor, fontStyle = FontStyle.Italic, source = attackerSource))
            return
        }

        val isCritical = Random.nextInt(100) < (attacker.fighter.skill + attacker.fighter.luck / 2)
        val damageMultiplier = if (isCritical) 1.5 else 1.0
        val baseDamage = attacker.fighter.attack + attacker.attackModifier
        val defenseReduction = (defender.fighter.defense + defender.defenseModifier) * 0.5
        var damage = (baseDamage * damageMultiplier - defenseReduction).toInt().coerceAtLeast(1)

        if (isCritical) {
            newLog.add(BattleLogEntry(message = "CRITICAL HIT!", color = attackerColor, fontWeight = FontWeight.Bold, source = attackerSource))
            battleSoundPlayer.playCriticalHitSound()
        }

        val blockChance = (defender.fighter.defense + defender.defenseModifier) / 2
        if (Random.nextInt(100) < blockChance) {
            damage /= 2
            newLog.add(BattleLogEntry(message = "${defender.fighter.name} blocks part of the attack!", color = defenderColor, source = defenderSource))
            battleSoundPlayer.playBlockSound()
        } else {
            battleSoundPlayer.playHitSound()
        }

        defender.currentHp -= damage
        newLog.add(BattleLogEntry(message = "${attacker.fighter.name} hits ${defender.fighter.name} for $damage damage.", color = attackerColor, source = attackerSource))

        if (isCritical && Random.nextInt(100) < 25) {
            defender.isStunned = true
            newLog.add(BattleLogEntry(message = "${defender.fighter.name} is stunned by the powerful blow!", color = defenderColor, fontWeight = FontWeight.Bold, source = defenderSource))
            battleSoundPlayer.playStunSound()
        }

        // Skill-based combo attack
        if (Random.nextInt(100) < attacker.fighter.skill) {
            val comboDamage = (baseDamage * 0.5).toInt().coerceAtLeast(1)
            defender.currentHp -= comboDamage
            newLog.add(BattleLogEntry(message = "COMBO! ${attacker.fighter.name} strikes again for $comboDamage damage!", color = attackerColor, fontWeight = FontWeight.Bold, source = attackerSource))
            battleSoundPlayer.playHitSound()
        }
    }

    private fun executeSpecialMove(attacker: BattleFighter, defender: BattleFighter, attackerColor: Color, defenderColor: Color, newLog: MutableList<BattleLogEntry>) {
        val attackerSource = if (_uiState.value.isFighter1Turn) MessageSource.FIGHTER1 else MessageSource.FIGHTER2
        val defenderSource = if (_uiState.value.isFighter1Turn) MessageSource.FIGHTER2 else MessageSource.FIGHTER1

        newLog.add(BattleLogEntry(message = "${attacker.fighter.name} uses ${attacker.fighter.specialMoveType.replace('_', ' ').uppercase()}!", color = attackerColor, fontWeight = FontWeight.Bold, source = attackerSource))
        attacker.specialMoveCooldown = 5 // Set cooldown for all special moves

        when (attacker.fighter.specialMoveType) {
            "power_attack" -> {
                battleSoundPlayer.playEnrageSound()
                val damage = (attacker.fighter.attack * 2 - defender.fighter.defense * 0.5).toInt().coerceAtLeast(5)
                defender.currentHp -= damage
                newLog.add(BattleLogEntry(message = "A massive blow deals $damage damage!", color = attackerColor, source = attackerSource))
            }
            "shield_up" -> {
                battleSoundPlayer.playFocusSound()
                attacker.defenseModifier += 20 // A significant but temporary boost
                newLog.add(BattleLogEntry(message = "${attacker.fighter.name} raises their defense!", color = attackerColor, source = attackerSource))
                // This modifier will wear off naturally as it's not persisted anywhere
            }
            "combo_strike" -> {
                newLog.add(BattleLogEntry(message = "A flurry of blows!", color = attackerColor, fontWeight = FontWeight.Bold, source = attackerSource))
                repeat(2) {
                    if(defender.currentHp > 0) executeNormalAttack(attacker, defender, attackerColor, defenderColor, newLog)
                }
            }
            "evasive_stance" -> {
                battleSoundPlayer.playFocusSound()
                if (attacker.currentHp < attacker.fighter.health) {
                    val healAmount = (attacker.fighter.health * 0.2).toInt().coerceAtLeast(1)
                    attacker.currentHp = (attacker.currentHp + healAmount).coerceAtMost(attacker.fighter.health)
                    newLog.add(BattleLogEntry(message = "${attacker.fighter.name} quickly heals for $healAmount HP!", color = attackerColor, source = attackerSource))
                } else {
                    newLog.add(BattleLogEntry(message = "${attacker.fighter.name} is already at full health!", color = attackerColor, source = attackerSource))
                }
            }
            "regeneration" -> {
                battleSoundPlayer.playHealSound()
                if (attacker.currentHp < attacker.fighter.health) {
                    attacker.turnsUntilRegenEnds = 3
                    newLog.add(BattleLogEntry(message = "${attacker.fighter.name} begins to regenerate health!", color = attackerColor, source = attackerSource))
                } else {
                    newLog.add(BattleLogEntry(message = "${attacker.fighter.name} tries to regenerate, but is already at full health!", color = attackerColor, source = attackerSource))
                }
            }
            "lucky_gambit" -> {
                battleSoundPlayer.playFocusSound()
                when(Random.nextInt(4)) {
                    0 -> {
                        val damage = (attacker.fighter.attack * 2.5).toInt()
                        defender.currentHp -= damage
                        newLog.add(BattleLogEntry(message = "JACKPOT! A huge hit for $damage damage!", color = attackerColor, fontWeight = FontWeight.Bold, source = attackerSource))
                    }
                    1 -> {
                        if (attacker.currentHp < attacker.fighter.health) {
                            attacker.currentHp = attacker.fighter.health
                            newLog.add(BattleLogEntry(message = "LADY LUCK SMILES! ${attacker.fighter.name} is fully healed!", color = attackerColor, fontWeight = FontWeight.Bold, source = attackerSource))
                        } else {
                            newLog.add(BattleLogEntry(message = "LADY LUCK SMILES, but ${attacker.fighter.name} is already at full health!", color = attackerColor, source = attackerSource))
                        }
                    }
                    2 -> {
                        defender.isStunned = true
                        newLog.add(BattleLogEntry(message = "WHAT LUCK! ${defender.fighter.name} is stunned!", color = defenderColor, fontWeight = FontWeight.Bold, source = defenderSource))
                    }
                    3 -> {
                        val damage = (attacker.fighter.attack * 0.5).toInt()
                        attacker.currentHp -= damage
                        newLog.add(BattleLogEntry(message = "BAD LUCK! The move backfires, dealing $damage damage to ${attacker.fighter.name}!", color = attackerColor, fontStyle = FontStyle.Italic, source = attackerSource))
                    }
                }
            }
        }
    }


    private fun endBattle(winner: BattleFighter, loser: BattleFighter, newLog: MutableList<BattleLogEntry>) {
        newLog.add(BattleLogEntry(message = "${loser.fighter.name} has been defeated!", color = Color.White, fontWeight = FontWeight.Bold, source = MessageSource.SYSTEM))
        saveBattleResult(winner.fighter, loser.fighter)

        val finalFighter1 = if (_uiState.value.fighter1?.fighter?.id == winner.fighter.id) winner else loser
        val finalFighter2 = if (_uiState.value.fighter2?.fighter?.id == winner.fighter.id) winner else loser

        _uiState.update {
            it.copy(
                fighter1 = finalFighter1.copy(),
                fighter2 = finalFighter2.copy(),
                battleLog = it.battleLog + newLog,
                isBattleOver = true,
                winner = winner.fighter
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    class BattleViewModelFactory(
        private val repository: FighterRepository,
        private val fighter1Id: Int,
        private val fighter2Id: Int,
        private val context: Context
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BattleViewModel::class.java)) {
                return BattleViewModel(repository, fighter1Id, fighter2Id, context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
