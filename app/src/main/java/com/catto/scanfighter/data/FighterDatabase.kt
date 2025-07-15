package com.catto.scanfighter.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Fighter::class], version = 3, exportSchema = false)
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
                    // This is a simple migration strategy that will clear the database
                    // if the schema changes. This is fine for development but should
                    // be replaced with a proper migration for a production app.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
