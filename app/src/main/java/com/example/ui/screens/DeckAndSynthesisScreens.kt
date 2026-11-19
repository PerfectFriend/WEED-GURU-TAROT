package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tsunami
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.audio.CyberNoirSynthPlayer
import com.example.data.CardLocalizationHelper
import com.example.data.CyberTarotCard
import com.example.data.CyberTarotDeck
import com.example.deckforge.CardForgeScreen
import com.example.deckforge.DeckBuilderScreen
import com.example.duel.DuelArenaScreen
import com.example.ui.components.CyberChip
import com.example.ui.components.Tarot420Discipline
import com.example.ui.components.WeedGuruTarotCard
import com.example.ui.components.getCard420Discipline
import com.example.ui.theme.Cyber
import com.example.ui.theme.CyberShapes
import com.example.ui.theme.ElegantViolet
import com.example.ui.theme.IbmPlexMonoFontFamily
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonRed
import com.example.ui.theme.Space
import com.example.ui.theme.rememberMotionEnabled
import com.example.viewmodel.TarotViewModel

val UNDER_CONSTRUCTION_CARD_INDICES: Set<Int> = emptySet()

fun isCardUnderConstruction(cardIndex: Int): Boolean = cardIndex in UNDER_CONSTRUCTION_CARD_INDICES

fun getCardImage(cardIndex: Int): Int? {
    if (cardIndex in UNDER_CONSTRUCTION_CARD_INDICES) {
        return R.drawable.card_under_construction_1795033576640
    }
    return when (cardIndex) {
        0 -> R.drawable.v3_card_00_sprout_1795034585398
        1 -> R.drawable.v3_card_01_budtender_1795034532094
        2 -> R.drawable.v3_card_02_hash_queen_1795034588250
        3 -> R.drawable.v3_card_03_mother_plant_1795034589605
        4 -> R.drawable.v3_card_04_grow_master_1795034531643
        5 -> R.drawable.v3_card_05_old_grower_1795034503983
        6 -> R.drawable.v3_card_06_symbiosis_1795034505993
        7 -> R.drawable.v3_card_07_autoflower_rush_1795034535201
        8 -> R.drawable.v3_card_08_super_cropping_1795034589276
        9 -> R.drawable.v3_card_09_curing_room_1795034589459
        10 -> R.drawable.v3_card_10_pheno_hunt_1795034591568
        11 -> R.drawable.v3_card_11_organic_balance_1795034533688
        12 -> R.drawable.v3_card_12_flushing_stage_1795034610844
        13 -> R.drawable.v3_card_13_harvest_1795034612924
        14 -> R.drawable.v3_card_14_climate_ctrl_1795034504035
        15 -> R.drawable.v3_card_15_spider_mite_1795034508829
        16 -> R.drawable.v3_card_16_crop_collapse_1795034613726
        17 -> R.drawable.v3_card_17_trichome_sparkle_1795034613504
        18 -> R.drawable.v3_card_18_guerrilla_growing_1795034615120
        19 -> R.drawable.v3_card_19_grow_light_1795034614527
        20 -> R.drawable.v3_card_20_cup_submission_1795034506088
        21 -> R.drawable.v3_card_21_green_planet_1795034535461
        22 -> R.drawable.v3_card_22_genesis_electron_1795034637676
        23 -> R.drawable.v3_card_23_genesis_proton_1795034536439
        24 -> R.drawable.v3_card_24_genesis_hydrogen_1795034637793
        25 -> R.drawable.v3_card_25_genesis_dna_1795034536877
        26 -> R.drawable.v3_card_26_genesis_singularity_1795034638171
        27 -> R.drawable.v3_card_27_amber_resin_1795034639187
        28 -> R.drawable.v3_card_28_liquid_trichomes_1795034510094
        29 -> R.drawable.v3_card_29_ocean_coral_1795034538631
        30 -> R.drawable.v3_card_30_ocean_leviathan_1795034641144
        31 -> R.drawable.card_genesis_eliminator_1795033578202
        32 -> R.drawable.v3_m32_gen_ace_1795035261549
        33 -> R.drawable.v3_m33_gen_two_1795035262140
        34 -> R.drawable.v3_m34_gen_three_1795035261475
        35 -> R.drawable.v3_m35_gen_four_1795035263671
        36 -> R.drawable.v3_m36_gen_five_1795035263569
        37 -> R.drawable.v3_m37_gen_six_1795035263728
        38 -> R.drawable.v3_m38_gen_seven_1795035264680
        39 -> R.drawable.v3_m39_gen_eight_1795035278725
        40 -> R.drawable.v3_m40_gen_nine_1795035280924
        41 -> R.drawable.v3_m41_gen_ten_1795035280338
        42 -> R.drawable.v3_m42_gen_page_1795035280900
        43 -> R.drawable.v3_m43_gen_knight_1795035281903
        44 -> R.drawable.v3_m44_gen_queen_1795035282665
        45 -> R.drawable.v3_m45_gen_king_1795035282998
        46 -> R.drawable.v3_m46_can_ace_1795035295203
        47 -> R.drawable.v3_m47_can_two_1795035296988
        48 -> R.drawable.v3_m48_can_three_1795035296950
        49 -> R.drawable.v3_m49_can_four_1795035297598
        50 -> R.drawable.v3_m50_can_five_1795035299310
        51 -> R.drawable.v3_m51_can_six_1795035299225
        52 -> R.drawable.v3_m52_can_seven_1795035299698
        53 -> R.drawable.v3_m53_can_eight_1795035315159
        54 -> R.drawable.v3_m54_can_nine_1795035315248
        55 -> R.drawable.v3_m55_can_ten_1795035315493
        56 -> R.drawable.v3_m56_can_page_1795035316750
        57 -> R.drawable.v3_m57_can_knight_1795035318160
        58 -> R.drawable.v3_m58_can_queen_1795035318235
        59 -> R.drawable.v3_m59_can_king_1795035318902
        60 -> R.drawable.v3_m60_res_ace_1795035337851
        61 -> R.drawable.v3_m61_res_two_1795035339126
        62 -> R.drawable.v3_m62_res_three_1795035340366
        63 -> R.drawable.v3_m63_res_four_1795035340031
        64 -> R.drawable.v3_m64_res_five_1795035341504
        65 -> R.drawable.v3_m65_res_six_1795035357155
        66 -> R.drawable.v3_m66_res_seven_1795035341511
        67 -> R.drawable.v3_m67_res_eight_1795035358156
        68 -> R.drawable.v3_m68_res_nine_1795035359163
        69 -> R.drawable.v3_m69_res_ten_1795035359258
        70 -> R.drawable.v3_m70_res_page_1795035359921
        71 -> R.drawable.v3_m71_res_knight_1795035360475
        72 -> R.drawable.v3_m72_res_queen_1795035361370
        73 -> R.drawable.v3_m73_res_king_1795035362306
        74 -> R.drawable.v3_m74_alc_ace_1795035376967
        75 -> R.drawable.v3_m75_alc_two_1795035377696
        76 -> R.drawable.v3_m76_alc_three_1795035377634
        77 -> R.drawable.v3_m77_alc_four_1795035378719
        78 -> R.drawable.v3_m78_alc_five_1795035379073
        79 -> R.drawable.v3_m79_alc_six_1795035380433
        80 -> R.drawable.v3_m80_alc_seven_1795035381957
        81 -> R.drawable.v3_m81_alc_eight_1795035513749
        82 -> R.drawable.v3_m82_alc_nine_1795035397039
        83 -> R.drawable.v3_m83_alc_ten_1795035397349
        84 -> R.drawable.v3_m84_alc_page_1795035398767
        85 -> R.drawable.v3_m85_alc_knight_1795035398851
        86 -> R.drawable.v3_m86_alc_queen_1795035399846
        87 -> R.drawable.v3_m87_alc_king_1795035400777
        else -> null
    }
}

fun getCardBackImage(series: String): Int {
    return when (series) {
        "Genesis Series" -> R.drawable.back_v3_genesis_series_1795034640705
        "Amber Series" -> R.drawable.back_v3_amber_series_1795034563700
        "Seas & Oceans" -> R.drawable.back_v3_ocean_series_1795034564849
        else -> R.drawable.back_v3_suit_genetics_1795034560961
    }
}

fun getSuitCardBackImage(discipline: Tarot420Discipline): Int {
    return when (discipline) {
        Tarot420Discipline.GENETICS -> R.drawable.back_v3_suit_genetics_1795034560961
        Tarot420Discipline.CANOPY -> R.drawable.back_v3_suit_canopy_1795034561562
        Tarot420Discipline.RESIN -> R.drawable.back_v3_suit_resin_1795034561824
        Tarot420Discipline.ALCHEMY -> R.drawable.back_v3_suit_alchemy_1795034564167
    }
}

fun getCardBackImageForCard(cardIndex: Int, series: String): Int {
    if (cardIndex == CyberTarotDeck.GENESIS_ELIMINATOR_INDEX) {
        return R.drawable.back_genesis_elements_1795033577974
    }
    return when (series) {
        "Genesis Series" -> R.drawable.back_v3_genesis_series_1795034640705
        "Amber Series" -> R.drawable.back_v3_amber_series_1795034563700
        "Seas & Oceans" -> R.drawable.back_v3_ocean_series_1795034564849
        else -> if (cardIndex >= 0) getSuitCardBackImage(getCard420Discipline(cardIndex)) else getCardBackImage(series)
    }
}

@Composable
fun CardSymbolIcon(cardIndex: Int, tintColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val icon = when (cardIndex) {
            0 -> Icons.Default.Spa
            1 -> Icons.Default.Science
            2 -> Icons.Default.AcUnit
            3 -> Icons.Default.Eco
            4 -> Icons.Default.Build
            5 -> Icons.Default.Agriculture
            6 -> Icons.Default.Favorite
            7 -> Icons.Default.Speed
            8 -> Icons.Default.FitnessCenter
            9 -> Icons.Default.FilterVintage
            10 -> Icons.Default.Widgets
            11 -> Icons.Default.DoneAll
            12, 28 -> Icons.Default.WaterDrop
            13 -> Icons.Default.ContentCut
            14 -> Icons.Default.Thermostat
            15 -> Icons.Default.BugReport
            16 -> Icons.Default.FlashOff
            17 -> Icons.Default.Star
            18 -> Icons.Default.Bedtime
            19 -> Icons.Default.WbSunny
            20 -> Icons.Default.EmojiEvents
            21 -> Icons.Default.Public
            22 -> Icons.Default.Bolt
            23 -> Icons.Default.Adjust
            24 -> Icons.Default.BubbleChart
            25 -> Icons.Default.Biotech
            26, 31 -> Icons.Default.AutoAwesome
            27 -> Icons.Default.Diamond
            29 -> Icons.Default.Waves
            30 -> Icons.Default.Tsunami
            in 32..45 -> Icons.Default.Eco
            in 46..59 -> Icons.Default.WbSunny
            in 60..73 -> Icons.Default.WaterDrop
            in 74..87 -> Icons.Default.Science
            else -> Icons.Default.Eco
        }
        Icon(icon, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
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
    val c = Cyber.colors
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

    val motionEnabled = rememberMotionEnabled()
    val haptic = LocalHapticFeedback.current
    var currentDeckSubTab by rememberSaveable { mutableIntStateOf(0) }

    BackHandler(enabled = currentDeckSubTab != 0) {
        currentDeckSubTab = 0
    }

    var columnCount by remember { mutableIntStateOf(2) }
    var gridGap by remember { mutableFloatStateOf(10f) }
    var glowSpread by remember { mutableFloatStateOf(6f) }
    var glowColorPreset by remember { mutableStateOf("Rarity Matched") }
    var presetStyle by remember { mutableStateOf("Standard CSS Grid") }
    var infoMode by remember { mutableStateOf("Digital Analog") }
    var showGridConfig by remember { mutableStateOf(false) }

    val pulseFactor = if (motionEnabled) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        infiniteTransition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(1400, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_anim"
        ).value
    } else {
        1.0f
    }

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
            .padding(horizontal = Space.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (currentDeckSubTab == 0) {
            Text(
                text = "DECK",
                style = MaterialTheme.typography.headlineSmall,
                color = NeonMagenta,
                modifier = Modifier.testTag("deck_title")
            )
            Text(
                text = "Rider-Waite Symbols",
                style = MaterialTheme.typography.bodyMedium,
                color = c.textMid,
                modifier = Modifier.padding(top = Space.xxs, bottom = Space.sm)
            )
        } else {
            Spacer(modifier = Modifier.height(Space.xs))
        }

        // SubTab Selection Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Space.md)
                .background(c.panel, CyberShapes.card)
                .border(1.dp, c.goldAntique.copy(alpha = 0.25f), CyberShapes.card)
                .padding(Space.xxs)
        ) {
            val tabs = listOf(
                Pair(0, "REGISTRY"),
                Pair(1, "BUILDER"),
                Pair(2, "FORGE"),
                Pair(3, "DUEL"),
                Pair(4, "ANALYTICS")
            )
            val tabColors = listOf(NeonMagenta, NeonCyan, NeonGold, NeonGreen, NeonCyan)

            tabs.forEachIndexed { idx, (tabIndex, title) ->
                val isSelected = currentDeckSubTab == tabIndex
                val color = tabColors[idx]
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .clip(CyberShapes.chip)
                        .background(if (isSelected) color.copy(alpha = 0.2f) else Color.Transparent)
                        .border(1.dp, if (isSelected) color else Color.Transparent, CyberShapes.chip)
                        .clickable(role = Role.Tab) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            currentDeckSubTab = tabIndex
                        }
                        .padding(vertical = Space.xs),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isSelected) c.textHigh else c.textMid,
                        maxLines = 1
                    )
                }
            }
        }

        when (currentDeckSubTab) {
            0 -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Space.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Space.sm)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search Cyber Deck") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = c.accent,
                            unfocusedBorderColor = c.goldAntique.copy(alpha = 0.4f),
                            focusedLabelColor = c.accent,
                            unfocusedLabelColor = c.textMid,
                            focusedTextColor = c.textHigh,
                            unfocusedTextColor = c.textHigh
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = { showGridConfig = !showGridConfig },
                        modifier = Modifier
                            .size(54.dp)
                            .background(if (showGridConfig) NeonCyan.copy(alpha = 0.15f) else c.panelRaised, CyberShapes.control)
                            .border(1.dp, if (showGridConfig) NeonCyan else c.goldAntique.copy(alpha = 0.4f), CyberShapes.control)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Grid System Config",
                            tint = if (showGridConfig) NeonCyan else c.textHigh
                        )
                    }
                }

                AnimatedVisibility(
                    visible = showGridConfig,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = c.panel),
                        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                        shape = CyberShapes.control,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = Space.md)
                    ) {
                        Column(modifier = Modifier.padding(Space.md)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CSS GRID ENGINE CONFIGURATION",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = NeonCyan
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                                    TextButton(
                                        onClick = {
                                            CyberTarotDeck.cards.forEach { card ->
                                                flippedCardsMap[card.index] = true
                                            }
                                        },
                                        modifier = Modifier.heightIn(min = 48.dp)
                                    ) {
                                        Text("FLIP ALL", style = MaterialTheme.typography.labelLarge, color = NeonGreen)
                                    }
                                    TextButton(
                                        onClick = { flippedCardsMap.clear() },
                                        modifier = Modifier.heightIn(min = 48.dp)
                                    ) {
                                        Text("LOCK ALL", style = MaterialTheme.typography.labelLarge, color = NeonRed)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(Space.sm))

                            Text(
                                text = "grid-template-columns: repeat($columnCount, 1fr);",
                                style = MaterialTheme.typography.labelMedium.copy(fontFamily = IbmPlexMonoFontFamily),
                                color = c.textMid
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = Space.sm),
                                horizontalArrangement = Arrangement.spacedBy(Space.sm)
                            ) {
                                listOf(1, 2, 3, 4).forEach { col ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .clip(CyberShapes.chip)
                                            .background(if (columnCount == col) NeonCyan.copy(alpha = 0.2f) else c.panelRaised)
                                            .border(1.dp, if (columnCount == col) NeonCyan else c.goldAntique.copy(alpha = 0.35f), CyberShapes.chip)
                                            .clickable { columnCount = col },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$col Col",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (columnCount == col) NeonCyan else c.textMid
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Space.lg)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "grid-gap: ${gridGap.toInt()}dp;",
                                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = IbmPlexMonoFontFamily),
                                        color = c.textMid
                                    )
                                    Slider(
                                        value = gridGap,
                                        onValueChange = { gridGap = it },
                                        valueRange = 4f..24f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = NeonCyan,
                                            activeTrackColor = NeonCyan,
                                            inactiveTrackColor = c.ink
                                        )
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "box-shadow: ${glowSpread.toInt()}dp;",
                                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = IbmPlexMonoFontFamily),
                                        color = c.textMid
                                    )
                                    Slider(
                                        value = glowSpread,
                                        onValueChange = { glowSpread = it },
                                        valueRange = 2f..14f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = NeonCyan,
                                            activeTrackColor = NeonCyan,
                                            inactiveTrackColor = c.ink
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(Space.xs))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Space.sm)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("GRID-STYLE", style = MaterialTheme.typography.labelMedium, color = c.textMid)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = Space.xxs),
                                        horizontalArrangement = Arrangement.spacedBy(Space.xxs)
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
                                                    .heightIn(min = 48.dp)
                                                    .clip(CyberShapes.chip)
                                                    .background(if (isSel) NeonMagenta.copy(alpha = 0.2f) else c.panelRaised)
                                                    .border(1.dp, if (isSel) NeonMagenta else c.goldAntique.copy(alpha = 0.35f), CyberShapes.chip)
                                                    .clickable { presetStyle = style },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(abbr, style = MaterialTheme.typography.labelMedium, color = if (isSel) NeonMagenta else c.textMid)
                                            }
                                        }
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text("GLOW-COLOR", style = MaterialTheme.typography.labelMedium, color = c.textMid)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = Space.xxs),
                                        horizontalArrangement = Arrangement.spacedBy(Space.xxs)
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
                                                    .heightIn(min = 48.dp)
                                                    .clip(CyberShapes.chip)
                                                    .background(if (isSel) NeonGreen.copy(alpha = 0.2f) else c.panelRaised)
                                                    .border(1.dp, if (isSel) NeonGreen else c.goldAntique.copy(alpha = 0.35f), CyberShapes.chip)
                                                    .clickable { glowColorPreset = colorPreset },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(abbr, style = MaterialTheme.typography.labelMedium, color = if (isSel) NeonGreen else c.textMid)
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(Space.sm))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("DISPLAY MODE", style = MaterialTheme.typography.labelMedium, color = c.textMid)
                                Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                                    listOf("Digital Analog", "Traditional Rider-Waite").forEach { mode ->
                                        CyberChip(
                                            label = if (mode == "Digital Analog") "Digital" else "Rider-Waite",
                                            selected = infoMode == mode,
                                            onClick = { infoMode = mode }
                                        )
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
                        .padding(bottom = Space.xs)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Space.sm)
                ) {
                    listOf("All Series", "Cyber Botanica", "Genesis Series", "Amber Series", "Seas & Oceans").forEach { seriesName ->
                        CyberChip(
                            label = seriesName.uppercase(),
                            selected = selectedSeriesFilter == seriesName,
                            onClick = { selectedSeriesFilter = seriesName }
                        )
                    }
                }

                // Rarity Filter Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Space.md)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Space.sm)
                ) {
                    listOf("All", "Common", "Rare", "Epic", "Legend", "Genesis").forEach { rarity ->
                        CyberChip(
                            label = rarity.uppercase(),
                            selected = selectedRarityFilter == rarity,
                            onClick = { selectedRarityFilter = rarity }
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (presetStyle == "Holographic Command Table") c.ink else Color.Transparent
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
                                    CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.CLICK)
                                },
                                onCardClick = {
                                    selectedInspectCard = card
                                    CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.CARD_DRAW)
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
            }
            1 -> DeckBuilderScreen(
                viewModel = viewModel,
                onLaunchDuel = { currentDeckSubTab = 3 }
            )
            2 -> CardForgeScreen(viewModel = viewModel)
            3 -> DuelArenaScreen(
                viewModel = viewModel,
                onBack = { currentDeckSubTab = 0 }
            )
            else -> NeuralAnalyticsView(
                mintedCards = mintedCards,
                marketListings = marketListings,
                systemLanguage = lang
            )
        }
    }

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
