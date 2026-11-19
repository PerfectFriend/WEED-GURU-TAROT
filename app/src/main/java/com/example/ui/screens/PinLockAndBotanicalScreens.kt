package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.CyberNoirSynthPlayer
import com.example.data.LocalizationHelper
import com.example.ui.components.CyberChip
import com.example.ui.components.CyberPanel
import com.example.ui.components.CyberSectionHeader
import com.example.ui.components.SkinBackdrop
import com.example.ui.theme.*
import com.example.viewmodel.TarotViewModel
import kotlinx.coroutines.launch

@Composable
fun CustomBigKeypad(
    skin: CyberSkin,
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = Cyber.colors
    val haptic = LocalHapticFeedback.current
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("C", "0", "⌫")
    )

    Column(
        modifier = modifier.padding(Space.sm),
        verticalArrangement = Arrangement.spacedBy(Space.md),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        keys.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(Space.lg),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { key ->
                    val isActionKey = key == "⌫" || key == "C"
                    val borderColor = if (isActionKey) {
                        c.goldAntique.copy(alpha = 0.45f)
                    } else {
                        c.accent.copy(alpha = 0.65f)
                    }
                    Surface(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.CLICK)
                            when (key) {
                                "⌫" -> onDeleteClick()
                                "C" -> onClearClick()
                                else -> onDigitClick(key)
                            }
                        },
                        shape = CyberShapes.control,
                        color = c.panelRaised.copy(alpha = 0.92f),
                        border = BorderStroke(1.5.dp, borderColor),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 60.dp)
                            .testTag("keypad_key_$key")
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = Space.md),
                            contentAlignment = Alignment.Center
                        ) {
                            if (key == "⌫") {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Backspace",
                                    tint = c.danger,
                                    modifier = Modifier.size(22.dp)
                                )
                            } else {
                                Text(
                                    text = key,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontFamily = Exo2FontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = if (key == "C") c.textMid else c.accent
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PinLockScreen(viewModel: TarotViewModel, onRestoreRequest: () -> Unit = {}) {
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val lang = profile?.systemLanguage ?: "en"
    val isRu = lang in listOf("ru", "uk")
    val c = Cyber.colors
    val coroutineScope = rememberCoroutineScope()
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        SkinBackdrop(skin = currentSkin)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .widthIn(max = 520.dp)
                .align(Alignment.Center)
                .padding(horizontal = Space.xl, vertical = Space.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top area
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = Space.lg)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(c.danger.copy(alpha = 0.14f))
                        .border(1.5.dp, c.danger.copy(alpha = 0.7f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = LocalizationHelper.getString(lang, "pin_required_title"),
                        tint = c.danger,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(Space.md))
                Text(
                    text = LocalizationHelper.getString(lang, "pin_required_title"),
                    style = MaterialTheme.typography.titleLarge,
                    color = c.danger,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(Space.xs))
                Text(
                    text = LocalizationHelper.getString(lang, "pin_required_desc"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textMid,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = Space.md)
                )
            }

            // Mid area (dots + status)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Space.md),
                    modifier = Modifier.padding(vertical = Space.md)
                ) {
                    for (i in 0 until 8) {
                        val filled = pinInput.length > i
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(if (filled) c.accent else c.panelRaised.copy(alpha = 0.65f))
                                .border(
                                    1.5.dp,
                                    if (filled) c.accent else c.goldAntique.copy(alpha = 0.55f),
                                    CircleShape
                                )
                        )
                    }
                }

                if (pinError) {
                    Text(
                        text = LocalizationHelper.getString(lang, "pin_error"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.danger,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = Space.xs)
                    )
                }
            }

            // Keypad area
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Space.sm)
            ) {
                CustomBigKeypad(
                    skin = currentSkin,
                    onDigitClick = { digit ->
                        if (!isVerifying && pinInput.length < 8) {
                            pinInput += digit
                            pinError = false
                            if (pinInput.length == 8) {
                                val candidate = pinInput
                                isVerifying = true
                                coroutineScope.launch {
                                    val success = viewModel.unlockApp(candidate)
                                    isVerifying = false
                                    if (!success) {
                                        pinError = true
                                        pinInput = ""
                                        CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.SCAN_BEEP)
                                    } else {
                                        CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
                                    }
                                }
                            }
                        }
                    },
                    onDeleteClick = {
                        if (pinInput.isNotEmpty()) {
                            pinInput = pinInput.dropLast(1)
                        }
                    },
                    onClearClick = {
                        pinInput = ""
                        pinError = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Space.sm))

                TextButton(
                    onClick = onRestoreRequest,
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = c.goldLeaf,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(Space.sm))
                    Text(
                        text = if (isRu) {
                            "Забыли ПИН? Восстановить из сид-фразы"
                        } else {
                            "Forgot PIN? Restore from Seed Phrase"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = c.goldLeaf
                    )
                }
            }
        }
    }
}

@Composable
fun SafeBotanicalModeScreen(viewModel: TarotViewModel) {
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val lang = profile?.systemLanguage ?: "en"
    val isRu = lang in listOf("ru", "uk")
    val c = Cyber.colors
    var chatInput by remember { mutableStateOf("") }
    val chatHistory = remember { mutableStateListOf<Pair<String, Boolean>>() }

    LaunchedEffect(isRu) {
        if (chatHistory.isEmpty()) {
            val welcome = if (isRu) {
                "Добро пожаловать в Ботанический Справочник. Спрашивайте о почве, графике полива, органическом компосте или освещении растений."
            } else {
                "Welcome to the Greenhouse Botanical Assistant! Ask about soil health, watering schedules, organic compost, or herbal horticulture."
            }
            chatHistory.add(welcome to false)
        }
    }

    val quickTopics = remember(isRu) {
        if (isRu) {
            listOf(
                Triple("Почва и NPK", Icons.Default.Science, "Органическая почва нуждается в азоте, фосфоре и калии (N-P-K). Компостный чай и биогумус мягко питают корни без химических ожогов."),
                Triple("Полив", Icons.Default.WaterDrop, "Золотое правило полива: проверяйте влажность на глубине 2-3 см. Лучше лёгкий недолив, чем застой воды и гниение корней."),
                Triple("Свет", Icons.Default.WbSunny, "Цветущим растениям нужно 6-8 часов прямого света или полноспектральная LED-лампа на высоте 30-45 см над кроной."),
                Triple("Обрезка", Icons.Default.ContentCut, "Обрезка стимулирует боковое ветвление. Всегда используйте стерильные ножницы и делайте срез чуть выше узла.")
            )
        } else {
            listOf(
                Triple("Soil Nutrients", Icons.Default.Science, "Organic soils thrive on nitrogen, phosphorus, and potassium (N-P-K). Compost tea, bone meal, and worm castings enrich soil microbiomes naturally."),
                Triple("Watering", Icons.Default.WaterDrop, "Check soil dryness 1 inch deep before watering. Root rot is caused by stagnant water cutting off oxygen; always use well-drained pots."),
                Triple("Lighting", Icons.Default.WbSunny, "Flowering foliage typically requires 6-8 hours of direct sunlight or full-spectrum LED lighting positioned 12-18 inches above the canopy."),
                Triple("Pruning", Icons.Default.ContentCut, "Pruning encourages lateral branching! Use sanitized scissors and snip stems directly above node pairs to improve airflow.")
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SkinBackdrop(skin = currentSkin)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .widthIn(max = 680.dp)
                .align(Alignment.Center)
                .padding(Space.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CyberPanel(
                borderColor = c.emeraldGlow.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Space.md)
                ) {
                    Icon(
                        imageVector = Icons.Default.Eco,
                        contentDescription = if (isRu) "Ботанический режим" else "Botanical Safe Mode",
                        tint = c.emeraldGlow,
                        modifier = Modifier.size(32.dp)
                    )
                    CyberSectionHeader(
                        title = if (isRu) "БОТАНИЧЕСКИЙ ПОМОЩНИК" else "BOTANICAL SAFE ASSISTANT",
                        subtitle = if (isRu) "Локальное зашифрованное ядро v2.4" else "Encrypted Local Herbology Core v2.4",
                        color = c.emeraldGlow
                    )
                }
            }

            Spacer(modifier = Modifier.height(Space.md))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Space.md)
            ) {
                items(chatHistory) { (msg, isUser) ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUser) {
                                    c.emerald.copy(alpha = 0.24f)
                                } else {
                                    c.panel.copy(alpha = 0.94f)
                                }
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isUser) c.emeraldGlow.copy(alpha = 0.6f) else c.goldAntique.copy(alpha = 0.35f)
                            ),
                            shape = CyberShapes.cardCompact,
                            modifier = Modifier.fillMaxWidth(0.86f)
                        ) {
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodyMedium,
                                color = c.textHigh,
                                modifier = Modifier.padding(Space.md)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Space.md))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Space.sm)
            ) {
                items(quickTopics) { (topic, icon, reply) ->
                    CyberChip(
                        label = topic,
                        selected = false,
                        leadingIcon = icon,
                        onClick = {
                            CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.CLICK)
                            val prompt = if (isRu) "Расскажите о теме: $topic" else "Tell me about $topic best practices."
                            chatHistory.add(prompt to true)
                            chatHistory.add(reply to false)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(Space.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.sm)
            ) {
                OutlinedTextField(
                    value = chatInput,
                    onValueChange = { chatInput = it },
                    placeholder = {
                        Text(
                            text = if (isRu) "Спросить об уходе за садом..." else "Ask about botanical care...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = c.textMid
                        )
                    },
                    singleLine = true,
                    shape = CyberShapes.control,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = c.emeraldGlow,
                        unfocusedBorderColor = c.goldAntique.copy(alpha = 0.4f),
                        focusedTextColor = c.textHigh,
                        unfocusedTextColor = c.textHigh
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )

                FilledIconButton(
                    onClick = {
                        if (chatInput.isNotBlank()) {
                            CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
                            val query = chatInput
                            chatHistory.add(query to true)
                            chatInput = ""
                            chatHistory.add(generateBotanicalReply(query, isRu) to false)
                        }
                    },
                    shape = CyberShapes.control,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = c.emeraldGlow,
                        contentColor = c.ink
                    ),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = if (isRu) "Отправить" else "Send"
                    )
                }
            }
        }
    }
}

private fun generateBotanicalReply(query: String, isRu: Boolean): String {
    val q = query.lowercase()
    return if (isRu) {
        when {
            q.contains("вод") || q.contains("полив") || q.contains("water") ->
                "Поддерживайте ровный цикл: поливайте до дренажа и давайте верхнему слою почвы просохнуть на пару сантиметров."
            q.contains("свет") || q.contains("ламп") || q.contains("light") ->
                "Светолюбивым растениям оптимальны 12–18 часов полного спектра в фазе роста и более спокойный режим в период отдыха."
            q.contains("почв") || q.contains("удобр") || q.contains("npk") ->
                "Воздушный субстрат с перлитом и органическим компостом помогает корням дышать и стабильно усваивать микроэлементы."
            else ->
                "Отличный вопрос! Стабильная температура 21–26°C, чистая циркуляция воздуха и бережный уход поддерживают здоровый рост."
        }
    } else {
        when {
            q.contains("water") || q.contains("hydro") || q.contains("dry") ->
                "Maintain a steady cycle. Water thoroughly until it drains, then allow the top soil layers to dry out. Excess humidity promotes root fungi."
            q.contains("light") || q.contains("sun") || q.contains("spectrum") ->
                "Flowering plants love light! Aim for 12-18 hours of spectrum lighting during the growth cycle, dialing back during resting periods."
            q.contains("soil") || q.contains("fertilizer") || q.contains("nutrient") || q.contains("npk") ->
                "An aerated soil mix with perlite allows roots to breathe. Organic compost feeds helpful microbes, keeping nutrient absorption smooth."
            else ->
                "That sounds like a fascinating pursuit! Remember, plants respond beautifully to stable temperatures (21-26°C), gentle clean airflow, and proper care."
        }
    }
}
