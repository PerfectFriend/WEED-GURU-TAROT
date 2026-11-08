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
import androidx.compose.ui.text.font.FontFamily
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
    val infiniteTransition = rememberInfiniteTransition(label = "forge_glow")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050811))
            .padding(horizontal = 12.dp)
            .testTag("card_forge_screen")
    ) {
        // Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f).padding(end = 6.dp)) {
                    Text(
                        text = if (lang == "ru") "КУЗНИЦА АРКАНОВ" else "ARCANA FORGE & SYNTHESIS",
                        color = Color(0xFFFFD700),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (lang == "ru") "Прокачка силы, брони, RAM и скинов" else "Enhance ATK, DEF, RAM cost & holographic skins",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // AI Forge Switch Button
                OutlinedButton(
                    onClick = {
                        CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.CLICK)
                        isAiForgeOpen = true
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF00F5FF)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF00F5FF), modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (lang == "ru") "ИИ-Кузница" else "AI Forge", color = Color(0xFF00F5FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // WEED Credits Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0D1B14),
                    border = BorderStroke(1.dp, Color(0xFF00FF66).copy(alpha = 0.6f)),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "🌿", fontSize = 11.sp)
                        Text(
                            text = "$weedBalance",
                            color = Color(0xFF00FF66),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Selected Card Holographic Workshop Stage
        val element = baseBattleCard.element
        val skinColor = when (currentUpgrade.customSkin) {
            "BIO_TOX" -> Color(0xFF00FF66)
            "GOLDEN_CIRCUIT" -> Color(0xFFFFD700)
            "VOID_PHANTOM" -> Color(0xFFBD00FF)
            else -> element.color
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF090E1D),
            border = BorderStroke(1.5.dp, skinColor.copy(alpha = glowPulse)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cyber Card Preview Frame
                Box(
                    modifier = Modifier
                        .width(105.dp)
                        .height(150.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF131A2E), Color(0xFF090C16))
                            )
                        )
                        .border(1.5.dp, skinColor, RoundedCornerShape(8.dp))
                        .padding(6.dp)
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
                                text = "$effRam⚡",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFB800)
                            )
                            Text(text = element.symbol, fontSize = 12.sp)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = selectedCard.name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "LVL ${currentUpgrade.upgradeLevel}/5",
                                fontSize = 9.sp,
                                color = skinColor,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Combat Stats
                        val effPwr = baseBattleCard.basePower + currentUpgrade.bonusPower
                        val effDef = baseBattleCard.baseDefense + currentUpgrade.bonusDefense
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "⚔$effPwr", fontSize = 11.sp, color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                            Text(text = "🛡$effDef", fontSize = 11.sp, color = Color(0xFF00FF66), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Stats & Upgrade Controls
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedCard.traditionalName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "★".repeat(currentUpgrade.upgradeLevel) + "☆".repeat(5 - currentUpgrade.upgradeLevel),
                            color = Color(0xFFFFD700),
                            fontSize = 11.sp
                        )
                    }

                    baseBattleCard.specialEffect?.let { effect ->
                        Text(
                            text = "⚡ ${if (lang == "ru") effect.titleRu else effect.titleEn}: ${if (lang == "ru") effect.descriptionRu else effect.descriptionEn}",
                            fontSize = 10.sp,
                            color = Color.LightGray,
                            lineHeight = 12.sp
                        )
                    }

                    // Upgrade Power Button
                    val powerCost = 100 * currentUpgrade.upgradeLevel
                    val canAffordPwr = weedBalance >= powerCost && currentUpgrade.upgradeLevel < 5
                    OutlinedButton(
                        onClick = {
                            viewModel.upgradeCardStat(selectedCardIndex, "POWER") { success ->
                                if (success) {
                                    CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
                                    upgradeFeedbackMessage = if (lang == "ru") "+1 Силы успешно синтезировано!" else "+1 Power synthesized!"
                                } else {
                                    upgradeFeedbackMessage = if (lang == "ru") "Недостаточно WEED кредитов!" else "Not enough WEED credits!"
                                }
                            }
                        },
                        enabled = canAffordPwr,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, if (canAffordPwr) Color(0xFFFF5252) else Color(0x33FFFFFF))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (lang == "ru") "⚔ Разгон Силы (+1)" else "⚔ Overclock (+1 ATK)",
                                fontSize = 10.sp,
                                color = if (canAffordPwr) Color(0xFFFF5252) else Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (currentUpgrade.upgradeLevel < 5) "$powerCost WEED" else "MAX",
                                fontSize = 10.sp,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Upgrade Defense Button
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
                            .height(30.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, if (canAffordDef) Color(0xFF00FF66) else Color(0x33FFFFFF))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (lang == "ru") "🛡 Усиление Щита (+1)" else "🛡 Shield (+1 DEF)",
                                fontSize = 10.sp,
                                color = if (canAffordDef) Color(0xFF00FF66) else Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (currentUpgrade.upgradeLevel < 5) "$defCost WEED" else "MAX",
                                fontSize = 10.sp,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // RAM Optimization (-1 RAM cost, unlocks at Lvl 3)
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
                            .height(30.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, if (canAffordRam) Color(0xFFFFB800) else Color(0x33FFFFFF))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (lang == "ru") "⚡ Оптимизация RAM (-1)" else "⚡ RAM Opt (-1 RAM)",
                                fontSize = 10.sp,
                                color = if (canAffordRam) Color(0xFFFFB800) else Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = when {
                                    ramAlreadyOptimized -> "DONE"
                                    !ramUnlocked -> "LVL 3 REQ"
                                    else -> "250 WEED"
                                },
                                fontSize = 10.sp,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Skin Matrix Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF050711))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (lang == "ru") "СКИН-МАТРИЦА АРКАНА:" else "HOLOGRAPHIC SKIN MATRIX:",
                    color = Color.LightGray,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val skins = listOf(
                        Triple("DEFAULT", if (lang == "ru") "Классик" else "Classic", Color(0xFF00F5FF)),
                        Triple("BIO_TOX", if (lang == "ru") "Био-Токс" else "Bio-Tox", Color(0xFF00FF66)),
                        Triple("GOLDEN_CIRCUIT", if (lang == "ru") "Золото" else "Gold", Color(0xFFFFD700)),
                        Triple("VOID_PHANTOM", if (lang == "ru") "Пустота" else "Void", Color(0xFFBD00FF))
                    )
                    skins.forEach { (skinKey, label, color) ->
                        val isSelected = currentUpgrade.customSkin == skinKey
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) color.copy(alpha = 0.25f) else Color(0xFF0F1524),
                            border = BorderStroke(1.dp, if (isSelected) color else Color(0x33FFFFFF)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.CLICK)
                                    viewModel.setCardSkin(selectedCardIndex, skinKey)
                                }
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                color = if (isSelected) color else Color.Gray,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(vertical = 4.dp),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Feedback alert banner if any
        upgradeFeedbackMessage?.let { msg ->
            Text(
                text = msg,
                color = Color(0xFF00FF66),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Search & Filter Arcana to Upgrade
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text(if (lang == "ru") "Выбор аркана в кузницу..." else "Filter arcana...", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFD700),
                    unfocusedBorderColor = Color.DarkGray
                )
            )

            // Element Filters
            ElementAffinity.values().forEach { elem ->
                val isSelected = selectedElementFilter == elem
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) elem.color.copy(alpha = 0.3f) else Color(0xFF0E1322))
                        .border(1.dp, if (isSelected) elem.color else Color(0x33FFFFFF), CircleShape)
                        .clickable {
                            CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.CLICK)
                            selectedElementFilter = if (isSelected) null else elem
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = elem.symbol, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // List of 78 Cards for Selection
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
            verticalArrangement = Arrangement.spacedBy(4.dp)
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
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSelected) Color(0xFF161F38) else Color(0xFF0B101E),
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFFFFD700) else Color(0x1FFFFFFF)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectCard(card.index) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = elem.symbol, fontSize = 16.sp)
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = card.name,
                                        color = if (isSelected) Color(0xFFFFD700) else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    if (up != null && up.upgradeLevel > 1) {
                                        Text(
                                            text = "LVL ${up.upgradeLevel}",
                                            color = Color(0xFFFFD700),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = "${card.traditionalName} • ${card.defaultRarity}",
                                    color = Color.Gray,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Bonus indicators
                        if (up != null && (up.bonusPower > 0 || up.bonusDefense > 0 || up.energyDiscount > 0)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (up.bonusPower > 0) {
                                    Text(text = "+${up.bonusPower}⚔", fontSize = 10.sp, color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                                }
                                if (up.bonusDefense > 0) {
                                    Text(text = "+${up.bonusDefense}🛡", fontSize = 10.sp, color = Color(0xFF00FF66), fontWeight = FontWeight.Bold)
                                }
                                if (up.energyDiscount > 0) {
                                    Text(text = "-1⚡", fontSize = 10.sp, color = Color(0xFFFFB800), fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Text(
                                text = if (isSelected) "ВЫБРАН" else "ВЫБРАТЬ",
                                fontSize = 10.sp,
                                color = if (isSelected) Color(0xFFFFD700) else Color.DarkGray,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
