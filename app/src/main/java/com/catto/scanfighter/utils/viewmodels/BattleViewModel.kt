package com.catto.scanfighter.utils.viewmodels

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.catto.scanfighter.data.Fighter
import com.catto.scanfighter.data.FighterRepository
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

    data class BattleFighter(
        val fighter: Fighter,
        val currentHp: Int,
        val stunnedForRounds: Int = 0,
        val color: Color,
        val attackModifier: Int = 0,
        val defenseModifier: Int = 0,
        val bleedRounds: Int = 0,
        val isFocused: Boolean = false,
        val isEnraged: Boolean = false
    )

    data class BattleUiState(
        val isLoading: Boolean = true,
        val fighter1: BattleFighter? = null,
        val fighter2: BattleFighter? = null,
        val battleLog: List<Pair<String, Color>> = emptyList(),
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
                val color1 = FighterStatsGenerator.generateColorSignature(f1.barcode, 1).first()
                val color2 = FighterStatsGenerator.generateColorSignature(f2.barcode, 1).first()

                val battleFighter1 = BattleFighter(f1, f1.health, color = color1)
                val battleFighter2 = BattleFighter(f2, f2.health, color = color2)
                val isFighter1Turn = f1.speed >= f2.speed
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        fighter1 = battleFighter1,
                        fighter2 = battleFighter2,
                        isFighter1Turn = isFighter1Turn,
                        battleLog = listOf("The battle between ${f1.name} and ${f2.name} begins!" to Color.White)
                    )
                }
            }
        }
    }

    fun runFullBattle() {
        viewModelScope.launch {
            while (!_uiState.value.isBattleOver) {
                nextTurn()
                delay(1500) // Delay between turns for readability
            }
            val winner = _uiState.value.winner
            if (winner != null) {
                val winnerName = winner.name
                val finalMessage = "--- Battle Over! $winnerName is victorious! ---"
                val signature = MusicUtils.generateMusicalSignature(winner.barcode)
                battleSoundPlayer.playVictorySignature(signature)
                _uiState.update {
                    it.copy(battleLog = it.battleLog + (finalMessage to Color.White))
                }
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

        var attacker = if (currentState.isFighter1Turn) currentState.fighter1!! else currentState.fighter2!!
        var defender = if (currentState.isFighter1Turn) currentState.fighter2!! else currentState.fighter1!!

        val newLog = mutableListOf<Pair<String, Color>>()
        if (currentState.battleLog.isNotEmpty()) {
            newLog.add("" to Color.White)
        }

        // --- Pre-Turn Effects ---
        if (attacker.currentHp < attacker.fighter.health * 0.25 && !attacker.isEnraged) {
            attacker = attacker.copy(isEnraged = true, attackModifier = attacker.attackModifier + 10)
            newLog.add("${attacker.fighter.name} is ENRAGED!" to attacker.color)
            battleSoundPlayer.playEnrageSound()
        }

        if (attacker.bleedRounds > 0) {
            val bleedDamage = 5
            attacker = attacker.copy(
                currentHp = (attacker.currentHp - bleedDamage).coerceAtLeast(0),
                bleedRounds = attacker.bleedRounds - 1
            )
            newLog.add("${attacker.fighter.name} bleeds for $bleedDamage damage." to attacker.color)
            if (attacker.currentHp <= 0) {
                _uiState.update {
                    it.copy(
                        fighter1 = if (it.isFighter1Turn) attacker else defender,
                        fighter2 = if (it.isFighter1Turn) defender else attacker,
                        battleLog = it.battleLog + newLog,
                        isBattleOver = true,
                        winner = defender.fighter
                    )
                }
                saveBattleResult(defender.fighter, attacker.fighter)
                return
            }
        }

        // --- Main Turn Action ---
        if (attacker.stunnedForRounds > 0) {
            newLog.add("${attacker.fighter.name} is stunned and cannot move!" to attacker.color)
            battleSoundPlayer.playStunSound()
            attacker = attacker.copy(stunnedForRounds = attacker.stunnedForRounds - 1)
        } else {
            if (!attacker.isFocused && Random.nextInt(100) < attacker.fighter.skill) {
                attacker = attacker.copy(isFocused = true)
                newLog.add("${attacker.fighter.name} is focusing their energy!" to attacker.color)
                battleSoundPlayer.playFocusSound()
            }

            if (Random.nextInt(100) < attacker.fighter.luck) {
                val healAmount = 10
                attacker = attacker.copy(currentHp = (attacker.currentHp + healAmount).coerceAtMost(attacker.fighter.health))
                newLog.add("${attacker.fighter.name} gets a lucky break and heals for $healAmount HP!" to attacker.color)
                battleSoundPlayer.playHealSound()
            }

            val (updatedAttacker, updatedDefender) = performAttack(attacker, defender, newLog)
            attacker = updatedAttacker
            defender = updatedDefender


            if (attacker.fighter.speed > defender.fighter.speed * 2 && Random.nextInt(100) < 25) {
                newLog.add("${attacker.fighter.name} is fast enough for a second attack!" to attacker.color)
                val (finalAttacker, finalDefender) = performAttack(attacker, defender, newLog)
                attacker = finalAttacker
                defender = finalDefender
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
                fighter1 = if (it.isFighter1Turn) attacker else defender,
                fighter2 = if (it.isFighter1Turn) defender else attacker,
                battleLog = it.battleLog + newLog,
                isFighter1Turn = !it.isFighter1Turn,
                isBattleOver = isBattleOver,
                winner = winnerFighter
            )
        }
    }

    private fun performAttack(
        attacker: BattleFighter,
        defender: BattleFighter,
        log: MutableList<Pair<String, Color>>
    ): Pair<BattleFighter, BattleFighter> {
        var currentAttacker = attacker
        var currentDefender = defender

        val hitChance = (currentAttacker.fighter.speed.toFloat() / (currentAttacker.fighter.speed + currentDefender.fighter.speed)) * 100 + 20
        if (Random.nextInt(100) < hitChance) {
            val isCritical = currentAttacker.isFocused || Random.nextInt(100) < (currentAttacker.fighter.skill)
            if(currentAttacker.isFocused) currentAttacker = currentAttacker.copy(isFocused = false)

            val damageMultiplier = if (isCritical) 1.5 else 1.0
            val baseDamage = currentAttacker.fighter.attack + currentAttacker.attackModifier
            val defenseReduction = (currentDefender.fighter.defense + currentDefender.defenseModifier) * 0.5
            var damage = (baseDamage * damageMultiplier - defenseReduction).toInt().coerceAtLeast(1)

            if (isCritical) {
                log.add("CRITICAL HIT! ${currentAttacker.fighter.name} attacks with fury!" to currentAttacker.color)
                battleSoundPlayer.playCriticalHitSound()
            } else {
                battleSoundPlayer.playHitSound()
            }

            val blockChance = (currentDefender.fighter.defense + currentDefender.defenseModifier) / 2
            if (Random.nextInt(100) < blockChance) {
                damage /= 2
                log.add("${currentDefender.fighter.name} blocks part of the attack!" to currentDefender.color)
                battleSoundPlayer.playBlockSound()

                if(Random.nextInt(100) < currentDefender.fighter.skill) {
                    val counterDamage = (currentDefender.fighter.attack * 0.5).toInt().coerceAtLeast(1)
                    currentAttacker = currentAttacker.copy(currentHp = (currentAttacker.currentHp - counterDamage).coerceAtLeast(0))
                    log.add("${currentDefender.fighter.name} counter-attacks for $counterDamage damage!" to currentDefender.color)
                }
            }

            val newHp = (currentDefender.currentHp - damage).coerceAtLeast(0)
            currentDefender = currentDefender.copy(currentHp = newHp)
            log.add("${currentAttacker.fighter.name} hits ${currentDefender.fighter.name} for $damage damage." to currentAttacker.color)

            if (isCritical && Random.nextInt(100) < 50) {
                currentDefender = applyRandomDebuff(currentDefender, log)
            }

        } else {
            log.add("${currentAttacker.fighter.name} attacks, but ${currentDefender.fighter.name} dodges!" to currentAttacker.color)
        }
        return Pair(currentAttacker, currentDefender)
    }

    private fun applyRandomDebuff(defender: BattleFighter, log: MutableList<Pair<String, Color>>): BattleFighter {
        var currentDefender = defender
        when(Random.nextInt(4)){
            0 -> { // Stun
                val stunDuration = Random.nextInt(1, 4)
                currentDefender = currentDefender.copy(stunnedForRounds = stunDuration)
                log.add("${currentDefender.fighter.name} is stunned for $stunDuration rounds!" to currentDefender.color)
                battleSoundPlayer.playStunSound()
            }
            1 -> { // Attack Down
                currentDefender = currentDefender.copy(attackModifier = currentDefender.attackModifier - 5)
                log.add("${currentDefender.fighter.name}'s attack was lowered!" to currentDefender.color)
                battleSoundPlayer.playDebuffSound()
            }
            2 -> { // Defense Down
                currentDefender = currentDefender.copy(defenseModifier = currentDefender.defenseModifier - 5)
                log.add("${currentDefender.fighter.name}'s defense was lowered!" to currentDefender.color)
                battleSoundPlayer.playDebuffSound()
            }
            3 -> { // Bleed
                currentDefender = currentDefender.copy(bleedRounds = 3)
                log.add("${currentDefender.fighter.name} is bleeding!" to currentDefender.color)
                battleSoundPlayer.playDebuffSound()
            }
        }
        return currentDefender
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
