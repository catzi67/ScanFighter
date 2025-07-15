package com.catto.scanfighter.utils.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.catto.scanfighter.data.Fighter
import com.catto.scanfighter.data.FighterRepository
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

    data class BattleFighter(
        val fighter: Fighter,
        var currentHp: Int,
        var isStunned: Boolean = false
    )

    data class BattleUiState(
        val isLoading: Boolean = true,
        val fighter1: BattleFighter? = null,
        val fighter2: BattleFighter? = null,
        val battleLog: List<String> = emptyList(),
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
                        battleLog = listOf("The battle between ${f1.name} and ${f2.name} begins!")
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
            val winnerName = _uiState.value.winner?.name ?: "No one"
            val finalMessage = "--- Battle Over! $winnerName is victorious! ---"
            _uiState.update {
                it.copy(battleLog = it.battleLog + finalMessage)
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

        val newLog = mutableListOf<String>()
        newLog.add("--- Turn ${(currentState.battleLog.count { it.startsWith("--- Turn") } + 1)} ---")

        if (attacker.isStunned) {
            newLog.add("${attacker.fighter.name} is stunned and can't move!")
            attacker.isStunned = false
        } else {
            val hitChance = (attacker.fighter.speed.toFloat() / (attacker.fighter.speed + defender.fighter.speed)) * 100 + 20
            if (Random.nextInt(100) < hitChance) {
                val isCritical = Random.nextInt(100) < (attacker.fighter.skill)
                val damageMultiplier = if (isCritical) 1.5 else 1.0
                val baseDamage = attacker.fighter.attack
                val defenseReduction = defender.fighter.defense * 0.5
                var damage = (baseDamage * damageMultiplier - defenseReduction).toInt().coerceAtLeast(1)

                if (isCritical) newLog.add("CRITICAL HIT! ${attacker.fighter.name} attacks with fury!")

                val blockChance = defender.fighter.defense / 2
                if (Random.nextInt(100) < blockChance) {
                    damage /= 2
                    newLog.add("${defender.fighter.name} blocks part of the attack!")
                }

                defender.currentHp = (defender.currentHp - damage).coerceAtLeast(0)
                newLog.add("${attacker.fighter.name} hits ${defender.fighter.name} for $damage damage.")

                if (isCritical && Random.nextInt(100) < 25) {
                    defender.isStunned = true
                    newLog.add("${defender.fighter.name} is stunned by the powerful blow!")
                }
            } else {
                newLog.add("${attacker.fighter.name} attacks, but ${defender.fighter.name} dodges!")
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
