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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.CyberNoirSynthPlayer
import com.example.data.CyberTarotCard
import com.example.data.CyberTarotDeck
import com.example.data.SavedDeck
import com.example.duel.BattleCardFactory
import com.example.duel.ElementAffinity
import com.example.ui.components.FramedTarotArtworkWindow
import com.example.ui.components.WeedGuruCardInspectorDialog
import com.example.ui.components.getArcanaRomanNumeral
import com.example.ui.theme.Cyber
import com.example.ui.theme.CyberShapes
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.Space
import com.example.viewmodel.TarotViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckBuilderScreen(
    viewModel: TarotViewModel,
    onBack: (() -> Unit)? = null,
    onLaunchDuel: () -> Unit
) {
    val savedDecks by viewModel.savedDecks.collectAsStateWithLifecycle()
    val activeDeck by viewModel.activeDeck.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val cardUpgrades by viewModel.cardUpgrades.collectAsStateWithLifecycle()
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val lang = userProfile?.systemLanguage ?: "en"

    // Current working deck in editor
    var selectedDeckId by remember { mutableIntStateOf(activeDeck?.id ?: 0) }
    val currentDeck = remember(savedDecks, selectedDeckId, activeDeck) {
        savedDecks.find { it.id == selectedDeckId } ?: activeDeck ?: savedDecks.firstOrNull()
    }

    var workingCards by remember(currentDeck) {
        mutableStateOf(currentDeck?.cardIndices ?: listOf(0, 1, 2, 3, 4, 7, 10, 11, 13, 17, 18, 19, 21, 25, 30))
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedElementFilter by remember { mutableStateOf<ElementAffinity?>(null) }
    var selectedRarityFilter by remember { mutableStateOf("All") }
    var showNewDeckDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showInspectCard by remember { mutableStateOf<CyberTarotCard?>(null) }

    // Validation
    val isDeckValid = workingCards.size in 15..22
    val primaryColor = currentSkin.primaryColor

    val c = Cyber.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.ink)
            .padding(horizontal = Space.md)
            .testTag("deck_builder_screen")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Space.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
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
                        text = if (lang == "ru") "КОНСТРУКТОР КОЛОД" else "DECK FORGE & TACTICS",
                        style = MaterialTheme.typography.titleMedium,
                        color = NeonCyan,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (lang == "ru") "Сборка, кривая RAM и баланс" else "Tactical synergy & RAM curve",
                        style = MaterialTheme.typography.labelMedium,
                        color = c.textMid,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Button(
                onClick = onLaunchDuel,
                colors = ButtonDefaults.buttonColors(containerColor = c.emeraldGlow),
                shape = CyberShapes.chip,
                contentPadding = PaddingValues(horizontal = Space.md, vertical = Space.xs),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(
                    text = if (lang == "ru") "В БОЙ" else "BATTLE",
                    style = MaterialTheme.typography.labelLarge,
                    color = c.ink
                )
            }
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Space.xs),
            horizontalArrangement = Arrangement.spacedBy(Space.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(savedDecks) { deck ->
                val isSelected = deck.id == currentDeck?.id
                val isActive = deck.isActive
                Surface(
                    shape = CyberShapes.cardCompact,
                    color = if (isSelected) primaryColor.copy(alpha = 0.24f) else c.panelRaised,
                    border = BorderStroke(
                        1.dp,
                        if (isActive) c.emeraldGlow else if (isSelected) primaryColor else c.goldAntique.copy(alpha = 0.3f)
                    ),
                    onClick = {
                        CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.CLICK)
                        selectedDeckId = deck.id
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = Space.md, vertical = Space.xs),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Space.xs)
                    ) {
                        Text(text = deck.icon, style = MaterialTheme.typography.titleSmall)
                        Text(
                            text = deck.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) c.textHigh else c.textMid
                        )
                        if (isActive) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(c.emeraldGlow)
                            )
                        }
                    }
                }
            }

            if (savedDecks.size < 5) {
                item {
                    OutlinedButton(
                        onClick = {
                            CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.CLICK)
                            showNewDeckDialog = true
                        },
                        shape = CyberShapes.cardCompact,
                        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
                        contentPadding = PaddingValues(horizontal = Space.md, vertical = Space.xs),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text("+ ${if (lang == "ru") "Новая" else "New"}", style = MaterialTheme.typography.labelMedium, color = NeonCyan)
                    }
                }
            }
        }

        currentDeck?.let { deck ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Space.xs)
                    .background(c.panel, CyberShapes.chip)
                    .border(1.dp, c.goldAntique.copy(alpha = 0.28f), CyberShapes.chip)
                    .padding(horizontal = Space.md, vertical = Space.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Space.xs)
                ) {
                    Text(
                        text = "${workingCards.size}/15",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isDeckValid) c.emeraldGlow else NeonAmber
                    )
                    Text(
                        text = if (isDeckValid) {
                            if (lang == "ru") "ГОТОВА" else "VALID"
                        } else {
                            if (lang == "ru") "НУЖНО 15" else "NEED 15"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isDeckValid) c.emeraldGlow else NeonAmber
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(Space.xs), verticalAlignment = Alignment.CenterVertically) {
                    if (!deck.isActive && isDeckValid) {
                        OutlinedButton(
                            onClick = {
                                CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
                                viewModel.setActiveDeck(deck.id)
                            },
                            shape = CyberShapes.chip,
                            contentPadding = PaddingValues(horizontal = Space.sm, vertical = Space.xs),
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text(
                                text = if (lang == "ru") "Активна" else "Activate",
                                style = MaterialTheme.typography.labelMedium,
                                color = c.emeraldGlow
                            )
                        }
                    }

                    Button(
                        onClick = {
                            CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
                            viewModel.saveDeck(
                                name = deck.name,
                                icon = deck.icon,
                                cardIndices = workingCards,
                                makeActive = deck.isActive,
                                description = deck.description,
                                deckId = deck.id
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = CyberShapes.chip,
                        contentPadding = PaddingValues(horizontal = Space.sm, vertical = Space.xs),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text(
                            text = if (lang == "ru") "Сохранить" else "Save",
                            style = MaterialTheme.typography.labelMedium,
                            color = c.ink
                        )
                    }

                    if (savedDecks.size > 1 && !deck.isActive) {
                        IconButton(
                            onClick = { showDeleteConfirmDialog = true },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = c.danger,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Tactical Analytics: Mana Curve & Element Synergies
        DeckTacticalAnalyticsCard(workingCards, lang)

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (lang == "ru") "СОСТАВ КОЛОДЫ (ТАП — ОБЗОР):" else "DECK ROSTER (TAP TO INSPECT):",
            style = MaterialTheme.typography.labelMedium,
            color = c.textMid,
            modifier = Modifier.padding(vertical = Space.xxs)
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(98.dp)
                .background(c.panel, CyberShapes.chip)
                .border(1.dp, c.goldAntique.copy(alpha = 0.25f), CyberShapes.chip)
                .padding(Space.xs),
            horizontalArrangement = Arrangement.spacedBy(Space.xs)
        ) {
            items(workingCards.mapIndexed { idx, cardIdx -> Pair(idx, cardIdx) }) { (listIdx, cardIdx) ->
                val card = CyberTarotDeck.cards.getOrNull(cardIdx)
                val up = cardUpgrades[cardIdx]
                val element = when (cardIdx % 4) {
                    0 -> ElementAffinity.CYBER
                    1 -> ElementAffinity.BIO
                    2 -> ElementAffinity.QUANTUM
                    else -> ElementAffinity.VOID
                }

                Surface(
                    shape = CyberShapes.chip,
                    color = c.panelRaised,
                    border = BorderStroke(1.dp, element.color.copy(alpha = 0.8f)),
                    onClick = {
                        if (card != null) {
                            CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.CARD_DRAW)
                            showInspectCard = card
                        }
                    },
                    modifier = Modifier
                        .width(74.dp)
                        .fillMaxHeight()
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(Space.xs),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${((card?.defaultRarity?.let {
                                        when (it) {
                                            "Genesis", "Legend" -> 5
                                            "Epic" -> 4
                                            "Rare" -> 3
                                            else -> 1 + (cardIdx % 3)
                                        }
                                    } ?: 2) - (up?.energyDiscount ?: 0)).coerceAtLeast(1)}R",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonAmber
                                )
                                Text(
                                    text = element.symbol,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(end = 16.dp)
                                )
                            }

                            Text(
                                text = card?.name ?: "#$cardIdx",
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                color = c.textHigh
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val basePwr = 2 + (cardIdx % 4) + (up?.bonusPower ?: 0)
                                val baseDef = 2 + ((cardIdx + 1) % 4) + (up?.bonusDefense ?: 0)
                                Text(text = "A$basePwr", style = MaterialTheme.typography.labelSmall, color = c.danger)
                                Text(text = "D$baseDef", style = MaterialTheme.typography.labelSmall, color = c.emeraldGlow)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                                .clip(CyberShapes.chip)
                                .background(c.ink.copy(alpha = 0.85f))
                                .clickable {
                                    CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.CLICK)
                                    val mutable = workingCards.toMutableList()
                                    mutable.removeAt(listIdx)
                                    workingCards = mutable
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = if (lang == "ru") "Убрать из колоды" else "Remove from deck",
                                tint = c.danger,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Space.sm))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Space.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text(if (lang == "ru") "Поиск аркана..." else "Search arcana...", style = MaterialTheme.typography.labelMedium) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
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

        val filteredCards = remember(searchQuery, selectedElementFilter, selectedRarityFilter) {
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
                val matchRarity = selectedRarityFilter == "All" || card.defaultRarity.equals(selectedRarityFilter, ignoreCase = true)
                matchElem && matchSearch && matchRarity
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(Space.xs)
        ) {
            items(filteredCards) { card ->
                val inDeckCount = workingCards.count { it == card.index }
                val up = cardUpgrades[card.index]
                val elem = when (card.index % 4) {
                    0 -> ElementAffinity.CYBER
                    1 -> ElementAffinity.BIO
                    2 -> ElementAffinity.QUANTUM
                    else -> ElementAffinity.VOID
                }

                Surface(
                    shape = CyberShapes.chip,
                    color = c.panel,
                    border = BorderStroke(1.dp, if (inDeckCount > 0) c.emeraldGlow.copy(alpha = 0.5f) else c.goldAntique.copy(alpha = 0.2f)),
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
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.CARD_DRAW)
                                    showInspectCard = card
                                }
                        ) {
                            FramedTarotArtworkWindow(
                                card = card,
                                isUpright = true,
                                compactWatermark = true,
                                showDiagonalWatermark = false,
                                rarityBorderColor = elem.color.copy(alpha = 0.7f),
                                cornerRadius = 6,
                                modifier = Modifier
                                    .width(34.dp)
                                    .height(50.dp)
                            )

                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(Space.xs)
                                ) {
                                    Text(
                                        text = card.name,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = c.textHigh,
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
                                    text = "${getArcanaRomanNumeral(card.index)} • ${card.traditionalName} • ${card.defaultRarity}",
                                    style = MaterialTheme.typography.bodySmall,
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
                            if (inDeckCount > 0) {
                                Text(
                                    text = "$inDeckCount/2",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = c.emeraldGlow
                                )
                            }

                            Button(
                                onClick = {
                                    if (inDeckCount < 2 && workingCards.size < 22) {
                                        CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.CLICK)
                                        workingCards = workingCards + card.index
                                    }
                                },
                                enabled = inDeckCount < 2 && workingCards.size < 22,
                                colors = ButtonDefaults.buttonColors(containerColor = elem.color.copy(alpha = 0.85f)),
                                shape = CyberShapes.chip,
                                contentPadding = PaddingValues(horizontal = Space.md, vertical = Space.xs),
                                modifier = Modifier.heightIn(min = 48.dp)
                            ) {
                                Text("+", style = MaterialTheme.typography.titleMedium, color = c.ink)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewDeckDialog) {
        NewDeckCreateDialog(
            lang = lang,
            onDismiss = { showNewDeckDialog = false },
            onCreate = { name, icon, templateIndices ->
                viewModel.saveDeck(
                    name = name,
                    icon = icon,
                    cardIndices = templateIndices,
                    makeActive = true,
                    description = "Custom synthesized cyber deck"
                )
                showNewDeckDialog = false
            }
        )
    }

    if (showDeleteConfirmDialog && currentDeck != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(if (lang == "ru") "Удалить колоду?" else "Delete Deck?") },
            text = { Text(if (lang == "ru") "Вы уверены, что хотите удалить ${currentDeck.name}?" else "Delete ${currentDeck.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteDeck(currentDeck)
                        showDeleteConfirmDialog = false
                        selectedDeckId = savedDecks.firstOrNull { it.id != currentDeck.id }?.id ?: 0
                    }
                ) {
                    Text(if (lang == "ru") "Удалить" else "Delete", color = c.danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(if (lang == "ru") "Отмена" else "Cancel")
                }
            }
        )
    }

    showInspectCard?.let { cardToInspect ->
        WeedGuruCardInspectorDialog(
            card = cardToInspect,
            isUpright = true,
            systemLanguage = lang,
            onDismiss = { showInspectCard = null }
        )
    }
}

@Composable
private fun DeckTacticalAnalyticsCard(
    cardIndices: List<Int>,
    lang: String
) {
    // Calculate Mana Curve (1 to 5+)
    val costCounts = remember(cardIndices) {
        val map = mutableMapOf(1 to 0, 2 to 0, 3 to 0, 4 to 0, 5 to 0)
        cardIndices.forEach { idx ->
            val cost = when (idx % 3) {
                0 -> 1
                1 -> 2
                else -> 3
            }
            val key = cost.coerceIn(1, 5)
            map[key] = (map[key] ?: 0) + 1
        }
        map
    }

    // Calculate Elements
    val elemCounts = remember(cardIndices) {
        val map = mutableMapOf(
            ElementAffinity.CYBER to 0,
            ElementAffinity.BIO to 0,
            ElementAffinity.QUANTUM to 0,
            ElementAffinity.VOID to 0
        )
        cardIndices.forEach { idx ->
            val elem = when (idx % 4) {
                0 -> ElementAffinity.CYBER
                1 -> ElementAffinity.BIO
                2 -> ElementAffinity.QUANTUM
                else -> ElementAffinity.VOID
            }
            map[elem] = (map[elem] ?: 0) + 1
        }
        map
    }

    val c = Cyber.colors
    val maxCostCount = costCounts.values.maxOrNull()?.coerceAtLeast(1) ?: 1

    Surface(
        shape = CyberShapes.chip,
        color = c.panel,
        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(Space.sm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (lang == "ru") "КРИВАЯ RAM (МАНА)" else "RAM MANA CURVE",
                    style = MaterialTheme.typography.labelMedium,
                    color = NeonCyan
                )

                Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                    ElementAffinity.values().forEach { elem ->
                        val count = elemCounts[elem] ?: 0
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(text = elem.symbol, style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "$count",
                                style = MaterialTheme.typography.labelSmall,
                                color = elem.color
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Space.xs))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Bottom
            ) {
                (1..5).forEach { ramCost ->
                    val count = costCounts[ramCost] ?: 0
                    val barHeightFraction = (count.toFloat() / maxCostCount).coerceIn(0.1f, 1f)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (count > 0) "$count" else "",
                            style = MaterialTheme.typography.labelSmall,
                            color = c.textMid
                        )
                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .height((20 * barHeightFraction).dp)
                                .clip(CyberShapes.chip)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(NeonCyan, c.emerald)
                                    )
                                )
                        )
                        Text(
                            text = if (ramCost == 5) "5+" else "$ramCost",
                            style = MaterialTheme.typography.labelSmall,
                            color = c.textMid
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NewDeckCreateDialog(
    lang: String,
    onDismiss: () -> Unit,
    onCreate: (name: String, icon: String, templateIndices: List<Int>) -> Unit
) {
    val c = Cyber.colors
    var deckName by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("⚡") }
    var selectedArchetype by remember { mutableStateOf(0) }

    val archetypes = listOf(
        Pair("Cyber Aggro ⚡", listOf(0, 4, 8, 12, 16, 20, 24, 28, 32, 36, 40, 44, 48, 52, 56)),
        Pair("Bio Regen 🌿", listOf(1, 5, 9, 13, 17, 21, 25, 29, 33, 37, 41, 45, 49, 53, 57)),
        Pair("Quantum Barrier 🔮", listOf(2, 6, 10, 14, 18, 22, 26, 30, 34, 38, 42, 46, 50, 54, 58)),
        Pair("Void Control 🌑", listOf(3, 7, 11, 15, 19, 23, 27, 31, 35, 39, 43, 47, 51, 55, 59))
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = CyberShapes.card,
            color = c.panel,
            border = BorderStroke(1.dp, NeonCyan),
            modifier = Modifier.padding(Space.md)
        ) {
            Column(
                modifier = Modifier.padding(Space.lg),
                verticalArrangement = Arrangement.spacedBy(Space.md)
            ) {
                Text(
                    text = if (lang == "ru") "СОЗДАНИЕ КИБЕР-КОЛОДЫ" else "SYNTHESIZE NEW DECK",
                    style = MaterialTheme.typography.titleSmall,
                    color = NeonCyan
                )

                OutlinedTextField(
                    value = deckName,
                    onValueChange = { deckName = it },
                    label = { Text(if (lang == "ru") "Название колоды" else "Deck Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = if (lang == "ru") "Выберите иконку:" else "Select Icon:",
                    style = MaterialTheme.typography.labelMedium,
                    color = c.textMid
                )
                Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                    listOf("⚡", "🌿", "🔮", "🌑", "⚔️", "🧬", "🛡️").forEach { ic ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CyberShapes.chip)
                                .background(if (selectedIcon == ic) NeonCyan.copy(alpha = 0.25f) else c.panelRaised)
                                .border(1.dp, if (selectedIcon == ic) NeonCyan else Color.Transparent, CyberShapes.chip)
                                .clickable { selectedIcon = ic },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = ic, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }

                Text(
                    text = if (lang == "ru") "Стартовый архетип:" else "Base Archetype:",
                    style = MaterialTheme.typography.labelMedium,
                    color = c.textMid
                )
                Column(verticalArrangement = Arrangement.spacedBy(Space.xs)) {
                    archetypes.forEachIndexed { index, (label, _) ->
                        Surface(
                            shape = CyberShapes.chip,
                            color = if (selectedArchetype == index) NeonCyan.copy(alpha = 0.2f) else c.panelRaised,
                            border = BorderStroke(1.dp, if (selectedArchetype == index) NeonCyan else c.goldAntique.copy(alpha = 0.25f)),
                            onClick = { selectedArchetype = index },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                        ) {
                            Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.padding(horizontal = Space.md)) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = c.textHigh
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (lang == "ru") "Отмена" else "Cancel", color = c.textMid)
                    }
                    Button(
                        onClick = {
                            val finalName = if (deckName.isNotBlank()) deckName else archetypes[selectedArchetype].first
                            onCreate(finalName, selectedIcon, archetypes[selectedArchetype].second)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Text(if (lang == "ru") "Создать" else "Create", color = c.ink, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
