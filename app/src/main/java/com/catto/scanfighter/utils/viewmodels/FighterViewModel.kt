package com.catto.scanfighter.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.catto.scanfighter.data.Fighter
import com.catto.scanfighter.data.FighterDatabase
import com.catto.scanfighter.utils.FighterStatsGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class FighterViewModel(application: Application) : AndroidViewModel(application) {

    private val fighterDao = FighterDatabase.getDatabase(application).fighterDao()
    val allFighters: Flow<List<Fighter>> = fighterDao.getAllFighters()

    fun createFighter(name: String, barcode: String) {
        viewModelScope.launch {
            val newFighter = FighterStatsGenerator.generateStats(name, barcode)
            fighterDao.insertFighter(newFighter)
        }
    }
}
