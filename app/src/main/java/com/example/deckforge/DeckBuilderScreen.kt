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
import androidx.compose.ui.text.font.FontFamily
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050811))
            .padding(horizontal = 12.dp)
            .testTag("deck_builder_screen")
    ) {
        // Top Header
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
                        text = if (lang == "ru") "КОНСТРУКТОР КОЛОД" else "DECK FORGE & TACTICS",
                        color = Color(0xFF00F5FF),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (lang == "ru") "Сборка, кривая RAM и стихийный баланс" else "Tactical synergy & RAM curve analysis",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Quick Play Button
            Button(
                onClick = onLaunchDuel,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF66)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (lang == "ru") "В БОЙ ⚔️" else "BATTLE ⚔️",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Saved Decks Selector Bar
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(savedDecks) { deck ->
                val isSelected = deck.id == currentDeck?.id
                val isActive = deck.isActive
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) primaryColor.copy(alpha = 0.25f) else Color(0xFF0E1322),
                    border = BorderStroke(
                        1.dp,
                        if (isActive) Color(0xFF00FF66) else if (isSelected) primaryColor else Color(0x33FFFFFF)
                    ),
                    modifier = Modifier
                        .clickable {
                            CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.CLICK)
                            selectedDeckId = deck.id
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = deck.icon, fontSize = 14.sp)
                        Text(
                            text = deck.name,
                            color = if (isSelected) Color.White else Color.LightGray,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        if (isActive) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00FF66))
                            )
                        }
                    }
                }
            }

            // New Deck Button (up to 5 decks)
            if (savedDecks.size < 5) {
                item {
                    OutlinedButton(
                        onClick = {
                            CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.CLICK)
                            showNewDeckDialog = true
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF00F5FF).copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("+", color = Color(0xFF00F5FF), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (lang == "ru") "Новая" else "New", color = Color(0xFF00F5FF), fontSize = 11.sp)
                    }
                }
            }
        }

        // Active Deck Actions & Validation Strip
        currentDeck?.let { deck ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(Color(0xFF0A0F1D), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${workingCards.size}/15",
                        color = if (isDeckValid) Color(0xFF00FF66) else Color(0xFFFFB800),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                    Text(
                        text = if (isDeckValid) {
                            if (lang == "ru") "ГОТОВА К БОЮ" else "VALID FOR BATTLE"
                        } else {
                            if (lang == "ru") "ТРЕБУЕТСЯ 15 КАРТ" else "NEEDS 15 CARDS"
                        },
                        color = if (isDeckValid) Color(0xFF00FF66) else Color(0xFFFFB800),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (!deck.isActive && isDeckValid) {
                        OutlinedButton(
                            onClick = {
                                CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
                                viewModel.setActiveDeck(deck.id)
                            },
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(
                                if (lang == "ru") "Активировать" else "Set Active",
                                fontSize = 10.sp,
                                color = Color(0xFF00FF66)
                            )
                        }
                    }

                    // Save Deck Changes Button
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
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            if (lang == "ru") "Сохранить" else "Save",
                            fontSize = 10.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (savedDecks.size > 1 && !deck.isActive) {
                        IconButton(
                            onClick = { showDeleteConfirmDialog = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Tactical Analytics: Mana Curve & Element Synergies
        DeckTacticalAnalyticsCard(workingCards, lang)

        Spacer(modifier = Modifier.height(6.dp))

        // Deck Slots / Working Cards Scroll
        Text(
            text = if (lang == "ru") "СОСТАВ КОЛОДЫ (НАЖМИТЕ ДЛЯ УДАЛЕНИЯ):" else "DECK ROSTER (TAP TO REMOVE):",
            color = Color.LightGray,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(vertical = 2.dp)
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .background(Color(0xFF080C18), RoundedCornerShape(8.dp))
                .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(8.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF101626),
                    border = BorderStroke(1.dp, element.color.copy(alpha = 0.8f)),
                    modifier = Modifier
                        .width(62.dp)
                        .fillMaxHeight()
                        .clickable {
                            CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.CLICK)
                            val mutable = workingCards.toMutableList()
                            mutable.removeAt(listIdx)
                            workingCards = mutable
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${((card?.defaultRarity?.let {
                                    when (it) {
                                        "Genesis", "Legend" -> 5
                                        "Epic" -> 4
                                        "Rare" -> 3
                                        else -> 1 + (cardIdx % 3)
                                    }
                                } ?: 2) - (up?.energyDiscount ?: 0)).coerceAtLeast(1)}⚡",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFB800)
                            )
                            Text(text = element.symbol, fontSize = 9.sp)
                        }

                        Text(
                            text = card?.name ?: "#$cardIdx",
                            fontSize = 8.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val basePwr = 2 + (cardIdx % 4) + (up?.bonusPower ?: 0)
                            val baseDef = 2 + ((cardIdx + 1) % 4) + (up?.bonusDefense ?: 0)
                            Text(text = "⚔$basePwr", fontSize = 8.sp, color = Color(0xFFFF5252))
                            Text(text = "🛡$baseDef", fontSize = 8.sp, color = Color(0xFF00FF66))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Catalog Search & Filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text(if (lang == "ru") "Поиск аркана..." else "Search arcana...", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00F5FF),
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

        Spacer(modifier = Modifier.height(6.dp))

        // Catalog Pool List
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
            verticalArrangement = Arrangement.spacedBy(6.dp)
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
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0B101E),
                    border = BorderStroke(1.dp, if (inDeckCount > 0) Color(0xFF00FF66).copy(alpha = 0.5f) else Color(0x1FFFFFFF)),
                    modifier = Modifier.fillMaxWidth()
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(elem.color.copy(alpha = 0.15f))
                                    .border(1.dp, elem.color.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = elem.symbol, fontSize = 16.sp)
                            }

                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = card.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    if (up != null && up.upgradeLevel > 1) {
                                        Text(
                                            text = "★${up.upgradeLevel}",
                                            color = Color(0xFFFFD700),
                                            fontSize = 10.sp,
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

                        // Add / Remove Controls
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (inDeckCount > 0) {
                                Text(
                                    text = "$inDeckCount/2",
                                    color = Color(0xFF00FF66),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
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
                                colors = ButtonDefaults.buttonColors(containerColor = elem.color.copy(alpha = 0.8f)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("+", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // New Deck Modal
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

    // Delete Deck Confirmation
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
                    Text(if (lang == "ru") "Удалить" else "Delete", color = Color(0xFFFF5252))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(if (lang == "ru") "Отмена" else "Cancel")
                }
            }
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

    val maxCostCount = costCounts.values.maxOrNull()?.coerceAtLeast(1) ?: 1

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF090D1A),
        border = BorderStroke(1.dp, Color(0x2200F5FF)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (lang == "ru") "КРИВАЯ RAM (МАНА)" else "RAM MANA CURVE",
                    color = Color(0xFF00F5FF),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                // Element counts
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ElementAffinity.values().forEach { elem ->
                        val count = elemCounts[elem] ?: 0
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(text = elem.symbol, fontSize = 10.sp)
                            Text(
                                text = "$count",
                                fontSize = 10.sp,
                                color = elem.color,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Bar Charts for RAM
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
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
                            fontSize = 8.sp,
                            color = Color.LightGray,
                            fontFamily = FontFamily.Monospace
                        )
                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .height((24 * barHeightFraction).dp)
                                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF00F5FF), Color(0xFF0055FF))
                                    )
                                )
                        )
                        Text(
                            text = if (ramCost == 5) "5+" else "$ramCost",
                            fontSize = 9.sp,
                            color = Color.Gray,
                            fontFamily = FontFamily.Monospace
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
    var deckName by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("⚡") }
    var selectedArchetype by remember { mutableStateOf(0) } // 0: Cyber, 1: Bio, 2: Quantum, 3: Void

    val archetypes = listOf(
        Pair("Cyber Aggro ⚡", listOf(0, 4, 8, 12, 16, 20, 24, 28, 32, 36, 40, 44, 48, 52, 56)),
        Pair("Bio Regen 🌿", listOf(1, 5, 9, 13, 17, 21, 25, 29, 33, 37, 41, 45, 49, 53, 57)),
        Pair("Quantum Barrier 🔮", listOf(2, 6, 10, 14, 18, 22, 26, 30, 34, 38, 42, 46, 50, 54, 58)),
        Pair("Void Control 🌑", listOf(3, 7, 11, 15, 19, 23, 27, 31, 35, 39, 43, 47, 51, 55, 59))
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0C101E),
            border = BorderStroke(1.dp, Color(0xFF00F5FF)),
            modifier = Modifier.padding(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (lang == "ru") "СОЗДАНИЕ КИБЕР-КОЛОДЫ" else "SYNTHESIZE NEW DECK",
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF00F5FF),
                    fontSize = 14.sp
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
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("⚡", "🌿", "🔮", "🌑", "⚔️", "🧬", "🛡️").forEach { ic ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedIcon == ic) Color(0xFF00F5FF).copy(alpha = 0.3f) else Color(0xFF131A2C))
                                .border(1.dp, if (selectedIcon == ic) Color(0xFF00F5FF) else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { selectedIcon = ic },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = ic, fontSize = 16.sp)
                        }
                    }
                }

                Text(
                    text = if (lang == "ru") "Стартовый архетип:" else "Base Archetype:",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    archetypes.forEachIndexed { index, (label, _) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedArchetype == index) Color(0xFF00F5FF).copy(alpha = 0.2f) else Color(0xFF131A2C),
                            border = BorderStroke(1.dp, if (selectedArchetype == index) Color(0xFF00F5FF) else Color(0x22FFFFFF)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedArchetype = index }
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(8.dp),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (lang == "ru") "Отмена" else "Cancel", color = Color.Gray)
                    }
                    Button(
                        onClick = {
                            val finalName = if (deckName.isNotBlank()) deckName else archetypes[selectedArchetype].first
                            onCreate(finalName, selectedIcon, archetypes[selectedArchetype].second)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F5FF))
                    ) {
                        Text(if (lang == "ru") "Создать" else "Create", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
