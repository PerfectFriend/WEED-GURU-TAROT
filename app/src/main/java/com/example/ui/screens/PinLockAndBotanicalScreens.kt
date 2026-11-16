package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.LocalizationHelper
import com.example.ui.theme.CyberSkin
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.RajdhaniFontFamily
import com.example.viewmodel.TarotViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomBigKeypad(
    skin: CyberSkin,
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("C", "0", "⌫")
    )

    Column(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        keys.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { key ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1B1B2B))
                            .border(1.5.dp, if (key == "⌫" || key == "C") Color.DarkGray else skin.primaryColor.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
                            .clickable {
                                com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.CLICK)
                                when (key) {
                                    "⌫" -> onDeleteClick()
                                    "C" -> onClearClick()
                                    else -> onDigitClick(key)
                                }
                            }
                            .testTag("keypad_key_$key"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = key,
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = if (key == "⌫") NeonRed else if (key == "C") Color.LightGray else skin.primaryColor
                        )
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
    val coroutineScope = rememberCoroutineScope()
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030307))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top area
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Lock",
                tint = NeonRed,
                modifier = Modifier.size(52.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = LocalizationHelper.getString(lang, "pin_required_title"),
                fontFamily = RajdhaniFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = NeonRed,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = LocalizationHelper.getString(lang, "pin_required_desc"),
                fontFamily = RajdhaniFontFamily,
                fontSize = 16.sp,
                color = Color(0xFFB0B0C5),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Mid area (dots + status)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Glow circle indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                for (i in 0 until 8) {
                    val filled = pinInput.length > i
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(if (filled) currentSkin.primaryColor else Color(0x22FFFFFF))
                            .border(1.5.dp, if (filled) currentSkin.primaryColor else Color(0xFFB0B0C5), CircleShape)
                    )
                }
            }

            if (pinError) {
                Text(
                    text = LocalizationHelper.getString(lang, "pin_error"),
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 16.sp,
                    color = NeonRed,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Keypad area
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
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
                                    com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.SCAN_BEEP)
                                } else {
                                    com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
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

            Spacer(modifier = Modifier.height(6.dp))

            TextButton(
                onClick = onRestoreRequest,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = if (isRu) "🔑 Забыли ПИН? Восстановить из сид-фразы" else "🔑 Forgot PIN? Restore from Seed Phrase",
                    color = currentSkin.primaryColor,
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SafeBotanicalModeScreen(viewModel: TarotViewModel) {
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    var chatInput by remember { mutableStateOf("") }
    val chatHistory = remember { mutableStateListOf<Pair<String, Boolean>>() } // message to isUser

    LaunchedEffect(Unit) {
        if (chatHistory.isEmpty()) {
            chatHistory.add("Welcome to the Greenhouse Botanical Assistant! 🌿 Ask me anything about soil health, watering schedules, organic compost, or herbal horticulture. How can I help your garden bloom today?" to false)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C1610))
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Safe Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Eco,
                contentDescription = "Safe Mode",
                tint = NeonGreen,
                modifier = Modifier.size(36.dp)
            )
            Column {
                Text(
                    text = "BOTANICAL SAFE ASSISTANT",
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
                Text(
                    text = "Encrypted Local Herbology Core v2.4",
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 15.sp,
                    color = Color(0xFFB0B0C5)
                )
            }
        }

        HorizontalDivider(color = Color(0x3300FF00), thickness = 1.dp, modifier = Modifier.padding(bottom = 12.dp))

        // Chats list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(chatHistory) { (msg, isUser) ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) Color(0xFF1B3B2B) else Color(0xFF161F1B)
                        ),
                        border = BorderStroke(1.dp, if (isUser) Color(0x6600FF00) else Color(0x3300FF00)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        Text(
                            text = msg,
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 17.sp,
                            color = Color.White,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick tips Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Soil Nutrients 🧪", "Watering 💧", "Lighting ☀️", "Pruning ✂️").forEach { topic ->
                Box(
                    modifier = Modifier
                        .background(Color(0xFF14241C), RoundedCornerShape(20.dp))
                        .border(1.dp, Color(0x4400FF00), RoundedCornerShape(20.dp))
                        .clickable {
                            com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.CLICK)
                            val prompt = "Tell me about $topic best practices."
                            chatHistory.add(prompt to true)
                            val reply = when (topic) {
                                "Soil Nutrients 🧪" -> "Organic soils thrive on nitrogen, phosphorus, and potassium (N-P-K). For home gardens, compost tea, bone meal, and worm castings are exceptional ways to enrich soil microbiomes naturally without artificial burn!"
                                "Watering 💧" -> "The golden rule of watering is: check soil dryness 1 inch deep. It is far safer to underwater than overwater. Root rot is caused by stagnant water cutting off root oxygen. Always use well-drained pots with bottom drainage."
                                "Lighting ☀️" -> "Herbs and flowering foliage typically require 6-8 hours of direct sunlight. Indoors, placing a full-spectrum LED grow-light 12-18 inches above the canopy can perfectly simulate midday solar radiation for compact, healthy nodes."
                                else -> "Pruning encourages lateral branching! Always use sanitized scissors. Snip stems directly above node pairs to double your future flower canopy size and improve airflow, preventing mold development."
                            }
                            chatHistory.add(reply to false)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = topic,
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        color = NeonGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Input Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = chatInput,
                onValueChange = { chatInput = it },
                placeholder = { Text("Ask about botanical care...", color = Color(0xFFB0B0C5), fontSize = 16.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonGreen,
                    unfocusedBorderColor = Color(0x3300FF00),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 17.sp),
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = {
                    if (chatInput.isNotBlank()) {
                        com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
                        val query = chatInput
                        chatHistory.add(query to true)
                        chatInput = ""
                        val response = generateBotanicalReply(query)
                        chatHistory.add(response to false)
                    }
                },
                modifier = Modifier
                    .background(NeonGreen, RoundedCornerShape(8.dp))
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.Black
                )
            }
        }
    }
}

private fun generateBotanicalReply(query: String): String {
    val q = query.lowercase()
    return when {
        q.contains("water") || q.contains("hydro") || q.contains("dry") -> {
            "Maintain a steady cycle. Water thoroughly until it drains, then allow the top soil layers to dry out. Excess humidity promotes root fungi."
        }
        q.contains("light") || q.contains("sun") || q.contains("spectrum") -> {
            "Flowering plants love light! Aim for 12-18 hours of spectrum lighting during the growth cycle, dialing back during resting periods."
        }
        q.contains("soil") || q.contains("fertilizer") || q.contains("nutrient") || q.contains("npk") -> {
            "An aerated soil mix with perlite allows roots to breathe. Organic compost feeds helpful microbes, keeping nutrient absorption smooth."
        }
        else -> {
            "That sounds like a fascinating pursuit! Remember, plants respond beautifully to stable temperatures (21-26°C), gentle clean airflow, and proper care. Keep the environment clean!"
        }
    }
}
