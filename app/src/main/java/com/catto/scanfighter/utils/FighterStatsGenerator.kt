package com.catto.scanfighter.utils

import androidx.compose.ui.graphics.Color
import com.catto.scanfighter.data.Fighter
import java.nio.ByteBuffer
import java.security.MessageDigest
import kotlin.math.abs

object FighterStatsGenerator {

    fun generateStats(name: String, barcode: String): Fighter {
        // Use SHA-256 to create a consistent hash from the barcode string.
        // String.hashCode() is not guaranteed to be the same across different JVMs/devices.
        // SHA-256 is a standard that will always produce the same result for the same input.
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(barcode.toByteArray(Charsets.UTF_8))

        // Use the first 8 bytes of the hash to create a Long. This will be our stable seed.
        val seed = ByteBuffer.wrap(hashBytes, 0, 8).long

        // The rest of the stat generation is based on this deterministic seed.
        val hp = 50 + (abs(seed) % 51).toInt()          // HP: 50-100
        val attack = 10 + (abs(seed shr 8) % 41).toInt()  // Attack: 10-50
        val defense = 5 + (abs(seed shr 16) % 36).toInt() // Defense: 5-40
        val speed = 1 + (abs(seed shr 24) % 20).toInt()  // Speed: 1-20
        val luck = 1 + (abs(seed shr 32) % 10).toInt() // Luck: 1-10

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

    fun generateColorSignature(barcode: String, count: Int = 5): List<Color> {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(barcode.toByteArray(Charsets.UTF_8))
        val seed = ByteBuffer.wrap(hashBytes, 0, 8).long
        val colors = mutableListOf<Color>()

        for (i in 0 until count) {
            val h = (abs(seed shr (i * 8)) % 360).toFloat()
            val s = 0.5f + (abs(seed shr (i * 4)) % 50) / 100f
            val v = 0.7f + (abs(seed shr (i * 2)) % 30) / 100f
            colors.add(Color.hsv(h, s, v))
        }
        return colors
    }
}
