package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.data.GamePreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class SoundEffects(private val context: Context, private val prefs: GamePreferences) {

    private val scope = CoroutineScope(Dispatchers.Default)

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun playBounce() {
        if (!prefs.isSoundEnabled) return
        scope.launch {
            // Boing sine sweep: 320 Hz up to 640 Hz
            playSweepTone(startFreq = 340f, endFreq = 680f, durationMs = 120)
        }
        vibrate(30)
    }

    fun playCoin() {
        if (!prefs.isSoundEnabled) return
        scope.launch {
            // High bright bell ding
            playDualTone(988f, 1318f, durationMs = 100)
        }
        vibrate(20)
    }

    fun playPowerUp() {
        if (!prefs.isSoundEnabled) return
        scope.launch {
            // Rising fanfare: 440 -> 660 -> 880 Hz
            playSweepTone(440f, 980f, durationMs = 250)
        }
        vibrate(60)
    }

    fun playDangerHit() {
        if (!prefs.isSoundEnabled) return
        scope.launch {
            // Low thud buzz: 200 Hz down to 90 Hz
            playSweepTone(220f, 90f, durationMs = 200)
        }
        vibrate(100)
    }

    fun playSizzle() {
        if (!prefs.isSoundEnabled) return
        scope.launch {
            playNoiseBurst(durationMs = 150)
        }
        vibrate(40)
    }

    fun playVictory() {
        if (!prefs.isSoundEnabled) return
        scope.launch {
            val notes = listOf(523.25f, 659.25f, 783.99f, 1046.50f)
            for (note in notes) {
                playTone(note, durationMs = 80)
            }
        }
        vibrate(120)
    }

    fun playCookFlip() {
        if (!prefs.isSoundEnabled) return
        scope.launch {
            playSweepTone(500f, 750f, durationMs = 90)
            playNoiseBurst(durationMs = 100)
        }
        vibrate(45)
    }

    private fun vibrate(durationMs: Long) {
        if (!prefs.isHapticEnabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        } catch (_: Exception) {
            // Gracefully ignore if device disables haptics
        }
    }

    private fun playTone(freq: Float, durationMs: Int) {
        val sampleRate = 22050
        val numSamples = (durationMs * sampleRate) / 1000
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val angle = 2.0 * Math.PI * i / (sampleRate / freq)
            val envelope = (1.0 - (i.toDouble() / numSamples)).toFloat() // fade out
            buffer[i] = (sin(angle) * 30000.0 * envelope).toInt().toShort()
        }

        playBuffer(buffer, sampleRate)
    }

    private fun playSweepTone(startFreq: Float, endFreq: Float, durationMs: Int) {
        val sampleRate = 22050
        val numSamples = (durationMs * sampleRate) / 1000
        val buffer = ShortArray(numSamples)

        var phase = 0.0
        for (i in 0 until numSamples) {
            val t = i.toDouble() / numSamples
            val currentFreq = startFreq.toDouble() + (endFreq.toDouble() - startFreq.toDouble()) * t
            phase += 2.0 * Math.PI * currentFreq / sampleRate
            val envelope = (1.0 - t).coerceIn(0.0, 1.0)
            buffer[i] = (sin(phase) * 28000.0 * envelope).toInt().toShort()
        }

        playBuffer(buffer, sampleRate)
    }

    private fun playDualTone(freq1: Float, freq2: Float, durationMs: Int) {
        val sampleRate = 22050
        val numSamples = (durationMs * sampleRate) / 1000
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val angle1 = 2.0 * Math.PI * i / (sampleRate / freq1)
            val angle2 = 2.0 * Math.PI * i / (sampleRate / freq2)
            val envelope = (1.0 - (i.toDouble() / numSamples)).toFloat()
            val sample = (sin(angle1.toDouble()) + sin(angle2.toDouble())) * 0.5 * 30000.0 * envelope
            buffer[i] = sample.toInt().toShort()
        }

        playBuffer(buffer, sampleRate)
    }

    private fun playNoiseBurst(durationMs: Int) {
        val sampleRate = 22050
        val numSamples = (durationMs * sampleRate) / 1000
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val noise = (Math.random() * 2.0 - 1.0)
            val envelope = (1.0 - (i.toDouble() / numSamples)).toFloat()
            buffer[i] = (noise * 22000.0 * envelope).toInt().toShort()
        }

        playBuffer(buffer, sampleRate)
    }

    private fun playBuffer(buffer: ShortArray, sampleRate: Int) {
        try {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            // Track will release after playing
            scope.launch {
                kotlinx.coroutines.delay(buffer.size * 1000L / sampleRate + 50)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {
            // AudioTrack unavailable or device audio muted
        }
    }
}
