package com.example.deckforge

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.CyberNoirSynthPlayer
import com.example.data.CardUpgrade
import com.example.data.CyberTarotCard
import com.example.data.CyberTarotDeck
import com.example.duel.BattleCardFactory
import com.example.duel.CardSpecialEffect
import com.example.duel.ElementAffinity
import com.example.ui.components.FramedTarotArtworkWindow
import com.example.ui.components.WeedGuruCardInspectorDialog
import com.example.ui.components.getArcanaRomanNumeral
import com.example.ui.theme.Cyber
import com.example.ui.theme.CyberShapes
import com.example.ui.theme.ElegantViolet
import com.example.ui.theme.IbmPlexMonoFontFamily
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.Space
import com.example.ui.theme.rememberMotionEnabled
import com.example.viewmodel.TarotViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardForgeScreen(
    viewModel: TarotViewModel,
    onBack: (() -> Unit)? = null
) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val cardUpgrades by viewModel.cardUpgrades.collectAsStateWithLifecycle()
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val lang = userProfile?.systemLanguage ?: "en"
    val weedBalance = userProfile?.weed ?: 0

    var selectedCardIndex by remember { mutableIntStateOf(0) }
    val selectedCard = remember(selectedCardIndex) { CyberTarotDeck.cards.getOrNull(selectedCardIndex) ?: CyberTarotDeck.cards[0] }
    val currentUpgrade = cardUpgrades[selectedCardIndex] ?: CardUpgrade(cardIndex = selectedCardIndex)
    val baseBattleCard = remember(selectedCard) { BattleCardFactory.fromTarot(selectedCard) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedElementFilter by remember { mutableStateOf<ElementAffinity?>(null) }
    var upgradeFeedbackMessage by remember { mutableStateOf<String?>(null) }
    var isAiForgeOpen by remember { mutableStateOf(false) }
    var showForgeInspectDialog by remember { mutableStateOf(false) }

    if (isAiForgeOpen) {
        com.example.forge.AiArcanaForgeScreen(
            viewModel = viewModel,
            onBack = { isAiForgeOpen = false }
        )
        return
    }

    // Sound effect on select
    val onSelectCard: (Int) -> Unit = { idx ->
        CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.CLICK)
        selectedCardIndex = idx
        upgradeFeedbackMessage = null
    }

    // Animation for card hologram glow
    val motionEnabled = rememberMotionEnabled()
    val glowPulse = if (motionEnabled) {
        val infiniteTransition = rememberInfiniteTransition(label = "forge_glow")
        infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow"
        ).value
    } else {
        0.75f
    }

    val c = Cyber.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.ink)
            .padding(horizontal = Space.md)
            .testTag("card_forge_screen")
    ) {
        // Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Space.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = c.textHigh
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f).padding(end = Space.xs)) {
                    Text(
                        text = if (lang == "ru") "КУЗНИЦА АРКАНОВ" else "ARCANA FORGE & SYNTHESIS",
                        style = MaterialTheme.typography.titleMedium,
                        color = c.goldLeaf,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (lang == "ru") "Сила, броня, RAM и скины" else "Enhance ATK, DEF, RAM & skins",
                        style = MaterialTheme.typography.labelMedium,
                        color = c.textMid,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.xs)
            ) {
                OutlinedButton(
                    onClick = {
                        CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.CLICK)
                        isAiForgeOpen = true
                    },
                    shape = CyberShapes.chip,
                    border = BorderStroke(1.dp, NeonCyan),
                    contentPadding = PaddingValues(horizontal = Space.sm, vertical = Space.xs),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(Space.xs))
                    Text(
                        text = if (lang == "ru") "ИИ-Кузница" else "AI Forge",
                        style = MaterialTheme.typography.labelMedium,
                        color = NeonCyan
                    )
                }

                Surface(
                    shape = CyberShapes.control,
                    color = c.panelRaised,
                    border = BorderStroke(1.dp, c.emeraldGlow.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = Space.sm, vertical = Space.xs),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Space.xs)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = c.emeraldGlow,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "$weedBalance",
                            style = MaterialTheme.typography.labelSmall,
                            color = c.emeraldGlow
                        )
                    }
                }
            }
        }

        // Selected Card Holographic Workshop Stage
        val element = baseBattleCard.element
        val skinColor = when (currentUpgrade.customSkin) {
            "BIO_TOX" -> c.emeraldGlow
            "GOLDEN_CIRCUIT" -> c.goldLeaf
            "VOID_PHANTOM" -> ElegantViolet
            else -> element.color
        }

        Surface(
            shape = CyberShapes.control,
            color = c.panel,
            border = BorderStroke(1.5.dp, skinColor.copy(alpha = glowPulse)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Space.xs)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Space.md),
                horizontalArrangement = Arrangement.spacedBy(Space.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(126.dp)
                        .height(198.dp)
                        .clip(CyberShapes.cardCompact)
                        .background(Brush.verticalGradient(listOf(c.panelRaised, c.ink)))
                        .border(1.8.dp, skinColor, CyberShapes.cardCompact)
                        .clickable { showForgeInspectDialog = true }
                        .padding(5.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val effRam = (baseBattleCard.energyCost - currentUpgrade.energyDiscount).coerceAtLeast(1)
                            Text(
                                text = "${getArcanaRomanNumeral(selectedCard.index)} · ${effRam}RAM",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonAmber,
                                maxLines = 1
                            )
                            Text(text = element.symbol, style = MaterialTheme.typography.labelSmall)
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        FramedTarotArtworkWindow(
                            card = selectedCard,
                            isUpright = true,
                            compactWatermark = true,
                            showDiagonalWatermark = true,
                            rarityBorderColor = skinColor.copy(alpha = 0.85f),
                            cornerRadius = 6,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = selectedCard.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = c.textHigh,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )

                        val effPwr = baseBattleCard.basePower + currentUpgrade.bonusPower
                        val effDef = baseBattleCard.baseDefense + currentUpgrade.bonusDefense
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "A:$effPwr", style = MaterialTheme.typography.labelSmall, color = c.danger)
                            Text(text = "L${currentUpgrade.upgradeLevel}", style = MaterialTheme.typography.labelSmall, color = skinColor)
                            Text(text = "D:$effDef", style = MaterialTheme.typography.labelSmall, color = c.emeraldGlow)
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Space.xs)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedCard.traditionalName,
                            style = MaterialTheme.typography.titleSmall,
                            color = c.textHigh,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "★".repeat(currentUpgrade.upgradeLevel) + "☆".repeat(5 - currentUpgrade.upgradeLevel),
                            style = MaterialTheme.typography.labelMedium,
                            color = c.goldLeaf
                        )
                    }

                    baseBattleCard.specialEffect?.let { effect ->
                        Text(
                            text = "${if (lang == "ru") effect.titleRu else effect.titleEn}: ${if (lang == "ru") effect.descriptionRu else effect.descriptionEn}",
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textMid,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    val powerCost = 100 * currentUpgrade.upgradeLevel
                    val canAffordPwr = weedBalance >= powerCost && currentUpgrade.upgradeLevel < 5
                    OutlinedButton(
                        onClick = {
                            viewModel.upgradeCardStat(selectedCardIndex, "POWER") { success ->
                                if (success) {
                                    CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
                                    upgradeFeedbackMessage = if (lang == "ru") "+1 Силы синтезировано!" else "+1 Power synthesized!"
                                } else {
                                    upgradeFeedbackMessage = if (lang == "ru") "Недостаточно WEED кредитов!" else "Not enough WEED credits!"
                                }
                            }
                        },
                        enabled = canAffordPwr,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                        contentPadding = PaddingValues(horizontal = Space.sm, vertical = Space.xs),
                        shape = CyberShapes.chip,
                        border = BorderStroke(1.dp, if (canAffordPwr) c.danger else c.textMid.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (lang == "ru") "Сила (+1)" else "Overclock (+1 ATK)",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (canAffordPwr) c.danger else c.textMid
                            )
                            Text(
                                text = if (currentUpgrade.upgradeLevel < 5) "$powerCost" else "MAX",
                                style = MaterialTheme.typography.labelSmall,
                                color = c.textHigh
                            )
                        }
                    }

                    val defCost = 100 * currentUpgrade.upgradeLevel
                    val canAffordDef = weedBalance >= defCost && currentUpgrade.upgradeLevel < 5
                    OutlinedButton(
                        onClick = {
                            viewModel.upgradeCardStat(selectedCardIndex, "DEFENSE") { success ->
                                if (success) {
                                    CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
                                    upgradeFeedbackMessage = if (lang == "ru") "+1 Брони укреплено!" else "+1 Shield reinforced!"
                                } else {
                                    upgradeFeedbackMessage = if (lang == "ru") "Недостаточно WEED кредитов!" else "Not enough WEED credits!"
                                }
                            }
                        },
                        enabled = canAffordDef,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                        contentPadding = PaddingValues(horizontal = Space.sm, vertical = Space.xs),
                        shape = CyberShapes.chip,
                        border = BorderStroke(1.dp, if (canAffordDef) c.emeraldGlow else c.textMid.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (lang == "ru") "Щит (+1)" else "Shield (+1 DEF)",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (canAffordDef) c.emeraldGlow else c.textMid
                            )
                            Text(
                                text = if (currentUpgrade.upgradeLevel < 5) "$defCost" else "MAX",
                                style = MaterialTheme.typography.labelSmall,
                                color = c.textHigh
                            )
                        }
                    }

                    val ramUnlocked = currentUpgrade.upgradeLevel >= 3
                    val ramAlreadyOptimized = currentUpgrade.energyDiscount > 0
                    val canAffordRam = ramUnlocked && !ramAlreadyOptimized && weedBalance >= 250
                    OutlinedButton(
                        onClick = {
                            viewModel.upgradeCardStat(selectedCardIndex, "RAM") { success ->
                                if (success) {
                                    CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
                                    upgradeFeedbackMessage = if (lang == "ru") "Стоимость RAM снижена на -1!" else "RAM cost reduced by 1!"
                                }
                            }
                        },
                        enabled = canAffordRam,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                        contentPadding = PaddingValues(horizontal = Space.sm, vertical = Space.xs),
                        shape = CyberShapes.chip,
                        border = BorderStroke(1.dp, if (canAffordRam) NeonAmber else c.textMid.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (lang == "ru") "RAM (-1)" else "RAM Opt (-1)",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (canAffordRam) NeonAmber else c.textMid
                            )
                            Text(
                                text = when {
                                    ramAlreadyOptimized -> "DONE"
                                    !ramUnlocked -> "LVL 3"
                                    else -> "250"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = c.textHigh
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(c.ink.copy(alpha = 0.85f))
                    .padding(horizontal = Space.md, vertical = Space.sm)
            ) {
                Text(
                    text = if (lang == "ru") "СКИН-МАТРИЦА АРКАНА:" else "HOLOGRAPHIC SKIN MATRIX:",
                    style = MaterialTheme.typography.labelMedium,
                    color = c.textMid
                )
                Spacer(modifier = Modifier.height(Space.xs))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Space.sm)
                ) {
                    val skins = listOf(
                        Triple("DEFAULT", if (lang == "ru") "Классик" else "Classic", NeonCyan),
                        Triple("BIO_TOX", if (lang == "ru") "Био-Токс" else "Bio-Tox", c.emeraldGlow),
                        Triple("GOLDEN_CIRCUIT", if (lang == "ru") "Золото" else "Gold", c.goldLeaf),
                        Triple("VOID_PHANTOM", if (lang == "ru") "Пустота" else "Void", ElegantViolet)
                    )
                    skins.forEach { (skinKey, label, color) ->
                        val isSelected = currentUpgrade.customSkin == skinKey
                        Surface(
                            shape = CyberShapes.chip,
                            color = if (isSelected) color.copy(alpha = 0.24f) else c.panelRaised,
                            border = BorderStroke(1.dp, if (isSelected) color else c.goldAntique.copy(alpha = 0.25f)),
                            onClick = {
                                CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.CLICK)
                                viewModel.setCardSkin(selectedCardIndex, skinKey)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = Space.xs)) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    textAlign = TextAlign.Center,
                                    color = if (isSelected) color else c.textMid,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        upgradeFeedbackMessage?.let { msg ->
            Text(
                text = msg,
                style = MaterialTheme.typography.labelMedium,
                color = c.emeraldGlow,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Space.xxs)
            )
        }

        Spacer(modifier = Modifier.height(Space.xs))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Space.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text(if (lang == "ru") "Выбор аркана..." else "Filter arcana...", style = MaterialTheme.typography.labelMedium) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = c.goldLeaf,
                    unfocusedBorderColor = c.goldAntique.copy(alpha = 0.4f)
                )
            )

            ElementAffinity.values().forEach { elem ->
                val isSelected = selectedElementFilter == elem
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) elem.color.copy(alpha = 0.3f) else c.panelRaised)
                        .border(1.dp, if (isSelected) elem.color else c.goldAntique.copy(alpha = 0.3f), CircleShape)
                        .clickable {
                            CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.CLICK)
                            selectedElementFilter = if (isSelected) null else elem
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = elem.symbol, style = MaterialTheme.typography.titleSmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(Space.xs))

        val cardsList = remember(searchQuery, selectedElementFilter) {
            CyberTarotDeck.cards.filter { card ->
                val elem = when (card.index % 4) {
                    0 -> ElementAffinity.CYBER
                    1 -> ElementAffinity.BIO
                    2 -> ElementAffinity.QUANTUM
                    else -> ElementAffinity.VOID
                }
                val matchElem = selectedElementFilter == null || elem == selectedElementFilter
                val matchSearch = searchQuery.isBlank() ||
                        card.name.contains(searchQuery, ignoreCase = true) ||
                        card.traditionalName.contains(searchQuery, ignoreCase = true)
                matchElem && matchSearch
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(Space.xs)
        ) {
            items(cardsList) { card ->
                val isSelected = card.index == selectedCardIndex
                val up = cardUpgrades[card.index]
                val elem = when (card.index % 4) {
                    0 -> ElementAffinity.CYBER
                    1 -> ElementAffinity.BIO
                    2 -> ElementAffinity.QUANTUM
                    else -> ElementAffinity.VOID
                }

                Surface(
                    shape = CyberShapes.chip,
                    color = if (isSelected) c.panelRaised else c.panel,
                    border = BorderStroke(1.dp, if (isSelected) c.goldLeaf else c.goldAntique.copy(alpha = 0.25f)),
                    onClick = { onSelectCard(card.index) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Space.sm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Space.sm),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = elem.symbol, style = MaterialTheme.typography.titleMedium)
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(Space.xs)
                                ) {
                                    Text(
                                        text = card.name,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (isSelected) c.goldLeaf else c.textHigh,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (up != null && up.upgradeLevel > 1) {
                                        Text(
                                            text = "L${up.upgradeLevel}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = c.goldLeaf
                                        )
                                    }
                                }
                                Text(
                                    text = "${card.traditionalName} • ${card.defaultRarity}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = c.textMid,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        if (up != null && (up.bonusPower > 0 || up.bonusDefense > 0 || up.energyDiscount > 0)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
                                if (up.bonusPower > 0) {
                                    Text(text = "+${up.bonusPower}A", style = MaterialTheme.typography.labelSmall, color = c.danger)
                                }
                                if (up.bonusDefense > 0) {
                                    Text(text = "+${up.bonusDefense}D", style = MaterialTheme.typography.labelSmall, color = c.emeraldGlow)
                                }
                                if (up.energyDiscount > 0) {
                                    Text(text = "-1R", style = MaterialTheme.typography.labelSmall, color = NeonAmber)
                                }
                            }
                        } else {
                            Text(
                                text = if (isSelected) {
                                    if (lang == "ru") "ВЫБРАН" else "ACTIVE"
                                } else {
                                    if (lang == "ru") "ВЫБРАТЬ" else "SELECT"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) c.goldLeaf else c.textMid
                            )
                        }
                    }
                }
            }
        }
    }

    if (showForgeInspectDialog) {
        WeedGuruCardInspectorDialog(
            card = selectedCard,
            isUpright = true,
            systemLanguage = lang,
            onDismiss = { showForgeInspectDialog = false }
        )
    }
}
