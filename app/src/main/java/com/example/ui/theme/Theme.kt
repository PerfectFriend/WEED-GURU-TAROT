package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberColorScheme = darkColorScheme(
    primary = NeonMagenta,
    secondary = NeonCyan,
    tertiary = NeonGold,
    background = CyberObsidian,
    surface = CyberCharcoal,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFFE2E2EC),
    onSurface = Color(0xFFE2E2EC),
    surfaceVariant = CyberMutedBlue,
    onSurfaceVariant = Color(0xFFB0B0C5),
    error = NeonRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic colors so we preserve our pristine custom cyberpunk theme
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CyberColorScheme,
        typography = Typography,
        content = content
    )
}
