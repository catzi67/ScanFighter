package com.catto.scanfighter.utils

import androidx.compose.ui.graphics.Color
import com.catto.scanfighter.data.Fighter
import java.nio.ByteBuffer
import java.security.MessageDigest
import kotlin.math.abs

object FighterStatsGenerator {

    // A fixed palette of 12 distinct colors, evenly spaced on the color wheel.
    private val COLOR_PALETTE: List<Color> = List(12) { i ->
        Color.hsv(i * 30f, 0.9f, 0.9f)
    }

    fun generateStats(name: String, barcode: String): Fighter {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(barcode.toByteArray(Charsets.UTF_8))
        val seed = ByteBuffer.wrap(hashBytes, 0, 8).long

        val hp = 50 + (abs(seed) % 51).toInt()
        val attack = 10 + (abs(seed shr 8) % 41).toInt()
        val defense = 5 + (abs(seed shr 16) % 36).toInt()
        val speed = 1 + (abs(seed shr 24) % 20).toInt()
        val luck = 1 + (abs(seed shr 32) % 10).toInt()
        val skill = 5 + (abs(seed shr 40) % 26).toInt()

        return Fighter(
            name = name,
            barcode = barcode,
            health = hp,
            attack = attack,
            defense = defense,
            speed = speed,
            luck = luck,
            skill = skill
        )
    }

    fun generateColorSignature(barcode: String, count: Int = 5): List<Color> {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(barcode.toByteArray(Charsets.UTF_8))
        val seed = ByteBuffer.wrap(hashBytes, 0, 8).long
        val colors = mutableListOf<Color>()

        // Deterministically pick 'count' colors from our fixed palette.
        for (i in 0 until count) {
            val index = (abs(seed shr (i * 3)) % COLOR_PALETTE.size).toInt()
            colors.add(COLOR_PALETTE[index])
        }
        return colors
    }
}
