package com.example.chess.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

object ChessHaptics {
    private const val TAG = "ChessHaptics"

    @Suppress("DEPRECATION")
    private fun getVibrator(context: Context): Vibrator? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get Vibrator service", e)
            null
        }
    }

    /**
     * Standard piece move: A very short, crisp, subtle tap (15ms).
     */
    fun playMoveHaptic(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Short, low-amplitude click
                val effect = VibrationEffect.createOneShot(15, 70)
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(15)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing move haptic", e)
        }
    }

    /**
     * Piece capture: A sharper, dual-pulse click pattern to emulate physical impact.
     */
    fun playCaptureHaptic(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Dual pulses: 15ms pulse, 30ms quiet, 20ms pulse
                val timings = longArrayOf(0, 15, 30, 20)
                val amplitudes = intArrayOf(0, 120, 0, 180)
                val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                val pattern = longArrayOf(0, 15, 30, 20)
                vibrator.vibrate(pattern, -1)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing capture haptic", e)
        }
    }

    /**
     * King in check: A distinct, pulsing alert pattern (three rapid warning pulses).
     */
    fun playCheckHaptic(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Three quick urgent pulses at high intensity
                val timings = longArrayOf(0, 60, 60, 60, 60, 120)
                val amplitudes = intArrayOf(0, 200, 0, 200, 0, 255)
                val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                val pattern = longArrayOf(0, 60, 60, 60, 60, 120)
                vibrator.vibrate(pattern, -1)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing check haptic", e)
        }
    }

    /**
     * Game Over Victory: A triumphant, high-frequency rhythmic pulse train.
     */
    fun playVictoryHaptic(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Ascending tempo rhythmic pulses
                val timings = longArrayOf(0, 40, 80, 40, 60, 40, 40, 120)
                val amplitudes = intArrayOf(0, 100, 0, 130, 0, 180, 0, 255)
                val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                val pattern = longArrayOf(0, 40, 80, 40, 60, 40, 40, 120)
                vibrator.vibrate(pattern, -1)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing victory haptic", e)
        }
    }

    /**
     * Game Over Defeat or Draw: A long, heavy, fading rumble.
     */
    fun playDefeatHaptic(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Descending rumble intensity
                val timings = longArrayOf(0, 100, 50, 150, 50, 250)
                val amplitudes = intArrayOf(0, 255, 0, 150, 0, 80)
                val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                val pattern = longArrayOf(0, 100, 50, 150, 50, 250)
                vibrator.vibrate(pattern, -1)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing defeat haptic", e)
        }
    }
}
