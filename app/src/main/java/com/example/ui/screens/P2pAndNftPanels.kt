package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CyberTarotDeck
import com.example.data.MintedCard
import com.example.data.PeerContact
import com.example.ui.components.CyberActionButton
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
import kotlinx.coroutines.launch
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
    val c = Cyber.colors
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
        verticalArrangement = Arrangement.spacedBy(Space.lg)
    ) {
        item {
            CyberPanel(
                borderColor = c.accent.copy(alpha = 0.65f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SANDBOX P2P LEDGER (LOCAL ECDSA)",
                            style = MaterialTheme.typography.labelLarge,
                            color = c.goldLeaf
                        )
                        Text(
                            text = "${profile?.username ?: "Netrunner"}_LOCAL_LEDGER",
                            style = MaterialTheme.typography.titleMedium,
                            color = c.textHigh
                        )
                    }

                    CyberTextButton(
                        onClick = { showTransferDialog = true },
                        text = "BROADCAST TX",
                        skin = currentSkin,
                        containerColor = c.accent,
                        contentColor = c.ink,
                        shape = CyberShapes.control
                    )
                }

                Spacer(modifier = Modifier.height(Space.md))
                HorizontalDivider(color = c.goldAntique.copy(alpha = 0.25f))
                Spacer(modifier = Modifier.height(Space.md))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Space.md)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "WEED BALANCE",
                            style = MaterialTheme.typography.labelMedium,
                            color = c.textMid
                        )
                        Text(
                            text = "${profile?.weed ?: 0} WEED",
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = IbmPlexMonoFontFamily,
                            color = c.accent
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TAROT MODULES OWNED",
                            style = MaterialTheme.typography.labelMedium,
                            color = c.textMid
                        )
                        Text(
                            text = "${mintedCards.size} NODES",
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = IbmPlexMonoFontFamily,
                            color = c.goldLeaf
                        )
                    }
                }
            }
        }

        item {
            CyberPanel(
                borderColor = NeonMagenta.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                CyberSectionHeader(
                    title = "DECENTRALIZED TOKENIZED ACTIVES",
                    subtitle = "Multi-chain asset balances integrated via secure bridge protocol",
                    color = c.goldLeaf
                )
                Spacer(modifier = Modifier.height(Space.md))

                val activeTokens = listOf(
                    TokenAsset("ETH", "Ethereum Core", profile?.balanceEth ?: 0.5, "0x9E88d6C7BaF8F4f3183f233f6329686259e7ab9C", 3250.0, NeonCyan, Icons.Default.CurrencyExchange),
                    TokenAsset("TON", "The Open Network (Gram)", profile?.balanceTon ?: 50.0, "UQBgN6V569MhR18ePoPnXJpL_7XbA1f_4b9C9e7aB_90C7", 7.20, NeonCyan, Icons.Default.AirlineStops),
                    TokenAsset("SOL", "Solana Network", profile?.balanceSol ?: 2.0, "8Fx3C4WqpQePy79CbC9e7aB90C733f6329686259e7a", 142.50, NeonMagenta, Icons.Default.Bolt),
                    TokenAsset("TRX", "TRON Protocol", profile?.balanceTrx ?: 250.0, "TY48Xm9qYePy79CbC9e7aB90C733f6329686259e7ab9C", 0.12, c.danger, Icons.Default.Timeline),
                    TokenAsset("USDC", "Circle USD Coin", profile?.balanceUsdc ?: 100.0, "0x9E88d6C7BaF8F4f3183f233f6329686259e7ab9C", 1.00, c.textHigh, Icons.Default.RadioButtonChecked)
                )

                activeTokens.forEach { token ->
                    Surface(
                        color = c.panelRaised,
                        shape = CyberShapes.chip,
                        border = BorderStroke(1.dp, c.goldAntique.copy(alpha = 0.2f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Space.xs)
                    ) {
                        Row(
                            modifier = Modifier.padding(Space.md),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(token.color.copy(alpha = 0.12f), CircleShape)
                                        .border(1.dp, token.color.copy(alpha = 0.35f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = token.icon,
                                        contentDescription = token.symbol,
                                        tint = token.color,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(Space.md))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = token.symbol,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = c.textHigh
                                        )
                                        if (token.symbol == "TON") {
                                            Spacer(modifier = Modifier.width(Space.xs))
                                            Text(
                                                text = "GRAM",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = NeonAmber,
                                                modifier = Modifier
                                                    .background(NeonAmber.copy(alpha = 0.15f), CyberShapes.chip)
                                                    .padding(horizontal = Space.xs, vertical = Space.xxs)
                                            )
                                        }
                                    }
                                    Text(
                                        text = token.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = c.textMid
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${String.format(Locale.US, "%.4f", token.balance)} ${token.symbol}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontFamily = IbmPlexMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = token.color
                                )
                                Text(
                                    text = "$${String.format(Locale.US, "%.2f", token.balance * token.priceUsd)} USD",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = c.textMid
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Space.md))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Space.sm)
                ) {
                    CyberTextButton(
                        onClick = { showDepositDialog = true },
                        text = "DEPOSIT",
                        skin = currentSkin,
                        containerColor = c.emeraldGlow.copy(alpha = 0.14f),
                        contentColor = c.emeraldGlow,
                        shape = CyberShapes.control,
                        modifier = Modifier.weight(1f)
                    )

                    CyberTextButton(
                        onClick = { showSendDialog = true },
                        text = "SEND",
                        skin = currentSkin,
                        containerColor = c.danger.copy(alpha = 0.14f),
                        contentColor = c.danger,
                        shape = CyberShapes.control,
                        modifier = Modifier.weight(1f)
                    )

                    CyberTextButton(
                        onClick = { showSwapDialog = true },
                        text = "SWAP / DEX",
                        skin = currentSkin,
                        containerColor = c.accent.copy(alpha = 0.14f),
                        contentColor = c.accent,
                        shape = CyberShapes.control,
                        modifier = Modifier.weight(1.2f)
                    )
                }
            }
        }

        item {
            CyberSectionHeader(
                title = "BLOCKCHAIN LEDGER EXPLORER",
                color = c.textHigh
            )
        }

        if (transactions.isEmpty()) {
            item {
                Surface(
                    color = c.ink,
                    shape = CyberShapes.control,
                    border = BorderStroke(1.dp, c.goldAntique.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "AWAITING GENESIS BLOCK PROTOCOLS...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = IbmPlexMonoFontFamily,
                            color = c.textMid
                        )
                    }
                }
            }
        } else {
            items(transactions) { tx ->
                val payloadText = if (tx.cardIndex != -1) {
                    val card = CyberTarotDeck.getCard(tx.cardIndex)
                    "Transferred Card MODULE #${String.format(Locale.US, "%02d", tx.cardIndex)} (${card.name})"
                } else {
                    "Sent Weed allocation: ${tx.amountWeed} WEED"
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = c.panel),
                    border = BorderStroke(1.dp, c.goldAntique.copy(alpha = 0.25f)),
                    shape = CyberShapes.cardCompact,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(Space.md)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "BLOCK #${String.format(Locale.US, "%04d", tx.blockNumber)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontFamily = IbmPlexMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = c.accent
                            )
                            Text(
                                text = "NONCE: ${tx.nonce}",
                                style = MaterialTheme.typography.labelSmall,
                                color = c.textMid
                            )
                        }
                        Spacer(modifier = Modifier.height(Space.xs))
                        Text(
                            text = "HASH: ${tx.blockHash}",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonMagenta
                        )
                        Text(
                            text = "PREV: ${tx.previousHash}",
                            style = MaterialTheme.typography.labelSmall,
                            color = c.textMid
                        )
                        Spacer(modifier = Modifier.height(Space.sm))
                        HorizontalDivider(color = c.goldAntique.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(Space.sm))

                        Text(
                            text = "FROM: ${tx.senderAddress}",
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textHigh
                        )
                        Text(
                            text = "TO: ${tx.recipientAddress}",
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textHigh
                        )
                        Text(
                            text = "DATA: $payloadText",
                            style = MaterialTheme.typography.bodySmall,
                            color = c.goldLeaf,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(Space.xs))
                        Text(
                            text = "SIGNATURE: ${tx.signature}",
                            style = MaterialTheme.typography.labelSmall,
                            color = c.textMid
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
                    style = MaterialTheme.typography.titleMedium,
                    color = c.accent
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Executing cryptographic Proof-of-Work hashes sequentially to match difficulty targets.",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textMid
                    )
                    Spacer(modifier = Modifier.height(Space.lg))
                    LinearProgressIndicator(
                        progress = { currentProgress },
                        color = c.accent,
                        trackColor = c.panelRaised,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(Space.sm))
                    Text(
                        text = "NONCE SCANS: ${(currentProgress * 100000).toInt()}/100000",
                        style = MaterialTheme.typography.labelSmall,
                        color = c.textMid
                    )
                }
            },
            confirmButton = {},
            dismissButton = {},
            containerColor = c.ink,
            titleContentColor = c.textHigh,
            textContentColor = c.textMid
        )
    }

    if (p2pStatus == "SUCCESS") {
        AlertDialog(
            onDismissRequest = { viewModel.resetP2pStatus() },
            title = {
                Text(
                    text = "[SANDBOX] LEDGER BLOCK SIGNED",
                    style = MaterialTheme.typography.titleMedium,
                    color = c.emeraldGlow
                )
            },
            text = {
                Text(
                    text = "Local ECDSA P-256 signature & proof-of-work hash recorded in your Sandbox ledger. No real WEED or Vault cards were burned.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textMid
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.resetP2pStatus() }) {
                    Text("OK", style = MaterialTheme.typography.labelLarge, color = c.emeraldGlow)
                }
            },
            containerColor = c.panel
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
                    text = "SANDBOX P2P TRANSFER BLOCK",
                    style = MaterialTheme.typography.titleMedium,
                    color = c.accent
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Space.md)) {
                    Column {
                        Text(
                            text = "RECIPIENT PEER NODE",
                            style = MaterialTheme.typography.labelMedium,
                            color = c.textMid
                        )
                        Spacer(modifier = Modifier.height(Space.xs))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .background(c.ink, CyberShapes.chip)
                                .border(1.dp, c.goldAntique.copy(alpha = 0.3f), CyberShapes.chip)
                                .clickable { dropdownExpanded = true }
                                .padding(Space.md),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = selectedRecipient?.name ?: "SELECT PEER NODE...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (selectedRecipient != null) c.textHigh else c.textMid
                            )
                        }

                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier.background(c.panel)
                        ) {
                            contacts.forEach { contact ->
                                DropdownMenuItem(
                                    text = { Text(contact.name, style = MaterialTheme.typography.bodyMedium, color = c.textHigh) },
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
                            style = MaterialTheme.typography.labelMedium,
                            color = c.textMid
                        )
                        Spacer(modifier = Modifier.height(Space.xs))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(c.ink, CyberShapes.chip)
                                .border(1.dp, c.goldAntique.copy(alpha = 0.25f), CyberShapes.chip)
                                .padding(Space.xs)
                        ) {
                            listOf(false to "WEED", true to "TAROT NFT MODULE").forEach { (isCard, label) ->
                                val isSel = isCardTransfer == isCard
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp)
                                        .background(
                                            if (isSel) c.accent.copy(alpha = 0.2f) else Color.Transparent,
                                            CyberShapes.chip
                                        )
                                        .clickable { isCardTransfer = isCard },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isSel) c.accent else c.textMid
                                    )
                                }
                            }
                        }
                    }

                    if (isCardTransfer) {
                        Column {
                            Text(
                                text = "SELECT TAROT MODULE",
                                style = MaterialTheme.typography.labelMedium,
                                color = c.textMid
                            )
                            Spacer(modifier = Modifier.height(Space.xs))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .background(c.ink, CyberShapes.chip)
                                    .border(1.dp, c.goldAntique.copy(alpha = 0.3f), CyberShapes.chip)
                                    .clickable { cardDropdownExpanded = true }
                                    .padding(Space.md),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                val cardName = selectedCardToTransfer?.let {
                                    val card = CyberTarotDeck.getCard(it.cardIndex)
                                    "NODE #${String.format(Locale.US, "%02d", it.cardIndex)}: ${card.name} (#${String.format(Locale.US, "%04d", it.mintNumber)})"
                                } ?: "SELECT OWNED NFT..."
                                Text(
                                    text = cardName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (selectedCardToTransfer != null) c.textHigh else c.textMid
                                )
                            }

                            DropdownMenu(
                                expanded = cardDropdownExpanded,
                                onDismissRequest = { cardDropdownExpanded = false },
                                modifier = Modifier.background(c.panel)
                            ) {
                                mintedCards.forEach { card ->
                                    val info = CyberTarotDeck.getCard(card.cardIndex)
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "NODE #${String.format(Locale.US, "%02d", card.cardIndex)}: ${info.name} (#${String.format(Locale.US, "%04d", card.mintNumber)})",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = c.textHigh
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
                            label = { Text("Amount Weed") },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("BIP39 Seed Phrase (Signature)") },
                        placeholder = { Text("Enter private keys/seed to authorize", style = MaterialTheme.typography.bodySmall) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        textStyle = MaterialTheme.typography.bodyLarge,
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
                    Text("SIGN & BROADCAST", style = MaterialTheme.typography.labelLarge, color = c.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTransferDialog = false }) {
                    Text("CANCEL", style = MaterialTheme.typography.labelLarge, color = c.textMid)
                }
            },
            containerColor = c.panel,
            titleContentColor = c.textHigh,
            textContentColor = c.textMid
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
                Text("BRIDGE DEPOSIT PROTOCOL", style = MaterialTheme.typography.titleMedium, color = c.accent)
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Space.md),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Deposit external tokenized assets via decentralized smart-contract gateways.",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textMid
                    )

                    Text("SELECT TARGET ASSET:", style = MaterialTheme.typography.labelLarge, color = c.textHigh)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Space.xs)
                    ) {
                        listOf("ETH", "TON", "SOL", "TRX", "USDC").forEach { symbol ->
                            val isSel = selectedAsset == symbol
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .background(if (isSel) c.accent.copy(alpha = 0.2f) else c.panelRaised, CyberShapes.chip)
                                    .border(1.dp, if (isSel) c.accent else c.goldAntique.copy(alpha = 0.3f), CyberShapes.chip)
                                    .clickable { selectedAsset = symbol },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(symbol, style = MaterialTheme.typography.labelMedium, color = if (isSel) c.accent else c.textMid)
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(c.ink, CyberShapes.chip)
                            .border(1.dp, c.goldAntique.copy(alpha = 0.3f), CyberShapes.chip)
                            .padding(Space.md)
                    ) {
                        Text("DEPOSIT ADDRESS:", style = MaterialTheme.typography.labelMedium, color = c.textMid)
                        Spacer(modifier = Modifier.height(Space.xxs))
                        Text(
                            text = currentAddress,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = IbmPlexMonoFontFamily,
                            color = c.textHigh,
                            modifier = Modifier.clickable {
                                clipboardManager.setText(AnnotatedString(currentAddress))
                                viewModel.postNotification("Address copied to clipboard!")
                            }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(100.dp)
                            .background(Color.White, CyberShapes.chip)
                            .border(1.dp, c.goldAntique.copy(alpha = 0.4f), CyberShapes.chip)
                            .padding(Space.sm),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cells = 10
                            val cellSize = size.width / cells
                            for (r in 0 until cells) {
                                for (cIdx in 0 until cells) {
                                    val isFilled = (r == 0 && cIdx < 3) || (r < 3 && cIdx == 0) ||
                                        (r == cells - 1 && cIdx < 3) || (r > cells - 4 && cIdx == 0) ||
                                        (r == 0 && cIdx > cells - 4) || (r < 3 && cIdx == cells - 1) ||
                                        ((r + cIdx) % 3 == 0) || (r > 3 && r < 7 && cIdx > 3 && cIdx < 7)
                                    if (isFilled) {
                                        drawRect(
                                            color = Color.Black,
                                            topLeft = Offset(cIdx * cellSize, r * cellSize),
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
                        label = { Text("Simulate Faucet Amount") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = c.textHigh,
                            unfocusedTextColor = c.textHigh,
                            focusedBorderColor = c.accent,
                            unfocusedBorderColor = c.goldAntique.copy(alpha = 0.35f)
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge,
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
                            viewModel.postNotification("Please enter a valid amount!")
                        }
                    },
                    text = "MOCK NETWORK DEPOSIT",
                    skin = currentSkin,
                    containerColor = c.emeraldGlow,
                    contentColor = c.ink,
                    shape = CyberShapes.control
                )
            },
            dismissButton = {
                TextButton(onClick = { showDepositDialog = false }) {
                    Text("CLOSE", style = MaterialTheme.typography.labelLarge, color = c.textMid)
                }
            },
            containerColor = c.panel,
            titleContentColor = c.textHigh,
            textContentColor = c.textMid
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
                Text("SEND TOKENIZED ASSETS", style = MaterialTheme.typography.titleMedium, color = c.danger)
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Space.md),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "[SANDBOX LEDGER] Simulates local ledger withdrawal only (no mainnet broadcast).",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.goldLeaf
                    )

                    Text("SELECT ASSET TO SEND:", style = MaterialTheme.typography.labelLarge, color = c.textHigh)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Space.xs)
                    ) {
                        listOf("ETH", "TON", "SOL", "TRX", "USDC").forEach { symbol ->
                            val isSel = selectedAsset == symbol
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .background(if (isSel) c.danger.copy(alpha = 0.2f) else c.panelRaised, CyberShapes.chip)
                                    .border(1.dp, if (isSel) c.danger else c.goldAntique.copy(alpha = 0.3f), CyberShapes.chip)
                                    .clickable { selectedAsset = symbol },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(symbol, style = MaterialTheme.typography.labelMedium, color = if (isSel) c.danger else c.textMid)
                            }
                        }
                    }

                    Text("AVAILABLE BALANCE: $maxBalance $selectedAsset", style = MaterialTheme.typography.labelMedium, color = c.textMid)

                    OutlinedTextField(
                        value = recipientAddr,
                        onValueChange = { recipientAddr = it },
                        label = { Text("Recipient Address") },
                        placeholder = { Text("e.g. 0x... or UQ...") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = c.textHigh,
                            unfocusedTextColor = c.textHigh,
                            focusedBorderColor = c.danger,
                            unfocusedBorderColor = c.goldAntique.copy(alpha = 0.35f)
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = sendAmount,
                        onValueChange = { sendAmount = it },
                        label = { Text("Amount to Transfer") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = c.textHigh,
                            unfocusedTextColor = c.textHigh,
                            focusedBorderColor = c.danger,
                            unfocusedBorderColor = c.goldAntique.copy(alpha = 0.35f)
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )

                    val hasPin = profile?.pinCode?.isNotBlank() == true
                    OutlinedTextField(
                        value = pinVal,
                        onValueChange = { pinVal = it },
                        label = { Text(if (hasPin) "Verify 8-Digit Security PIN" else "PIN Verification (PIN not set)") },
                        singleLine = true,
                        placeholder = { Text("Enter security PIN to sign tx") },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = c.textHigh,
                            unfocusedTextColor = c.textHigh,
                            focusedBorderColor = c.danger,
                            unfocusedBorderColor = c.goldAntique.copy(alpha = 0.35f)
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge,
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
                            viewModel.postNotification("Invalid transaction amount!")
                        } else if (amt > maxBalance) {
                            viewModel.postNotification("Insufficient balance!")
                        } else if (recipientAddr.isBlank()) {
                            viewModel.postNotification("Recipient address cannot be empty!")
                        } else {
                            coroutineScope.launch {
                                val pinCorrect = !hasPin || viewModel.verifyPrimaryPin(enteredPin)
                                if (!pinCorrect) {
                                    viewModel.postNotification("Incorrect Security PIN signature!")
                                } else {
                                    viewModel.performAssetTransaction(selectedAsset, -amt, "SEND", recipientAddr)
                                    showSendDialog = false
                                }
                            }
                        }
                    },
                    text = "SIMULATE WITHDRAWAL",
                    skin = currentSkin,
                    containerColor = c.danger,
                    contentColor = c.textHigh,
                    shape = CyberShapes.control
                )
            },
            dismissButton = {
                TextButton(onClick = { showSendDialog = false }) {
                    Text("CANCEL", style = MaterialTheme.typography.labelLarge, color = c.textMid)
                }
            },
            containerColor = c.panel,
            titleContentColor = c.textHigh,
            textContentColor = c.textMid
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
                Text("DEX BRIDGE SWAP PROTOCOL", style = MaterialTheme.typography.titleMedium, color = c.accent)
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Space.md),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Swap assets in peer-to-peer liquidity pools instantly. Automated market maker quotes apply.",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textMid
                    )

                    Text("FROM ASSET:", style = MaterialTheme.typography.labelLarge, color = c.textHigh)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Space.xs)
                    ) {
                        listOf("WEED", "USDC", "ETH", "TON", "SOL", "TRX").forEach { symbol ->
                            val isSel = fromAsset == symbol
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .background(if (isSel) c.accent.copy(alpha = 0.2f) else c.panelRaised, CyberShapes.chip)
                                    .border(1.dp, if (isSel) c.accent else c.goldAntique.copy(alpha = 0.3f), CyberShapes.chip)
                                    .clickable {
                                        fromAsset = symbol
                                        if (symbol != "WEED") {
                                            toAsset = "WEED"
                                        } else if (toAsset == "WEED") {
                                            toAsset = "USDC"
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (symbol == "WEED") "WED" else symbol,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSel) c.accent else c.textMid
                                )
                            }
                        }
                    }

                    Text(
                        "AVAILABLE BALANCE: ${String.format(Locale.US, "%.4f", fromBalance)} ${if (fromAsset == "WEED") "WED" else fromAsset}",
                        style = MaterialTheme.typography.labelMedium,
                        color = c.textMid
                    )

                    OutlinedTextField(
                        value = fromAmountInput,
                        onValueChange = { fromAmountInput = it },
                        label = { Text("Amount to Swap") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = c.textHigh,
                            unfocusedTextColor = c.textHigh,
                            focusedBorderColor = c.accent,
                            unfocusedBorderColor = c.goldAntique.copy(alpha = 0.35f)
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("TO ASSET:", style = MaterialTheme.typography.labelLarge, color = c.textHigh)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Space.xs)
                    ) {
                        listOf("WEED", "USDC", "ETH", "TON", "SOL", "TRX").forEach { symbol ->
                            val isSel = toAsset == symbol
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .background(if (isSel) NeonMagenta.copy(alpha = 0.2f) else c.panelRaised, CyberShapes.chip)
                                    .border(1.dp, if (isSel) NeonMagenta else c.goldAntique.copy(alpha = 0.3f), CyberShapes.chip)
                                    .clickable {
                                        toAsset = symbol
                                        if (symbol != "WEED") {
                                            fromAsset = "WEED"
                                        } else if (fromAsset == "WEED") {
                                            fromAsset = "USDC"
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (symbol == "WEED") "WED" else symbol,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSel) NeonMagenta else c.textMid
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(c.ink, CyberShapes.chip)
                            .border(1.dp, NeonMagenta.copy(alpha = 0.3f), CyberShapes.chip)
                            .padding(Space.md)
                    ) {
                        Text("ESTIMATED CONVERSION:", style = MaterialTheme.typography.labelMedium, color = c.textMid)
                        Spacer(modifier = Modifier.height(Space.xxs))
                        Text(
                            text = "${String.format(Locale.US, "%.6f", toAmountCalculated)} ${if (toAsset == "WEED") "WED" else toAsset}",
                            style = MaterialTheme.typography.titleSmall,
                            fontFamily = IbmPlexMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = NeonMagenta
                        )
                        Spacer(modifier = Modifier.height(Space.xs))
                        val directRate = fromRate / toRate
                        Text(
                            text = "Rate: 1 ${if (fromAsset == "WEED") "WED" else fromAsset} = ${String.format(Locale.US, "%.6f", directRate)} ${if (toAsset == "WEED") "WED" else toAsset}",
                            style = MaterialTheme.typography.labelSmall,
                            color = c.textMid
                        )
                    }
                }
            },
            confirmButton = {
                CyberTextButton(
                    onClick = {
                        val amt = fromAmountInput.toDoubleOrNull()
                        if (amt == null || amt <= 0) {
                            viewModel.postNotification("Invalid swap amount!")
                        } else if (amt > fromBalance) {
                            viewModel.postNotification("Insufficient balance to swap!")
                        } else {
                            viewModel.swapAssets(fromAsset, toAsset, amt, toAmountCalculated)
                            showSwapDialog = false
                        }
                    },
                    text = "EXECUTE SWAP",
                    skin = currentSkin,
                    containerColor = c.accent,
                    contentColor = c.ink,
                    shape = CyberShapes.control
                )
            },
            dismissButton = {
                TextButton(onClick = { showSwapDialog = false }) {
                    Text("CANCEL", style = MaterialTheme.typography.labelLarge, color = c.textMid)
                }
            },
            containerColor = c.panel,
            titleContentColor = c.textHigh,
            textContentColor = c.textMid
        )
    }
}

@Composable
fun OnChainNftPanel(viewModel: TarotViewModel) {
    val c = Cyber.colors
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
        "SUCCESS" -> c.emeraldGlow
        "MINTING" -> c.goldLeaf
        "FAILED" -> c.danger
        else -> c.textMid
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Space.lg)
    ) {
        item {
            CyberPanel(
                borderColor = NeonMagenta.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            ) {
                CyberSectionHeader(
                    title = "READ-ONLY RPC / SANDBOX NFT INTERFACE",
                    subtitle = "RPC queries live chain state (gas/network). Live minting requires external signer; leave RPC blank for local Sandbox receipts.",
                    color = NeonMagenta
                )
                Spacer(modifier = Modifier.height(Space.md))

                OutlinedTextField(
                    value = editingRpc,
                    onValueChange = { editingRpc = it },
                    label = { Text("RPC Endpoint URL") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = c.textHigh,
                        unfocusedTextColor = c.textHigh,
                        focusedBorderColor = NeonMagenta,
                        unfocusedBorderColor = c.goldAntique.copy(alpha = 0.35f)
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth().testTag("contract_rpc_input")
                )

                Spacer(modifier = Modifier.height(Space.sm))

                OutlinedTextField(
                    value = editingAddress,
                    onValueChange = { editingAddress = it },
                    label = { Text("Smart Contract Address") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = c.textHigh,
                        unfocusedTextColor = c.textHigh,
                        focusedBorderColor = NeonMagenta,
                        unfocusedBorderColor = c.goldAntique.copy(alpha = 0.35f)
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth().testTag("contract_address_input")
                )

                Spacer(modifier = Modifier.height(Space.md))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "GAS: ${String.format(Locale.US, "%.2f", gasPrice)} Gwei",
                            style = MaterialTheme.typography.labelLarge,
                            fontFamily = IbmPlexMonoFontFamily,
                            color = c.accent
                        )
                        contractDetails?.let {
                            Text(
                                text = "NET: ${it.networkName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = c.textMid
                            )
                        }
                    }

                    CyberTextButton(
                        onClick = { viewModel.updateRpcAndContractAddress(editingRpc, editingAddress) },
                        text = "CONNECT / UPDATE",
                        skin = currentSkin,
                        containerColor = NeonMagenta,
                        contentColor = c.textHigh,
                        shape = CyberShapes.control,
                        modifier = Modifier.testTag("save_contract_config_btn")
                    )
                }
            }
        }

        item {
            CyberPanel(
                borderColor = c.goldAntique.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ON-CHAIN KERNEL LOGS",
                        style = MaterialTheme.typography.titleSmall,
                        color = c.textHigh
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(Space.xs))
                        Text(
                            text = status,
                            style = MaterialTheme.typography.labelMedium,
                            color = statusColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Space.sm))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(c.ink, CyberShapes.chip)
                        .border(1.dp, c.goldAntique.copy(alpha = 0.25f), CyberShapes.chip)
                        .padding(Space.sm)
                ) {
                    LazyColumn(
                        reverseLayout = true,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(logs.reversed()) { log ->
                            Text(
                                text = "> $log",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (log.contains("ERROR")) c.danger else if (log.contains("CONFIRMED")) c.emeraldGlow else c.textMid,
                                modifier = Modifier.padding(vertical = Space.xxs)
                            )
                        }
                    }
                }
            }
        }

        item {
            CyberSectionHeader(
                title = "MINTER DEPLOYMENTS (${onChainMints.size} CONTRACT TOKENS)",
                color = c.textHigh
            )
        }

        if (onChainMints.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Space.lg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NO DEPLOYED NFTs YET.\nGo to SECURE DECK VAULT, select a card, and mint to chain.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.textMid,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(onChainMints) { nft ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = c.panel),
                    border = BorderStroke(1.dp, c.goldAntique.copy(alpha = 0.25f)),
                    shape = CyberShapes.cardCompact,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Space.md),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "TOKEN ID: #${nft.tokenId}",
                                style = MaterialTheme.typography.titleSmall,
                                fontFamily = IbmPlexMonoFontFamily,
                                color = NeonMagenta
                            )
                            Text(
                                text = "TX HASH: ${nft.txHash.take(16)}...",
                                style = MaterialTheme.typography.labelSmall,
                                color = c.textMid
                            )
                        }

                        if (nft.explorerUrl.isNotBlank()) {
                            val localUri = Uri.parse(nft.explorerUrl)
                            val context = LocalContext.current
                            CyberActionButton(
                                text = "EXPLORER",
                                onClick = {
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, localUri))
                                    } catch (_: Exception) {
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
