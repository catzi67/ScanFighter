package com.catto.scanfighter.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FighterDao {
    @Insert
    suspend fun insertFighter(fighter: Fighter)

    @Query("SELECT * FROM fighters ORDER BY name ASC")
    fun getAllFighters(): Flow<List<Fighter>>
}
