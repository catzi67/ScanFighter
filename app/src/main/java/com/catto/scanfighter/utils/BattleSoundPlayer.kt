package com.catto.scanfighter.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.catto.scanfighter.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A dedicated sound player for battle-specific sound effects.
 * This class encapsulates the logic for generating sounds for different battle events.
 */
class BattleSoundPlayer(context: Context) {

    private val soundPlayer = SoundPlayer() // Kept for generated tones
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    // New SoundPool for playing samples
    private val soundPool: SoundPool
    private var swordSoundId: Int = 0
    private var missSoundId: Int = 0
    private var regenerationSoundId: Int = 0
    private var isSoundPoolLoaded = false

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(3) // Allow up to 3 sounds to play at once
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) {
                isSoundPoolLoaded = true
            }
        }

        // Load the sound sample from res/raw
        swordSoundId = soundPool.load(context, R.raw.sword, 1)
        missSoundId = soundPool.load(context, R.raw.uurgghh, 1)
        regenerationSoundId = soundPool.load(context, R.raw.regeneration, 1)
    }

    private fun playSample(soundId: Int, rate: Float = 1.0f) {
        if (isSoundPoolLoaded) {
            soundPool.play(soundId, 1f, 1f, 1, 0, rate)
        }
    }

    /**
     * Plays a sound for a missed attack.
     */
    fun playMissSound() {
        playSample(missSoundId)
    }

    /**
     * Plays a sharp, metallic sound for a standard hit.
     */
    fun playHitSound() {
        playSample(swordSoundId, 1.2f) // Play faster for a sharper sound
    }

    /**
     * Plays a dramatic "shing" and impact sound for a critical hit.
     */
    fun playCriticalHitSound() {
        playSample(swordSoundId, 1.5f) // Play even faster and sharper
    }

    /**
     * Plays a heavy thud and a sharp "clang" for a blocked attack.
     */
    fun playBlockSound() {
        playSample(swordSoundId, 0.8f) // Play slower for a heavier sound
    }

    /**
     * Plays a dizzying, wavering sound for a stun.
     */
    fun playStunSound() {
        coroutineScope.launch {
            soundPlayer.playNote(400f, 200)
            delay(100)
            soundPlayer.playNote(380f, 300)
        }
    }

    /**
     * Plays a powerful sound for an enraged fighter.
     */
    fun playEnrageSound() {
        coroutineScope.launch {
            soundPlayer.playNote(100f, 200)
            delay(150)
            soundPlayer.playNote(150f, 300)
        }
    }

    /**
     * Plays a shimmering sound for a focused fighter.
     */
    fun playFocusSound() {
        coroutineScope.launch {
            soundPlayer.playNote(600f, 150)
            delay(100)
            soundPlayer.playNote(800f, 150)
            delay(100)
            soundPlayer.playNote(1000f, 250)
        }
    }

    /**
     * Plays a gentle sound for healing.
     */
    fun playRegenerationSound() {
        playSample(regenerationSoundId)
    }

    /**
     * Plays a descending tone for a debuff.
     */
    fun playDebuffSound() {
        coroutineScope.launch {
            soundPlayer.playNote(400f, 200)
            delay(150)
            soundPlayer.playNote(250f, 300)
        }
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
