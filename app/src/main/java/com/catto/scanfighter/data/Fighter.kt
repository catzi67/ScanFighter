package com.catto.scanfighter.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fighters")
data class Fighter(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val barcode: String,
    val hp: Int,
    val attack: Int,
    val defense: Int,
    val speed: Int,
    val luck: Int
)
