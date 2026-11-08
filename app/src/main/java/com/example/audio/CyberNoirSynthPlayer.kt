package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sin
import kotlin.math.PI

/**
 * A real-time, low-overhead digital audio synthesizer that generates a warm,
 * botanical organic landscape (72Hz earth photosynthesis hum, slowly sweeping
 * relaxing lo-fi garden drone chords) and interactive warm acoustic feedback.
 * Uses Android's AudioTrack and Kotlin Coroutines for seamless playback.
 */
object CyberNoirSynthPlayer {
    private const val TAG = "GanjaSynthPlayer"
    private const val SAMPLE_RATE = 22050

    // Sound toggle state
    private val _isAmbientOn = MutableStateFlow(false)
    val isAmbientOn: StateFlow<Boolean> = _isAmbientOn.asStateFlow()

    private var audioJob: Job? = null
    private val synthScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Direct FX trigger parameters
    private var fxPhase = 0.0
    private var fxDurationSamples = 0
    private var fxActiveType = FXType.NONE
    private var fxRemainingSamples = 0

    enum class FXType {
        NONE, CARD_DRAW, SCAN_BEEP, SUCCESS_CHIME, CLICK, LASER_CLASH, SHIELD_BLOCK, COMBAT_VICTORY
    }

    /**
     * Toggles the ambient soundscape on/off.
     */
    fun toggleAmbient(enabled: Boolean) {
        _isAmbientOn.value = enabled
        if (enabled) {
            startSynth()
        } else {
            stopSynth()
        }
    }

    /**
     * Triggers a specialized cybernetic sound effect integrated directly into the audio stream.
     */
    fun triggerFX(type: FXType) {
        if (!_isAmbientOn.value) return // Only play FX if sound is enabled

        synchronized(this) {
            fxActiveType = type
            fxPhase = 0.0
            fxDurationSamples = when (type) {
                FXType.NONE -> 0
                FXType.CLICK -> (SAMPLE_RATE * 0.08).toInt() // 80ms short click
                FXType.CARD_DRAW -> (SAMPLE_RATE * 0.45).toInt() // 450ms sweep
                FXType.SCAN_BEEP -> (SAMPLE_RATE * 0.35).toInt() // 350ms rhythmic pulse
                FXType.SUCCESS_CHIME -> (SAMPLE_RATE * 0.8).toInt() // 800ms chord sweep
                FXType.LASER_CLASH -> (SAMPLE_RATE * 0.3).toInt() // 300ms energetic laser strike
                FXType.SHIELD_BLOCK -> (SAMPLE_RATE * 0.25).toInt() // 250ms metallic resonance
                FXType.COMBAT_VICTORY -> (SAMPLE_RATE * 1.2).toInt() // 1.2s triumphant synth arpeggio
            }
            fxRemainingSamples = fxDurationSamples
        }
        Log.d(TAG, "Triggered FX: $type")
    }

    private fun startSynth() = synchronized(this) {
        if (audioJob != null) return

        audioJob = synthScope.launch {
            var audioTrack: AudioTrack? = null
            try {
                val minBufferSize = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                // Use a stable buffer size
                val bufferSize = (minBufferSize * 2).coerceAtLeast(2048)

                audioTrack = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    AudioTrack.Builder()
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(SAMPLE_RATE)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build()
                        )
                        .setBufferSizeInBytes(bufferSize)
                        .setTransferMode(AudioTrack.MODE_STREAM)
                        .build()
                } else {
                    @Suppress("DEPRECATION")
                    AudioTrack(
                        AudioManager.STREAM_MUSIC,
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize,
                        AudioTrack.MODE_STREAM
                    )
                }

                audioTrack.play()

                val shortBuffer = ShortArray(1024)
                var globalPhase = 0.0
                var dronePhase = 0.0
                var pulsePhase = 0.0
                var fadeMultiplier = 0.0f // Smooth fade-in on start

                while (coroutineContext.isActive && _isAmbientOn.value) {
                    for (i in shortBuffer.indices) {
                        // 1. Core Ambient Deep Hum (72Hz earth/photosynthesis heartbeat hum)
                        // Modulated gently to simulate the organic, breathing rhythm of a living greenhouse
                        val humMod = sin(2.0 * PI * 0.15 * (globalPhase / SAMPLE_RATE)) * 0.5
                        val humFreq = 72.0 + humMod
                        val humSample = sin(2.0 * PI * humFreq * (globalPhase / SAMPLE_RATE)) * 0.22

                        // 2. Slow Sweeping Organic Lofi Pad Drone (Warm acoustic resonances in Major-7th)
                        // Sweeping between 146.83Hz (D3) and 220.0Hz (A3) for a very relaxing botanical vibe
                        val lfo = (sin(2.0 * PI * 0.04 * (dronePhase / SAMPLE_RATE)) + 1.0) / 2.0 // 0.0 to 1.0 slowly
                        val droneFreq = 146.83 + lfo * 73.17 // Sweeps from 146.83Hz to 220.0Hz
                        val droneSample1 = sin(2.0 * PI * droneFreq * (dronePhase / SAMPLE_RATE)) * 0.16
                        val droneSample2 = sin(2.0 * PI * (droneFreq * 1.2) * (dronePhase / SAMPLE_RATE)) * 0.08 // Soft third harmonic

                        // 3. Rhythmic garden rain drops (simulating gentle morning dew sliding down leaves)
                        val pulseLfo = sin(2.0 * PI * 0.5 * (pulsePhase / SAMPLE_RATE)) // periodic sweep
                        val pulseSample = if (pulseLfo > 0.96) {
                            sin(2.0 * PI * 330.0 * (pulsePhase / SAMPLE_RATE)) * 0.04 * (pulseLfo - 0.96) * 25.0
                        } else 0.0

                        // Mix ambient layers
                        var mixedSample = humSample + droneSample1 + droneSample2 + pulseSample

                        // 4. Blending dynamic interactive warm acoustic FX
                        var fxSample = 0.0
                        synchronized(this@CyberNoirSynthPlayer) {
                            if (fxRemainingSamples > 0) {
                                val progress = (fxDurationSamples - fxRemainingSamples).toDouble() / fxDurationSamples
                                when (fxActiveType) {
                                    FXType.CLICK -> {
                                        // Gentle woody snap (snap of a twig or warm tap)
                                        fxSample = sin(2.0 * PI * 330.0 * fxPhase) * 0.20 * (1.0 - progress)
                                        fxPhase += 330.0 / SAMPLE_RATE
                                    }
                                    FXType.CARD_DRAW -> {
                                        // Soft wind blow (breeze rustling green leaves)
                                        val currentFreq = 180.0 + (120.0 * progress)
                                        fxSample = sin(2.0 * PI * currentFreq * fxPhase) * 0.18 * (1.0 - progress)
                                        fxPhase += currentFreq / SAMPLE_RATE
                                    }
                                    FXType.SCAN_BEEP -> {
                                        // Calming water droplet ripple
                                        val currentFreq = 600.0 - (400.0 * progress)
                                        val s1 = sin(2.0 * PI * currentFreq * fxPhase)
                                        fxSample = s1 * 0.15 * (1.0 - progress)
                                        fxPhase += currentFreq / SAMPLE_RATE
                                    }
                                    FXType.SUCCESS_CHIME -> {
                                        // Warm botanical D Major 7th ascending blossom arpeggio
                                        val scaleNote = when {
                                            progress < 0.25 -> 146.83 // D3
                                            progress < 0.50 -> 185.00 // F#3
                                            progress < 0.75 -> 220.00 // A3
                                            else -> 277.18 // C#4
                                        }
                                        fxSample = sin(2.0 * PI * scaleNote * fxPhase) * 0.22 * (1.0 - progress)
                                        fxPhase += scaleNote / SAMPLE_RATE
                                    }
                                    FXType.LASER_CLASH -> {
                                        // Rapid frequency drop laser strike with saw-like harmonics
                                        val freq = 880.0 * (1.0 - progress * 0.7)
                                        val fundamental = sin(2.0 * PI * freq * fxPhase)
                                        val sub = sin(2.0 * PI * (freq * 0.5) * fxPhase) * 0.4
                                        fxSample = (fundamental + sub) * 0.26 * (1.0 - progress)
                                        fxPhase += freq / SAMPLE_RATE
                                    }
                                    FXType.SHIELD_BLOCK -> {
                                        // Deep resonant metallic deflection ping
                                        val freq = 220.0 + sin(progress * 15.0) * 40.0
                                        fxSample = sin(2.0 * PI * freq * fxPhase) * 0.28 * (1.0 - progress)
                                        fxPhase += freq / SAMPLE_RATE
                                    }
                                    FXType.COMBAT_VICTORY -> {
                                        // Triumphant cyber-fanfare chord progression
                                        val note = when {
                                            progress < 0.20 -> 261.63 // C4
                                            progress < 0.40 -> 329.63 // E4
                                            progress < 0.60 -> 392.00 // G4
                                            progress < 0.80 -> 523.25 // C5
                                            else -> 659.25            // E5
                                        }
                                        val lead = sin(2.0 * PI * note * fxPhase) * 0.25
                                        val shimmer = sin(2.0 * PI * (note * 2.0) * fxPhase) * 0.1
                                        fxSample = (lead + shimmer) * (1.0 - progress * 0.4)
                                        fxPhase += note / SAMPLE_RATE
                                    }
                                    FXType.NONE -> {}
                                }
                                fxRemainingSamples--
                                if (fxRemainingSamples <= 0) {
                                    fxActiveType = FXType.NONE
                                }
                            }
                        }

                        mixedSample += fxSample

                        // Smoothly ramp up on start to prevent pops
                        if (fadeMultiplier < 1.0f) {
                            fadeMultiplier += 0.0001f
                        }
                        mixedSample *= fadeMultiplier

                        // Clamp value to safe 16-bit range
                        val finalSample = (mixedSample * Short.MAX_VALUE).coerceIn(Short.MIN_VALUE.toDouble(), Short.MAX_VALUE.toDouble())
                        shortBuffer[i] = finalSample.toInt().toShort()

                        // Advance independent phases
                        globalPhase += 1.0
                        dronePhase += 1.002 // slight detune mismatch to make movement rich
                        pulsePhase += 0.998
                    }

                    // Write synthesized frame block to AudioTrack
                    audioTrack.write(shortBuffer, 0, shortBuffer.size)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Audio synthesis thread error: ${e.message}", e)
            } finally {
                try {
                    audioTrack?.stop()
                    audioTrack?.release()
                } catch (e: Exception) {
                    Log.e(TAG, "Error releasing audio track: ${e.message}")
                }
                Log.d(TAG, "Synthesizer thread stopped.")
            }
        }
    }

    private fun stopSynth() = synchronized(this) {
        audioJob?.cancel()
        audioJob = null
    }
}
