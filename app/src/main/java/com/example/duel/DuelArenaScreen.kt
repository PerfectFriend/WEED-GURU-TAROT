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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.CyberNoirSynthPlayer
import com.example.ui.theme.Cyber
import com.example.ui.theme.CyberShapes
import com.example.ui.theme.IbmPlexMonoFontFamily
import com.example.ui.theme.IbmPlexSansFontFamily
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.Space
import com.example.ui.theme.rememberMotionEnabled
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

    val motionEnabled = rememberMotionEnabled()
    val pulseGlow = if (motionEnabled) {
        val infiniteTransition = rememberInfiniteTransition(label = "duel_pulse")
        infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_glow"
        ).value
    } else {
        0.7f
    }

    val c = Cyber.colors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(c.ink)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 48.dp.toPx()
            val gridColor = c.panelRaised.copy(alpha = 0.55f)
            for (x in 0..(size.width / step).toInt()) {
                drawLine(gridColor, Offset(x * step, 0f), Offset(x * step, size.height), 1f)
            }
            for (y in 0..(size.height / step).toInt()) {
                drawLine(gridColor, Offset(0f, y * step), Offset(size.width, y * step), 1f)
            }
        }

        val duelScrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(duelScrollState)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = Space.md, vertical = Space.xs)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Space.xs),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (duelState.winner == null) showSurrenderConfirm = true else onBack()
                    },
                    modifier = Modifier.size(48.dp).testTag("duel_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Exit Duel",
                        tint = currentSkin.primaryColor
                    )
                }

                Surface(
                    shape = CyberShapes.chip,
                    color = c.panel.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, currentSkin.primaryColor.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = Space.md, vertical = Space.xs),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Space.xs)
                    ) {
                        Text(
                            text = if (lang == "ru") "ХОД ${duelState.turnNumber}" else "TURN ${duelState.turnNumber}",
                            style = MaterialTheme.typography.labelMedium,
                            color = currentSkin.primaryColor
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (duelState.isPlayerTurn && !duelState.isAiThinking) c.emeraldGlow else c.danger
                                )
                        )
                        Text(
                            text = if (duelState.isPlayerTurn && !duelState.isAiThinking) {
                                if (lang == "ru") "ВАШ ХОД" else "YOUR TURN"
                            } else {
                                if (lang == "ru") "ХОД ИИ..." else "AI TURN..."
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = c.textHigh
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
                    IconButton(
                        onClick = { showRadioInDuel = true },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (radioState.isPlaying) currentSkin.primaryColor.copy(alpha = 0.2f) else c.panelRaised,
                                CircleShape
                            )
                            .border(
                                1.dp,
                                if (radioState.isPlaying) currentSkin.primaryColor else c.goldAntique.copy(alpha = 0.3f),
                                CircleShape
                            )
                            .testTag("duel_radio_button")
                    ) {
                        if (radioState.isPlaying) {
                            Row(
                                modifier = Modifier.width(16.dp).height(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(1.5.dp),
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
                                contentDescription = "Radio",
                                tint = if (radioState.isBuffering) NeonAmber else c.textMid,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.CLICK)
                            isP2pLobbyOpen = true
                        },
                        shape = CyberShapes.chip,
                        border = BorderStroke(1.dp, c.emeraldGlow),
                        contentPadding = PaddingValues(horizontal = Space.sm, vertical = Space.xs),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Public, contentDescription = null, tint = c.emeraldGlow, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("P2P", style = MaterialTheme.typography.labelMedium, color = c.emeraldGlow)
                    }

                    IconButton(
                        onClick = { engine.startMatch("Normal") },
                        modifier = Modifier.size(48.dp).testTag("duel_restart_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restart Match",
                            tint = c.textMid
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

            Text(
                text = if (lang == "ru") "КАРТЫ В РУКЕ (${duelState.playerHand.size}/6):" else "HAND CARDS (${duelState.playerHand.size}/6):",
                style = MaterialTheme.typography.labelMedium,
                color = c.textMid,
                modifier = Modifier.padding(horizontal = Space.xs, vertical = Space.xxs)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(136.dp),
                horizontalArrangement = Arrangement.spacedBy(Space.sm),
                contentPadding = PaddingValues(horizontal = Space.xs)
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

        duelState.winner?.let { duelWinner ->
            DuelResultDialog(
                winner = duelWinner,
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
                        Text(if (lang == "ru") "Сдаться" else "Surrender", color = c.danger)
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
                systemLanguage = lang,
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
    val c = Cyber.colors
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CyberShapes.cardCompact,
        color = c.panel.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, c.danger.copy(alpha = 0.55f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Space.md, vertical = Space.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.sm)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(c.danger.copy(alpha = 0.16f))
                        .border(1.dp, c.danger, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Memory, contentDescription = null, tint = c.danger, modifier = Modifier.size(18.dp))
                }

                Column {
                    Text(
                        text = if (lang == "ru") "Нейро-Демиург v4" else "Neuro-Demiurge v4",
                        style = MaterialTheme.typography.titleSmall,
                        color = c.textHigh
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Space.xs)
                    ) {
                        Text(
                            text = "HP: ${state.enemyHp}/${state.enemyMaxHp}",
                            style = MaterialTheme.typography.labelSmall,
                            color = c.danger
                        )
                        if (state.enemyShield > 0) {
                            Text(
                                text = "SH +${state.enemyShield}",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonCyan
                            )
                        }
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    for (i in 1..state.enemyMaxEnergy) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CyberShapes.chip)
                                .background(
                                    if (i <= state.enemyEnergy) NeonAmber else c.panelRaised
                                )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Space.xs))
                Text(
                    text = if (lang == "ru") "${state.enemyHandSize} в руке" else "${state.enemyHandSize} in hand",
                    style = MaterialTheme.typography.labelSmall,
                    color = c.textMid
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
    val c = Cyber.colors
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CyberShapes.chip,
        color = c.panel.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, c.goldAntique.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = Space.sm, vertical = Space.xs)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ElementTag(ElementAffinity.CYBER)
                Text("➔", style = MaterialTheme.typography.labelSmall, color = c.textMid)
                ElementTag(ElementAffinity.BIO)
                Text("➔", style = MaterialTheme.typography.labelSmall, color = c.textMid)
                ElementTag(ElementAffinity.VOID)
                Text("➔", style = MaterialTheme.typography.labelSmall, color = c.textMid)
                ElementTag(ElementAffinity.QUANTUM)
                Text("➔", style = MaterialTheme.typography.labelSmall, color = c.textMid)
                ElementTag(ElementAffinity.CYBER)
            }

            Spacer(modifier = Modifier.height(Space.xs))

            val latestLog = state.combatLog.firstOrNull() ?: "Готов к синхронизации."
            Text(
                text = latestLog,
                style = MaterialTheme.typography.labelSmall,
                color = when {
                    latestLog.contains("ПОБЕДА") -> c.emeraldGlow
                    latestLog.contains("ПОРАЖЕНИЕ") -> c.danger
                    else -> primaryColor
                },
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
        Text(element.symbol, style = MaterialTheme.typography.labelSmall)
        Text(
            text = element.name,
            color = element.color,
            style = MaterialTheme.typography.labelSmall
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
    val c = Cyber.colors
    val borderColor = if (canAttack) NeonCyan else card.element.color.copy(alpha = 0.7f)

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .clickable(enabled = canAttack || isTargetable, onClick = onClick)
            .testTag(if (isPlayer) "player_card_slot" else "enemy_card_slot"),
        shape = CyberShapes.chip,
        color = c.panelRaised,
        border = BorderStroke(if (canAttack) 2.dp else 1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Space.xs),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${card.element.symbol} ${card.element.name}",
                    style = MaterialTheme.typography.labelSmall,
                    color = card.element.color,
                    maxLines = 1
                )
                if (card.specialEffect != null) {
                    Text("⚡", style = MaterialTheme.typography.labelSmall, color = NeonAmber)
                }
            }

            Text(
                text = card.name,
                style = MaterialTheme.typography.labelSmall,
                color = c.textHigh,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            if (isPlayer) {
                if (canAttack) {
                    Surface(
                        shape = CyberShapes.chip,
                        color = NeonCyan,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "АТАКА",
                            style = MaterialTheme.typography.labelSmall,
                            color = c.ink,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = Space.xxs)
                        )
                    }
                } else if (card.hasAttackedThisTurn) {
                    Text(
                        text = "ОТДЫХ",
                        style = MaterialTheme.typography.labelSmall,
                        color = c.textMid,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "A:${card.currentPower}",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonAmber
                )
                Text(
                    text = "D:${card.currentDefense}",
                    style = MaterialTheme.typography.labelSmall,
                    color = c.emeraldGlow
                )
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
    val c = Cyber.colors
    val strokeColor = if (isHighlighted) NeonCyan.copy(alpha = glowMultiplier)
    else if (isEnemy) c.danger.copy(alpha = 0.25f)
    else c.goldAntique.copy(alpha = 0.25f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CyberShapes.chip)
            .background(c.panel.copy(alpha = 0.8f))
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
        Text(
            text = if (isHighlighted) "ВЫЛОЖИТЬ" else "СЛОТ $slotLabel",
            style = MaterialTheme.typography.labelSmall,
            color = if (isHighlighted) NeonCyan else c.textMid
        )
    }
}

@Composable
private fun HandCardView(
    card: BattleCard,
    isSelected: Boolean,
    canAfford: Boolean,
    onClick: () -> Unit
) {
    val c = Cyber.colors
    val borderColor = if (isSelected) NeonCyan
    else if (canAfford) card.element.color.copy(alpha = 0.8f)
    else c.textMid.copy(alpha = 0.3f)

    Surface(
        modifier = Modifier
            .width(102.dp)
            .fillMaxHeight()
            .clickable(onClick = onClick)
            .testTag("hand_card_${card.cardIndex}"),
        shape = CyberShapes.chip,
        color = if (isSelected) c.panelRaised else c.panel,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Space.xs),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (canAfford) NeonCyan else c.panelRaised,
                    modifier = Modifier.size(20.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${card.energyCost}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (canAfford) c.ink else c.textMid
                        )
                    }
                }
                Text(
                    text = card.element.symbol,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Text(
                text = card.name,
                style = MaterialTheme.typography.labelSmall,
                color = if (canAfford) c.textHigh else c.textMid,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (card.specialEffect != null) {
                Text(
                    text = card.specialEffect.titleRu,
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonAmber,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "A:${card.currentPower}",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonAmber
                )
                Text(
                    text = "D:${card.currentDefense}",
                    style = MaterialTheme.typography.labelSmall,
                    color = c.emeraldGlow
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
    val c = Cyber.colors
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CyberShapes.cardCompact,
        color = c.panel.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Space.sm, vertical = Space.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.sm)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(c.panelRaised)
                        .border(1.dp, primaryColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = primaryColor, modifier = Modifier.size(16.dp))
                }

                Column {
                    Text(
                        text = playerName,
                        style = MaterialTheme.typography.labelMedium,
                        color = c.textHigh
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
                        Text(
                            text = "HP: ${state.playerHp}/${state.playerMaxHp}",
                            style = MaterialTheme.typography.labelSmall,
                            color = c.emeraldGlow
                        )
                        if (state.playerShield > 0) {
                            Text(
                                text = "SH +${state.playerShield}",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonCyan
                            )
                        }
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "RAM ${state.playerEnergy}/${state.playerMaxEnergy}",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonCyan
                )
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    for (i in 1..state.playerMaxEnergy) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CyberShapes.chip)
                                .background(
                                    if (i <= state.playerEnergy) NeonCyan else c.panelRaised
                                )
                        )
                    }
                }
            }

            Button(
                onClick = onEndTurn,
                enabled = state.isPlayerTurn && !state.isAiThinking,
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    disabledContainerColor = c.panelRaised
                ),
                shape = CyberShapes.chip,
                contentPadding = PaddingValues(horizontal = Space.md, vertical = Space.xs),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("end_turn_button")
            ) {
                Text(
                    text = if (lang == "ru") "ХОД" else "END",
                    style = MaterialTheme.typography.labelMedium,
                    color = c.ink
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
    val c = Cyber.colors
    Dialog(onDismissRequest = onExit) {
        Surface(
            shape = CyberShapes.card,
            color = c.panel,
            border = BorderStroke(
                2.dp,
                if (winner == DuelWinner.PLAYER) c.emeraldGlow else c.danger
            ),
            modifier = Modifier.padding(Space.lg)
        ) {
            Column(
                modifier = Modifier.padding(Space.xl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (winner == DuelWinner.PLAYER) {
                        if (lang == "ru") "ПОБЕДА" else "VICTORY"
                    } else {
                        if (lang == "ru") "ПОРАЖЕНИЕ" else "DEFEAT"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (winner == DuelWinner.PLAYER) c.emeraldGlow else c.danger
                )

                Spacer(modifier = Modifier.height(Space.sm))

                Text(
                    text = if (winner == DuelWinner.PLAYER) {
                        if (lang == "ru")
                            "Вы перегрузили ядро Нейро-Демиурга!\nПолучено: +150 WEED Credits, +50 XP"
                        else
                            "You overcharged the Neuro-Demiurge core!\nRewards: +150 WEED Credits, +50 XP"
                    } else {
                        if (lang == "ru")
                            "Ваша нейросеть обесточена. Требуется калибровка колоды."
                        else
                            "Your neural node was disconnected. Recalibrate your deck."
                    },
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textHigh
                )

                Spacer(modifier = Modifier.height(Space.lg))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Space.sm)
                ) {
                    OutlinedButton(
                        onClick = onExit,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                    ) {
                        Text(if (lang == "ru") "В Хаб" else "Hub", color = c.textHigh)
                    }

                    Button(
                        onClick = onRestart,
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                    ) {
                        Text(if (lang == "ru") "Реванш" else "Rematch", color = c.ink, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
