// In app/src/main/java/com/catto/scanfighter/utils/viewmodels/FighterViewModel.kt

package com.catto.scanfighter.utils.viewmodels // Ensure this package is correct

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.catto.scanfighter.data.Fighter
import com.catto.scanfighter.data.FighterRepository // Import repository
import com.catto.scanfighter.utils.FighterStatsGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class FighterViewModel(private val repository: FighterRepository) : ViewModel() { // Change fighterDao to repository

    val allFighters: Flow<List<Fighter>> = repository.allFighters // Use repository

    fun createFighter(name: String, barcode: String) {
        viewModelScope.launch {
            val newFighter = FighterStatsGenerator.generateStats(name, barcode)
            repository.addFighter(newFighter) // Use repository
        }
    }

    fun updateFighter(fighter: Fighter) {
        viewModelScope.launch {
            repository.updateFighter(fighter) // Use repository
        }
    }

    fun deleteFighter(fighter: Fighter) {
        viewModelScope.launch {
            repository.deleteFighter(fighter) // Use repository
        }
    }

    /**
     * Factory for creating a FighterViewModel with a constructor that takes a FighterRepository.
     */
    class FighterViewModelFactory(private val repository: FighterRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FighterViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FighterViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}