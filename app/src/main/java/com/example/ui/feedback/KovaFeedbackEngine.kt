package com.example.ui.feedback

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Lightweight, zero-asset synthesized UI sound & native HapticFeedbackConstants engine.
 * Synthesizes pleasant Duolingo-style micro-sounds via PCM AudioTrack on a single background thread:
 * - Tap / Toggle: soft woody pop (~48ms)
 * - Pedestal Press: crisp keycap pop (~40ms)
 * - Success / Pass: cheerful rising marimba chime chord (C5 -> E5 -> G5)
 * - Locked / Error: gentle low double-note (E3 -> C3)
 */
object KovaSoundSynthesizer {
    private const val SAMPLE_RATE = 22050
    private val audioExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "kova-sound-synth").apply { isDaemon = true }
    }

    private val tapPopPcm: ShortArray by lazy {
        synthesizeSweepPop(durationMs = 46, startHz = 660.0, endHz = 420.0, amplitude = 0.24)
    }

    private val pedestalPopPcm: ShortArray by lazy {
        synthesizeSweepPop(durationMs = 38, startHz = 540.0, endHz = 360.0, amplitude = 0.22)
    }

    private val successChimePcm: ShortArray by lazy {
        // Rising C5 -> E5 -> G5 cheerful marimba arpeggio
        synthesizeMarimbaSequence(
            notesHz = doubleArrayOf(523.25, 659.25, 783.99),
            noteStepMs = 78,
            noteTailMs = 190,
            amplitude = 0.28,
        )
    }

    private val errorLowPcm: ShortArray by lazy {
        // Gentle muted E3 -> C3 double-note
        synthesizeMarimbaSequence(
            notesHz = doubleArrayOf(196.0, 146.83),
            noteStepMs = 92,
            noteTailMs = 130,
            amplitude = 0.24,
        )
    }

    fun playTapPop() = playAsync(tapPopPcm)
    fun playPedestalPop() = playAsync(pedestalPopPcm)
    fun playSuccessChime() = playAsync(successChimePcm)
    fun playErrorLock() = playAsync(errorLowPcm)

    private fun playAsync(pcm: ShortArray) {
        audioExecutor.execute {
            runCatching {
                val byteSize = pcm.size * 2
                val minBuf = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                ).coerceAtLeast(byteSize)

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build(),
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build(),
                    )
                    .setBufferSizeInBytes(minBuf)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                try {
                    track.write(pcm, 0, pcm.size)
                    track.play()
                    val durationMs = (pcm.size * 1000L) / SAMPLE_RATE + 30L
                    Thread.sleep(durationMs)
                } finally {
                    runCatching { track.stop() }
                    runCatching { track.release() }
                }
            }
        }
    }

    private fun synthesizeSweepPop(
        durationMs: Int,
        startHz: Double,
        endHz: Double,
        amplitude: Double,
    ): ShortArray {
        val sampleCount = (SAMPLE_RATE * durationMs) / 1000
        val buffer = ShortArray(sampleCount)
        var phase = 0.0
        for (i in 0 until sampleCount) {
            val t = i.toDouble() / sampleCount.coerceAtLeast(1)
            val freq = startHz + (endHz - startHz) * t
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val attack = (i / (SAMPLE_RATE * 0.004)).coerceIn(0.0, 1.0)
            val decay = exp(-6.2 * t)
            val sample = (sin(phase) * 0.82 + sin(phase * 2.0) * 0.18) * attack * decay * amplitude
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthesizeMarimbaSequence(
        notesHz: DoubleArray,
        noteStepMs: Int,
        noteTailMs: Int,
        amplitude: Double,
    ): ShortArray {
        val totalMs = (notesHz.size - 1) * noteStepMs + noteTailMs
        val totalSamples = (SAMPLE_RATE * totalMs) / 1000
        val mix = DoubleArray(totalSamples)

        notesHz.forEachIndexed { idx, freq ->
            val startSample = (SAMPLE_RATE * idx * noteStepMs) / 1000
            val noteSamples = (SAMPLE_RATE * noteTailMs) / 1000
            for (i in 0 until noteSamples) {
                val target = startSample + i
                if (target >= totalSamples) break
                val timeSec = i.toDouble() / SAMPLE_RATE
                val attack = (timeSec / 0.006).coerceIn(0.0, 1.0)
                val decay = exp(-14.0 * timeSec)
                val fundamental = sin(2.0 * PI * freq * timeSec)
                val warmHarmonic = sin(2.0 * PI * (freq * 2.0) * timeSec) * 0.22 * exp(-24.0 * timeSec)
                mix[target] += (fundamental + warmHarmonic) * attack * decay * amplitude
            }
        }

        return ShortArray(totalSamples) { idx ->
            (mix[idx].coerceIn(-0.95, 0.95) * Short.MAX_VALUE).toInt().toShort()
        }
    }
}

@Immutable
data class KovaFeedbackController(
    val enabled: Boolean = true,
) {
    fun tap(view: View?) {
        if (!enabled) return
        view?.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        KovaSoundSynthesizer.playTapPop()
    }

    fun pedestalPress(view: View?) {
        if (!enabled) return
        view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        KovaSoundSynthesizer.playPedestalPop()
    }

    fun success(view: View?) {
        if (!enabled) return
        val constant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.CONFIRM
        } else {
            HapticFeedbackConstants.LONG_PRESS
        }
        view?.performHapticFeedback(constant)
        KovaSoundSynthesizer.playSuccessChime()
    }

    fun errorLock(view: View?) {
        if (!enabled) return
        val constant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.REJECT
        } else {
            HapticFeedbackConstants.LONG_PRESS
        }
        view?.performHapticFeedback(constant)
        KovaSoundSynthesizer.playErrorLock()
    }
}

val LocalKovaFeedback = staticCompositionLocalOf { KovaFeedbackController(enabled = true) }
