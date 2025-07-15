package com.catto.scanfighter.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fighters")
data class Fighter(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val barcode: String,
    val health: Int,
    val attack: Int,
    val defense: Int,
    val speed: Int,
    val luck: Int,
    val skill: Int, // Add this line
    val wins: Int = 0,
    val losses: Int = 0
)
