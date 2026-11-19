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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CardLocalizationHelper
import com.example.data.CyberTarotDeck
import com.example.data.LunarCalendarService
import com.example.data.MintedCard
import com.example.data.MintingAuspiciousnessTier
import com.example.ui.components.CyberActionButton
import com.example.ui.components.CyberLunarCycleWidget
import com.example.ui.components.CyberNeonButton
import com.example.ui.components.CyberRarityGlowBadge
import com.example.ui.components.CyberRarityShaderOverlay
import com.example.ui.components.FramedTarotArtworkWindow
import com.example.ui.components.getArcanaRomanNumeral
import com.example.ui.components.getCard420Discipline
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
import java.util.Calendar
import java.util.Locale

@Composable
fun VaultScreen(viewModel: TarotViewModel) {
    val c = Cyber.colors
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val lang = profile?.systemLanguage ?: "en"
    val mintedCards by viewModel.mintedCards.collectAsStateWithLifecycle()
    var selectedMintedCard by remember { mutableStateOf<MintedCard?>(null) }
    var listPriceInput by remember { mutableStateOf("") }
    var isListingMode by remember { mutableStateOf(false) }
    var isNftMintingMode by remember { mutableStateOf(false) }
    var testnetWalletInput by remember { mutableStateOf("0x9E88d6C7BaF8F4f3183f233f6329686259e7ab9C") }
    var cardToInspectReview by remember { mutableStateOf<MintedCard?>(null) }
    var showLunarCycleModal by remember { mutableStateOf(false) }

    val todayCal = remember { Calendar.getInstance() }
    val todayLunarDay = remember(lang) {
        LunarCalendarService.getLunarDayInfo(
            todayCal.get(Calendar.YEAR),
            todayCal.get(Calendar.MONTH) + 1,
            todayCal.get(Calendar.DAY_OF_MONTH),
            lang
        )
    }
    val todayLunarMinting = remember(lang) {
        LunarCalendarService.getTarotMintingWindow(
            todayCal.get(Calendar.YEAR),
            todayCal.get(Calendar.MONTH) + 1,
            todayCal.get(Calendar.DAY_OF_MONTH),
            lang
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Space.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "VAULT",
            style = MaterialTheme.typography.headlineSmall,
            color = NeonCyan,
            modifier = Modifier.testTag("vault_title")
        )
        Text(
            text = "Cryptographic Nodes",
            style = MaterialTheme.typography.bodyMedium,
            color = c.textMid,
            modifier = Modifier.padding(top = Space.xxs, bottom = Space.md)
        )

        val lunarStatusColor = when (todayLunarMinting.tier) {
            MintingAuspiciousnessTier.AUSPICIOUS -> NeonGreen
            MintingAuspiciousnessTier.BALANCED -> NeonCyan
            MintingAuspiciousnessTier.CHALLENGING -> NeonRed
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Space.md)
                .heightIn(min = 48.dp)
                .clip(CyberShapes.control)
                .background(c.panel)
                .border(1.dp, lunarStatusColor.copy(alpha = 0.6f), CyberShapes.control)
                .clickable { showLunarCycleModal = true }
                .padding(horizontal = Space.md, vertical = Space.sm)
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
                    Text(text = todayLunarDay.phaseIcon, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.width(Space.sm))
                    Column {
                        Text(
                            text = "${todayLunarDay.phaseName.uppercase()} • ${todayLunarDay.illumination}%",
                            style = MaterialTheme.typography.titleSmall,
                            color = c.textHigh
                        )
                        Text(
                            text = todayLunarMinting.badgeLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = lunarStatusColor
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (lang == "ru") "ЦИКЛ" else "CYCLE",
                        style = MaterialTheme.typography.labelLarge,
                        color = NeonCyan
                    )
                    Spacer(modifier = Modifier.width(Space.xxs))
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Lunar Cycle",
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        if (mintedCards.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 64.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Empty",
                    tint = c.textMid.copy(alpha = 0.5f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(Space.lg))
                Text(
                    text = "VAULT SECURED - 0 CARDS OWNED",
                    style = MaterialTheme.typography.titleMedium,
                    color = c.textMid
                )
                Text(
                    text = "Perform a divination scan and mint drawn cards to get started.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textMid,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = Space.xl)
                        .padding(top = Space.xs)
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                horizontalArrangement = Arrangement.spacedBy(Space.md),
                verticalArrangement = Arrangement.spacedBy(Space.md)
            ) {
                items(mintedCards) { mintedCard ->
                    val card = CyberTarotDeck.getCard(mintedCard.cardIndex)
                    val rarityColor = remember(mintedCard.rarity) {
                        when (mintedCard.rarity) {
                            "Legend" -> NeonGold
                            "Epic" -> NeonMagenta
                            "Rare" -> NeonCyan
                            else -> NeonGreen
                        }
                    }

                    val cardBgBrush = Brush.verticalGradient(
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
                            .background(cardBgBrush, CyberShapes.control)
                            .clickable { cardToInspectReview = mintedCard }
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            CyberRarityShaderOverlay(
                                rarity = mintedCard.rarity,
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(CyberShapes.control)
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Space.md)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CyberRarityGlowBadge(rarity = mintedCard.rarity)
                                    Text(
                                        text = "#${String.format(Locale.US, "%04d", mintedCard.mintNumber)}",
                                        style = MaterialTheme.typography.labelLarge.copy(fontFamily = IbmPlexMonoFontFamily),
                                        color = c.textHigh
                                    )
                                }

                                Spacer(modifier = Modifier.height(Space.sm))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(0.75f)
                                ) {
                                    FramedTarotArtworkWindow(
                                        card = card,
                                        isUpright = true,
                                        compactWatermark = true,
                                        showDiagonalWatermark = true,
                                        rarityBorderColor = rarityColor,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(Space.xs)
                                            .background(c.ink.copy(alpha = 0.82f), CyberShapes.chip)
                                            .border(0.8.dp, NeonGold.copy(alpha = 0.7f), CyberShapes.chip)
                                            .padding(horizontal = Space.xs, vertical = Space.xxs)
                                    ) {
                                        Text(
                                            text = "${getArcanaRomanNumeral(card.index)} • ${getCard420Discipline(card.index).icon}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = OrbitronFontFamily,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = NeonGold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(Space.sm))

                                val localizedName = CardLocalizationHelper.getLocalizedName(card.index, lang)
                                val localizedTraditional = CardLocalizationHelper.getLocalizedTraditionalName(card.index, lang)
                                Text(
                                    text = localizedName.uppercase(),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = c.textHigh,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${card.series} • $localizedTraditional",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = c.goldLeaf,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                HorizontalDivider(
                                    color = rarityColor.copy(alpha = 0.3f),
                                    modifier = Modifier.padding(vertical = Space.sm)
                                )

                                if (mintedCard.isListed) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("LISTED", style = MaterialTheme.typography.labelMedium, color = c.goldLeaf)
                                        Text(
                                            text = "${mintedCard.listPrice} N$",
                                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = IbmPlexMonoFontFamily),
                                            color = c.goldLeaf
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "SECURED IN COLD VAULT",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeonCyan
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedMintedCard?.let { mintedCard ->
        val card = CyberTarotDeck.getCard(mintedCard.cardIndex)
        val localizedName = CardLocalizationHelper.getLocalizedName(card.index, lang)
        val localizedTraditional = CardLocalizationHelper.getLocalizedTraditionalName(card.index, lang)
        val localizedDesc = CardLocalizationHelper.getLocalizedDescription(card.index, lang)
        val localizedCircuitry = CardLocalizationHelper.getLocalizedCircuitryElement(card.index, lang)
        val rarityColor = when (mintedCard.rarity) {
            "Legend" -> NeonGold
            "Epic" -> NeonMagenta
            "Rare" -> NeonCyan
            else -> NeonGreen
        }

        AlertDialog(
            onDismissRequest = { selectedMintedCard = null },
            containerColor = c.panel,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = localizedName.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            color = c.textHigh
                        )
                        Text(
                            text = "Rider-Waite: $localizedTraditional",
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textMid
                        )
                    }
                    Text(
                        text = "#${String.format(Locale.US, "%04d", mintedCard.mintNumber)}",
                        style = MaterialTheme.typography.labelLarge.copy(fontFamily = IbmPlexMonoFontFamily),
                        color = c.textHigh
                    )
                }
            },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    item {
                        HorizontalDivider(color = c.goldAntique.copy(alpha = 0.25f), modifier = Modifier.padding(bottom = Space.md))

                        Text(
                            text = "NODE SPECIFICATION LORE",
                            style = MaterialTheme.typography.labelLarge,
                            color = rarityColor
                        )
                        Text(
                            text = localizedDesc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = c.textHigh,
                            modifier = Modifier.padding(top = Space.xxs, bottom = Space.md)
                        )

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Hardware Core: ",
                                style = MaterialTheme.typography.labelLarge,
                                color = c.textMid
                            )
                            Text(
                                text = "`$localizedCircuitry`",
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = IbmPlexMonoFontFamily),
                                color = NeonCyan
                            )
                        }

                        HorizontalDivider(color = c.goldAntique.copy(alpha = 0.25f), modifier = Modifier.padding(vertical = Space.md))

                        if (!isListingMode) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(Space.sm)
                            ) {
                                if (mintedCard.isListed) {
                                    CyberNeonButton(
                                        text = "CANCEL MARKET LISTING",
                                        onClick = {
                                            viewModel.cancelCardListing(mintedCard.cardIndex, mintedCard.mintNumber)
                                            selectedMintedCard = null
                                        },
                                        color = NeonRed,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    CyberNeonButton(
                                        text = "LIST ON NEURAL EXCHANGE",
                                        onClick = { isListingMode = true },
                                        color = NeonCyan,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    val basePrice = viewModel.getBaseMarketPrice(mintedCard.rarity)
                                    val brokerPrice = ((basePrice * 3) / 4).coerceAtLeast(15)

                                    CyberActionButton(
                                        text = "INSTANT SELL TO BROKER ($brokerPrice N$)",
                                        onClick = {
                                            viewModel.sellInstantly(mintedCard.id)
                                            selectedMintedCard = null
                                        },
                                        icon = Icons.Default.Bolt,
                                        secondary = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    if (!isNftMintingMode) {
                                        CyberNeonButton(
                                            text = "MINT AS TESTNET NFT",
                                            onClick = { isNftMintingMode = true },
                                            color = NeonMagenta,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("mint_testnet_nft_button")
                                        )
                                    } else {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(c.ink, CyberShapes.control)
                                                .border(1.dp, NeonMagenta.copy(alpha = 0.35f), CyberShapes.control)
                                                .padding(Space.md)
                                        ) {
                                            Text(
                                                text = "ON-CHAIN WALLET DESTINATION",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = NeonMagenta
                                            )
                                            Spacer(modifier = Modifier.height(Space.xs))
                                            OutlinedTextField(
                                                value = testnetWalletInput,
                                                onValueChange = { testnetWalletInput = it },
                                                placeholder = { Text("0x...") },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = c.textHigh,
                                                    unfocusedTextColor = c.textHigh,
                                                    focusedBorderColor = currentSkin.primaryColor,
                                                    unfocusedBorderColor = c.goldAntique.copy(alpha = 0.4f)
                                                ),
                                                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = IbmPlexMonoFontFamily),
                                                modifier = Modifier.fillMaxWidth()
                                            )

                                            Spacer(modifier = Modifier.height(Space.sm))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(Space.sm)
                                            ) {
                                                CyberActionButton(
                                                    text = "CANCEL",
                                                    onClick = { isNftMintingMode = false },
                                                    secondary = true,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                CyberNeonButton(
                                                    text = "MINT NFT",
                                                    onClick = {
                                                        viewModel.mintTarotInterpretationOnChain(
                                                            cardIndex = mintedCard.cardIndex,
                                                            mintNumber = mintedCard.mintNumber,
                                                            rarity = mintedCard.rarity,
                                                            interpretation = card.description,
                                                            walletAddress = testnetWalletInput
                                                        )
                                                        isNftMintingMode = false
                                                        selectedMintedCard = null
                                                    },
                                                    color = NeonMagenta,
                                                    modifier = Modifier
                                                        .weight(1.4f)
                                                        .testTag("confirm_nft_mint_btn")
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "SET ASK PRICE (WEED)",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = c.textHigh
                                )
                                Spacer(modifier = Modifier.height(Space.xs))
                                OutlinedTextField(
                                    value = listPriceInput,
                                    onValueChange = { listPriceInput = it },
                                    placeholder = { Text("e.g. 500") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = c.textHigh,
                                        unfocusedTextColor = c.textHigh,
                                        focusedBorderColor = currentSkin.primaryColor,
                                        unfocusedBorderColor = c.goldAntique.copy(alpha = 0.4f)
                                    ),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = IbmPlexMonoFontFamily),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(Space.md))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(Space.md)
                                ) {
                                    CyberActionButton(
                                        text = "BACK",
                                        onClick = { isListingMode = false },
                                        secondary = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    CyberNeonButton(
                                        text = "EXECUTE LIST",
                                        onClick = {
                                            val price = listPriceInput.toIntOrNull() ?: 0
                                            viewModel.listCardOnMarket(mintedCard.id, price)
                                            selectedMintedCard = null
                                        },
                                        color = NeonCyan,
                                        modifier = Modifier.weight(1.4f)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { selectedMintedCard = null },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("CLOSE", style = MaterialTheme.typography.labelLarge, color = NeonCyan)
                }
            }
        )
    }

    cardToInspectReview?.let { mintedCard ->
        FullScreenCardReview(
            card = CyberTarotDeck.getCard(mintedCard.cardIndex),
            mintedCard = mintedCard,
            isMarketView = false,
            systemLanguage = lang,
            skin = currentSkin,
            actionLabel = if (lang == "ru") "УПРАВЛЕНИЕ NFT" else "MANAGE NFT",
            onClose = { cardToInspectReview = null },
            onActionClick = {
                selectedMintedCard = mintedCard
                isListingMode = false
                listPriceInput = ""
                cardToInspectReview = null
            }
        )
    }

    if (showLunarCycleModal) {
        AlertDialog(
            onDismissRequest = { showLunarCycleModal = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxSize()
                .padding(Space.lg),
            containerColor = c.ink,
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center
                ) {
                    CyberLunarCycleWidget(
                        systemLanguage = lang,
                        currentSkin = currentSkin,
                        onMintClick = {
                            showLunarCycleModal = false
                            viewModel.resetDivination()
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showLunarCycleModal = false },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(
                        text = if (lang == "ru") "ЗАКРЫТЬ" else "CLOSE",
                        style = MaterialTheme.typography.labelLarge,
                        color = NeonCyan
                    )
                }
            }
        )
    }
}
