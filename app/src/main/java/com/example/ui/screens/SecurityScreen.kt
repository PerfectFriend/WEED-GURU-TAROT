package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.LocalizationHelper
import com.example.ui.components.CyberPanel
import com.example.ui.theme.Cyber
import com.example.ui.theme.CyberShapes
import com.example.ui.theme.IbmPlexMonoFontFamily
import com.example.ui.theme.Space
import com.example.viewmodel.TarotViewModel
import kotlinx.coroutines.launch

@Composable
fun SecurityScreen(viewModel: TarotViewModel) {
    val c = Cyber.colors
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val deckRating by viewModel.deckRating.collectAsStateWithLifecycle()
    val deckApproxCost by viewModel.deckApproxCost.collectAsStateWithLifecycle()
    val generatedMnemonic by viewModel.generatedMnemonic.collectAsStateWithLifecycle()
    val verificationIndices by viewModel.verificationIndices.collectAsStateWithLifecycle()
    val verificationOptions by viewModel.verificationOptions.collectAsStateWithLifecycle()
    val verificationAnswers by viewModel.verificationAnswers.collectAsStateWithLifecycle()
    val cloudLogs by viewModel.cloudLogs.collectAsStateWithLifecycle()
    val regStep by viewModel.securityRegistrationStep.collectAsStateWithLifecycle()
    val cloudUserEmail by viewModel.cloudUserEmail.collectAsStateWithLifecycle()
    val cloudUserName by viewModel.cloudUserName.collectAsStateWithLifecycle()
    val isCloudSigningIn by viewModel.isCloudSigningIn.collectAsStateWithLifecycle()
    val cloudReadingsCount by viewModel.cloudReadingsCount.collectAsStateWithLifecycle()
    val cloudSyncStatus by viewModel.cloudSyncStatus.collectAsStateWithLifecycle()
    val isWatermarkUnlocked by viewModel.isWatermarkUnlocked.collectAsStateWithLifecycle()
    val isWatermarkHidden by viewModel.isWatermarkHidden.collectAsStateWithLifecycle()

    val lang = profile?.systemLanguage ?: "en"
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    fun t(key: String): String = LocalizationHelper.getString(lang, key)

    var firstNameInput by remember { mutableStateOf("") }
    var lastNameInput by remember { mutableStateOf("") }
    var dobInput by remember { mutableStateOf("") }
    var pinCodeInput by remember { mutableStateOf("") }

    var entropyWord1 by remember { mutableStateOf("") }
    var entropyWord2 by remember { mutableStateOf("") }
    var entropyWord3 by remember { mutableStateOf("") }
    var entropyWord4 by remember { mutableStateOf("") }
    var entropyWord5 by remember { mutableStateOf("") }

    var showSeedRequestPassword by remember { mutableStateOf(false) }
    var seedRequestPasswordInput by remember { mutableStateOf("") }
    var showSeedPhraseReveal by remember { mutableStateOf(false) }
    var revealedSeedPhrase by remember { mutableStateOf("") }

    SecureScreenWindowEffect(enabled = showSeedPhraseReveal || regStep == 1)

    LaunchedEffect(showSeedPhraseReveal) {
        revealedSeedPhrase = if (showSeedPhraseReveal) {
            viewModel.getDecryptedSeedPhrase()
        } else {
            ""
        }
    }

    var proxyType by remember { mutableStateOf(profile?.proxyType ?: "NONE") }
    var proxyHost by remember { mutableStateOf(profile?.proxyHost ?: "127.0.0.1") }
    var proxyPortString by remember { mutableStateOf((profile?.proxyPort ?: 9050).toString()) }
    var proxyConfig by remember { mutableStateOf(viewModel.decryptProxyConfig(profile?.proxyConfig ?: "")) }
    var isProxyActive by remember { mutableStateOf(profile?.isProxyActive ?: false) }

    LaunchedEffect(profile) {
        profile?.let {
            proxyType = it.proxyType
            proxyHost = it.proxyHost
            proxyPortString = it.proxyPort.toString()
            proxyConfig = viewModel.decryptProxyConfig(it.proxyConfig)
            isProxyActive = it.isProxyActive
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Space.lg),
        contentPadding = PaddingValues(top = Space.sm, bottom = Space.xxl),
        verticalArrangement = Arrangement.spacedBy(Space.lg)
    ) {
        item {
            Text(
                text = t("settings_header"),
                style = MaterialTheme.typography.headlineMedium,
                color = c.accent,
                modifier = Modifier.testTag("security_title")
            )
            Text(
                text = "Secure your local core, configure routing gateways, and manage decentralized cloud backup nodes.",
                style = MaterialTheme.typography.bodyMedium,
                color = c.textMid,
                modifier = Modifier.padding(top = Space.xs, bottom = Space.sm)
            )
        }

        item {
            SecuritySkinsCard(
                currentSkin = currentSkin,
                lang = lang,
                onSelectSkin = { viewModel.updateSkin(it) }
            )
        }

        item {
            SecurityIdentityCard(
                viewModel = viewModel,
                context = context,
                profile = profile,
                currentSkin = currentSkin,
                deckRating = deckRating,
                deckApproxCost = deckApproxCost
            )
        }

        item {
            SecurityLanguageCard(
                lang = lang,
                headerText = t("lang_header"),
                descText = t("lang_desc"),
                onSelectLanguage = { viewModel.updateSystemLanguage(it) }
            )
        }

        item {
            SecurityWatermarkCard(
                viewModel = viewModel,
                lang = lang,
                isWatermarkUnlocked = isWatermarkUnlocked,
                isWatermarkHidden = isWatermarkHidden,
                balanceUsdc = profile?.balanceUsdc ?: 0.0,
                balanceWeed = profile?.weed ?: 0
            )
        }

        item {
            CyberPanel(
                borderColor = if (profile?.isSecured == true) c.accent else c.goldAntique.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (profile?.isSecured == true) Icons.Default.CheckCircle else Icons.Default.Shield,
                        contentDescription = "Shield",
                        tint = if (profile?.isSecured == true) c.accent else c.textMid,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(Space.sm))
                    Text(
                        text = t("bip39_header"),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (profile?.isSecured == true) c.accent else c.textHigh
                    )
                }

                Spacer(modifier = Modifier.height(Space.md))

                if (profile?.isSecured == true) {
                    Text(
                        text = t("secured_badge") + " TERMINAL CONFIGURATION ACTIVE",
                        style = MaterialTheme.typography.titleSmall,
                        color = c.accent
                    )
                    Spacer(modifier = Modifier.height(Space.sm))
                    Text(
                        text = "Alias: ${profile?.alias ?: "Netrunner"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.textHigh
                    )
                    Text(
                        text = "Vault Protection: Keystore AES-256-GCM + PBKDF2-HMAC-SHA256 PIN",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textMid
                    )
                    Text(
                        text = "Device EC P-256 PubKey: ${viewModel.getDevicePublicKeyBase64().take(18)}...",
                        style = MaterialTheme.typography.labelSmall,
                        color = c.textMid
                    )

                    Spacer(modifier = Modifier.height(Space.lg))

                    if (showSeedPhraseReveal) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(c.ink, CyberShapes.chip)
                                .border(1.dp, c.accent, CyberShapes.chip)
                                .padding(Space.md)
                        ) {
                            Column {
                                Text(
                                    "RECOVERY MNEMONIC (24 WORDS) • SCREEN CAPTURE BLOCKED:",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = c.accent
                                )
                                Spacer(modifier = Modifier.height(Space.sm))
                                Text(
                                    revealedSeedPhrase.ifBlank { "Decrypting from Keystore vault..." },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = IbmPlexMonoFontFamily,
                                    color = c.textHigh
                                )
                                Spacer(modifier = Modifier.height(Space.md))
                                Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                                    CyberTextButton(
                                        onClick = {
                                            copySensitiveTextWithAutoClear(
                                                context = context,
                                                scope = coroutineScope,
                                                label = "BIP39 Seed",
                                                secretText = revealedSeedPhrase
                                            )
                                            viewModel.postNotification("Seed copied to sensitive clipboard (auto-clears in 45s)")
                                        },
                                        text = t("btn_copy"),
                                        skin = currentSkin,
                                        containerColor = c.accent.copy(alpha = 0.2f),
                                        contentColor = c.accent,
                                        shape = CyberShapes.control,
                                        modifier = Modifier.weight(1f)
                                    )
                                    CyberTextButton(
                                        onClick = { showSeedPhraseReveal = false },
                                        text = t("btn_hide_seed"),
                                        skin = currentSkin,
                                        containerColor = c.panelRaised,
                                        contentColor = c.textHigh,
                                        shape = CyberShapes.control,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                            CyberTextButton(
                                onClick = { showSeedRequestPassword = true },
                                text = t("btn_view_seed"),
                                skin = currentSkin,
                                containerColor = c.accent.copy(alpha = 0.16f),
                                contentColor = c.accent,
                                shape = CyberShapes.control,
                                modifier = Modifier.weight(1f)
                            )
                            CyberTextButton(
                                onClick = { viewModel.resetSecurity() },
                                text = "DEACTIVATE",
                                skin = currentSkin,
                                containerColor = c.danger.copy(alpha = 0.16f),
                                contentColor = c.danger,
                                shape = CyberShapes.control,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else {
                    when (regStep) {
                        0 -> {
                            Text(
                                text = t("bip39_desc"),
                                style = MaterialTheme.typography.bodyMedium,
                                color = c.textMid
                            )
                            Spacer(modifier = Modifier.height(Space.md))

                            val fieldColors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = c.accent,
                                unfocusedBorderColor = c.goldAntique.copy(alpha = 0.35f),
                                focusedLabelColor = c.accent,
                                unfocusedLabelColor = c.textMid,
                                focusedTextColor = c.textHigh,
                                unfocusedTextColor = c.textHigh
                            )

                            OutlinedTextField(
                                value = firstNameInput,
                                onValueChange = { firstNameInput = it },
                                label = { Text("First Name") },
                                singleLine = true,
                                colors = fieldColors,
                                textStyle = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.fillMaxWidth().testTag("first_name_input")
                            )
                            Spacer(modifier = Modifier.height(Space.sm))
                            OutlinedTextField(
                                value = lastNameInput,
                                onValueChange = { lastNameInput = it },
                                label = { Text("Last Name") },
                                singleLine = true,
                                colors = fieldColors,
                                textStyle = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.fillMaxWidth().testTag("last_name_input")
                            )
                            Spacer(modifier = Modifier.height(Space.sm))
                            OutlinedTextField(
                                value = dobInput,
                                onValueChange = { dobInput = it },
                                label = { Text("Date of Birth (YYYY-MM-DD)") },
                                placeholder = { Text("1998-05-15") },
                                singleLine = true,
                                colors = fieldColors,
                                textStyle = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.fillMaxWidth().testTag("dob_input")
                            )

                            Spacer(modifier = Modifier.height(Space.md))
                            Text(
                                text = if (lang == "ru") {
                                    "ИСТОЧНИК ЭНТРОПИИ (5 слов, до 16 букв каждое):"
                                } else {
                                    "ENTROPY SOURCE (5 words, up to 16 letters each):"
                                },
                                style = MaterialTheme.typography.labelLarge,
                                color = c.accent,
                                modifier = Modifier.padding(bottom = Space.xs)
                            )

                            listOf(
                                Triple("Word 1 / Слово 1", entropyWord1) { v: String -> if (v.length <= 16 && v.all { it.isLetter() }) entropyWord1 = v },
                                Triple("Word 2 / Слово 2", entropyWord2) { v: String -> if (v.length <= 16 && v.all { it.isLetter() }) entropyWord2 = v },
                                Triple("Word 3 / Слово 3", entropyWord3) { v: String -> if (v.length <= 16 && v.all { it.isLetter() }) entropyWord3 = v },
                                Triple("Word 4 / Слово 4", entropyWord4) { v: String -> if (v.length <= 16 && v.all { it.isLetter() }) entropyWord4 = v },
                                Triple("Word 5 / Слово 5", entropyWord5) { v: String -> if (v.length <= 16 && v.all { it.isLetter() }) entropyWord5 = v }
                            ).forEachIndexed { idx, (label, value, onValueChange) ->
                                OutlinedTextField(
                                    value = value,
                                    onValueChange = onValueChange,
                                    label = { Text(label) },
                                    singleLine = true,
                                    colors = fieldColors,
                                    textStyle = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = Space.xxs)
                                        .testTag("entropy_word_${idx + 1}")
                                )
                            }

                            Spacer(modifier = Modifier.height(Space.lg))

                            CyberTextButton(
                                onClick = {
                                    viewModel.generateSecureSeed(
                                        birthdate = dobInput,
                                        firstName = firstNameInput,
                                        lastName = lastNameInput,
                                        entropyWords = listOf(entropyWord1, entropyWord2, entropyWord3, entropyWord4, entropyWord5)
                                    )
                                },
                                text = t("btn_generate"),
                                skin = currentSkin,
                                containerColor = c.accent,
                                contentColor = c.ink,
                                shape = CyberShapes.control,
                                modifier = Modifier.fillMaxWidth().testTag("generate_seed_button")
                            )
                        }
                        1 -> {
                            Text(
                                text = t("seed_generated_title"),
                                style = MaterialTheme.typography.titleMedium,
                                color = c.accent
                            )
                            Spacer(modifier = Modifier.height(Space.xs))
                            Text(
                                text = t("seed_warning"),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = c.danger
                            )
                            Spacer(modifier = Modifier.height(Space.md))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(c.ink.copy(alpha = 0.7f), CyberShapes.chip)
                                    .border(1.dp, c.goldAntique.copy(alpha = 0.3f), CyberShapes.chip)
                                    .padding(Space.sm)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(Space.xs)) {
                                    for (row in 0 until 6) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(Space.xs)
                                        ) {
                                            for (col in 0 until 4) {
                                                val wordIdx = row * 4 + col
                                                val word = generatedMnemonic?.getOrNull(wordIdx) ?: ""
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .background(c.panelRaised, CyberShapes.chip)
                                                        .border(1.dp, c.goldAntique.copy(alpha = 0.2f), CyberShapes.chip)
                                                        .padding(horizontal = Space.xs, vertical = Space.sm)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            "${wordIdx + 1}.",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = c.textMid,
                                                            modifier = Modifier.width(18.dp)
                                                        )
                                                        Text(
                                                            word,
                                                            style = MaterialTheme.typography.labelMedium,
                                                            color = c.textHigh,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(Space.lg))

                            Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                                CyberTextButton(
                                    onClick = { viewModel.resetMnemonicForm() },
                                    text = t("btn_abort"),
                                    skin = currentSkin,
                                    containerColor = c.panelRaised,
                                    contentColor = c.textMid,
                                    shape = CyberShapes.control,
                                    modifier = Modifier.weight(1f)
                                )
                                CyberTextButton(
                                    onClick = {
                                        val seedText = generatedMnemonic?.joinToString(" ") ?: ""
                                        copySensitiveTextWithAutoClear(
                                            context = context,
                                            scope = coroutineScope,
                                            label = "BIP39 Seed",
                                            secretText = seedText
                                        )
                                        viewModel.postNotification("Seed copied to sensitive clipboard (auto-clears in 45s)")
                                    },
                                    text = t("btn_copy"),
                                    skin = currentSkin,
                                    containerColor = c.accent.copy(alpha = 0.16f),
                                    contentColor = c.accent,
                                    shape = CyberShapes.control,
                                    modifier = Modifier.weight(1.5f).testTag("copy_seed_button")
                                )
                            }
                            Spacer(modifier = Modifier.height(Space.sm))
                            CyberTextButton(
                                onClick = { viewModel.proceedToVerification() },
                                text = t("btn_proceed"),
                                skin = currentSkin,
                                containerColor = c.accent,
                                contentColor = c.ink,
                                shape = CyberShapes.control,
                                modifier = Modifier.fillMaxWidth().testTag("proceed_verification_button")
                            )
                        }
                        2 -> {
                            Text(
                                text = t("challenge_title"),
                                style = MaterialTheme.typography.titleMedium,
                                color = c.accent
                            )
                            Text(
                                text = t("challenge_desc"),
                                style = MaterialTheme.typography.bodySmall,
                                color = c.textMid,
                                modifier = Modifier.padding(bottom = Space.md)
                            )

                            verificationIndices.forEach { targetIdx ->
                                Column(modifier = Modifier.padding(vertical = Space.xs)) {
                                    Text(
                                        "Select Word #${targetIdx + 1}:",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = c.textHigh
                                    )
                                    Spacer(modifier = Modifier.height(Space.xs))

                                    val options = verificationOptions[targetIdx] ?: emptyList()
                                    val currentSelection = verificationAnswers[targetIdx] ?: ""

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(Space.xs)
                                    ) {
                                        options.forEach { optionWord ->
                                            val isSelected = currentSelection == optionWord
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .heightIn(min = 48.dp)
                                                    .clip(CyberShapes.control)
                                                    .background(if (isSelected) c.accent.copy(alpha = 0.16f) else c.panelRaised)
                                                    .border(
                                                        width = 1.dp,
                                                        color = if (isSelected) c.accent else Color.Transparent,
                                                        shape = CyberShapes.control
                                                    )
                                                    .clickable { viewModel.selectVerificationWord(targetIdx, optionWord) }
                                                    .padding(vertical = Space.sm),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = optionWord,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = if (isSelected) c.accent else c.textHigh
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(Space.lg))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Space.sm)
                            ) {
                                CyberTextButton(
                                    onClick = { viewModel.setSecurityRegistrationStep(1) },
                                    text = "BACK TO SEED",
                                    skin = currentSkin,
                                    containerColor = c.panelRaised,
                                    contentColor = c.textMid,
                                    shape = CyberShapes.control,
                                    modifier = Modifier.weight(1f)
                                )
                                CyberTextButton(
                                    onClick = { viewModel.verifyAndSaveSecureSeed() },
                                    text = t("btn_verify"),
                                    skin = currentSkin,
                                    containerColor = c.accent,
                                    contentColor = c.ink,
                                    shape = CyberShapes.control,
                                    modifier = Modifier.weight(1.5f).testTag("verify_challenge_button")
                                )
                            }
                        }
                        3 -> {
                            Text(
                                text = t("pin_setup_title"),
                                style = MaterialTheme.typography.titleMedium,
                                color = c.accent
                            )
                            Spacer(modifier = Modifier.height(Space.xs))
                            Text(
                                text = t("pin_setup_desc"),
                                style = MaterialTheme.typography.bodySmall,
                                color = c.textMid
                            )
                            Spacer(modifier = Modifier.height(Space.md))

                            OutlinedTextField(
                                value = pinCodeInput,
                                onValueChange = {
                                    if (it.length <= 8 && it.all { char -> char.isDigit() }) {
                                        pinCodeInput = it
                                    }
                                },
                                placeholder = { Text("Enter 8 digits") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = c.accent,
                                    unfocusedBorderColor = c.goldAntique.copy(alpha = 0.35f),
                                    focusedTextColor = c.textHigh,
                                    unfocusedTextColor = c.textHigh
                                ),
                                textStyle = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = IbmPlexMonoFontFamily,
                                    letterSpacing = 4.sp,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("pin_code_setup_input")
                            )

                            Spacer(modifier = Modifier.height(Space.md))

                            Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                                CyberTextButton(
                                    onClick = { viewModel.resetMnemonicForm() },
                                    text = "ABORT",
                                    skin = currentSkin,
                                    containerColor = c.panelRaised,
                                    contentColor = c.textMid,
                                    shape = CyberShapes.control,
                                    modifier = Modifier.weight(1f)
                                )
                                CyberTextButton(
                                    onClick = {
                                        viewModel.saveSecuredProfileWithPin(dobInput, firstNameInput, lastNameInput, pinCodeInput)
                                    },
                                    text = "ACTIVATE PIN",
                                    skin = currentSkin,
                                    containerColor = c.accent,
                                    contentColor = c.ink,
                                    shape = CyberShapes.control,
                                    modifier = Modifier.weight(2f).testTag("save_pin_button")
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            SecurityCloudAllocationCard(
                viewModel = viewModel,
                profile = profile,
                currentSkin = currentSkin,
                cloudLogs = cloudLogs,
                t = ::t
            )
        }

        item {
            SecurityProxyAndCloudSyncCards(
                viewModel = viewModel,
                context = context,
                lang = lang,
                currentSkin = currentSkin,
                proxyType = proxyType,
                onProxyTypeChange = { proxyType = it },
                proxyHost = proxyHost,
                onProxyHostChange = { proxyHost = it },
                proxyPortString = proxyPortString,
                onProxyPortChange = { proxyPortString = it },
                proxyConfig = proxyConfig,
                onProxyConfigChange = { proxyConfig = it },
                isProxyActive = isProxyActive,
                cloudUserEmail = cloudUserEmail,
                cloudUserName = cloudUserName,
                isCloudSigningIn = isCloudSigningIn,
                cloudReadingsCount = cloudReadingsCount,
                cloudSyncStatus = cloudSyncStatus,
                t = ::t
            )
        }
    }

    if (showSeedRequestPassword) {
        AlertDialog(
            onDismissRequest = { showSeedRequestPassword = false },
            title = {
                Text(
                    text = "AUTHORIZE TERMINAL DECRYPTION",
                    style = MaterialTheme.typography.titleMedium,
                    color = c.accent
                )
            },
            text = {
                Column {
                    Text(
                        text = t("input_password_reveal"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.textMid
                    )
                    Spacer(modifier = Modifier.height(Space.sm))
                    OutlinedTextField(
                        value = seedRequestPasswordInput,
                        onValueChange = { seedRequestPasswordInput = it },
                        label = { Text("Password") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = c.accent,
                            unfocusedBorderColor = c.goldAntique.copy(alpha = 0.35f),
                            focusedTextColor = c.textHigh,
                            unfocusedTextColor = c.textHigh
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val candidate = seedRequestPasswordInput
                        coroutineScope.launch {
                            val hasPin = profile?.pinCode?.isNotBlank() == true
                            val authorized = if (hasPin) viewModel.verifyPrimaryPin(candidate) else candidate.isNotEmpty()
                            if (authorized) {
                                showSeedRequestPassword = false
                                showSeedPhraseReveal = true
                                seedRequestPasswordInput = ""
                            } else {
                                viewModel.postNotification("Incorrect security PIN. Access denied.")
                            }
                        }
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("AUTHORIZE", style = MaterialTheme.typography.labelLarge, color = c.accent)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSeedRequestPassword = false },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("CANCEL", style = MaterialTheme.typography.labelLarge, color = c.textMid)
                }
            },
            containerColor = c.panel,
            titleContentColor = c.textHigh,
            textContentColor = c.textMid
        )
    }
}
