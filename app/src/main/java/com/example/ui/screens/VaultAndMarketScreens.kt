package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.TarotViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VaultScreen(viewModel: TarotViewModel) {
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

    val todayCal = remember { java.util.Calendar.getInstance() }
    val todayLunarDay = remember(lang) {
        LunarCalendarService.getLunarDayInfo(
            todayCal.get(java.util.Calendar.YEAR),
            todayCal.get(java.util.Calendar.MONTH) + 1,
            todayCal.get(java.util.Calendar.DAY_OF_MONTH),
            lang
        )
    }
    val todayLunarMinting = remember(lang) {
        LunarCalendarService.getTarotMintingWindow(
            todayCal.get(java.util.Calendar.YEAR),
            todayCal.get(java.util.Calendar.MONTH) + 1,
            todayCal.get(java.util.Calendar.DAY_OF_MONTH),
            lang
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 54.dp)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "VAULT",
            fontSize = 24.sp,
            color = NeonCyan,
            fontFamily = RajdhaniFontFamily,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("vault_title")
        )
        Text(
            text = "Cryptographic Nodes",
            fontSize = 16.sp,
            color = Color(0xFFB0B0C5),
            fontFamily = RajdhaniFontFamily,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        // Auspicious Lunar Cycle Minting Bar
        val lunarStatusColor = when (todayLunarMinting.tier) {
            MintingAuspiciousnessTier.AUSPICIOUS -> Color(0xFF00FF66)
            MintingAuspiciousnessTier.BALANCED -> Color(0xFF00F5FF)
            MintingAuspiciousnessTier.CHALLENGING -> Color(0xFFFF3366)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF070A14))
                .border(1.dp, lunarStatusColor.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                .clickable { showLunarCycleModal = true }
                .padding(horizontal = 12.dp, vertical = 9.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = todayLunarDay.phaseIcon, fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${todayLunarDay.phaseName.uppercase()} • ${todayLunarDay.illumination}%",
                            fontSize = 16.sp,
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = todayLunarMinting.badgeLabel,
                            fontSize = 14.sp,
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = lunarStatusColor
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (lang == "ru") "ЦИКЛ" else "CYCLE",
                        fontSize = 15.sp,
                        fontFamily = RajdhaniFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                    Spacer(modifier = Modifier.width(4.dp))
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
            // Empty Vault state
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
                    tint = Color.DarkGray,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "VAULT SECURED - 0 CARDS OWNED",
                    fontSize = 16.sp,
                    fontFamily = RajdhaniFontFamily,
                    color = Color(0xFFB0B0C5),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Perform a divination scan and mint drawn cards to get started.",
                    fontSize = 15.sp,
                    fontFamily = RajdhaniFontFamily,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp).padding(top = 4.dp)
                )
            }
        } else {
            // Cards Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            Color(0xFF0A0918).copy(alpha = 0.95f)
                        )
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        border = BorderStroke(2.5.dp, rarityColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(cardBgBrush, RoundedCornerShape(12.dp))
                            .clickable {
                                cardToInspectReview = mintedCard
                            }
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            CyberRarityShaderOverlay(
                                rarity = mintedCard.rarity,
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                // Rarity Badge & Number
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CyberRarityGlowBadge(
                                        rarity = mintedCard.rarity
                                    )

                                    ContrastText(
                                        text = "#${String.format("%04d", mintedCard.mintNumber)}",
                                        fontSize = 16.sp,
                                        fontFamily = RajdhaniFontFamily,
                                        color = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Card Art Preview (Unified Framed Tarot Artwork + WEED GURU PROPERTY Watermark)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(155.dp)
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
                                            .padding(6.dp)
                                            .background(Color.Black.copy(alpha = 0.78f), RoundedCornerShape(6.dp))
                                            .border(0.8.dp, NeonGold.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${getArcanaRomanNumeral(card.index)} • ${getCard420Discipline(card.index).icon}",
                                            fontFamily = OrbitronFontFamily,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NeonGold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Card Title
                                val localizedName = CardLocalizationHelper.getLocalizedName(card.index, lang)
                                val localizedTraditional = CardLocalizationHelper.getLocalizedTraditionalName(card.index, lang)
                                ContrastText(
                                    text = localizedName.uppercase(),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = RajdhaniFontFamily,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                ContrastText(
                                    text = "${card.series} • $localizedTraditional",
                                    fontSize = 14.sp,
                                    fontFamily = RajdhaniFontFamily,
                                    color = NeonGold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                HorizontalDivider(color = rarityColor.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

                                // Market Listing Status
                                if (mintedCard.isListed) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        ContrastText(
                                            "LISTED",
                                            fontSize = 15.sp,
                                            color = NeonGold,
                                            fontFamily = RajdhaniFontFamily,
                                            fontWeight = FontWeight.Bold
                                        )
                                        ContrastText(
                                            "${mintedCard.listPrice} N$",
                                            fontSize = 15.sp,
                                            color = NeonGold,
                                            fontFamily = RajdhaniFontFamily,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    ContrastText(
                                        "SECURED IN COLD VAULT",
                                        fontSize = 15.sp,
                                        color = NeonCyan,
                                        fontFamily = RajdhaniFontFamily
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail Modal for selected owned card
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
            containerColor = CyberCharcoal,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = localizedName.uppercase(),
                            color = Color.White,
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Rider-Waite: $localizedTraditional",
                            color = Color(0xFFB0B0C5),
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp
                        )
                    }

                    Text(
                        text = "#${String.format("%04d", mintedCard.mintNumber)}",
                        color = Color.White,
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 14.sp
                    )
                }
            },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    item {
                        HorizontalDivider(color = CyberMutedBlue, modifier = Modifier.padding(bottom = 12.dp))

                        // Hardware Specs
                        Text(
                            "NODE SPECIFICATION LORE",
                            color = rarityColor,
                            fontSize = 16.sp,
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = localizedDesc,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontFamily = RajdhaniFontFamily,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )

                        // Hardware Analogy
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "Hardware Core: ",
                                color = Color(0xFFB0B0C5),
                                fontSize = 16.sp,
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "`$localizedCircuitry`",
                                color = NeonCyan,
                                fontSize = 16.sp,
                                fontFamily = RajdhaniFontFamily
                            )
                        }

                        HorizontalDivider(color = CyberMutedBlue, modifier = Modifier.padding(vertical = 12.dp))

                        if (!isListingMode) {
                            // Display Standard Actions
                            Column(modifier = Modifier.fillMaxWidth()) {
                                if (mintedCard.isListed) {
                                    // Cancel Listing Option
                                    CyberTextButton(
                                        onClick = {
                                            viewModel.cancelCardListing(mintedCard.cardIndex, mintedCard.mintNumber)
                                            selectedMintedCard = null
                                        },
                                        text = "CANCEL MARKET LISTING",
                                        skin = currentSkin,
                                        containerColor = NeonRed,
                                        contentColor = Color.White,
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    // List on Market
                                    CyberTextButton(
                                        onClick = { isListingMode = true },
                                        text = "LIST ON NEURAL EXCHANGE",
                                        skin = currentSkin,
                                        containerColor = NeonCyan,
                                        contentColor = CyberObsidian,
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp)
                                    )

                                    // Instant liquidation to Net Broker
                                    val basePrice = viewModel.getBaseMarketPrice(mintedCard.rarity)
                                    val brokerPrice = ((basePrice * 3) / 4).coerceAtLeast(15)

                                    CyberButton(
                                        onClick = {
                                            viewModel.sellInstantly(mintedCard.id)
                                            selectedMintedCard = null
                                        },
                                        skin = currentSkin,
                                        containerColor = ElegantIndigo,
                                        contentColor = Color.White,
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Bolt,
                                                contentDescription = null,
                                                tint = NeonCyan,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            AutoResizingText(
                                                text = "INSTANT SELL TO BROKER ($brokerPrice N$)",
                                                fontFamily = RajdhaniFontFamily,
                                                targetTextSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    if (!isNftMintingMode) {
                                        CyberButton(
                                            onClick = { isNftMintingMode = true },
                                            skin = currentSkin,
                                            containerColor = NeonMagenta,
                                            contentColor = Color.White,
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier.fillMaxWidth().testTag("mint_testnet_nft_button")
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Bolt,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                AutoResizingText(
                                                    text = "MINT AS TESTNET NFT",
                                                    fontFamily = RajdhaniFontFamily,
                                                    targetTextSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    } else {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFF09090E), RoundedCornerShape(8.dp))
                                                .border(1.dp, NeonMagenta.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                .padding(12.dp)
                                        ) {
                                            Text(
                                                "ON-CHAIN WALLET DESTINATION",
                                                color = NeonMagenta,
                                                fontSize = 15.sp,
                                                fontFamily = RajdhaniFontFamily,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            OutlinedTextField(
                                                value = testnetWalletInput,
                                                onValueChange = { testnetWalletInput = it },
                                                placeholder = { Text("0x...", color = Color.White.copy(alpha = 0.6f), fontSize = 17.sp, fontWeight = FontWeight.Bold) },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White,
                                                    focusedBorderColor = currentSkin.primaryColor,
                                                    unfocusedBorderColor = currentSkin.secondaryColor.copy(alpha = 0.5f),
                                                    focusedContainerColor = Color.Black.copy(alpha = 0.65f),
                                                    unfocusedContainerColor = Color.Black.copy(alpha = 0.45f)
                                                ),
                                                textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 17.sp, fontWeight = FontWeight.Bold),
                                                modifier = Modifier.fillMaxWidth()
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                CyberTextButton(
                                                    onClick = { isNftMintingMode = false },
                                                    text = "CANCEL",
                                                    skin = currentSkin,
                                                    containerColor = Color.Transparent,
                                                    contentColor = Color.Gray,
                                                    shape = RoundedCornerShape(16.dp),
                                                    modifier = Modifier.weight(1f)
                                                )
                                                CyberTextButton(
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
                                                    text = "MINT NFT",
                                                    skin = currentSkin,
                                                    containerColor = NeonMagenta,
                                                    contentColor = Color.White,
                                                    shape = RoundedCornerShape(16.dp),
                                                    modifier = Modifier.weight(1.5f).testTag("confirm_nft_mint_btn")
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Display Listing Input Frame
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    "SET ASK PRICE (WEED)",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = listPriceInput,
                                    onValueChange = { listPriceInput = it },
                                    placeholder = { Text("e.g. 500", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = currentSkin.primaryColor,
                                        unfocusedBorderColor = currentSkin.secondaryColor.copy(alpha = 0.5f),
                                        focusedContainerColor = Color.Black.copy(alpha = 0.65f),
                                        unfocusedContainerColor = Color.Black.copy(alpha = 0.45f)
                                    ),
                                    textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CyberTextButton(
                                        onClick = { isListingMode = false },
                                        text = "BACK",
                                        skin = currentSkin,
                                        containerColor = ElegantIndigo,
                                        contentColor = Color.White,
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.weight(1f)
                                    )

                                    CyberTextButton(
                                        onClick = {
                                            val price = listPriceInput.toIntOrNull() ?: 0
                                            viewModel.listCardOnMarket(mintedCard.id, price)
                                            selectedMintedCard = null
                                        },
                                        text = "EXECUTE LIST",
                                        skin = currentSkin,
                                        containerColor = NeonCyan,
                                        contentColor = CyberObsidian,
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.weight(1.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedMintedCard = null }) {
                    Text("CLOSE", color = NeonCyan, fontFamily = RajdhaniFontFamily)
                }
            }
        )
    }

    // Modal Fullscreen card review overlay for VaultScreen
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
                .padding(16.dp),
            containerColor = Color(0xFF070812),
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
                TextButton(onClick = { showLunarCycleModal = false }) {
                    Text(
                        text = if (lang == "ru") "ЗАКРЫТЬ" else "CLOSE",
                        color = NeonCyan,
                        fontFamily = RajdhaniFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}

// ==========================================
// 3. NEURAL MARKET & MINER (Exchange Screen)
// ==========================================
@Composable
fun MarketScreen(viewModel: TarotViewModel) {
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val lang = profile?.systemLanguage ?: "en"
    val listings by viewModel.filteredMarketListings.collectAsStateWithLifecycle()
    val searchBy by viewModel.marketQuery.collectAsStateWithLifecycle()
    val filterByRarity by viewModel.marketRarityFilter.collectAsStateWithLifecycle()
    val transactionLogs by viewModel.transactionLogs.collectAsStateWithLifecycle()
    val miningCooldown by viewModel.miningCooldown.collectAsStateWithLifecycle()

    var cardToBuyConfirm by remember { mutableStateOf<MarketListing?>(null) }
    var cardToInspectReview by remember { mutableStateOf<MarketListing?>(null) }

    val rarities = listOf("All", "Common", "Rare", "Epic", "Legend")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 54.dp)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "EXCHANGE",
                fontSize = 24.sp,
                color = NeonGold,
                fontFamily = RajdhaniFontFamily,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("exchange_title")
            )
            Text(
                text = "P2P Transactions",
                fontSize = 16.sp,
                color = Color(0xFFB0B0C5),
                fontFamily = RajdhaniFontFamily,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            // Dynamic CORE NODE HARVESTER / Miner
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "HARVESTER",
                                color = NeonGreen,
                                fontSize = 17.sp,
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Harvest Weed credits",
                                color = Color(0xFFB0B0C5),
                                fontSize = 15.sp,
                                fontFamily = RajdhaniFontFamily
                            )
                        }

                        if (miningCooldown > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(NeonGold.copy(alpha = 0.15f))
                                    .border(1.dp, NeonGold, RoundedCornerShape(24.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "00:${String.format("%02d", miningCooldown)} COOLDOWN",
                                    fontSize = 15.sp,
                                    color = NeonGold,
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            CyberButton(
                                onClick = { viewModel.startMining() },
                                skin = currentSkin,
                                containerColor = NeonGreen,
                                contentColor = CyberObsidian,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = CyberObsidian,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    AutoResizingText(
                                        text = "HARVEST (+350 N$)",
                                        targetTextSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = RajdhaniFontFamily,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    if (miningCooldown > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { (30f - miningCooldown.toFloat()) / 30f },
                            color = NeonGreen,
                            trackColor = CyberMutedBlue,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Search and filters
            OutlinedTextField(
                value = searchBy,
                onValueChange = { viewModel.setMarketQuery(it) },
                placeholder = { Text("Search by name, traditional, or seller...", color = Color.White.copy(alpha = 0.65f), fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = currentSkin.primaryColor, modifier = Modifier.size(20.dp)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = currentSkin.primaryColor,
                    unfocusedBorderColor = currentSkin.secondaryColor.copy(alpha = 0.5f),
                    focusedContainerColor = Color.Black.copy(alpha = 0.65f),
                    unfocusedContainerColor = Color.Black.copy(alpha = 0.45f)
                ),
                textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("market_search_input"),
                shape = RoundedCornerShape(12.dp)
            )

            // Rarity horizontally scrolling row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rarities.forEach { r ->
                    val isSelected = filterByRarity == r
                    val badgeColor = when (r) {
                        "Legend" -> NeonGold
                        "Epic" -> NeonMagenta
                        "Rare" -> NeonCyan
                        "Common" -> NeonGreen
                        else -> Color.White
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (isSelected) badgeColor.copy(alpha = 0.2f) else CyberCharcoal)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) badgeColor else CyberMutedBlue,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .clickable { viewModel.setMarketRarityFilter(r) }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = r.uppercase(),
                            fontSize = 15.sp,
                            color = if (isSelected) badgeColor else Color(0xFFB0B0C5),
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Active Listings Grid/List
        if (listings.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(imageVector = Icons.Default.Storefront, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "NO TRANSACTIONS IN DOCK",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        color = Color(0xFFB0B0C5),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            items(listings) { listing ->
                val card = CyberTarotDeck.getCard(listing.cardIndex)
                val rarityColor = remember(listing.rarity) {
                    when (listing.rarity) {
                        "Legend" -> NeonGold
                        "Epic" -> NeonMagenta
                        "Rare" -> NeonCyan
                        else -> NeonGreen
                    }
                }

                val cardBgBrush = Brush.horizontalGradient(
                    colors = listOf(
                        rarityColor.copy(alpha = 0.22f),
                        Color(0xFF0F0E24).copy(alpha = 0.95f)
                    )
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(2.5.dp, rarityColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                        .background(cardBgBrush, RoundedCornerShape(12.dp))
                        .clickable { cardToInspectReview = listing }
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        CyberRarityShaderOverlay(
                            rarity = listing.rarity,
                            modifier = Modifier
                                .matchParentSize()
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Card Illustration with Unified Tarot Frame & WEED GURU PROPERTY Watermark
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(225.dp)
                                    .padding(horizontal = 10.dp, vertical = 10.dp)
                            ) {
                                FramedTarotArtworkWindow(
                                    card = card,
                                    isUpright = true,
                                    compactWatermark = false,
                                    showDiagonalWatermark = true,
                                    rarityBorderColor = rarityColor,
                                    cornerRadius = 10,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(8.dp)
                                        .background(Color.Black.copy(alpha = 0.80f), RoundedCornerShape(6.dp))
                                        .border(0.9.dp, NeonGold.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "${getArcanaRomanNumeral(card.index)} • ${card.series.uppercase()}",
                                        fontFamily = OrbitronFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonGold
                                    )
                                }
                            }

                            // Labels are below, and do not compete for width
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CyberRarityGlowBadge(
                                            rarity = listing.rarity
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        ContrastText(
                                            text = "#${String.format("%04d", listing.mintNumber)}",
                                            fontSize = 17.sp,
                                            fontFamily = RajdhaniFontFamily,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    ContrastText(
                                        text = "${listing.price} N$",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = RajdhaniFontFamily,
                                        color = NeonGold
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                val localizedName = CardLocalizationHelper.getLocalizedName(card.index, lang)
                                ContrastText(
                                    text = localizedName.uppercase(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = RajdhaniFontFamily,
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                ContrastText(
                                    text = "Seller: ${listing.sellerName}",
                                    fontSize = 16.sp,
                                    fontFamily = RajdhaniFontFamily,
                                    color = NeonGold,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                if (listing.sellerName == "User (You)") {
                                    CyberTextButton(
                                        onClick = { viewModel.cancelCardListing(listing.cardIndex, listing.mintNumber) },
                                        text = "DELIST",
                                        skin = currentSkin,
                                        containerColor = NeonRed,
                                        contentColor = Color.White,
                                        shape = RoundedCornerShape(12.dp),
                                        targetTextSize = 12.sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                    )
                                } else {
                                    CyberTextButton(
                                        onClick = { cardToBuyConfirm = listing },
                                        text = "BUY NFT",
                                        skin = currentSkin,
                                        containerColor = NeonCyan,
                                        contentColor = CyberObsidian,
                                        shape = RoundedCornerShape(12.dp),
                                        targetTextSize = 12.sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Transaction Logs Title
        item {
            HorizontalDivider(color = CyberMutedBlue, modifier = Modifier.padding(vertical = 16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Terminal, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "NEURAL LEDGER SYNCED",
                    fontSize = 16.sp,
                    color = NeonCyan,
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Transaction history entries
        if (transactionLogs.isEmpty()) {
            item {
                Text(
                    "Initializing ledger sync records...",
                    fontSize = 15.sp,
                    fontFamily = RajdhaniFontFamily,
                    color = Color.DarkGray
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
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "[$dateString] ${log.type}: $cardName -> ${log.targetParty}",
                        fontSize = 14.sp,
                        fontFamily = RajdhaniFontFamily,
                        color = when (log.type) {
                            "BUY" -> NeonCyan
                            "SELL" -> NeonGold
                            "MINT" -> NeonMagenta
                            "MINE" -> NeonGreen
                            else -> Color.Gray
                        },
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${if (log.type == "BUY" || log.type == "MINT") "-" else "+"}${log.price} N$",
                        fontSize = 14.sp,
                        fontFamily = RajdhaniFontFamily,
                        color = if (log.type == "BUY" || log.type == "MINT") NeonRed else NeonGreen
                    )
                }
            }
        }
    }

    // Modal Overlays for Market Screen
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

    cardToBuyConfirm?.let { listing ->
        val card = CyberTarotDeck.getCard(listing.cardIndex)
        AlertDialog(
            onDismissRequest = { cardToBuyConfirm = null },
            containerColor = CyberCharcoal,
            title = {
                Text(
                    text = "AUTHORIZE TRANSACTION",
                    color = NeonGold,
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Do you want to authorize secure credit escrow transfer for this node?",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF09090E), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "NODE: ${card.name.uppercase()}",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 16.sp,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "SERIAL: #${String.format("%04d", listing.mintNumber)}",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 15.sp,
                                color = Color(0xFFB0B0C5)
                            )
                            Text(
                                text = "RARITY: ${listing.rarity.uppercase()}",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 15.sp,
                                color = when (listing.rarity) {
                                    "Legend" -> NeonGold
                                    "Epic" -> NeonMagenta
                                    "Rare" -> NeonCyan
                                    else -> NeonGreen
                                }
                            )
                            HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "ESCROW DEBT:",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 16.sp,
                                    color = Color(0xFFB0B0C5)
                                )
                                Text(
                                    "${listing.price} N$",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 16.sp,
                                    color = NeonGold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                CyberTextButton(
                    onClick = {
                        viewModel.buyCardFromMarket(listing)
                        cardToBuyConfirm = null
                    },
                    text = "CONFIRM DECREE",
                    skin = currentSkin,
                    containerColor = NeonCyan,
                    contentColor = CyberObsidian,
                    shape = RoundedCornerShape(12.dp)
                )
            },
            dismissButton = {
                CyberTextButton(
                    onClick = { cardToBuyConfirm = null },
                    text = "ABORT",
                    skin = currentSkin,
                    containerColor = Color.Transparent,
                    contentColor = Color.Gray,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        )
    }
}
