package com.catto.scanfighter.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A dedicated sound player for battle-specific sound effects.
 * This class encapsulates the logic for generating sounds for different battle events.
 */
class BattleSoundPlayer {

    private val soundPlayer = SoundPlayer()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)


    /**
     * Plays a sharp, percussive sound for a standard hit.
     */
    fun playHitSound() {
        soundPlayer.playNote(180f, 150) // Low, short thud
    }

    /**
     * Plays a more dramatic, impactful sound for a critical hit.
     */
    fun playCriticalHitSound() {
        soundPlayer.playNote(300f, 250) // Sharper, more impactful sound
    }

    /**
     * Plays a dull thud for a blocked attack.
     */
    fun playBlockSound() {
        soundPlayer.playNote(120f, 200) // Very low, dull thud
    }

    /**
     * Plays a dizzying, wavering sound for a stun.
     */
    fun playStunSound() {
        soundPlayer.playNote(450f, 100)
        soundPlayer.playNote(250f, 200)
    }

    /**
     * Plays a powerful sound for an enraged fighter.
     */
    fun playEnrageSound() {
        soundPlayer.playNote(100f, 500)
    }

    /**
     * Plays a shimmering sound for a focused fighter.
     */
    fun playFocusSound() {
        soundPlayer.playNote(1200f, 300)
    }

    /**
     * Plays a gentle sound for healing.
     */
    fun playHealSound() {
        soundPlayer.playNote(800f, 400)
    }

    /**
     * Plays a descending tone for a debuff.
     */
    fun playDebuffSound() {
        soundPlayer.playNote(400f, 150)
        soundPlayer.playNote(300f, 250)
    }


    /**
     * Plays the winning fighter's musical signature.
     * @param signature A list of note frequencies to play.
     */
    fun playVictorySignature(signature: List<Float>) {
        coroutineScope.launch {
            signature.forEach { note ->
                soundPlayer.playNote(note, 300)
                delay(200)
            }
        }
    }
}
