package com.catto.scanfighter.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

object MusicUtils {

    // A Chromatic scale includes all 12 pitches in an octave, providing more unique notes.
    private val CHROMATIC_SCALE = floatArrayOf(
        261.63f, // C4
        277.18f, // C#4
        293.66f, // D4
        311.13f, // D#4
        329.63f, // E4
        349.23f, // F4
        369.99f, // F#4
        392.00f, // G4
        415.30f, // G#4
        440.00f, // A4
        466.16f, // A#4
        493.88f, // B4
    )

    // This palette MUST be identical to the one in FighterStatsGenerator.
    private val COLOR_PALETTE: List<Color> = List(12) { i ->
        Color.hsv(i * 30f, 0.9f, 0.9f)
    }

    /**
     * Generates a unique musical signature based on a fighter's color signature.
     * This function finds the index of each color in the fixed palette and maps it
     * to the corresponding note in the chromatic scale.
     *
     * @param colors The list of colors from the fighter's visual signature.
     * @return A list of unique note frequencies for the fighter.
     */
    fun generateMusicalSignature(colors: List<Color>): List<Float> {
        val signature = mutableListOf<Float>()

        colors.forEach { color ->
            // Find the index of the color in our fixed palette.
            val index = COLOR_PALETTE.indexOf(color)

            if (index != -1) {
                // Use the index to get the corresponding note from the scale.
                signature.add(CHROMATIC_SCALE[index])
            } else {
                // Fallback for safety, though this should not happen with the new system.
                // It maps the hue to a note, like the previous implementation.
                val hsv = FloatArray(3)
                android.graphics.Color.colorToHSV(color.toArgb(), hsv)
                val hue = hsv[0]
                val noteIndex = (hue / 360 * CHROMATIC_SCALE.size).toInt() % CHROMATIC_SCALE.size
                signature.add(CHROMATIC_SCALE[noteIndex])
            }
        }

        return signature
    }
}
