package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirlineStops
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CyberTarotDeck
import com.example.data.MintedCard
import com.example.data.PeerContact
import com.example.ui.theme.CyberCharcoal
import com.example.ui.theme.CyberObsidian
import com.example.ui.theme.ElegantIndigo
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonRed
import com.example.ui.theme.RajdhaniFontFamily
import com.example.viewmodel.TarotViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TokenAsset(
    val symbol: String,
    val name: String,
    val balance: Double,
    val depositAddress: String,
    val priceUsd: Double,
    val color: Color,
    val icon: ImageVector
)

@Composable
fun P2pLedgerPanel(viewModel: TarotViewModel) {
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val contacts by viewModel.peerContacts.collectAsStateWithLifecycle()
    val transactions by viewModel.p2pTransactions.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val mintedCards by viewModel.mintedCards.collectAsStateWithLifecycle()

    val p2pStatus by viewModel.p2pStatus.collectAsStateWithLifecycle()
    val miningProgress by viewModel.miningProgress.collectAsStateWithLifecycle()

    var showTransferDialog by remember { mutableStateOf(false) }
    var showDepositDialog by remember { mutableStateOf(false) }
    var showSendDialog by remember { mutableStateOf(false) }
    var showSwapDialog by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Wallet Balances
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                border = BorderStroke(1.dp, NeonCyan),
                shape = RoundedCornerShape(8.dp),
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
                                text = "🧪 SANDBOX P2P LEDGER (LOCAL ECDSA)",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 15.sp,
                                color = NeonGold
                            )
                            Text(
                                text = "${profile?.username ?: "Netrunner"}_LOCAL_LEDGER",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        CyberTextButton(
                            onClick = { showTransferDialog = true },
                            text = "BROADCAST TX",
                            skin = currentSkin,
                            containerColor = NeonCyan,
                            contentColor = CyberObsidian,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color.DarkGray, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "WEED BALANCE",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 14.sp,
                                color = Color(0xFFB0B0C5)
                            )
                            Text(
                                text = "${profile?.weed ?: 0} WEED",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "TAROT MODULES OWNED",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 14.sp,
                                color = Color(0xFFB0B0C5)
                            )
                            Text(
                                text = "${mintedCards.size} NODES",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonMagenta
                            )
                        }
                    }
                }
            }
        }

        // Tokenized Actives Section (ETH, TON, SOL, TRX, USDC)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                border = BorderStroke(1.dp, NeonMagenta.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DECENTRALIZED TOKENIZED ACTIVES",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonMagenta
                    )
                    Text(
                        text = "Multi-chain asset balances integrated via secure bridge protocol",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 14.sp,
                        color = Color(0xFFB0B0C5),
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    val activeTokens = listOf(
                        TokenAsset("ETH", "Ethereum Core", profile?.balanceEth ?: 0.5, "0x9E88d6C7BaF8F4f3183f233f6329686259e7ab9C", 3250.0, NeonCyan, Icons.Default.CurrencyExchange),
                        TokenAsset("TON", "The Open Network (Gram)", profile?.balanceTon ?: 50.0, "UQBgN6V569MhR18ePoPnXJpL_7XbA1f_4b9C9e7aB_90C7", 7.20, NeonCyan, Icons.Default.AirlineStops),
                        TokenAsset("SOL", "Solana Network", profile?.balanceSol ?: 2.0, "8Fx3C4WqpQePy79CbC9e7aB90C733f6329686259e7a", 142.50, NeonMagenta, Icons.Default.Bolt),
                        TokenAsset("TRX", "TRON Protocol", profile?.balanceTrx ?: 250.0, "TY48Xm9qYePy79CbC9e7aB90C733f6329686259e7ab9C", 0.12, NeonRed, Icons.Default.Timeline),
                        TokenAsset("USDC", "Circle USD Coin", profile?.balanceUsdc ?: 100.0, "0x9E88d6C7BaF8F4f3183f233f6329686259e7ab9C", 1.00, Color.White, Icons.Default.RadioButtonChecked)
                    )

                    activeTokens.forEach { token ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(Color(0xFF0D0D15), RoundedCornerShape(6.dp))
                                .border(1.dp, Color.DarkGray, RoundedCornerShape(6.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(token.color.copy(alpha = 0.1f), CircleShape)
                                        .border(1.dp, token.color.copy(alpha = 0.3f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = token.icon,
                                        contentDescription = token.symbol,
                                        tint = token.color,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = token.symbol,
                                            fontFamily = RajdhaniFontFamily,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        if (token.symbol == "TON") {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "GRAM",
                                                fontFamily = RajdhaniFontFamily,
                                                fontSize = 16.sp,
                                                color = Color.Yellow,
                                                modifier = Modifier
                                                    .background(Color.Yellow.copy(alpha = 0.15f), RoundedCornerShape(2.dp))
                                                    .padding(horizontal = 3.dp, vertical = 0.5.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = token.name,
                                        fontFamily = RajdhaniFontFamily,
                                        fontSize = 14.sp,
                                        color = Color(0xFFB0B0C5)
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${String.format("%.4f", token.balance)} ${token.symbol}",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = token.color
                                )
                                Text(
                                    text = "$${String.format("%.2f", token.balance * token.priceUsd)} USD",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 14.sp,
                                    color = Color(0xFFB0B0C5)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CyberTextButton(
                            onClick = { showDepositDialog = true },
                            text = "DEPOSIT",
                            skin = currentSkin,
                            containerColor = NeonGreen.copy(alpha = 0.1f),
                            contentColor = NeonGreen,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )

                        CyberTextButton(
                            onClick = { showSendDialog = true },
                            text = "SEND",
                            skin = currentSkin,
                            containerColor = NeonRed.copy(alpha = 0.1f),
                            contentColor = NeonRed,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )

                        CyberTextButton(
                            onClick = { showSwapDialog = true },
                            text = "SWAP / DEX",
                            skin = currentSkin,
                            containerColor = NeonCyan.copy(alpha = 0.1f),
                            contentColor = NeonCyan,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.2f)
                        )
                    }
                }
            }
        }

        // Ledger Block Explorer title
        item {
            Text(
                text = "BLOCKCHAIN LEDGER EXPLORER",
                fontFamily = RajdhaniFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 1.sp
            )
        }

        if (transactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color.Black, RoundedCornerShape(8.dp))
                        .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AWAITING GENESIS BLOCK PROTOCOLS...",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 15.sp,
                        color = Color(0xFFB0B0C5)
                    )
                }
            }
        } else {
            items(transactions) { tx ->
                val payloadText = if (tx.cardIndex != -1) {
                    val card = CyberTarotDeck.getCard(tx.cardIndex)
                    "Transferred Card MODULE #${String.format("%02d", tx.cardIndex)} (${card.name})"
                } else {
                    "Sent Weed allocation: ${tx.amountWeed} WEED"
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF07070F)),
                    border = BorderStroke(1.dp, Color.DarkGray),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "BLOCK #${String.format("%04d", tx.blockNumber)}",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                            Text(
                                text = "NONCE: ${tx.nonce}",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 17.sp,
                                color = Color(0xFFB0B0C5)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "HASH: ${tx.blockHash}",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 17.sp,
                            color = NeonMagenta
                        )
                        Text(
                            text = "PREV: ${tx.previousHash}",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 17.sp,
                            color = Color(0xFFB0B0C5)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "FROM: ${tx.senderAddress}",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "TO: ${tx.recipientAddress}",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "DATA: $payloadText",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 15.sp,
                            color = NeonGold,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "SIGNATURE: ${tx.signature}",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 17.sp,
                            color = Color(0xFFB0B0C5)
                        )
                    }
                }
            }
        }
    }

    val currentProgress = miningProgress
    if (p2pStatus == "MINING" && currentProgress != null) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(
                    text = "MINING TRANSACTIONS BLOCK...",
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Executing cryptographical Proof-of-Work hashes sequentially. Finding SHA-256 block hash prefix matching difficulty targets...",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 14.sp,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { currentProgress },
                        color = NeonCyan,
                        trackColor = Color.DarkGray,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "NONCE SCANS: ${(currentProgress * 100000).toInt()}/100000",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 17.sp,
                        color = Color(0xFFB0B0C5)
                    )
                }
            },
            confirmButton = {},
            dismissButton = {},
            containerColor = CyberObsidian,
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }

    if (p2pStatus == "SUCCESS") {
        AlertDialog(
            onDismissRequest = { viewModel.resetP2pStatus() },
            title = {
                Text(
                    text = "🧪 [SANDBOX] LEDGER BLOCK SIGNED",
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonGreen
                )
            },
            text = {
                Text(
                    text = "Local ECDSA P-256 signature & proof-of-work hash recorded in your Sandbox ledger. No real WEED or Vault cards were burned.",
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 15.sp,
                    color = Color.LightGray
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.resetP2pStatus() }) {
                    Text("OK", fontFamily = RajdhaniFontFamily, color = NeonGreen)
                }
            },
            containerColor = CyberCharcoal
        )
    }

    if (showTransferDialog) {
        var selectedRecipient by remember { mutableStateOf<PeerContact?>(null) }
        var isCardTransfer by remember { mutableStateOf(false) }
        var creditsAmount by remember { mutableStateOf("100") }
        var selectedCardToTransfer by remember { mutableStateOf<MintedCard?>(null) }
        var passwordInput by remember { mutableStateOf("") }
        var dropdownExpanded by remember { mutableStateOf(false) }
        var cardDropdownExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showTransferDialog = false },
            title = {
                Text(
                    text = "🧪 SANDBOX P2P TRANSFER BLOCK",
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column {
                        Text(
                            text = "RECIPIENT PEER NODE",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 17.sp,
                            color = Color(0xFFB0B0C5)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black, RoundedCornerShape(4.dp))
                                .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                                .clickable { dropdownExpanded = true }
                                .padding(12.dp)
                        ) {
                            Text(
                                text = selectedRecipient?.name ?: "SELECT PEER NODE...",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 16.sp,
                                color = if (selectedRecipient != null) Color.White else Color(0xFFB0B0C5)
                            )
                        }

                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier.background(CyberCharcoal)
                        ) {
                            contacts.forEach { contact ->
                                DropdownMenuItem(
                                    text = { Text(contact.name, fontFamily = RajdhaniFontFamily, color = Color.White) },
                                    onClick = {
                                        selectedRecipient = contact
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Column {
                        Text(
                            text = "ASSET CLASS TO BROADCAST",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 17.sp,
                            color = Color(0xFFB0B0C5)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black, RoundedCornerShape(4.dp))
                                .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                                .padding(4.dp)
                        ) {
                            listOf(false to "WEED", true to "TAROT NFT MODULE").forEach { (isCard, label) ->
                                val isSel = isCardTransfer == isCard
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (isSel) NeonCyan.copy(alpha = 0.2f) else Color.Transparent,
                                            RoundedCornerShape(3.dp)
                                        )
                                        .clickable { isCardTransfer = isCard }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontFamily = RajdhaniFontFamily,
                                        fontSize = 15.sp,
                                        color = if (isSel) NeonCyan else Color(0xFFB0B0C5)
                                    )
                                }
                            }
                        }
                    }

                    if (isCardTransfer) {
                        Column {
                            Text(
                                text = "SELECT TAROT MODULE",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 17.sp,
                                color = Color(0xFFB0B0C5)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black, RoundedCornerShape(4.dp))
                                    .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                                    .clickable { cardDropdownExpanded = true }
                                    .padding(12.dp)
                            ) {
                                val cardName = selectedCardToTransfer?.let {
                                    val card = CyberTarotDeck.getCard(it.cardIndex)
                                    "NODE #${String.format("%02d", it.cardIndex)}: ${card.name} (#${String.format("%04d", it.mintNumber)})"
                                } ?: "SELECT OWNED NFT..."
                                Text(
                                    text = cardName,
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 16.sp,
                                    color = if (selectedCardToTransfer != null) Color.White else Color(0xFFB0B0C5)
                                )
                            }

                            DropdownMenu(
                                expanded = cardDropdownExpanded,
                                onDismissRequest = { cardDropdownExpanded = false },
                                modifier = Modifier.background(CyberCharcoal)
                            ) {
                                mintedCards.forEach { card ->
                                    val info = CyberTarotDeck.getCard(card.cardIndex)
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "NODE #${String.format("%02d", card.cardIndex)}: ${info.name} (#${String.format("%04d", card.mintNumber)})",
                                                fontFamily = RajdhaniFontFamily,
                                                color = Color.White
                                            )
                                        },
                                        onClick = {
                                            selectedCardToTransfer = card
                                            cardDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = creditsAmount,
                            onValueChange = { creditsAmount = it },
                            label = { Text("Amount Weed", fontFamily = RajdhaniFontFamily) },
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = RajdhaniFontFamily),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("BIP39 Seed Phrase (Signature)", fontFamily = RajdhaniFontFamily) },
                        placeholder = { Text("Enter private keys/seed to authorize", fontFamily = RajdhaniFontFamily, fontSize = 14.sp) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        textStyle = TextStyle(fontFamily = RajdhaniFontFamily),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val recipient = selectedRecipient?.name
                        if (recipient != null && passwordInput.isNotBlank()) {
                            val weedVal = if (isCardTransfer) 0 else (creditsAmount.toIntOrNull() ?: 100)
                            val cardIdx = selectedCardToTransfer?.cardIndex ?: -1
                            val mintNum = selectedCardToTransfer?.mintNumber ?: -1
                            viewModel.sendP2pTransaction(
                                recipient,
                                weedVal,
                                cardIdx,
                                mintNum,
                                passwordInput
                            )
                            showTransferDialog = false
                        }
                    }
                ) {
                    Text("SIGN & BROADCAST", fontFamily = RajdhaniFontFamily, color = NeonCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTransferDialog = false }) {
                    Text("CANCEL", fontFamily = RajdhaniFontFamily, color = Color(0xFFB0B0C5))
                }
            },
            containerColor = CyberCharcoal,
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }

    if (showDepositDialog) {
        var selectedAsset by remember { mutableStateOf("ETH") }
        var depositAmount by remember { mutableStateOf("0.5") }

        val addresses = mapOf(
            "ETH" to "0x9E88d6C7BaF8F4f3183f233f6329686259e7ab9C",
            "TON" to "UQBgN6V569MhR18ePoPnXJpL_7XbA1f_4b9C9e7aB_90C7",
            "SOL" to "8Fx3C4WqpQePy79CbC9e7aB90C733f6329686259e7a",
            "TRX" to "TY48Xm9qYePy79CbC9e7aB90C733f6329686259e7ab9C",
            "USDC" to "0x9E88d6C7BaF8F4f3183f233f6329686259e7ab9C"
        )

        val currentAddress = addresses[selectedAsset] ?: ""

        AlertDialog(
            onDismissRequest = { showDepositDialog = false },
            title = {
                Text("BRIDGE DEPOSIT PROTOCOL", fontFamily = RajdhaniFontFamily, fontWeight = FontWeight.Bold, color = NeonCyan, fontSize = 16.sp)
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Deposit external tokenized assets via decentralized smart-contract gateways.",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 15.sp,
                        color = Color(0xFFB0B0C5)
                    )

                    Text("SELECT TARGET ASSET:", fontFamily = RajdhaniFontFamily, fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("ETH", "TON", "SOL", "TRX", "USDC").forEach { symbol ->
                            val isSel = selectedAsset == symbol
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSel) NeonCyan.copy(alpha = 0.2f) else Color(0xFF0D0D15), RoundedCornerShape(4.dp))
                                    .border(1.dp, if (isSel) NeonCyan else Color.DarkGray, RoundedCornerShape(4.dp))
                                    .clickable { selectedAsset = symbol }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(symbol, fontFamily = RajdhaniFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isSel) NeonCyan else Color(0xFFB0B0C5))
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF09090E), RoundedCornerShape(6.dp))
                            .border(1.dp, Color.DarkGray, RoundedCornerShape(6.dp))
                            .padding(10.dp)
                    ) {
                        Text("DEPOSIT ADDRESS:", fontFamily = RajdhaniFontFamily, fontSize = 17.sp, color = Color(0xFFB0B0C5))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentAddress,
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 14.sp,
                            color = Color.LightGray,
                            modifier = Modifier.clickable {
                                clipboardManager.setText(AnnotatedString(currentAddress))
                                viewModel.postNotification("📋 Address copied to clipboard!")
                            }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(100.dp)
                            .background(Color.White, RoundedCornerShape(4.dp))
                            .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cells = 10
                            val cellSize = size.width / cells
                            for (r in 0 until cells) {
                                for (c in 0 until cells) {
                                    val isFilled = (r == 0 && c < 3) || (r < 3 && c == 0) ||
                                        (r == cells - 1 && c < 3) || (r > cells - 4 && c == 0) ||
                                        (r == 0 && c > cells - 4) || (r < 3 && c == cells - 1) ||
                                        ((r + c) % 3 == 0) || (r > 3 && r < 7 && c > 3 && c < 7)
                                    if (isFilled) {
                                        drawRect(
                                            color = Color.Black,
                                            topLeft = Offset(c * cellSize, r * cellSize),
                                            size = Size(cellSize, cellSize)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = depositAmount,
                        onValueChange = { depositAmount = it },
                        label = { Text("Simulate Faucet Amount", fontFamily = RajdhaniFontFamily, fontSize = 15.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color.DarkGray
                        ),
                        textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 16.sp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                CyberTextButton(
                    onClick = {
                        val amt = depositAmount.toDoubleOrNull()
                        if (amt != null && amt > 0) {
                            viewModel.performAssetTransaction(selectedAsset, amt, "DEPOSIT")
                            showDepositDialog = false
                        } else {
                            viewModel.postNotification("❌ Please enter a valid amount!")
                        }
                    },
                    text = "MOCK NETWORK DEPOSIT",
                    skin = currentSkin,
                    containerColor = NeonGreen,
                    contentColor = CyberObsidian,
                    shape = RoundedCornerShape(12.dp)
                )
            },
            dismissButton = {
                TextButton(onClick = { showDepositDialog = false }) {
                    Text("CLOSE", fontFamily = RajdhaniFontFamily, color = Color(0xFFB0B0C5), fontSize = 15.sp)
                }
            },
            containerColor = CyberCharcoal,
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }

    if (showSendDialog) {
        var selectedAsset by remember { mutableStateOf("ETH") }
        var sendAmount by remember { mutableStateOf("0.1") }
        var recipientAddr by remember { mutableStateOf("") }
        var pinVal by remember { mutableStateOf("") }

        val maxBalance = when (selectedAsset) {
            "ETH" -> profile?.balanceEth ?: 0.5
            "TON" -> profile?.balanceTon ?: 50.0
            "SOL" -> profile?.balanceSol ?: 2.0
            "TRX" -> profile?.balanceTrx ?: 250.0
            "USDC" -> profile?.balanceUsdc ?: 100.0
            else -> 0.0
        }

        AlertDialog(
            onDismissRequest = { showSendDialog = false },
            title = {
                Text("SEND TOKENIZED ASSETS", fontFamily = RajdhaniFontFamily, fontWeight = FontWeight.Bold, color = NeonRed, fontSize = 16.sp)
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "🧪 [SANDBOX LEDGER] Simulates local ledger withdrawal only (no mainnet broadcast).",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 14.sp,
                        color = NeonGold
                    )

                    Text("SELECT ASSET TO SEND:", fontFamily = RajdhaniFontFamily, fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("ETH", "TON", "SOL", "TRX", "USDC").forEach { symbol ->
                            val isSel = selectedAsset == symbol
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSel) NeonRed.copy(alpha = 0.2f) else Color(0xFF0D0D15), RoundedCornerShape(4.dp))
                                    .border(1.dp, if (isSel) NeonRed else Color.DarkGray, RoundedCornerShape(4.dp))
                                    .clickable { selectedAsset = symbol }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(symbol, fontFamily = RajdhaniFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isSel) NeonRed else Color(0xFFB0B0C5))
                            }
                        }
                    }

                    Text("AVAILABLE BALANCE: $maxBalance $selectedAsset", fontFamily = RajdhaniFontFamily, fontSize = 14.sp, color = Color(0xFFB0B0C5))

                    OutlinedTextField(
                        value = recipientAddr,
                        onValueChange = { recipientAddr = it },
                        label = { Text("Recipient Address", fontFamily = RajdhaniFontFamily, fontSize = 15.sp) },
                        placeholder = { Text("e.g. 0x... or UQ...", fontSize = 14.sp, color = Color.DarkGray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonRed,
                            unfocusedBorderColor = Color.DarkGray
                        ),
                        textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 16.sp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = sendAmount,
                        onValueChange = { sendAmount = it },
                        label = { Text("Amount to Transfer", fontFamily = RajdhaniFontFamily, fontSize = 15.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonRed,
                            unfocusedBorderColor = Color.DarkGray
                        ),
                        textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 16.sp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    val hasPin = profile?.pinCode?.isNotBlank() == true
                    OutlinedTextField(
                        value = pinVal,
                        onValueChange = { pinVal = it },
                        label = { Text(if (hasPin) "Verify 8-Digit Security PIN" else "PIN Verification (Bypassable - PIN not set)", fontFamily = RajdhaniFontFamily, fontSize = 15.sp) },
                        singleLine = true,
                        placeholder = { Text("Enter security PIN to sign tx", fontSize = 14.sp, color = Color.DarkGray) },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonRed,
                            unfocusedBorderColor = Color.DarkGray
                        ),
                        textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 16.sp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                CyberTextButton(
                    onClick = {
                        val amt = sendAmount.toDoubleOrNull()
                        val hasPin = profile?.pinCode?.isNotBlank() == true
                        val enteredPin = pinVal

                        if (amt == null || amt <= 0) {
                            viewModel.postNotification("❌ Invalid transaction amount!")
                        } else if (amt > maxBalance) {
                            viewModel.postNotification("❌ Insufficient balance!")
                        } else if (recipientAddr.isBlank()) {
                            viewModel.postNotification("❌ Recipient address cannot be empty!")
                        } else {
                            coroutineScope.launch {
                                val pinCorrect = !hasPin || viewModel.verifyPrimaryPin(enteredPin)
                                if (!pinCorrect) {
                                    viewModel.postNotification("❌ Incorrect Security PIN signature!")
                                } else {
                                    viewModel.performAssetTransaction(selectedAsset, -amt, "SEND", recipientAddr)
                                    showSendDialog = false
                                }
                            }
                        }
                    },
                    text = "SIMULATE WITHDRAWAL",
                    skin = currentSkin,
                    containerColor = NeonRed,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp)
                )
            },
            dismissButton = {
                TextButton(onClick = { showSendDialog = false }) {
                    Text("CANCEL", fontFamily = RajdhaniFontFamily, color = Color(0xFFB0B0C5), fontSize = 15.sp)
                }
            },
            containerColor = CyberCharcoal,
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }

    if (showSwapDialog) {
        var fromAsset by remember { mutableStateOf("USDC") }
        var toAsset by remember { mutableStateOf("ETH") }
        var fromAmountInput by remember { mutableStateOf("10.0") }

        val rateMap = mapOf(
            "WEED" to 1.0,
            "USDC" to 1.0,
            "ETH" to 3250.0,
            "TON" to 7.2,
            "SOL" to 142.5,
            "TRX" to 0.12
        )

        val balanceMap = mapOf(
            "WEED" to (profile?.weed?.toDouble() ?: 1000.0),
            "ETH" to (profile?.balanceEth ?: 0.5),
            "TON" to (profile?.balanceTon ?: 50.0),
            "SOL" to (profile?.balanceSol ?: 2.0),
            "TRX" to (profile?.balanceTrx ?: 250.0),
            "USDC" to (profile?.balanceUsdc ?: 100.0)
        )

        val fromRate = rateMap[fromAsset] ?: 1.0
        val toRate = rateMap[toAsset] ?: 1.0
        val fromBalance = balanceMap[fromAsset] ?: 0.0

        val fromAmountDouble = fromAmountInput.toDoubleOrNull() ?: 0.0
        val toAmountCalculated = if (fromRate > 0 && toRate > 0) {
            (fromAmountDouble * fromRate) / toRate
        } else {
            0.0
        }

        AlertDialog(
            onDismissRequest = { showSwapDialog = false },
            title = {
                Text("DEX BRIDGE SWAP PROTOCOL", fontFamily = RajdhaniFontFamily, fontWeight = FontWeight.Bold, color = NeonCyan, fontSize = 16.sp)
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Swap assets in peer-to-peer liquidity pools instantly. Automated market maker quotes apply.",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 15.sp,
                        color = Color(0xFFB0B0C5)
                    )

                    Text("FROM ASSET:", fontFamily = RajdhaniFontFamily, fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("WEED", "USDC", "ETH", "TON", "SOL", "TRX").forEach { symbol ->
                            val isSel = fromAsset == symbol
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSel) NeonCyan.copy(alpha = 0.2f) else Color(0xFF0D0D15), RoundedCornerShape(4.dp))
                                    .border(1.dp, if (isSel) NeonCyan else Color.DarkGray, RoundedCornerShape(4.dp))
                                    .clickable {
                                        fromAsset = symbol
                                        if (symbol != "WEED") {
                                            toAsset = "WEED"
                                        } else if (toAsset == "WEED") {
                                            toAsset = "USDC"
                                        }
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (symbol == "WEED") "WED" else symbol,
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) NeonCyan else Color(0xFFB0B0C5)
                                )
                            }
                        }
                    }

                    Text("AVAILABLE BALANCE: ${String.format("%.4f", fromBalance)} ${if (fromAsset == "WEED") "WED" else fromAsset}", fontFamily = RajdhaniFontFamily, fontSize = 14.sp, color = Color(0xFFB0B0C5))

                    OutlinedTextField(
                        value = fromAmountInput,
                        onValueChange = { fromAmountInput = it },
                        label = { Text("Amount to Swap", fontFamily = RajdhaniFontFamily, fontSize = 15.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color.DarkGray
                        ),
                        textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 16.sp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text("TO ASSET:", fontFamily = RajdhaniFontFamily, fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("WEED", "USDC", "ETH", "TON", "SOL", "TRX").forEach { symbol ->
                            val isSel = toAsset == symbol
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSel) NeonMagenta.copy(alpha = 0.2f) else Color(0xFF0D0D15), RoundedCornerShape(4.dp))
                                    .border(1.dp, if (isSel) NeonMagenta else Color.DarkGray, RoundedCornerShape(4.dp))
                                    .clickable {
                                        toAsset = symbol
                                        if (symbol != "WEED") {
                                            fromAsset = "WEED"
                                        } else if (fromAsset == "WEED") {
                                            fromAsset = "USDC"
                                        }
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (symbol == "WEED") "WED" else symbol,
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) NeonMagenta else Color(0xFFB0B0C5)
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF09090E), RoundedCornerShape(6.dp))
                            .border(1.dp, NeonMagenta.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(10.dp)
                    ) {
                        Text("ESTIMATED CONVERSION:", fontFamily = RajdhaniFontFamily, fontSize = 17.sp, color = Color(0xFFB0B0C5))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${String.format("%.6f", toAmountCalculated)} ${if (toAsset == "WEED") "WED" else toAsset}",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonMagenta
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val directRate = fromRate / toRate
                        Text(
                            text = "Rate: 1 ${if (fromAsset == "WEED") "WED" else fromAsset} = ${String.format("%.6f", directRate)} ${if (toAsset == "WEED") "WED" else toAsset}",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 17.sp,
                            color = Color.LightGray
                        )
                    }
                }
            },
            confirmButton = {
                CyberTextButton(
                    onClick = {
                        val amt = fromAmountInput.toDoubleOrNull()
                        if (amt == null || amt <= 0) {
                            viewModel.postNotification("❌ Invalid swap amount!")
                        } else if (amt > fromBalance) {
                            viewModel.postNotification("❌ Insufficient balance to swap!")
                        } else {
                            viewModel.swapAssets(fromAsset, toAsset, amt, toAmountCalculated)
                            showSwapDialog = false
                        }
                    },
                    text = "EXECUTE SWAP",
                    skin = currentSkin,
                    containerColor = NeonCyan,
                    contentColor = CyberObsidian,
                    shape = RoundedCornerShape(12.dp)
                )
            },
            dismissButton = {
                TextButton(onClick = { showSwapDialog = false }) {
                    Text("CANCEL", fontFamily = RajdhaniFontFamily, color = Color(0xFFB0B0C5), fontSize = 15.sp)
                }
            },
            containerColor = CyberCharcoal,
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }
}

@Composable
fun OnChainNftPanel(viewModel: TarotViewModel) {
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val rpcUrl by viewModel.smartContractRpcUrl.collectAsStateWithLifecycle()
    val contractAddress by viewModel.smartContractAddress.collectAsStateWithLifecycle()
    val onChainMints by viewModel.onChainMints.collectAsStateWithLifecycle()
    val gasPrice by viewModel.gasPriceGwei.collectAsStateWithLifecycle()
    val contractDetails by viewModel.contractDetails.collectAsStateWithLifecycle()
    val status by viewModel.smartContractStatus.collectAsStateWithLifecycle()
    val logs by viewModel.smartContractLog.collectAsStateWithLifecycle()

    var editingRpc by remember { mutableStateOf(rpcUrl) }
    var editingAddress by remember { mutableStateOf(contractAddress) }

    val statusColor = when (status) {
        "SUCCESS" -> NeonGreen
        "MINTING" -> NeonGold
        "FAILED" -> Color.Red
        else -> Color.Gray
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                border = BorderStroke(1.dp, NeonMagenta),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🧪 READ-ONLY RPC / SANDBOX NFT INTERFACE",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonMagenta
                    )
                    Text(
                        text = "RPC queries live chain state (gas/network). Live minting requires external signer; leave RPC blank for local Sandbox receipts.",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 13.sp,
                        color = Color(0xFFB0B0C5),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = editingRpc,
                        onValueChange = { editingRpc = it },
                        label = { Text("RPC Endpoint URL", fontSize = 15.sp, fontFamily = RajdhaniFontFamily) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonMagenta,
                            unfocusedBorderColor = Color.DarkGray
                        ),
                        textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 16.sp),
                        modifier = Modifier.fillMaxWidth().testTag("contract_rpc_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editingAddress,
                        onValueChange = { editingAddress = it },
                        label = { Text("Smart Contract Address", fontSize = 15.sp, fontFamily = RajdhaniFontFamily) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonMagenta,
                            unfocusedBorderColor = Color.DarkGray
                        ),
                        textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 16.sp),
                        modifier = Modifier.fillMaxWidth().testTag("contract_address_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "GAS: ${String.format("%.2f", gasPrice)} Gwei",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 15.sp,
                                color = NeonCyan
                            )
                            contractDetails?.let {
                                Text(
                                    text = "NET: ${it.networkName}",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 15.sp,
                                    color = Color(0xFFB0B0C5)
                                )
                            }
                        }

                        CyberTextButton(
                            onClick = { viewModel.updateRpcAndContractAddress(editingRpc, editingAddress) },
                            text = "CONNECT / UPDATE",
                            skin = currentSkin,
                            containerColor = NeonMagenta,
                            contentColor = Color.White,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("save_contract_config_btn")
                        )
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberObsidian),
                border = BorderStroke(1.dp, Color.DarkGray),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ON-CHAIN KERNEL LOGS",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            color = Color.LightGray
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = status,
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 15.sp,
                                color = statusColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(Color.Black, RoundedCornerShape(4.dp))
                            .border(1.dp, Color(0xFF1F1F2E), RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    ) {
                        LazyColumn(
                            reverseLayout = true,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(logs.reversed()) { log ->
                                Text(
                                    text = "> $log",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 14.sp,
                                    color = if (log.contains("ERROR")) Color.Red else if (log.contains("CONFIRMED")) NeonGreen else Color.LightGray,
                                    modifier = Modifier.padding(vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "MINTER DEPLOYMENTS (${onChainMints.size} CONTRACT TOKENS)",
                fontFamily = RajdhaniFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        if (onChainMints.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NO DEPLOYED NFTs YET.\nGo to SECURE DECK VAULT, select a card, and mint to chain.",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 15.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(onChainMints) { nft ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                    border = BorderStroke(1.dp, Color(0xFF1F1F2E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "TOKEN ID: #${nft.tokenId}",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonMagenta
                                )
                                Text(
                                    text = "TX HASH: ${nft.txHash.take(16)}...",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 14.sp,
                                    color = Color(0xFFB0B0C5)
                                )
                            }

                            if (nft.explorerUrl.isNotBlank()) {
                                val localUri = android.net.Uri.parse(nft.explorerUrl)
                                val context = LocalContext.current
                                CyberTextButton(
                                    onClick = {
                                        try {
                                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, localUri)
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            // Silent catch if no browser installed
                                        }
                                    },
                                    text = "EXPLORER",
                                    skin = currentSkin,
                                    containerColor = Color.Transparent,
                                    contentColor = NeonCyan,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(28.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ======================== WEED PROTOCOL PANEL ========================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeedProtocolPanel(viewModel: TarotViewModel) {
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                border = BorderStroke(1.dp, currentSkin.primaryColor.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "WEED HARVEST CORE",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = currentSkin.primaryColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Current Streak Balance",
                                fontSize = 15.sp,
                                fontFamily = RajdhaniFontFamily,
                                color = Color(0xFFB0B0C5)
                            )
                            Text(
                                text = "${profile?.weedStreak ?: 0} Consecutive Claims",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = RajdhaniFontFamily,
                                color = Color.White
                            )
                        }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Black),
                            border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.3f)),
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text(
                                text = "Multiplier: +${profile?.weedStreak?.plus(1) ?: 1}x WEED",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = RajdhaniFontFamily,
                                color = NeonGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black, RoundedCornerShape(4.dp))
                            .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(if (isClaimReady) NeonGreen else NeonGold, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isClaimReady) "CORE HARVESTER ARMED" else "COOLANT FLUSH IN PROGRESS",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isClaimReady) NeonGreen else NeonGold
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
                                "%02d:%02d:%02d".format(hours, minutes, seconds)
                            },
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isClaimReady) NeonGreen else Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    CyberButton(
                        onClick = { viewModel.claimActivityWeed() },
                        skin = currentSkin,
                        enabled = isClaimReady,
                        containerColor = NeonGreen,
                        contentColor = CyberObsidian,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        AutoResizingText(
                            text = "EXTRACT ACTIVITY WEED",
                            targetTextSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = RajdhaniFontFamily,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Claim once every 6 hours. Starts at 1 WEED, increments by +1 each claim. Miss the 6h window after readiness and streak resets to 1!",
                        fontSize = 17.sp,
                        fontFamily = RajdhaniFontFamily,
                        color = Color(0xFFB0B0C5),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                border = BorderStroke(1.dp, currentSkin.secondaryColor.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AI TREASURER AMM SWAP & TICKERS",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = currentSkin.primaryColor
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "AGGREGATED AVERAGES (TOP 10 EXCHANGES)",
                        fontSize = 14.sp,
                        fontFamily = RajdhaniFontFamily,
                        color = Color(0xFFB0B0C5)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("BTC", "ETH", "SOL", "WEED").forEach { coin ->
                            val isSelectedCoin = selectedTickerCoin == coin
                            val rate = averageRates[coin] ?: 0.0
                            val displayPrice = if (rate >= 1.0) "$${String.format("%.2f", rate)}" else "$${String.format("%.4f", rate)}"

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedTickerCoin = coin
                                        showTickersDetail = true
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelectedCoin) Color.Black else Color(0xFF0A0A10)
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelectedCoin) currentSkin.primaryColor else Color.DarkGray
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = coin,
                                        fontSize = 17.sp,
                                        fontFamily = RajdhaniFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelectedCoin) currentSkin.primaryColor else Color.White
                                    )
                                    Text(
                                        text = displayPrice,
                                        fontSize = 17.sp,
                                        fontFamily = RajdhaniFontFamily,
                                        color = Color.LightGray
                                    )
                                }
                            }
                        }
                    }

                    if (showTickersDetail) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Black),
                            border = BorderStroke(1.dp, Color.DarkGray)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "$selectedTickerCoin LIVE ARBITRAGE ARRAYS",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = RajdhaniFontFamily,
                                        color = currentSkin.primaryColor
                                    )
                                    Text(
                                        text = "[X] CLOSE",
                                        fontSize = 17.sp,
                                        fontFamily = RajdhaniFontFamily,
                                        color = Color.Red,
                                        modifier = Modifier.clickable { showTickersDetail = false }
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))

                                val exchanges = listOf(
                                    "Binance", "Coinbase", "Kraken", "OKX", "Bybit",
                                    "Huobi", "Gate.io", "Bitfinex", "KuCoin", "Gemini"
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    exchanges.forEach { ex ->
                                        val price = exchangeTickers[ex]?.get(selectedTickerCoin) ?: averageRates[selectedTickerCoin] ?: 0.0
                                        val displayPrice = if (price >= 1.0) "$${String.format("%.3f", price)}" else "$${String.format("%.5f", price)}"
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "• $ex",
                                                fontSize = 17.sp,
                                                fontFamily = RajdhaniFontFamily,
                                                color = Color(0xFFB0B0C5)
                                            )
                                            Text(
                                                text = displayPrice,
                                                fontSize = 17.sp,
                                                fontFamily = RajdhaniFontFamily,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "ROUTER SWAP UTILITY (100% UNISWAP ROUTER SIM)",
                        fontSize = 14.sp,
                        fontFamily = RajdhaniFontFamily,
                        color = Color(0xFFB0B0C5),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SELL ASSET",
                                fontSize = 17.sp,
                                fontFamily = RajdhaniFontFamily,
                                color = Color(0xFFB0B0C5)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black, RoundedCornerShape(4.dp))
                                    .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
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
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = swapFromAsset,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = RajdhaniFontFamily,
                                    color = Color.White
                                )
                                Text(text = "▼", fontSize = 17.sp, color = Color(0xFFB0B0C5))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = swapAmountInput,
                                onValueChange = { swapAmountInput = it },
                                singleLine = true,
                                textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 16.sp, color = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Text(
                            text = "➔",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB0B0C5)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "RECEIVE ASSET",
                                fontSize = 17.sp,
                                fontFamily = RajdhaniFontFamily,
                                color = Color(0xFFB0B0C5)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black, RoundedCornerShape(4.dp))
                                    .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
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
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = swapToAsset,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = RajdhaniFontFamily,
                                    color = Color.White
                                )
                                Text(text = "▼", fontSize = 17.sp, color = Color(0xFFB0B0C5))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .background(Color(0xFF0F0F16), RoundedCornerShape(4.dp))
                                    .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                val outPrice = if (calculatedReceive >= 1.0) String.format("%.2f", calculatedReceive) else String.format("%.4f", calculatedReceive)
                                Text(
                                    text = outPrice,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = RajdhaniFontFamily,
                                    color = NeonGreen
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    CyberButton(
                        onClick = {
                            val amt = swapAmountInput.toDoubleOrNull() ?: 0.0
                            viewModel.swapAssets(swapFromAsset, swapToAsset, amt, calculatedReceive)
                        },
                        skin = currentSkin,
                        containerColor = currentSkin.secondaryColor,
                        contentColor = CyberObsidian,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        AutoResizingText(
                            text = "EXECUTE AUTOMATED AMM ROUTER",
                            targetTextSize = 12.sp,
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
                        text = "AI TREASURER INTERNALS LEDGER",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(Color.Black, RoundedCornerShape(4.dp))
                            .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(treasurerLogs) { log ->
                                val color = when {
                                    log.contains("[MINT]") -> NeonGreen
                                    log.contains("[BURN]") -> NeonMagenta
                                    log.contains("[SWAP]") -> NeonCyan
                                    else -> Color.Gray
                                }
                                Text(
                                    text = log,
                                    fontSize = 17.sp,
                                    fontFamily = RajdhaniFontFamily,
                                    color = color,
                                    lineHeight = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ======================== NEURAL ADS PANEL ========================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeuralAdsPanel(viewModel: TarotViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val enabledThemes by viewModel.enabledAdThemes.collectAsStateWithLifecycle()
    val banners by viewModel.adBanners.collectAsStateWithLifecycle()
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()

    var adPromptInput by remember { mutableStateOf("") }
    var selectedAdTheme by remember { mutableStateOf("Cybernetics") }
    var selectedColorPreset by remember { mutableStateOf("#FF00FF") }

    val isAdFreeSubscribed = profile?.isAdFreeSubscribed == true && (profile?.adFreeExpiryTime ?: 0L) > System.currentTimeMillis()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                border = BorderStroke(1.dp, if (isAdFreeSubscribed) NeonGreen else Color.DarkGray),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AD-SHIELD DEFLATIONARY PROTOCOL",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAdFreeSubscribed) NeonGreen else Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Status Indicator",
                                fontSize = 14.sp,
                                fontFamily = RajdhaniFontFamily,
                                color = Color(0xFFB0B0C5)
                            )
                            Text(
                                text = if (isAdFreeSubscribed) "🛡️ ACTIVE PROTECTION LAYER" else "⚠️ UNPROTECTED TUNNELS",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = RajdhaniFontFamily,
                                color = if (isAdFreeSubscribed) NeonGreen else NeonGold
                            )
                        }

                        if (isAdFreeSubscribed) {
                            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                            val expDate = sdf.format(Date(profile?.adFreeExpiryTime ?: 0L))
                            Text(
                                text = "Expires: $expDate",
                                fontSize = 17.sp,
                                fontFamily = RajdhaniFontFamily,
                                color = Color(0xFFB0B0C5)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    CyberButton(
                        onClick = { viewModel.subscribeAdFree {} },
                        skin = currentSkin,
                        containerColor = ElegantIndigo,
                        contentColor = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        AutoResizingText(
                            text = "BUY 30-DAY AD-SHIELD (500 WEED)",
                            targetTextSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = RajdhaniFontFamily,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ad-Shield filters all incoming sponsored broadcast cards instantly. 100% of WEED spent is BURNED by the AI Treasurer.",
                        fontSize = 17.sp,
                        fontFamily = RajdhaniFontFamily,
                        color = Color(0xFFB0B0C5),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                border = BorderStroke(1.dp, currentSkin.primaryColor.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "REGISTER BROADCAST INTERNALS SLOT",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = currentSkin.primaryColor
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "AD PROMPT (What are you advertising? AI writes it!)",
                        fontSize = 14.sp,
                        fontFamily = RajdhaniFontFamily,
                        color = Color(0xFFB0B0C5)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = adPromptInput,
                        onValueChange = { adPromptInput = it },
                        placeholder = { Text("e.g., Sell custom cyberdecks with preloaded software tunnels.", fontSize = 16.sp, color = Color.DarkGray) },
                        textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 16.sp, color = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "BROADCAST TOPIC/THEME",
                        fontSize = 14.sp,
                        fontFamily = RajdhaniFontFamily,
                        color = Color(0xFFB0B0C5)
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    val themes = listOf("Cybernetics", "Tarot", "Biotech", "Darknet", "AI Overlords", "Quantum Finance")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(themes) { th ->
                            val isSel = selectedAdTheme == th
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isSel) currentSkin.primaryColor.copy(alpha = 0.2f) else Color.Transparent,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .border(1.dp, if (isSel) currentSkin.primaryColor else Color.DarkGray, RoundedCornerShape(4.dp))
                                    .clickable { selectedAdTheme = th }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = th.uppercase(),
                                    fontSize = 17.sp,
                                    fontFamily = RajdhaniFontFamily,
                                    color = if (isSel) currentSkin.primaryColor else Color(0xFFB0B0C5)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "GLOW ACCENT COLOR PRESET",
                        fontSize = 14.sp,
                        fontFamily = RajdhaniFontFamily,
                        color = Color(0xFFB0B0C5)
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    val colors = listOf(
                        "#FF00FF" to "Magenta",
                        "#00FF00" to "Green",
                        "#00FFFF" to "Cyan",
                        "#FFD700" to "Gold",
                        "#FF4500" to "Red"
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        colors.forEach { (hex, _) ->
                            val isSel = selectedColorPreset == hex
                            val col = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { currentSkin.primaryColor }

                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(col, CircleShape)
                                    .border(
                                        width = 2.dp,
                                        color = if (isSel) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColorPreset = hex }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    CyberButton(
                        onClick = {
                            viewModel.purchaseAdBanner(adPromptInput, selectedAdTheme, selectedColorPreset) { success ->
                                if (success) adPromptInput = ""
                            }
                        },
                        skin = currentSkin,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        AutoResizingText(
                            text = "COMPILE & BROADCAST SLOT (100 WEED)",
                            targetTextSize = 11.sp,
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
                        text = "QUEUE SEGMENT FILTERS (MINIMUM 3 ACTIVE)",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "To balance decentralized broad-cast channels, you must keep at least 3 active sectors enabled at all times.",
                        fontSize = 17.sp,
                        fontFamily = RajdhaniFontFamily,
                        color = Color(0xFFB0B0C5)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val themes = listOf("Cybernetics", "Tarot", "Biotech", "Darknet", "AI Overlords", "Quantum Finance")
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        themes.forEach { th ->
                            val isEnabled = enabledThemes.contains(th)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black, RoundedCornerShape(4.dp))
                                    .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                                    .clickable { viewModel.toggleAdTheme(th) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = th.uppercase(),
                                    fontSize = 14.sp,
                                    fontFamily = RajdhaniFontFamily,
                                    color = if (isEnabled) Color.White else Color(0xFFB0B0C5)
                                )
                                Text(
                                    text = if (isEnabled) "ACTIVE (TUNNEL ON)" else "SHUTDOWN",
                                    fontSize = 17.sp,
                                    fontFamily = RajdhaniFontFamily,
                                    color = if (isEnabled) NeonGreen else Color.Red,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
