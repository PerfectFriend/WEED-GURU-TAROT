package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.*
import com.example.ui.theme.*
import kotlin.math.*

/**
 * Visual Lunar Cycle Widget displaying the current moon phase, interactive 14-day
 * calendar timeline, and dynamic auspicious/challenging minting recommendations
 * for Cyber Tarot cards.
 */
@Composable
fun CyberLunarCycleWidget(
    systemLanguage: String = "en",
    currentSkin: CyberSkin = CyberSkin.JAMAICA,
    onMintClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val lang = if (systemLanguage in listOf("en", "ru", "es")) systemLanguage else "en"
    val c = Cyber.colors

    val forecastList = remember(lang) {
        LunarCalendarService.getTarotMintingForecast(daysAhead = 14, lang = lang)
    }

    var selectedDayIndex by remember { mutableIntStateOf(0) }
    val focusedDay = forecastList.getOrElse(selectedDayIndex) { forecastList.first() }
    val mintingWindow = focusedDay.mintingWindow

    val tierColor by animateColorAsState(
        targetValue = when (mintingWindow.tier) {
            MintingAuspiciousnessTier.AUSPICIOUS -> EmeraldPure
            MintingAuspiciousnessTier.BALANCED -> NeonCyan
            MintingAuspiciousnessTier.CHALLENGING -> CrimsonPulse
        },
        animationSpec = tween(500),
        label = "tier_color_anim"
    )

    val tierAccentColor = when (mintingWindow.tier) {
        MintingAuspiciousnessTier.AUSPICIOUS -> SolarGold
        MintingAuspiciousnessTier.BALANCED -> CyanLight
        MintingAuspiciousnessTier.CHALLENGING -> NeonPurple
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cyber_lunar_cycle_widget")
            .shadow(16.dp, CyberShapes.card, ambientColor = tierColor, spotColor = tierColor),
        shape = CyberShapes.card,
        colors = CardDefaults.cardColors(containerColor = c.panel.copy(alpha = 0.96f)),
        border = BorderStroke(1.5.dp, tierColor.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Space.lg)
        ) {
            // Header: Title & Today Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(tierColor, CircleShape)
                            .border(1.5.dp, c.textHigh, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(Space.sm))
                    Text(
                        text = when (lang) {
                            "ru" -> "ЛУННЫЙ ОРАКУЛ ЧЕКАНКИ"
                            "es" -> "ORÁCULO LUNAR DE ACUÑACIÓN"
                            else -> "LUNAR MINTING ORACLE"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontFamily = IbmPlexMonoFontFamily,
                        color = tierColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(Space.sm))

                if (selectedDayIndex != 0) {
                    TextButton(
                        onClick = { selectedDayIndex = 0 },
                        contentPadding = PaddingValues(horizontal = Space.sm, vertical = Space.xs),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(Space.xs))
                        Text(
                            text = when (lang) {
                                "ru" -> "СЕГОДНЯ"
                                "es" -> "HOY"
                                else -> "TODAY"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = IbmPlexMonoFontFamily,
                            color = NeonCyan
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(CyberShapes.chip)
                            .background(tierColor.copy(alpha = 0.15f))
                            .border(1.dp, tierColor, CyberShapes.chip)
                            .padding(horizontal = Space.sm, vertical = Space.xxs)
                    ) {
                        Text(
                            text = when (lang) {
                                "ru" -> "АКТИВНО СЕЙЧАС"
                                "es" -> "EN TIEMPO REAL"
                                else -> "REAL-TIME"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = tierColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Space.md))

            // Main Central Display: Interactive Moon Visual Sphere & Status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(c.ink.copy(alpha = 0.85f), CyberShapes.card)
                    .border(1.dp, tierColor.copy(alpha = 0.35f), CyberShapes.card)
                    .padding(Space.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    VisualMoonSphere(
                        ageFactor = focusedDay.ageFactor,
                        illumination = focusedDay.illumination,
                        glowColor = tierColor,
                        accentColor = tierAccentColor,
                        modifier = Modifier.size(110.dp)
                    )
                }

                Spacer(modifier = Modifier.width(Space.md))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = focusedDay.dateString.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = c.textMid,
                            maxLines = 1
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NightsStay,
                                contentDescription = null,
                                tint = tierAccentColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(Space.xxs))
                            Text(
                                text = focusedDay.astrologicalSign,
                                style = MaterialTheme.typography.labelSmall,
                                color = tierAccentColor,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Space.xxs))

                    Text(
                        text = focusedDay.phaseName.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = IbmPlexMonoFontFamily,
                        color = c.textHigh
                    )

                    Text(
                        text = "${when (lang) { "ru" -> "Освещенность:" "es" -> "Iluminación:" else -> "Illumination:" }} ${focusedDay.illumination}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = c.textMid
                    )

                    Spacer(modifier = Modifier.height(Space.sm))

                    Box(
                        modifier = Modifier
                            .clip(CyberShapes.chip)
                            .background(tierColor.copy(alpha = 0.20f))
                            .border(1.5.dp, tierColor, CyberShapes.chip)
                            .padding(horizontal = Space.sm, vertical = Space.xs)
                    ) {
                        Text(
                            text = mintingWindow.badgeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = tierColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Space.md))

            // Astral Conductivity Gauge & Active Modifiers
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(c.ink.copy(alpha = 0.55f), CyberShapes.cardCompact)
                    .border(1.dp, c.goldAntique.copy(alpha = 0.22f), CyberShapes.cardCompact)
                    .padding(Space.md)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (lang) {
                            "ru" -> "АСТРАЛЬНАЯ ПРОВОДИМОСТЬ"
                            "es" -> "CONDUCTIVIDAD ASTRAL"
                            else -> "ASTRAL CONDUCTIVITY"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = c.textMid
                    )
                    Text(
                        text = "${(mintingWindow.astralConductivity * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        fontFamily = IbmPlexMonoFontFamily,
                        color = tierColor
                    )
                }

                Spacer(modifier = Modifier.height(Space.xs))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CyberShapes.chip)
                        .background(c.panelRaised)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(mintingWindow.astralConductivity)
                            .fillMaxHeight()
                            .clip(CyberShapes.chip)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(tierColor.copy(alpha = 0.5f), tierColor, tierAccentColor)
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(Space.md))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Space.sm)
                ) {
                    val dropRateText = if (mintingWindow.dropRateModifierPercent > 0) {
                        "+${mintingWindow.dropRateModifierPercent}% Rare/Epic"
                    } else if (mintingWindow.dropRateModifierPercent < 0) {
                        "${mintingWindow.dropRateModifierPercent}% Static"
                    } else {
                        "Standard 1.0x"
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(c.panelRaised.copy(alpha = 0.85f), CyberShapes.chip)
                            .border(1.dp, tierColor.copy(alpha = 0.4f), CyberShapes.chip)
                            .padding(Space.sm)
                    ) {
                        Column {
                            Text(
                                text = when (lang) { "ru" -> "ДРОП-РЕЙТ" "es" -> "TASA" else -> "DROP BOOST" },
                                style = MaterialTheme.typography.labelSmall,
                                color = c.textMid
                            )
                            Text(
                                text = dropRateText,
                                style = MaterialTheme.typography.labelMedium,
                                fontFamily = IbmPlexMonoFontFamily,
                                color = tierColor
                            )
                        }
                    }

                    val gasText = if (mintingWindow.gasFeeDiscountPercent > 0) {
                        "-${mintingWindow.gasFeeDiscountPercent}% Gas (\$WEED)"
                    } else if (mintingWindow.gasFeeDiscountPercent < 0) {
                        "+${abs(mintingWindow.gasFeeDiscountPercent)}% Congestion"
                    } else {
                        "100 \$WEED"
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(c.panelRaised.copy(alpha = 0.85f), CyberShapes.chip)
                            .border(1.dp, tierAccentColor.copy(alpha = 0.4f), CyberShapes.chip)
                            .padding(Space.sm)
                    ) {
                        Column {
                            Text(
                                text = when (lang) { "ru" -> "КОМИССИЯ \$WEED" "es" -> "TARIFA \$WEED" else -> "GAS MODIFIER" },
                                style = MaterialTheme.typography.labelSmall,
                                color = c.textMid
                            )
                            Text(
                                text = gasText,
                                style = MaterialTheme.typography.labelMedium,
                                fontFamily = IbmPlexMonoFontFamily,
                                color = tierAccentColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Space.md))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (lang) { "ru" -> "Стихии: " "es" -> "Elementos: " else -> "Affinity: " },
                        style = MaterialTheme.typography.labelSmall,
                        color = c.textMid
                    )
                    mintingWindow.favoredElements.forEach { element ->
                        Box(
                            modifier = Modifier
                                .padding(end = Space.xs)
                                .clip(CyberShapes.chip)
                                .background(c.panelRaised)
                                .border(1.dp, tierColor.copy(alpha = 0.5f), CyberShapes.chip)
                                .padding(horizontal = Space.sm, vertical = Space.xxs)
                        ) {
                            Text(
                                text = element,
                                style = MaterialTheme.typography.labelSmall,
                                color = tierColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Space.xs))

                Text(
                    text = mintingWindow.ritualAdvice,
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textHigh,
                    modifier = Modifier.padding(top = Space.xxs)
                )
            }

            Spacer(modifier = Modifier.height(Space.md))

            Text(
                text = when (lang) {
                    "ru" -> "КАЛЕНДАРЬ АСТРАЛЬНЫХ ЦИКЛОВ (14 ДНЕЙ)"
                    "es" -> "CALENDARIO LUNAR (14 DÍAS)"
                    else -> "14-DAY ASTRAL TIMELINE"
                },
                style = MaterialTheme.typography.labelSmall,
                color = c.textMid,
                modifier = Modifier.padding(bottom = Space.sm)
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Space.sm)
            ) {
                itemsIndexed(forecastList) { index, day ->
                    val isSelected = index == selectedDayIndex
                    val itemTierColor = when (day.mintingWindow.tier) {
                        MintingAuspiciousnessTier.AUSPICIOUS -> EmeraldPure
                        MintingAuspiciousnessTier.BALANCED -> NeonCyan
                        MintingAuspiciousnessTier.CHALLENGING -> CrimsonPulse
                    }

                    Surface(
                        onClick = { selectedDayIndex = index },
                        shape = CyberShapes.cardCompact,
                        color = if (isSelected) itemTierColor.copy(alpha = 0.22f) else c.panelRaised.copy(alpha = 0.72f),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) itemTierColor else c.goldAntique.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier
                            .width(72.dp)
                            .heightIn(min = 84.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = Space.sm, horizontal = Space.xs)
                        ) {
                            Text(
                                text = if (index == 0) (if (lang == "ru") "СЕГОДНЯ" else "TODAY") else day.dateString.take(6),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) c.textHigh else c.textMid,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(Space.xs))

                            Text(
                                text = day.phaseIcon,
                                style = MaterialTheme.typography.titleLarge
                            )

                            Spacer(modifier = Modifier.height(Space.xs))

                            Text(
                                text = "${day.illumination}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = itemTierColor
                            )

                            Box(
                                modifier = Modifier
                                    .padding(top = Space.xs)
                                    .size(6.dp)
                                    .background(itemTierColor, CircleShape)
                            )
                        }
                    }
                }
            }

            if (onMintClick != null) {
                Spacer(modifier = Modifier.height(Space.md))

                Button(
                    onClick = onMintClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("lunar_mint_action_button"),
                    shape = CyberShapes.control,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tierColor,
                        contentColor = InkDeep
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(Space.sm))
                    Text(
                        text = if (mintingWindow.tier == MintingAuspiciousnessTier.AUSPICIOUS) {
                            when (lang) {
                                "ru" -> "ЧЕКАНИТЬ С ЛУННЫМ БОНУСОМ (+18%)"
                                "es" -> "ACUÑAR CON BONO LUNAR (+18%)"
                                else -> "MINT WITH LUNAR BOOST (+18%)"
                            }
                        } else {
                            when (lang) {
                                "ru" -> "ЧЕКАНИТЬ АРКАН В ТЕКУЩЕЙ ФАЗЕ"
                                "es" -> "ACUÑAR EN FASE ACTUAL"
                                else -> "MINT DURING CURRENT PHASE"
                            }
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontFamily = IbmPlexMonoFontFamily
                    )
                }
            }
        }
    }
}

/**
 * Procedural Vector Moon Sphere that accurately renders the illuminated crescent/gibbous
 * terminator curve, glowing corona, lunar craters, and celestial reticle.
 */
@Composable
fun VisualMoonSphere(
    ageFactor: Double,
    illumination: Int,
    glowColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val motionEnabled = rememberMotionEnabled()
    val pulseScale: Float
    val reticleAngle: Float
    if (motionEnabled) {
        val infiniteTransition = rememberInfiniteTransition(label = "lunar_corona_pulse")
        pulseScale = infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(2800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_scale"
        ).value
        reticleAngle = infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(24000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "reticle_rotation"
        ).value
    } else {
        pulseScale = 1.0f
        reticleAngle = 0f
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val center = Offset(width * 0.5f, height * 0.5f)
        val radius = width * 0.36f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    glowColor.copy(alpha = 0.35f * pulseScale),
                    glowColor.copy(alpha = 0.12f),
                    Color.Transparent
                ),
                center = center,
                radius = radius * 1.55f * pulseScale
            ),
            radius = radius * 1.55f * pulseScale,
            center = center
        )

        rotate(degrees = reticleAngle, pivot = center) {
            drawCircle(
                color = glowColor.copy(alpha = 0.35f),
                radius = radius * 1.25f,
                center = center,
                style = Stroke(
                    width = 1.2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
                )
            )

            drawLine(
                color = accentColor.copy(alpha = 0.6f),
                start = Offset(center.x - radius * 1.35f, center.y),
                end = Offset(center.x - radius * 1.15f, center.y),
                strokeWidth = 1.8f
            )
            drawLine(
                color = accentColor.copy(alpha = 0.6f),
                start = Offset(center.x + radius * 1.15f, center.y),
                end = Offset(center.x + radius * 1.35f, center.y),
                strokeWidth = 1.8f
            )
            drawLine(
                color = accentColor.copy(alpha = 0.6f),
                start = Offset(center.x, center.y - radius * 1.35f),
                end = Offset(center.x, center.y - radius * 1.15f),
                strokeWidth = 1.8f
            )
            drawLine(
                color = accentColor.copy(alpha = 0.6f),
                start = Offset(center.x, center.y + radius * 1.15f),
                end = Offset(center.x, center.y + radius * 1.35f),
                strokeWidth = 1.8f
            )
        }

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(MoonDarkRim, MoonDarkCore, MoonDarkVoid),
                center = center,
                radius = radius
            ),
            radius = radius,
            center = center
        )

        val crater1 = Offset(center.x - radius * 0.35f, center.y - radius * 0.25f)
        drawCircle(color = InkDeep.copy(alpha = 0.22f), radius = radius * 0.18f, center = crater1)
        val crater2 = Offset(center.x + radius * 0.25f, center.y + radius * 0.35f)
        drawCircle(color = InkDeep.copy(alpha = 0.18f), radius = radius * 0.22f, center = crater2)
        val crater3 = Offset(center.x - radius * 0.15f, center.y + radius * 0.40f)
        drawCircle(color = InkDeep.copy(alpha = 0.14f), radius = radius * 0.14f, center = crater3)

        drawIlluminatedPhase(center, radius, ageFactor, glowColor)

        drawCircle(
            color = TextHigh.copy(alpha = 0.4f),
            radius = radius,
            center = center,
            style = Stroke(width = 1.2f)
        )
    }
}

private fun DrawScope.drawIlluminatedPhase(
    center: Offset,
    radius: Float,
    ageFactor: Double,
    glowColor: Color
) {
    if (ageFactor < 0.02 || ageFactor > 0.98) {
        drawCircle(
            color = glowColor.copy(alpha = 0.15f),
            radius = radius,
            center = center,
            style = Stroke(width = 1f)
        )
        return
    }

    if (ageFactor in 0.47..0.53) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    MoonLitBright,
                    MoonLitSoft,
                    MoonLitCool,
                    glowColor.copy(alpha = 0.75f)
                ),
                center = Offset(center.x - radius * 0.2f, center.y - radius * 0.2f),
                radius = radius
            ),
            radius = radius,
            center = center
        )
        return
    }

    val isWaxing = ageFactor < 0.5
    val phaseRatio = if (isWaxing) ageFactor * 2.0 else (1.0 - ageFactor) * 2.0
    val terminatorXFactor = ((phaseRatio - 0.5) * 2.0).toFloat()

    val litBrush = Brush.radialGradient(
        colors = listOf(
            MoonLitBright,
            MoonLitSoft,
            MoonLitEdge,
            glowColor.copy(alpha = 0.65f)
        ),
        center = Offset(
            center.x + (if (isWaxing) radius * 0.3f else -radius * 0.3f),
            center.y - radius * 0.2f
        ),
        radius = radius
    )

    val path = Path()
    val top = Offset(center.x, center.y - radius)

    if (isWaxing) {
        path.moveTo(top.x, top.y)
        path.arcTo(
            rect = androidx.compose.ui.geometry.Rect(
                center.x - radius, center.y - radius,
                center.x + radius, center.y + radius
            ),
            startAngleDegrees = -90f,
            sweepAngleDegrees = 180f,
            forceMoveTo = false
        )
        path.quadraticTo(
            center.x + radius * terminatorXFactor,
            center.y,
            top.x,
            top.y
        )
    } else {
        path.moveTo(top.x, top.y)
        path.arcTo(
            rect = androidx.compose.ui.geometry.Rect(
                center.x - radius, center.y - radius,
                center.x + radius, center.y + radius
            ),
            startAngleDegrees = 90f,
            sweepAngleDegrees = 180f,
            forceMoveTo = false
        )
        path.quadraticTo(
            center.x - radius * terminatorXFactor,
            center.y,
            top.x,
            top.y
        )
    }
    path.close()

    drawPath(path = path, brush = litBrush)
}
