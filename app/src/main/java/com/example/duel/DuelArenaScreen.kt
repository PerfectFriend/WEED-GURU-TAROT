package com.example.duel

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.example.viewmodel.TarotViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuelArenaScreen(
    viewModel: TarotViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val coroutineScope = rememberCoroutineScope()
    val engine = remember { DuelCombatEngine() }
    val duelState by engine.state.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val activeDeck by viewModel.activeDeck.collectAsStateWithLifecycle()
    val lang = userProfile?.systemLanguage ?: "en"

    var selectedHandCard by remember { mutableStateOf<BattleCard?>(null) }
    var showSurrenderConfirm by remember { mutableStateOf(false) }
    var isP2pLobbyOpen by remember { mutableStateOf(false) }

    if (isP2pLobbyOpen) {
        com.example.p2p.P2pDuelLobbyScreen(
            viewModel = viewModel,
            onBack = { isP2pLobbyOpen = false },
            onStartP2pDuel = { node ->
                isP2pLobbyOpen = false
                coroutineScope.launch {
                    val battleCards = viewModel.getActiveBattleDeck()
                    val deckName = activeDeck?.name ?: "Genesis Cyber Deck"
                    engine.startMatch(difficulty = "Hardcore", customPlayerDeck = battleCards, deckName = "$deckName (vs ${node.name})")
                }
            }
        )
        return
    }

    var rewardedForThisMatch by remember { mutableStateOf(false) }
    val radioState by com.example.audio.CyberRadioPlayer.state.collectAsStateWithLifecycle()
    var showRadioInDuel by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val battleCards = viewModel.getActiveBattleDeck()
        val deckName = activeDeck?.name ?: "Genesis Cyber Deck"
        engine.startMatch(difficulty = "Normal", customPlayerDeck = battleCards, deckName = deckName)
    }

    LaunchedEffect(duelState.winner) {
        if (duelState.winner == DuelWinner.PLAYER && !rewardedForThisMatch) {
            rewardedForThisMatch = true
            viewModel.addWeedCredits(150)
        } else if (duelState.winner == null) {
            rewardedForThisMatch = false
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "duel_pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF04060C))
    ) {
        // Background Matrix Grid effect
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 48.dp.toPx()
            for (x in 0..(size.width / step).toInt()) {
                drawLine(
                    color = Color(0xFF0D1B2A).copy(alpha = 0.4f),
                    start = Offset(x * step, 0f),
                    end = Offset(x * step, size.height),
                    strokeWidth = 1f
                )
            }
            for (y in 0..(size.height / step).toInt()) {
                drawLine(
                    color = Color(0xFF0D1B2A).copy(alpha = 0.4f),
                    start = Offset(0f, y * step),
                    end = Offset(size.width, y * step),
                    strokeWidth = 1f
                )
            }
        }

        val duelScrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(duelScrollState)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            // Top HUD
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (duelState.winner == null) {
                            showSurrenderConfirm = true
                        } else {
                            onBack()
                        }
                    },
                    modifier = Modifier.testTag("duel_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Exit Duel",
                        tint = currentSkin.primaryColor
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0x99111827),
                    border = BorderStroke(1.dp, currentSkin.primaryColor.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (lang == "ru") "ХОД ${duelState.turnNumber}" else "TURN ${duelState.turnNumber}",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = currentSkin.primaryColor,
                            fontSize = 12.sp
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (duelState.isPlayerTurn && !duelState.isAiThinking) Color(0xFF00FF66) else Color(0xFFFF5252)
                                )
                        )
                        Text(
                            text = if (duelState.isPlayerTurn && !duelState.isAiThinking) {
                                if (lang == "ru") "ВАШ ХОД" else "YOUR TURN"
                            } else {
                                if (lang == "ru") "ХОД ИИ..." else "AI TURN..."
                            },
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // DI.FM Cyber Radio Console Launcher Button
                    IconButton(
                        onClick = { showRadioInDuel = true },
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                if (radioState.isPlaying) currentSkin.primaryColor.copy(alpha = 0.2f) else Color(0x15FFFFFF),
                                CircleShape
                            )
                            .border(
                                1.dp,
                                if (radioState.isPlaying) currentSkin.primaryColor else Color(0x22FFFFFF),
                                CircleShape
                            )
                            .testTag("duel_radio_button")
                    ) {
                        if (radioState.isPlaying) {
                            Row(
                                modifier = Modifier
                                    .width(14.dp)
                                    .height(11.dp),
                                horizontalArrangement = Arrangement.spacedBy(1.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                radioState.visualizerBars.take(3).forEach { bar ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(bar.coerceIn(0.25f, 1f))
                                            .background(currentSkin.primaryColor)
                                    )
                                }
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Radio,
                                contentDescription = "Radio DI.FM",
                                tint = if (radioState.isBuffering) Color(0xFFFFB800) else Color.LightGray,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.CLICK)
                            isP2pLobbyOpen = true
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF00FF66)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("P2P 🌐", fontSize = 10.sp, color = Color(0xFF00FF66), fontWeight = FontWeight.Bold)
                    }

                    IconButton(
                        onClick = { engine.startMatch("Normal") },
                        modifier = Modifier.testTag("duel_restart_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restart Match",
                            tint = Color(0xFFAAAAAA)
                        )
                    }
                }
            }

            // Enemy Zone
            EnemyZoneCard(
                state = duelState,
                lang = lang
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Enemy Board (3 slots)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in 0..2) {
                    Box(modifier = Modifier.weight(1f)) {
                        val enemyCard = duelState.enemyBoard[i]
                        if (enemyCard != null) {
                            BoardCardView(
                                card = enemyCard,
                                isPlayer = false,
                                isTargetable = duelState.isPlayerTurn && !duelState.isAiThinking,
                                onClick = {}
                            )
                        } else {
                            EmptySlotView(
                                slotLabel = "${i + 1}",
                                isEnemy = true,
                                isHighlighted = false,
                                onClick = {}
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Center Clash Zone: Elemental Matrix & Terminal Combat Log
            CombatCenterStrip(
                state = duelState,
                primaryColor = currentSkin.primaryColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Player Board (3 slots)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in 0..2) {
                    Box(modifier = Modifier.weight(1f)) {
                        val playerCard = duelState.playerBoard[i]
                        if (playerCard != null) {
                            BoardCardView(
                                card = playerCard,
                                isPlayer = true,
                                isTargetable = false,
                                canAttack = duelState.isPlayerTurn && !duelState.isAiThinking && !playerCard.hasAttackedThisTurn,
                                onClick = {
                                    if (duelState.isPlayerTurn && !duelState.isAiThinking && !playerCard.hasAttackedThisTurn) {
                                        engine.attackWithSlot(i)
                                    }
                                }
                            )
                        } else {
                            val canDeploy = selectedHandCard != null &&
                                    duelState.isPlayerTurn &&
                                    !duelState.isAiThinking &&
                                    duelState.playerEnergy >= (selectedHandCard?.energyCost ?: 99)

                            EmptySlotView(
                                slotLabel = "${i + 1}",
                                isEnemy = false,
                                isHighlighted = canDeploy,
                                glowMultiplier = if (canDeploy) pulseGlow else 0.4f,
                                onClick = {
                                    selectedHandCard?.let { card ->
                                        if (engine.playCard(card, i)) {
                                            selectedHandCard = null
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Player Status Bar & Action Controls
            PlayerVitalStatsRow(
                state = duelState,
                playerName = userProfile?.alias ?: "Netrunner",
                primaryColor = currentSkin.primaryColor,
                lang = lang,
                onEndTurn = {
                    selectedHandCard = null
                    engine.endPlayerTurn(coroutineScope) { credits, xp ->
                        viewModel.addWeedCredits(credits)
                    }
                }
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Player Hand
            Text(
                text = if (lang == "ru") "КАРТЫ В РУКЕ (${duelState.playerHand.size}/6):" else "HAND CARDS (${duelState.playerHand.size}/6):",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = Color(0xFF888899),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(duelState.playerHand, key = { it.instanceId }) { card ->
                    val isSelected = selectedHandCard?.instanceId == card.instanceId
                    val canAfford = duelState.playerEnergy >= card.energyCost && duelState.isPlayerTurn && !duelState.isAiThinking

                    HandCardView(
                        card = card,
                        isSelected = isSelected,
                        canAfford = canAfford,
                        onClick = {
                            selectedHandCard = if (isSelected) null else card
                        }
                    )
                }
            }
        }

        // Victory / Defeat Overlay Dialog
        if (duelState.winner != null) {
            DuelResultDialog(
                winner = duelState.winner!!,
                lang = lang,
                primaryColor = currentSkin.primaryColor,
                onRestart = {
                    rewardedForThisMatch = false
                    coroutineScope.launch {
                        val battleCards = viewModel.getActiveBattleDeck()
                        val deckName = activeDeck?.name ?: "Genesis Cyber Deck"
                        engine.startMatch("Normal", customPlayerDeck = battleCards, deckName = deckName)
                    }
                },
                onExit = onBack
            )
        }

        // Surrender confirm dialog
        if (showSurrenderConfirm) {
            AlertDialog(
                onDismissRequest = { showSurrenderConfirm = false },
                title = { Text(if (lang == "ru") "Покинуть дуэль?" else "Surrender match?") },
                text = { Text(if (lang == "ru") "Текущий прогресс матча будет потерян." else "Current match progress will be forfeited.") },
                confirmButton = {
                    TextButton(onClick = {
                        showSurrenderConfirm = false
                        onBack()
                    }) {
                        Text(if (lang == "ru") "Сдаться" else "Surrender", color = Color(0xFFFF5252))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSurrenderConfirm = false }) {
                        Text(if (lang == "ru") "Продолжить" else "Resume")
                    }
                }
            )
        }

        if (showRadioInDuel) {
            com.example.ui.components.CyberRadioConsoleDialog(
                onDismiss = { showRadioInDuel = false }
            )
        }
    }
}

@Composable
private fun EnemyZoneCard(
    state: DuelState,
    lang: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xE6121422),
        border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2A0A10))
                        .border(1.dp, Color(0xFFFF5252), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🤖", fontSize = 18.sp)
                }

                Column {
                    Text(
                        text = if (lang == "ru") "Нейро-Демиург v4" else "Neuro-Demiurge v4",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "HP: ${state.enemyHp}/${state.enemyMaxHp}",
                            color = Color(0xFFFF5252),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        if (state.enemyShield > 0) {
                            Text(
                                text = "🛡️ +${state.enemyShield}",
                                color = Color(0xFF00F5FF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Enemy RAM dots & Hand count
            Column(horizontalAlignment = Alignment.End) {
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    for (i in 1..state.enemyMaxEnergy) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (i <= state.enemyEnergy) Color(0xFFFFB800) else Color(0xFF333344)
                                )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${state.enemyHandSize} cards in hand",
                    color = Color(0xFF888899),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun CombatCenterStrip(
    state: DuelState,
    primaryColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xCC090C15),
        border = BorderStroke(1.dp, Color(0xFF1F2937))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            // Elemental Circle Reminder
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ElementTag(ElementAffinity.CYBER)
                Text("➔", color = Color.Gray, fontSize = 9.sp)
                ElementTag(ElementAffinity.BIO)
                Text("➔", color = Color.Gray, fontSize = 9.sp)
                ElementTag(ElementAffinity.VOID)
                Text("➔", color = Color.Gray, fontSize = 9.sp)
                ElementTag(ElementAffinity.QUANTUM)
                Text("➔", color = Color.Gray, fontSize = 9.sp)
                ElementTag(ElementAffinity.CYBER)
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Latest Combat Log entry
            val latestLog = state.combatLog.firstOrNull() ?: "Готов к синхронизации."
            Text(
                text = latestLog,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = if (latestLog.contains("ПОБЕДА")) Color(0xFF00FF66)
                else if (latestLog.contains("ПОРАЖЕНИЕ")) Color(0xFFFF5252)
                else primaryColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ElementTag(element: ElementAffinity) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(element.symbol, fontSize = 10.sp)
        Text(
            text = element.name,
            color = element.color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun BoardCardView(
    card: BattleCard,
    isPlayer: Boolean,
    isTargetable: Boolean = false,
    canAttack: Boolean = false,
    onClick: () -> Unit
) {
    val borderColor = if (canAttack) Color(0xFF00F5FF) else card.element.color.copy(alpha = 0.7f)

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .clickable(enabled = canAttack || isTargetable, onClick = onClick)
            .testTag(if (isPlayer) "player_card_slot" else "enemy_card_slot"),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF101322),
        border = BorderStroke(if (canAttack) 2.dp else 1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Element symbol & Cost
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${card.element.symbol} ${card.element.name}",
                    fontSize = 9.sp,
                    color = card.element.color,
                    fontWeight = FontWeight.Bold
                )
                if (card.specialEffect != null) {
                    Text("⚡", fontSize = 9.sp, color = Color(0xFFFFB800))
                }
            }

            // Name
            Text(
                text = card.name,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Attack action button or status
            if (isPlayer) {
                if (canAttack) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF00F5FF),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚔️ АТАКА",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 9.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                } else if (card.hasAttackedThisTurn) {
                    Text(
                        text = "ОТДЫХ",
                        color = Color(0xFF666677),
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Stats footer: Power & Defense
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Power
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⚔️", fontSize = 10.sp)
                    Text(
                        text = "${card.currentPower}",
                        color = Color(0xFFFFB800),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
                // Defense / HP
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🛡️", fontSize = 10.sp)
                    Text(
                        text = "${card.currentDefense}",
                        color = Color(0xFF00FF66),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptySlotView(
    slotLabel: String,
    isEnemy: Boolean,
    isHighlighted: Boolean,
    glowMultiplier: Float = 1.0f,
    onClick: () -> Unit
) {
    val strokeColor = if (isHighlighted) Color(0xFF00F5FF).copy(alpha = glowMultiplier)
    else if (isEnemy) Color(0xFF332222)
    else Color(0xFF22283A)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF080B14))
            .drawBehind {
                drawRoundRect(
                    color = strokeColor,
                    style = Stroke(
                        width = if (isHighlighted) 2.dp.toPx() else 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                )
            }
            .clickable(enabled = isHighlighted, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isHighlighted) "ВЫЛОЖИТЬ" else "СЛОТ $slotLabel",
                fontFamily = FontFamily.Monospace,
                fontSize = if (isHighlighted) 10.sp else 9.sp,
                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
                color = if (isHighlighted) Color(0xFF00F5FF) else Color(0xFF555566)
            )
        }
    }
}

@Composable
private fun HandCardView(
    card: BattleCard,
    isSelected: Boolean,
    canAfford: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Color(0xFF00F5FF)
    else if (canAfford) card.element.color.copy(alpha = 0.8f)
    else Color(0xFF333344)

    Surface(
        modifier = Modifier
            .width(96.dp)
            .fillMaxHeight()
            .clickable(onClick = onClick)
            .testTag("hand_card_${card.cardIndex}"),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) Color(0xFF142038) else Color(0xFF0E121E),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Cost & Element
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (canAfford) Color(0xFF00F5FF) else Color(0xFF444455),
                    modifier = Modifier.size(18.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${card.energyCost}",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp
                        )
                    }
                }
                Text(
                    text = card.element.symbol,
                    fontSize = 12.sp
                )
            }

            // Card name
            Text(
                text = card.name,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = if (canAfford) Color.White else Color(0xFF888899),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 11.sp
            )

            // Special effect pill
            if (card.specialEffect != null) {
                Text(
                    text = card.specialEffect.titleRu,
                    fontSize = 8.sp,
                    color = Color(0xFFFFB800),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Power / Defense
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "⚔️${card.currentPower}",
                    color = Color(0xFFFFB800),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Text(
                    text = "🛡️${card.currentDefense}",
                    color = Color(0xFF00FF66),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun PlayerVitalStatsRow(
    state: DuelState,
    playerName: String,
    primaryColor: Color,
    lang: String,
    onEndTurn: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xE6101424),
        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Player Avatar info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF003838))
                        .border(1.dp, primaryColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚡", fontSize = 16.sp)
                }

                Column {
                    Text(
                        text = playerName,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "HP: ${state.playerHp}/${state.playerMaxHp}",
                            color = Color(0xFF00FF66),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        if (state.playerShield > 0) {
                            Text(
                                text = "🛡️ +${state.playerShield}",
                                color = Color(0xFF00F5FF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Player Energy RAM
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "RAM ${state.playerEnergy}/${state.playerMaxEnergy}",
                    color = Color(0xFF00F5FF),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    for (i in 1..state.playerMaxEnergy) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (i <= state.playerEnergy) Color(0xFF00F5FF) else Color(0xFF223344)
                                )
                        )
                    }
                }
            }

            // End Turn Button
            Button(
                onClick = onEndTurn,
                enabled = state.isPlayerTurn && !state.isAiThinking,
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    disabledContainerColor = Color(0xFF222838)
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier
                    .height(36.dp)
                    .testTag("end_turn_button")
            ) {
                Text(
                    text = if (lang == "ru") "ХОД ➔" else "END ➔",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun DuelResultDialog(
    winner: DuelWinner,
    lang: String,
    primaryColor: Color,
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    Dialog(onDismissRequest = onExit) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0B0F1C),
            border = BorderStroke(
                2.dp,
                if (winner == DuelWinner.PLAYER) Color(0xFF00FF66) else Color(0xFFFF5252)
            ),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (winner == DuelWinner.PLAYER) "🏆 ПОБЕДА!" else "💀 ПОРАЖЕНИЕ",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = if (winner == DuelWinner.PLAYER) Color(0xFF00FF66) else Color(0xFFFF5252)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (winner == DuelWinner.PLAYER) {
                        if (lang == "ru")
                            "Вы успешно перегрузили ядро Нейро-Демиурга!\nПолучено: +150 WEED Credits, +50 XP"
                        else
                            "You overcharged the Neuro-Demiurge core!\nRewards: +150 WEED Credits, +50 XP"
                    } else {
                        if (lang == "ru")
                            "Ваша нейросеть обесточена. Требуется калибровка колоды."
                        else
                            "Your neural node was disconnected. Recalibrate your deck."
                    },
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    color = Color.White,
                    fontFamily = FontFamily.SansSerif
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onExit,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (lang == "ru") "В Хаб" else "Hub", color = Color.White)
                    }

                    Button(
                        onClick = onRestart,
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (lang == "ru") "Реванш" else "Rematch", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
