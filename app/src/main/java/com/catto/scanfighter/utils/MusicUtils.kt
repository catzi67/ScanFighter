package com.catto.scanfighter.utils

import java.nio.ByteBuffer
import java.security.MessageDigest
import kotlin.math.abs

object MusicUtils {

    // A C-Major scale provides a good selection of consonant notes to choose from.
    private val C_MAJOR_SCALE = floatArrayOf(
        261.63f, // C4
        293.66f, // D4
        329.63f, // E4
        349.23f, // F4
        392.00f, // G4
        440.00f, // A4
        493.88f, // B4
    )

    /**
     * Generates a unique musical signature based on a fighter's barcode.
     * This function uses the barcode's hash to deterministically select a
     * starting octave and a sequence of notes from the C_MAJOR_SCALE.
     *
     * @param barcode The unique barcode string for the fighter.
     * @param numNotes The number of notes in the signature (should match the number of color bars).
     * @return A list of unique note frequencies for the fighter.
     */
    fun generateMusicalSignature(barcode: String, numNotes: Int = 5): List<Float> {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(barcode.toByteArray(Charsets.UTF_8))
        val seed = ByteBuffer.wrap(hashBytes, 0, 8).long

        val signature = mutableListOf<Float>()

        // The first part of the seed determines the starting octave (0.5x, 1x, or 2x frequency).
        val octaveMultiplier = when ((abs(seed) % 3).toInt()) {
            0 -> 0.5f
            1 -> 1.0f
            else -> 2.0f
        }

        // The rest of the seed determines the notes from the scale.
        for (i in 0 until numNotes) {
            // Use different parts of the seed for each note to ensure variety.
            val noteIndex = (abs(seed shr (i * 4)) % C_MAJOR_SCALE.size).toInt()
            val baseFrequency = C_MAJOR_SCALE[noteIndex]
            signature.add(baseFrequency * octaveMultiplier)
        }

        return signature
    }
}
