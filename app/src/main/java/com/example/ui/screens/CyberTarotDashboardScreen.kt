package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.LocalizationHelper
import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import com.example.ui.components.CyberActionButton
import com.example.ui.components.CyberChip
import com.example.ui.components.CyberPanel
import com.example.ui.components.CyberRadioConsoleDialog
import com.example.ui.components.CyberRadioMiniBar
import com.example.ui.components.CyberSectionHeader
import com.example.ui.components.CyberTopBar
import com.example.ui.components.RarityTag
import com.example.ui.components.SkinBackdrop
import com.example.ui.components.localizedString
import com.example.ui.theme.Cyber
import com.example.ui.theme.CyberShapes
import com.example.ui.theme.CyberSkin
import com.example.ui.theme.CyberTheme
import com.example.ui.theme.Exo2FontFamily
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.Space
import com.example.ui.theme.rememberMotionEnabled
import com.example.viewmodel.TarotViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CyberTarotDashboard(viewModel: TarotViewModel) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var splashStep by rememberSaveable { mutableStateOf(SplashStep.CONNECT_SCREEN) }

    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val notification by viewModel.uiNotification.collectAsStateWithLifecycle()
    val isLocked by viewModel.isAppLocked.collectAsStateWithLifecycle()
    val isSafeMode by viewModel.isSafeBotanicalMode.collectAsStateWithLifecycle()
    val showBlessingsPortal by viewModel.showBlessingsPortal.collectAsStateWithLifecycle()
    var showOnboarding by rememberSaveable { mutableStateOf(false) }
    val lang = profile?.systemLanguage ?: "en"
    val motionEnabled = rememberMotionEnabled()
    val haptic = LocalHapticFeedback.current
    val c = Cyber.colors

    val detectedSystemLang = remember { LocalizationHelper.getSystemDefaultLanguage() }
    val activeLang = if (lang.isNotBlank()) lang else detectedSystemLang
    val appBrandTitle = "CYBERTAROT"
    val validatingText = localizedString(R.string.splash_validating_profile, activeLang)
    val launchingText = localizedString(R.string.splash_launching_setup, activeLang)
    var splashStatusText by remember(validatingText) { mutableStateOf(validatingText) }

    BackHandler(
        enabled = !isLocked && selectedTab != 0 && !showOnboarding && splashStep == SplashStep.DONE && !isSafeMode
    ) {
        selectedTab = 0
    }

    LaunchedEffect(splashStep, profile) {
        if (splashStep == SplashStep.CONNECT_SCREEN) {
            viewModel.updateSystemLanguage(detectedSystemLang)
            delay(600)
            val profileSecured = profile?.isSecured == true && !profile?.pinCode.isNullOrBlank()
            if (!profileSecured) {
                splashStatusText = launchingText
                showOnboarding = true
            }
            splashStep = SplashStep.DONE
        }
    }

    val tabs = listOf(
        LocalizationHelper.getString(activeLang, "tab_divination") to Icons.Default.AutoAwesome,
        LocalizationHelper.getString(activeLang, "tab_cards") to Icons.Default.Style,
        LocalizationHelper.getString(activeLang, "tab_arena") to Icons.Default.Shield,
        LocalizationHelper.getString(activeLang, "tab_crypto") to Icons.Default.CurrencyBitcoin,
        LocalizationHelper.getString(activeLang, "tab_settings") to Icons.Default.Settings
    )

    val tabSubtitle = when (selectedTab) {
        0 -> localizedString(R.string.tab_sub_divination, activeLang)
        1 -> localizedString(R.string.tab_sub_cards, activeLang)
        2 -> localizedString(R.string.tab_sub_arena, activeLang)
        3 -> localizedString(R.string.tab_sub_crypto, activeLang)
        else -> localizedString(R.string.tab_sub_settings, activeLang)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val radioState by com.example.audio.CyberRadioPlayer.state.collectAsStateWithLifecycle()

        if (isSafeMode) {
            SafeBotanicalModeScreen(viewModel)
        } else if (showOnboarding) {
            OnboardingWizard(viewModel, onComplete = {
                showOnboarding = false
                splashStep = SplashStep.DONE
            })
        } else {
            SkinBackdrop(
                skin = currentSkin,
                modifier = Modifier.fillMaxSize()
            )

            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                topBar = {
                    if (!isLocked && selectedTab != 2) {
                        CyberTopBar(
                            brandTitle = appBrandTitle,
                            subtitle = tabSubtitle,
                            weedBalance = profile?.weed ?: 0,
                            isRadioPlaying = radioState.isPlaying,
                            isRadioBuffering = radioState.isBuffering,
                            visualizerBars = radioState.visualizerBars,
                            onToggleRadio = { com.example.audio.CyberRadioPlayer.toggleConsole() },
                            onOpenBlessings = { viewModel.setBlessingsPortalVisible(true) },
                            systemLanguage = activeLang
                        )
                    }
                },
                bottomBar = {
                    if (!isLocked) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (radioState.isPlaying || radioState.isBuffering) {
                                CyberRadioMiniBar(
                                    systemLanguage = activeLang,
                                    modifier = Modifier.padding(horizontal = Space.md, vertical = Space.xs),
                                    onOpenConsole = { com.example.audio.CyberRadioPlayer.openConsole() }
                                )
                            }

                            NavigationBar(
                                containerColor = c.panel.copy(alpha = 0.95f),
                                tonalElevation = 0.dp,
                                windowInsets = NavigationBarDefaults.windowInsets,
                                modifier = Modifier
                                    .testTag("bottom_nav_bar")
                                    .drawBehind {
                                        drawLine(
                                            color = c.goldAntique.copy(alpha = 0.3f),
                                            start = Offset(0f, 0f),
                                            end = Offset(size.width, 0f),
                                            strokeWidth = 1.dp.toPx()
                                        )
                                    }
                            ) {
                                tabs.forEachIndexed { index, (title, icon) ->
                                    val isSelected = selectedTab == index
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = {
                                            if (selectedTab != index) {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                selectedTab = index
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = title
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = title,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontFamily = Exo2FontFamily,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = c.emeraldGlow,
                                            selectedTextColor = c.emeraldGlow,
                                            indicatorColor = c.emerald.copy(alpha = 0.24f),
                                            unselectedIconColor = c.textMid,
                                            unselectedTextColor = c.textMid
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (isLocked) {
                        PinLockScreen(viewModel, onRestoreRequest = { showOnboarding = true })
                    } else {
                        AnimatedContent(
                            targetState = selectedTab,
                            transitionSpec = {
                                if (motionEnabled) {
                                    fadeIn(tween(180)) togetherWith fadeOut(tween(120))
                                } else {
                                    EnterTransition.None togetherWith ExitTransition.None
                                }
                            },
                            label = "dashboard_tab_transition"
                        ) { tab ->
                            when (tab) {
                                0 -> DivinationScreen(viewModel)
                                1 -> UnifiedCardsScreen(viewModel)
                                2 -> com.example.duel.DuelArenaScreen(
                                    viewModel = viewModel,
                                    onBack = { selectedTab = 0 }
                                )
                                3 -> CryptoHubScreen(viewModel)
                                else -> SecurityScreen(viewModel)
                            }
                        }
                    }

                    notification?.let { msg ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Space.lg)
                                .align(Alignment.BottomCenter)
                        ) {
                            Surface(
                                color = c.panel.copy(alpha = 0.96f),
                                shape = CyberShapes.control,
                                border = BorderStroke(1.dp, c.goldLeaf.copy(alpha = 0.7f)),
                                tonalElevation = 4.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = Space.md, vertical = Space.sm),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = c.textHigh,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { viewModel.dismissNotification() },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = localizedString(R.string.action_dismiss, activeLang),
                                            tint = c.textMid,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showBlessingsPortal) {
            BlessingQuestsHubDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.setBlessingsPortalVisible(false) }
            )
        }

        if (radioState.isConsoleOpen) {
            CyberRadioConsoleDialog(
                systemLanguage = activeLang,
                onDismiss = { com.example.audio.CyberRadioPlayer.closeConsole() }
            )
        }

        if (splashStep != SplashStep.DONE && !showOnboarding && !isSafeMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(c.ink),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_splash_ganja_sunflowers),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    c.ink.copy(alpha = 0.72f),
                                    c.ink.copy(alpha = 0.48f),
                                    c.ink.copy(alpha = 0.90f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .padding(horizontal = Space.xl, vertical = Space.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = Space.lg)
                    ) {
                        Text(
                            text = appBrandTitle,
                            color = c.goldLeaf,
                            letterSpacing = 3.sp,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontFamily = OrbitronFontFamily,
                                shadow = Shadow(
                                    color = c.emeraldGlow.copy(alpha = 0.6f),
                                    offset = Offset(0f, 0f),
                                    blurRadius = 20f
                                )
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(Space.xs))
                        Text(
                            text = localizedString(R.string.splash_subtitle, activeLang),
                            style = MaterialTheme.typography.titleMedium,
                            color = c.textHigh,
                            textAlign = TextAlign.Center
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = Space.lg),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Space.lg)
                    ) {
                        Surface(
                            shape = CyberShapes.control,
                            color = c.panel.copy(alpha = 0.88f),
                            border = BorderStroke(1.dp, c.goldAntique.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = Space.lg, vertical = Space.sm),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Space.sm)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = c.emeraldGlow
                                )
                                Text(
                                    text = splashStatusText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = c.textHigh
                                )
                            }
                        }

                        CyberActionButton(
                            text = localizedString(R.string.action_enter, activeLang),
                            icon = Icons.Default.Bolt,
                            onClick = {
                                com.example.audio.CyberNoirSynthPlayer.triggerFX(
                                    com.example.audio.CyberNoirSynthPlayer.FXType.SUCCESS_CHIME
                                )
                                val profileSecured = profile?.isSecured == true && !profile?.pinCode.isNullOrBlank()
                                if (!profileSecured) {
                                    showOnboarding = true
                                }
                                splashStep = SplashStep.DONE
                            },
                            modifier = Modifier.fillMaxWidth(0.92f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardSkinPreviewCanvas(
    skin: CyberSkin,
    lang: String
) {
    CyberTheme(skin = skin) {
        Box(modifier = Modifier.fillMaxSize()) {
            SkinBackdrop(skin = skin)
            Column(modifier = Modifier.fillMaxSize()) {
                CyberTopBar(
                    brandTitle = "CYBERTAROT",
                    subtitle = localizedString(R.string.tab_sub_divination, lang),
                    weedBalance = 1420,
                    isRadioPlaying = false,
                    isRadioBuffering = false,
                    visualizerBars = listOf(0.4f, 0.8f, 0.5f),
                    onToggleRadio = {},
                    onOpenBlessings = {},
                    systemLanguage = lang
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Space.lg),
                    verticalArrangement = Arrangement.spacedBy(Space.md)
                ) {
                    CyberPanel {
                        CyberSectionHeader(
                            title = localizedString(R.string.divination_title, lang),
                            subtitle = localizedString(R.string.divination_subtitle, lang)
                        )
                        Spacer(modifier = Modifier.height(Space.md))
                        Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                            CyberChip(
                                label = localizedString(R.string.spread_flow_title, lang),
                                selected = true,
                                onClick = {}
                            )
                            RarityTag("Legend")
                        }
                        Spacer(modifier = Modifier.height(Space.md))
                        CyberActionButton(
                            text = localizedString(R.string.divination_initialize_scan, lang),
                            icon = Icons.Default.AutoAwesome,
                            onClick = {},
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Oracle Temple • EN", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PreviewDashboardOracleTemple() {
    DashboardSkinPreviewCanvas(skin = CyberSkin.ORACLE_TEMPLE, lang = "en")
}

@Preview(name = "Jamaica • Scrim EN", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PreviewDashboardJamaica() {
    DashboardSkinPreviewCanvas(skin = CyberSkin.JAMAICA, lang = "en")
}

@Preview(name = "Gaudi Van Gogh • RU • 1.5x", fontScale = 1.5f, uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PreviewDashboardGaudiLargeFontRu() {
    DashboardSkinPreviewCanvas(skin = CyberSkin.GAUDI_VANGOGH, lang = "ru")
}

