package com.catto.scanfighter.data

import kotlinx.coroutines.flow.Flow

class FighterRepository(private val fighterDao: FighterDao) {

    val allFighters: Flow<List<Fighter>> = fighterDao.getAllFightersSortedByWins()

    suspend fun getFighterById(id: Int): Fighter? {
        return fighterDao.getFighterById(id)
    }

    suspend fun addFighter(fighter: Fighter) {
        fighterDao.insertFighter(fighter)
    }

    suspend fun updateFighter(fighter: Fighter) {
        fighterDao.updateFighter(fighter)
    }

    suspend fun deleteFighter(fighter: Fighter) {
        fighterDao.deleteFighter(fighter)
    }
}
