package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.CardLocalizationHelper
import com.example.data.CyberTarotCard
import com.example.data.CyberTarotDeck
import com.example.ui.components.CyberActionButton
import com.example.ui.components.CyberPanel
import com.example.ui.components.WeedGuruTarotCard
import com.example.ui.components.localizedString
import com.example.ui.theme.Cyber
import com.example.ui.theme.CyberShapes
import com.example.ui.theme.Space
import com.example.ui.theme.rememberMotionEnabled
import com.example.viewmodel.DivinationState
import com.example.viewmodel.TarotViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun UnifiedCardsScreen(viewModel: TarotViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val lang = profile?.systemLanguage ?: "en"
    var cardSubTab by rememberSaveable { mutableIntStateOf(0) }
    val c = Cyber.colors

    val vaultTitle = localizedString(R.string.cards_tab_vault, lang)
    val deckTitle = localizedString(R.string.cards_tab_deck, lang)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = Space.sm)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Space.lg, vertical = Space.xs)
                .background(c.panel.copy(alpha = 0.92f), CyberShapes.control)
                .border(1.dp, c.goldAntique.copy(alpha = 0.35f), CyberShapes.control)
                .padding(Space.xs),
            horizontalArrangement = Arrangement.spacedBy(Space.xs)
        ) {
            val subTabs = listOf(
                Triple(vaultTitle, Icons.Default.Style, 0),
                Triple(deckTitle, Icons.Default.Shield, 1)
            )
            subTabs.forEach { (title, icon, idx) ->
                val isSelected = cardSubTab == idx
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .clip(CyberShapes.chip)
                        .background(if (isSelected) c.emerald.copy(alpha = 0.22f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (isSelected) c.emeraldGlow else Color.Transparent,
                            CyberShapes.chip
                        )
                        .clickable(role = Role.Tab) {
                            com.example.audio.CyberNoirSynthPlayer.triggerFX(
                                com.example.audio.CyberNoirSynthPlayer.FXType.CLICK
                            )
                            cardSubTab = idx
                        }
                        .padding(horizontal = Space.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) c.emeraldGlow else c.textMid,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(Space.xs))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isSelected) c.textHigh else c.textMid,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (cardSubTab == 0) {
                VaultScreen(viewModel)
            } else {
                DeckScreen(viewModel)
            }
        }
    }
}

@Composable
fun DivinationScreen(viewModel: TarotViewModel) {
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val divinationState by viewModel.divinationState.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val lang = profile?.systemLanguage ?: "en"
    var questionInput by rememberSaveable { mutableStateOf("") }
    val historyReadings by viewModel.divinationReadings.collectAsStateWithLifecycle()
    var expandedReadingId by remember { mutableStateOf<Int?>(null) }
    val motionEnabled = rememberMotionEnabled()
    val haptic = LocalHapticFeedback.current
    val c = Cyber.colors

    LaunchedEffect(divinationState) {
        when (divinationState) {
            is DivinationState.Scanning -> {
                com.example.audio.CyberNoirSynthPlayer.triggerFX(
                    com.example.audio.CyberNoirSynthPlayer.FXType.SCAN_BEEP
                )
            }
            is DivinationState.Success -> {
                com.example.audio.CyberNoirSynthPlayer.triggerFX(
                    com.example.audio.CyberNoirSynthPlayer.FXType.SUCCESS_CHIME
                )
            }
            else -> Unit
        }
    }

    var selectedCardsCount by rememberSaveable { mutableIntStateOf(1) }
    var showVipPromptForCards by remember { mutableStateOf<Int?>(null) }
    val isVipUnlocked by viewModel.isVipUnlocked.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Space.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (val state = divinationState) {
            is DivinationState.Idle -> {
                LazyColumn(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        Spacer(modifier = Modifier.height(Space.sm))
                        Text(
                            text = localizedString(R.string.divination_title, lang),
                            style = MaterialTheme.typography.headlineSmall,
                            color = c.goldLeaf,
                            modifier = Modifier.testTag("divination_title")
                        )
                        Text(
                            text = localizedString(R.string.divination_subtitle, lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textMid,
                            modifier = Modifier.padding(top = Space.xs, bottom = Space.md)
                        )

                        CyberPanel(
                            borderColor = currentSkin.primaryColor.copy(alpha = 0.6f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Space.xs)
                        ) {
                            CyberOrnamentalHeader(
                                title = localizedString(R.string.divination_prompt_header, lang),
                                skin = currentSkin
                            )
                            Spacer(modifier = Modifier.height(Space.xs))
                            OutlinedTextField(
                                value = questionInput,
                                onValueChange = { questionInput = it },
                                placeholder = {
                                    Text(
                                        text = localizedString(R.string.divination_prompt_placeholder, lang),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = c.textMid
                                    )
                                },
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = c.textHigh),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = c.textHigh,
                                    unfocusedTextColor = c.textHigh,
                                    focusedBorderColor = currentSkin.primaryColor,
                                    unfocusedBorderColor = c.goldAntique.copy(alpha = 0.45f),
                                    focusedContainerColor = c.panelRaised,
                                    unfocusedContainerColor = c.ink.copy(alpha = 0.72f)
                                ),
                                shape = CyberShapes.control,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 90.dp)
                                    .testTag("question_input_field"),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    keyboardController?.hide()
                                })
                            )
                        }

                        CyberPanel(
                            borderColor = currentSkin.secondaryColor.copy(alpha = 0.6f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Space.xs)
                        ) {
                            CyberOrnamentalHeader(
                                title = localizedString(R.string.divination_spread_header, lang),
                                skin = currentSkin
                            )
                            Spacer(modifier = Modifier.height(Space.sm))

                            Column(verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(Space.sm)
                                ) {
                                    SpreadOptionButton(
                                        title = localizedString(R.string.spread_single_title, lang),
                                        subtitle = localizedString(R.string.spread_single_sub, lang),
                                        selected = selectedCardsCount == 1,
                                        locked = false,
                                        skin = currentSkin,
                                        onClick = { selectedCardsCount = 1 },
                                        modifier = Modifier.weight(1f)
                                    )
                                    SpreadOptionButton(
                                        title = localizedString(R.string.spread_flow_title, lang),
                                        subtitle = localizedString(R.string.spread_flow_sub, lang),
                                        selected = selectedCardsCount == 3,
                                        locked = false,
                                        skin = currentSkin,
                                        onClick = { selectedCardsCount = 3 },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(Space.sm)
                                ) {
                                    SpreadOptionButton(
                                        title = localizedString(R.string.spread_romantic_title, lang),
                                        subtitle = localizedString(R.string.spread_romantic_sub, lang),
                                        selected = selectedCardsCount == 5,
                                        locked = !isVipUnlocked,
                                        skin = currentSkin,
                                        onClick = {
                                            if (isVipUnlocked) selectedCardsCount = 5 else showVipPromptForCards = 5
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    SpreadOptionButton(
                                        title = localizedString(R.string.spread_celtic_title, lang),
                                        subtitle = localizedString(R.string.spread_celtic_sub, lang),
                                        selected = selectedCardsCount == 10,
                                        locked = !isVipUnlocked,
                                        skin = currentSkin,
                                        onClick = {
                                            if (isVipUnlocked) selectedCardsCount = 10 else showVipPromptForCards = 10
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(Space.md))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Space.xs)
                            ) {
                                val infoColor = if (selectedCardsCount >= 5) c.accentAlt else c.emeraldGlow
                                Icon(
                                    imageVector = if (selectedCardsCount >= 5) Icons.Default.Star else Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = infoColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = when (selectedCardsCount) {
                                        1 -> localizedString(R.string.spread_reward_1, lang)
                                        3 -> localizedString(R.string.spread_reward_3, lang)
                                        5 -> localizedString(R.string.spread_reward_5, lang)
                                        else -> localizedString(R.string.spread_reward_10, lang)
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = infoColor
                                )
                            }
                        }

                        Surface(
                            color = c.panel.copy(alpha = 0.92f),
                            shape = CyberShapes.panel,
                            border = BorderStroke(1.5.dp, c.goldLeaf.copy(alpha = 0.65f)),
                            onClick = { viewModel.setBlessingsPortalVisible(true) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Space.xs)
                        ) {
                            Row(
                                modifier = Modifier.padding(Space.lg),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = c.goldLeaf,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(Space.md))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = localizedString(R.string.oracle_blessings_title, lang),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = c.textHigh
                                    )
                                    Spacer(modifier = Modifier.height(Space.xxs))
                                    Text(
                                        text = localizedString(R.string.oracle_blessings_sub, lang),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = c.textMid
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = localizedString(R.string.cd_open, lang),
                                    tint = c.goldLeaf,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        val leaseSuccessMsg = localizedString(R.string.vip_lease_acquired, lang)
                        val insufficientMsg = localizedString(R.string.vip_insufficient_credits, lang)

                        showVipPromptForCards?.let { count ->
                            val cost = if (count == 5) 100 else 200
                            AlertDialog(
                                onDismissRequest = { showVipPromptForCards = null },
                                title = {
                                    Text(
                                        text = localizedString(R.string.vip_dialog_title, lang),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = c.goldLeaf
                                    )
                                },
                                text = {
                                    Text(
                                        text = localizedString(R.string.vip_dialog_body, lang, count, cost),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = c.textMid
                                    )
                                },
                                confirmButton = {
                                    CyberActionButton(
                                        text = localizedString(R.string.vip_spend_format, lang, cost),
                                        onClick = {
                                            if (viewModel.deductWeedCredits(cost)) {
                                                viewModel.postNotification(leaseSuccessMsg)
                                                selectedCardsCount = count
                                                showVipPromptForCards = null
                                                viewModel.runDivinationScan(questionInput, count)
                                            } else {
                                                viewModel.postNotification(insufficientMsg)
                                            }
                                        },
                                        modifier = Modifier.heightIn(min = 48.dp)
                                    )
                                },
                                dismissButton = {
                                    CyberActionButton(
                                        text = localizedString(R.string.vip_portal_button, lang),
                                        secondary = true,
                                        onClick = {
                                            showVipPromptForCards = null
                                            viewModel.setBlessingsPortalVisible(true)
                                        },
                                        modifier = Modifier.heightIn(min = 48.dp)
                                    )
                                },
                                containerColor = c.panel,
                                tonalElevation = 6.dp
                            )
                        }

                        Spacer(modifier = Modifier.height(Space.md))

                        CyberActionButton(
                            text = localizedString(R.string.divination_initialize_scan, lang),
                            icon = Icons.Default.Bolt,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                keyboardController?.hide()
                                viewModel.runDivinationScan(questionInput, selectedCardsCount)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("initialize_scan_button")
                        )

                        Spacer(modifier = Modifier.height(Space.lg))
                        CyberAdBanner(viewModel)
                    }

                    if (historyReadings.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(Space.xl))
                            HorizontalDivider(color = c.goldAntique.copy(alpha = 0.25f))
                            Spacer(modifier = Modifier.height(Space.lg))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = localizedString(R.string.cd_history, lang),
                                    tint = c.goldLeaf,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(Space.sm))
                                Text(
                                    text = localizedString(R.string.divination_history_title, lang),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = c.goldLeaf
                                )
                            }
                            Spacer(modifier = Modifier.height(Space.md))
                        }

                        items(historyReadings, key = { it.id }) { reading ->
                            val isExpanded = expandedReadingId == reading.id

                            val indices = remember(reading.cardIndicesString) {
                                reading.cardIndicesString.split(",").filter { it.isNotEmpty() }.mapNotNull { it.toIntOrNull() }
                            }
                            val uprights = remember(reading.isUprightString) {
                                reading.isUprightString.split(",").filter { it.isNotEmpty() }.map { it.toBoolean() }
                            }
                            val drawnCards = remember(indices, uprights) {
                                indices.zip(uprights).map { (index, isUpright) ->
                                    CyberTarotDeck.getCard(index) to isUpright
                                }
                            }

                            Surface(
                                color = c.panel.copy(alpha = 0.92f),
                                shape = CyberShapes.control,
                                border = BorderStroke(
                                    1.dp,
                                    if (isExpanded) c.emeraldGlow else c.goldAntique.copy(alpha = 0.4f)
                                ),
                                onClick = {
                                    expandedReadingId = if (isExpanded) null else reading.id
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = Space.xs)
                            ) {
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
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = reading.question.ifEmpty {
                                                    localizedString(R.string.divination_no_question, lang)
                                                },
                                                style = MaterialTheme.typography.titleSmall,
                                                color = c.textHigh,
                                                maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(Space.xs))
                                            Text(
                                                text = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                                                    .format(Date(reading.timestamp)),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = c.textMid
                                            )
                                        }
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = localizedString(R.string.cd_toggle, lang),
                                            tint = if (isExpanded) c.emeraldGlow else c.textMid,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    if (!isExpanded) {
                                        Spacer(modifier = Modifier.height(Space.sm))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(Space.xs),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            drawnCards.forEach { (card, isUpright) ->
                                                val cardName = CardLocalizationHelper.getLocalizedName(card.index, lang)
                                                Box(
                                                    modifier = Modifier
                                                        .clip(CyberShapes.chip)
                                                        .background(c.panelRaised)
                                                        .border(1.dp, c.goldAntique.copy(alpha = 0.45f), CyberShapes.chip)
                                                        .padding(horizontal = Space.sm, vertical = Space.xxs)
                                                ) {
                                                    Text(
                                                        text = cardName + (if (isUpright) "" else " ▼"),
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = c.accent
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    if (isExpanded) {
                                        Spacer(modifier = Modifier.height(Space.md))
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState())
                                                .padding(vertical = Space.xs),
                                            horizontalArrangement = Arrangement.spacedBy(Space.md, Alignment.CenterHorizontally)
                                        ) {
                                            drawnCards.forEach { (card, isUpright) ->
                                                InteractiveCyberCard(card, isUpright, viewModel)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(Space.md))
                                        HorizontalDivider(color = c.goldAntique.copy(alpha = 0.25f))
                                        Spacer(modifier = Modifier.height(Space.sm))

                                        Text(
                                            text = reading.report,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = c.textHigh
                                        )

                                        Spacer(modifier = Modifier.height(Space.md))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            CyberActionButton(
                                                text = localizedString(R.string.action_delete, lang),
                                                icon = Icons.Default.DeleteOutline,
                                                secondary = true,
                                                onClick = {
                                                    viewModel.deleteDivinationReading(reading.id)
                                                    expandedReadingId = null
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            is DivinationState.Scanning -> {
                val logLines = listOf(
                    localizedString(R.string.divination_scan_log_1, lang),
                    localizedString(R.string.divination_scan_log_2, lang),
                    localizedString(R.string.divination_scan_log_3, lang),
                    localizedString(R.string.divination_scan_log_4, lang),
                    localizedString(R.string.divination_scan_log_5, lang)
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = Space.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val pulseScale = if (motionEnabled) {
                        val infiniteTransition = rememberInfiniteTransition(label = "scan_pulse")
                        infiniteTransition.animateFloat(
                            initialValue = 0.9f,
                            targetValue = 1.1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulse_scale"
                        ).value
                    } else {
                        1.0f
                    }

                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = localizedString(R.string.cd_scanning, lang),
                        tint = c.goldLeaf,
                        modifier = Modifier
                            .size(84.dp)
                            .shadow(
                                elevation = 24.dp * pulseScale,
                                ambientColor = c.goldLeaf,
                                spotColor = c.goldLeaf,
                                clip = false
                            )
                    )

                    Spacer(modifier = Modifier.height(Space.xxl))
                    Text(
                        text = localizedString(R.string.divination_scanning_title, lang),
                        style = MaterialTheme.typography.titleMedium,
                        color = c.accent
                    )

                    Spacer(modifier = Modifier.height(Space.lg))
                    CyberPanel(
                        borderColor = c.goldAntique.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth(0.88f)
                    ) {
                        var logText by remember(logLines) { mutableStateOf(logLines.first()) }
                        LaunchedEffect(logLines) {
                            for (log in logLines.drop(1)) {
                                delay(400)
                                logText = log
                            }
                        }
                        Text(
                            text = "> $logText",
                            style = MaterialTheme.typography.labelLarge,
                            color = c.emeraldGlow
                        )
                    }
                }
            }

            is DivinationState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Spacer(modifier = Modifier.height(Space.lg))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = localizedString(R.string.divination_complete, lang),
                                style = MaterialTheme.typography.titleMedium,
                                color = c.emeraldGlow
                            )

                            CyberActionButton(
                                text = localizedString(R.string.divination_new_scan, lang),
                                icon = Icons.Default.Refresh,
                                secondary = true,
                                onClick = { viewModel.resetDivination() }
                            )
                        }

                        Text(
                            text = localizedString(R.string.divination_question_format, lang, state.question),
                            style = MaterialTheme.typography.bodyMedium,
                            color = c.textMid,
                            textAlign = TextAlign.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Space.sm)
                        )

                        Spacer(modifier = Modifier.height(Space.lg))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = Space.sm),
                            horizontalArrangement = Arrangement.spacedBy(Space.lg, Alignment.CenterHorizontally)
                        ) {
                            state.cards.forEach { (card, isUpright) ->
                                InteractiveCyberCard(card, isUpright, viewModel)
                            }
                        }

                        Spacer(modifier = Modifier.height(Space.md))

                        CyberPanel(
                            borderColor = currentSkin.primaryColor.copy(alpha = 0.7f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Space.xs)
                        ) {
                            CyberOrnamentalHeader(
                                title = localizedString(R.string.divination_report_header, lang),
                                skin = currentSkin
                            )

                            Spacer(modifier = Modifier.height(Space.md))

                            CyberActionButton(
                                text = if (isSpeaking) {
                                    localizedString(R.string.divination_stop_voice, lang)
                                } else {
                                    localizedString(R.string.divination_speak_voice, lang)
                                },
                                icon = if (isSpeaking) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                secondary = !isSpeaking,
                                onClick = {
                                    if (isSpeaking) viewModel.stopSpeaking() else viewModel.speakText(state.report)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(Space.md))

                            Text(
                                text = state.report,
                                style = MaterialTheme.typography.bodyMedium,
                                color = c.textHigh,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(modifier = Modifier.height(Space.xxl))
                    }
                }
            }

            is DivinationState.Error -> {
                viewModel.resetDivination()
            }
        }
    }
}

@Composable
private fun SpreadOptionButton(
    title: String,
    subtitle: String,
    selected: Boolean,
    locked: Boolean,
    skin: com.example.ui.theme.CyberSkin,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = Cyber.colors
    val haptic = LocalHapticFeedback.current
    val borderColor = when {
        selected -> c.emeraldGlow
        locked -> c.accentAlt.copy(alpha = 0.45f)
        else -> c.goldAntique.copy(alpha = 0.45f)
    }
    val containerColor = when {
        selected -> c.emerald.copy(alpha = 0.24f)
        locked -> c.panelRaised.copy(alpha = 0.6f)
        else -> c.panelRaised.copy(alpha = 0.9f)
    }
    val titleColor = when {
        selected -> c.emeraldGlow
        locked -> c.goldLeaf
        else -> c.textHigh
    }

    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        shape = CyberShapes.control,
        color = containerColor,
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, borderColor),
        modifier = modifier.heightIn(min = 56.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = Space.sm, vertical = Space.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (locked) {
                    Spacer(modifier = Modifier.width(Space.xs))
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = skin.secondaryColor,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) c.textHigh else c.textMid,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun InteractiveCyberCard(
    card: CyberTarotCard,
    isUpright: Boolean,
    viewModel: TarotViewModel
) {
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val lang = profile?.systemLanguage ?: "en"
    val c = Cyber.colors
    var isRevealed by remember { mutableStateOf(false) }
    var wasMinted by remember { mutableStateOf(false) }
    var showFullScreenReview by remember { mutableStateOf(false) }

    val localizedName = CardLocalizationHelper.getLocalizedName(card.index, lang)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(184.dp)
    ) {
        WeedGuruTarotCard(
            card = card,
            isUpright = isUpright,
            isFlipped = !isRevealed,
            compact = false,
            systemLanguage = lang,
            showOrientationBadge = true,
            showRarityShader = true,
            backPromptLabel = localizedString(R.string.card_tap_to_reveal, lang),
            onCardClick = {
                if (!isRevealed) {
                    isRevealed = true
                    com.example.audio.CyberNoirSynthPlayer.triggerFX(
                        com.example.audio.CyberNoirSynthPlayer.FXType.CARD_DRAW
                    )
                } else {
                    showFullScreenReview = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reading_tarot_card_${card.index}")
        )

        Spacer(modifier = Modifier.height(Space.sm))

        if (isRevealed) {
            if (!wasMinted) {
                CyberActionButton(
                    text = localizedString(R.string.card_mint_nft, lang),
                    icon = Icons.Default.CheckCircle,
                    onClick = {
                        viewModel.mintNFT(card.index) { success ->
                            if (success) wasMinted = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.95f)
                )
            } else {
                Box(
                    modifier = Modifier
                        .clip(CyberShapes.control)
                        .background(c.emerald.copy(alpha = 0.16f))
                        .border(1.dp, c.emeraldGlow, CyberShapes.control)
                        .padding(horizontal = Space.md, vertical = Space.sm)
                ) {
                    Text(
                        text = localizedString(R.string.card_minted, lang),
                        style = MaterialTheme.typography.labelLarge,
                        color = c.emeraldGlow
                    )
                }
            }
        }

        if (showFullScreenReview) {
            FullScreenCardReview(
                card = card,
                isMarketView = false,
                systemLanguage = lang,
                skin = currentSkin,
                isUpright = isUpright,
                showOrientation = true,
                onClose = { showFullScreenReview = false }
            )
        }
    }
}
