package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.CardLocalizationHelper
import com.example.data.CyberTarotDeck
import com.example.data.MarketListing
import com.example.data.MintedCard
import com.example.ui.components.CyberActionButton
import com.example.ui.components.CyberChip
import com.example.ui.components.CyberNeonButton
import com.example.ui.components.CyberPanel
import com.example.ui.components.CyberRarityGlowBadge
import com.example.ui.components.CyberRarityShaderOverlay
import com.example.ui.components.FramedTarotArtworkWindow
import com.example.ui.components.getArcanaRomanNumeral
import com.example.ui.theme.Cyber
import com.example.ui.theme.CyberShapes
import com.example.ui.theme.IbmPlexMonoFontFamily
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonRed
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.Space
import com.example.viewmodel.TarotViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MarketScreen(viewModel: TarotViewModel) {
    val c = Cyber.colors
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val lang = profile?.systemLanguage ?: "en"
    val listings by viewModel.filteredMarketListings.collectAsStateWithLifecycle()
    val searchBy by viewModel.marketQuery.collectAsStateWithLifecycle()
    val filterByRarity by viewModel.marketRarityFilter.collectAsStateWithLifecycle()
    val transactionLogs by viewModel.transactionLogs.collectAsStateWithLifecycle()
    val miningCooldown by viewModel.miningCooldown.collectAsStateWithLifecycle()
    val isWatermarkUnlocked by viewModel.isWatermarkUnlocked.collectAsStateWithLifecycle()
    val isWatermarkHidden by viewModel.isWatermarkHidden.collectAsStateWithLifecycle()

    var cardToBuyConfirm by remember { mutableStateOf<MarketListing?>(null) }
    var cardToInspectReview by remember { mutableStateOf<MarketListing?>(null) }
    var inspectGenesisEliminator by remember { mutableStateOf(false) }

    val rarities = listOf("All", "Common", "Rare", "Epic", "Legend", "Genesis")
    val haptic = LocalHapticFeedback.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Space.lg),
        contentPadding = PaddingValues(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "EXCHANGE",
                style = MaterialTheme.typography.headlineSmall,
                color = c.goldLeaf,
                modifier = Modifier.testTag("exchange_title")
            )
            Text(
                text = "P2P Transactions",
                style = MaterialTheme.typography.bodyMedium,
                color = c.textMid,
                modifier = Modifier.padding(top = Space.xxs, bottom = Space.md)
            )

            CyberPanel(
                borderColor = NeonGreen.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Space.sm)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "HARVESTER",
                            style = MaterialTheme.typography.titleMedium,
                            color = NeonGreen
                        )
                        Text(
                            text = "Harvest Weed credits",
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textMid
                        )
                    }

                    if (miningCooldown > 0) {
                        Box(
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .clip(CyberShapes.card)
                                .background(c.goldLeaf.copy(alpha = 0.15f))
                                .border(1.dp, c.goldLeaf, CyberShapes.card)
                                .padding(horizontal = Space.md),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "00:${String.format(Locale.US, "%02d", miningCooldown)} COOLDOWN",
                                style = MaterialTheme.typography.labelLarge.copy(fontFamily = IbmPlexMonoFontFamily),
                                color = c.goldLeaf
                            )
                        }
                    } else {
                        CyberActionButton(
                            text = "HARVEST (+350 N$)",
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.startMining()
                            },
                            icon = Icons.Default.Bolt
                        )
                    }
                }

                if (miningCooldown > 0) {
                    Spacer(modifier = Modifier.height(Space.sm))
                    LinearProgressIndicator(
                        progress = { (30f - miningCooldown.toFloat()) / 30f },
                        color = NeonGreen,
                        trackColor = c.ink,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(CyberShapes.chip)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Space.sm))

            // Collector Spotlight: GENESIS WATERMARK ELIMINATOR ($5.99 eq.)
            CyberPanel(
                borderColor = NeonGold.copy(alpha = 0.82f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Space.sm)
                    .testTag("market_genesis_eliminator_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = NeonGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(Space.xs))
                        Text(
                            text = "GENESIS WATERMARK ELIMINATOR",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = OrbitronFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = NeonGold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(NeonGold.copy(alpha = 0.16f), CyberShapes.chip)
                            .border(1.dp, NeonGold, CyberShapes.chip)
                            .padding(horizontal = Space.sm, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isWatermarkUnlocked) "UNLOCKED" else "$5.99",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = IbmPlexMonoFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = NeonGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Space.xs))
                Text(
                    text = if (lang == "ru") {
                        "Золотая карта серии Genesis + рубашка Genesis (Рождение Жизни из Стихий). Открывает опцию снятия полноэкранной вотермарки WEED GURU PROPERTY."
                    } else {
                        "Gold-framed Genesis edition + Genesis Emergence-of-Life card back. Unlocks permanent fullscreen WEED GURU PROPERTY watermark toggle."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textMid
                )

                Spacer(modifier = Modifier.height(Space.sm))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { inspectGenesisEliminator = true },
                    horizontalArrangement = Arrangement.spacedBy(Space.sm)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.card_genesis_eliminator_1795033578202),
                        contentDescription = "Genesis Watermark Eliminator Front",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.75f)
                            .clip(CyberShapes.cardCompact)
                            .border(1.5.dp, NeonGold, CyberShapes.cardCompact)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.back_genesis_elements_1795033577974),
                        contentDescription = "Genesis Emergence of Life Card Back",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.75f)
                            .clip(CyberShapes.cardCompact)
                            .border(1.5.dp, NeonGold, CyberShapes.cardCompact)
                    )
                }

                Spacer(modifier = Modifier.height(Space.md))

                if (isWatermarkUnlocked) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(c.ink.copy(alpha = 0.72f), CyberShapes.control)
                            .border(1.dp, NeonGold.copy(alpha = 0.5f), CyberShapes.control)
                            .padding(horizontal = Space.md, vertical = Space.sm),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (lang == "ru") "СНЯТЬ ВОТЕРМАРКУ (FULLSCREEN)" else "HIDE FULLSCREEN WATERMARK",
                                style = MaterialTheme.typography.labelLarge,
                                color = NeonGold
                            )
                            Text(
                                text = if (isWatermarkHidden) {
                                    if (lang == "ru") "Вотермарка скрыта в полноэкранном режиме" else "Watermark hidden in fullscreen mode"
                                } else {
                                    if (lang == "ru") "Вотермарка WEED GURU PROPERTY активна" else "WEED GURU PROPERTY watermark active"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = c.textMid
                            )
                        }
                        Switch(
                            checked = isWatermarkHidden,
                            onCheckedChange = { viewModel.toggleFullscreenWatermark(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = NeonGold,
                                uncheckedThumbColor = NeonGold,
                                uncheckedTrackColor = c.panelRaised
                            )
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Space.sm)
                    ) {
                        CyberNeonButton(
                            text = "5.99 USDC ($5.99)",
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.purchaseGenesisWatermarkEliminator(payWithUsdc = true)
                            },
                            color = NeonGold,
                            modifier = Modifier.weight(1f)
                        )
                        CyberNeonButton(
                            text = "60 WEED",
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.purchaseGenesisWatermarkEliminator(payWithUsdc = false)
                            },
                            color = NeonGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Space.sm))

            OutlinedTextField(
                value = searchBy,
                onValueChange = { viewModel.setMarketQuery(it) },
                placeholder = { Text("Search by name, traditional, or seller...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = currentSkin.primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = c.textHigh,
                    unfocusedTextColor = c.textHigh,
                    focusedBorderColor = currentSkin.primaryColor,
                    unfocusedBorderColor = c.goldAntique.copy(alpha = 0.4f),
                    focusedContainerColor = c.ink.copy(alpha = 0.65f),
                    unfocusedContainerColor = c.ink.copy(alpha = 0.45f)
                ),
                textStyle = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("market_search_input"),
                shape = CyberShapes.control
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = Space.md),
                horizontalArrangement = Arrangement.spacedBy(Space.sm)
            ) {
                rarities.forEach { r ->
                    CyberChip(
                        label = r.uppercase(),
                        selected = filterByRarity == r,
                        onClick = { viewModel.setMarketRarityFilter(r) }
                    )
                }
            }
        }

        if (listings.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        tint = c.textMid.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(Space.md))
                    Text(
                        text = "NO TRANSACTIONS IN DOCK",
                        style = MaterialTheme.typography.titleMedium,
                        color = c.textMid
                    )
                }
            }
        } else {
            items(listings) { listing ->
                val card = CyberTarotDeck.getCard(listing.cardIndex)
                val rarityColor = remember(listing.rarity) {
                    when (listing.rarity) {
                        "Genesis", "Legend" -> NeonGold
                        "Epic" -> NeonMagenta
                        "Rare" -> NeonCyan
                        else -> NeonGreen
                    }
                }

                val cardBgBrush = Brush.horizontalGradient(
                    colors = listOf(
                        rarityColor.copy(alpha = 0.22f),
                        c.panel.copy(alpha = 0.95f)
                    )
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(2.dp, rarityColor),
                    shape = CyberShapes.control,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Space.sm)
                        .background(cardBgBrush, CyberShapes.control)
                        .clickable { cardToInspectReview = listing }
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        CyberRarityShaderOverlay(
                            rarity = listing.rarity,
                            modifier = Modifier
                                .matchParentSize()
                                .clip(CyberShapes.control)
                        )
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Space.sm),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .widthIn(max = 220.dp)
                                        .fillMaxWidth(0.62f)
                                        .aspectRatio(0.75f)
                                ) {
                                    FramedTarotArtworkWindow(
                                        card = card,
                                        isUpright = true,
                                        compactWatermark = true,
                                        showDiagonalWatermark = true,
                                        rarityBorderColor = rarityColor,
                                        cornerRadius = 10,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(Space.sm)
                                            .background(c.ink.copy(alpha = 0.82f), CyberShapes.chip)
                                            .border(0.9.dp, NeonGold.copy(alpha = 0.8f), CyberShapes.chip)
                                            .padding(horizontal = Space.sm, vertical = Space.xxs)
                                    ) {
                                        Text(
                                            text = "${getArcanaRomanNumeral(card.index)} • ${card.series.uppercase()}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = OrbitronFontFamily,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = NeonGold
                                        )
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Space.lg)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CyberRarityGlowBadge(rarity = listing.rarity)
                                        Spacer(modifier = Modifier.width(Space.sm))
                                        Text(
                                            text = "#${String.format(Locale.US, "%04d", listing.mintNumber)}",
                                            style = MaterialTheme.typography.labelLarge.copy(fontFamily = IbmPlexMonoFontFamily),
                                            color = c.textHigh
                                        )
                                    }

                                    Text(
                                        text = "${listing.price} N$",
                                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = IbmPlexMonoFontFamily),
                                        color = c.goldLeaf
                                    )
                                }

                                Spacer(modifier = Modifier.height(Space.sm))

                                val localizedName = CardLocalizationHelper.getLocalizedName(card.index, lang)
                                Text(
                                    text = localizedName.uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = c.textHigh
                                )

                                Spacer(modifier = Modifier.height(Space.xxs))

                                Text(
                                    text = "Seller: ${listing.sellerName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = c.goldLeaf
                                )

                                Spacer(modifier = Modifier.height(Space.md))

                                if (listing.sellerName == "User (You)") {
                                    CyberNeonButton(
                                        text = "DELIST",
                                        onClick = { viewModel.cancelCardListing(listing.cardIndex, listing.mintNumber) },
                                        color = NeonRed,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    CyberNeonButton(
                                        text = "BUY NFT",
                                        onClick = { cardToBuyConfirm = listing },
                                        color = NeonCyan,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            HorizontalDivider(
                color = c.goldAntique.copy(alpha = 0.25f),
                modifier = Modifier.padding(vertical = Space.lg)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(Space.xs))
                Text(
                    text = "NEURAL LEDGER SYNCED",
                    style = MaterialTheme.typography.titleSmall,
                    color = NeonCyan
                )
            }
            Spacer(modifier = Modifier.height(Space.sm))
        }

        if (transactionLogs.isEmpty()) {
            item {
                Text(
                    text = "Initializing ledger sync records...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textMid
                )
            }
        } else {
            items(transactionLogs.take(15)) { log ->
                val cardName = if (log.cardIndex >= 0) CyberTarotDeck.getCard(log.cardIndex).name else "CREDIT NODE"
                val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
                val dateString = dateFormat.format(Date(log.timestamp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Space.xxs),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "[$dateString] ${log.type}: $cardName -> ${log.targetParty}",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = IbmPlexMonoFontFamily),
                        color = when (log.type) {
                            "BUY" -> NeonCyan
                            "SELL" -> NeonGold
                            "MINT" -> NeonMagenta
                            "MINE" -> NeonGreen
                            else -> c.textMid
                        },
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${if (log.type == "BUY" || log.type == "MINT") "-" else "+"}${log.price} N$",
                        style = MaterialTheme.typography.labelLarge.copy(fontFamily = IbmPlexMonoFontFamily),
                        color = if (log.type == "BUY" || log.type == "MINT") NeonRed else NeonGreen
                    )
                }
            }
        }
    }

    cardToInspectReview?.let { listing ->
        FullScreenCardReview(
            card = CyberTarotDeck.getCard(listing.cardIndex),
            mintedCard = MintedCard(cardIndex = listing.cardIndex, rarity = listing.rarity, mintNumber = listing.mintNumber),
            isMarketView = true,
            systemLanguage = lang,
            skin = currentSkin,
            onClose = { cardToInspectReview = null },
            onActionClick = {
                cardToBuyConfirm = listing
                cardToInspectReview = null
            }
        )
    }

    if (inspectGenesisEliminator) {
        FullScreenCardReview(
            card = CyberTarotDeck.getCard(CyberTarotDeck.GENESIS_ELIMINATOR_INDEX),
            isMarketView = false,
            systemLanguage = lang,
            skin = currentSkin,
            onClose = { inspectGenesisEliminator = false }
        )
    }

    cardToBuyConfirm?.let { listing ->
        val card = CyberTarotDeck.getCard(listing.cardIndex)
        AlertDialog(
            onDismissRequest = { cardToBuyConfirm = null },
            containerColor = c.panel,
            title = {
                Text(
                    text = "AUTHORIZE TRANSACTION",
                    style = MaterialTheme.typography.titleMedium,
                    color = c.goldLeaf
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Do you want to authorize secure credit escrow transfer for this node?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.textHigh
                    )
                    Spacer(modifier = Modifier.height(Space.lg))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(c.ink, CyberShapes.control)
                            .border(1.dp, c.goldAntique.copy(alpha = 0.25f), CyberShapes.control)
                            .padding(Space.md)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(Space.xxs)) {
                            Text(
                                text = "NODE: ${card.name.uppercase()}",
                                style = MaterialTheme.typography.titleSmall,
                                color = NeonCyan
                            )
                            Text(
                                text = "SERIAL: #${String.format(Locale.US, "%04d", listing.mintNumber)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = IbmPlexMonoFontFamily),
                                color = c.textMid
                            )
                            Text(
                                text = "RARITY: ${listing.rarity.uppercase()}",
                                style = MaterialTheme.typography.labelLarge,
                                color = when (listing.rarity) {
                                    "Legend" -> NeonGold
                                    "Epic" -> NeonMagenta
                                    "Rare" -> NeonCyan
                                    else -> NeonGreen
                                }
                            )
                            HorizontalDivider(
                                color = c.goldAntique.copy(alpha = 0.25f),
                                modifier = Modifier.padding(vertical = Space.xs)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "ESCROW DEBT:",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = c.textMid
                                )
                                Text(
                                    text = "${listing.price} N$",
                                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = IbmPlexMonoFontFamily),
                                    color = c.goldLeaf
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                CyberNeonButton(
                    text = "CONFIRM DECREE",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.buyCardFromMarket(listing)
                        cardToBuyConfirm = null
                    },
                    color = NeonCyan
                )
            },
            dismissButton = {
                CyberActionButton(
                    text = "ABORT",
                    onClick = { cardToBuyConfirm = null },
                    secondary = true
                )
            }
        )
    }
}
