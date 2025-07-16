package com.catto.scanfighter.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Fighter::class], version = 4, exportSchema = false)
abstract class FighterDatabase : RoomDatabase() {

    abstract fun fighterDao(): FighterDao

    companion object {
        @Volatile
        private var INSTANCE: FighterDatabase? = null

        fun getDatabase(context: Context): FighterDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FighterDatabase::class.java,
                    "fighter_database"
                )
                    // Updated to the new version of the method to resolve the deprecation warning.
                    // This migration strategy will clear the database if the schema changes.
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
