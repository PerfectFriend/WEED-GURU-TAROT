package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.LocalizationHelper
import com.example.data.LunarCalendarService
import com.example.data.PeerContact
import com.example.ui.components.CyberLunarCycleWidget
import com.example.ui.theme.CyberCharcoal
import com.example.ui.theme.CyberObsidian
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.RajdhaniFontFamily
import com.example.viewmodel.TarotViewModel

// ======================== NET-OPS CONTROL CENTER SYSTEM ========================

@Composable
fun NetOpsScreen(viewModel: TarotViewModel) {
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    var activeSubTab by remember { mutableStateOf(0) } // 0: Lunar Calendar, 1: Comms...
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val lang = profile?.systemLanguage ?: "en"
    var showNetworkSettingsDialog by remember { mutableStateOf(false) }

    if (showNetworkSettingsDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showNetworkSettingsDialog = false }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.5.dp, NeonCyan, RoundedCornerShape(16.dp)),
                color = CyberObsidian
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
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Network Settings",
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == "ru") "СЕТЕВЫЕ НАСТРОЙКИ" else "NETWORK SETTINGS",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = RajdhaniFontFamily,
                                color = Color.White
                            )
                        }
                        IconButton(onClick = { showNetworkSettingsDialog = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tunnel configuration
                    Box(modifier = Modifier.weight(1f)) {
                        BridgeTunnelPanel(viewModel)
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 54.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        // Botanical Top Section with Gear Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = LocalizationHelper.getString(lang, "tab_netops"),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = RajdhaniFontFamily,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (lang == "ru") "Системы оранжерей и лунные циклы" else if (lang == "es") "Sistemas de invernadero y ciclos lunares" else "Greenhouse Systems & Lunar Cycles",
                    fontSize = 17.sp,
                    fontFamily = RajdhaniFontFamily,
                    color = Color(0xFFB0B0C5)
                )
            }

            // Gear button for Network Settings
            IconButton(
                onClick = {
                    com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.CLICK)
                    showNetworkSettingsDialog = true
                },
                modifier = Modifier
                    .size(44.dp)
                    .background(NeonCyan.copy(alpha = 0.1f), CircleShape)
                    .border(1.dp, NeonCyan.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Network Settings Gear",
                    tint = NeonCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Monospace Tab Buttons
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .background(Color(0xFF0D0D15), RoundedCornerShape(4.dp))
                .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = if (lang == "ru") {
                listOf("ЛУНА 🌙", "СВЯЗЬ", "РЕЕСТР", "ДИСПЕНСЕР", "СЕМЕНА", "РЕКЛАМА", "ЗАНАЧКА")
            } else if (lang == "es") {
                listOf("LUNAR 🌙", "COMMS", "LEDGER", "DISPENSARIO", "SEMILLAS", "ANUNCIOS", "ALMACÉN")
            } else {
                listOf("LUNAR 🌙", "COMMS", "LEDGER", "DISPENSARY", "SEEDS", "ADS", "STASH")
            }
            items(tabs.size) { index ->
                val title = tabs[index]
                val isSelected = activeSubTab == index
                val tabColor = when (index) {
                    0 -> NeonGreen
                    3 -> NeonMagenta
                    4 -> NeonGreen
                    5 -> NeonGold
                    6 -> NeonCyan
                    else -> NeonCyan
                }
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) tabColor.copy(alpha = 0.15f) else Color.Transparent,
                            RoundedCornerShape(3.dp)
                        )
                        .clickable { activeSubTab = index }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) tabColor else Color(0xFFB0B0C5)
                    )
                }
            }
        }

        // Sub-panels
        Box(modifier = Modifier.weight(1f)) {
            when (activeSubTab) {
                0 -> GrowerLunarCalendarPanel(viewModel)
                1 -> SecureCommsPanel(viewModel)
                2 -> P2pLedgerPanel(viewModel)
                3 -> OnChainNftPanel(viewModel)
                4 -> WeedProtocolPanel(viewModel)
                5 -> NeuralAdsPanel(viewModel)
                6 -> SwarmStoragePanel(viewModel)
            }
        }
    }
}

@Composable
fun GrowerLunarCalendarPanel(viewModel: TarotViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val lang = profile?.systemLanguage ?: "en"
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    var lunarSubTab by remember { mutableIntStateOf(0) } // 0: Tarot Minting Oracle, 1: Botanical Greenhouse
    val calendarDays = remember(lang) { LunarCalendarService.getLunarCalendarForCurrentMonth(lang) }
    val currentDayIndex = remember {
        val today = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH)
        (today - 1).coerceIn(0, calendarDays.size - 1)
    }
    val todayInfo = calendarDays[currentDayIndex]

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("grower_lunar_calendar_panel"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Selector: Tarot Minting Oracle vs Botanical Greenhouse
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0C0E1A), RoundedCornerShape(10.dp))
                    .border(1.dp, currentSkin.primaryColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(4.dp)
            ) {
                // Tab 0: Tarot Minting Oracle
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (lunarSubTab == 0) currentSkin.primaryColor.copy(alpha = 0.25f) else Color.Transparent)
                        .clickable { lunarSubTab = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (lang == "ru") "🔮 ОРАКУЛ ЧЕКАНКИ" else if (lang == "es") "🔮 ACUÑACIÓN TAROT" else "🔮 MINTING ORACLE",
                        fontSize = 16.sp,
                        fontFamily = RajdhaniFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = if (lunarSubTab == 0) currentSkin.primaryColor else Color(0xFFB0B0C5)
                    )
                }

                // Tab 1: Botanical Greenhouse
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (lunarSubTab == 1) Color(0xFF00FF66).copy(alpha = 0.25f) else Color.Transparent)
                        .clickable { lunarSubTab = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (lang == "ru") "🌱 ОРАНЖЕРЕЯ" else if (lang == "es") "🌱 INVERNADERO" else "🌱 GREENHOUSE",
                        fontSize = 16.sp,
                        fontFamily = RajdhaniFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = if (lunarSubTab == 1) Color(0xFF00FF66) else Color(0xFFB0B0C5)
                    )
                }
            }
        }

        if (lunarSubTab == 0) {
            item {
                CyberLunarCycleWidget(
                    systemLanguage = lang,
                    currentSkin = currentSkin,
                    onMintClick = {
                        viewModel.resetDivination()
                    }
                )
            }
        } else {
            // Today's Moon Phase Hero Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0x66004D1A)),
                    border = BorderStroke(1.5.dp, Color(0xFF00FF66).copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = LocalizationHelper.getString(lang, "daily_advice_header"),
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00FF66)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = todayInfo.phaseName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = todayInfo.phaseIcon,
                                fontSize = 44.sp,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = LocalizationHelper.getString(lang, "illumination_label"),
                                    fontSize = 15.sp,
                                    color = Color(0xFFB0B0C5),
                                    fontFamily = RajdhaniFontFamily
                                )
                                Text(
                                    text = "${todayInfo.illumination}%",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = LocalizationHelper.getString(lang, "moon_zodiac"),
                                    fontSize = 15.sp,
                                    color = Color(0xFFB0B0C5),
                                    fontFamily = RajdhaniFontFamily
                                )
                                Text(
                                    text = todayInfo.astrologicalSign,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Column(modifier = Modifier.weight(1.3f)) {
                                Text(
                                    text = LocalizationHelper.getString(lang, "soil_fertility"),
                                    fontSize = 15.sp,
                                    color = Color(0xFFB0B0C5),
                                    fontFamily = RajdhaniFontFamily
                                )
                                Text(
                                    text = if (todayInfo.isFertile) LocalizationHelper.getString(lang, "high_fertile") else LocalizationHelper.getString(lang, "barren_dry"),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (todayInfo.isFertile) Color(0xFF00FF66) else Color(0xFFFF5500)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Recommended Action Badge
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "🌿", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = todayInfo.recommendedAction.uppercase(),
                                        fontFamily = RajdhaniFontFamily,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00FF66)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = todayInfo.detailInstructions,
                                    fontSize = 16.sp,
                                    color = Color.LightGray,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            // Partner Links Section
            item {
                val uriHandler = LocalUriHandler.current
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0x3300FFCC)),
                    border = BorderStroke(1.5.dp, Color(0xFF00FFCC).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (lang == "ru") "ПАРТНЕРСКИЕ РЕСУРСЫ" else "PARTNER RESOURCES",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00FFCC)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // SeedFinder
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                                border = BorderStroke(1.dp, Color(0xFF00FFCC).copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        try {
                                            uriHandler.openUri("https://www.seedfinder.eu")
                                        } catch (e: Exception) {
                                            // Ignore
                                        }
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🌱", fontSize = 24.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "SeedFinder.eu",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (lang == "ru") "Генетика сортов" else "Strain Database",
                                        fontSize = 14.sp,
                                        color = Color(0xFFB0B0C5),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            // Rasta Prosto
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                                border = BorderStroke(1.dp, Color(0xFF00FFCC).copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        try {
                                            uriHandler.openUri("https://rasta_prosto.r.me")
                                        } catch (e: Exception) {
                                            // Ignore
                                        }
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🛒", fontSize = 24.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Rasta Prosto",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (lang == "ru") "Магазин семян" else "Seed Shop",
                                        fontSize = 14.sp,
                                        color = Color(0xFFB0B0C5),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Monthly Forecast Section Title
            item {
                Text(
                    text = LocalizationHelper.getString(lang, "monthly_grower_calendar"),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = RajdhaniFontFamily,
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            // Daily table rows
            items(calendarDays) { day ->
                val isToday = day.dayOfMonth == (currentDayIndex + 1)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isToday) Color(0x3300FF66) else Color(0xFF0F0F16)
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isToday) Color(0xFF00FF66) else Color.DarkGray.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text(
                                text = day.phaseIcon,
                                fontSize = 24.sp
                            )
                            Column {
                                Text(
                                    text = day.dateString,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isToday) Color(0xFF00FF66) else Color.White
                                )
                                Text(
                                    text = "${day.illumination}% lit | ${day.astrologicalSign.substringBefore(" (")}",
                                    fontSize = 14.sp,
                                    color = Color(0xFFB0B0C5),
                                    fontFamily = RajdhaniFontFamily
                                )
                            }
                        }

                        // Task badge
                        Box(
                            modifier = Modifier
                                .background(
                                    if (day.isFertile) Color(0x2200FF66) else Color(0x22FF5500),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .weight(0.8f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.recommendedAction,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (day.isFertile) Color(0xFF00FF66) else Color(0xFFFF9900),
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BridgeTunnelPanel(viewModel: TarotViewModel) {
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val host by viewModel.bridgeHost.collectAsStateWithLifecycle()
    val port by viewModel.bridgePort.collectAsStateWithLifecycle()
    val type by viewModel.bridgeType.collectAsStateWithLifecycle()
    val status by viewModel.bridgeStatus.collectAsStateWithLifecycle()
    val logs by viewModel.bridgeLog.collectAsStateWithLifecycle()

    var editingHost by remember { mutableStateOf(host) }
    var editingPort by remember { mutableStateOf(port) }
    var editingType by remember { mutableStateOf(type) }

    val statusColor = when (status) {
        "ONLINE" -> NeonGreen
        "TESTING" -> NeonGold
        "OFFLINE" -> Color.Red
        else -> Color.Gray
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                border = BorderStroke(1.dp, Color.DarkGray),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TUNNEL CONFIGURATION",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Host Input
                    OutlinedTextField(
                        value = editingHost,
                        onValueChange = {
                            editingHost = it
                            viewModel.updateBridgeConfig(it, editingPort, editingType)
                        },
                        label = { Text("Proxy Host IP", fontFamily = RajdhaniFontFamily) },
                        singleLine = true,
                        textStyle = TextStyle(fontFamily = RajdhaniFontFamily, color = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Port Input
                        OutlinedTextField(
                            value = editingPort,
                            onValueChange = {
                                editingPort = it
                                viewModel.updateBridgeConfig(editingHost, it, editingType)
                            },
                            label = { Text("Port", fontFamily = RajdhaniFontFamily) },
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = RajdhaniFontFamily, color = Color.White),
                            modifier = Modifier.weight(1f)
                        )

                        // SOCKS / HTTP Toggle
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "PROXY TYPE",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 14.sp,
                                color = Color(0xFFB0B0C5),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .background(Color.Black, RoundedCornerShape(4.dp))
                                    .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("SOCKS", "HTTP").forEach { pType ->
                                    val isSel = editingType == pType
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .background(
                                                if (isSel) NeonCyan.copy(alpha = 0.2f) else Color.Transparent,
                                                RoundedCornerShape(3.dp)
                                            )
                                            .clickable {
                                                editingType = pType
                                                viewModel.updateBridgeConfig(editingHost, editingPort, pType)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = pType,
                                            fontFamily = RajdhaniFontFamily,
                                            fontSize = 16.sp,
                                            color = if (isSel) NeonCyan else Color(0xFFB0B0C5)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Status display
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black, RoundedCornerShape(4.dp))
                            .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(statusColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "GATEWAY STATUS: $status",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                            Text(
                                text = "OBFS4 Bridge connection & Tor circuitry active",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 17.sp,
                                color = Color(0xFFB0B0C5)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    CyberButton(
                        onClick = { viewModel.testBridge() },
                        skin = currentSkin,
                        enabled = status != "TESTING",
                        containerColor = NeonCyan,
                        contentColor = CyberObsidian,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AutoResizingText(
                            text = if (status == "TESTING") "CONNECTING CIRC... " else "ESTABLISH BRIDGE ROUTE",
                            targetTextSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = RajdhaniFontFamily,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                border = BorderStroke(1.dp, Color.DarkGray),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TERMINAL NET-TUNNEL LOGS",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .background(Color.Black, RoundedCornerShape(4.dp))
                            .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                            .padding(10.dp)
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(logs) { log ->
                                Text(
                                    text = log,
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 14.sp,
                                    color = if (log.contains("ERROR") || log.contains("failed") || log.contains("failed.")) Color.Red else if (log.contains("ONLINE") || log.contains("secured")) NeonGreen else Color.LightGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SecureCommsPanel(viewModel: TarotViewModel) {
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val contacts by viewModel.peerContacts.collectAsStateWithLifecycle()
    var selectedContact by remember { mutableStateOf<PeerContact?>(null) }
    var showAddContactDialog by remember { mutableStateOf(false) }

    val activeContact = selectedContact
    if (activeContact != null) {
        val messages by viewModel.getMessagesForContact(activeContact.id).collectAsStateWithLifecycle(emptyList())
        var textInput by remember { mutableStateOf("") }

        Column(modifier = Modifier.fillMaxSize()) {
            // Chat Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberCharcoal, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .border(1.dp, Color.DarkGray, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(NeonMagenta.copy(alpha = 0.2f), CircleShape)
                            .border(1.dp, NeonMagenta, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = activeContact.name.take(1).uppercase(),
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = NeonMagenta,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = activeContact.name,
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Text(
                            text = activeContact.onionAddress,
                            fontFamily = RajdhaniFontFamily,
                            color = Color(0xFFB0B0C5),
                            fontSize = 17.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.width(180.dp)
                        )
                    }
                }

                Text(
                    text = "CLOSE CHANNEL",
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 14.sp,
                    color = Color(0xFFB0B0C5),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { selectedContact = null }
                )
            }

            // Message List
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.Black)
                    .border(1.dp, Color.DarkGray)
                    .padding(8.dp)
            ) {
                if (messages.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "CHANNEL SECURED. AWAITING TELEMETRY HANDSHAKE...",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                    }
                } else {
                    val listState = rememberLazyListState()
                    LaunchedEffect(messages.size) {
                        if (messages.isNotEmpty()) {
                            listState.animateScrollToItem(messages.size - 1)
                        }
                    }
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(messages) { msg ->
                            val isMe = msg.senderName == "Me"
                            val bubbleColor = if (isMe) NeonCyan.copy(alpha = 0.08f) else NeonMagenta.copy(alpha = 0.08f)
                            val borderColor = if (isMe) NeonCyan.copy(alpha = 0.4f) else NeonMagenta.copy(alpha = 0.4f)
                            val align = if (isMe) Alignment.End else Alignment.Start

                            Column(modifier = Modifier.fillMaxWidth()) {
                                Box(
                                    modifier = Modifier
                                        .align(align)
                                        .widthIn(max = 260.dp)
                                        .background(bubbleColor, RoundedCornerShape(6.dp))
                                        .border(1.dp, borderColor, RoundedCornerShape(6.dp))
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = msg.senderName.uppercase(),
                                            fontFamily = RajdhaniFontFamily,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isMe) NeonCyan else NeonMagenta
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = msg.plaintext,
                                            fontFamily = RajdhaniFontFamily,
                                            fontSize = 16.sp,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f), thickness = 0.5.dp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "ENC_CIPHERTEXT: ${msg.ciphertext}",
                                            fontFamily = RajdhaniFontFamily,
                                            fontSize = 16.sp,
                                            color = Color(0xFFB0B0C5),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "ALGORITHM: AES-256-GCM / 12-byte IV",
                                            fontFamily = RajdhaniFontFamily,
                                            fontSize = 16.sp,
                                            color = Color(0xFFB0B0C5)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Message Input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberCharcoal, RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                    .border(1.dp, Color.DarkGray, RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Send encrypted message...", fontFamily = RajdhaniFontFamily, fontSize = 16.sp) },
                    singleLine = true,
                    textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 16.sp, color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color.DarkGray
                    ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            viewModel.sendEncryptedMessage(activeContact.id, textInput)
                            textInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(NeonCyan, RoundedCornerShape(4.dp))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = CyberObsidian
                    )
                }
            }
        }
    } else {
        // Address Book Contact list
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PEER CONTACTS",
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                CyberTextButton(
                    onClick = { showAddContactDialog = true },
                    text = "+ ADD NODE",
                    skin = currentSkin,
                    containerColor = NeonCyan,
                    contentColor = CyberObsidian,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (contacts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.Black, RoundedCornerShape(8.dp))
                        .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "TERMINAL ADDRESS BOOK EMPTY",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            color = Color(0xFFB0B0C5),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add peer nodes to initiate handshakes.",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(contacts) { contact ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                            border = BorderStroke(1.dp, Color.DarkGray),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedContact = contact }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(NeonCyan.copy(alpha = 0.1f), CircleShape)
                                            .border(1.dp, NeonCyan, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = contact.name.take(1).uppercase(),
                                            fontFamily = RajdhaniFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = NeonCyan
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = contact.name,
                                            fontFamily = RajdhaniFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            text = "ADDR: ${contact.onionAddress}",
                                            fontFamily = RajdhaniFontFamily,
                                            color = Color(0xFFB0B0C5),
                                            fontSize = 17.sp
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(
                                        onClick = { selectedContact = contact },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Chat,
                                            contentDescription = "Message",
                                            tint = NeonCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteContact(contact.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color.Red,
                                            modifier = Modifier.size(16.dp)
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

    // Add contact dialog
    if (showAddContactDialog) {
        var nameInput by remember { mutableStateOf("") }
        var onionInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddContactDialog = false },
            title = {
                Text(
                    text = "REGISTER NEW PEER NODE",
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Code Name", fontFamily = RajdhaniFontFamily) },
                        singleLine = true,
                        textStyle = TextStyle(fontFamily = RajdhaniFontFamily),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = onionInput,
                        onValueChange = { onionInput = it },
                        placeholder = { Text("e.g. jx7v9pxmno...onion", fontFamily = RajdhaniFontFamily, fontSize = 16.sp) },
                        label = { Text("Onion / Cyber Address", fontFamily = RajdhaniFontFamily) },
                        singleLine = true,
                        textStyle = TextStyle(fontFamily = RajdhaniFontFamily),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (nameInput.isNotBlank() && onionInput.isNotBlank()) {
                            viewModel.addContact(nameInput, onionInput)
                            showAddContactDialog = false
                        }
                    }
                ) {
                    Text("REGISTER", fontFamily = RajdhaniFontFamily, color = NeonCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddContactDialog = false }) {
                    Text("CANCEL", fontFamily = RajdhaniFontFamily, color = Color(0xFFB0B0C5))
                }
            },
            containerColor = CyberCharcoal,
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }
}
