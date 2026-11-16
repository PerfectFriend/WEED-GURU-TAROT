package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.RajdhaniFontFamily
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
    var cryptoTab by remember { mutableStateOf(0) } // 0: Wallet & Swap, 1: NFT Market & Mint, 2: Ledger

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

    // Swap rate calculation
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
            .padding(top = 54.dp)
    ) {
        // Top Crypto Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .background(Color(0xFF0C0E1A), RoundedCornerShape(12.dp))
                .border(1.dp, currentSkin.primaryColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val subTabs = if (isRu) {
                listOf("💰 КОШЕЛЕК & ОБМЕН" to 0, "💎 NFT МАРКЕТ" to 1, "📜 РЕЕСТР" to 2)
            } else {
                listOf("💰 WALLET & SWAP" to 0, "💎 NFT MARKET" to 1, "📜 LEDGER" to 2)
            }
            subTabs.forEach { (title, idx) ->
                val isSelected = cryptoTab == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) currentSkin.primaryColor.copy(alpha = 0.2f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (isSelected) currentSkin.primaryColor else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.CLICK)
                            cryptoTab = idx
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) currentSkin.primaryColor else Color.LightGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        when (cryptoTab) {
            0 -> {
                // Wallet Balances & Instant AMM DEX Swap
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Balances Overview Card
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1222)),
                            border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(currentSkin.primaryColor, Color(0xFF00E5FF)))),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = if (isRu) "НЕКАСТОДИАЛЬНЫЙ КОШЕЛЕК" else "SELF-CUSTODIAL WALLET",
                                            fontFamily = RajdhaniFontFamily,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = currentSkin.primaryColor,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = "${profile?.weed ?: 0} \$WEED",
                                            fontFamily = RajdhaniFontFamily,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                    }
                                    CyberButton(
                                        onClick = {
                                            viewModel.addWeedCredits(50)
                                            com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
                                        },
                                        skin = currentSkin,
                                        modifier = Modifier.height(38.dp)
                                    ) {
                                        Text(
                                            text = if (isRu) "+50 ХАРВЕСТ 🌿" else "+50 HARVEST 🌿",
                                            fontFamily = RajdhaniFontFamily,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                HorizontalDivider(color = Color(0x22FFFFFF))

                                // Multi-token asset balances
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("TON", fontSize = 15.sp, color = Color(0xFFB0B0C5), fontFamily = RajdhaniFontFamily)
                                        Text(String.format(Locale.US, "%.1f", userTon), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0098EA), fontFamily = RajdhaniFontFamily)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("ETH", fontSize = 15.sp, color = Color(0xFFB0B0C5), fontFamily = RajdhaniFontFamily)
                                        Text(String.format(Locale.US, "%.3f", userEth), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8C8C8C), fontFamily = RajdhaniFontFamily)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("SOL", fontSize = 15.sp, color = Color(0xFFB0B0C5), fontFamily = RajdhaniFontFamily)
                                        Text(String.format(Locale.US, "%.2f", userSol), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF14F195), fontFamily = RajdhaniFontFamily)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("USDC", fontSize = 15.sp, color = Color(0xFFB0B0C5), fontFamily = RajdhaniFontFamily)
                                        Text(String.format(Locale.US, "%.1f", userUsdc), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2775CA), fontFamily = RajdhaniFontFamily)
                                    }
                                }
                            }
                        }
                    }

                    // AMM DEX Instant Swap Card
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF090D18)),
                            border = BorderStroke(1.dp, Color(0x3300FF88)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isRu) "⚡ МГНОВЕННЫЙ ОБМЕН (AMM SWAP)" else "⚡ INSTANT DEX SWAP",
                                        fontFamily = RajdhaniFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = NeonGreen
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf("0.5%", "1.0%", "2.5%").forEach { slip ->
                                            val isSelected = slippageTolerance == slip
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(if (isSelected) NeonGreen.copy(alpha = 0.2f) else Color(0xFF161A28))
                                                    .border(1.dp, if (isSelected) NeonGreen else Color.Transparent, RoundedCornerShape(4.dp))
                                                    .clickable { slippageTolerance = slip }
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(slip, fontSize = 14.sp, fontFamily = RajdhaniFontFamily, color = Color.White)
                                            }
                                        }
                                    }
                                }

                                // From Token
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF131726), RoundedCornerShape(10.dp))
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(if (isRu) "Вы отдаете:" else "You Pay:", fontSize = 16.sp, color = Color(0xFFB0B0C5), fontFamily = RajdhaniFontFamily)
                                        Text(
                                            text = if (isRu) "Баланс: $userWeed" else "Bal: $userWeed",
                                            fontSize = 16.sp,
                                            color = currentSkin.primaryColor,
                                            fontFamily = RajdhaniFontFamily,
                                            modifier = Modifier.clickable { swapAmountInput = userWeed.toString() }
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = swapAmountInput,
                                            onValueChange = { swapAmountInput = it },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f).height(50.dp),
                                            textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 16.sp, color = Color.White),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedBorderColor = Color.Transparent,
                                                focusedBorderColor = Color.Transparent
                                            )
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF1E2438),
                                            border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                                            modifier = Modifier.clickable {
                                                fromToken = if (fromToken == "WEED") "TON" else "WEED"
                                            }
                                        ) {
                                            Text(
                                                text = "🪙 $fromToken ▾",
                                                fontFamily = RajdhaniFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }

                                // Swap direction toggle icon
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    IconButton(
                                        onClick = {
                                            val tmp = fromToken
                                            fromToken = toToken
                                            toToken = tmp
                                            com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.CLICK)
                                        },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color(0xFF1E2438), CircleShape)
                                            .border(1.dp, currentSkin.primaryColor, CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SwapVert,
                                            contentDescription = "Swap",
                                            tint = currentSkin.primaryColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                // To Token
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF131726), RoundedCornerShape(10.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(if (isRu) "Вы получаете (оценочно):" else "You Receive (estimated):", fontSize = 16.sp, color = Color(0xFFB0B0C5), fontFamily = RajdhaniFontFamily)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = String.format(Locale.US, "%.4f", estimatedOutput),
                                            fontFamily = RajdhaniFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = NeonGreen
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF1E2438),
                                            border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                                            modifier = Modifier.clickable {
                                                toToken = if (toToken == "TON") "ETH" else if (toToken == "ETH") "USDC" else "TON"
                                            }
                                        ) {
                                            Text(
                                                text = "🪙 $toToken ▾",
                                                fontFamily = RajdhaniFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }

                                // Execute Swap Button
                                CyberButton(
                                    onClick = {
                                        if (inputNum > 0) {
                                            if (fromToken == "WEED") {
                                                if (userWeed >= inputNum.toInt()) {
                                                    viewModel.deductWeedCredits(inputNum.toInt())
                                                    com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
                                                    viewModel.postNotification(
                                                        if (isRu) "✔️ Обмен завершен: -$inputNum \$WEED ➔ +${String.format(Locale.US, "%.4f", estimatedOutput)} $toToken"
                                                        else "✔️ Swap executed: -$inputNum \$WEED ➔ +${String.format(Locale.US, "%.4f", estimatedOutput)} $toToken"
                                                    )
                                                } else {
                                                    viewModel.postNotification(if (isRu) "❌ Недостаточно \$WEED для обмена" else "❌ Insufficient \$WEED for swap")
                                                }
                                            } else {
                                                viewModel.addWeedCredits(estimatedOutput.toInt())
                                                com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
                                                viewModel.postNotification(
                                                    if (isRu) "✔️ Обмен завершен: -$inputNum $fromToken ➔ +${estimatedOutput.toInt()} \$WEED"
                                                    else "✔️ Swap executed: -$inputNum $fromToken ➔ +${estimatedOutput.toInt()} \$WEED"
                                                )
                                            }
                                        }
                                    },
                                    skin = currentSkin,
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                ) {
                                    Text(
                                        text = if (isRu) "⚡ СОВЕРШИТЬ ОБМЕН В ПУЛЕ" else "⚡ EXECUTE AMM SWAP",
                                        fontFamily = RajdhaniFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // NFT Market & Minting
                MarketScreen(viewModel)
            }
            2 -> {
                // Ledger & On-Chain History
                val transactionLogs by viewModel.transactionLogs.collectAsStateWithLifecycle()
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = if (isRu) "📜 ДЕЦЕНТРАЛИЗОВАННЫЙ РЕЕСТР" else "📜 ON-CHAIN LEDGER",
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = NeonCyan
                        )
                        Text(
                            text = if (isRu) "Хроника транзакций, смарт-контрактов и минтинга карт в сети." else "Audit trail of cryptographically verified NFT mints & transfers.",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            color = Color(0xFFB0B0C5)
                        )
                    }

                    if (transactionLogs.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(150.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isRu) "Реестр пуст. Совершите первую сделку или минт." else "Ledger clear. Execute a swap or mint to record.",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 16.sp,
                                    color = Color(0xFFB0B0C5)
                                )
                            }
                        }
                    } else {
                        items(transactionLogs) { log ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C101D)),
                                border = BorderStroke(1.dp, Color(0x22FFFFFF)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = log.type,
                                            fontFamily = RajdhaniFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = currentSkin.primaryColor
                                        )
                                        Text(
                                            text = "${log.targetParty} • ${log.rarity}",
                                            fontFamily = RajdhaniFontFamily,
                                            fontSize = 15.sp,
                                            color = Color.LightGray
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${log.price} WEED",
                                            fontFamily = RajdhaniFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = NeonGreen
                                        )
                                        Text(
                                            text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp)),
                                            fontFamily = RajdhaniFontFamily,
                                            fontSize = 14.sp,
                                            color = Color(0xFFB0B0C5)
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
