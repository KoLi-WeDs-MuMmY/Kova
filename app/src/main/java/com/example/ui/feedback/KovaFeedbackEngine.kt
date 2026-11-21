package com.example.ui.feedback

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

/**
 * Rich, zero-asset, instant-restart synthesized UI sound & native HapticFeedbackConstants engine.
 *
 * Key properties:
 * - Routes through Android's Media volume channel (USAGE_MEDIA / CONTENT_TYPE_MUSIC) so cues
 *   remain audible even when the device is on vibrate.
 * - Single-slot conflating playback with an atomic generation counter: whenever a new trigger
 *   fires while another cue is playing, the active AudioTrack is immediately paused & flushed
 *   back to sample 0 so rapid clicks NEVER stack up or echo after tapping stops.
 * - 6+ Duolingo-inspired crisp tactile cues with smooth raised-cosine envelopes (zero clicks).
 */
object KovaSoundSynthesizer {
    private const val SAMPLE_RATE = 44100
    private const val CHUNK_SAMPLES = 384 // ~8.7ms per chunk for instant preemption

    private val lock = Object()
    private val generation = AtomicLong(0L)

    @Volatile
    private var activeTrack: AudioTrack? = null

    private var pendingPcm: ShortArray? = null
    private var pendingGeneration: Long = 0L

    private val workerThread = Thread({ playbackLoop() }, "kova-sound-synth-worker").apply {
        isDaemon = true
        priority = Thread.MAX_PRIORITY - 1
        start()
    }

    // 1. Tap / chip / toggle pop (crisp woody bubble pop ~48ms)
    private val tapPopPcm: ShortArray by lazy {
        synthesizeSweepPop(durationMs = 48, startHz = 740.0, endHz = 460.0, amplitude = 0.60)
    }

    // 2. Pedestal press (slightly deeper keycap thock ~52ms)
    private val pedestalPopPcm: ShortArray by lazy {
        synthesizeSweepPop(durationMs = 52, startHz = 580.0, endHz = 360.0, amplitude = 0.62)
    }

    // 3. Chapter focus expand / drop-down (warm rising velvet swoosh-pop ~118ms)
    private val chapterSwooshPcm: ShortArray by lazy {
        synthesizeChapterSwoosh(durationMs = 118, amplitude = 0.64)
    }

    // 4. Split-ratio cycle (75:25 -> 50:50 -> 25:75 mechanical double-detent tick ~66ms)
    private val splitTickPcm: ShortArray by lazy {
        synthesizeMechanicalDoubleTick(amplitude = 0.62)
    }

    // 5. Flashcard 3D flip (light breezy snap-whoosh ~76ms)
    private val cardFlipPcm: ShortArray by lazy {
        synthesizeCardFlip(durationMs = 76, amplitude = 0.58)
    }

    // 6. "Got It" / Note Saved (bright ascending 2-note spark chime E5 -> B5)
    private val gotItChimePcm: ShortArray by lazy {
        synthesizeMarimbaSequence(
            notesHz = doubleArrayOf(659.25, 987.77),
            noteStepMs = 70,
            noteTailMs = 190,
            amplitude = 0.64,
            harmonicGain = 0.30,
        )
    }

    // 7. "Review Again" / Locked item (gentle low double-thud G3 -> D3)
    private val softThudPcm: ShortArray by lazy {
        synthesizeMarimbaSequence(
            notesHz = doubleArrayOf(196.0, 146.83),
            noteStepMs = 82,
            noteTailMs = 145,
            amplitude = 0.65,
            harmonicGain = 0.14,
        )
    }

    // 8. Lesson / Mock Test Pass / Assignment / Sprint Finish (joyful 4-note rising arpeggio C5 -> E5 -> G5 -> C6)
    private val celebrationArpeggioPcm: ShortArray by lazy {
        synthesizeMarimbaSequence(
            notesHz = doubleArrayOf(523.25, 659.25, 783.99, 1046.50),
            noteStepMs = 74,
            noteTailMs = 260,
            amplitude = 0.68,
            harmonicGain = 0.32,
        )
    }

    fun playTapPop() = triggerInstantRestart(tapPopPcm)
    fun playPedestalPop() = triggerInstantRestart(pedestalPopPcm)
    fun playChapterSwoosh() = triggerInstantRestart(chapterSwooshPcm)
    fun playSplitTick() = triggerInstantRestart(splitTickPcm)
    fun playCardFlip() = triggerInstantRestart(cardFlipPcm)
    fun playGotItChime() = triggerInstantRestart(gotItChimePcm)
    fun playSoftThud() = triggerInstantRestart(softThudPcm)
    fun playSuccessChime() = triggerInstantRestart(celebrationArpeggioPcm)

    /**
     * Immediately interrupts any currently playing sound, flushes its AudioTrack back to 0,
     * replaces any queued request (conflating rapid taps), and restarts cleanly from sample 0.
     */
    private fun triggerInstantRestart(pcm: ShortArray) {
        // Ensure worker thread reference is initialized
        workerThread.isAlive
        val nextGen = generation.incrementAndGet()

        // Immediately flush any active track so playback cuts off cleanly right away
        activeTrack?.let { track ->
            runCatching {
                track.pause()
                track.flush()
            }
        }

        synchronized(lock) {
            pendingPcm = pcm
            pendingGeneration = nextGen
            lock.notifyAll()
        }
    }

    private fun playbackLoop() {
        while (true) {
            var pcmToPlay: ShortArray?
            var targetGen: Long

            synchronized(lock) {
                while (pendingPcm == null) {
                    runCatching { lock.wait() }
                }
                pcmToPlay = pendingPcm
                targetGen = pendingGeneration
                pendingPcm = null
            }

            val samples = pcmToPlay ?: continue
            if (generation.get() != targetGen) continue

            playInterruptible(samples, targetGen)
        }
    }

    private fun playInterruptible(pcm: ShortArray, targetGen: Long) {
        runCatching {
            val minBuf = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
            )
            if (minBuf <= 0) return

            val bufferBytes = maxOf(minBuf * 2, CHUNK_SAMPLES * 4 * 2)
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

            activeTrack = track
            try {
                if (generation.get() != targetGen) return
                track.setVolume(1.0f)
                track.play()

                var offset = 0
                while (offset < pcm.size && generation.get() == targetGen) {
                    val toWrite = minOf(CHUNK_SAMPLES, pcm.size - offset)
                    val written = track.write(
                        pcm,
                        offset,
                        toWrite,
                        AudioTrack.WRITE_BLOCKING,
                    )
                    if (written <= 0) break
                    offset += written
                }

                // Short interruptible drain only if this sound wasn't superseded
                if (generation.get() == targetGen) {
                    val silence = ShortArray(CHUNK_SAMPLES * 2)
                    track.write(silence, 0, silence.size, AudioTrack.WRITE_BLOCKING)
                    val drainDeadline = System.currentTimeMillis() + 55L
                    synchronized(lock) {
                        while (generation.get() == targetGen) {
                            val remaining = drainDeadline - System.currentTimeMillis()
                            if (remaining <= 0L) break
                            runCatching { lock.wait(remaining) }
                        }
                    }
                }
            } finally {
                if (activeTrack === track) {
                    activeTrack = null
                }
                runCatching { track.pause() }
                runCatching { track.flush() }
                runCatching { track.stop() }
                runCatching { track.release() }
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
        val attackSamples = (SAMPLE_RATE * 0.004).toInt().coerceAtLeast(1)
        val releaseSamples = (SAMPLE_RATE * 0.011).toInt().coerceAtLeast(1)
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
            val decay = exp(-5.4 * t)
            val wave = sin(phase) * 0.84 + sin(phase * 2.0) * 0.16
            val sample = wave * attackEnv * releaseEnv * decay * amplitude
            buffer[i] = (sample.coerceIn(-0.96, 0.96) * Short.MAX_VALUE).toInt().toShort()
        }
        return buffer
    }

    private fun synthesizeChapterSwoosh(
        durationMs: Int,
        amplitude: Double,
    ): ShortArray {
        val sampleCount = (SAMPLE_RATE * durationMs) / 1000
        val attackSamples = (SAMPLE_RATE * 0.016).toInt().coerceAtLeast(1)
        val releaseSamples = (SAMPLE_RATE * 0.024).toInt().coerceAtLeast(1)
        val buffer = ShortArray(sampleCount)
        var phase = 0.0
        for (i in 0 until sampleCount) {
            val t = i.toDouble() / sampleCount.coerceAtLeast(1)
            // Warm rising bell-swoosh from 310Hz -> 620Hz with a crisp upper fifth sparkle
            val freq = 310.0 + 310.0 * (1.0 - exp(-3.4 * t))
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val attack = if (i < attackSamples) 0.5 * (1.0 - cos(PI * i / attackSamples)) else 1.0
            val release = if (i >= sampleCount - releaseSamples) {
                val rem = (sampleCount - 1 - i).coerceAtLeast(0)
                0.5 * (1.0 - cos(PI * rem / releaseSamples))
            } else {
                1.0
            }
            val decay = exp(-2.8 * t)
            val body = sin(phase) * 0.72 + sin(phase * 1.5) * 0.20 + sin(phase * 2.0) * 0.08
            val sample = body * attack * release * decay * amplitude
            buffer[i] = (sample.coerceIn(-0.95, 0.95) * Short.MAX_VALUE).toInt().toShort()
        }
        return buffer
    }

    private fun synthesizeMechanicalDoubleTick(amplitude: Double): ShortArray {
        val totalSamples = (SAMPLE_RATE * 66) / 1000
        val mix = DoubleArray(totalSamples)
        val clicks = listOf(0 to 1180.0, (SAMPLE_RATE * 26) / 1000 to 1560.0)
        val clickSamples = (SAMPLE_RATE * 22) / 1000
        clicks.forEach { (start, freq) ->
            for (i in 0 until clickSamples) {
                val idx = start + i
                if (idx >= totalSamples) break
                val t = i.toDouble() / SAMPLE_RATE
                val attack = if (i < 60) 0.5 * (1.0 - cos(PI * i / 60.0)) else 1.0
                val decay = exp(-145.0 * t)
                val tone = sin(2.0 * PI * freq * t) * 0.78 + sin(2.0 * PI * freq * 2.3 * t) * 0.22
                mix[idx] += tone * attack * decay * amplitude
            }
        }
        return ShortArray(totalSamples) { i ->
            (mix[i].coerceIn(-0.95, 0.95) * Short.MAX_VALUE).toInt().toShort()
        }
    }

    private fun synthesizeCardFlip(
        durationMs: Int,
        amplitude: Double,
    ): ShortArray {
        val sampleCount = (SAMPLE_RATE * durationMs) / 1000
        val buffer = ShortArray(sampleCount)
        var phase = 0.0
        var lcg = 0x5DEECE66DL
        for (i in 0 until sampleCount) {
            val t = i.toDouble() / sampleCount.coerceAtLeast(1)
            // Soft filtered air-whoosh + crisp rising flip snap
            val freq = 380.0 + 460.0 * t
            phase += 2.0 * PI * freq / SAMPLE_RATE
            lcg = (lcg * 0x5DEECE66DL + 0xBL) and ((1L shl 48) - 1)
            val noise = (((lcg ushr 32).toInt().toDouble() / Int.MAX_VALUE) * 2.0 - 1.0)
            val env = sin(PI * t) * exp(-2.4 * t)
            val wave = sin(phase) * 0.78 + noise * 0.14
            val sample = wave * env * amplitude
            buffer[i] = (sample.coerceIn(-0.95, 0.95) * Short.MAX_VALUE).toInt().toShort()
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
        val attackSec = 0.006
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
                val decay = exp(-11.2 * timeSec)
                val fundamental = sin(2.0 * PI * freq * timeSec)
                val warmHarmonic = sin(2.0 * PI * (freq * 2.0) * timeSec) * harmonicGain * exp(-17.5 * timeSec)
                mix[target] += (fundamental + warmHarmonic) * attack * decay * amplitude
            }
        }

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

    fun chapterSwoosh(view: View?) {
        if (!enabled) return
        performReliableHaptic(view, HapticFeedbackConstants.KEYBOARD_TAP)
        KovaSoundSynthesizer.playChapterSwoosh()
    }

    fun splitTick(view: View?) {
        if (!enabled) return
        performReliableHaptic(view, HapticFeedbackConstants.CLOCK_TICK)
        KovaSoundSynthesizer.playSplitTick()
    }

    fun cardFlip(view: View?) {
        if (!enabled) return
        performReliableHaptic(view, HapticFeedbackConstants.KEYBOARD_TAP)
        KovaSoundSynthesizer.playCardFlip()
    }

    fun gotItChime(view: View?) {
        if (!enabled) return
        val constant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.CONFIRM
        } else {
            HapticFeedbackConstants.KEYBOARD_TAP
        }
        performReliableHaptic(view, constant)
        KovaSoundSynthesizer.playGotItChime()
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
