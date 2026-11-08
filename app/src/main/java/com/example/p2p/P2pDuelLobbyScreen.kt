package com.example.p2p

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.CyberNoirSynthPlayer
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun P2pDuelLobbyScreen(
    viewModel: TarotViewModel,
    onBack: (() -> Unit)? = null,
    onStartP2pDuel: (P2pNode) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val peerContacts by viewModel.peerContacts.collectAsStateWithLifecycle()
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val lang = userProfile?.systemLanguage ?: "en"

    var isScanning by remember { mutableStateOf(false) }
    var selectedNodeForTrade by remember { mutableStateOf<P2pNode?>(null) }
    var tradeSuccessMessage by remember { mutableStateOf<String?>(null) }

    val baseNodes = remember(peerContacts) {
        listOf(
            P2pNode("1", "Cipher_Alpha", "k2f3p9zk6h4pxz8m.onion", -42, 14, "Cyber Aggro ⚡"),
            P2pNode("2", "Proxy_Net", "z4hk7xmp8qk2zlw3.onion", -58, 28, "Quantum Barrier 🔮"),
            P2pNode("3", "Neon_Shadow_04", "m8qp2x7d1k9w4zla.onion", -65, 34, "Void Control 🌑"),
            P2pNode("4", "Bio_Harvester", "h4jx9w2m8l1k7pzq.onion", -72, 45, "Bio Regen 🌿")
        )
    }

    var discoveredNodes by remember { mutableStateOf(baseNodes) }

    val infiniteTransition = rememberInfiniteTransition(label = "p2p_pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050811))
            .padding(horizontal = 14.dp)
            .testTag("p2p_duel_lobby_screen")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f).padding(end = 6.dp)) {
                    Text(
                        text = if (lang == "ru") "P2P ДУЭЛИ И СЕТЬ" else "P2P DUEL & MESH LOBBY",
                        color = Color(0xFF00FF66),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (lang == "ru") "Беспроводной поиск соперников и обмен картами" else "Mesh network player search & encrypted trade",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Scan Button
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF66)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                if (isScanning) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                } else {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (lang == "ru") "СКАН" else "SCAN", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
            }
        }

        // Terminal Status Banner
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF09121B),
            border = BorderStroke(1.dp, Color(0xFF00FF66).copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00FF66))
                )
                Text(
                    text = if (lang == "ru") "Шлюз P2P Mesh активен • Найдено узлов: ${discoveredNodes.size}" else "P2P Mesh Gateway active • Discovered nodes: ${discoveredNodes.size}",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        tradeSuccessMessage?.let { msg ->
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF0C2417),
                border = BorderStroke(1.dp, Color(0xFF00FF66)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = msg,
                    color = Color(0xFF00FF66),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (lang == "ru") "УЗЛЫ ПОБЛИЗОСТИ (ВЫБЕРИТЕ ДЛЯ ВЫЗОВА):" else "NEARBY NODES (TAP TO CHALLENGE):",
            color = Color.Gray,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Nodes List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(discoveredNodes) { node ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0C1222),
                    border = BorderStroke(1.dp, Color(0x3300FF66)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00FF66))
                                )
                                Column {
                                    Text(
                                        text = node.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = node.address,
                                        color = Color.Gray,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${node.signalDbm} dBm",
                                    color = Color(0xFF00F5FF),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${node.pingMs} ms",
                                    color = Color.LightGray,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Колода: ${node.activeDeck}",
                                color = Color(0xFFFFB800),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.CLICK)
                                        selectedNodeForTrade = node
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text(if (lang == "ru") "Обмен" else "Trade", fontSize = 10.sp, color = Color(0xFF00F5FF))
                                }

                                Button(
                                    onClick = {
                                        CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.LASER_CLASH)
                                        onStartP2pDuel(node)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF66)),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text(
                                        text = if (lang == "ru") "ВЫЗОВ ⚔️" else "DUEL ⚔️",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // P2P Card Trade Confirmation Dialog
    selectedNodeForTrade?.let { node ->
        Dialog(onDismissRequest = { selectedNodeForTrade = null }) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF0C1424),
                border = BorderStroke(1.dp, Color(0xFF00F5FF)),
                modifier = Modifier.padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (lang == "ru") "P2P ОБМЕН АРКАНАМИ" else "P2P ARCANA TRADE",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF00F5FF)
                    )

                    Text(
                        text = if (lang == "ru") "Прямой обмен зашифрованным блоком с узлом ${node.name}." else "Direct encrypted ledger swap with node ${node.name}.",
                        fontSize = 11.sp,
                        color = Color.White
                    )

                    Text(
                        text = "SHA-256 Block Signature: SIG_P2P_${node.id}_${System.currentTimeMillis() % 100000}",
                        fontSize = 9.sp,
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { selectedNodeForTrade = null }) {
                            Text(if (lang == "ru") "Отмена" else "Cancel", color = Color.Gray)
                        }
                        Button(
                            onClick = {
                                CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
                                tradeSuccessMessage = if (lang == "ru") "✓ Сделка с ${node.name} успешно подписана и добавлена в P2P Ledger!" else "✓ Trade with ${node.name} signed into P2P Ledger!"
                                selectedNodeForTrade = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF66))
                        ) {
                            Text(if (lang == "ru") "Подписать обмен" else "Sign Trade", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
