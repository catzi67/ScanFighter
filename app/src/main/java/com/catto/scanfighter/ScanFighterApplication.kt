package com.catto.scanfighter

import android.app.Application
import com.catto.scanfighter.data.FighterDatabase
import com.catto.scanfighter.data.FighterRepository

/**
 * Custom Application class to provide a single instance of the database.
 */
class ScanFighterApplication : Application() {
    // Using by lazy so the database is only created when it's first needed.
    val database: FighterDatabase by lazy { FighterDatabase.getDatabase(this) }
    val repository: FighterRepository by lazy { FighterRepository(database.fighterDao()) }

}
