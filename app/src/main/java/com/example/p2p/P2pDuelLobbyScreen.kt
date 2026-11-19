package com.example.p2p

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.CyberNoirSynthPlayer
import com.example.ui.components.CyberPanel
import com.example.ui.theme.Cyber
import com.example.ui.theme.CyberShapes
import com.example.ui.theme.IbmPlexMonoFontFamily
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.Space
import com.example.viewmodel.TarotViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class P2pNode(
    val id: String,
    val name: String,
    val address: String,
    val signalDbm: Int,
    val pingMs: Int,
    val activeDeck: String,
    val isOnline: Boolean = true
)

@Composable
fun P2pDuelLobbyScreen(
    viewModel: TarotViewModel,
    onBack: (() -> Unit)? = null,
    onStartP2pDuel: (P2pNode) -> Unit
) {
    val c = Cyber.colors
    val coroutineScope = rememberCoroutineScope()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val peerContacts by viewModel.peerContacts.collectAsStateWithLifecycle()
    val lang = userProfile?.systemLanguage ?: "en"

    var isScanning by remember { mutableStateOf(false) }
    var selectedNodeForTrade by remember { mutableStateOf<P2pNode?>(null) }
    var tradeSuccessMessage by remember { mutableStateOf<String?>(null) }

    val baseNodes = remember(peerContacts) {
        listOf(
            P2pNode("1", "Cipher_Alpha [SANDBOX]", "sandbox://peer-alpha", -42, 14, "Cyber Aggro"),
            P2pNode("2", "Proxy_Net [SANDBOX]", "sandbox://peer-proxy", -58, 28, "Quantum Barrier"),
            P2pNode("3", "Neon_Shadow_04 [SANDBOX]", "sandbox://peer-shadow", -65, 34, "Void Control"),
            P2pNode("4", "Bio_Harvester [SANDBOX]", "sandbox://peer-bio", -72, 45, "Bio Regen")
        )
    }

    var discoveredNodes by remember { mutableStateOf(baseNodes) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.ink)
            .padding(horizontal = Space.lg)
            .testTag("p2p_duel_lobby_screen")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Space.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = c.textHigh
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f).padding(end = Space.xs)) {
                    Text(
                        text = if (lang == "ru") "SANDBOX P2P ДУЭЛИ" else "SANDBOX P2P DUEL LOBBY",
                        color = c.emeraldGlow,
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = IbmPlexMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (lang == "ru") "Локальный симулятор ботов и тестового обмена" else "Local bot sparring & simulated trade sandbox",
                        color = c.textMid,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Button(
                onClick = {
                    CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.SCAN_BEEP)
                    isScanning = true
                    coroutineScope.launch {
                        delay(1500)
                        isScanning = false
                        discoveredNodes = baseNodes.shuffled()
                        CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
                    }
                },
                enabled = !isScanning,
                colors = ButtonDefaults.buttonColors(containerColor = c.emeraldGlow),
                shape = CyberShapes.control,
                contentPadding = PaddingValues(horizontal = Space.md, vertical = Space.sm),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                if (isScanning) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = c.ink, strokeWidth = 2.dp)
                } else {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = c.ink, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(Space.xs))
                    Text(
                        text = if (lang == "ru") "СКАН" else "SCAN",
                        color = c.ink,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        Surface(
            shape = CyberShapes.chip,
            color = c.panel,
            border = BorderStroke(1.dp, c.emeraldGlow.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(Space.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.sm)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(c.emeraldGlow)
                )
                Text(
                    text = if (lang == "ru") "Режим Песочницы: ${discoveredNodes.size} локальных ботов" else "Sandbox Mode: ${discoveredNodes.size} simulated bots",
                    color = c.textHigh,
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = IbmPlexMonoFontFamily
                )
            }
        }

        tradeSuccessMessage?.let { msg ->
            Spacer(modifier = Modifier.height(Space.sm))
            Surface(
                shape = CyberShapes.chip,
                color = c.emerald.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, c.emeraldGlow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = msg,
                    color = c.emeraldGlow,
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = IbmPlexMonoFontFamily,
                    modifier = Modifier.padding(Space.sm),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(Space.md))

        Text(
            text = if (lang == "ru") "УЗЛЫ ПОБЛИЗОСТИ (ВЫБЕРИТЕ ДЛЯ ВЫЗОВА):" else "NEARBY NODES (TAP TO CHALLENGE):",
            color = c.textMid,
            style = MaterialTheme.typography.labelMedium,
            fontFamily = IbmPlexMonoFontFamily
        )

        Spacer(modifier = Modifier.height(Space.sm))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(Space.sm)
        ) {
            items(discoveredNodes) { node ->
                CyberPanel(
                    borderColor = c.emeraldGlow.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Space.sm),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(c.emeraldGlow)
                            )
                            Column {
                                Text(
                                    text = node.name,
                                    color = c.textHigh,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontFamily = IbmPlexMonoFontFamily
                                )
                                Text(
                                    text = node.address,
                                    color = c.textMid,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${node.signalDbm} dBm",
                                color = NeonCyan,
                                style = MaterialTheme.typography.labelMedium,
                                fontFamily = IbmPlexMonoFontFamily
                            )
                            Text(
                                text = "${node.pingMs} ms",
                                color = c.textHigh,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Space.md))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (lang == "ru") "Колода: ${node.activeDeck}" else "Deck: ${node.activeDeck}",
                            color = NeonAmber,
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = IbmPlexMonoFontFamily,
                            modifier = Modifier.weight(1f)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                            OutlinedButton(
                                onClick = {
                                    CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.CLICK)
                                    selectedNodeForTrade = node
                                },
                                shape = CyberShapes.control,
                                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
                                contentPadding = PaddingValues(horizontal = Space.md, vertical = Space.sm),
                                modifier = Modifier.heightIn(min = 48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(Space.xs))
                                Text(
                                    text = if (lang == "ru") "Обмен" else "Trade",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = NeonCyan
                                )
                            }

                            Button(
                                onClick = {
                                    CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.LASER_CLASH)
                                    onStartP2pDuel(node)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = c.emeraldGlow),
                                shape = CyberShapes.control,
                                contentPadding = PaddingValues(horizontal = Space.md, vertical = Space.sm),
                                modifier = Modifier.heightIn(min = 48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = c.ink,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(Space.xs))
                                Text(
                                    text = if (lang == "ru") "ВЫЗОВ" else "DUEL",
                                    color = c.ink,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    selectedNodeForTrade?.let { node ->
        Dialog(onDismissRequest = { selectedNodeForTrade = null }) {
            Surface(
                shape = CyberShapes.card,
                color = c.panel,
                border = BorderStroke(1.dp, NeonCyan),
                modifier = Modifier.padding(Space.md)
            ) {
                Column(
                    modifier = Modifier.padding(Space.lg),
                    verticalArrangement = Arrangement.spacedBy(Space.md)
                ) {
                    Text(
                        text = if (lang == "ru") "SANDBOX-ОБМЕН" else "SANDBOX TRADE SIM",
                        style = MaterialTheme.typography.titleSmall,
                        fontFamily = IbmPlexMonoFontFamily,
                        color = NeonCyan
                    )

                    Text(
                        text = if (lang == "ru") {
                            "Локальная симуляция сделки с ботом ${node.name}. Реальные карты и токены не списываются."
                        } else {
                            "Simulated local trade preview with bot ${node.name}. Real vault cards and WEED are untouched."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.textHigh
                    )

                    Text(
                        text = "Sandbox ID: SIM_${node.id}_${System.currentTimeMillis() % 100000}",
                        style = MaterialTheme.typography.labelSmall,
                        color = c.textMid
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { selectedNodeForTrade = null },
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text(if (lang == "ru") "Отмена" else "Cancel", color = c.textMid)
                        }
                        Spacer(modifier = Modifier.width(Space.sm))
                        Button(
                            onClick = {
                                CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
                                tradeSuccessMessage = if (lang == "ru") {
                                    "[SANDBOX] Тестовый обмен с ${node.name} записан в симулятор"
                                } else {
                                    "[SANDBOX] Simulated trade with ${node.name} recorded"
                                }
                                selectedNodeForTrade = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = c.emeraldGlow),
                            shape = CyberShapes.control,
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text(
                                text = if (lang == "ru") "Симулировать" else "Simulate Trade",
                                color = c.ink,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}
