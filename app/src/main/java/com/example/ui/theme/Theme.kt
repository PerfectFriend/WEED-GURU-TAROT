package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class CyberColors(
    val ink: Color = InkDeep,
    val panel: Color = PanelDark,
    val panelRaised: Color = PanelRaised,
    val goldLeaf: Color = GoldLeaf,
    val goldAntique: Color = GoldAntique,
    val bronzeShadow: Color = BronzeShadow,
    val emerald: Color = EmeraldLife,
    val emeraldGlow: Color = EmeraldGlow,
    val textHigh: Color = TextHigh,
    val textMid: Color = TextMid,
    val danger: Color = SignalDanger,
    val accent: Color = EmeraldGlow,
    val accentAlt: Color = GoldLeaf,
    val glow: Color = Color(0x6646D68C),
    val needsDenseScrim: Boolean = false
)

val LocalCyberColors = staticCompositionLocalOf { CyberColors() }

object Cyber {
    val colors: CyberColors
        @Composable
        get() = LocalCyberColors.current
}

@Composable
fun CyberTheme(
    skin: CyberSkin = CyberSkin.ORACLE_TEMPLE,
    content: @Composable () -> Unit
) {
    val cyberColors = remember(skin) {
        CyberColors(
            accent = skin.primaryColor,
            accentAlt = skin.secondaryColor,
            glow = skin.ambientGlowColor,
            needsDenseScrim = (skin == CyberSkin.JAMAICA)
        )
    }

    val colorScheme = remember(cyberColors) {
        darkColorScheme(
            primary = cyberColors.accent,
            onPrimary = cyberColors.ink,
            primaryContainer = cyberColors.panelRaised,
            onPrimaryContainer = cyberColors.textHigh,
            secondary = cyberColors.accentAlt,
            onSecondary = cyberColors.ink,
            secondaryContainer = cyberColors.panelRaised,
            onSecondaryContainer = cyberColors.textHigh,
            tertiary = cyberColors.goldLeaf,
            onTertiary = cyberColors.ink,
            background = cyberColors.ink,
            onBackground = cyberColors.textHigh,
            surface = cyberColors.panel,
            onSurface = cyberColors.textHigh,
            surfaceVariant = cyberColors.panelRaised,
            onSurfaceVariant = cyberColors.textMid,
            outline = cyberColors.goldAntique.copy(alpha = 0.45f),
            outlineVariant = cyberColors.bronzeShadow.copy(alpha = 0.35f),
            error = cyberColors.danger,
            onError = Color.White
        )
    }

    CompositionLocalProvider(LocalCyberColors provides cyberColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = CyberTypography,
            content = content
        )
    }
}

@Composable
fun MyApplicationTheme(
    skin: CyberSkin = CyberSkin.ORACLE_TEMPLE,
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    CyberTheme(skin = skin, content = content)
}
