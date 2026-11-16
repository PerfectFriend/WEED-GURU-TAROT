package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.data.LocalizationHelper
import com.example.ui.theme.*
import com.example.viewmodel.TarotViewModel
import kotlinx.coroutines.launch

@Composable
fun SecurityScreen(viewModel: TarotViewModel) {
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

    val lang = profile?.systemLanguage ?: "en"
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Localization helper shortcut
    fun t(key: String): String {
        return LocalizationHelper.getString(lang, key)
    }

    // Wizard Form Inputs
    var firstNameInput by remember { mutableStateOf("") }
    var lastNameInput by remember { mutableStateOf("") }
    var dobInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var pinCodeInput by remember { mutableStateOf("") }

    // 5 custom entropy words
    var entropyWord1 by remember { mutableStateOf("") }
    var entropyWord2 by remember { mutableStateOf("") }
    var entropyWord3 by remember { mutableStateOf("") }
    var entropyWord4 by remember { mutableStateOf("") }
    var entropyWord5 by remember { mutableStateOf("") }

    // Seed Reveal Query States
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

    // Proxy Configurations States (pre-populate decrypted value on profile load)
    var proxyType by remember { mutableStateOf(profile?.proxyType ?: "NONE") }
    var proxyHost by remember { mutableStateOf(profile?.proxyHost ?: "127.0.0.1") }
    var proxyPortString by remember { mutableStateOf((profile?.proxyPort ?: 9050).toString()) }
    var proxyConfig by remember { mutableStateOf(viewModel.decryptProxyConfig(profile?.proxyConfig ?: "")) }
    var isProxyActive by remember { mutableStateOf(profile?.isProxyActive ?: false) }
    var proxyTestStatus by remember { mutableStateOf<String?>(null) }
    var isTestingProxy by remember { mutableStateOf(false) }

    // Sync state when profile is received
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
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 54.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = t("settings_header"),
                fontFamily = RajdhaniFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = NeonCyan,
                modifier = Modifier.testTag("security_title")
            )
            Text(
                text = "Secure your local core, configure routing gateways, and manage decentralized cloud backup nodes.",
                fontFamily = RajdhaniFontFamily,
                fontSize = 16.sp,
                color = Color(0xFFB0B0C5),
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )
        }

        // Section: NEURAL STYLE SKIN SELECTOR (Matrix, Cyberpunk, Void, Dark Nebula, Star Core, Cosmos, Galaxy, VanGogh)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCharcoal.copy(alpha = 0.72f)),
                border = BorderStroke(1.dp, currentSkin.primaryColor.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Skin Settings",
                            tint = currentSkin.primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "NEURAL COSMIC SKINS",
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = "Calibrate the visual wavelength of your oracle interface. Wallpapers dynamically bleed through system cards.",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        color = Color(0xFFB0B0C5),
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    // LazyRow listing all skins horizontally
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(CyberSkin.values()) { skin ->
                            val isSelected = currentSkin == skin
                            Card(
                                modifier = Modifier
                                    .width(135.dp)
                                    .clickable { viewModel.updateSkin(skin) },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) skin.primaryColor.copy(alpha = 0.15f) else Color(0x1A000000)
                                ),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) skin.primaryColor else Color.DarkGray
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Visual thumbnail representation
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(60.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                    ) {
                                        Image(
                                            painter = painterResource(id = skin.backgroundResId),
                                            contentDescription = "Skin ${skin.displayName} wallpaper preview",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(alpha = 0.35f))
                                        )
                                        // Circular selection indicator
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .background(skin.primaryColor, CircleShape)
                                                    .align(Alignment.Center),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = skin.getLocalizedName(lang),
                                        fontFamily = RajdhaniFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = if (isSelected) skin.primaryColor else Color.White
                                    )
                                    Text(
                                        text = skin.id.uppercase(),
                                        fontFamily = RajdhaniFontFamily,
                                        fontSize = 17.sp,
                                        color = Color(0xFFB0B0C5)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = (if (lang == "ru") "Активная палитра: " else if (lang == "es") "Paleta activa: " else "Active Palette: ") + currentSkin.getLocalizedName(lang),
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = currentSkin.primaryColor
                    )
                    Text(
                        text = currentSkin.getLocalizedDescription(lang),
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 14.sp,
                        color = Color.LightGray
                    )
                }
            }
        }

        // Section 0: 👤 USER NEURAL IDENTITY PROFILE
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCharcoal.copy(alpha = 0.8f)),
                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                var aliasText by remember { mutableStateOf(profile?.alias ?: "Matrix_Walker") }
                var bioText by remember { mutableStateOf(profile?.bio ?: "Core system Netrunner navigating the decentralized neural web.") }
                var selectedAvatar by remember { mutableStateOf(profile?.avatarIndex ?: 0) }
                var isEditing by remember { mutableStateOf(false) }

                // Device image picker launcher
                val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickVisualMedia(),
                    onResult = { uri ->
                        if (uri != null) {
                            viewModel.saveDeviceAvatarImage(context, uri)
                        }
                    }
                )

                var showAiGeneratorDialog by remember { mutableStateOf(false) }

                // Sync states on load
                LaunchedEffect(profile) {
                    profile?.let {
                        aliasText = it.alias
                        bioText = it.bio
                        selectedAvatar = it.avatarIndex
                    }
                }

                val avatarList = listOf(
                    Triple("Matrix Walker", NeonGreen, Icons.Default.Person),
                    Triple("Neon Mage", NeonMagenta, Icons.Default.Bolt),
                    Triple("Quantum Ghost", NeonCyan, Icons.Default.Face),
                    Triple("Deck Master", NeonGold, Icons.Default.Star),
                    Triple("Cyber Seeker", Color.White, Icons.Default.Info),
                    Triple("Core Netrunner", ElegantViolet, Icons.Default.Shield)
                )

                if (showAiGeneratorDialog) {
                    var selectedStyle by remember { mutableStateOf("Matrix Rain") }
                    val stylesList = listOf(
                        "Matrix Rain" to "Green cascading binary lines, cyber circuitry symbols.",
                        "Neon Synthwave" to "Orange/magenta sun, retro perspective horizon grid.",
                        "Neural Synapses" to "Cyan interconnected nodes, synapses & network hexagons.",
                        "Quantum Singularity" to "Deep violet gravitational gravity orbits, gold event horizon.",
                        "Arcade Vector" to "Techno-emblem, high-contrast symmetrical geometric vectors."
                    )

                    AlertDialog(
                        onDismissRequest = { showAiGeneratorDialog = false },
                        containerColor = Color(0xFF0F0E17),
                        title = {
                            Text(
                                "NEURAL AVATAR COMPILER v2.4",
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                                fontSize = 16.sp
                            )
                        },
                        text = {
                            Column {
                                Text(
                                    "Select visual design language style for neural synthesis of user ${aliasText}:",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 16.sp,
                                    color = Color.LightGray,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                stylesList.forEach { (styleName, desc) ->
                                    val isStyleSelected = selectedStyle == styleName
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable { selectedStyle = styleName },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isStyleSelected) NeonCyan.copy(alpha = 0.15f) else Color(0xFF161522)
                                        ),
                                        border = BorderStroke(1.dp, if (isStyleSelected) NeonCyan else Color.DarkGray)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                styleName.uppercase(),
                                                fontFamily = RajdhaniFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isStyleSelected) NeonCyan else Color.White,
                                                fontSize = 16.sp
                                            )
                                            Text(
                                                desc,
                                                fontFamily = RajdhaniFontFamily,
                                                color = Color(0xFFB0B0C5),
                                                fontSize = 14.sp,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            CyberButton(
                                onClick = {
                                    showAiGeneratorDialog = false
                                    viewModel.compileAndSaveCustomAvatar(context, aliasText, selectedStyle)
                                },
                                skin = currentSkin,
                                modifier = Modifier.height(36.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("COMPILE", fontFamily = RajdhaniFontFamily, fontSize = 16.sp)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showAiGeneratorDialog = false }) {
                                Text("CANCEL", fontFamily = RajdhaniFontFamily, color = Color(0xFFB0B0C5), fontSize = 16.sp)
                            }
                        }
                    )
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "User Identity",
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "USER NEURAL IDENTITY",
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }

                        IconButton(onClick = {
                            if (isEditing) {
                                viewModel.updateUserProfileSetup(aliasText, selectedAvatar, bioText, profile?.customAvatarUri)
                            }
                            isEditing = !isEditing
                        }) {
                            Icon(
                                imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = NeonCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Avatar & Details block
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (profile?.customAvatarUri != null) {
                            val painter = rememberAsyncImagePainter(
                                model = ImageRequest.Builder(context)
                                    .data(profile?.customAvatarUri)
                                    .crossfade(true)
                                    .build()
                            )
                            Image(
                                painter = painter,
                                contentDescription = "Custom Profile Avatar",
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, NeonCyan, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            val avatarInfo = avatarList.getOrElse(selectedAvatar) { avatarList[0] }
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(avatarInfo.second.copy(alpha = 0.15f))
                                    .border(2.dp, avatarInfo.second, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = avatarInfo.third,
                                    contentDescription = avatarInfo.first,
                                    tint = avatarInfo.second,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            if (isEditing) {
                                OutlinedTextField(
                                    value = aliasText,
                                    onValueChange = { aliasText = it },
                                    label = { Text("Neural Alias", fontSize = 15.sp, fontFamily = RajdhaniFontFamily) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = NeonCyan,
                                        unfocusedBorderColor = Color.DarkGray
                                    ),
                                    textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 16.sp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                Text(
                                    text = aliasText.uppercase(),
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = if (profile?.customAvatarUri != null) NeonCyan else avatarList.getOrElse(selectedAvatar) { avatarList[0] }.second
                                )
                                Text(
                                    text = if (profile?.customAvatarUri != null) "Matrix Class: Custom AI Avatar" else "Matrix Class: ${avatarList.getOrElse(selectedAvatar) { avatarList[0] }.first}",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 16.sp,
                                    color = Color(0xFFB0B0C5),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Summary / bio
                    if (isEditing) {
                        OutlinedTextField(
                            value = bioText,
                            onValueChange = { bioText = it },
                            label = { Text("Short Bio / Userinfo Matrix", fontSize = 15.sp, fontFamily = RajdhaniFontFamily) },
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = Color.DarkGray
                            ),
                            textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 16.sp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = bioText,
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            color = Color.LightGray,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF09090E), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Avatar selector (if editing)
                    if (isEditing) {
                        Text(
                            text = "CUSTOM IDENTITY AVATAR ENGINE",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 15.sp,
                            color = Color(0xFFB0B0C5),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CyberButton(
                                onClick = {
                                    singlePhotoPickerLauncher.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                                skin = currentSkin,
                                modifier = Modifier.weight(1f).height(36.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("DEVICE FILE", fontFamily = RajdhaniFontFamily, fontSize = 15.sp)
                            }
                            CyberButton(
                                onClick = {
                                    showAiGeneratorDialog = true
                                },
                                skin = currentSkin,
                                modifier = Modifier.weight(1f).height(36.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("AI GENERATE", fontFamily = RajdhaniFontFamily, fontSize = 15.sp)
                            }
                        }

                        if (profile?.customAvatarUri != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            CyberButton(
                                onClick = {
                                    viewModel.clearCustomAvatar()
                                },
                                skin = currentSkin,
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("RESTORE STANDARD PRESETS", fontFamily = RajdhaniFontFamily, fontSize = 15.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                            }
                        } else {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "CHOOSE AVATAR PRESET",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 15.sp,
                                color = Color(0xFFB0B0C5),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                avatarList.forEachIndexed { index, triple ->
                                    val isSelected = selectedAvatar == index
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) triple.second.copy(alpha = 0.25f) else Color.Transparent)
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) triple.second else Color.DarkGray,
                                                shape = CircleShape
                                            )
                                            .clickable { selectedAvatar = index },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = triple.third,
                                            contentDescription = triple.first,
                                            tint = if (isSelected) triple.second else Color(0xFFB0B0C5),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    HorizontalDivider(color = Color(0x1AFFFFFF), modifier = Modifier.padding(vertical = 4.dp))

                    // Global Deck Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                                .background(Color(0xFF05050A), RoundedCornerShape(8.dp))
                                .border(1.dp, NeonGold.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "GLOBAL DECK RATING",
                                fontSize = 17.sp,
                                fontFamily = RajdhaniFontFamily,
                                color = NeonGold,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "★ $deckRating",
                                fontSize = 16.sp,
                                fontFamily = RajdhaniFontFamily,
                                color = NeonGold,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                                .background(Color(0xFF05050A), RoundedCornerShape(8.dp))
                                .border(1.dp, NeonCyan.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "APPROX DECK VALUE",
                                fontSize = 17.sp,
                                fontFamily = RajdhaniFontFamily,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$deckApproxCost N$",
                                fontSize = 16.sp,
                                fontFamily = RajdhaniFontFamily,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Section 1: 🌐 GLOBAL LOCALIZATION
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCharcoal.copy(alpha = 0.8f)),
                border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Language Selection",
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = t("lang_header"),
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = t("lang_desc"),
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        color = Color(0xFFB0B0C5)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // 8-Language Grid
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            val chunkedLanguages = LocalizationHelper.languages.chunked(2)
                            chunkedLanguages.forEach { rowLangs ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowLangs.forEach { (code, name) ->
                                        val isActive = lang == code
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(if (isActive) NeonCyan.copy(alpha = 0.15f) else CyberCharcoal)
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isActive) NeonCyan else Color(0x11FFFFFF),
                                                    shape = RoundedCornerShape(16.dp)
                                                )
                                                .clip(RoundedCornerShape(16.dp))
                                                .clickable {
                                                    viewModel.updateSystemLanguage(code)
                                                }
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = name,
                                                fontFamily = RajdhaniFontFamily,
                                                fontSize = 16.sp,
                                                color = if (isActive) NeonCyan else Color.White,
                                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
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

        // Section 2: 🔑 BIP39 KEYCHAIN & 8-DIGIT PIN SETUP
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCharcoal.copy(alpha = 0.8f)),
                border = BorderStroke(1.dp, if (profile?.isSecured == true) NeonCyan else Color(0x33FFFFFF)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = if (profile?.isSecured == true) Icons.Default.CheckCircle else Icons.Default.Shield,
                            contentDescription = "Shield",
                            tint = if (profile?.isSecured == true) NeonCyan else Color.LightGray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = t("bip39_header"),
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (profile?.isSecured == true) NeonCyan else Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (profile?.isSecured == true) {
                        // SECURED TERMINAL BADGE
                        Text(
                            text = "✔️ " + t("secured_badge") + " TERMINAL CONFIGURATION ACTIVE",
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = NeonCyan
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Alias: ${profile?.alias ?: "Netrunner"}",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            color = Color.LightGray
                        )
                        Text(
                            text = "Vault Protection: Keystore AES-256-GCM + PBKDF2-HMAC-SHA256 PIN",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 15.sp,
                            color = Color(0xFFB0B0C5)
                        )
                        Text(
                            text = "Device EC P-256 PubKey: ${viewModel.getDevicePublicKeyBase64().take(18)}...",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (showSeedPhraseReveal) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black)
                                    .border(1.dp, NeonCyan, RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        "RECOVERY MNEMONIC (24 WORDS) • SCREEN CAPTURE BLOCKED:",
                                        fontFamily = RajdhaniFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = NeonCyan
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        revealedSeedPhrase.ifBlank { "Decrypting from Keystore vault..." },
                                        fontFamily = RajdhaniFontFamily,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        CyberTextButton(
                                            onClick = {
                                                copySensitiveTextWithAutoClear(
                                                    context = context,
                                                    scope = coroutineScope,
                                                    label = "BIP39 Seed",
                                                    secretText = revealedSeedPhrase
                                                )
                                                viewModel.postNotification("📋 Seed copied to sensitive clipboard (auto-clears in 45s)")
                                            },
                                            text = t("btn_copy"),
                                            skin = currentSkin,
                                            containerColor = NeonCyan.copy(alpha = 0.2f),
                                            contentColor = NeonCyan,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.weight(1f)
                                        )
                                        CyberTextButton(
                                            onClick = { showSeedPhraseReveal = false },
                                            text = t("btn_hide_seed"),
                                            skin = currentSkin,
                                            containerColor = CyberCharcoal,
                                            contentColor = Color.White,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CyberTextButton(
                                    onClick = { showSeedRequestPassword = true },
                                    text = t("btn_view_seed"),
                                    skin = currentSkin,
                                    containerColor = NeonCyan.copy(alpha = 0.15f),
                                    contentColor = NeonCyan,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                )
                                CyberTextButton(
                                    onClick = { viewModel.resetSecurity() },
                                    text = "DEACTIVATE",
                                    skin = currentSkin,
                                    containerColor = NeonRed.copy(alpha = 0.15f),
                                    contentColor = NeonRed,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    } else {
                        // BIP39 REGISTRATION WIZARD
                        when (regStep) {
                            0 -> {
                                // Form inputs
                                Text(
                                    text = t("bip39_desc"),
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 16.sp,
                                    color = Color(0xFFB0B0C5)
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = firstNameInput,
                                    onValueChange = { firstNameInput = it },
                                    label = { Text("First Name", fontFamily = RajdhaniFontFamily) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NeonCyan,
                                        unfocusedBorderColor = Color.DarkGray,
                                        focusedLabelColor = NeonCyan,
                                        unfocusedLabelColor = Color.Gray,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    textStyle = TextStyle(fontFamily = RajdhaniFontFamily),
                                    modifier = Modifier.fillMaxWidth().testTag("first_name_input")
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = lastNameInput,
                                    onValueChange = { lastNameInput = it },
                                    label = { Text("Last Name", fontFamily = RajdhaniFontFamily) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NeonCyan,
                                        unfocusedBorderColor = Color.DarkGray,
                                        focusedLabelColor = NeonCyan,
                                        unfocusedLabelColor = Color.Gray,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    textStyle = TextStyle(fontFamily = RajdhaniFontFamily),
                                    modifier = Modifier.fillMaxWidth().testTag("last_name_input")
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = dobInput,
                                    onValueChange = { dobInput = it },
                                    label = { Text("Date of Birth (YYYY-MM-DD)", fontFamily = RajdhaniFontFamily) },
                                    placeholder = { Text("1998-05-15", color = Color.DarkGray) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NeonCyan,
                                        unfocusedBorderColor = Color.DarkGray,
                                        focusedLabelColor = NeonCyan,
                                        unfocusedLabelColor = Color.Gray,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    textStyle = TextStyle(fontFamily = RajdhaniFontFamily),
                                    modifier = Modifier.fillMaxWidth().testTag("dob_input")
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = if (lang == "ru") {
                                        "🛡️ ИСТОЧНИК ЭНТРОПИИ (5 слов, до 16 букв каждое):"
                                    } else {
                                        "🛡️ ENTROPY SOURCE (5 words, up to 16 letters each):"
                                    },
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )

                                listOf(
                                    Triple("Word 1 / Слово 1", entropyWord1, { v: String -> if (v.length <= 16 && v.all { it.isLetter() }) entropyWord1 = v }),
                                    Triple("Word 2 / Слово 2", entropyWord2, { v: String -> if (v.length <= 16 && v.all { it.isLetter() }) entropyWord2 = v }),
                                    Triple("Word 3 / Слово 3", entropyWord3, { v: String -> if (v.length <= 16 && v.all { it.isLetter() }) entropyWord3 = v }),
                                    Triple("Word 4 / Слово 4", entropyWord4, { v: String -> if (v.length <= 16 && v.all { it.isLetter() }) entropyWord4 = v }),
                                    Triple("Word 5 / Слово 5", entropyWord5, { v: String -> if (v.length <= 16 && v.all { it.isLetter() }) entropyWord5 = v })
                                ).forEachIndexed { idx, (label, value, onValueChange) ->
                                    OutlinedTextField(
                                        value = value,
                                        onValueChange = onValueChange,
                                        label = { Text(label, fontFamily = RajdhaniFontFamily, fontSize = 16.sp) },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = NeonCyan,
                                            unfocusedBorderColor = Color.DarkGray,
                                            focusedLabelColor = NeonCyan,
                                            unfocusedLabelColor = Color.Gray,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 16.sp),
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).testTag("entropy_word_${idx + 1}")
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                CyberButton(
                                    onClick = {
                                        viewModel.generateSecureSeed(
                                            birthdate = dobInput,
                                            firstName = firstNameInput,
                                            lastName = lastNameInput,
                                            entropyWords = listOf(entropyWord1, entropyWord2, entropyWord3, entropyWord4, entropyWord5)
                                        )
                                    },
                                    skin = currentSkin,
                                    containerColor = NeonCyan,
                                    contentColor = CyberObsidian,
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("generate_seed_button")
                                ) {
                                    AutoResizingText(
                                        text = t("btn_generate"),
                                        targetTextSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = RajdhaniFontFamily,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            1 -> {
                                // MNEMONIC SEED IMMEDIATE DISPLAY SCREEN (Copy first!)
                                Text(
                                    text = "🔑 " + t("seed_generated_title"),
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan,
                                    fontSize = 17.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = t("seed_warning"),
                                    fontFamily = RajdhaniFontFamily,
                                    color = NeonRed,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                // 24 Words Grid
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.5f))
                                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        for (row in 0 until 6) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                for (col in 0 until 4) {
                                                    val wordIdx = row * 4 + col
                                                    val word = generatedMnemonic?.getOrNull(wordIdx) ?: ""
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .background(CyberCharcoal)
                                                            .border(0.5.dp, Color(0x11FFFFFF), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 4.dp, vertical = 6.dp)
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(
                                                                "${wordIdx + 1}.",
                                                                fontFamily = RajdhaniFontFamily,
                                                                fontSize = 17.sp,
                                                                color = Color(0xFFB0B0C5),
                                                                modifier = Modifier.width(14.dp)
                                                            )
                                                            Text(
                                                                word,
                                                                fontFamily = RajdhaniFontFamily,
                                                                fontSize = 15.sp,
                                                                color = Color.White,
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

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    CyberTextButton(
                                        onClick = { viewModel.resetMnemonicForm() },
                                        text = t("btn_abort"),
                                        skin = currentSkin,
                                        containerColor = Color.Transparent,
                                        contentColor = Color.Gray,
                                        shape = RoundedCornerShape(12.dp),
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
                                            viewModel.postNotification("📋 Seed copied to sensitive clipboard (auto-clears in 45s)")
                                        },
                                        text = t("btn_copy"),
                                        skin = currentSkin,
                                        containerColor = NeonCyan.copy(alpha = 0.15f),
                                        contentColor = NeonCyan,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1.5f).testTag("copy_seed_button")
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                CyberTextButton(
                                    onClick = { viewModel.proceedToVerification() },
                                    text = t("btn_proceed"),
                                    skin = currentSkin,
                                    containerColor = NeonCyan,
                                    contentColor = CyberObsidian,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().height(44.dp).testTag("proceed_verification_button")
                                )
                            }
                            2 -> {
                                // MEMORY CHALLENGE SCREEN
                                Text(
                                    text = "🔒 " + t("challenge_title"),
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan,
                                    fontSize = 17.sp
                                )
                                Text(
                                    text = t("challenge_desc"),
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 15.sp,
                                    color = Color(0xFFB0B0C5),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                verificationIndices.forEach { targetIdx ->
                                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                        Text(
                                            "Select Word #${targetIdx + 1}:",
                                            fontFamily = RajdhaniFontFamily,
                                            fontSize = 16.sp,
                                            color = Color.LightGray
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))

                                        val options = verificationOptions[targetIdx] ?: emptyList()
                                        val currentSelection = verificationAnswers[targetIdx] ?: ""

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            options.forEach { optionWord ->
                                                val isSelected = currentSelection == optionWord
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .background(if (isSelected) NeonCyan.copy(alpha = 0.15f) else CyberCharcoal)
                                                        .border(
                                                            width = 1.dp,
                                                            color = if (isSelected) NeonCyan else Color.Transparent,
                                                            shape = RoundedCornerShape(16.dp)
                                                        )
                                                        .clip(RoundedCornerShape(16.dp))
                                                        .clickable {
                                                            viewModel.selectVerificationWord(targetIdx, optionWord)
                                                        }
                                                        .padding(vertical = 8.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = optionWord,
                                                        fontFamily = RajdhaniFontFamily,
                                                        fontSize = 15.sp,
                                                        color = if (isSelected) NeonCyan else Color.White,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CyberTextButton(
                                        onClick = { viewModel.setSecurityRegistrationStep(1) }, // Back to reveal
                                        text = "BACK TO SEED",
                                        skin = currentSkin,
                                        containerColor = Color.Transparent,
                                        contentColor = Color.Gray,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    )
                                    CyberTextButton(
                                        onClick = {
                                            viewModel.verifyAndSaveSecureSeed()
                                        },
                                        text = t("btn_verify"),
                                        skin = currentSkin,
                                        containerColor = NeonCyan,
                                        contentColor = CyberObsidian,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1.5f).testTag("verify_challenge_button")
                                    )
                                }
                            }
                            3 -> {
                                // SET 8-DIGIT SECURITY PIN SCREEN
                                Text(
                                    text = "🔢 " + t("pin_setup_title"),
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan,
                                    fontSize = 17.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = t("pin_setup_desc"),
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 16.sp,
                                    color = Color(0xFFB0B0C5)
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = pinCodeInput,
                                    onValueChange = {
                                        if (it.length <= 8 && it.all { char -> char.isDigit() }) {
                                            pinCodeInput = it
                                        }
                                    },
                                    placeholder = { Text("Enter 8 digits", color = Color.DarkGray) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NeonCyan,
                                        unfocusedBorderColor = Color.DarkGray,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    textStyle = TextStyle(
                                        fontFamily = RajdhaniFontFamily,
                                        fontSize = 18.sp,
                                        letterSpacing = 4.sp,
                                        textAlign = TextAlign.Center
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("pin_code_setup_input")
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    CyberTextButton(
                                        onClick = { viewModel.resetMnemonicForm() },
                                        text = "ABORT",
                                        skin = currentSkin,
                                        containerColor = Color.Transparent,
                                        contentColor = Color.Gray,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    )
                                    CyberTextButton(
                                        onClick = {
                                            viewModel.saveSecuredProfileWithPin(dobInput, firstNameInput, lastNameInput, pinCodeInput)
                                        },
                                        text = "ACTIVATE PIN ✔️",
                                        skin = currentSkin,
                                        containerColor = NeonCyan,
                                        contentColor = CyberObsidian,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(2f).testTag("save_pin_button")
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 3: 💾 DECENTRALIZED CRYPTO CLOUD (Container space allocation slider)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCharcoal.copy(alpha = 0.8f)),
                border = BorderStroke(1.dp, if (profile?.isCloudAllocated == true) NeonCyan else Color(0x33FFFFFF)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = "Cloud Node",
                            tint = if (profile?.isCloudAllocated == true) NeonCyan else Color.LightGray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = t("p2p_network_stats"),
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (profile?.isCloudAllocated == true) NeonCyan else Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = t("cloud_alloc_desc"),
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        color = Color(0xFFB0B0C5)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val freeDiskSpace = remember { viewModel.getFreeDiskSpaceGb() }
                    val maxCloudAlloc = remember { viewModel.getMaxCloudAllocationGb() }

                    Text(
                        text = "${t("free_space_label")} ${"%.2f".format(freeDiskSpace)} GB",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        color = Color.LightGray
                    )
                    Text(
                        text = "${t("cloud_max")} ${"%.2f".format(maxCloudAlloc)} GB",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (profile?.isCloudAllocated == true) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "LOCAL PEER STATUS:",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 16.sp,
                                color = Color(0xFFB0B0C5)
                            )
                            Box(
                                modifier = Modifier
                                    .background(Color(0x3339FF14), RoundedCornerShape(4.dp))
                                    .border(1.dp, Color(0xFF39FF14), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "ACTIVE ONLINE",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 14.sp,
                                    color = Color(0xFF39FF14),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val currentSpace = profile?.allocatedDiskSpaceGb ?: 2.0f
                        Text(
                            text = "${t("cloud_status_allocated")} ${"%.2f".format(currentSpace)} GB",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = currentSpace,
                            onValueChange = { newValue ->
                                viewModel.updateCloudAllocation(true, newValue)
                            },
                            valueRange = 2.0f..kotlin.math.max(2.0f, maxCloudAlloc),
                            colors = SliderDefaults.colors(
                                thumbColor = NeonCyan,
                                activeTrackColor = NeonCyan,
                                inactiveTrackColor = Color.DarkGray
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("disk_slider")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        CyberTextButton(
                            onClick = { viewModel.updateCloudAllocation(false, 0f) },
                            text = t("btn_deactivate") + " CLOUD NODE",
                            skin = currentSkin,
                            containerColor = NeonRed.copy(alpha = 0.15f),
                            contentColor = NeonRed,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color(0x11FFFFFF))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Active Sync redundant peer listings
                        Text(
                            "GLOBAL P2P SYNC PEERS [3/3 SYNCS]",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val syncPeers = listOf(
                            Triple("node-alpha-48fa.cyber", "99.8%", "ACTIVE COPY"),
                            Triple("node-gamma-cf10.cyber", "98.5%", "ACTIVE COPY"),
                            Triple("node-omega-d933.cyber", "99.9%", "ACTIVE COPY")
                        )
                        syncPeers.forEach { (peerName, uptime, status) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).background(NeonCyan, RoundedCornerShape(3.dp)))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(peerName, fontFamily = RajdhaniFontFamily, fontSize = 15.sp, color = Color.White)
                                }
                                Text("Uptime: $uptime | $status", fontFamily = RajdhaniFontFamily, fontSize = 14.sp, color = Color(0xFFB0B0C5))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "DECENTRALIZED NODE SYNC LOGS:",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .background(Color.Black)
                                .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(cloudLogs) { log ->
                                    Text(
                                        text = log,
                                        fontFamily = RajdhaniFontFamily,
                                        fontSize = 14.sp,
                                        color = if (log.contains("+")) Color(0xFF39FF14) else Color.LightGray
                                    )
                                }
                            }
                        }
                    } else {
                        // CLOUD OFFLINE DISK ALLOCATOR SETUP
                        Text(
                            text = t("cloud_status_unallocated"),
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            color = NeonRed,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        CyberTextButton(
                            onClick = {
                                val initAlloc = if (maxCloudAlloc > 2.0f) 2.0f else maxCloudAlloc
                                viewModel.updateCloudAllocation(true, initAlloc)
                            },
                            text = t("btn_allocate"),
                            skin = currentSkin,
                            containerColor = NeonCyan,
                            contentColor = CyberObsidian,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp).testTag("allocate_cloud_button")
                        )
                    }
                }
            }
        }

        // Section 4: 🛡️ NET GATEWAYS (V2RAY / TOR / VPN Routing Settings)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCharcoal.copy(alpha = 0.8f)),
                border = BorderStroke(1.dp, if (isProxyActive) NeonCyan else Color(0x33FFFFFF)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Proxy Routing",
                            tint = if (isProxyActive) NeonCyan else Color.LightGray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = t("proxy_header"),
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isProxyActive) NeonCyan else Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = t("proxy_desc"),
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 14.sp,
                        color = Color(0xFFB0B0C5)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = t("proxy_type_label") + " $proxyType",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 14.sp,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("NONE", "TOR", "V2RAY").forEach { proto ->
                                val isSelected = proxyType == proto
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (isSelected) NeonCyan.copy(alpha = 0.15f) else CyberCharcoal, RoundedCornerShape(8.dp))
                                        .border(
                                            width = 1.5.dp,
                                            color = if (isSelected) NeonCyan else Color(0x33FFFFFF),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { proxyType = proto }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = proto,
                                        fontFamily = RajdhaniFontFamily,
                                        fontSize = 14.sp,
                                        color = if (isSelected) NeonCyan else Color.White,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("SOCKS5", "HTTP").forEach { proto ->
                                val isSelected = proxyType == proto
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (isSelected) NeonCyan.copy(alpha = 0.15f) else CyberCharcoal, RoundedCornerShape(8.dp))
                                        .border(
                                            width = 1.5.dp,
                                            color = if (isSelected) NeonCyan else Color(0x33FFFFFF),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { proxyType = proto }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = proto,
                                        fontFamily = RajdhaniFontFamily,
                                        fontSize = 14.sp,
                                        color = if (isSelected) NeonCyan else Color.White,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    if (proxyType != "NONE") {
                        Spacer(modifier = Modifier.height(16.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = proxyHost,
                                onValueChange = { proxyHost = it },
                                label = { Text("Proxy Host", fontFamily = RajdhaniFontFamily, fontSize = 14.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = Color.DarkGray,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 15.sp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = proxyPortString,
                                onValueChange = { proxyPortString = it },
                                label = { Text("Port", fontFamily = RajdhaniFontFamily, fontSize = 14.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = Color.DarkGray,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 15.sp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = proxyConfig,
                            onValueChange = { proxyConfig = it },
                            label = { Text("Routing Config / Credentials", fontFamily = RajdhaniFontFamily, fontSize = 14.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = Color.DarkGray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 15.sp),
                            modifier = Modifier.fillMaxWidth().height(100.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Connection tester results
                        proxyTestStatus?.let { status ->
                            Text(
                                text = status,
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 14.sp,
                                color = if (status.contains("Success")) Color(0xFF39FF14) else NeonRed,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CyberTextButton(
                                onClick = {
                                    isTestingProxy = true
                                    proxyTestStatus = "📡 Testing TCP Handshake..."
                                    val portVal = proxyPortString.toIntOrNull() ?: 9050
                                    viewModel.testProxyConnection(proxyType, proxyHost, portVal) { success, msg ->
                                        isTestingProxy = false
                                        proxyTestStatus = msg
                                    }
                                },
                                text = if (isTestingProxy) "CONNECTING..." else t("btn_test_gateway"),
                                skin = currentSkin,
                                enabled = !isTestingProxy,
                                containerColor = CyberCharcoal,
                                contentColor = NeonCyan,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1.2f)
                            )

                            val portVal = proxyPortString.toIntOrNull() ?: 9050
                            CyberTextButton(
                                onClick = {
                                    viewModel.updateProxySettings(proxyType, proxyHost, portVal, proxyConfig, !isProxyActive)
                                },
                                text = if (isProxyActive) t("btn_deactivate") else "ACTIVATE ✔️",
                                skin = currentSkin,
                                containerColor = if (isProxyActive) NeonRed.copy(alpha = 0.2f) else NeonCyan,
                                contentColor = if (isProxyActive) NeonRed else CyberObsidian,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Section 4: ☁️ GOOGLE CLOUD IDENTITY & FIRESTORE BACKUP (H7)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCharcoal.copy(alpha = 0.8f)),
                border = BorderStroke(
                    1.dp,
                    if (cloudUserEmail != null) NeonGreen else Color(0x33FFFFFF)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = "Cloud Sync",
                            tint = if (cloudUserEmail != null) NeonGreen else NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (lang == "ru") "ОБЛАЧНАЯ СИНХРОНИЗАЦИЯ FIRESTORE" else "CLOUD IDENTITY & FIRESTORE SYNC",
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (cloudUserEmail != null) {
                            "${cloudUserName ?: "Verified User"} ($cloudUserEmail) • Cloud Readings: $cloudReadingsCount"
                        } else {
                            if (lang == "ru") {
                                "Опциональный вход через Google для резервного копирования раскладов и метаданных профиля в Firestore."
                            } else {
                                "Optional Google Sign-In to back up tarot readings and non-sensitive profile metadata to Firestore."
                            }
                        },
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 15.sp,
                        color = if (cloudUserEmail != null) NeonGreen else Color(0xFFB0B0C5)
                    )

                    cloudSyncStatus?.let { status ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = status,
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 14.sp,
                            color = NeonCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (cloudUserEmail == null) {
                        CyberTextButton(
                            onClick = { viewModel.signInWithGoogle(context) },
                            text = if (isCloudSigningIn) {
                                if (lang == "ru") "АВТОРИЗАЦИЯ..." else "SIGNING IN..."
                            } else {
                                if (lang == "ru") "ВОЙТИ ЧЕРЕЗ GOOGLE" else "SIGN IN WITH GOOGLE"
                            },
                            skin = currentSkin,
                            enabled = !isCloudSigningIn,
                            containerColor = NeonCyan,
                            contentColor = CyberObsidian,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("google_cloud_signin_button")
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CyberTextButton(
                                onClick = { viewModel.syncProfileToCloud() },
                                text = if (lang == "ru") "СИНХР. ПРОФИЛЬ" else "SYNC PROFILE",
                                skin = currentSkin,
                                containerColor = NeonCyan,
                                contentColor = CyberObsidian,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            CyberTextButton(
                                onClick = { viewModel.syncLatestReadingToCloud() },
                                text = if (lang == "ru") "БЭКАП РАСКЛАДА" else "SYNC READING",
                                skin = currentSkin,
                                containerColor = CyberObsidian,
                                contentColor = NeonGreen,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        CyberTextButton(
                            onClick = { viewModel.signOutFromCloud(context) },
                            text = if (lang == "ru") "ВЫЙТИ ИЗ АККАУНТА" else "SIGN OUT",
                            skin = currentSkin,
                            containerColor = NeonRed.copy(alpha = 0.2f),
                            contentColor = NeonRed,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    // Modal Password reveal dialog
    if (showSeedRequestPassword) {
        AlertDialog(
            onDismissRequest = { showSeedRequestPassword = false },
            title = {
                Text(
                    text = "AUTHORIZE TERMINAL DECRYPTION",
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    fontSize = 14.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = t("input_password_reveal"),
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = seedRequestPasswordInput,
                        onValueChange = { seedRequestPasswordInput = it },
                        label = { Text("Password", fontFamily = RajdhaniFontFamily) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color.DarkGray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        textStyle = TextStyle(fontFamily = RajdhaniFontFamily),
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
                                viewModel.postNotification("❌ Incorrect security PIN. Access denied.")
                            }
                        }
                    }
                ) {
                    Text("AUTHORIZE", fontFamily = RajdhaniFontFamily, color = NeonCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSeedRequestPassword = false }) {
                    Text("CANCEL", fontFamily = RajdhaniFontFamily, color = Color(0xFFB0B0C5))
                }
            },
            containerColor = CyberCharcoal,
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }
}
