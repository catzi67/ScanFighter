package com.catto.scanfighter.utils

import com.catto.scanfighter.data.Fighter
import kotlin.math.abs

object FighterStatsGenerator {

    fun generateStats(name: String, barcode: String): Fighter {
        val hashCode = barcode.hashCode()
        val seed = abs(hashCode.toLong())

        val hp = 50 + (seed % 51).toInt()          // HP: 50-100
        val attack = 10 + (seed / 100 % 41).toInt()  // Attack: 10-50
        val defense = 5 + (seed / 10000 % 36).toInt() // Defense: 5-40
        val speed = 1 + (seed / 1000000 % 20).toInt()  // Speed: 1-20
        val luck = 1 + (seed / 100000000 % 10).toInt() // Luck: 1-10

        return Fighter(
            name = name,
            barcode = barcode,
            hp = hp,
            attack = attack,
            defense = defense,
            speed = speed,
            luck = luck
        )
    }
}
