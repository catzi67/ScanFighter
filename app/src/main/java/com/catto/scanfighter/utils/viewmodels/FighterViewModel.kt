package com.catto.scanfighter.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.catto.scanfighter.data.Fighter
import com.catto.scanfighter.data.FighterDao
import com.catto.scanfighter.utils.FighterStatsGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class FighterViewModel(private val fighterDao: FighterDao) : ViewModel() {

    val allFighters: Flow<List<Fighter>> = fighterDao.getAllFightersSortedByWins()

    fun createFighter(name: String, barcode: String) {
        viewModelScope.launch {
            val newFighter = FighterStatsGenerator.generateStats(name, barcode)
            fighterDao.insertFighter(newFighter)
        }
    }

    fun updateFighter(fighter: Fighter) {
        viewModelScope.launch {
            fighterDao.updateFighter(fighter)
        }
    }

    fun deleteFighter(fighter: Fighter) {
        viewModelScope.launch {
            fighterDao.deleteFighter(fighter)
        }
    }
}

/**
 * Factory for creating a FighterViewModel with a constructor that takes a FighterDao.
 */
class FighterViewModelFactory(private val fighterDao: FighterDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FighterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FighterViewModel(fighterDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
