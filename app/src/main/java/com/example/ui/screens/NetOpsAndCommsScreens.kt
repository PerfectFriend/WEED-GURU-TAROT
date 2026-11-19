package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.CyberNoirSynthPlayer
import com.example.data.LocalizationHelper
import com.example.data.LunarCalendarService
import com.example.data.PeerContact
import com.example.ui.components.CyberActionButton
import com.example.ui.components.CyberChip
import com.example.ui.components.CyberLunarCycleWidget
import com.example.ui.components.CyberPanel
import com.example.ui.components.CyberSectionHeader
import com.example.ui.theme.Cyber
import com.example.ui.theme.CyberShapes
import com.example.ui.theme.IbmPlexMonoFontFamily
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.Space
import com.example.viewmodel.TarotViewModel
import java.util.Calendar

@Composable
fun NetOpsScreen(viewModel: TarotViewModel) {
    val c = Cyber.colors
    var activeSubTab by remember { mutableIntStateOf(0) }
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val lang = profile?.systemLanguage ?: "en"
    var showNetworkSettingsDialog by remember { mutableStateOf(false) }

    if (showNetworkSettingsDialog) {
        Dialog(
            onDismissRequest = { showNetworkSettingsDialog = false }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .clip(CyberShapes.card)
                    .border(1.5.dp, c.accent, CyberShapes.card),
                color = c.ink
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Space.lg)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Network Settings",
                                tint = c.accent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(Space.sm))
                            Text(
                                text = if (lang == "ru") "СЕТЕВЫЕ НАСТРОЙКИ" else "NETWORK SETTINGS",
                                style = MaterialTheme.typography.titleMedium,
                                color = c.textHigh
                            )
                        }
                        IconButton(
                            onClick = { showNetworkSettingsDialog = false },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = c.textMid
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Space.lg))

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
            .padding(top = Space.sm, start = Space.lg, end = Space.lg, bottom = Space.lg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = LocalizationHelper.getString(lang, "tab_netops"),
                    style = MaterialTheme.typography.headlineMedium,
                    color = c.textHigh
                )
                Text(
                    text = when (lang) {
                        "ru" -> "Системы оранжерей и лунные циклы"
                        "es" -> "Sistemas de invernadero y ciclos lunares"
                        else -> "Greenhouse Systems & Lunar Cycles"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textMid
                )
            }

            IconButton(
                onClick = {
                    CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.CLICK)
                    showNetworkSettingsDialog = true
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(c.accent.copy(alpha = 0.12f), CircleShape)
                    .border(1.dp, c.accent.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Network Settings",
                    tint = c.accent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(Space.lg))

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Space.lg),
            horizontalArrangement = Arrangement.spacedBy(Space.sm)
        ) {
            val tabs = when (lang) {
                "ru" -> listOf("ЛУНА", "СВЯЗЬ", "РЕЕСТР", "ДИСПЕНСЕР", "СЕМЕНА", "РЕКЛАМА", "ЗАНАЧКА")
                "es" -> listOf("LUNAR", "COMMS", "LEDGER", "DISPENSARIO", "SEMILLAS", "ANUNCIOS", "ALMACÉN")
                else -> listOf("LUNAR", "COMMS", "LEDGER", "DISPENSARY", "SEEDS", "ADS", "STASH")
            }
            items(tabs.size) { index ->
                CyberChip(
                    label = tabs[index],
                    selected = activeSubTab == index,
                    onClick = { activeSubTab = index }
                )
            }
        }

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
    val c = Cyber.colors
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val lang = profile?.systemLanguage ?: "en"
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    var lunarSubTab by remember { mutableIntStateOf(0) }
    val calendarDays = remember(lang) { LunarCalendarService.getLunarCalendarForCurrentMonth(lang) }
    val currentDayIndex = remember {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        (today - 1).coerceIn(0, calendarDays.size - 1)
    }
    val todayInfo = calendarDays[currentDayIndex]

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("grower_lunar_calendar_panel"),
        verticalArrangement = Arrangement.spacedBy(Space.lg)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Space.sm)
            ) {
                CyberChip(
                    label = when (lang) {
                        "ru" -> "ОРАКУЛ ЧЕКАНКИ"
                        "es" -> "ACUÑACIÓN TAROT"
                        else -> "MINTING ORACLE"
                    },
                    selected = lunarSubTab == 0,
                    onClick = { lunarSubTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                CyberChip(
                    label = when (lang) {
                        "ru" -> "ОРАНЖЕРЕЯ"
                        "es" -> "INVERNADERO"
                        else -> "GREENHOUSE"
                    },
                    selected = lunarSubTab == 1,
                    onClick = { lunarSubTab = 1 },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (lunarSubTab == 0) {
            item {
                CyberLunarCycleWidget(
                    systemLanguage = lang,
                    currentSkin = currentSkin,
                    onMintClick = { viewModel.resetDivination() }
                )
            }
        } else {
            item {
                CyberPanel(
                    borderColor = c.emeraldGlow.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = LocalizationHelper.getString(lang, "daily_advice_header"),
                                style = MaterialTheme.typography.labelLarge,
                                color = c.emeraldGlow
                            )
                            Spacer(modifier = Modifier.height(Space.xs))
                            Text(
                                text = todayInfo.phaseName,
                                style = MaterialTheme.typography.titleLarge,
                                color = c.textHigh
                            )
                        }
                        Text(
                            text = todayInfo.phaseIcon,
                            style = MaterialTheme.typography.displayLarge,
                            modifier = Modifier.padding(start = Space.md)
                        )
                    }

                    Spacer(modifier = Modifier.height(Space.md))
                    HorizontalDivider(color = c.goldAntique.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(Space.md))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Space.md)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = LocalizationHelper.getString(lang, "illumination_label"),
                                style = MaterialTheme.typography.labelMedium,
                                color = c.textMid
                            )
                            Text(
                                text = "${todayInfo.illumination}%",
                                style = MaterialTheme.typography.titleSmall,
                                fontFamily = IbmPlexMonoFontFamily,
                                color = c.textHigh
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = LocalizationHelper.getString(lang, "moon_zodiac"),
                                style = MaterialTheme.typography.labelMedium,
                                color = c.textMid
                            )
                            Text(
                                text = todayInfo.astrologicalSign,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = c.textHigh
                            )
                        }

                        Column(modifier = Modifier.weight(1.3f)) {
                            Text(
                                text = LocalizationHelper.getString(lang, "soil_fertility"),
                                style = MaterialTheme.typography.labelMedium,
                                color = c.textMid
                            )
                            Text(
                                text = if (todayInfo.isFertile) LocalizationHelper.getString(lang, "high_fertile") else LocalizationHelper.getString(lang, "barren_dry"),
                                style = MaterialTheme.typography.titleSmall,
                                color = if (todayInfo.isFertile) c.emeraldGlow else NeonAmber
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Space.lg))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(c.ink.copy(alpha = 0.6f), CyberShapes.chip)
                            .border(1.dp, c.goldAntique.copy(alpha = 0.25f), CyberShapes.chip)
                            .padding(Space.md)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Spa,
                                    contentDescription = null,
                                    tint = c.emeraldGlow,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(Space.sm))
                                Text(
                                    text = todayInfo.recommendedAction.uppercase(),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = c.emeraldGlow
                                )
                            }
                            Spacer(modifier = Modifier.height(Space.xs))
                            Text(
                                text = todayInfo.detailInstructions,
                                style = MaterialTheme.typography.bodySmall,
                                color = c.textHigh
                            )
                        }
                    }
                }
            }

            item {
                val uriHandler = LocalUriHandler.current
                CyberPanel(
                    borderColor = NeonCyan.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CyberSectionHeader(
                        title = if (lang == "ru") "ПАРТНЁРСКИЕ РЕСУРСЫ" else "PARTNER RESOURCES",
                        color = NeonCyan
                    )
                    Spacer(modifier = Modifier.height(Space.md))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Space.md)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = c.panelRaised),
                            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.35f)),
                            shape = CyberShapes.cardCompact,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 88.dp)
                                .clickable {
                                    try {
                                        uriHandler.openUri("https://www.seedfinder.eu")
                                    } catch (_: Exception) {
                                    }
                                }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Space.md),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFlorist,
                                    contentDescription = null,
                                    tint = c.emeraldGlow,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(Space.xs))
                                Text(
                                    text = "SeedFinder.eu",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = c.textHigh,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = if (lang == "ru") "Генетика сортов" else "Strain Database",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = c.textMid,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = c.panelRaised),
                            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.35f)),
                            shape = CyberShapes.cardCompact,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 88.dp)
                                .clickable {
                                    try {
                                        uriHandler.openUri("https://rasta_prosto.r.me")
                                    } catch (_: Exception) {
                                    }
                                }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Space.md),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    tint = c.goldLeaf,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(Space.xs))
                                Text(
                                    text = "Rasta Prosto",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = c.textHigh,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = if (lang == "ru") "Магазин семян" else "Seed Shop",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = c.textMid,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            item {
                CyberSectionHeader(
                    title = LocalizationHelper.getString(lang, "monthly_grower_calendar"),
                    color = c.textHigh
                )
            }

            items(calendarDays) { day ->
                val isToday = day.dayOfMonth == (currentDayIndex + 1)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isToday) c.emerald.copy(alpha = 0.22f) else c.panel
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isToday) c.emeraldGlow else c.goldAntique.copy(alpha = 0.25f)
                    ),
                    shape = CyberShapes.cardCompact,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Space.md, vertical = Space.md),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Space.sm),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text(
                                text = day.phaseIcon,
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Column {
                                Text(
                                    text = day.dateString,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (isToday) c.emeraldGlow else c.textHigh
                                )
                                Text(
                                    text = "${day.illumination}% lit | ${day.astrologicalSign.substringBefore(" (")}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = c.textMid
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .background(
                                    if (day.isFertile) c.emeraldGlow.copy(alpha = 0.14f) else NeonAmber.copy(alpha = 0.14f),
                                    CyberShapes.chip
                                )
                                .padding(horizontal = Space.sm, vertical = Space.xs)
                                .weight(0.8f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.recommendedAction,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (day.isFertile) c.emeraldGlow else NeonAmber,
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
    val c = Cyber.colors
    val host by viewModel.bridgeHost.collectAsStateWithLifecycle()
    val port by viewModel.bridgePort.collectAsStateWithLifecycle()
    val type by viewModel.bridgeType.collectAsStateWithLifecycle()
    val status by viewModel.bridgeStatus.collectAsStateWithLifecycle()
    val logs by viewModel.bridgeLog.collectAsStateWithLifecycle()

    var editingHost by remember { mutableStateOf(host) }
    var editingPort by remember { mutableStateOf(port) }
    var editingType by remember { mutableStateOf(type) }

    val statusColor = when (status) {
        "ONLINE" -> c.emeraldGlow
        "TESTING" -> c.goldLeaf
        "OFFLINE" -> c.danger
        else -> c.textMid
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Space.lg)
    ) {
        item {
            CyberPanel(
                borderColor = c.goldAntique.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                CyberSectionHeader(
                    title = "TUNNEL CONFIGURATION",
                    color = c.accent
                )
                Spacer(modifier = Modifier.height(Space.md))

                OutlinedTextField(
                    value = editingHost,
                    onValueChange = {
                        editingHost = it
                        viewModel.updateBridgeConfig(it, editingPort, editingType)
                    },
                    label = { Text("Proxy Host IP") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = c.textHigh),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(Space.sm))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Space.sm),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedTextField(
                        value = editingPort,
                        onValueChange = {
                            editingPort = it
                            viewModel.updateBridgeConfig(editingHost, it, editingType)
                        },
                        label = { Text("Port") },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = c.textHigh),
                        modifier = Modifier.weight(1f)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "PROXY TYPE",
                            style = MaterialTheme.typography.labelMedium,
                            color = c.textMid,
                            modifier = Modifier.padding(bottom = Space.xs)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Space.xs)
                        ) {
                            listOf("SOCKS", "HTTP").forEach { pType ->
                                CyberChip(
                                    label = pType,
                                    selected = editingType == pType,
                                    onClick = {
                                        editingType = pType
                                        viewModel.updateBridgeConfig(editingHost, editingPort, pType)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Space.lg))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(c.ink, CyberShapes.chip)
                        .border(1.dp, c.goldAntique.copy(alpha = 0.25f), CyberShapes.chip)
                        .padding(Space.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(statusColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(Space.sm))
                    Column {
                        Text(
                            text = "GATEWAY STATUS: $status",
                            style = MaterialTheme.typography.titleSmall,
                            color = statusColor
                        )
                        Text(
                            text = "OBFS4 Bridge connection & Tor circuitry active",
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textMid
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Space.lg))

                CyberActionButton(
                    text = if (status == "TESTING") "CONNECTING CIRC..." else "ESTABLISH BRIDGE ROUTE",
                    onClick = { viewModel.testBridge() },
                    enabled = status != "TESTING",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            CyberPanel(
                borderColor = c.goldAntique.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                CyberSectionHeader(
                    title = "TERMINAL NET-TUNNEL LOGS",
                    color = c.textHigh
                )
                Spacer(modifier = Modifier.height(Space.sm))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(c.ink, CyberShapes.chip)
                        .border(1.dp, c.goldAntique.copy(alpha = 0.25f), CyberShapes.chip)
                        .padding(Space.md)
                ) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(logs) { log ->
                            Text(
                                text = log,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (log.contains("ERROR") || log.contains("failed")) c.danger else if (log.contains("ONLINE") || log.contains("secured")) c.emeraldGlow else c.textMid
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SecureCommsPanel(viewModel: TarotViewModel) {
    val c = Cyber.colors
    val contacts by viewModel.peerContacts.collectAsStateWithLifecycle()
    var selectedContact by remember { mutableStateOf<PeerContact?>(null) }
    var showAddContactDialog by remember { mutableStateOf(false) }

    val activeContact = selectedContact
    if (activeContact != null) {
        val messages by viewModel.getMessagesForContact(activeContact.id).collectAsStateWithLifecycle(emptyList())
        var textInput by remember { mutableStateOf("") }

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(c.panel, CyberShapes.cardCompact)
                    .border(1.dp, c.goldAntique.copy(alpha = 0.3f), CyberShapes.cardCompact)
                    .padding(Space.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(NeonMagenta.copy(alpha = 0.2f), CircleShape)
                            .border(1.dp, NeonMagenta, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = activeContact.name.take(1).uppercase(),
                            style = MaterialTheme.typography.titleSmall,
                            color = NeonMagenta
                        )
                    }
                    Spacer(modifier = Modifier.width(Space.sm))
                    Column {
                        Text(
                            text = activeContact.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = c.textHigh
                        )
                        Text(
                            text = activeContact.onionAddress,
                            style = MaterialTheme.typography.labelSmall,
                            color = c.textMid,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 180.dp)
                        )
                    }
                }

                TextButton(
                    onClick = { selectedContact = null },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(
                        text = "CLOSE CHANNEL",
                        style = MaterialTheme.typography.labelMedium,
                        color = c.textMid
                    )
                }
            }

            Spacer(modifier = Modifier.height(Space.xs))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(c.ink, CyberShapes.chip)
                    .border(1.dp, c.goldAntique.copy(alpha = 0.25f), CyberShapes.chip)
                    .padding(Space.sm)
            ) {
                if (messages.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "CHANNEL SECURED. AWAITING TELEMETRY HANDSHAKE...",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = IbmPlexMonoFontFamily,
                            color = c.textMid
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
                        verticalArrangement = Arrangement.spacedBy(Space.sm)
                    ) {
                        items(messages) { msg ->
                            val isMe = msg.senderName == "Me"
                            val bubbleColor = if (isMe) c.accent.copy(alpha = 0.1f) else NeonMagenta.copy(alpha = 0.1f)
                            val borderColor = if (isMe) c.accent.copy(alpha = 0.45f) else NeonMagenta.copy(alpha = 0.45f)
                            val align = if (isMe) Alignment.End else Alignment.Start

                            Column(modifier = Modifier.fillMaxWidth()) {
                                Box(
                                    modifier = Modifier
                                        .align(align)
                                        .widthIn(max = 260.dp)
                                        .background(bubbleColor, CyberShapes.cardCompact)
                                        .border(1.dp, borderColor, CyberShapes.cardCompact)
                                        .padding(Space.sm)
                                ) {
                                    Column {
                                        Text(
                                            text = msg.senderName.uppercase(),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (isMe) c.accent else NeonMagenta
                                        )
                                        Spacer(modifier = Modifier.height(Space.xxs))
                                        Text(
                                            text = msg.plaintext,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = c.textHigh
                                        )
                                        Spacer(modifier = Modifier.height(Space.xs))
                                        HorizontalDivider(color = c.goldAntique.copy(alpha = 0.2f))
                                        Spacer(modifier = Modifier.height(Space.xxs))
                                        Text(
                                            text = "ENC_CIPHERTEXT: ${msg.ciphertext}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = c.textMid,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "ALGORITHM: AES-256-GCM / 12-byte IV",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = c.textMid
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Space.xs))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(c.panel, CyberShapes.cardCompact)
                    .border(1.dp, c.goldAntique.copy(alpha = 0.3f), CyberShapes.cardCompact)
                    .padding(Space.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Send encrypted message...") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = c.textHigh),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = c.accent,
                        unfocusedBorderColor = c.goldAntique.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(Space.sm))
                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            viewModel.sendEncryptedMessage(activeContact.id, textInput)
                            textInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(c.accent, CyberShapes.control)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = c.ink
                    )
                }
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PEER CONTACTS",
                    style = MaterialTheme.typography.titleMedium,
                    color = c.textHigh
                )
                CyberActionButton(
                    text = "+ ADD NODE",
                    onClick = { showAddContactDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(Space.md))

            if (contacts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(c.ink, CyberShapes.cardCompact)
                        .border(1.dp, c.goldAntique.copy(alpha = 0.25f), CyberShapes.cardCompact),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "TERMINAL ADDRESS BOOK EMPTY",
                            style = MaterialTheme.typography.titleSmall,
                            color = c.textMid
                        )
                        Spacer(modifier = Modifier.height(Space.xs))
                        Text(
                            text = "Add peer nodes to initiate handshakes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textMid
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Space.sm)
                ) {
                    items(contacts) { contact ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = c.panel),
                            border = BorderStroke(1.dp, c.goldAntique.copy(alpha = 0.25f)),
                            shape = CyberShapes.cardCompact,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedContact = contact }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Space.md),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .background(c.accent.copy(alpha = 0.14f), CircleShape)
                                            .border(1.dp, c.accent, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = contact.name.take(1).uppercase(),
                                            style = MaterialTheme.typography.titleSmall,
                                            color = c.accent
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(Space.md))
                                    Column {
                                        Text(
                                            text = contact.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = c.textHigh
                                        )
                                        Text(
                                            text = "ADDR: ${contact.onionAddress}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = c.textMid,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
                                    IconButton(
                                        onClick = { selectedContact = contact },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Chat,
                                            contentDescription = "Message",
                                            tint = c.accent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteContact(contact.id) },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = c.danger,
                                            modifier = Modifier.size(18.dp)
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

    if (showAddContactDialog) {
        var nameInput by remember { mutableStateOf("") }
        var onionInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddContactDialog = false },
            title = {
                Text(
                    text = "REGISTER NEW PEER NODE",
                    style = MaterialTheme.typography.titleMedium,
                    color = c.accent
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Space.md)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Code Name") },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = onionInput,
                        onValueChange = { onionInput = it },
                        placeholder = { Text("e.g. jx7v9pxmno...onion") },
                        label = { Text("Onion / Cyber Address") },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge,
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
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("REGISTER", style = MaterialTheme.typography.labelLarge, color = c.accent)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddContactDialog = false },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("CANCEL", style = MaterialTheme.typography.labelLarge, color = c.textMid)
                }
            },
            containerColor = c.panel,
            titleContentColor = c.textHigh,
            textContentColor = c.textMid
        )
    }
}
