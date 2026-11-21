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
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

/**
 * Audible, zero-asset synthesized UI sound & native HapticFeedbackConstants engine.
 *
 * Uses Android's Media volume channel (USAGE_MEDIA / CONTENT_TYPE_MUSIC) with MODE_STREAM
 * at 44.1 kHz and smooth cosine attack/release envelopes so sounds play reliably across
 * Android devices and emulators even in vibrate mode—with zero harsh clicks.
 *
 * Sound cues:
 * - Tap / Toggle / Chip: soft woody pop (~56ms)
 * - Pedestal Press: crisp keycap pop (~48ms)
 * - Success / Pass / Sprint Complete: joyful rising 3-note chime (C5 -> E5 -> G5)
 * - Locked / Review Again: gentle low two-note thud (G3 -> D3)
 */
object KovaSoundSynthesizer {
    private const val SAMPLE_RATE = 44100
    private val audioExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "kova-sound-synth").apply { isDaemon = true }
    }

    private val tapPopPcm: ShortArray by lazy {
        synthesizeSweepPop(durationMs = 56, startHz = 690.0, endHz = 430.0, amplitude = 0.62)
    }

    private val pedestalPopPcm: ShortArray by lazy {
        synthesizeSweepPop(durationMs = 48, startHz = 560.0, endHz = 350.0, amplitude = 0.60)
    }

    private val successChimePcm: ShortArray by lazy {
        // Rising C5 -> E5 -> G5 bright marimba arpeggio with gentle shimmer
        synthesizeMarimbaSequence(
            notesHz = doubleArrayOf(523.25, 659.25, 783.99),
            noteStepMs = 82,
            noteTailMs = 240,
            amplitude = 0.68,
            harmonicGain = 0.28,
        )
    }

    private val softThudPcm: ShortArray by lazy {
        // Gentle muted G3 -> D3 soft low double-note for locked items & "Review Again"
        synthesizeMarimbaSequence(
            notesHz = doubleArrayOf(196.0, 146.83),
            noteStepMs = 90,
            noteTailMs = 155,
            amplitude = 0.64,
            harmonicGain = 0.14,
        )
    }

    fun playTapPop() = playAsync(tapPopPcm)
    fun playPedestalPop() = playAsync(pedestalPopPcm)
    fun playSuccessChime() = playAsync(successChimePcm)
    fun playSoftThud() = playAsync(softThudPcm)

    private fun playAsync(pcm: ShortArray) {
        audioExecutor.execute {
            runCatching {
                val minBuf = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                )
                if (minBuf <= 0) return@runCatching

                val byteSize = pcm.size * 2
                val bufferBytes = maxOf(minBuf * 2, byteSize)

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build(),
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build(),
                    )
                    .setBufferSizeInBytes(bufferBytes)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                try {
                    track.setVolume(1.0f)
                    track.play()
                    var offset = 0
                    while (offset < pcm.size) {
                        val written = track.write(
                            pcm,
                            offset,
                            pcm.size - offset,
                            AudioTrack.WRITE_BLOCKING,
                        )
                        if (written <= 0) break
                        offset += written
                    }
                    // Write a tiny silent tail so AudioTrack drains completely before stopping
                    val tailSilence = ShortArray(SAMPLE_RATE / 40)
                    track.write(tailSilence, 0, tailSilence.size, AudioTrack.WRITE_BLOCKING)
                    val durationMs = ((pcm.size + tailSilence.size) * 1000L) / SAMPLE_RATE + 25L
                    Thread.sleep(durationMs)
                } finally {
                    runCatching { track.pause() }
                    runCatching { track.flush() }
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
        val attackSamples = (SAMPLE_RATE * 0.005).toInt().coerceAtLeast(1)
        val releaseSamples = (SAMPLE_RATE * 0.012).toInt().coerceAtLeast(1)
        val buffer = ShortArray(sampleCount)
        var phase = 0.0
        for (i in 0 until sampleCount) {
            val t = i.toDouble() / sampleCount.coerceAtLeast(1)
            val freq = startHz + (endHz - startHz) * t
            phase += 2.0 * PI * freq / SAMPLE_RATE

            val attackEnv = if (i < attackSamples) {
                0.5 * (1.0 - cos(PI * i / attackSamples))
            } else {
                1.0
            }
            val releaseEnv = if (i >= sampleCount - releaseSamples) {
                val rem = (sampleCount - 1 - i).coerceAtLeast(0)
                0.5 * (1.0 - cos(PI * rem / releaseSamples))
            } else {
                1.0
            }
            val decay = exp(-5.2 * t)
            val wave = sin(phase) * 0.84 + sin(phase * 2.0) * 0.16
            val sample = wave * attackEnv * releaseEnv * decay * amplitude
            buffer[i] = (sample.coerceIn(-0.96, 0.96) * Short.MAX_VALUE).toInt().toShort()
        }
        return buffer
    }

    private fun synthesizeMarimbaSequence(
        notesHz: DoubleArray,
        noteStepMs: Int,
        noteTailMs: Int,
        amplitude: Double,
        harmonicGain: Double,
    ): ShortArray {
        val totalMs = (notesHz.size - 1) * noteStepMs + noteTailMs
        val totalSamples = (SAMPLE_RATE * totalMs) / 1000
        val mix = DoubleArray(totalSamples)
        val attackSec = 0.007
        val tailReleaseSamples = (SAMPLE_RATE * 0.018).toInt().coerceAtLeast(1)

        notesHz.forEachIndexed { idx, freq ->
            val startSample = (SAMPLE_RATE * idx * noteStepMs) / 1000
            val noteSamples = (SAMPLE_RATE * noteTailMs) / 1000
            for (i in 0 until noteSamples) {
                val target = startSample + i
                if (target >= totalSamples) break
                val timeSec = i.toDouble() / SAMPLE_RATE
                val attack = if (timeSec < attackSec) {
                    0.5 * (1.0 - cos(PI * timeSec / attackSec))
                } else {
                    1.0
                }
                val decay = exp(-11.5 * timeSec)
                val fundamental = sin(2.0 * PI * freq * timeSec)
                val warmHarmonic = sin(2.0 * PI * (freq * 2.0) * timeSec) * harmonicGain * exp(-18.0 * timeSec)
                mix[target] += (fundamental + warmHarmonic) * attack * decay * amplitude
            }
        }

        // Apply gentle fade-out at the very end to eliminate any click
        for (k in 0 until tailReleaseSamples) {
            val idx = totalSamples - 1 - k
            if (idx < 0) break
            val fade = 0.5 * (1.0 - cos(PI * k / tailReleaseSamples))
            mix[idx] *= fade
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
        performReliableHaptic(view, HapticFeedbackConstants.CLOCK_TICK)
        KovaSoundSynthesizer.playTapPop()
    }

    fun pedestalPress(view: View?) {
        if (!enabled) return
        performReliableHaptic(view, HapticFeedbackConstants.KEYBOARD_TAP)
        KovaSoundSynthesizer.playPedestalPop()
    }

    fun success(view: View?) {
        if (!enabled) return
        val constant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.CONFIRM
        } else {
            HapticFeedbackConstants.LONG_PRESS
        }
        performReliableHaptic(view, constant)
        KovaSoundSynthesizer.playSuccessChime()
    }

    fun errorLock(view: View?) {
        if (!enabled) return
        val constant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.REJECT
        } else {
            HapticFeedbackConstants.LONG_PRESS
        }
        performReliableHaptic(view, constant)
        KovaSoundSynthesizer.playSoftThud()
    }

    fun softThud(view: View?) {
        if (!enabled) return
        performReliableHaptic(view, HapticFeedbackConstants.KEYBOARD_TAP)
        KovaSoundSynthesizer.playSoftThud()
    }

    @Suppress("DEPRECATION")
    private fun performReliableHaptic(view: View?, constant: Int) {
        if (view == null) return
        runCatching {
            val fired = view.performHapticFeedback(
                constant,
                HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING,
            )
            if (!fired) {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
        }
    }
}

val LocalKovaFeedback = staticCompositionLocalOf { KovaFeedbackController(enabled = true) }
