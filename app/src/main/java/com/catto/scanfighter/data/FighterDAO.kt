package com.catto.scanfighter.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FighterDao {
    @Insert
    suspend fun insertFighter(fighter: Fighter)

    @Update
    suspend fun updateFighter(fighter: Fighter)

    @Query("SELECT * FROM fighters ORDER BY wins DESC")
    fun getAllFightersSortedByWins(): Flow<List<Fighter>>
}
