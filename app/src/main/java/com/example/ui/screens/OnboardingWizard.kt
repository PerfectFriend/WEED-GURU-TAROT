package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.LocalizationHelper
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.RajdhaniFontFamily
import com.example.viewmodel.TarotViewModel

@Composable
fun OnboardingWizard(viewModel: TarotViewModel, onComplete: () -> Unit) {
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    var currentStep by remember { mutableStateOf(0) } // 0: Lang & Security Notice, 1: Mnemonic Seed, 2: Seed Verification, 3: Alias/Avatar, 4: Container/P2P, 5: PIN setup
    var isRestoreMode by remember { mutableStateOf(false) }
    var restoreSeedInput by remember { mutableStateOf("") }
    var restorePinInput by remember { mutableStateOf("") }
    var restoreError by remember { mutableStateOf("") }

    val tempLang = remember { mutableStateOf(profile?.systemLanguage ?: "en") }
    val aliasInput = remember { mutableStateOf("") }
    val selectedAvatarIdx = remember { mutableStateOf(0) }
    val allocatedStorageGb = remember { mutableStateOf(16f) }

    // Generate real cryptographically secure random BIP39 phrase once
    val generatedMnemonicList = remember {
        com.example.data.Bip39Helper.generateMnemonic(
            com.example.data.Bip39Helper.generateRandomEntropy(256)
        )
    }

    // Challenge verification states
    val testIndices = remember {
        (0..23).shuffled().take(3).sorted()
    }
    val verificationOptions = remember {
        testIndices.associateWith { correctIdx ->
            val correctWord = generatedMnemonicList[correctIdx]
            val others = (1..3).map {
                var w = com.example.data.Bip39Wordlist.words.random()
                while (w == correctWord) { w = com.example.data.Bip39Wordlist.words.random() }
                w
            }
            (others + correctWord).shuffled()
        }
    }
    val verificationAnswers = remember { mutableStateMapOf<Int, String>() }

    // Keypad state for Master 8-digit PIN (no hardcoded safe/panic PINs)
    val masterPin = remember { mutableStateOf("") }
    var setupError by remember { mutableStateOf("") }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    SecureScreenWindowEffect(enabled = !isRestoreMode && currentStep == 1)

    // Set temporary language dynamically
    LaunchedEffect(tempLang.value) {
        viewModel.updateSystemLanguage(tempLang.value)
    }

    val t = { key: String -> LocalizationHelper.getString(tempLang.value, key) }

    if (isRestoreMode) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF030308))
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (tempLang.value in listOf("ru", "uk")) "🔑 ВОССТАНОВЛЕНИЕ ИЗ СИД-ФРАЗЫ" else "🔑 BIP-39 SEED RESTORATION",
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = NeonCyan
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (tempLang.value in listOf("ru", "uk")) "Введите 12 или 24 слова вашей мнемоники и задайте новый 8-значный ПИН для быстрого входа." else "Enter your 12 or 24-word seed phrase and set a new 8-digit PIN for quick access.",
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 16.sp,
                    color = Color(0xFFB0B0C5),
                    textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = restoreSeedInput,
                    onValueChange = {
                        restoreSeedInput = it
                        restoreError = ""
                    },
                    placeholder = {
                        Text(
                            if (tempLang.value in listOf("ru", "uk")) "Вставьте или введите 12/24 слов через пробел..." else "Paste or type 12/24 words separated by spaces...",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            color = Color(0xFFB0B0C5)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 16.sp, color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color(0x33FFFFFF)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val wordCount = restoreSeedInput.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
                    Text(
                        text = if (tempLang.value in listOf("ru", "uk")) "Слов: $wordCount / 24" else "Words: $wordCount / 24",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        color = if (wordCount == 12 || wordCount == 24) NeonGreen else Color.LightGray
                    )
                    TextButton(
                        onClick = {
                            val clip = clipboardManager.getText()?.text ?: ""
                            if (clip.isNotBlank()) {
                                restoreSeedInput = clip.trim()
                            }
                        }
                    ) {
                        Text(
                            text = if (tempLang.value in listOf("ru", "uk")) "📋 ВСТАВИТЬ" else "📋 PASTE",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = if (tempLang.value in listOf("ru", "uk")) "🔢 ЗАДАЙТЕ 8-ЗНАЧНЫЙ ПИН-КОД ДЛЯ ВХОДА" else "🔢 CONFIGURE 8-DIGIT ENTRY PIN",
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    for (i in 0 until 8) {
                        val filled = restorePinInput.length > i
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(if (filled) NeonCyan else Color(0x22FFFFFF))
                                .border(1.5.dp, if (filled) NeonCyan else Color(0xFFB0B0C5), CircleShape)
                        )
                    }
                }

                if (restoreError.isNotBlank()) {
                    Text(
                        text = restoreError,
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        color = NeonRed,
                        fontWeight = FontWeight.Bold
                    )
                }

                CustomBigKeypad(
                    skin = currentSkin,
                    onDigitClick = { digit ->
                        if (restorePinInput.length < 8) {
                            restorePinInput += digit
                            restoreError = ""
                        }
                    },
                    onDeleteClick = {
                        if (restorePinInput.isNotEmpty()) {
                            restorePinInput = restorePinInput.dropLast(1)
                        }
                    },
                    onClearClick = {
                        restorePinInput = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CyberButton(
                    onClick = { isRestoreMode = false },
                    skin = currentSkin,
                    modifier = Modifier
                        .width(120.dp)
                        .height(44.dp)
                ) {
                    Text(
                        text = if (tempLang.value in listOf("ru", "uk")) "⬅️ НАЗАД" else "⬅️ BACK",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                val wordsList = restoreSeedInput.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
                val isRestoreValid = (wordsList.size == 12 || wordsList.size == 24) && restorePinInput.length == 8

                CyberButton(
                    onClick = {
                        if (!com.example.data.Bip39Helper.validateWords(wordsList)) {
                            restoreError = if (tempLang.value in listOf("ru", "uk")) "❌ Некорректные слова мнемоники BIP-39" else "❌ Invalid BIP-39 mnemonic words"
                        } else if (restorePinInput.length != 8) {
                            restoreError = if (tempLang.value in listOf("ru", "uk")) "❌ ПИН должен содержать ровно 8 цифр" else "❌ PIN must be exactly 8 digits"
                        } else {
                            viewModel.saveSecuredProfileWithPin(
                                dob = "",
                                first = "",
                                last = "",
                                pin = restorePinInput,
                                pinSafe = null,
                                pinPanic = null,
                                aliasVal = "Grower",
                                avatarIdx = 0,
                                customWords = wordsList
                            )
                            com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
                            onComplete()
                        }
                    },
                    skin = currentSkin,
                    modifier = Modifier
                        .width(160.dp)
                        .height(44.dp)
                        .graphicsLayer { alpha = if (isRestoreValid) 1f else 0.4f },
                    enabled = isRestoreValid
                ) {
                    Text(
                        text = if (tempLang.value in listOf("ru", "uk")) "ВОССТАНОВИТЬ 🔓" else "RESTORE 🔓",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030308))
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STEP ${currentStep + 1} OF 6",
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = currentSkin.primaryColor
                )
                Text(
                    text = when (currentStep) {
                        0 -> "🌐 LANGUAGE & RESPONSIBILITY"
                        1 -> "🌱 SEED MEMORY CORE"
                        2 -> "🧠 NEURAL CHALLENGE"
                        3 -> "👤 LOCAL GROWER IDENT"
                        4 -> "📦 DECENTRALIZED CONTAINERS"
                        else -> "🔢 SECURITY INTERLOCK"
                    },
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFFB0B0C5)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (i in 0 until 6) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (currentStep >= i) currentSkin.primaryColor else Color(0x33FFFFFF))
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (currentStep) {
                0 -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "🌐 LOCALIZATION DISCOVERY SCAN",
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(currentSkin.primaryColor.copy(alpha = 0.05f))
                                .border(1.5.dp, currentSkin.primaryColor.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Lang",
                                tint = currentSkin.primaryColor,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            LocalizationHelper.languages.chunked(2).forEach { pair ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    pair.forEach { (code, name) ->
                                        val isSelected = tempLang.value == code
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) currentSkin.primaryColor.copy(alpha = 0.15f) else Color(0xFF161622))
                                                .border(1.5.dp, if (isSelected) currentSkin.primaryColor else Color(0x1AFFFFFF), RoundedCornerShape(10.dp))
                                                .clickable {
                                                    com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.CLICK)
                                                    tempLang.value = code
                                                }
                                                .padding(horizontal = 12.dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) currentSkin.primaryColor else Color(0xFFB0B0C5))
                                                )
                                                Text(
                                                    text = name,
                                                    fontFamily = RajdhaniFontFamily,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.White else Color.LightGray
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1111)),
                            border = BorderStroke(1.5.dp, NeonRed.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Warning",
                                        tint = NeonRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = if (tempLang.value == "ru") "⚠️ ВАЖНОЕ ПРЕДУПРЕЖДЕНИЕ БЕЗОПАСНОСТИ" else "⚠️ CRITICAL SECURITY WARNING",
                                        fontFamily = RajdhaniFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = NeonRed
                                    )
                                }
                                Text(
                                    text = if (tempLang.value == "ru") {
                                        "Это полностью децентрализованное приложение с некастодиальным кошельком. Здесь НЕТ центрального сервера, службы поддержки клиентов или механизма восстановления паролей.\n\n" +
                                        "• Только ВЫ отвечаете за сохранность вашей сид-фразы и ПИН-кодов.\n" +
                                        "• Если вы потеряете сид-фразу, никто и никогда не сможет восстановить ваш кошелек или ваши токены WEED.\n" +
                                        "• При утере данных они исчезнут навсегда."
                                    } else {
                                        "This is a fully decentralized, self-custodial neural application. There is NO central server, no customer support, and no password reset.\n\n" +
                                        "• ONLY YOU are responsible for saving your security credentials and PINs.\n" +
                                        "• If you lose your seed phrase, NO ONE can recover your wallet or access your WEED assets.\n" +
                                        "• All lost data is gone forever."
                                    },
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 16.sp,
                                    color = Color.LightGray,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0C1425),
                            border = BorderStroke(1.5.dp, NeonCyan.copy(alpha = 0.7f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.CLICK)
                                    isRestoreMode = true
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = "Restore",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (tempLang.value in listOf("ru", "uk")) "🔑 У МЕНЯ ЕСТЬ СИД • ВОССТАНОВИТЬ" else "🔑 HAVE A SEED PHRASE? RESTORE",
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = NeonCyan
                                )
                            }
                        }
                    }
                }
                1 -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = t("bip39_header"),
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (tempLang.value == "ru") "Запишите эту мнемоническую фразу из 24 слов на бумагу и сохраните в надежном месте." else "Please write down this 24-word mnemonic seed phrase on paper and save it securely.",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            color = Color(0xFFB0B0C5),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF11111E)),
                            border = BorderStroke(1.5.dp, currentSkin.primaryColor.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "⚠️ " + t("seed_warning"),
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonRed,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                HorizontalDivider(color = Color(0x22FFFFFF), thickness = 1.dp)

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    generatedMnemonicList.chunked(3).forEach { chunk ->
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            chunk.forEach { word ->
                                                val idx = generatedMnemonicList.indexOf(word) + 1
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(34.dp)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color(0xFF1B1B2B))
                                                        .padding(horizontal = 8.dp),
                                                    contentAlignment = Alignment.CenterStart
                                                ) {
                                                    Text(
                                                        text = "$idx. $word",
                                                        fontFamily = RajdhaniFontFamily,
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Button(
                                    onClick = {
                                        com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
                                        copySensitiveTextWithAutoClear(
                                            context = context,
                                            scope = coroutineScope,
                                            label = "BIP39 Seed",
                                            secretText = generatedMnemonicList.joinToString(" ")
                                        )
                                        viewModel.postNotification("📋 Seed copied to sensitive clipboard (auto-clears in 45s)")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = currentSkin.primaryColor.copy(alpha = 0.2f)),
                                    border = BorderStroke(1.dp, currentSkin.primaryColor),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().height(44.dp)
                                ) {
                                    Text(
                                        text = t("btn_copy"),
                                        fontFamily = RajdhaniFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
                2 -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = if (tempLang.value == "ru") "🧠 ПРОВЕРКА СИД-ФРАЗЫ" else "🧠 NEURAL SEED VERIFICATION",
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (tempLang.value == "ru") "Выберите правильные слова для проверки, что вы действительно сохранили мнемонику." else "Select the correct words to verify you have securely saved your seed phrase.",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            color = Color(0xFFB0B0C5),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        testIndices.forEach { testIdx ->
                            val correctWord = generatedMnemonicList[testIdx]
                            val options = verificationOptions[testIdx] ?: emptyList()
                            val selected = verificationAnswers[testIdx]

                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F1A)),
                                border = BorderStroke(1.dp, if (selected == correctWord) NeonGreen.copy(alpha = 0.4f) else if (selected != null) NeonRed.copy(alpha = 0.4f) else Color.DarkGray),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = if (tempLang.value == "ru") "Какое слово идет под номером #${testIdx + 1}?" else "Identify Word #${testIdx + 1}:",
                                        fontFamily = RajdhaniFontFamily,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        options.take(2).forEach { option ->
                                            val isChosen = selected == option
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(38.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (isChosen) (if (option == correctWord) NeonGreen.copy(alpha = 0.2f) else NeonRed.copy(alpha = 0.2f)) else Color(0xFF1B1B2B))
                                                    .border(1.dp, if (isChosen) (if (option == correctWord) NeonGreen else NeonRed) else Color.Transparent, RoundedCornerShape(6.dp))
                                                    .clickable {
                                                        com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.CLICK)
                                                        verificationAnswers[testIdx] = option
                                                    }
                                                    .padding(horizontal = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = option,
                                                    fontFamily = RajdhaniFontFamily,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        options.drop(2).take(2).forEach { option ->
                                            val isChosen = selected == option
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(38.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (isChosen) (if (option == correctWord) NeonGreen.copy(alpha = 0.2f) else NeonRed.copy(alpha = 0.2f)) else Color(0xFF1B1B2B))
                                                    .border(1.dp, if (isChosen) (if (option == correctWord) NeonGreen else NeonRed) else Color.Transparent, RoundedCornerShape(6.dp))
                                                    .clickable {
                                                        com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.CLICK)
                                                        verificationAnswers[testIdx] = option
                                                    }
                                                    .padding(horizontal = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = option,
                                                    fontFamily = RajdhaniFontFamily,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    val avatars = listOf(
                        "Neon Botanist",
                        "Cyber Farmer",
                        "Solar Harvester",
                        "Quantum Grower",
                        "Ganja Guru",
                        "Netrunner Alchemist"
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "👤 CHOOSE LOCAL GROWER IDENTITY",
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = aliasInput.value,
                            onValueChange = { aliasInput.value = it },
                            label = { Text("Local Alias (Username)", color = Color(0xFFB0B0C5), fontSize = 16.sp, fontFamily = RajdhaniFontFamily) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = currentSkin.primaryColor,
                                unfocusedBorderColor = Color(0x33FFFFFF),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 14.sp),
                            modifier = Modifier.fillMaxWidth().testTag("onboarding_alias_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            avatars.chunked(2).forEachIndexed { rowIdx, pair ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    pair.forEach { avName ->
                                        val curIdx = rowIdx * 2 + pair.indexOf(avName)
                                        val isSelected = selectedAvatarIdx.value == curIdx
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(56.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) currentSkin.primaryColor.copy(alpha = 0.15f) else Color(0xFF13131F))
                                                .border(1.5.dp, if (isSelected) currentSkin.primaryColor else Color(0x1AFFFFFF), RoundedCornerShape(10.dp))
                                                .clickable {
                                                    com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.CLICK)
                                                    selectedAvatarIdx.value = curIdx
                                                }
                                                .padding(horizontal = 12.dp, vertical = 6.dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) currentSkin.primaryColor else Color(0xFFB0B0C5)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Face,
                                                        contentDescription = "Avatar",
                                                        tint = Color.Black,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                Text(
                                                    text = avName,
                                                    fontFamily = RajdhaniFontFamily,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.White else Color.LightGray
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                4 -> {
                    val cloudEarned = allocatedStorageGb.value / 4f
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "📦 DECENTRALIZED CRYPTO-CONTAINER",
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Choose storage allocation for your local AES-256-GCM container and experimental LAN Swarm replication.",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            color = Color(0xFFB0B0C5),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF10101C)),
                            border = BorderStroke(1.dp, Color(0x22FFFFFF)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "ALLOCATE STORAGE SPACE: ${allocatedStorageGb.value.toInt()} GB",
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = currentSkin.primaryColor
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Slider(
                                    value = allocatedStorageGb.value,
                                    onValueChange = {
                                        val stepped = when {
                                            it < 24 -> 16f
                                            it < 48 -> 32f
                                            it < 96 -> 64f
                                            else -> 128f
                                        }
                                        allocatedStorageGb.value = stepped
                                    },
                                    valueRange = 16f..128f,
                                    steps = 3,
                                    colors = SliderDefaults.colors(
                                        activeTrackColor = currentSkin.primaryColor,
                                        thumbColor = currentSkin.primaryColor
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("P2P SHARED", fontSize = 15.sp, color = Color(0xFFB0B0C5), fontFamily = RajdhaniFontFamily)
                                        Text("${allocatedStorageGb.value.toInt()} GB", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White, fontFamily = RajdhaniFontFamily)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("CLOUD BACKUP", fontSize = 15.sp, color = Color(0xFFB0B0C5), fontFamily = RajdhaniFontFamily)
                                        Text("${cloudEarned.toInt()} GB", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = NeonGreen, fontFamily = RajdhaniFontFamily)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF121A16)),
                            border = BorderStroke(1.dp, Color(0x3300FF00)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "📖 DECENTRALIZED CRYPTO-CLOUD STUDY",
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = NeonGreen
                                )
                                Text(
                                    text = "This application stores encrypted local archives inside an AES-256-GCM container and supports experimental LAN Swarm shard replication plus an offline Sandbox simulator. No centralized logging server exists.",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 15.sp,
                                    color = Color.LightGray
                                )
                                HorizontalDivider(color = Color(0x2200FF00), thickness = 1.dp)
                                Text(
                                    text = "• LOCAL ENCRYPTION: Container shards use AES-256-GCM with keys sealed by AndroidKeyStore + PBKDF2-HMAC-SHA256.\n" +
                                           "• AUTHENTICATED LAN SWARM: TCP shard replication transfers only ciphertext frames with HMAC-SHA256 peer auth and SHA-256 integrity digests.\n" +
                                           "• HONEST SANDBOX: Simulated P2P transfers and on-chain previews are labeled [SANDBOX] without fake mainnet broadcasts.\n" +
                                           "• CONTROL: You can deallocate containers, reset credentials, or trigger an explicit wipe inside Settings anytime.",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 14.sp,
                                    color = Color(0xFFB0B0C5)
                                )
                            }
                        }
                    }
                }
                else -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🔢 MASTER ENTRY PIN",
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = currentSkin.primaryColor
                        )

                        Text(
                            text = "Set up your 8-digit PIN code to lock, verify, and authorize fast entry into the app.",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            color = Color(0xFFB0B0C5),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(vertical = 10.dp)
                        ) {
                            for (i in 0 until 8) {
                                val filled = masterPin.value.length > i
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(if (filled) currentSkin.primaryColor else Color(0x22FFFFFF))
                                        .border(1.5.dp, if (filled) currentSkin.primaryColor else Color(0xFFB0B0C5), CircleShape)
                                )
                            }
                        }

                        if (setupError.isNotBlank()) {
                            Text(
                                text = setupError,
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 16.sp,
                                color = NeonRed,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        CustomBigKeypad(
                            skin = currentSkin,
                            onDigitClick = { digit ->
                                if (masterPin.value.length < 8) {
                                    masterPin.value += digit
                                    setupError = ""
                                }
                            },
                            onDeleteClick = {
                                if (masterPin.value.isNotEmpty()) {
                                    masterPin.value = masterPin.value.dropLast(1)
                                }
                            },
                            onClearClick = {
                                masterPin.value = ""
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentStep > 0) {
                CyberButton(
                    onClick = {
                        com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.CLICK)
                        currentStep -= 1
                    },
                    skin = currentSkin,
                    modifier = Modifier.width(110.dp).height(44.dp)
                ) {
                    Text(
                        text = "⬅️ BACK",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(110.dp))
            }

            val nextText = if (currentStep == 5 && masterPin.value.length == 8) "ACTIVATE 🔒" else "NEXT ➡️"
            val isNextEnabled = when (currentStep) {
                1 -> true
                2 -> {
                    testIndices.all { idx ->
                        verificationAnswers[idx] == generatedMnemonicList[idx]
                    }
                }
                3 -> aliasInput.value.isNotBlank()
                4 -> true
                5 -> masterPin.value.length == 8
                else -> true
            }

            CyberButton(
                onClick = {
                    com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
                    if (currentStep < 5) {
                        currentStep += 1
                    } else {
                        viewModel.saveSecuredProfileWithPin(
                            dob = "",
                            first = "",
                            last = "",
                            pin = masterPin.value,
                            pinSafe = null,
                            pinPanic = null,
                            aliasVal = aliasInput.value,
                            avatarIdx = selectedAvatarIdx.value,
                            customWords = generatedMnemonicList
                        )
                        onComplete()
                    }
                },
                skin = currentSkin,
                modifier = Modifier.width(130.dp).height(44.dp).graphicsLayer { alpha = if (isNextEnabled) 1f else 0.4f },
                enabled = isNextEnabled
            ) {
                Text(
                    text = nextText,
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
