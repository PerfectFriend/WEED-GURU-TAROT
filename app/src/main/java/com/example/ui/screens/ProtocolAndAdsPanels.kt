package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CyberActionButton
import com.example.ui.components.CyberChip
import com.example.ui.components.CyberPanel
import com.example.ui.components.CyberSectionHeader
import com.example.ui.theme.Cyber
import com.example.ui.theme.CyberShapes
import com.example.ui.theme.IbmPlexMonoFontFamily
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.Space
import com.example.viewmodel.TarotViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WeedProtocolPanel(viewModel: TarotViewModel) {
    val c = Cyber.colors
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val averageRates by viewModel.compiledAverageRates.collectAsStateWithLifecycle()
    val exchangeTickers by viewModel.topExchangesTickers.collectAsStateWithLifecycle()
    val treasurerLogs by viewModel.treasurerLogs.collectAsStateWithLifecycle()
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()

    var showTickersDetail by remember { mutableStateOf(false) }
    var selectedTickerCoin by remember { mutableStateOf("BTC") }

    var currentTimeMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTimeMillis = System.currentTimeMillis()
        }
    }

    val lastClaim = profile?.lastWeedClaimTime ?: 0L
    val claimInterval = 6 * 60 * 60 * 1000L
    val timePassed = currentTimeMillis - lastClaim
    val isClaimReady = lastClaim == 0L || timePassed >= claimInterval
    val remainingMs = claimInterval - timePassed

    var swapFromAsset by remember { mutableStateOf("WEED") }
    var swapToAsset by remember { mutableStateOf("USDC") }
    var swapAmountInput by remember { mutableStateOf("10") }

    val fromRate = averageRates[swapFromAsset] ?: 0.01
    val toRate = averageRates[swapToAsset] ?: 0.01
    val calculatedReceive = remember(swapAmountInput, fromRate, toRate) {
        val amt = swapAmountInput.toDoubleOrNull() ?: 0.0
        if (toRate > 0) (amt * fromRate) / toRate else 0.0
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Space.lg),
        contentPadding = PaddingValues(bottom = Space.xxl)
    ) {
        item {
            CyberPanel(
                borderColor = currentSkin.primaryColor.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                CyberSectionHeader(
                    title = "WEED HARVEST CORE",
                    color = currentSkin.primaryColor
                )
                Spacer(modifier = Modifier.height(Space.sm))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Current Streak Balance",
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textMid
                        )
                        Text(
                            text = "${profile?.weedStreak ?: 0} Consecutive Claims",
                            style = MaterialTheme.typography.titleSmall,
                            color = c.textHigh
                        )
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = c.ink),
                        border = BorderStroke(1.dp, c.emeraldGlow.copy(alpha = 0.35f)),
                        shape = CyberShapes.chip
                    ) {
                        Text(
                            text = "Multiplier: +${profile?.weedStreak?.plus(1) ?: 1}x WEED",
                            style = MaterialTheme.typography.labelLarge,
                            color = c.emeraldGlow,
                            modifier = Modifier.padding(horizontal = Space.sm, vertical = Space.xs)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Space.md))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(c.ink, CyberShapes.chip)
                        .border(1.dp, c.goldAntique.copy(alpha = 0.25f), CyberShapes.chip)
                        .padding(Space.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(if (isClaimReady) c.emeraldGlow else c.goldLeaf, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(Space.sm))
                        Text(
                            text = if (isClaimReady) "CORE HARVESTER ARMED" else "COOLANT FLUSH IN PROGRESS",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isClaimReady) c.emeraldGlow else c.goldLeaf
                        )
                    }

                    Text(
                        text = if (isClaimReady) {
                            "READY"
                        } else {
                            val remainingSec = (remainingMs / 1000).toInt()
                            val hours = remainingSec / 3600
                            val minutes = (remainingSec % 3600) / 60
                            val seconds = remainingSec % 60
                            String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontFamily = IbmPlexMonoFontFamily,
                        color = if (isClaimReady) c.emeraldGlow else c.textHigh
                    )
                }

                Spacer(modifier = Modifier.height(Space.lg))

                CyberActionButton(
                    text = "EXTRACT ACTIVITY WEED",
                    onClick = { viewModel.claimActivityWeed() },
                    enabled = isClaimReady,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Space.sm))
                Text(
                    text = "Claim once every 6 hours. Starts at 1 WEED, increments by +1 each claim. Miss the 6h window after readiness and streak resets to 1.",
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textMid,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            CyberPanel(
                borderColor = currentSkin.secondaryColor.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                CyberSectionHeader(
                    title = "AI TREASURER AMM SWAP & TICKERS",
                    subtitle = "AGGREGATED AVERAGES (TOP 10 EXCHANGES)",
                    color = currentSkin.primaryColor
                )
                Spacer(modifier = Modifier.height(Space.md))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Space.xs)
                ) {
                    listOf("BTC", "ETH", "SOL", "WEED").forEach { coin ->
                        val isSelectedCoin = selectedTickerCoin == coin
                        val rate = averageRates[coin] ?: 0.0
                        val displayPrice = if (rate >= 1.0) {
                            "$${String.format(Locale.US, "%.2f", rate)}"
                        } else {
                            "$${String.format(Locale.US, "%.4f", rate)}"
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 52.dp)
                                .clickable {
                                    selectedTickerCoin = coin
                                    showTickersDetail = true
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelectedCoin) c.ink else c.panelRaised
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isSelectedCoin) currentSkin.primaryColor else c.goldAntique.copy(alpha = 0.3f)
                            ),
                            shape = CyberShapes.chip
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Space.sm),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = coin,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSelectedCoin) currentSkin.primaryColor else c.textHigh
                                )
                                Text(
                                    text = displayPrice,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = c.textMid
                                )
                            }
                        }
                    }
                }

                if (showTickersDetail) {
                    Spacer(modifier = Modifier.height(Space.md))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = c.ink),
                        border = BorderStroke(1.dp, c.goldAntique.copy(alpha = 0.3f)),
                        shape = CyberShapes.chip
                    ) {
                        Column(modifier = Modifier.padding(Space.md)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$selectedTickerCoin LIVE ARBITRAGE ARRAYS",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = currentSkin.primaryColor
                                )
                                Text(
                                    text = "CLOSE",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = c.danger,
                                    modifier = Modifier
                                        .clickable { showTickersDetail = false }
                                        .padding(Space.sm)
                                )
                            }
                            Spacer(modifier = Modifier.height(Space.xs))

                            val exchanges = listOf(
                                "Binance", "Coinbase", "Kraken", "OKX", "Bybit",
                                "Huobi", "Gate.io", "Bitfinex", "KuCoin", "Gemini"
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(Space.xxs)) {
                                exchanges.forEach { ex ->
                                    val price = exchangeTickers[ex]?.get(selectedTickerCoin) ?: averageRates[selectedTickerCoin] ?: 0.0
                                    val displayPrice = if (price >= 1.0) {
                                        "$${String.format(Locale.US, "%.3f", price)}"
                                    } else {
                                        "$${String.format(Locale.US, "%.5f", price)}"
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "• $ex",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = c.textMid
                                        )
                                        Text(
                                            text = displayPrice,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = c.textHigh
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Space.lg))

                Text(
                    text = "ROUTER SWAP UTILITY (100% UNISWAP ROUTER SIM)",
                    style = MaterialTheme.typography.labelLarge,
                    color = c.textMid,
                    modifier = Modifier.padding(bottom = Space.xs)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Space.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SELL ASSET",
                            style = MaterialTheme.typography.labelMedium,
                            color = c.textMid
                        )
                        Spacer(modifier = Modifier.height(Space.xs))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .background(c.ink, CyberShapes.chip)
                                .border(1.dp, c.goldAntique.copy(alpha = 0.3f), CyberShapes.chip)
                                .clickable {
                                    val next = when (swapFromAsset) {
                                        "WEED" -> "USDC"
                                        "USDC" -> "SOL"
                                        "SOL" -> "ETH"
                                        else -> "WEED"
                                    }
                                    swapFromAsset = next
                                    if (next != "WEED") {
                                        swapToAsset = "WEED"
                                    } else if (swapToAsset == "WEED") {
                                        swapToAsset = "USDC"
                                    }
                                }
                                .padding(horizontal = Space.md),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = swapFromAsset,
                                style = MaterialTheme.typography.titleSmall,
                                color = c.textHigh
                            )
                            Text(text = "▼", style = MaterialTheme.typography.labelMedium, color = c.textMid)
                        }
                        Spacer(modifier = Modifier.height(Space.xs))
                        OutlinedTextField(
                            value = swapAmountInput,
                            onValueChange = { swapAmountInput = it },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = c.textHigh),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Text(
                        text = "→",
                        style = MaterialTheme.typography.titleLarge,
                        color = c.textMid
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "RECEIVE ASSET",
                            style = MaterialTheme.typography.labelMedium,
                            color = c.textMid
                        )
                        Spacer(modifier = Modifier.height(Space.xs))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .background(c.ink, CyberShapes.chip)
                                .border(1.dp, c.goldAntique.copy(alpha = 0.3f), CyberShapes.chip)
                                .clickable {
                                    val next = when (swapToAsset) {
                                        "WEED" -> "USDC"
                                        "USDC" -> "SOL"
                                        "SOL" -> "ETH"
                                        else -> "WEED"
                                    }
                                    swapToAsset = next
                                    if (next != "WEED") {
                                        swapFromAsset = "WEED"
                                    } else if (swapFromAsset == "WEED") {
                                        swapFromAsset = "USDC"
                                    }
                                }
                                .padding(horizontal = Space.md),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = swapToAsset,
                                style = MaterialTheme.typography.titleSmall,
                                color = c.textHigh
                            )
                            Text(text = "▼", style = MaterialTheme.typography.labelMedium, color = c.textMid)
                        }
                        Spacer(modifier = Modifier.height(Space.xs))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .background(c.panelRaised, CyberShapes.chip)
                                .border(1.dp, c.goldAntique.copy(alpha = 0.3f), CyberShapes.chip)
                                .padding(horizontal = Space.sm),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            val outPrice = if (calculatedReceive >= 1.0) {
                                String.format(Locale.US, "%.2f", calculatedReceive)
                            } else {
                                String.format(Locale.US, "%.4f", calculatedReceive)
                            }
                            Text(
                                text = outPrice,
                                style = MaterialTheme.typography.titleSmall,
                                fontFamily = IbmPlexMonoFontFamily,
                                color = c.emeraldGlow
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Space.lg))

                CyberActionButton(
                    text = "EXECUTE AUTOMATED AMM ROUTER",
                    onClick = {
                        val amt = swapAmountInput.toDoubleOrNull() ?: 0.0
                        viewModel.swapAssets(swapFromAsset, swapToAsset, amt, calculatedReceive)
                    },
                    secondary = true,
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
                    title = "AI TREASURER INTERNALS LEDGER",
                    color = c.textHigh
                )
                Spacer(modifier = Modifier.height(Space.sm))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(c.ink, CyberShapes.chip)
                        .border(1.dp, c.goldAntique.copy(alpha = 0.25f), CyberShapes.chip)
                        .padding(Space.sm)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(Space.xs)
                    ) {
                        items(treasurerLogs) { log ->
                            val color = when {
                                log.contains("[MINT]") -> c.emeraldGlow
                                log.contains("[BURN]") -> NeonMagenta
                                log.contains("[SWAP]") -> NeonCyan
                                else -> c.textMid
                            }
                            Text(
                                text = log,
                                style = MaterialTheme.typography.labelSmall,
                                color = color
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NeuralAdsPanel(viewModel: TarotViewModel) {
    val c = Cyber.colors
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val enabledThemes by viewModel.enabledAdThemes.collectAsStateWithLifecycle()
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()

    var adPromptInput by remember { mutableStateOf("") }
    var selectedAdTheme by remember { mutableStateOf("Cybernetics") }
    var selectedColorPreset by remember { mutableStateOf("#FF00FF") }

    val isAdFreeSubscribed = profile?.isAdFreeSubscribed == true && (profile?.adFreeExpiryTime ?: 0L) > System.currentTimeMillis()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Space.lg),
        contentPadding = PaddingValues(bottom = Space.xxl)
    ) {
        item {
            CyberPanel(
                borderColor = if (isAdFreeSubscribed) c.emeraldGlow else c.goldAntique.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                CyberSectionHeader(
                    title = "AD-SHIELD DEFLATIONARY PROTOCOL",
                    color = if (isAdFreeSubscribed) c.emeraldGlow else c.textHigh
                )
                Spacer(modifier = Modifier.height(Space.sm))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Status Indicator",
                            style = MaterialTheme.typography.labelMedium,
                            color = c.textMid
                        )
                        Text(
                            text = if (isAdFreeSubscribed) "ACTIVE PROTECTION LAYER" else "UNPROTECTED TUNNELS",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isAdFreeSubscribed) c.emeraldGlow else c.goldLeaf
                        )
                    }

                    if (isAdFreeSubscribed) {
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        val expDate = sdf.format(Date(profile?.adFreeExpiryTime ?: 0L))
                        Text(
                            text = "Expires: $expDate",
                            style = MaterialTheme.typography.labelSmall,
                            color = c.textMid
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Space.md))

                CyberActionButton(
                    text = "BUY 30-DAY AD-SHIELD (500 WEED)",
                    onClick = { viewModel.subscribeAdFree {} },
                    secondary = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(Space.xs))
                Text(
                    text = "Ad-Shield filters incoming sponsored broadcast cards instantly. 100% of WEED spent is burned by the AI Treasurer.",
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textMid,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            CyberPanel(
                borderColor = currentSkin.primaryColor.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                CyberSectionHeader(
                    title = "REGISTER BROADCAST INTERNALS SLOT",
                    color = currentSkin.primaryColor
                )
                Spacer(modifier = Modifier.height(Space.md))

                Text(
                    text = "AD PROMPT (What are you advertising? AI writes it)",
                    style = MaterialTheme.typography.labelMedium,
                    color = c.textMid
                )
                Spacer(modifier = Modifier.height(Space.xs))
                OutlinedTextField(
                    value = adPromptInput,
                    onValueChange = { adPromptInput = it },
                    placeholder = { Text("e.g., Sell custom cyberdecks with preloaded tunnels.") },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = c.textHigh),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 72.dp)
                )

                Spacer(modifier = Modifier.height(Space.md))

                Text(
                    text = "BROADCAST TOPIC/THEME",
                    style = MaterialTheme.typography.labelMedium,
                    color = c.textMid
                )
                Spacer(modifier = Modifier.height(Space.xs))

                val themes = listOf("Cybernetics", "Tarot", "Biotech", "Darknet", "AI Overlords", "Quantum Finance")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(Space.sm),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(themes) { th ->
                        CyberChip(
                            label = th.uppercase(),
                            selected = selectedAdTheme == th,
                            onClick = { selectedAdTheme = th }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Space.md))

                Text(
                    text = "GLOW ACCENT COLOR PRESET",
                    style = MaterialTheme.typography.labelMedium,
                    color = c.textMid
                )
                Spacer(modifier = Modifier.height(Space.xs))

                val presetColors = listOf(
                    "#FF00FF" to NeonMagenta,
                    "#00FF00" to c.emeraldGlow,
                    "#00FFFF" to NeonCyan,
                    "#FFD700" to c.goldLeaf,
                    "#FF4500" to c.danger
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Space.md),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    presetColors.forEach { (hex, col) ->
                        val isSel = selectedColorPreset == hex
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable { selectedColorPreset = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(col, CircleShape)
                                    .border(
                                        width = 2.dp,
                                        color = if (isSel) c.textHigh else c.ink,
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Space.lg))

                CyberActionButton(
                    text = "COMPILE & BROADCAST SLOT (100 WEED)",
                    onClick = {
                        viewModel.purchaseAdBanner(adPromptInput, selectedAdTheme, selectedColorPreset) { success ->
                            if (success) adPromptInput = ""
                        }
                    },
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
                    title = "QUEUE SEGMENT FILTERS (MINIMUM 3 ACTIVE)",
                    subtitle = "Keep at least 3 active broadcast sectors enabled at all times.",
                    color = c.textHigh
                )
                Spacer(modifier = Modifier.height(Space.md))

                val themes = listOf("Cybernetics", "Tarot", "Biotech", "Darknet", "AI Overlords", "Quantum Finance")
                Column(verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                    themes.forEach { th ->
                        val isEnabled = enabledThemes.contains(th)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .background(c.ink, CyberShapes.chip)
                                .border(1.dp, c.goldAntique.copy(alpha = 0.3f), CyberShapes.chip)
                                .clickable { viewModel.toggleAdTheme(th) }
                                .padding(horizontal = Space.md, vertical = Space.sm),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = th.uppercase(),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isEnabled) c.textHigh else c.textMid
                            )
                            Text(
                                text = if (isEnabled) "ACTIVE (TUNNEL ON)" else "SHUTDOWN",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isEnabled) c.emeraldGlow else c.danger
                            )
                        }
                    }
                }
            }
        }
    }
}
