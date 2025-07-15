package com.catto.scanfighter.utils

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

class SoundPlayer {

    private val sampleRate = 44100
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    fun playNote(frequency: Float, durationMs: Int = 700) {
        coroutineScope.launch {
            val numSamples = durationMs * sampleRate / 1000
            val audioBuffer = ShortArray(numSamples)

            val fadeInSamples = (numSamples * 0.05).toInt().coerceAtLeast(1)
            val fadeOutSamples = (numSamples * 0.20).toInt().coerceAtLeast(2)
            val sustainSamples = numSamples - fadeInSamples - fadeOutSamples

            for (i in 0 until numSamples) {
                val sineValue = sin(2.0 * Math.PI * i / (sampleRate / frequency))
                val envelope = when {
                    i < fadeInSamples -> i.toFloat() / fadeInSamples
                    i < fadeInSamples + sustainSamples -> 1.0f
                    else -> {
                        val progressInFadeOut = i - (fadeInSamples + sustainSamples)
                        (1.0f - progressInFadeOut.toFloat() / (fadeOutSamples - 1)).coerceAtLeast(0.0f)
                    }
                }
                val sampleValue = (sineValue * envelope * Short.MAX_VALUE).toInt()
                audioBuffer[i] = sampleValue.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                // Use a buffer size that is a multiple of the frame size to avoid issues.
                .setBufferSizeInBytes(audioBuffer.size * 2)
                .build()

            audioTrack.play()
            audioTrack.write(audioBuffer, 0, audioBuffer.size)

            // **THE FIX**: Instead of a fixed delay, we poll the playback position.
            // This loop actively waits until the AudioTrack has finished playing all the samples
            // from the buffer, which is the most reliable way to prevent cutting the sound off early.
            while (audioTrack.playbackHeadPosition < numSamples && audioTrack.playState == AudioTrack.PLAYSTATE_PLAYING) {
                // Wait for a very short time to avoid burning CPU cycles in a tight loop.
                delay(10)
            }

            // By the time the loop above finishes, the sound has completed naturally.
            // It is now safe to stop and release the track without causing a click.
            audioTrack.stop()
            audioTrack.release()
        }
    }
}
