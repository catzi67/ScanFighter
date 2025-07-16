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
    val skill: Int,
    val specialMoveType: String, // Added to define the fighter's unique special move
    val wins: Int = 0,
    val losses: Int = 0
)
