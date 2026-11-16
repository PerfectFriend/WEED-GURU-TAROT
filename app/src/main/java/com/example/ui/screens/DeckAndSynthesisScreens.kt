package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.*
import com.example.ui.components.WeedGuruTarotCard
import com.example.ui.theme.*
import com.example.viewmodel.TarotViewModel

fun getCardImage(cardIndex: Int): Int? {
    return when (cardIndex) {
        0 -> R.drawable.img_card_00_sprout_1794505405019
        1 -> R.drawable.img_card_01_budtender_v2_1794566668115
        2 -> R.drawable.img_card_02_hash_queen_1794505432186
        3 -> R.drawable.img_card_03_mother_plant_1794505443960
        4 -> R.drawable.img_card_04_grow_master_1794505457551
        5 -> R.drawable.img_card_05_old_grower_1794505471438
        6 -> R.drawable.img_card_06_terpene_symbiosis_1794505485665
        7 -> R.drawable.img_card_07_autoflower_rush_1794505499624
        8 -> R.drawable.img_card_08_super_cropping_1794505513012
        9 -> R.drawable.img_card_09_curing_room_1794505526829
        10 -> R.drawable.img_card_10_pheno_hunt_1794505539718
        11 -> R.drawable.img_card_11_organic_balance_1794505555778
        12 -> R.drawable.img_card_12_flushing_stage_1794566585071
        13 -> R.drawable.img_card_13_harvest_1794566585432
        14 -> R.drawable.img_card_14_climate_controller_1794566587382
        15 -> R.drawable.img_card_15_spider_mite_attack_v2_1794566668223
        16 -> R.drawable.img_card_16_crop_collapse_1794566586939
        17 -> R.drawable.img_card_17_trichome_sparkle_1794566603781
        18 -> R.drawable.img_card_18_guerrilla_growing_1794566605208
        19 -> R.drawable.img_card_19_grow_light_1794566604902
        20 -> R.drawable.img_card_20_cup_submission_1794566605638
        21 -> R.drawable.img_card_21_green_planet_1794566606404
        22 -> R.drawable.img_card_22_genesis_electron_1794566622200
        23 -> R.drawable.img_card_23_genesis_proton_1794566623656
        24 -> R.drawable.img_card_24_genesis_hydrogen_1794566623123
        25 -> R.drawable.img_card_25_genesis_dna_1794566624227
        26 -> R.drawable.img_card_26_genesis_singularity_1794566625883
        27 -> R.drawable.img_card_27_amber_resin_1794566645693
        28 -> R.drawable.img_card_28_amber_trichomes_1794566646336
        29 -> R.drawable.img_card_29_ocean_coral_1794566646598
        30 -> R.drawable.img_card_30_ocean_leviathan_v2_1794566668019
        else -> null
    }
}

fun getCardBackImage(series: String): Int {
    return when (series) {
        "Genesis Series" -> R.drawable.back_genesis_series_1792151368530
        "Amber Series" -> R.drawable.back_amber_series_1792151382337
        "Seas & Oceans" -> R.drawable.back_ocean_series_1792151396531
        else -> R.drawable.back_cyber_botanica_1792151356014
    }
}

@Composable
fun CardSymbolIcon(cardIndex: Int, tintColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when (cardIndex) {
            0 -> Icon(Icons.Default.Spa, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            1 -> Icon(Icons.Default.Science, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            2 -> Icon(Icons.Default.AcUnit, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            3 -> Icon(Icons.Default.Eco, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            4 -> Icon(Icons.Default.Build, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            5 -> Icon(Icons.Default.Agriculture, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            6 -> Icon(Icons.Default.Favorite, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            7 -> Icon(Icons.Default.Speed, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            8 -> Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            9 -> Icon(Icons.Default.FilterVintage, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            10 -> Icon(Icons.Default.Widgets, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            11 -> Icon(Icons.Default.DoneAll, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            12 -> Icon(Icons.Default.WaterDrop, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            13 -> Icon(Icons.Default.ContentCut, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            14 -> Icon(Icons.Default.Thermostat, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            15 -> Icon(Icons.Default.BugReport, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            16 -> Icon(Icons.Default.FlashOff, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            17 -> Icon(Icons.Default.Star, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            18 -> Icon(Icons.Default.Bedtime, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            19 -> Icon(Icons.Default.WbSunny, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            20 -> Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            21 -> Icon(Icons.Default.Public, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            22 -> Icon(Icons.Default.Bolt, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            23 -> Icon(Icons.Default.Adjust, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            24 -> Icon(Icons.Default.BubbleChart, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            25 -> Icon(Icons.Default.Biotech, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            26 -> Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            27 -> Icon(Icons.Default.Diamond, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            28 -> Icon(Icons.Default.WaterDrop, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            29 -> Icon(Icons.Default.Waves, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            30 -> Icon(Icons.Default.Tsunami, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            else -> Icon(Icons.Default.Eco, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
        }
    }
}

fun Modifier.neonGlowEffect(
    color: Color,
    radius: Dp,
    pulseFactor: Float = 1.0f,
    cornerRadius: Dp = 12.dp
): Modifier = drawBehind {
    val sizePx = size
    val radiusPx = radius.toPx() * pulseFactor
    val layers = 4
    for (i in 1..layers) {
        val fraction = i.toFloat() / layers
        val currentRadius = radiusPx * fraction
        val alpha = 0.18f * (1.0f - fraction)
        drawRoundRect(
            color = color.copy(alpha = alpha),
            size = sizePx,
            cornerRadius = CornerRadius(cornerRadius.toPx()),
            style = Stroke(width = currentRadius)
        )
    }
}

@Composable
fun DeckScreen(viewModel: TarotViewModel) {
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val lang = profile?.systemLanguage ?: "en"
    val mintedCards by viewModel.mintedCards.collectAsStateWithLifecycle()
    val marketListings by viewModel.marketListings.collectAsStateWithLifecycle()

    var selectedInspectCard by remember { mutableStateOf<CyberTarotCard?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedRarityFilter by remember { mutableStateOf("All") }
    var selectedSeriesFilter by remember { mutableStateOf("All Series") }
    val flippedCardsMap = remember { mutableStateMapOf<Int, Boolean>() }

    var currentDeckSubTab by remember { mutableStateOf(0) } // 0: Registry, 1: Builder, 2: Forge, 3: Duel, 4: Analytics

    // Interactive CSS Grid simulation controls
    var columnCount by remember { mutableStateOf(2) }
    var gridGap by remember { mutableStateOf(10f) }
    var glowSpread by remember { mutableStateOf(6f) }
    var glowColorPreset by remember { mutableStateOf("Rarity Matched") }
    var presetStyle by remember { mutableStateOf("Standard CSS Grid") }
    var infoMode by remember { mutableStateOf("Digital Analog") }
    var showGridConfig by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseFactor by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_anim"
    )

    val filteredCards = remember(searchQuery, selectedRarityFilter, selectedSeriesFilter, lang) {
        CyberTarotDeck.cards.filter { card ->
            val localizedName = CardLocalizationHelper.getLocalizedName(card.index, lang)
            val localizedTraditional = CardLocalizationHelper.getLocalizedTraditionalName(card.index, lang)
            val localizedDesc = CardLocalizationHelper.getLocalizedDescription(card.index, lang)
            val matchesSearch = localizedName.contains(searchQuery, ignoreCase = true) ||
                localizedTraditional.contains(searchQuery, ignoreCase = true) ||
                localizedDesc.contains(searchQuery, ignoreCase = true)
            val matchesRarity = selectedRarityFilter == "All" || card.defaultRarity.equals(selectedRarityFilter, ignoreCase = true)
            val matchesSeries = selectedSeriesFilter == "All Series" || card.series.equals(selectedSeriesFilter, ignoreCase = true)
            matchesSearch && matchesRarity && matchesSeries
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 54.dp)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (currentDeckSubTab == 0) {
            Text(
                text = "DECK",
                fontSize = 24.sp,
                color = NeonMagenta,
                fontFamily = RajdhaniFontFamily,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("deck_title")
            )
            Text(
                text = "Rider-Waite Symbols",
                fontSize = 16.sp,
                color = Color(0xFFB0B0C5),
                fontFamily = RajdhaniFontFamily,
                modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(4.dp))
        }

        // SubTab Selection Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .background(Color(0xFF09090F), RoundedCornerShape(24.dp))
                .border(1.dp, Color(0x1F7F7F7F), RoundedCornerShape(24.dp))
                .padding(3.dp)
        ) {
            val tabs = listOf(
                Pair(0, "REGISTRY"),
                Pair(1, "BUILDER"),
                Pair(2, "FORGE"),
                Pair(3, "DUEL"),
                Pair(4, "ANALYTICS")
            )
            val tabColors = listOf(
                NeonMagenta,
                Color(0xFF00F5FF),
                Color(0xFFFFD700),
                Color(0xFF00FF66),
                NeonCyan
            )

            tabs.forEachIndexed { idx, (tabIndex, title) ->
                val isSelected = currentDeckSubTab == tabIndex
                val color = tabColors[idx]
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) color.copy(alpha = 0.2f) else Color.Transparent)
                        .border(1.dp, if (isSelected) color else Color.Transparent, RoundedCornerShape(16.dp))
                        .clickable { currentDeckSubTab = tabIndex }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        title,
                        color = if (isSelected) Color.White else Color(0xFFB0B0C5),
                        fontFamily = RajdhaniFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        if (currentDeckSubTab == 0) {
            // Registry controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Cyber Deck", fontFamily = RajdhaniFontFamily) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedLabelColor = NeonCyan,
                        unfocusedLabelColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 16.sp),
                    modifier = Modifier.weight(1f)
                )

                // Toggle CSS Grid Config Panel
                IconButton(
                    onClick = { showGridConfig = !showGridConfig },
                    modifier = Modifier
                        .size(54.dp)
                        .background(if (showGridConfig) NeonCyan.copy(alpha = 0.15f) else CyberCharcoal, RoundedCornerShape(8.dp))
                        .border(1.dp, if (showGridConfig) NeonCyan else Color.DarkGray, RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Grid System Config",
                        tint = if (showGridConfig) NeonCyan else Color.White
                    )
                }
            }

            // Expanded CSS Grid Config Panel
            AnimatedVisibility(
                visible = showGridConfig,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A14)),
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "CSS GRID ENGINE CONFIGURATION",
                                color = NeonCyan,
                                fontSize = 14.sp,
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                TextButton(
                                    onClick = {
                                        CyberTarotDeck.cards.forEach { card ->
                                            flippedCardsMap[card.index] = true
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("FLIP ALL", fontSize = 17.sp, color = NeonGreen, fontFamily = RajdhaniFontFamily, fontWeight = FontWeight.Bold)
                                }
                                TextButton(
                                    onClick = {
                                        flippedCardsMap.clear()
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("LOCK ALL", fontSize = 17.sp, color = NeonRed, fontFamily = RajdhaniFontFamily, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "grid-template-columns: repeat(${columnCount}, 1fr);",
                            color = Color.LightGray,
                            fontSize = 14.sp,
                            fontFamily = RajdhaniFontFamily
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(1, 2, 3, 4).forEach { col ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (columnCount == col) NeonCyan.copy(alpha = 0.2f) else CyberCharcoal, RoundedCornerShape(4.dp))
                                        .border(1.dp, if (columnCount == col) NeonCyan else Color.DarkGray, RoundedCornerShape(4.dp))
                                        .clickable { columnCount = col }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("${col} Col", fontSize = 17.sp, color = if (columnCount == col) NeonCyan else Color(0xFFB0B0C5), fontFamily = RajdhaniFontFamily, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "grid-gap: ${gridGap.toInt()}dp;",
                                    color = Color.LightGray,
                                    fontSize = 16.sp,
                                    fontFamily = RajdhaniFontFamily
                                )
                                Slider(
                                    value = gridGap,
                                    onValueChange = { gridGap = it },
                                    valueRange = 4f..24f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = NeonCyan,
                                        activeTrackColor = NeonCyan,
                                        inactiveTrackColor = Color.DarkGray
                                    ),
                                    modifier = Modifier.height(24.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "box-shadow: ${glowSpread.toInt()}dp glow;",
                                    color = Color.LightGray,
                                    fontSize = 16.sp,
                                    fontFamily = RajdhaniFontFamily
                                )
                                Slider(
                                    value = glowSpread,
                                    onValueChange = { glowSpread = it },
                                    valueRange = 2f..14f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = NeonCyan,
                                        activeTrackColor = NeonCyan,
                                        inactiveTrackColor = Color.DarkGray
                                    ),
                                    modifier = Modifier.height(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("GRID-STYLE", color = Color(0xFFB0B0C5), fontSize = 16.sp, fontFamily = RajdhaniFontFamily)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf("Standard CSS Grid", "Neon Matrix Grid", "Holographic Command Table").forEach { style ->
                                        val isSel = presetStyle == style
                                        val abbr = when (style) {
                                            "Standard CSS Grid" -> "CSS"
                                            "Neon Matrix Grid" -> "Neon"
                                            else -> "Holo"
                                        }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(if (isSel) NeonMagenta.copy(alpha = 0.2f) else CyberCharcoal, RoundedCornerShape(4.dp))
                                                .border(1.dp, if (isSel) NeonMagenta else Color.DarkGray, RoundedCornerShape(4.dp))
                                                .clickable { presetStyle = style }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(abbr, fontSize = 16.sp, color = if (isSel) NeonMagenta else Color(0xFFB0B0C5), fontFamily = RajdhaniFontFamily)
                                        }
                                    }
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text("GLOW-COLOR", color = Color(0xFFB0B0C5), fontSize = 16.sp, fontFamily = RajdhaniFontFamily)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf("Rarity Matched", "Cyber Cyan", "Sunset Magenta").forEach { colorPreset ->
                                        val isSel = glowColorPreset == colorPreset
                                        val abbr = when (colorPreset) {
                                            "Rarity Matched" -> "Rare"
                                            "Cyber Cyan" -> "Cyan"
                                            else -> "Mage"
                                        }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(if (isSel) NeonGreen.copy(alpha = 0.2f) else CyberCharcoal, RoundedCornerShape(4.dp))
                                                .border(1.dp, if (isSel) NeonGreen else Color.DarkGray, RoundedCornerShape(4.dp))
                                                .clickable { glowColorPreset = colorPreset }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(abbr, fontSize = 16.sp, color = if (isSel) NeonGreen else Color(0xFFB0B0C5), fontFamily = RajdhaniFontFamily)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("CARD DISPLAY MODE:", color = Color(0xFFB0B0C5), fontSize = 16.sp, fontFamily = RajdhaniFontFamily)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Digital Analog", "Traditional Rider-Waite").forEach { mode ->
                                    val isSel = infoMode == mode
                                    Box(
                                        modifier = Modifier
                                            .background(if (isSel) ElegantViolet.copy(alpha = 0.3f) else CyberCharcoal, RoundedCornerShape(4.dp))
                                            .border(1.dp, if (isSel) ElegantViolet else Color.DarkGray, RoundedCornerShape(4.dp))
                                            .clickable { infoMode = mode }
                                            .padding(horizontal = 10.dp, vertical = 8.dp)
                                    ) {
                                        Text(mode, fontSize = 16.sp, color = if (isSel) Color.White else Color(0xFFB0B0C5), fontFamily = RajdhaniFontFamily)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Series Selection Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All Series", "Cyber Botanica", "Genesis Series", "Amber Series", "Seas & Oceans").forEach { seriesName ->
                    val isSelected = selectedSeriesFilter == seriesName
                    val seriesColor = when (seriesName) {
                        "Genesis Series" -> Color(0xFFFFD700)
                        "Amber Series" -> Color(0xFFFF9900)
                        "Seas & Oceans" -> NeonCyan
                        "Cyber Botanica" -> NeonGreen
                        else -> Color.White
                    }
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) seriesColor.copy(alpha = 0.25f) else CyberCharcoal,
                                RoundedCornerShape(16.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) seriesColor else Color.DarkGray,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedSeriesFilter = seriesName }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = seriesName.uppercase(),
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 15.sp,
                            color = if (isSelected) seriesColor else Color(0xFFB0B0C5),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Scrollable Rarity Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Common", "Rare", "Epic", "Legend", "Genesis").forEach { rarity ->
                    val isSelected = selectedRarityFilter == rarity
                    val filterColor = when (rarity) {
                        "Genesis" -> Color(0xFFFFD700)
                        "Legend" -> NeonGold
                        "Epic" -> NeonMagenta
                        "Rare" -> NeonCyan
                        "Common" -> NeonGreen
                        else -> Color.White
                    }
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) filterColor.copy(alpha = 0.2f) else CyberCharcoal,
                                RoundedCornerShape(16.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) filterColor else Color.DarkGray,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedRarityFilter = rarity }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = rarity.uppercase(),
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 15.sp,
                            color = if (isSelected) filterColor else Color(0xFFB0B0C5),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // CSS Grid Matrix Container
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (presetStyle == "Holographic Command Table") {
                            Color(0xFF030307)
                        } else Color.Transparent
                    )
                    .drawBehind {
                        if (presetStyle == "Holographic Command Table") {
                            val gridSpacing = 60.dp.toPx()
                            val width = size.width
                            val height = size.height

                            var x = 0f
                            while (x < width) {
                                drawLine(
                                    color = NeonCyan.copy(alpha = 0.08f),
                                    start = Offset(x, 0f),
                                    end = Offset(x, height),
                                    strokeWidth = 1f
                                )
                                x += gridSpacing
                            }

                            var y = 0f
                            while (y < height) {
                                drawLine(
                                    color = NeonCyan.copy(alpha = 0.08f),
                                    start = Offset(0f, y),
                                    end = Offset(width, y),
                                    strokeWidth = 1f
                                )
                                y += gridSpacing
                            }
                        }
                    }
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columnCount),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    horizontalArrangement = Arrangement.spacedBy(gridGap.dp),
                    verticalArrangement = Arrangement.spacedBy(gridGap.dp)
                ) {
                    items(filteredCards) { card ->
                        val rarityColor = remember(card.defaultRarity) {
                            when (card.defaultRarity) {
                                "Genesis" -> NeonGold
                                "Legend" -> NeonAmber
                                "Epic" -> NeonMagenta
                                "Rare" -> NeonCyan
                                else -> NeonGreen
                            }
                        }

                        val chosenGlowColor = remember(glowColorPreset, card.defaultRarity) {
                            when (glowColorPreset) {
                                "Cyber Cyan" -> NeonCyan
                                "Matrix Green" -> NeonGreen
                                "Sunset Magenta" -> NeonMagenta
                                "Royal Gold" -> NeonGold
                                else -> rarityColor
                            }
                        }

                        val isFlipped = flippedCardsMap[card.index] ?: false
                        val cardOuterGlow = if (presetStyle == "Neon Matrix Grid") glowSpread.dp * 1.5f else glowSpread.dp

                        WeedGuruTarotCard(
                            card = card,
                            isUpright = true,
                            isFlipped = isFlipped,
                            compact = columnCount >= 3,
                            systemLanguage = lang,
                            showTraditionalAsPrimary = infoMode != "Digital Analog",
                            showOrientationBadge = false,
                            showRarityShader = presetStyle != "Standard CSS Grid",
                            glowColorOverride = chosenGlowColor,
                            onFlipClick = {
                                flippedCardsMap[card.index] = !isFlipped
                                com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.CLICK)
                            },
                            onCardClick = {
                                selectedInspectCard = card
                                com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.CARD_DRAW)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .neonGlowEffect(
                                    color = if (isFlipped) chosenGlowColor else chosenGlowColor.copy(alpha = 0.35f),
                                    radius = cardOuterGlow,
                                    pulseFactor = if (presetStyle == "Neon Matrix Grid") pulseFactor else 1.0f,
                                    cornerRadius = 12.dp
                                )
                        )
                    }
                }
            }
        } else if (currentDeckSubTab == 1) {
            com.example.deckforge.DeckBuilderScreen(
                viewModel = viewModel,
                onLaunchDuel = { currentDeckSubTab = 3 }
            )
        } else if (currentDeckSubTab == 2) {
            com.example.deckforge.CardForgeScreen(
                viewModel = viewModel
            )
        } else if (currentDeckSubTab == 3) {
            com.example.duel.DuelArenaScreen(
                viewModel = viewModel,
                onBack = { currentDeckSubTab = 0 }
            )
        } else {
            NeuralAnalyticsView(mintedCards = mintedCards, marketListings = marketListings)
        }
    }

    // Fullscreen Card Review for selected inspect card (frameless + card back support)
    selectedInspectCard?.let { card ->
        FullScreenCardReview(
            card = card,
            mintedCard = mintedCards.find { it.cardIndex == card.index },
            isMarketView = false,
            systemLanguage = lang,
            skin = currentSkin,
            initiallyFlipped = flippedCardsMap[card.index] ?: false,
            onClose = { selectedInspectCard = null },
            onActionClick = { selectedInspectCard = null }
        )
    }
}

@Composable
fun CardSynthesisView(viewModel: TarotViewModel) {
    val mintedCards by viewModel.mintedCards.collectAsStateWithLifecycle()
    val unlistedOwned = remember(mintedCards) { mintedCards.filter { !it.isListed } }

    val commonCards = remember(unlistedOwned) { unlistedOwned.filter { it.rarity.equals("Common", ignoreCase = true) } }
    val rareCards = remember(unlistedOwned) { unlistedOwned.filter { it.rarity.equals("Rare", ignoreCase = true) } }
    val epicCards = remember(unlistedOwned) { unlistedOwned.filter { it.rarity.equals("Epic", ignoreCase = true) } }

    var isFusing by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0B18)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, NeonGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NeonGold, modifier = Modifier.size(24.dp))
                        Text(
                            "SYNTHESIS MATRIX / СИНТЕЗ КАРТ",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGold,
                            fontFamily = RajdhaniFontFamily
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Fuse lower rarity cards into higher tiers according to the Genesis Formula. 10 Epic cards fuse into 1 Gold Genesis Series card ('Начало Начал').",
                        fontSize = 16.sp,
                        color = Color.LightGray,
                        fontFamily = RajdhaniFontFamily,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // 1. Common -> Rare (4 Commons -> 1 Rare)
        item {
            SynthesisRecipeCard(
                title = "COMMON ➔ RARE",
                subtitle = "4 Common Cards ➔ 1 Rare NFT Card",
                ownedCount = commonCards.size,
                requiredCount = 4,
                accentColor = NeonGreen,
                targetColor = NeonCyan,
                buttonText = "FUSE 4 COMMONS ➔ 1 RARE",
                isFusing = isFusing,
                onFuse = {
                    isFusing = true
                    viewModel.fuseCards(TarotRepository.FusionType.COMMON_TO_RARE) {
                        isFusing = false
                    }
                }
            )
        }

        // 2. Rare -> Epic (4 Rares -> 1 Epic)
        item {
            SynthesisRecipeCard(
                title = "RARE ➔ EPIC",
                subtitle = "4 Rare Cards ➔ 1 Epic NFT Card",
                ownedCount = rareCards.size,
                requiredCount = 4,
                accentColor = NeonCyan,
                targetColor = NeonMagenta,
                buttonText = "FUSE 4 RARES ➔ 1 EPIC",
                isFusing = isFusing,
                onFuse = {
                    isFusing = true
                    viewModel.fuseCards(TarotRepository.FusionType.RARE_TO_EPIC) {
                        isFusing = false
                    }
                }
            )
        }

        // 3. Epic -> Legend (4 Epics -> 1 Legend)
        item {
            SynthesisRecipeCard(
                title = "EPIC ➔ LEGEND",
                subtitle = "4 Epic Cards ➔ 1 Legend NFT Card",
                ownedCount = epicCards.size,
                requiredCount = 4,
                accentColor = NeonMagenta,
                targetColor = NeonGold,
                buttonText = "FUSE 4 EPICS ➔ 1 LEGEND",
                isFusing = isFusing,
                onFuse = {
                    isFusing = true
                    viewModel.fuseCards(TarotRepository.FusionType.EPIC_TO_LEGEND) {
                        isFusing = false
                    }
                }
            )
        }

        // 4. Epic -> Genesis Gold (10 Epics -> 1 Genesis Gold)
        item {
            SynthesisRecipeCard(
                title = "EPIC ➔ GENESIS GOLD (НАЧАЛО НАЧАЛ)",
                subtitle = "10 Epic Cards ➔ 1 Genesis Gold Card (#22 Electron, #23 Proton, #24 Hydrogen...)",
                ownedCount = epicCards.size,
                requiredCount = 10,
                accentColor = NeonMagenta,
                targetColor = Color(0xFFFFD700),
                buttonText = "FUSE 10 EPICS ➔ 1 GENESIS GOLD",
                isFusing = isFusing,
                onFuse = {
                    isFusing = true
                    viewModel.fuseCards(TarotRepository.FusionType.EPIC_TO_GENESIS) {
                        isFusing = false
                    }
                }
            )
        }
    }
}

@Composable
fun SynthesisRecipeCard(
    title: String,
    subtitle: String,
    ownedCount: Int,
    requiredCount: Int,
    accentColor: Color,
    targetColor: Color,
    buttonText: String,
    isFusing: Boolean,
    onFuse: () -> Unit
) {
    val canFuse = ownedCount >= requiredCount && !isFusing

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF12121A)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (canFuse) targetColor else Color(0x33FFFFFF)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = targetColor,
                    fontFamily = RajdhaniFontFamily
                )
                Text(
                    "AVAILABLE: $ownedCount / $requiredCount",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (canFuse) NeonGreen else Color(0xFFB0B0C5),
                    fontFamily = RajdhaniFontFamily
                )
            }

            Text(
                subtitle,
                fontSize = 15.sp,
                color = Color.LightGray,
                fontFamily = RajdhaniFontFamily
            )

            Button(
                onClick = onFuse,
                enabled = canFuse,
                colors = ButtonDefaults.buttonColors(
                    containerColor = targetColor,
                    contentColor = Color.Black,
                    disabledContainerColor = Color(0xFF222230),
                    disabledContentColor = Color.DarkGray
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
            ) {
                Text(
                    if (isFusing) "SYNTHESIZING..." else buttonText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = RajdhaniFontFamily
                )
            }
        }
    }
}
