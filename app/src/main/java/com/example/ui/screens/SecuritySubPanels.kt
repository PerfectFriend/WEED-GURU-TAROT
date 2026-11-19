package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.R
import com.example.data.LocalizationHelper
import com.example.data.UserProfile
import com.example.ui.components.CyberActionButton
import com.example.ui.components.CyberChip
import com.example.ui.components.CyberNeonButton
import com.example.ui.components.CyberPanel
import com.example.ui.theme.Cyber
import com.example.ui.theme.CyberShapes
import com.example.ui.theme.CyberSkin
import com.example.ui.theme.ElegantViolet
import com.example.ui.theme.IbmPlexMonoFontFamily
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.Space
import com.example.ui.theme.getLocalizedDescription
import com.example.ui.theme.getLocalizedName
import com.example.viewmodel.TarotViewModel
import java.util.Locale
import androidx.compose.foundation.layout.aspectRatio

@Composable
internal fun SecuritySkinsCard(
    currentSkin: CyberSkin,
    lang: String,
    onSelectSkin: (CyberSkin) -> Unit
) {
    val c = Cyber.colors
    CyberPanel(
        borderColor = currentSkin.primaryColor.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth()
    ) {
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
            Spacer(modifier = Modifier.width(Space.sm))
            Text(
                text = "NEURAL COSMIC SKINS",
                style = MaterialTheme.typography.titleMedium,
                color = c.textHigh,
                maxLines = 1
            )
        }
        Text(
            text = "Calibrate the visual wavelength of your oracle interface. Wallpapers dynamically bleed through system cards.",
            style = MaterialTheme.typography.bodySmall,
            color = c.textMid,
            modifier = Modifier.padding(top = Space.xs, bottom = Space.md)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Space.sm),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(CyberSkin.values()) { skin ->
                val isSelected = currentSkin == skin
                Card(
                    modifier = Modifier
                        .width(138.dp)
                        .clickable { onSelectSkin(skin) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) skin.primaryColor.copy(alpha = 0.16f) else c.panelRaised
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) skin.primaryColor else c.goldAntique.copy(alpha = 0.3f)
                    ),
                    shape = CyberShapes.cardCompact
                ) {
                    Column(
                        modifier = Modifier.padding(Space.sm),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .clip(CyberShapes.chip)
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
                                    .background(c.ink.copy(alpha = 0.35f))
                            )
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .background(skin.primaryColor, CircleShape)
                                        .align(Alignment.Center),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = c.ink,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(Space.xs))
                        Text(
                            text = skin.getLocalizedName(lang),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) skin.primaryColor else c.textHigh,
                            maxLines = 1
                        )
                        Text(
                            text = skin.id.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = c.textMid,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Space.md))
        Text(
            text = (if (lang == "ru") "Активная палитра: " else if (lang == "es") "Paleta activa: " else "Active Palette: ") + currentSkin.getLocalizedName(lang),
            style = MaterialTheme.typography.titleSmall,
            color = currentSkin.primaryColor
        )
        Text(
            text = currentSkin.getLocalizedDescription(lang),
            style = MaterialTheme.typography.bodySmall,
            color = c.textMid
        )
    }
}

@Composable
internal fun SecurityIdentityCard(
    viewModel: TarotViewModel,
    context: Context,
    profile: UserProfile?,
    currentSkin: CyberSkin,
    deckRating: Int,
    deckApproxCost: Int
) {
    val c = Cyber.colors
    var aliasText by remember { mutableStateOf(profile?.alias ?: "Matrix_Walker") }
    var bioText by remember { mutableStateOf(profile?.bio ?: "Core system Netrunner navigating the decentralized neural web.") }
    var selectedAvatar by remember { mutableStateOf(profile?.avatarIndex ?: 0) }
    var isEditing by remember { mutableStateOf(false) }

    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.saveDeviceAvatarImage(context, uri)
            }
        }
    )

    var showAiGeneratorDialog by remember { mutableStateOf(false) }

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
        Triple("Cyber Seeker", c.textHigh, Icons.Default.Info),
        Triple("Core Netrunner", ElegantViolet, Icons.Default.Shield)
    )

    if (showAiGeneratorDialog) {
        var selectedStyle by remember { mutableStateOf("Matrix Rain") }
        val stylesList = listOf(
            "Matrix Rain" to "Green cascading binary lines, cyber circuitry symbols.",
            "Neon Synthwave" to "Orange/magenta sun, retro perspective horizon grid.",
            "Neural Synapses" to "Cyan interconnected nodes, synapses & network hexagons.",
            "Quantum Singularity" to "Deep violet gravitational orbits, gold event horizon.",
            "Arcade Vector" to "Techno-emblem, high-contrast symmetrical geometric vectors."
        )

        AlertDialog(
            onDismissRequest = { showAiGeneratorDialog = false },
            containerColor = c.panel,
            title = {
                Text(
                    "NEURAL AVATAR COMPILER v2.4",
                    style = MaterialTheme.typography.titleMedium,
                    color = c.accent
                )
            },
            text = {
                Column {
                    Text(
                        "Select visual design style for neural synthesis of $aliasText:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.textMid,
                        modifier = Modifier.padding(bottom = Space.md)
                    )
                    stylesList.forEach { (styleName, desc) ->
                        val isStyleSelected = selectedStyle == styleName
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Space.xs)
                                .clickable { selectedStyle = styleName },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isStyleSelected) c.accent.copy(alpha = 0.16f) else c.panelRaised
                            ),
                            border = BorderStroke(1.dp, if (isStyleSelected) c.accent else c.goldAntique.copy(alpha = 0.25f)),
                            shape = CyberShapes.chip
                        ) {
                            Column(modifier = Modifier.padding(Space.md)) {
                                Text(
                                    styleName.uppercase(),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (isStyleSelected) c.accent else c.textHigh
                                )
                                Text(
                                    desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = c.textMid,
                                    modifier = Modifier.padding(top = Space.xxs)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                CyberActionButton(
                    text = "COMPILE",
                    onClick = {
                        showAiGeneratorDialog = false
                        viewModel.compileAndSaveCustomAvatar(context, aliasText, selectedStyle)
                    }
                )
            },
            dismissButton = {
                TextButton(
                    onClick = { showAiGeneratorDialog = false },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("CANCEL", style = MaterialTheme.typography.labelLarge, color = c.textMid)
                }
            }
        )
    }

    CyberPanel(
        borderColor = c.accent.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "User Identity",
                    tint = c.accent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(Space.sm))
                Text(
                    text = "USER NEURAL IDENTITY",
                    style = MaterialTheme.typography.titleMedium,
                    color = c.textHigh
                )
            }

            IconButton(
                onClick = {
                    if (isEditing) {
                        viewModel.updateUserProfileSetup(aliasText, selectedAvatar, bioText, profile?.customAvatarUri)
                    }
                    isEditing = !isEditing
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                    contentDescription = "Edit Profile",
                    tint = c.accent
                )
            }
        }

        Spacer(modifier = Modifier.height(Space.md))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (profile?.customAvatarUri != null) {
                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(context)
                        .data(profile.customAvatarUri)
                        .crossfade(true)
                        .build()
                )
                Image(
                    painter = painter,
                    contentDescription = "Custom Profile Avatar",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .border(2.dp, c.accent, CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                val avatarInfo = avatarList.getOrElse(selectedAvatar) { avatarList[0] }
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(avatarInfo.second.copy(alpha = 0.16f))
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

            Spacer(modifier = Modifier.width(Space.lg))

            Column(modifier = Modifier.weight(1f)) {
                if (isEditing) {
                    OutlinedTextField(
                        value = aliasText,
                        onValueChange = { aliasText = it },
                        label = { Text("Neural Alias") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = c.textHigh,
                            unfocusedTextColor = c.textHigh,
                            focusedBorderColor = c.accent,
                            unfocusedBorderColor = c.goldAntique.copy(alpha = 0.35f)
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    val activeAvatar = avatarList.getOrElse(selectedAvatar) { avatarList[0] }
                    Text(
                        text = aliasText.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (profile?.customAvatarUri != null) c.accent else activeAvatar.second
                    )
                    Text(
                        text = if (profile?.customAvatarUri != null) "Matrix Class: Custom AI Avatar" else "Matrix Class: ${activeAvatar.first}",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textMid,
                        modifier = Modifier.padding(top = Space.xxs)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Space.md))

        if (isEditing) {
            OutlinedTextField(
                value = bioText,
                onValueChange = { bioText = it },
                label = { Text("Short Bio / Userinfo Matrix") },
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = c.textHigh,
                    unfocusedTextColor = c.textHigh,
                    focusedBorderColor = c.accent,
                    unfocusedBorderColor = c.goldAntique.copy(alpha = 0.35f)
                ),
                textStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = bioText,
                style = MaterialTheme.typography.bodyMedium,
                color = c.textHigh,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(c.ink, CyberShapes.chip)
                    .padding(Space.md)
            )
        }

        if (isEditing) {
            Spacer(modifier = Modifier.height(Space.md))
            Text(
                text = "CUSTOM IDENTITY AVATAR ENGINE",
                style = MaterialTheme.typography.labelLarge,
                color = c.textMid,
                modifier = Modifier.padding(bottom = Space.xs)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Space.sm)
            ) {
                CyberActionButton(
                    text = "DEVICE FILE",
                    onClick = {
                        singlePhotoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
                CyberActionButton(
                    text = "AI GENERATE",
                    onClick = { showAiGeneratorDialog = true },
                    secondary = true,
                    modifier = Modifier.weight(1f)
                )
            }

            if (profile?.customAvatarUri != null) {
                Spacer(modifier = Modifier.height(Space.sm))
                CyberActionButton(
                    text = "RESTORE STANDARD PRESETS",
                    onClick = { viewModel.clearCustomAvatar() },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Spacer(modifier = Modifier.height(Space.md))
                Text(
                    text = "CHOOSE AVATAR PRESET",
                    style = MaterialTheme.typography.labelLarge,
                    color = c.textMid,
                    modifier = Modifier.padding(bottom = Space.xs)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    avatarList.forEachIndexed { index, triple ->
                        val isSelected = selectedAvatar == index
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) triple.second.copy(alpha = 0.25f) else Color.Transparent)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) triple.second else c.goldAntique.copy(alpha = 0.35f),
                                    shape = CircleShape
                                )
                                .clickable { selectedAvatar = index },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = triple.third,
                                contentDescription = triple.first,
                                tint = if (isSelected) triple.second else c.textMid,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Space.md))
        HorizontalDivider(color = c.goldAntique.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(Space.md))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Space.sm)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(c.ink, CyberShapes.chip)
                    .border(1.dp, c.goldLeaf.copy(alpha = 0.25f), CyberShapes.chip)
                    .padding(Space.md),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "GLOBAL DECK RATING",
                    style = MaterialTheme.typography.labelMedium,
                    color = c.goldLeaf
                )
                Spacer(modifier = Modifier.height(Space.xs))
                Text(
                    text = "★ $deckRating",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = IbmPlexMonoFontFamily,
                    color = c.goldLeaf
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(c.ink, CyberShapes.chip)
                    .border(1.dp, c.accent.copy(alpha = 0.25f), CyberShapes.chip)
                    .padding(Space.md),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "APPROX DECK VALUE",
                    style = MaterialTheme.typography.labelMedium,
                    color = c.accent
                )
                Spacer(modifier = Modifier.height(Space.xs))
                Text(
                    text = "$deckApproxCost N$",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = IbmPlexMonoFontFamily,
                    color = c.accent
                )
            }
        }
    }
}

@Composable
internal fun SecurityLanguageCard(
    lang: String,
    headerText: String,
    descText: String,
    onSelectLanguage: (String) -> Unit
) {
    val c = Cyber.colors
    CyberPanel(
        borderColor = c.goldAntique.copy(alpha = 0.35f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = "Language Selection",
                tint = c.accent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(Space.sm))
            Text(
                text = headerText,
                style = MaterialTheme.typography.titleMedium,
                color = c.textHigh
            )
        }
        Spacer(modifier = Modifier.height(Space.xs))
        Text(
            text = descText,
            style = MaterialTheme.typography.bodySmall,
            color = c.textMid
        )
        Spacer(modifier = Modifier.height(Space.md))

        LocalizationHelper.languages.chunked(2).forEach { rowLangs ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Space.xs),
                horizontalArrangement = Arrangement.spacedBy(Space.sm)
            ) {
                rowLangs.forEach { (code, name) ->
                    CyberChip(
                        label = name,
                        selected = lang == code,
                        onClick = { onSelectLanguage(code) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
internal fun SecurityWatermarkCard(
    viewModel: TarotViewModel,
    lang: String,
    isWatermarkUnlocked: Boolean,
    isWatermarkHidden: Boolean,
    balanceUsdc: Double,
    balanceWeed: Int
) {
    val c = Cyber.colors
    CyberPanel(
        borderColor = NeonGold.copy(alpha = if (isWatermarkUnlocked) 0.78f else 0.45f),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("security_watermark_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Watermark Removal Option",
                    tint = NeonGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(Space.sm))
                Text(
                    text = if (lang == "ru") "ВОТЕРМАРКА WEED GURU PROPERTY" else "WEED GURU PROPERTY WATERMARK",
                    style = MaterialTheme.typography.titleMedium,
                    color = NeonGold
                )
            }
            Text(
                text = if (isWatermarkUnlocked) "UNLOCKED" else "$5.99",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = IbmPlexMonoFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = if (isWatermarkUnlocked) NeonGreen else NeonGold
            )
        }

        Spacer(modifier = Modifier.height(Space.xs))
        Text(
            text = if (lang == "ru") {
                "По умолчанию в полноэкранном просмотре отображается вотермарка WEED GURU PROPERTY с листьями конопли по краям. Для её отключения требуется коллекционная золотая карта GENESIS WATERMARK ELIMINATOR (экв. \$5.99)."
            } else {
                "Fullscreen card view displays the edge-leaf WEED GURU PROPERTY watermark by default. Acquire GENESIS WATERMARK ELIMINATOR (\$5.99 eq.) to toggle it ON/OFF."
            },
            style = MaterialTheme.typography.bodySmall,
            color = c.textMid
        )

        Spacer(modifier = Modifier.height(Space.md))

        if (isWatermarkUnlocked) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(c.ink, CyberShapes.control)
                    .border(1.dp, NeonGold.copy(alpha = 0.45f), CyberShapes.control)
                    .padding(horizontal = Space.md, vertical = Space.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (lang == "ru") "Убирать вотермарку в полноэкранном режиме" else "Remove fullscreen watermark",
                        style = MaterialTheme.typography.labelLarge,
                        color = c.textHigh
                    )
                    Text(
                        text = if (isWatermarkHidden) {
                            if (lang == "ru") "Чистый холст активен" else "Clean canvas active"
                        } else {
                            if (lang == "ru") "Вотермарка отображается" else "Watermark visible"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isWatermarkHidden) NeonGreen else c.textMid
                    )
                }
                Switch(
                    checked = isWatermarkHidden,
                    onCheckedChange = { viewModel.toggleFullscreenWatermark(it) },
                    modifier = Modifier.testTag("security_watermark_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = NeonGold,
                        uncheckedThumbColor = NeonGold,
                        uncheckedTrackColor = c.panelRaised
                    )
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Space.sm)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.card_genesis_eliminator_1795033578202),
                    contentDescription = "Genesis Watermark Eliminator",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(0.75f)
                        .clip(CyberShapes.cardCompact)
                        .border(1.dp, NeonGold, CyberShapes.cardCompact)
                )
                Image(
                    painter = painterResource(id = R.drawable.back_genesis_elements_1795033577974),
                    contentDescription = "Genesis Emergence of Life Card Back",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(0.75f)
                        .clip(CyberShapes.cardCompact)
                        .border(1.dp, NeonGold, CyberShapes.cardCompact)
                )
            }
            Spacer(modifier = Modifier.height(Space.sm))
            Text(
                text = String.format(Locale.US, "Balance: %.2f USDC • %d WEED", balanceUsdc, balanceWeed),
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = IbmPlexMonoFontFamily),
                color = c.textMid
            )
            Spacer(modifier = Modifier.height(Space.sm))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Space.sm)
            ) {
                CyberNeonButton(
                    text = "5.99 USDC ($5.99)",
                    onClick = { viewModel.purchaseGenesisWatermarkEliminator(payWithUsdc = true) },
                    color = NeonGold,
                    modifier = Modifier.weight(1f)
                )
                CyberNeonButton(
                    text = "60 WEED",
                    onClick = { viewModel.purchaseGenesisWatermarkEliminator(payWithUsdc = false) },
                    color = NeonGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
internal fun SecurityCloudAllocationCard(
    viewModel: TarotViewModel,
    profile: UserProfile?,
    currentSkin: CyberSkin,
    cloudLogs: List<String>,
    t: (String) -> String
) {
    val c = Cyber.colors
    val isAllocated = profile?.isCloudAllocated == true
    CyberPanel(
        borderColor = if (isAllocated) c.accent else c.goldAntique.copy(alpha = 0.35f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.CloudQueue,
                contentDescription = "Cloud Node",
                tint = if (isAllocated) c.accent else c.textMid,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(Space.sm))
            Text(
                text = t("p2p_network_stats"),
                style = MaterialTheme.typography.titleMedium,
                color = if (isAllocated) c.accent else c.textHigh
            )
        }

        Spacer(modifier = Modifier.height(Space.xs))
        Text(
            text = t("cloud_alloc_desc"),
            style = MaterialTheme.typography.bodySmall,
            color = c.textMid
        )
        Spacer(modifier = Modifier.height(Space.md))

        val freeDiskSpace = remember { viewModel.getFreeDiskSpaceGb() }
        val maxCloudAlloc = remember { viewModel.getMaxCloudAllocationGb() }

        Text(
            text = "${t("free_space_label")} ${String.format(Locale.US, "%.2f", freeDiskSpace)} GB",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = IbmPlexMonoFontFamily,
            color = c.textHigh
        )
        Text(
            text = "${t("cloud_max")} ${String.format(Locale.US, "%.2f", maxCloudAlloc)} GB",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = IbmPlexMonoFontFamily,
            color = c.textMid
        )

        Spacer(modifier = Modifier.height(Space.md))

        if (isAllocated) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "LOCAL PEER STATUS:",
                    style = MaterialTheme.typography.labelLarge,
                    color = c.textMid
                )
                Box(
                    modifier = Modifier
                        .background(c.emeraldGlow.copy(alpha = 0.16f), CyberShapes.chip)
                        .border(1.dp, c.emeraldGlow, CyberShapes.chip)
                        .padding(horizontal = Space.sm, vertical = Space.xs)
                ) {
                    Text(
                        "ACTIVE ONLINE",
                        style = MaterialTheme.typography.labelMedium,
                        color = c.emeraldGlow
                    )
                }
            }

            Spacer(modifier = Modifier.height(Space.md))

            val currentSpace = profile?.allocatedDiskSpaceGb ?: 2.0f
            Text(
                text = "${t("cloud_status_allocated")} ${String.format(Locale.US, "%.2f", currentSpace)} GB",
                style = MaterialTheme.typography.titleSmall,
                fontFamily = IbmPlexMonoFontFamily,
                color = c.accent
            )

            Spacer(modifier = Modifier.height(Space.xs))
            Slider(
                value = currentSpace,
                onValueChange = { newValue ->
                    viewModel.updateCloudAllocation(true, newValue)
                },
                valueRange = 2.0f..kotlin.math.max(2.0f, maxCloudAlloc),
                colors = SliderDefaults.colors(
                    thumbColor = c.accent,
                    activeTrackColor = c.accent,
                    inactiveTrackColor = c.panelRaised
                ),
                modifier = Modifier.fillMaxWidth().testTag("disk_slider")
            )

            Spacer(modifier = Modifier.height(Space.md))

            CyberTextButton(
                onClick = { viewModel.updateCloudAllocation(false, 0f) },
                text = t("btn_deactivate") + " CLOUD NODE",
                skin = currentSkin,
                containerColor = c.danger.copy(alpha = 0.16f),
                contentColor = c.danger,
                shape = CyberShapes.control,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Space.md))
            HorizontalDivider(color = c.goldAntique.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(Space.sm))

            Text(
                "GLOBAL P2P SYNC PEERS [3/3 SYNCS]",
                style = MaterialTheme.typography.labelLarge,
                color = c.textHigh
            )
            Spacer(modifier = Modifier.height(Space.xs))
            val syncPeers = listOf(
                Triple("node-alpha-48fa.cyber", "99.8%", "ACTIVE COPY"),
                Triple("node-gamma-cf10.cyber", "98.5%", "ACTIVE COPY"),
                Triple("node-omega-d933.cyber", "99.9%", "ACTIVE COPY")
            )
            syncPeers.forEach { (peerName, uptime, status) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = Space.xxs),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).background(c.accent, CyberShapes.chip))
                        Spacer(modifier = Modifier.width(Space.xs))
                        Text(peerName, style = MaterialTheme.typography.bodySmall, fontFamily = IbmPlexMonoFontFamily, color = c.textHigh)
                    }
                    Text("Uptime: $uptime | $status", style = MaterialTheme.typography.labelSmall, color = c.textMid)
                }
            }

            Spacer(modifier = Modifier.height(Space.md))
            Text(
                "DECENTRALIZED NODE SYNC LOGS:",
                style = MaterialTheme.typography.labelLarge,
                color = c.accent
            )
            Spacer(modifier = Modifier.height(Space.xs))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(c.ink, CyberShapes.chip)
                    .border(1.dp, c.goldAntique.copy(alpha = 0.25f), CyberShapes.chip)
                    .padding(Space.sm)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(Space.xs)
                ) {
                    items(cloudLogs) { log ->
                        Text(
                            text = log,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (log.contains("+")) c.emeraldGlow else c.textMid
                        )
                    }
                }
            }
        } else {
            Text(
                text = t("cloud_status_unallocated"),
                style = MaterialTheme.typography.titleSmall,
                color = c.danger
            )
            Spacer(modifier = Modifier.height(Space.md))

            CyberTextButton(
                onClick = {
                    val initAlloc = if (maxCloudAlloc > 2.0f) 2.0f else maxCloudAlloc
                    viewModel.updateCloudAllocation(true, initAlloc)
                },
                text = t("btn_allocate"),
                skin = currentSkin,
                containerColor = c.accent,
                contentColor = c.ink,
                shape = CyberShapes.control,
                modifier = Modifier.fillMaxWidth().testTag("allocate_cloud_button")
            )
        }
    }
}

@Composable
internal fun SecurityProxyAndCloudSyncCards(
    viewModel: TarotViewModel,
    context: Context,
    lang: String,
    currentSkin: CyberSkin,
    proxyType: String,
    onProxyTypeChange: (String) -> Unit,
    proxyHost: String,
    onProxyHostChange: (String) -> Unit,
    proxyPortString: String,
    onProxyPortChange: (String) -> Unit,
    proxyConfig: String,
    onProxyConfigChange: (String) -> Unit,
    isProxyActive: Boolean,
    cloudUserEmail: String?,
    cloudUserName: String?,
    isCloudSigningIn: Boolean,
    cloudReadingsCount: Int,
    cloudSyncStatus: String?,
    t: (String) -> String
) {
    val c = Cyber.colors
    var proxyTestStatus by remember { mutableStateOf<String?>(null) }
    var isTestingProxy by remember { mutableStateOf(false) }

    CyberPanel(
        borderColor = if (isProxyActive) c.accent else c.goldAntique.copy(alpha = 0.35f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Proxy Routing",
                tint = if (isProxyActive) c.accent else c.textMid,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(Space.sm))
            Text(
                text = t("proxy_header"),
                style = MaterialTheme.typography.titleMedium,
                color = if (isProxyActive) c.accent else c.textHigh
            )
        }

        Spacer(modifier = Modifier.height(Space.xs))
        Text(
            text = t("proxy_desc"),
            style = MaterialTheme.typography.bodySmall,
            color = c.textMid
        )
        Spacer(modifier = Modifier.height(Space.md))

        Text(
            text = t("proxy_type_label") + " $proxyType",
            style = MaterialTheme.typography.labelLarge,
            color = c.textHigh
        )
        Spacer(modifier = Modifier.height(Space.sm))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Space.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Space.sm)
            ) {
                listOf("NONE", "TOR", "V2RAY").forEach { proto ->
                    CyberChip(
                        label = proto,
                        selected = proxyType == proto,
                        onClick = { onProxyTypeChange(proto) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Space.sm)
            ) {
                listOf("SOCKS5", "HTTP").forEach { proto ->
                    CyberChip(
                        label = proto,
                        selected = proxyType == proto,
                        onClick = { onProxyTypeChange(proto) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (proxyType != "NONE") {
            Spacer(modifier = Modifier.height(Space.md))

            OutlinedTextField(
                value = proxyHost,
                onValueChange = onProxyHostChange,
                label = { Text("Proxy Host") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = c.accent,
                    unfocusedBorderColor = c.goldAntique.copy(alpha = 0.35f),
                    focusedTextColor = c.textHigh,
                    unfocusedTextColor = c.textHigh
                ),
                textStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(Space.sm))
            OutlinedTextField(
                value = proxyPortString,
                onValueChange = onProxyPortChange,
                label = { Text("Port") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = c.accent,
                    unfocusedBorderColor = c.goldAntique.copy(alpha = 0.35f),
                    focusedTextColor = c.textHigh,
                    unfocusedTextColor = c.textHigh
                ),
                textStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(Space.sm))
            OutlinedTextField(
                value = proxyConfig,
                onValueChange = onProxyConfigChange,
                label = { Text("Routing Config / Credentials") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = c.accent,
                    unfocusedBorderColor = c.goldAntique.copy(alpha = 0.35f),
                    focusedTextColor = c.textHigh,
                    unfocusedTextColor = c.textHigh
                ),
                textStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth().height(100.dp)
            )

            Spacer(modifier = Modifier.height(Space.md))

            proxyTestStatus?.let { status ->
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (status.contains("Success")) c.emeraldGlow else c.danger,
                    modifier = Modifier.padding(bottom = Space.sm)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Space.sm)
            ) {
                CyberTextButton(
                    onClick = {
                        isTestingProxy = true
                        proxyTestStatus = "Testing TCP Handshake..."
                        val portVal = proxyPortString.toIntOrNull() ?: 9050
                        viewModel.testProxyConnection(proxyType, proxyHost, portVal) { _, msg ->
                            isTestingProxy = false
                            proxyTestStatus = msg
                        }
                    },
                    text = if (isTestingProxy) "CONNECTING..." else t("btn_test_gateway"),
                    skin = currentSkin,
                    enabled = !isTestingProxy,
                    containerColor = c.panelRaised,
                    contentColor = c.accent,
                    shape = CyberShapes.control,
                    modifier = Modifier.weight(1.2f)
                )

                val portVal = proxyPortString.toIntOrNull() ?: 9050
                CyberTextButton(
                    onClick = {
                        viewModel.updateProxySettings(proxyType, proxyHost, portVal, proxyConfig, !isProxyActive)
                    },
                    text = if (isProxyActive) t("btn_deactivate") else "ACTIVATE",
                    skin = currentSkin,
                    containerColor = if (isProxyActive) c.danger.copy(alpha = 0.2f) else c.accent,
                    contentColor = if (isProxyActive) c.danger else c.ink,
                    shape = CyberShapes.control,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(Space.lg))

    CyberPanel(
        borderColor = if (cloudUserEmail != null) c.emeraldGlow else c.goldAntique.copy(alpha = 0.35f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.CloudSync,
                contentDescription = "Cloud Sync",
                tint = if (cloudUserEmail != null) c.emeraldGlow else c.accent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(Space.sm))
            Text(
                text = if (lang == "ru") "ОБЛАЧНАЯ СИНХРОНИЗАЦИЯ FIRESTORE" else "CLOUD IDENTITY & FIRESTORE SYNC",
                style = MaterialTheme.typography.titleMedium,
                color = c.textHigh
            )
        }

        Spacer(modifier = Modifier.height(Space.sm))

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
            style = MaterialTheme.typography.bodySmall,
            color = if (cloudUserEmail != null) c.emeraldGlow else c.textMid
        )

        cloudSyncStatus?.let { status ->
            Spacer(modifier = Modifier.height(Space.xs))
            Text(
                text = status,
                style = MaterialTheme.typography.bodySmall,
                color = c.accent
            )
        }

        Spacer(modifier = Modifier.height(Space.md))

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
                containerColor = c.accent,
                contentColor = c.ink,
                shape = CyberShapes.control,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("google_cloud_signin_button")
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Space.sm)
            ) {
                CyberTextButton(
                    onClick = { viewModel.syncProfileToCloud() },
                    text = if (lang == "ru") "СИНХР. ПРОФИЛЬ" else "SYNC PROFILE",
                    skin = currentSkin,
                    containerColor = c.accent,
                    contentColor = c.ink,
                    shape = CyberShapes.control,
                    modifier = Modifier.weight(1f)
                )
                CyberTextButton(
                    onClick = { viewModel.syncLatestReadingToCloud() },
                    text = if (lang == "ru") "БЭКАП РАСКЛАДА" else "SYNC READING",
                    skin = currentSkin,
                    containerColor = c.panelRaised,
                    contentColor = c.emeraldGlow,
                    shape = CyberShapes.control,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(Space.sm))
            CyberTextButton(
                onClick = { viewModel.signOutFromCloud(context) },
                text = if (lang == "ru") "ВЫЙТИ ИЗ АККАУНТА" else "SIGN OUT",
                skin = currentSkin,
                containerColor = c.danger.copy(alpha = 0.2f),
                contentColor = c.danger,
                shape = CyberShapes.control,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
