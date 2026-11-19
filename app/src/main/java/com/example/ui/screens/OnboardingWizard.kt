package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.CyberNoirSynthPlayer
import com.example.data.Bip39Helper
import com.example.data.Bip39Wordlist
import com.example.data.LocalizationHelper
import com.example.ui.components.CyberActionButton
import com.example.ui.components.CyberChip
import com.example.ui.components.CyberPanel
import com.example.ui.theme.Cyber
import com.example.ui.theme.CyberShapes
import com.example.ui.theme.IbmPlexMonoFontFamily
import com.example.ui.theme.Space
import com.example.viewmodel.TarotViewModel

@Composable
fun OnboardingWizard(viewModel: TarotViewModel, onComplete: () -> Unit) {
    val c = Cyber.colors
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    var currentStep by remember { mutableStateOf(0) }
    var isRestoreMode by remember { mutableStateOf(false) }
    var restoreSeedInput by remember { mutableStateOf("") }
    var restorePinInput by remember { mutableStateOf("") }
    var restoreError by remember { mutableStateOf("") }

    val tempLang = remember { mutableStateOf(profile?.systemLanguage ?: "en") }
    val aliasInput = remember { mutableStateOf("") }
    val selectedAvatarIdx = remember { mutableStateOf(0) }
    val allocatedStorageGb = remember { mutableStateOf(16f) }

    val generatedMnemonicList = remember {
        Bip39Helper.generateMnemonic(Bip39Helper.generateRandomEntropy(256))
    }

    val testIndices = remember { (0..23).shuffled().take(3).sorted() }
    val verificationOptions = remember {
        testIndices.associateWith { correctIdx ->
            val correctWord = generatedMnemonicList[correctIdx]
            val others = (1..3).map {
                var w = Bip39Wordlist.words.random()
                while (w == correctWord) {
                    w = Bip39Wordlist.words.random()
                }
                w
            }
            (others + correctWord).shuffled()
        }
    }
    val verificationAnswers = remember { mutableStateMapOf<Int, String>() }

    val masterPin = remember { mutableStateOf("") }
    var setupError by remember { mutableStateOf("") }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    SecureScreenWindowEffect(enabled = !isRestoreMode && currentStep == 1)

    LaunchedEffect(tempLang.value) {
        viewModel.updateSystemLanguage(tempLang.value)
    }

    val t = { key: String -> LocalizationHelper.getString(tempLang.value, key) }

    if (isRestoreMode) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(c.ink)
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(Space.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (tempLang.value in listOf("ru", "uk")) "ВОССТАНОВЛЕНИЕ ИЗ СИД-ФРАЗЫ" else "BIP-39 SEED RESTORATION",
                    style = MaterialTheme.typography.titleMedium,
                    color = c.accent
                )
                Spacer(modifier = Modifier.height(Space.xs))
                Text(
                    text = if (tempLang.value in listOf("ru", "uk")) {
                        "Введите 12 или 24 слова вашей мнемоники и задайте новый 8-значный ПИН для быстрого входа."
                    } else {
                        "Enter your 12 or 24-word seed phrase and set a new 8-digit PIN for quick access."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textMid,
                    textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Space.md),
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
                            if (tempLang.value in listOf("ru", "uk")) "Вставьте или введите 12/24 слов через пробел..." else "Paste or type 12/24 words separated by spaces..."
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = c.textHigh),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = c.accent,
                        unfocusedBorderColor = c.goldAntique.copy(alpha = 0.35f)
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
                        style = MaterialTheme.typography.labelLarge,
                        color = if (wordCount == 12 || wordCount == 24) c.emeraldGlow else c.textHigh
                    )
                    TextButton(
                        onClick = {
                            val clip = clipboardManager.getText()?.text ?: ""
                            if (clip.isNotBlank()) {
                                restoreSeedInput = clip.trim()
                            }
                        },
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = null,
                            tint = c.accent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(Space.xs))
                        Text(
                            text = if (tempLang.value in listOf("ru", "uk")) "ВСТАВИТЬ" else "PASTE",
                            style = MaterialTheme.typography.labelLarge,
                            color = c.accent
                        )
                    }
                }

                Text(
                    text = if (tempLang.value in listOf("ru", "uk")) "ЗАДАЙТЕ 8-ЗНАЧНЫЙ ПИН-КОД ДЛЯ ВХОДА" else "CONFIGURE 8-DIGIT ENTRY PIN",
                    style = MaterialTheme.typography.titleSmall,
                    color = c.textHigh
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(Space.md),
                    modifier = Modifier.padding(vertical = Space.xs)
                ) {
                    for (i in 0 until 8) {
                        val filled = restorePinInput.length > i
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(if (filled) c.accent else c.panelRaised)
                                .border(1.5.dp, if (filled) c.accent else c.textMid, CircleShape)
                        )
                    }
                }

                if (restoreError.isNotBlank()) {
                    Text(
                        text = restoreError,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = c.danger
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
                    onClearClick = { restorePinInput = "" },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Space.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CyberActionButton(
                    text = if (tempLang.value in listOf("ru", "uk")) "НАЗАД" else "BACK",
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    onClick = { isRestoreMode = false },
                    secondary = true
                )

                val wordsList = restoreSeedInput.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
                val isRestoreValid = (wordsList.size == 12 || wordsList.size == 24) && restorePinInput.length == 8

                CyberActionButton(
                    text = if (tempLang.value in listOf("ru", "uk")) "ВОССТАНОВИТЬ" else "RESTORE",
                    icon = Icons.Default.LockOpen,
                    enabled = isRestoreValid,
                    onClick = {
                        if (!Bip39Helper.validateWords(wordsList)) {
                            restoreError = if (tempLang.value in listOf("ru", "uk")) {
                                "Некорректные слова мнемоники BIP-39"
                            } else {
                                "Invalid BIP-39 mnemonic words"
                            }
                        } else if (restorePinInput.length != 8) {
                            restoreError = if (tempLang.value in listOf("ru", "uk")) {
                                "ПИН должен содержать ровно 8 цифр"
                            } else {
                                "PIN must be exactly 8 digits"
                            }
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
                            CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
                            onComplete()
                        }
                    },
                    modifier = Modifier.graphicsLayer { alpha = if (isRestoreValid) 1f else 0.45f }
                )
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.ink)
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(Space.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = Space.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STEP ${currentStep + 1} OF 6",
                    style = MaterialTheme.typography.titleSmall,
                    fontFamily = IbmPlexMonoFontFamily,
                    color = currentSkin.primaryColor
                )
                Text(
                    text = when (currentStep) {
                        0 -> "LANGUAGE & RESPONSIBILITY"
                        1 -> "SEED MEMORY CORE"
                        2 -> "NEURAL CHALLENGE"
                        3 -> "LOCAL GROWER IDENT"
                        4 -> "DECENTRALIZED CONTAINERS"
                        else -> "SECURITY INTERLOCK"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = c.textMid
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Space.xs)
            ) {
                for (i in 0 until 6) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(CyberShapes.chip)
                            .background(if (currentStep >= i) currentSkin.primaryColor else c.panelRaised)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = Space.lg),
            contentAlignment = Alignment.Center
        ) {
            when (currentStep) {
                0 -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Space.md),
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "LOCALIZATION DISCOVERY SCAN",
                            style = MaterialTheme.typography.titleMedium,
                            color = c.textHigh
                        )
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(currentSkin.primaryColor.copy(alpha = 0.08f))
                                .border(1.5.dp, currentSkin.primaryColor.copy(alpha = 0.45f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Language",
                                tint = currentSkin.primaryColor,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                            LocalizationHelper.languages.chunked(2).forEach { pair ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(Space.sm),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    pair.forEach { (code, name) ->
                                        CyberChip(
                                            label = name,
                                            selected = tempLang.value == code,
                                            onClick = {
                                                CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.CLICK)
                                                tempLang.value = code
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                        CyberPanel(
                            borderColor = c.danger.copy(alpha = 0.65f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Space.sm),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Warning",
                                    tint = c.danger,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = if (tempLang.value == "ru") "ВАЖНОЕ ПРЕДУПРЕЖДЕНИЕ БЕЗОПАСНОСТИ" else "CRITICAL SECURITY WARNING",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = c.danger
                                )
                            }
                            Spacer(modifier = Modifier.height(Space.sm))
                            Text(
                                text = if (tempLang.value == "ru") {
                                    "Это децентрализованное приложение с некастодиальным кошельком. Здесь НЕТ центрального сервера и службы восстановления паролей.\n\n" +
                                        "• Только ВЫ отвечаете за сохранность сид-фразы и ПИН-кодов.\n" +
                                        "• При утере сид-фразы никто не сможет восстановить доступ к активам."
                                } else {
                                    "This is a decentralized, self-custodial neural application. There is NO central server or password reset.\n\n" +
                                        "• ONLY YOU are responsible for saving your seed phrase and PIN.\n" +
                                        "• If you lose your seed phrase, assets cannot be recovered."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = c.textMid
                            )
                        }

                        Surface(
                            onClick = {
                                CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.CLICK)
                                isRestoreMode = true
                            },
                            shape = CyberShapes.control,
                            color = c.panelRaised,
                            border = BorderStroke(1.5.dp, c.accent.copy(alpha = 0.7f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(Space.md),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = "Restore",
                                    tint = c.accent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(Space.sm))
                                Text(
                                    text = if (tempLang.value in listOf("ru", "uk")) "У МЕНЯ ЕСТЬ СИД • ВОССТАНОВИТЬ" else "HAVE A SEED PHRASE? RESTORE",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = c.accent
                                )
                            }
                        }
                    }
                }
                1 -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Space.md),
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = t("bip39_header"),
                            style = MaterialTheme.typography.titleMedium,
                            color = c.textHigh
                        )
                        Text(
                            text = if (tempLang.value == "ru") {
                                "Запишите эту мнемоническую фразу из 24 слов на бумагу и сохраните в надёжном месте."
                            } else {
                                "Write down this 24-word mnemonic seed phrase on paper and save it securely."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textMid,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = Space.md)
                        )

                        CyberPanel(
                            borderColor = currentSkin.primaryColor.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = t("seed_warning"),
                                style = MaterialTheme.typography.labelLarge,
                                color = c.danger,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(Space.sm))
                            HorizontalDivider(color = c.goldAntique.copy(alpha = 0.25f))
                            Spacer(modifier = Modifier.height(Space.sm))

                            Column(verticalArrangement = Arrangement.spacedBy(Space.xs)) {
                                generatedMnemonicList.chunked(3).forEachIndexed { rowIdx, chunk ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(Space.xs),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        chunk.forEachIndexed { colIdx, word ->
                                            val idx = rowIdx * 3 + colIdx + 1
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(CyberShapes.chip)
                                                    .background(c.panelRaised)
                                                    .padding(horizontal = Space.sm, vertical = Space.sm),
                                                contentAlignment = Alignment.CenterStart
                                            ) {
                                                Text(
                                                    text = "$idx. $word",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontFamily = IbmPlexMonoFontFamily,
                                                    color = c.textHigh,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(Space.md))

                            Button(
                                onClick = {
                                    CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
                                    copySensitiveTextWithAutoClear(
                                        context = context,
                                        scope = coroutineScope,
                                        label = "BIP39 Seed",
                                        secretText = generatedMnemonicList.joinToString(" ")
                                    )
                                    viewModel.postNotification("Seed copied to sensitive clipboard (auto-clears in 45s)")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = currentSkin.primaryColor.copy(alpha = 0.22f)),
                                border = BorderStroke(1.dp, currentSkin.primaryColor),
                                shape = CyberShapes.control,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    tint = c.textHigh,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(Space.sm))
                                Text(
                                    text = t("btn_copy"),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = c.textHigh
                                )
                            }
                        }
                    }
                }
                2 -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Space.md),
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = if (tempLang.value == "ru") "ПРОВЕРКА СИД-ФРАЗЫ" else "NEURAL SEED VERIFICATION",
                            style = MaterialTheme.typography.titleMedium,
                            color = c.textHigh
                        )
                        Text(
                            text = if (tempLang.value == "ru") {
                                "Выберите правильные слова для подтверждения резервной копии."
                            } else {
                                "Select the correct words to verify your seed backup."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textMid,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = Space.md)
                        )

                        testIndices.forEach { testIdx ->
                            val correctWord = generatedMnemonicList[testIdx]
                            val options = verificationOptions[testIdx] ?: emptyList()
                            val selected = verificationAnswers[testIdx]

                            Card(
                                colors = CardDefaults.cardColors(containerColor = c.panel),
                                border = BorderStroke(
                                    1.dp,
                                    when (selected) {
                                        correctWord -> c.emeraldGlow.copy(alpha = 0.55f)
                                        null -> c.goldAntique.copy(alpha = 0.35f)
                                        else -> c.danger.copy(alpha = 0.55f)
                                    }
                                ),
                                shape = CyberShapes.cardCompact,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(Space.md),
                                    verticalArrangement = Arrangement.spacedBy(Space.sm)
                                ) {
                                    Text(
                                        text = if (tempLang.value == "ru") "Какое слово идёт под номером #${testIdx + 1}?" else "Identify Word #${testIdx + 1}:",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = c.textHigh
                                    )

                                    options.chunked(2).forEach { pair ->
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(Space.sm),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            pair.forEach { option ->
                                                val isChosen = selected == option
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .heightIn(min = 48.dp)
                                                        .clip(CyberShapes.chip)
                                                        .background(
                                                            if (isChosen) {
                                                                if (option == correctWord) c.emeraldGlow.copy(alpha = 0.22f) else c.danger.copy(alpha = 0.22f)
                                                            } else {
                                                                c.panelRaised
                                                            }
                                                        )
                                                        .border(
                                                            1.dp,
                                                            if (isChosen) {
                                                                if (option == correctWord) c.emeraldGlow else c.danger
                                                            } else {
                                                                Color.Transparent
                                                            },
                                                            CyberShapes.chip
                                                        )
                                                        .clickable {
                                                            CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.CLICK)
                                                            verificationAnswers[testIdx] = option
                                                        }
                                                        .padding(horizontal = Space.sm),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = option,
                                                        style = MaterialTheme.typography.labelLarge,
                                                        color = c.textHigh
                                                    )
                                                }
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
                        verticalArrangement = Arrangement.spacedBy(Space.md),
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "CHOOSE LOCAL GROWER IDENTITY",
                            style = MaterialTheme.typography.titleMedium,
                            color = c.textHigh
                        )

                        OutlinedTextField(
                            value = aliasInput.value,
                            onValueChange = { aliasInput.value = it },
                            label = { Text("Local Alias (Username)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = currentSkin.primaryColor,
                                unfocusedBorderColor = c.goldAntique.copy(alpha = 0.35f),
                                focusedTextColor = c.textHigh,
                                unfocusedTextColor = c.textHigh
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.fillMaxWidth().testTag("onboarding_alias_input")
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                            avatars.chunked(2).forEachIndexed { rowIdx, pair ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(Space.sm),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    pair.forEachIndexed { colIdx, avName ->
                                        val curIdx = rowIdx * 2 + colIdx
                                        val isSelected = selectedAvatarIdx.value == curIdx
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(min = 56.dp)
                                                .clip(CyberShapes.control)
                                                .background(if (isSelected) currentSkin.primaryColor.copy(alpha = 0.16f) else c.panelRaised)
                                                .border(
                                                    1.5.dp,
                                                    if (isSelected) currentSkin.primaryColor else c.goldAntique.copy(alpha = 0.25f),
                                                    CyberShapes.control
                                                )
                                                .clickable {
                                                    CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.CLICK)
                                                    selectedAvatarIdx.value = curIdx
                                                }
                                                .padding(horizontal = Space.md, vertical = Space.sm),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(Space.sm)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) currentSkin.primaryColor else c.textMid),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Face,
                                                        contentDescription = "Avatar",
                                                        tint = c.ink,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                Text(
                                                    text = avName,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = if (isSelected) c.textHigh else c.textMid
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
                        verticalArrangement = Arrangement.spacedBy(Space.md),
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "DECENTRALIZED CRYPTO-CONTAINER",
                            style = MaterialTheme.typography.titleMedium,
                            color = c.textHigh
                        )
                        Text(
                            text = "Choose storage allocation for your local AES-256-GCM container and experimental LAN Swarm replication.",
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textMid,
                            textAlign = TextAlign.Center
                        )

                        CyberPanel(
                            borderColor = c.goldAntique.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "ALLOCATE STORAGE SPACE: ${allocatedStorageGb.value.toInt()} GB",
                                style = MaterialTheme.typography.titleSmall,
                                color = currentSkin.primaryColor,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            Spacer(modifier = Modifier.height(Space.xs))
                            Slider(
                                value = allocatedStorageGb.value,
                                onValueChange = {
                                    allocatedStorageGb.value = when {
                                        it < 24 -> 16f
                                        it < 48 -> 32f
                                        it < 96 -> 64f
                                        else -> 128f
                                    }
                                },
                                valueRange = 16f..128f,
                                steps = 3,
                                colors = SliderDefaults.colors(
                                    activeTrackColor = currentSkin.primaryColor,
                                    thumbColor = currentSkin.primaryColor
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(Space.sm))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("P2P SHARED", style = MaterialTheme.typography.labelMedium, color = c.textMid)
                                    Text("${allocatedStorageGb.value.toInt()} GB", style = MaterialTheme.typography.titleSmall, fontFamily = IbmPlexMonoFontFamily, color = c.textHigh)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("CLOUD BACKUP", style = MaterialTheme.typography.labelMedium, color = c.textMid)
                                    Text("${cloudEarned.toInt()} GB", style = MaterialTheme.typography.titleSmall, fontFamily = IbmPlexMonoFontFamily, color = c.emeraldGlow)
                                }
                            }
                        }

                        CyberPanel(
                            borderColor = c.emeraldGlow.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "DECENTRALIZED CRYPTO-CLOUD NOTES",
                                style = MaterialTheme.typography.labelLarge,
                                color = c.emeraldGlow
                            )
                            Spacer(modifier = Modifier.height(Space.xs))
                            Text(
                                text = "• LOCAL ENCRYPTION: Container shards use AES-256-GCM with keys sealed by AndroidKeyStore + PBKDF2-HMAC-SHA256.\n" +
                                    "• AUTHENTICATED LAN SWARM: TCP shard replication uses HMAC-SHA256 peer auth and SHA-256 digests.\n" +
                                    "• HONEST SANDBOX: Simulated P2P transfers are labeled [SANDBOX] without fake broadcasts.\n" +
                                    "• CONTROL: You can wipe containers or reset credentials inside Settings anytime.",
                                style = MaterialTheme.typography.bodySmall,
                                color = c.textMid
                            )
                        }
                    }
                }
                else -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Space.sm),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "MASTER ENTRY PIN",
                            style = MaterialTheme.typography.titleMedium,
                            color = currentSkin.primaryColor
                        )
                        Text(
                            text = "Set up your 8-digit PIN code to lock, verify, and authorize fast entry into the app.",
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textMid,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = Space.md)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Space.md),
                            modifier = Modifier.padding(vertical = Space.sm)
                        ) {
                            for (i in 0 until 8) {
                                val filled = masterPin.value.length > i
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(if (filled) currentSkin.primaryColor else c.panelRaised)
                                        .border(1.5.dp, if (filled) currentSkin.primaryColor else c.textMid, CircleShape)
                                )
                            }
                        }

                        if (setupError.isNotBlank()) {
                            Text(
                                text = setupError,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = c.danger
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
                            onClearClick = { masterPin.value = "" },
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
                CyberActionButton(
                    text = "BACK",
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    secondary = true,
                    onClick = {
                        CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.CLICK)
                        currentStep -= 1
                    }
                )
            } else {
                Spacer(modifier = Modifier.width(110.dp))
            }

            val nextText = if (currentStep == 5 && masterPin.value.length == 8) "ACTIVATE" else "NEXT"
            val isNextEnabled = when (currentStep) {
                1 -> true
                2 -> testIndices.all { idx -> verificationAnswers[idx] == generatedMnemonicList[idx] }
                3 -> aliasInput.value.isNotBlank()
                4 -> true
                5 -> masterPin.value.length == 8
                else -> true
            }

            CyberActionButton(
                text = nextText,
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                enabled = isNextEnabled,
                onClick = {
                    CyberNoirSynthPlayer.triggerFX(CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
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
                modifier = Modifier.graphicsLayer { alpha = if (isNextEnabled) 1f else 0.45f }
            )
        }
    }
}
