// app/src/main/java/com/catto/scanfighter/data/FighterRepository.kt
package com.catto.scanfighter.data

import com.catto.scanfighter.data.Fighter
import com.catto.scanfighter.data.FighterDao
import kotlinx.coroutines.flow.Flow

class FighterRepository(private val fighterDao: FighterDao) {

    val allFighters: Flow<List<Fighter>> = fighterDao.getAllFighters()

    suspend fun getFighterById(id: Int): Fighter? {
        return fighterDao.getFighterById(id)
    }

    suspend fun addFighter(fighter: Fighter) {
        fighterDao.addFighter(fighter)
    }

    suspend fun updateFighter(fighter: Fighter) {
        fighterDao.updateFighter(fighter)
    }

    suspend fun deleteFighter(fighter: Fighter) {
        fighterDao.deleteFighter(fighter)
    }
}
