package com.example.forge

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.CyberNoirSynthPlayer
import com.example.data.GeminiService
import com.example.data.GeneratedArcanaData
import com.example.duel.ElementAffinity
import com.example.ui.components.CyberChip
import com.example.ui.components.CyberPanel
import com.example.ui.theme.Cyber
import com.example.ui.theme.CyberShapes
import com.example.ui.theme.IbmPlexMonoFontFamily
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.Space
import com.example.ui.theme.rememberMotionEnabled
import com.example.viewmodel.TarotViewModel
import kotlinx.coroutines.launch

@Composable
fun AiArcanaForgeScreen(
    viewModel: TarotViewModel,
    onBack: (() -> Unit)? = null
) {
    val c = Cyber.colors
    val coroutineScope = rememberCoroutineScope()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val lang = userProfile?.systemLanguage ?: "en"

    var userConcept by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var generatedCard by remember { mutableStateOf<GeneratedArcanaData?>(null) }
    var isSavedToCollection by remember { mutableStateOf(false) }

    val presetSuggestions = listOf(
        "Квантовый Самурай Матрицы",
        "Неоновый Био-Шаман",
        "Дракон Темной Материи",
        "Призрак Нулевого Байта",
        "Кибер-Алхимик Разума"
    )

    val motionEnabled = rememberMotionEnabled()
    val pulseGlow = if (motionEnabled) {
        val infiniteTransition = rememberInfiniteTransition(label = "forge_glow")
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.ink)
            .padding(horizontal = Space.lg)
            .verticalScroll(rememberScrollState())
            .testTag("ai_arcana_forge_screen")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Space.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = c.textHigh
                        )
                    }
                }
                Column {
                    Text(
                        text = if (lang == "ru") "ГЕНЕРАТИВНАЯ КУЗНИЦА" else "GEMINI ARCANA FORGE",
                        color = NeonCyan,
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = IbmPlexMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (lang == "ru") "Синтез уникальных Арканов" else "Neural card generation with Gemini AI",
                        color = c.textMid,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Surface(
                shape = CyberShapes.chip,
                color = c.panelRaised,
                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = Space.sm, vertical = Space.xs),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Space.xs)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(c.emeraldGlow)
                    )
                    Text(
                        text = "GEMINI CORE",
                        style = MaterialTheme.typography.labelMedium,
                        color = NeonCyan
                    )
                }
            }
        }

        OutlinedTextField(
            value = userConcept,
            onValueChange = { userConcept = it },
            label = { Text(if (lang == "ru") "Концепт Аркана (напр. 'Квантовый Самурай')" else "Card Concept (e.g. 'Quantum Samurai')") },
            placeholder = { Text(if (lang == "ru") "Опишите стиль, образ, стихию или роль..." else "Describe archetype, element, lore...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Space.xs),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = c.goldAntique.copy(alpha = 0.35f),
                focusedTextColor = c.textHigh,
                unfocusedTextColor = c.textHigh
            )
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Space.sm),
            horizontalArrangement = Arrangement.spacedBy(Space.sm)
        ) {
            items(presetSuggestions) { suggestion ->
                CyberChip(
                    label = suggestion,
                    selected = userConcept == suggestion,
                    onClick = {
                        CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.CLICK)
                        userConcept = suggestion
                    }
                )
            }
        }

        Button(
            onClick = {
                if (userConcept.isNotBlank() && !isGenerating) {
                    CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.SCAN_BEEP)
                    isGenerating = true
                    isSavedToCollection = false
                    coroutineScope.launch {
                        val cardData = GeminiService.generateCustomArcana(userConcept, lang)
                        generatedCard = cardData
                        isGenerating = false
                        CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
                    }
                }
            },
            enabled = userConcept.isNotBlank() && !isGenerating,
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Space.sm)
                .heightIn(min = 48.dp),
            shape = CyberShapes.control
        ) {
            if (isGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = c.ink,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(Space.sm))
                Text(
                    text = if (lang == "ru") "НЕЙРО-СИНТЕЗ..." else "SYNTHESIZING...",
                    color = c.ink,
                    style = MaterialTheme.typography.labelLarge
                )
            } else {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = c.ink)
                Spacer(modifier = Modifier.width(Space.xs))
                Text(
                    text = if (lang == "ru") "СИНТЕЗИРОВАТЬ АРКАН В КУЗНИЦЕ" else "FORGE NEURAL ARCANA",
                    color = c.ink,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(Space.xs))

        generatedCard?.let { card ->
            val elemAffinity = when (card.element.uppercase()) {
                "BIO" -> ElementAffinity.BIO
                "QUANTUM" -> ElementAffinity.QUANTUM
                "VOID" -> ElementAffinity.VOID
                else -> ElementAffinity.CYBER
            }

            CyberPanel(
                borderColor = elemAffinity.color.copy(alpha = pulseGlow),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Space.xs)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Space.xs)
                    ) {
                        Text(text = elemAffinity.symbol, style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "${elemAffinity.name} • ${card.rarity.uppercase()}",
                            color = elemAffinity.color,
                            style = MaterialTheme.typography.labelLarge,
                            fontFamily = IbmPlexMonoFontFamily
                        )
                    }

                    Surface(
                        shape = CyberShapes.chip,
                        color = NeonAmber.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, NeonAmber)
                    ) {
                        Text(
                            text = "${card.energyCost} RAM",
                            color = NeonAmber,
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = IbmPlexMonoFontFamily,
                            modifier = Modifier.padding(horizontal = Space.sm, vertical = Space.xs)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Space.sm))

                Text(
                    text = card.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = IbmPlexMonoFontFamily,
                    color = c.textHigh
                )
                Text(
                    text = card.traditionalName,
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textMid
                )

                Spacer(modifier = Modifier.height(Space.sm))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Space.md)
                ) {
                    Surface(
                        shape = CyberShapes.chip,
                        color = c.danger.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, c.danger),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(Space.sm),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = c.danger, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(Space.xs))
                                Text("ATK", style = MaterialTheme.typography.labelMedium, color = c.danger)
                            }
                            Text("${card.power}", style = MaterialTheme.typography.titleSmall, color = c.textHigh)
                        }
                    }

                    Surface(
                        shape = CyberShapes.chip,
                        color = c.emeraldGlow.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, c.emeraldGlow),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(Space.sm),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = c.emeraldGlow, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(Space.xs))
                                Text("DEF", style = MaterialTheme.typography.labelMedium, color = c.emeraldGlow)
                            }
                            Text("${card.defense}", style = MaterialTheme.typography.titleSmall, color = c.textHigh)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Space.md))

                Surface(
                    shape = CyberShapes.chip,
                    color = c.panelRaised,
                    border = BorderStroke(1.dp, c.goldAntique.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(Space.sm)) {
                        Text(
                            text = card.abilityTitle,
                            color = elemAffinity.color,
                            style = MaterialTheme.typography.labelLarge,
                            fontFamily = IbmPlexMonoFontFamily
                        )
                        Spacer(modifier = Modifier.height(Space.xxs))
                        Text(
                            text = card.abilityDescription,
                            color = c.textHigh,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Space.sm))

                Text(
                    text = "«${card.lore}»",
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textMid,
                    fontStyle = FontStyle.Italic
                )

                Spacer(modifier = Modifier.height(Space.md))

                Button(
                    onClick = {
                        CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.COMBAT_VICTORY)
                        isSavedToCollection = true
                        viewModel.addWeedCredits(100)
                    },
                    enabled = !isSavedToCollection,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSavedToCollection) c.emeraldGlow else c.goldLeaf
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    shape = CyberShapes.control
                ) {
                    if (isSavedToCollection) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = c.ink, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(Space.xs))
                    }
                    Text(
                        text = if (isSavedToCollection) {
                            if (lang == "ru") "СОХРАНЕНО В БАЗУ АРКАНОВ (+100 WEED)" else "SAVED TO ARCANA VAULT (+100 WEED)"
                        } else {
                            if (lang == "ru") "СОХРАНИТЬ В БОЕВУЮ КОЛОДУ" else "SAVE TO BATTLE ROSTER"
                        },
                        color = c.ink,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
