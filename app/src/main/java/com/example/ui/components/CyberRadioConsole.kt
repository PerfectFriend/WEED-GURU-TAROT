package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.CyberRadioPlayer
import com.example.audio.RadioStation

@Composable
fun CyberRadioMiniBar(
    modifier: Modifier = Modifier,
    onOpenConsole: () -> Unit
) {
    val state by CyberRadioPlayer.state.collectAsStateWithLifecycle()
    val station = state.currentStation
    val isPlaying = state.isPlaying
    val isBuffering = state.isBuffering

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xF2090D1A),
        border = BorderStroke(1.dp, station.accentColor.copy(alpha = if (isPlaying) 0.8f else 0.3f)),
        shadowElevation = if (isPlaying) 8.dp else 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpenConsole() }
            .testTag("cyber_radio_mini_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Radio Pulse Icon
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(station.accentColor.copy(alpha = 0.2f))
                        .border(1.dp, station.accentColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = station.icon, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = station.name,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        if (isPlaying) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00FF66))
                            )
                        }
                    }
                    Text(
                        text = if (isBuffering) "ПОДКЛЮЧЕНИЕ К ПОТОКУ DI.FM..." else station.genre,
                        color = if (isBuffering) Color(0xFFFFB800) else Color.Gray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Compact Equalizer Wave
            Row(
                modifier = Modifier
                    .width(42.dp)
                    .height(22.dp)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                state.visualizerBars.take(6).forEach { barHeight ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(barHeight.coerceIn(0.15f, 1f))
                            .clip(RoundedCornerShape(2.dp))
                            .background(station.accentColor)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Play / Pause Button
            IconButton(
                onClick = { CyberRadioPlayer.togglePlayPause() },
                modifier = Modifier
                    .size(32.dp)
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
                        contentDescription = "Play/Pause",
                        tint = station.accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CyberRadioConsoleDialog(
    onDismiss: () -> Unit
) {
    val state by CyberRadioPlayer.state.collectAsStateWithLifecycle()
    val station = state.currentStation
    val isPlaying = state.isPlaying
    val isBuffering = state.isBuffering

    val infiniteTransition = rememberInfiniteTransition(label = "radio_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, station.accentColor.copy(alpha = glowAlpha), RoundedCornerShape(20.dp))
                .testTag("cyber_radio_console_dialog"),
            color = Color(0xFF04060E)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
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
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0E1528))
                                .border(1.dp, Color(0xFF00F5FF).copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📻", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "CYBERNETIC RADIO",
                                color = Color(0xFF00F5FF),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "LIVE DI.FM & SYNTHWAVE STREAM",
                                color = Color.Gray,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.LightGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Now Playing Hero Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF080D1D),
                    border = BorderStroke(1.dp, station.accentColor.copy(alpha = 0.7f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
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
                                Text(
                                    text = station.icon,
                                    fontSize = 28.sp,
                                    modifier = Modifier
                                        .padding(end = 12.dp)
                                        .background(station.accentColor.copy(alpha = 0.15f), CircleShape)
                                        .padding(8.dp)
                                )
                                Column {
                                    Text(
                                        text = station.name,
                                        color = Color.White,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = station.genre,
                                        color = station.accentColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            // Stream Quality Badge
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isPlaying) Color(0xFF002914) else Color(0xFF1E1E28),
                                border = BorderStroke(1.dp, if (isPlaying) Color(0xFF00FF66) else Color.DarkGray)
                            ) {
                                Text(
                                    text = if (isPlaying) "LIVE • ${station.bitrate}" else if (isBuffering) "CONNECTING" else "READY",
                                    color = if (isPlaying) Color(0xFF00FF66) else if (isBuffering) Color(0xFFFFB800) else Color.Gray,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = station.description,
                            color = Color(0xFFCCCCDD),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Full 12-Band Equalizer Spectrum
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .background(Color(0xFF04060D), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0x15FFFFFF), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            state.visualizerBars.forEach { height ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(height.coerceIn(0.12f, 1f))
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(station.accentColor, station.accentColor.copy(alpha = 0.4f))
                                            )
                                        )
                                )
                            }
                        }

                        // Error Banner if stream drops
                        state.errorMessage?.let { err ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "⚠️ $err",
                                color = Color(0xFFFF5252),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Main Playback Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Prev Button
                            IconButton(
                                onClick = { CyberRadioPlayer.prevStation() },
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(Color(0xFF0F1524), CircleShape)
                                    .border(1.dp, Color(0x33FFFFFF), CircleShape)
                            ) {
                                Icon(Icons.Default.SkipPrevious, contentDescription = "Prev", tint = Color.White)
                            }

                            // Big Play / Pause Button
                            Button(
                                onClick = { CyberRadioPlayer.togglePlayPause() },
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = station.accentColor),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.size(54.dp)
                            ) {
                                if (isBuffering) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(26.dp),
                                        color = Color.Black,
                                        strokeWidth = 3.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Play/Pause",
                                        tint = Color.Black,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            // Stop Button
                            IconButton(
                                onClick = { CyberRadioPlayer.stop() },
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(Color(0xFF0F1524), CircleShape)
                                    .border(1.dp, Color(0x33FFFFFF), CircleShape)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color(0xFFFF5252))
                            }

                            // Next Button
                            IconButton(
                                onClick = { CyberRadioPlayer.nextStation() },
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(Color(0xFF0F1524), CircleShape)
                                    .border(1.dp, Color(0x33FFFFFF), CircleShape)
                            ) {
                                Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Volume Control Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (state.volume > 0f) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                                contentDescription = "Volume",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Slider(
                                value = state.volume,
                                onValueChange = { CyberRadioPlayer.setVolume(it) },
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = station.accentColor,
                                    activeTrackColor = station.accentColor,
                                    inactiveTrackColor = Color.DarkGray
                                )
                            )
                            Text(
                                text = "${(state.volume * 100).toInt()}%",
                                color = Color.Gray,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.width(32.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Station Channel Selector List
                Text(
                    text = "КАНАЛЫ ЭЛЕКТРОННОГО РАДИО (${CyberRadioPlayer.stations.size}):",
                    color = Color.LightGray,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(CyberRadioPlayer.stations) { st ->
                        val isSelected = st.id == station.id
                        val isCurrentPlaying = isSelected && isPlaying

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(0xFF10162B) else Color(0xFF080C16),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) st.accentColor else Color(0x22FFFFFF)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    CyberRadioPlayer.play(st)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = st.icon, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = st.name,
                                            color = if (isSelected) Color.White else Color(0xFFD0D0E0),
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = st.genre,
                                            color = if (isSelected) st.accentColor else Color.Gray,
                                            fontSize = 10.sp,
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
                                            .background(Color(0xFF00FF66))
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
