package com.example.ui.components

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Hexagon
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.Cyber
import com.example.ui.theme.CyberShapes
import com.example.ui.theme.CyberSkin
import com.example.ui.theme.CyberTheme
import com.example.ui.theme.IbmPlexMonoFontFamily
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.Space
import com.example.ui.theme.rememberMotionEnabled
import java.util.Locale

fun resolveLocalizedString(
    context: Context,
    @StringRes resId: Int,
    lang: String,
    vararg formatArgs: Any
): String {
    val localeTag = when (lang.lowercase()) {
        "ru", "uk" -> "ru"
        else -> "en"
    }
    val config = Configuration(context.resources.configuration).apply {
        setLocale(Locale.forLanguageTag(localeTag))
    }
    val localizedContext = context.createConfigurationContext(config)
    return if (formatArgs.isEmpty()) {
        localizedContext.getString(resId)
    } else {
        localizedContext.getString(resId, *formatArgs)
    }
}

@Composable
fun localizedString(
    @StringRes resId: Int,
    lang: String,
    vararg formatArgs: Any
): String {
    val context = LocalContext.current
    val localizedContext = remember(context, lang) {
        val localeTag = when (lang.lowercase()) {
            "ru", "uk" -> "ru"
            else -> "en"
        }
        val config = Configuration(context.resources.configuration).apply {
            setLocale(Locale.forLanguageTag(localeTag))
        }
        context.createConfigurationContext(config)
    }
    return if (formatArgs.isEmpty()) {
        localizedContext.getString(resId)
    } else {
        localizedContext.getString(resId, *formatArgs)
    }
}

/**
 * Unified skin-reactive ambient backdrop.
 * Uses the active CyberSkin wallpaper across every screen, softens fine wallpaper noise
 * (blur on API 31+ + ink gradient scrim), and marks the background decorative for TalkBack.
 */
@Composable
fun SkinBackdrop(
    skin: CyberSkin,
    modifier: Modifier = Modifier
) {
    val c = Cyber.colors
    val topAlpha = if (c.needsDenseScrim) 0.84f else 0.72f
    val bottomAlpha = if (c.needsDenseScrim) 0.94f else 0.88f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(c.ink)
    ) {
        Image(
            painter = painterResource(id = skin.backgroundResId),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = if (c.needsDenseScrim) 0.45f else 0.56f,
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Modifier.blur(18.dp)
                    } else {
                        Modifier
                    }
                )
        )

        // Quiet ink gradient scrim preserving contrast (>=4.5:1) over bright artwork
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            c.ink.copy(alpha = topAlpha),
                            c.ink.copy(alpha = (topAlpha + 0.06f).coerceAtMost(0.95f)),
                            c.ink.copy(alpha = bottomAlpha)
                        )
                    )
                )
        )

        // Subtle ambient skin tint aura at the top
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            skin.ambientGlowColor.copy(alpha = 0.16f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

/**
 * Quiet, edge-to-edge top bar with display-cutout insets, brand mark, honest section subtitle,
 * radio launcher, and WEED balance pill — every interactive item >= 48dp touch target.
 */
@Composable
fun CyberTopBar(
    brandTitle: String,
    subtitle: String,
    weedBalance: Int,
    isRadioPlaying: Boolean,
    isRadioBuffering: Boolean,
    visualizerBars: List<Float>,
    onToggleRadio: () -> Unit,
    onOpenBlessings: () -> Unit,
    modifier: Modifier = Modifier,
    systemLanguage: String = "en"
) {
    val c = Cyber.colors
    val haptic = LocalHapticFeedback.current

    Surface(
        color = c.panel.copy(alpha = 0.92f),
        tonalElevation = 0.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars.union(WindowInsets.displayCutout))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Space.lg, vertical = Space.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = c.goldLeaf,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = brandTitle,
                            fontFamily = OrbitronFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = c.goldLeaf,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(Space.xxs))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = c.accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Space.sm)
                ) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onToggleRadio()
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (isRadioPlaying) c.emerald.copy(alpha = 0.22f)
                                else c.panelRaised.copy(alpha = 0.85f)
                            )
                            .border(
                                1.dp,
                                if (isRadioPlaying) c.emeraldGlow else c.goldAntique.copy(alpha = 0.35f),
                                CircleShape
                            )
                            .testTag("cyber_radio_toggle_button")
                    ) {
                        if (isRadioPlaying) {
                            Row(
                                modifier = Modifier
                                    .width(16.dp)
                                    .height(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(1.5.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                visualizerBars.take(3).forEach { bar ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(bar.coerceIn(0.25f, 1f))
                                            .background(c.emeraldGlow)
                                    )
                                }
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Radio,
                                contentDescription = localizedString(R.string.cd_cyber_radio, systemLanguage),
                                tint = if (isRadioBuffering) c.goldLeaf else c.textHigh,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Surface(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onOpenBlessings()
                        },
                        shape = CyberShapes.control,
                        color = c.panelRaised,
                        border = BorderStroke(1.dp, c.goldAntique.copy(alpha = 0.55f)),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("wallet_credits_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = Space.md, vertical = Space.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = localizedString(R.string.cd_weed_credits, systemLanguage),
                                tint = c.accent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(Space.xs))
                            Text(
                                text = localizedString(R.string.balance_weed_format, systemLanguage, weedBalance),
                                fontFamily = IbmPlexMonoFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = c.textHigh,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
            HorizontalDivider(
                thickness = 1.dp,
                color = c.goldAntique.copy(alpha = 0.25f)
            )
        }
    }
}

/**
 * Chamfered dense-ink panel echoing the Art Nouveau card corners.
 */
@Composable
fun CyberPanel(
    modifier: Modifier = Modifier,
    borderColor: Color = Cyber.colors.goldAntique.copy(alpha = 0.4f),
    animateSize: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val c = Cyber.colors
    val motionEnabled = rememberMotionEnabled()
    Column(
        modifier = modifier
            .then(if (animateSize && motionEnabled) Modifier.animateContentSize(tween(220)) else Modifier)
            .clip(CyberShapes.panel)
            .background(c.panel.copy(alpha = 0.92f))
            .border(1.dp, borderColor, CyberShapes.panel)
            .padding(Space.lg),
        content = content
    )
}

/**
 * Accessible M3 action button enforcing a 48dp minimum touch target and haptic feedback.
 */
@Composable
fun CyberActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    secondary: Boolean = false,
    icon: ImageVector? = null
) {
    val c = Cyber.colors
    val haptic = LocalHapticFeedback.current
    val tint = if (secondary) c.goldLeaf else c.accent

    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        enabled = enabled,
        shape = CyberShapes.control,
        color = tint.copy(alpha = if (enabled) 0.16f else 0.05f),
        border = BorderStroke(1.dp, tint.copy(alpha = if (enabled) 0.85f else 0.28f)),
        modifier = modifier.heightIn(min = 48.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Space.lg, vertical = Space.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) tint else c.textMid.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(Space.sm))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) tint else c.textMid.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Selectable filter/segment chip that communicates selection via icon + border + color.
 */
@Composable
fun CyberChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null
) {
    val c = Cyber.colors
    val haptic = LocalHapticFeedback.current
    val borderColor = if (selected) c.emeraldGlow else c.goldAntique.copy(alpha = 0.35f)
    val fillColor = if (selected) c.emerald.copy(alpha = 0.22f) else c.panelRaised.copy(alpha = 0.72f)
    val textColor = if (selected) c.textHigh else c.textMid

    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        shape = CyberShapes.chip,
        color = fillColor,
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, borderColor),
        modifier = modifier.heightIn(min = 48.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Space.md, vertical = Space.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (selected) c.emeraldGlow else c.goldAntique,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(Space.xs))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Rarity tag that combines shape, icon, and color so tier is never color-only.
 */
@Composable
fun RarityTag(
    rarity: String,
    modifier: Modifier = Modifier
) {
    val tier = CyberRarityTier.fromString(rarity)
    val palette = CyberRarityPalettes.get(tier)
    val icon = when (tier) {
        CyberRarityTier.COMMON -> Icons.Default.Hexagon
        CyberRarityTier.RARE -> Icons.Default.Bolt
        CyberRarityTier.EPIC -> Icons.Default.AutoAwesome
        CyberRarityTier.LEGENDARY -> Icons.Default.Star
        CyberRarityTier.GENESIS -> Icons.Default.Diamond
    }

    Row(
        modifier = modifier
            .clip(CyberShapes.chip)
            .background(palette.darkBase.copy(alpha = 0.9f))
            .border(1.dp, palette.primary.copy(alpha = 0.7f), CyberShapes.chip)
            .padding(horizontal = Space.sm, vertical = Space.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.xs)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = palette.accent,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = rarity.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontFamily = IbmPlexMonoFontFamily,
            color = palette.accent,
            maxLines = 1
        )
    }
}

@Composable
fun CyberNeonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Cyber.colors.accent,
    enabled: Boolean = true,
    fontSize: TextUnit = 13.sp
) {
    val c = Cyber.colors
    val haptic = LocalHapticFeedback.current
    Surface(
        shape = CyberShapes.control,
        color = if (enabled) color.copy(alpha = 0.16f) else c.panelRaised,
        border = BorderStroke(1.dp, if (enabled) color else c.textMid.copy(alpha = 0.3f)),
        modifier = modifier
            .heightIn(min = 48.dp)
            .clickable(enabled = enabled, role = Role.Button) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
    ) {
        Box(
            modifier = Modifier.padding(horizontal = Space.lg, vertical = Space.md),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (enabled) color else c.textMid.copy(alpha = 0.5f),
                fontSize = if (fontSize.value < 12f) 12.sp else fontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = IbmPlexMonoFontFamily,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CyberBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Cyber.colors.emeraldGlow,
    fontSize: TextUnit = 12.sp
) {
    Box(
        modifier = modifier
            .clip(CyberShapes.chip)
            .background(color.copy(alpha = 0.16f))
            .border(1.dp, color.copy(alpha = 0.65f), CyberShapes.chip)
            .padding(horizontal = Space.sm, vertical = Space.xs),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            fontSize = if (fontSize.value < 12f) 12.sp else fontSize,
            fontWeight = FontWeight.Bold,
            fontFamily = IbmPlexMonoFontFamily
        )
    }
}

@Composable
fun CyberSectionHeader(
    title: String,
    subtitle: String? = null,
    color: Color = Cyber.colors.goldLeaf,
    modifier: Modifier = Modifier
) {
    val c = Cyber.colors
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = color
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = c.textMid
            )
        }
        Spacer(modifier = Modifier.height(Space.xs))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(color, color.copy(alpha = 0.2f), Color.Transparent)
                    )
                )
        )
    }
}

@Preview(name = "Oracle Theme • 1.0x", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Preview(name = "Gaudi Theme • 1.5x Font", fontScale = 1.5f, uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PreviewCyberDesignSystem() {
    CyberTheme(skin = CyberSkin.GAUDI_VANGOGH) {
        Column(
            modifier = Modifier
                .background(Cyber.colors.ink)
                .padding(Space.lg),
            verticalArrangement = Arrangement.spacedBy(Space.md)
        ) {
            CyberPanel {
                CyberSectionHeader(
                    title = "Ботанический Оракул",
                    subtitle = "Золото × Изумруд × Звёздная ночь"
                )
                Spacer(modifier = Modifier.height(Space.md))
                Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                    RarityTag("Legend")
                    CyberChip(label = "3 Карты", selected = true, onClick = {}, leadingIcon = Icons.Default.Shield)
                }
                Spacer(modifier = Modifier.height(Space.md))
                CyberActionButton(
                    text = "Сделать расклад",
                    onClick = {},
                    icon = Icons.Default.AutoAwesome,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
