package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.CyberRadioPlayer
import com.example.ui.theme.*

@Composable
fun CyberRadioMiniBar(
    modifier: Modifier = Modifier,
    systemLanguage: String = "en",
    onOpenConsole: () -> Unit
) {
    val state by CyberRadioPlayer.state.collectAsStateWithLifecycle()
    val station = state.currentStation
    val isPlaying = state.isPlaying
    val isBuffering = state.isBuffering
    val c = Cyber.colors
    val isRu = systemLanguage in listOf("ru", "uk")

    Surface(
        onClick = onOpenConsole,
        shape = CyberShapes.card,
        color = c.panel.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, station.accentColor.copy(alpha = if (isPlaying) 0.8f else 0.35f)),
        shadowElevation = if (isPlaying) 8.dp else 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .testTag("cyber_radio_mini_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Space.md, vertical = Space.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(station.accentColor.copy(alpha = 0.2f))
                        .border(1.dp, station.accentColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = station.icon,
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                Spacer(modifier = Modifier.width(Space.sm))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = station.name,
                            style = MaterialTheme.typography.labelLarge,
                            fontFamily = IbmPlexMonoFontFamily,
                            color = c.textHigh,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(Space.xs))
                        if (isPlaying) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPure)
                            )
                        }
                    }
                    Text(
                        text = if (isBuffering) {
                            if (isRu) "ПОДКЛЮЧЕНИЕ К ПОТОКУ..." else "CONNECTING TO STREAM..."
                        } else {
                            station.genre
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isBuffering) NeonAmber else c.textMid,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                modifier = Modifier
                    .width(42.dp)
                    .height(22.dp)
                    .padding(horizontal = Space.xs),
                horizontalArrangement = Arrangement.spacedBy(Space.xxs),
                verticalAlignment = Alignment.Bottom
            ) {
                state.visualizerBars.take(6).forEach { barHeight ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(barHeight.coerceIn(0.15f, 1f))
                            .clip(CyberShapes.chip)
                            .background(station.accentColor)
                    )
                }
            }

            Spacer(modifier = Modifier.width(Space.sm))

            IconButton(
                onClick = { CyberRadioPlayer.togglePlayPause() },
                modifier = Modifier
                    .size(48.dp)
                    .background(station.accentColor.copy(alpha = 0.2f), CircleShape)
                    .border(1.dp, station.accentColor, CircleShape)
            ) {
                if (isBuffering) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = station.accentColor,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRu) "Пауза / Воспроизведение" else "Play / Pause",
                        tint = station.accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CyberRadioConsoleDialog(
    systemLanguage: String = "en",
    onDismiss: () -> Unit
) {
    val state by CyberRadioPlayer.state.collectAsStateWithLifecycle()
    val station = state.currentStation
    val isPlaying = state.isPlaying
    val isBuffering = state.isBuffering
    val c = Cyber.colors
    val isRu = systemLanguage in listOf("ru", "uk")

    val motionEnabled = rememberMotionEnabled()
    val glowAlpha = if (motionEnabled) {
        val infiniteTransition = rememberInfiniteTransition(label = "radio_glow")
        infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 0.9f,
            animationSpec = infiniteRepeatable(
                animation = tween(1400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow"
        ).value
    } else {
        0.65f
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .clip(CyberShapes.sheet)
                .border(1.5.dp, station.accentColor.copy(alpha = glowAlpha), CyberShapes.sheet)
                .testTag("cyber_radio_console_dialog"),
            color = VoidNight
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Space.lg)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CyberShapes.chip)
                                .background(c.panelRaised)
                                .border(1.dp, NeonCyan.copy(alpha = 0.5f), CyberShapes.chip),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Radio,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(Space.md))
                        Column {
                            Text(
                                text = if (isRu) "КИБЕР-РАДИО" else "CYBERNETIC RADIO",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = IbmPlexMonoFontFamily,
                                color = NeonCyan
                            )
                            Text(
                                text = if (isRu) "ПОТОКИ SYNTHWAVE И ELECTRONIC" else "LIVE DI.FM & SYNTHWAVE STREAM",
                                style = MaterialTheme.typography.labelSmall,
                                color = c.textMid
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isRu) "Закрыть" else "Close",
                            tint = c.textHigh
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Space.md))

                // Now Playing Hero Card
                Surface(
                    shape = CyberShapes.card,
                    color = VoidSurface,
                    border = BorderStroke(1.dp, station.accentColor.copy(alpha = 0.7f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Space.md)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(end = Space.md)
                                        .size(48.dp)
                                        .background(station.accentColor.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = station.icon,
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                }
                                Column {
                                    Text(
                                        text = station.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontFamily = IbmPlexMonoFontFamily,
                                        color = c.textHigh,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = station.genre,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = station.accentColor
                                    )
                                }
                            }

                            Surface(
                                shape = CyberShapes.chip,
                                color = if (isPlaying) EmeraldPure.copy(alpha = 0.16f) else c.panelRaised,
                                border = BorderStroke(1.dp, if (isPlaying) EmeraldPure else c.goldAntique.copy(alpha = 0.35f))
                            ) {
                                Text(
                                    text = when {
                                        isPlaying -> "LIVE • ${station.bitrate}"
                                        isBuffering -> if (isRu) "ПОДКЛ." else "CONNECTING"
                                        else -> if (isRu) "ГОТОВ" else "READY"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = when {
                                        isPlaying -> EmeraldPure
                                        isBuffering -> NeonAmber
                                        else -> c.textMid
                                    },
                                    modifier = Modifier.padding(horizontal = Space.sm, vertical = Space.xs)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Space.sm))

                        Text(
                            text = station.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textMid
                        )

                        Spacer(modifier = Modifier.height(Space.md))

                        // Full 12-Band Equalizer Spectrum
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .background(c.ink, CyberShapes.chip)
                                .border(1.dp, c.goldAntique.copy(alpha = 0.18f), CyberShapes.chip)
                                .padding(horizontal = Space.md, vertical = Space.xs),
                            horizontalArrangement = Arrangement.spacedBy(Space.xs),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            state.visualizerBars.forEach { height ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(height.coerceIn(0.12f, 1f))
                                        .clip(CyberShapes.chip)
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(station.accentColor, station.accentColor.copy(alpha = 0.4f))
                                            )
                                        )
                                )
                            }
                        }

                        state.errorMessage?.let { err ->
                            Spacer(modifier = Modifier.height(Space.xs))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = c.danger,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(Space.xs))
                                Text(
                                    text = err,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = c.danger
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Space.md))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { CyberRadioPlayer.prevStation() },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(c.panelRaised, CircleShape)
                                    .border(1.dp, c.goldAntique.copy(alpha = 0.35f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipPrevious,
                                    contentDescription = if (isRu) "Предыдущая" else "Previous",
                                    tint = c.textHigh
                                )
                            }

                            Button(
                                onClick = { CyberRadioPlayer.togglePlayPause() },
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = station.accentColor,
                                    contentColor = InkDeep
                                ),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.size(54.dp)
                            ) {
                                if (isBuffering) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(26.dp),
                                        color = InkDeep,
                                        strokeWidth = 3.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isRu) "Пауза / Воспроизведение" else "Play / Pause",
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = { CyberRadioPlayer.stop() },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(c.panelRaised, CircleShape)
                                    .border(1.dp, c.goldAntique.copy(alpha = 0.35f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = if (isRu) "Стоп" else "Stop",
                                    tint = c.danger
                                )
                            }

                            IconButton(
                                onClick = { CyberRadioPlayer.nextStation() },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(c.panelRaised, CircleShape)
                                    .border(1.dp, c.goldAntique.copy(alpha = 0.35f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = if (isRu) "Следующая" else "Next",
                                    tint = c.textHigh
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Space.sm))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Space.xs),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Space.sm)
                        ) {
                            Icon(
                                imageVector = if (state.volume > 0f) {
                                    Icons.AutoMirrored.Filled.VolumeUp
                                } else {
                                    Icons.AutoMirrored.Filled.VolumeOff
                                },
                                contentDescription = if (isRu) "Громкость" else "Volume",
                                tint = c.textMid,
                                modifier = Modifier.size(18.dp)
                            )
                            Slider(
                                value = state.volume,
                                onValueChange = { CyberRadioPlayer.setVolume(it) },
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = station.accentColor,
                                    activeTrackColor = station.accentColor,
                                    inactiveTrackColor = c.panelRaised
                                )
                            )
                            Text(
                                text = "${(state.volume * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = c.textMid,
                                modifier = Modifier.width(42.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Space.md))

                Text(
                    text = if (isRu) {
                        "КАНАЛЫ ЭЛЕКТРОННОГО РАДИО (${CyberRadioPlayer.stations.size}):"
                    } else {
                        "ELECTRONIC RADIO CHANNELS (${CyberRadioPlayer.stations.size}):"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = IbmPlexMonoFontFamily,
                    color = c.textMid,
                    modifier = Modifier.padding(bottom = Space.xs)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Space.sm)
                ) {
                    items(CyberRadioPlayer.stations) { st ->
                        val isSelected = st.id == station.id
                        val isCurrentPlaying = isSelected && isPlaying

                        Surface(
                            onClick = { CyberRadioPlayer.play(st) },
                            shape = CyberShapes.cardCompact,
                            color = if (isSelected) c.panelRaised else c.panel,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) st.accentColor else c.goldAntique.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Space.md),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = st.icon,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.width(Space.sm))
                                    Column {
                                        Text(
                                            text = st.name,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontFamily = IbmPlexMonoFontFamily,
                                            color = if (isSelected) c.textHigh else c.textMid
                                        )
                                        Text(
                                            text = st.genre,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isSelected) st.accentColor else c.textMid,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                if (isCurrentPlaying) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(EmeraldPure)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
