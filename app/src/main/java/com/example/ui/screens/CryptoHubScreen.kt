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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.CyberNoirSynthPlayer
import com.example.ui.components.CyberActionButton
import com.example.ui.components.CyberChip
import com.example.ui.components.CyberPanel
import com.example.ui.theme.Cyber
import com.example.ui.theme.CyberShapes
import com.example.ui.theme.IbmPlexMonoFontFamily
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.Space
import com.example.viewmodel.TarotViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Unified Crypto Hub: Decentralized Wallet Balances, Instant AMM Swap, NFT Market, and On-chain Ledger
 */
@Composable
fun CryptoHubScreen(viewModel: TarotViewModel) {
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val lang = profile?.systemLanguage ?: "en"
    val isRu = lang in listOf("ru", "uk")
    val c = Cyber.colors
    val haptic = LocalHapticFeedback.current
    var cryptoTab by rememberSaveable { mutableIntStateOf(0) } // 0: Wallet & Swap, 1: NFT Market & Mint, 2: Ledger

    // AMM DEX Swap state
    var fromToken by remember { mutableStateOf("WEED") }
    var toToken by remember { mutableStateOf("TON") }
    var swapAmountInput by remember { mutableStateOf("100") }
    var slippageTolerance by remember { mutableStateOf("0.5%") }
    val userWeed = profile?.weed ?: 0
    val userTon = profile?.balanceTon ?: 50.0
    val userEth = profile?.balanceEth ?: 0.5
    val userSol = profile?.balanceSol ?: 2.0
    val userUsdc = 250.0

    val inputNum = swapAmountInput.toDoubleOrNull() ?: 0.0
    val estimatedOutput = remember(fromToken, toToken, inputNum) {
        when {
            fromToken == "WEED" && toToken == "TON" -> inputNum * 0.005
            fromToken == "WEED" && toToken == "ETH" -> inputNum * 0.00035
            fromToken == "WEED" && toToken == "USDC" -> inputNum * 0.1
            fromToken == "TON" && toToken == "WEED" -> inputNum * 200.0
            fromToken == "ETH" && toToken == "WEED" -> inputNum * 2850.0
            fromToken == "USDC" && toToken == "WEED" -> inputNum * 10.0
            else -> inputNum * 1.0
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = Space.xs)
    ) {
        // Top Crypto Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Space.lg, vertical = Space.xs)
                .background(c.panel, CyberShapes.control)
                .border(1.dp, c.goldAntique.copy(alpha = 0.35f), CyberShapes.control)
                .padding(Space.xs),
            horizontalArrangement = Arrangement.spacedBy(Space.xs)
        ) {
            val subTabs = if (isRu) {
                listOf(
                    Triple("КОШЕЛЕК", Icons.Default.AccountBalanceWallet, 0),
                    Triple("NFT МАРКЕТ", Icons.Default.Storefront, 1),
                    Triple("РЕЕСТР", Icons.AutoMirrored.Filled.ReceiptLong, 2)
                )
            } else {
                listOf(
                    Triple("WALLET", Icons.Default.AccountBalanceWallet, 0),
                    Triple("NFT MARKET", Icons.Default.Storefront, 1),
                    Triple("LEDGER", Icons.AutoMirrored.Filled.ReceiptLong, 2)
                )
            }
            subTabs.forEach { (title, icon, idx) ->
                val isSelected = cryptoTab == idx
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .clip(CyberShapes.chip)
                        .background(if (isSelected) c.accent.copy(alpha = 0.18f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (isSelected) c.accent else Color.Transparent,
                            CyberShapes.chip
                        )
                        .clickable(role = Role.Tab) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.CLICK)
                            cryptoTab = idx
                        }
                        .padding(horizontal = Space.xs),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) c.accent else c.textMid,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(Space.xs))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) c.accent else c.textMid,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        when (cryptoTab) {
            0 -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Space.lg),
                    contentPadding = PaddingValues(top = Space.sm, bottom = 84.dp),
                    verticalArrangement = Arrangement.spacedBy(Space.md)
                ) {
                    // Balances Overview Card
                    item {
                        CyberPanel(
                            borderColor = currentSkin.primaryColor.copy(alpha = 0.65f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isRu) "НЕКАСТОДИАЛЬНЫЙ КОШЕЛЕК" else "SELF-CUSTODIAL WALLET",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = c.goldLeaf
                                    )
                                    Spacer(modifier = Modifier.height(Space.xxs))
                                    Text(
                                        text = "${profile?.weed ?: 0} \$WEED",
                                        style = MaterialTheme.typography.headlineSmall.copy(fontFamily = IbmPlexMonoFontFamily),
                                        color = c.textHigh
                                    )
                                }
                                CyberActionButton(
                                    text = if (isRu) "+50 ХАРВЕСТ" else "+50 HARVEST",
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.addWeedCredits(50)
                                        CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(Space.md))
                            HorizontalDivider(color = c.goldAntique.copy(alpha = 0.25f))
                            Spacer(modifier = Modifier.height(Space.md))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                WalletMetricCell("TON", String.format(Locale.US, "%.1f", userTon), NeonCyan)
                                WalletMetricCell("ETH", String.format(Locale.US, "%.3f", userEth), c.textHigh)
                                WalletMetricCell("SOL", String.format(Locale.US, "%.2f", userSol), NeonGreen)
                                WalletMetricCell("USDC", String.format(Locale.US, "%.1f", userUsdc), NeonGold)
                            }
                        }
                    }

                    // AMM DEX Instant Swap Card
                    item {
                        CyberPanel(
                            borderColor = c.accent.copy(alpha = 0.45f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(Space.xs),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = c.accent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = if (isRu) "МГНОВЕННЫЙ ОБМЕН" else "INSTANT DEX SWAP",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = c.accent
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
                                    listOf("0.5%", "1.0%", "2.5%").forEach { slip ->
                                        CyberChip(
                                            label = slip,
                                            selected = slippageTolerance == slip,
                                            onClick = { slippageTolerance = slip }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(Space.md))

                            // From Token
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(c.ink, CyberShapes.control)
                                    .border(1.dp, c.goldAntique.copy(alpha = 0.25f), CyberShapes.control)
                                    .padding(Space.md)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isRu) "Вы отдаете:" else "You Pay:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = c.textMid
                                    )
                                    Text(
                                        text = if (isRu) "Баланс: $userWeed" else "Bal: $userWeed",
                                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = IbmPlexMonoFontFamily),
                                        color = c.accent,
                                        modifier = Modifier
                                            .clip(CyberShapes.chip)
                                            .clickable { swapAmountInput = userWeed.toString() }
                                            .padding(horizontal = Space.xs, vertical = Space.xxs)
                                    )
                                }
                                Spacer(modifier = Modifier.height(Space.xs))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = swapAmountInput,
                                        onValueChange = { swapAmountInput = it },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        textStyle = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = IbmPlexMonoFontFamily,
                                            color = c.textHigh
                                        ),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = Color.Transparent,
                                            focusedBorderColor = Color.Transparent
                                        )
                                    )
                                    Surface(
                                        shape = CyberShapes.chip,
                                        color = c.panelRaised,
                                        border = BorderStroke(1.dp, c.goldAntique.copy(alpha = 0.45f)),
                                        modifier = Modifier
                                            .heightIn(min = 48.dp)
                                            .clickable {
                                                fromToken = if (fromToken == "WEED") "TON" else "WEED"
                                            }
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.padding(horizontal = Space.md)
                                        ) {
                                            Text(
                                                text = "$fromToken ▾",
                                                style = MaterialTheme.typography.labelLarge.copy(fontFamily = IbmPlexMonoFontFamily),
                                                color = c.textHigh
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(Space.sm))

                            // Swap direction toggle icon
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        val tmp = fromToken
                                        fromToken = toToken
                                        toToken = tmp
                                        CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.CLICK)
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(c.panelRaised, CircleShape)
                                        .border(1.dp, c.accent, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SwapVert,
                                        contentDescription = "Swap direction",
                                        tint = c.accent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(Space.sm))

                            // To Token
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(c.ink, CyberShapes.control)
                                    .border(1.dp, c.goldAntique.copy(alpha = 0.25f), CyberShapes.control)
                                    .padding(Space.md)
                            ) {
                                Text(
                                    text = if (isRu) "Вы получаете (оценочно):" else "You Receive (estimated):",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = c.textMid
                                )
                                Spacer(modifier = Modifier.height(Space.xs))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = String.format(Locale.US, "%.4f", estimatedOutput),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontFamily = IbmPlexMonoFontFamily,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = c.accent
                                    )
                                    Surface(
                                        shape = CyberShapes.chip,
                                        color = c.panelRaised,
                                        border = BorderStroke(1.dp, c.goldAntique.copy(alpha = 0.45f)),
                                        modifier = Modifier
                                            .heightIn(min = 48.dp)
                                            .clickable {
                                                toToken = when (toToken) {
                                                    "TON" -> "ETH"
                                                    "ETH" -> "USDC"
                                                    else -> "TON"
                                                }
                                            }
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.padding(horizontal = Space.md)
                                        ) {
                                            Text(
                                                text = "$toToken ▾",
                                                style = MaterialTheme.typography.labelLarge.copy(fontFamily = IbmPlexMonoFontFamily),
                                                color = c.textHigh
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(Space.md))

                            CyberActionButton(
                                text = if (isRu) "СОВЕРШИТЬ ОБМЕН В ПУЛЕ" else "EXECUTE AMM SWAP",
                                onClick = {
                                    if (inputNum > 0) {
                                        if (fromToken == "WEED") {
                                            if (userWeed >= inputNum.toInt()) {
                                                viewModel.deductWeedCredits(inputNum.toInt())
                                                CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
                                                viewModel.postNotification(
                                                    if (isRu) "Обмен завершен: -$inputNum \$WEED -> +${String.format(Locale.US, "%.4f", estimatedOutput)} $toToken"
                                                    else "Swap executed: -$inputNum \$WEED -> +${String.format(Locale.US, "%.4f", estimatedOutput)} $toToken"
                                                )
                                            } else {
                                                viewModel.postNotification(if (isRu) "Недостаточно \$WEED для обмена" else "Insufficient \$WEED for swap")
                                            }
                                        } else {
                                            viewModel.addWeedCredits(estimatedOutput.toInt())
                                            CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
                                            viewModel.postNotification(
                                                if (isRu) "Обмен завершен: -$inputNum $fromToken -> +${estimatedOutput.toInt()} \$WEED"
                                                else "Swap executed: -$inputNum $fromToken -> +${estimatedOutput.toInt()} \$WEED"
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
            1 -> {
                MarketScreen(viewModel)
            }
            2 -> {
                val transactionLogs by viewModel.transactionLogs.collectAsStateWithLifecycle()
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Space.lg),
                    contentPadding = PaddingValues(top = Space.sm, bottom = 84.dp),
                    verticalArrangement = Arrangement.spacedBy(Space.sm)
                ) {
                    item {
                        Text(
                            text = if (isRu) "ДЕЦЕНТРАЛИЗОВАННЫЙ РЕЕСТР" else "ON-CHAIN LEDGER",
                            style = MaterialTheme.typography.titleMedium,
                            color = c.goldLeaf
                        )
                        Text(
                            text = if (isRu) "Хроника транзакций, смарт-контрактов и минтинга карт в сети." else "Audit trail of cryptographically verified NFT mints & transfers.",
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textMid,
                            modifier = Modifier.padding(top = Space.xxs, bottom = Space.xs)
                        )
                    }

                    if (transactionLogs.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isRu) "Реестр пуст. Совершите первую сделку или минт." else "Ledger clear. Execute a swap or mint to record.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = c.textMid
                                )
                            }
                        }
                    } else {
                        items(transactionLogs) { log ->
                            CyberPanel(
                                borderColor = c.goldAntique.copy(alpha = 0.25f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = log.type,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = currentSkin.primaryColor
                                        )
                                        Text(
                                            text = "${log.targetParty} • ${log.rarity}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = c.textMid
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${log.price} WEED",
                                            style = MaterialTheme.typography.labelLarge.copy(fontFamily = IbmPlexMonoFontFamily),
                                            color = c.accent
                                        )
                                        Text(
                                            text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp)),
                                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = IbmPlexMonoFontFamily),
                                            color = c.textMid
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
}

@Composable
private fun WalletMetricCell(label: String, value: String, color: Color) {
    val c = Cyber.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = c.textMid
        )
        Spacer(modifier = Modifier.height(Space.xxs))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = IbmPlexMonoFontFamily,
                fontWeight = FontWeight.Bold
            ),
            color = color
        )
    }
}
