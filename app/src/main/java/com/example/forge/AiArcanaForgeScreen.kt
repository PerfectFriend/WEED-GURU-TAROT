package com.example.forge

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
import com.example.data.GeminiService
import com.example.data.GeneratedArcanaData
import com.example.duel.ElementAffinity
import com.example.viewmodel.TarotViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiArcanaForgeScreen(
    viewModel: TarotViewModel,
    onBack: (() -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val lang = userProfile?.systemLanguage ?: "en"

    var userConcept by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var generatedCard by remember { mutableStateOf<GeneratedArcanaData?>(null) }
    var isSavedToCollection by remember { mutableStateOf(false) }

    val presetSuggestions = listOf(
        "⚡ Квантовый Самурай Матрицы",
        "🌿 Неоновый Био-Шаман",
        "🔮 Дракон Темной Материи",
        "🌑 Призрак Нулевого Байта",
        "🧬 Кибер-Алхимик Разума"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "forge_glow")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050811))
            .padding(horizontal = 14.dp)
            .testTag("ai_arcana_forge_screen")
    ) {
        // Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
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
                Column {
                    Text(
                        text = if (lang == "ru") "ГЕНЕРАТИВНАЯ КУЗНИЦА" else "GEMINI ARCANA FORGE",
                        color = Color(0xFF00F5FF),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (lang == "ru") "Синтез уникальных Арканов через нейросеть" else "Neural card generation with Gemini AI",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // AI Status Badge
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF10192A),
                border = BorderStroke(1.dp, Color(0xFF00F5FF).copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00FF66))
                    )
                    Text(
                        text = "GEMINI CORE",
                        fontSize = 9.sp,
                        color = Color(0xFF00F5FF),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Concept Prompt Input
        OutlinedTextField(
            value = userConcept,
            onValueChange = { userConcept = it },
            label = { Text(if (lang == "ru") "Концепт Аркана (напр. 'Квантовый Самурай')" else "Card Concept (e.g. 'Quantum Samurai')") },
            placeholder = { Text(if (lang == "ru") "Опишите стиль, образ, стихию или роль..." else "Describe archetype, element, lore...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00F5FF),
                unfocusedBorderColor = Color.DarkGray
            )
        )

        // Suggestion Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(presetSuggestions) { suggestion ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0C1322),
                    border = BorderStroke(1.dp, Color(0x3300F5FF)),
                    modifier = Modifier.clickable {
                        CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.CLICK)
                        userConcept = suggestion
                    }
                ) {
                    Text(
                        text = suggestion,
                        fontSize = 10.sp,
                        color = Color.LightGray,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Generate Action Button
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
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F5FF)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .height(44.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (isGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.Black,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (lang == "ru") "НЕЙРО-СИНТЕЗ..." else "SYNTHESIZING...",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            } else {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (lang == "ru") "СИНТЕЗИРОВАТЬ АРКАН В КУЗНИЦЕ" else "FORGE NEURAL ARCANA",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Generated Card Holographic Display
        generatedCard?.let { card ->
            val elemAffinity = when (card.element.uppercase()) {
                "BIO" -> ElementAffinity.BIO
                "QUANTUM" -> ElementAffinity.QUANTUM
                "VOID" -> ElementAffinity.VOID
                else -> ElementAffinity.CYBER
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0B1020),
                border = BorderStroke(1.5.dp, elemAffinity.color.copy(alpha = pulseGlow)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Card Top Bar: Element, Cost, Rarity
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = elemAffinity.symbol, fontSize = 20.sp)
                            Text(
                                text = "${elemAffinity.name} • ${card.rarity.uppercase()}",
                                color = elemAffinity.color,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFB800).copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, Color(0xFFFFB800))
                        ) {
                            Text(
                                text = "${card.energyCost} RAM ⚡",
                                color = Color(0xFFFFB800),
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Title & Archetype
                    Text(
                        text = card.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = card.traditionalName,
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Combat Stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFF5252).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFFFF5252)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("⚔ ATK", fontSize = 11.sp, color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                                Text("${card.power}", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Black)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF00FF66).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFF00FF66)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🛡 DEF", fontSize = 11.sp, color = Color(0xFF00FF66), fontWeight = FontWeight.Bold)
                                Text("${card.defense}", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Ability Box
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF11172A),
                        border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "⚡ ${card.abilityTitle}",
                                color = elemAffinity.color,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = card.abilityDescription,
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Lore
                    Text(
                        text = "«${card.lore}»",
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Save / Add to Deck Action
                    Button(
                        onClick = {
                            CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.COMBAT_VICTORY)
                            isSavedToCollection = true
                            viewModel.addWeedCredits(100)
                        },
                        enabled = !isSavedToCollection,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSavedToCollection) Color(0xFF00FF66) else Color(0xFFFFD700)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isSavedToCollection) {
                                if (lang == "ru") "✓ СОХРАНЕНО В БАЗУ АРКАНОВ (+100 WEED)" else "✓ SAVED TO ARCANA VAULT (+100 WEED)"
                            } else {
                                if (lang == "ru") "СОХРАНИТЬ В БОЕВУЮ КОЛОДУ" else "SAVE TO BATTLE ROSTER"
                            },
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
