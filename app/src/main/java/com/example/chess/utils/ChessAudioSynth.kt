package com.example.chess.utils

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.exp

object ChessAudioSynth {
    private const val TAG = "ChessAudioSynth"
    private const val SAMPLE_RATE = 44100
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    /**
     * Play a clean, physical wood-like tapping sound for a normal move.
     */
    fun playMove() {
        coroutineScope.launch {
            val duration = 0.05f // 50ms
            val numSamples = (SAMPLE_RATE * duration).toInt()
            val samples = ShortArray(numSamples)
            
            for (i in 0 until numSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                // Crisp physical frequency decay: from 800Hz down to 180Hz
                val freq = 800.0 - (620.0 * (t / duration))
                // Clean fast exponential amplitude decay
                val env = exp(-t * 90.0)
                val angle = 2.0 * PI * freq * t
                samples[i] = (sin(angle) * Short.MAX_VALUE * env * 0.45).toInt().toShort()
            }
            playPCM(samples)
        }
    }

    /**
     * Play a snappier, dual-frequency clicking sound representing a piece capture.
     */
    fun playCapture() {
        coroutineScope.launch {
            val duration = 0.07f // 70ms
            val numSamples = (SAMPLE_RATE * duration).toInt()
            val samples = ShortArray(numSamples)
            
            for (i in 0 until numSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                // Capture sound: slightly higher transient and sharper snap
                val freq = 1200.0 - (950.0 * (t / duration))
                // Extremely rapid initial decay with slight vibrato / secondary pluck
                val env = exp(-t * 60.0) * (1.0 + sin(t * PI / duration * 5.0) * 0.3)
                val angle = 2.0 * PI * freq * t
                samples[i] = (sin(angle) * Short.MAX_VALUE * env * 0.4).toInt().toShort()
            }
            playPCM(samples)
        }
    }

    /**
     * Play a resonant, warm, minor/major alert chime representing a king in check.
     */
    fun playCheck() {
        coroutineScope.launch {
            val duration = 0.28f // 280ms
            val numSamples = (SAMPLE_RATE * duration).toInt()
            val samples = ShortArray(numSamples)
            
            val f1 = 493.88 // B4
            val f2 = 587.33 // D5
            
            for (i in 0 until numSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                // Elegant long ringing decay envelope
                val env = exp(-t * 14.0)
                // Combine B4 + D5 minor interval to produce warning tension
                val wave = 0.5 * sin(2.0 * PI * f1 * t) + 0.5 * sin(2.0 * PI * f2 * t)
                samples[i] = (wave * Short.MAX_VALUE * env * 0.45).toInt().toShort()
            }
            playPCM(samples)
        }
    }

    /**
     * Play a triumphant ascending dynamic arpeggio for victory.
     */
    fun playVictory() {
        coroutineScope.launch {
            val duration = 0.6f // 600ms
            val numSamples = (SAMPLE_RATE * duration).toInt()
            val samples = ShortArray(numSamples)
            
            // Notes: C5 (523.25), E5 (659.25), G5 (783.99), C6 (1046.50)
            val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50)
            
            for (i in 0 until numSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                // Progression step based on time
                val noteIndex = ((t / duration) * notes.size).toInt().coerceIn(0, notes.size - 1)
                val freq = notes[noteIndex]
                
                // Exponential decay per note step
                val stepProgress = (t % (duration / notes.size)) / (duration / notes.size)
                val env = exp(-stepProgress * 4.0) * 0.5
                
                val angle = 2.0 * PI * freq * t
                samples[i] = (sin(angle) * Short.MAX_VALUE * env).toInt().toShort()
            }
            playPCM(samples)
        }
    }

    /**
     * Play a somber descending flat/minor arpeggio for draw or defeat.
     */
    fun playDefeatOrDraw() {
        coroutineScope.launch {
            val duration = 0.6f // 600ms
            val numSamples = (SAMPLE_RATE * duration).toInt()
            val samples = ShortArray(numSamples)
            
            // Notes: G4 (392.00), Eb4 (311.13), C4 (261.63)
            val notes = doubleArrayOf(392.00, 311.13, 261.63)
            
            for (i in 0 until numSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                val noteIndex = ((t / duration) * notes.size).toInt().coerceIn(0, notes.size - 1)
                val freq = notes[noteIndex]
                
                val stepProgress = (t % (duration / notes.size)) / (duration / notes.size)
                val env = exp(-stepProgress * 3.5) * 0.45
                
                val angle = 2.0 * PI * freq * t
                samples[i] = (sin(angle) * Short.MAX_VALUE * env).toInt().toShort()
            }
            playPCM(samples)
        }
    }

    private fun playPCM(samples: ShortArray) {
        try {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(samples.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(samples, 0, samples.size)
            audioTrack.play()
            
            // Cleanup audio track memory after playback finishes
            val durationMs = (samples.size * 1000L) / SAMPLE_RATE
            coroutineScope.launch {
                delay(durationMs + 150)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (ignored: Exception) {}
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play synthesized audio wave", e)
        }
    }
}
