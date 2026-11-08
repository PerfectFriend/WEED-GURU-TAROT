package com.example.audio

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

data class RadioStation(
    val id: String,
    val name: String,
    val genre: String,
    val description: String,
    val streamUrl: String,
    val icon: String = "📻",
    val accentColor: Color = Color(0xFF00F5FF),
    val bitrate: String = "128K MP3"
)

data class RadioState(
    val currentStation: RadioStation,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val volume: Float = 0.85f,
    val errorMessage: String? = null,
    val visualizerBars: List<Float> = List(12) { 0.15f },
    val isConsoleOpen: Boolean = false
)

/**
 * High-performance internet radio player for curated DI.FM-style electronic,
 * chillout, psybient, and synthwave streams.
 */
object CyberRadioPlayer {
    private const val TAG = "CyberRadioPlayer"

    val stations = listOf(
        RadioStation(
            id = "di_chillout",
            name = "DI / Cyber Chillout",
            genre = "Chillout, Ambient Electronica, Psybient",
            description = "Безупречный чилаут, эмбиент и даунтемпо для медитации и глубоких размышлений.",
            streamUrl = "https://ice1.somafm.com/groovesalad-128-mp3",
            icon = "🌿",
            accentColor = Color(0xFF00FF66),
            bitrate = "128K MP3"
        ),
        RadioStation(
            id = "di_synthwave",
            name = "DI / Cyberpunk Dark Synth",
            genre = "Dark Electro, Synthwave, Hacker Beats",
            description = "Музыка киберпанк-подполья, темные синтезаторы и драйвовый электронный ритм.",
            streamUrl = "https://ice1.somafm.com/defcon-128-mp3",
            icon = "⚡",
            accentColor = Color(0xFF00F5FF),
            bitrate = "128K MP3"
        ),
        RadioStation(
            id = "di_dronezone",
            name = "DI / Deep Space Psychill",
            genre = "Deep Space Ambient, Drones, Psychill",
            description = "Глубокие космические дроны и психоделические звуковые ландшафты.",
            streamUrl = "https://ice1.somafm.com/dronezone-128-mp3",
            icon = "🔮",
            accentColor = Color(0xFFBD00FF),
            bitrate = "128K MP3"
        ),
        RadioStation(
            id = "di_deeptech",
            name = "DI / Deep Tech & Minimal",
            genre = "Deep House, Minimal Tech, Hypnotic",
            description = "Гипнотический дип-тек и плотный минимал-грув для фокуса и карточных дуэлей.",
            streamUrl = "https://ice1.somafm.com/beatblender-128-mp3",
            icon = "🌑",
            accentColor = Color(0xFFFFB800),
            bitrate = "128K MP3"
        ),
        RadioStation(
            id = "di_spacestation",
            name = "DI / Space Station Trance",
            genre = "Ambient Trance, Spacemusic, Progressive",
            description = "Мелодичный прогрессив и космический транс с орбитальной станции.",
            streamUrl = "https://ice1.somafm.com/spacestation-128-mp3",
            icon = "🛰️",
            accentColor = Color(0xFF00E5FF),
            bitrate = "128K MP3"
        ),
        RadioStation(
            id = "di_goa_dub",
            name = "DI / Suburbs of Goa & Psydub",
            genre = "World Psybient, Organic Dub, Ethnic Chill",
            description = "Психоделический органический даб, восточные мотивы и плотный суб-бас.",
            streamUrl = "https://ice1.somafm.com/suburbsofgoa-128-mp3",
            icon = "🕉️",
            accentColor = Color(0xFFFF5252),
            bitrate = "128K MP3"
        ),
        RadioStation(
            id = "di_vaporwaves",
            name = "DI / Vaporwaves & Dreamwave",
            genre = "Vaporwave, 80s Dreamwave, Retro Cyber",
            description = "Ностальгический неоновый вайб 80-х, дримвейв и ретро-эстетика киберпространства.",
            streamUrl = "https://ice1.somafm.com/vaporwaves-128-mp3",
            icon = "🌆",
            accentColor = Color(0xFFFF007F),
            bitrate = "128K MP3"
        ),
        RadioStation(
            id = "di_secretagent",
            name = "DI / Noir Agent Lounge",
            genre = "Spy Surf, Downtempo Electric Lounge",
            description = "Атмосферный лаунж шпионского нуара и стильные ретро-инструменталы.",
            streamUrl = "https://ice1.somafm.com/secretagent-128-mp3",
            icon = "🕶️",
            accentColor = Color(0xFF39FF14),
            bitrate = "128K MP3"
        ),
        RadioStation(
            id = "di_missioncontrol",
            name = "DI / Mission Control Ambient",
            genre = "Telemetry, Deep Ambient, Space Comms",
            description = "Атмосферный эмбиент с реальными радиопереговорами космических миссий.",
            streamUrl = "https://ice1.somafm.com/missioncontrol-128-mp3",
            icon = "🚀",
            accentColor = Color(0xFF00F0FF),
            bitrate = "128K MP3"
        ),
        RadioStation(
            id = "di_cliqhop",
            name = "DI / Glitch & IDM Circuit",
            genre = "IDM, Glitch, Experimental Electro",
            description = "Интеллектуальная электроника, глитич-ритмы и кибернетические текстуры.",
            streamUrl = "https://ice1.somafm.com/cliqhop-128-mp3",
            icon = "🔬",
            accentColor = Color(0xFFE040FB),
            bitrate = "128K MP3"
        )
    )

    private val _state = MutableStateFlow(
        RadioState(currentStation = stations[0])
    )
    val state: StateFlow<RadioState> = _state.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var visualizerJob: Job? = null

    init {
        startVisualizerTicker()
    }

    fun openConsole() {
        _state.value = _state.value.copy(isConsoleOpen = true)
    }

    fun closeConsole() {
        _state.value = _state.value.copy(isConsoleOpen = false)
    }

    fun toggleConsole() {
        _state.value = _state.value.copy(isConsoleOpen = !_state.value.isConsoleOpen)
    }

    fun play(station: RadioStation) {
        val curr = _state.value
        _state.value = curr.copy(
            currentStation = station,
            isBuffering = true,
            isPlaying = false,
            errorMessage = null
        )

        // Make sure synth drone is off when radio starts
        CyberNoirSynthPlayer.toggleAmbient(false)

        scope.launch(Dispatchers.IO) {
            try {
                releasePlayer()
                val player = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    setDataSource(station.streamUrl)
                    setVolume(curr.volume, curr.volume)
                    setOnPreparedListener { mp ->
                        mp.start()
                        scope.launch(Dispatchers.Main) {
                            _state.value = _state.value.copy(
                                isBuffering = false,
                                isPlaying = true,
                                errorMessage = null
                            )
                        }
                    }
                    setOnErrorListener { _, what, extra ->
                        Log.e(TAG, "MediaPlayer error: what=$what extra=$extra")
                        scope.launch(Dispatchers.Main) {
                            _state.value = _state.value.copy(
                                isBuffering = false,
                                isPlaying = false,
                                errorMessage = "Ошибка потока: подключение прервано. Попробуйте другой канал."
                            )
                        }
                        true
                    }
                    setOnCompletionListener {
                        scope.launch(Dispatchers.Main) {
                            _state.value = _state.value.copy(
                                isBuffering = false,
                                isPlaying = false
                            )
                        }
                    }
                }
                player.prepareAsync()
                mediaPlayer = player
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start radio stream", e)
                scope.launch(Dispatchers.Main) {
                    _state.value = _state.value.copy(
                        isBuffering = false,
                        isPlaying = false,
                        errorMessage = "Сбой соединения: ${e.message}"
                    )
                }
            }
        }
    }

    fun togglePlayPause() {
        val curr = _state.value
        if (curr.isPlaying) {
            pause()
        } else {
            if (mediaPlayer != null) {
                try {
                    mediaPlayer?.start()
                    _state.value = curr.copy(isPlaying = true, isBuffering = false)
                    CyberNoirSynthPlayer.toggleAmbient(false)
                } catch (e: Exception) {
                    play(curr.currentStation)
                }
            } else {
                play(curr.currentStation)
            }
        }
    }

    fun pause() {
        try {
            mediaPlayer?.pause()
            _state.value = _state.value.copy(isPlaying = false, isBuffering = false)
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing", e)
            stop()
        }
    }

    fun stop() {
        releasePlayer()
        _state.value = _state.value.copy(isPlaying = false, isBuffering = false)
    }

    fun nextStation() {
        val currIndex = stations.indexOfFirst { it.id == _state.value.currentStation.id }
        val nextIndex = (currIndex + 1) % stations.size
        play(stations[nextIndex])
    }

    fun prevStation() {
        val currIndex = stations.indexOfFirst { it.id == _state.value.currentStation.id }
        val prevIndex = if (currIndex <= 0) stations.size - 1 else currIndex - 1
        play(stations[prevIndex])
    }

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _state.value = _state.value.copy(volume = clamped)
        try {
            mediaPlayer?.setVolume(clamped, clamped)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting volume", e)
        }
    }

    private fun releasePlayer() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing player", e)
        } finally {
            mediaPlayer = null
        }
    }

    private fun startVisualizerTicker() {
        visualizerJob?.cancel()
        visualizerJob = scope.launch {
            while (isActive) {
                delay(120)
                val curr = _state.value
                val newBars = if (curr.isPlaying) {
                    List(12) { i ->
                        val base = 0.35f + (sin(i * 0.8f + System.currentTimeMillis() / 250.0).toFloat() * 0.25f)
                        val noise = Random.nextFloat() * 0.4f
                        (base + noise).coerceIn(0.15f, 1.0f)
                    }
                } else if (curr.isBuffering) {
                    List(12) { i ->
                        val phase = (System.currentTimeMillis() / 150.0).toInt() % 12
                        if (i == phase) 0.9f else 0.2f
                    }
                } else {
                    List(12) { 0.12f }
                }
                _state.value = _state.value.copy(visualizerBars = newBars)
            }
        }
    }

    private fun sin(rad: Double): Double = kotlin.math.sin(rad)
}
