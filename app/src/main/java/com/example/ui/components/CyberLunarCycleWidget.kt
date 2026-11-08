package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*
import java.util.Calendar
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

    // 14-day forecast starting from today
    val forecastList = remember(lang) {
        LunarCalendarService.getTarotMintingForecast(daysAhead = 14, lang = lang)
    }

    // Selected day index in forecast (0 = today)
    var selectedDayIndex by remember { mutableIntStateOf(0) }
    val focusedDay = forecastList.getOrElse(selectedDayIndex) { forecastList.first() }
    val mintingWindow = focusedDay.mintingWindow

    val tierColor by animateColorAsState(
        targetValue = when (mintingWindow.tier) {
            MintingAuspiciousnessTier.AUSPICIOUS -> Color(0xFF00FF66) // Neon Emerald
            MintingAuspiciousnessTier.BALANCED -> Color(0xFF00F5FF)   // Electric Cyan
            MintingAuspiciousnessTier.CHALLENGING -> Color(0xFFFF3366) // Neon Crimson/Red
        },
        animationSpec = tween(500),
        label = "tier_color_anim"
    )

    val tierAccentColor = when (mintingWindow.tier) {
        MintingAuspiciousnessTier.AUSPICIOUS -> Color(0xFFFFD700) // Solar Gold
        MintingAuspiciousnessTier.BALANCED -> Color(0xFF80FAFF)
        MintingAuspiciousnessTier.CHALLENGING -> Color(0xFFBD00FF) // Void Purple
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cyber_lunar_cycle_widget")
            .shadow(16.dp, RoundedCornerShape(16.dp), ambientColor = tierColor, spotColor = tierColor),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF070812)),
        border = BorderStroke(2.dp, tierColor.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Title & Today Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(tierColor, CircleShape)
                            .border(1.5.dp, Color.White, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (lang == "ru") "ЛУННЫЙ ОРАКУЛ ЧЕКАНКИ" else if (lang == "es") "ORÁCULO LUNAR DE ACUÑACIÓN" else "LUNAR MINTING ORACLE",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = tierColor,
                        letterSpacing = 1.2.sp
                    )
                }

                if (selectedDayIndex != 0) {
                    TextButton(
                        onClick = { selectedDayIndex = 0 },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (lang == "ru") "ВЕРНУТЬСЯ К СЕГОДНЯ ↺" else if (lang == "es") "HOY ↺" else "TODAY ↺",
                            color = NeonCyan,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(tierColor.copy(alpha = 0.15f))
                            .border(1.dp, tierColor, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (lang == "ru") "АКТИВНО СЕЙЧАС" else if (lang == "es") "EN TIEMPO REAL" else "REAL-TIME",
                            color = tierColor,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Central Display: Interactive Moon Visual Sphere & Status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF030308), RoundedCornerShape(14.dp))
                    .border(1.dp, tierColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Procedurally rendered Moon Sphere Canvas
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

                Spacer(modifier = Modifier.width(14.dp))

                // Moon Phase Info & Auspicious Status
                Column(modifier = Modifier.weight(1f)) {
                    // Date & Zodiac Sign
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = focusedDay.dateString.uppercase(),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "☽ ${focusedDay.astrologicalSign}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = tierAccentColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Phase Name & Illumination
                    Text(
                        text = focusedDay.phaseName.uppercase(),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )

                    Text(
                        text = "${if (lang == "ru") "Освещенность:" else if (lang == "es") "Iluminación:" else "Illumination:"} ${focusedDay.illumination}%",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFDDDDDD)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Auspicious/Challenging Status Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(tierColor.copy(alpha = 0.20f))
                            .border(1.5.dp, tierColor, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = mintingWindow.badgeLabel,
                            color = tierColor,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Astral Conductivity Gauge & Active Modifiers
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                // Conductivity Bar Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (lang == "ru") "АСТРАЛЬНАЯ ПРОВОДИМОСТЬ" else if (lang == "es") "CONDUCTIVIDAD ASTRAL" else "ASTRAL CONDUCTIVITY",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${(mintingWindow.astralConductivity * 100).toInt()}%",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = tierColor,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Custom Animated Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF151824))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(mintingWindow.astralConductivity)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(tierColor.copy(alpha = 0.5f), tierColor, tierAccentColor)
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Modifiers Row: Drop Rate + Gas Fee
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Drop Rate Modifier Badge
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
                            .background(Color(0xFF0B101E), RoundedCornerShape(6.dp))
                            .border(1.dp, tierColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(
                                text = if (lang == "ru") "ДРОП-РЕЙТ" else if (lang == "es") "TASA DE CAÍDA" else "DROP BOOST",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color.Gray
                            )
                            Text(
                                text = dropRateText,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = tierColor
                            )
                        }
                    }

                    // Gas / $WEED Fee Modifier
                    val gasText = if (mintingWindow.gasFeeDiscountPercent > 0) {
                        "-${mintingWindow.gasFeeDiscountPercent}% Gas (\$WEED)"
                    } else if (mintingWindow.gasFeeDiscountPercent < 0) {
                        "+${abs(mintingWindow.gasFeeDiscountPercent)}% Congestion"
                    } else {
                        "Standard 100 \$WEED"
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF0B101E), RoundedCornerShape(6.dp))
                            .border(1.dp, tierAccentColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(
                                text = if (lang == "ru") "КОМИССИЯ \$WEED" else if (lang == "es") "TARIFA \$WEED" else "GAS MODIFIER",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color.Gray
                            )
                            Text(
                                text = gasText,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = tierAccentColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Favored Elements & Patron Arcana
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (lang == "ru") "Стихии: " else if (lang == "es") "Elementos: " else "Affinity: ",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    mintingWindow.favoredElements.forEach { element ->
                        Box(
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF161C2C))
                                .border(1.dp, tierColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = element,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = tierColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Astrological Minting Ritual Lore
                Text(
                    text = mintingWindow.ritualAdvice,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFDDDDDD),
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 14-Day Forecast Timeline Strip
            Text(
                text = if (lang == "ru") "КАЛЕНДАРЬ АСТРАЛЬНЫХ ЦИКЛОВ (14 ДНЕЙ)" else if (lang == "es") "CALENDARIO LUNAR (14 DÍAS)" else "14-DAY ASTRAL TIMELINE",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(forecastList) { index, day ->
                    val isSelected = index == selectedDayIndex
                    val itemTierColor = when (day.mintingWindow.tier) {
                        MintingAuspiciousnessTier.AUSPICIOUS -> Color(0xFF00FF66)
                        MintingAuspiciousnessTier.BALANCED -> Color(0xFF00F5FF)
                        MintingAuspiciousnessTier.CHALLENGING -> Color(0xFFFF3366)
                    }

                    Box(
                        modifier = Modifier
                            .width(68.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) itemTierColor.copy(alpha = 0.25f) else Color(0xFF0C0E18)
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) itemTierColor else Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedDayIndex = index }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (index == 0) (if (lang == "ru") "СЕГОДНЯ" else "TODAY") else day.dateString.take(6),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (isSelected) Color.White else Color.Gray,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = day.phaseIcon,
                                fontSize = 22.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "${day.illumination}%",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = itemTierColor,
                                fontWeight = FontWeight.Bold
                            )

                            // Status Indicator Dot
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .size(6.dp)
                                    .background(itemTierColor, CircleShape)
                            )
                        }
                    }
                }
            }

            // Direct Call to Action Button
            if (onMintClick != null) {
                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onMintClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("lunar_mint_action_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = tierColor)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Mint",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (mintingWindow.tier == MintingAuspiciousnessTier.AUSPICIOUS) {
                            if (lang == "ru") "ЧЕКАНИТЬ С ЛУННЫМ БОНУСОМ (+18%)" else if (lang == "es") "ACUÑAR CON BONO LUNAR (+18%)" else "MINT WITH LUNAR BOOST (+18%)"
                        } else {
                            if (lang == "ru") "ЧЕКАНИТЬ АРКАН В ТЕКУЩЕЙ ФАЗЕ" else if (lang == "es") "ACUÑAR EN FASE ACTUAL" else "MINT DURING CURRENT PHASE"
                        },
                        color = Color.Black,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp
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
    val infiniteTransition = rememberInfiniteTransition(label = "lunar_corona_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val reticleAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "reticle_rotation"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val center = Offset(width * 0.5f, height * 0.5f)
        val radius = width * 0.36f

        // 1. Outer Atmospheric Corona Glow
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

        // 2. Cyber Reticle Orbital Arc
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

            // Crosshair tick marks
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

        // 3. Moon Dark Base Disk (Earthshine / Unlit hemisphere)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF242735),
                    Color(0xFF141722),
                    Color(0xFF0A0C14)
                ),
                center = center,
                radius = radius
            ),
            radius = radius,
            center = center
        )

        // 4. Subtle Craters on the Moon Disk
        val crater1 = Offset(center.x - radius * 0.35f, center.y - radius * 0.25f)
        drawCircle(color = Color(0x33000000), radius = radius * 0.18f, center = crater1)
        val crater2 = Offset(center.x + radius * 0.25f, center.y + radius * 0.35f)
        drawCircle(color = Color(0x28000000), radius = radius * 0.22f, center = crater2)
        val crater3 = Offset(center.x - radius * 0.15f, center.y + radius * 0.40f)
        drawCircle(color = Color(0x22000000), radius = radius * 0.14f, center = crater3)

        // 5. Procedural Terminator Curve & Illuminated Moon Phase
        // ageFactor: 0.0=New, 0.25=First Q, 0.5=Full, 0.75=Last Q, 1.0=New
        drawIlluminatedPhase(center, radius, ageFactor, glowColor, accentColor)

        // 6. Moon Rim Highlight / Glass Edge
        drawCircle(
            color = Color.White.copy(alpha = 0.4f),
            radius = radius,
            center = center,
            style = Stroke(width = 1.2f)
        )
    }
}

/**
 * Draws the illuminated portion of the moon based on the synodic age factor (0.0 .. 1.0).
 */
private fun DrawScope.drawIlluminatedPhase(
    center: Offset,
    radius: Float,
    ageFactor: Double,
    glowColor: Color,
    accentColor: Color
) {
    if (ageFactor < 0.02 || ageFactor > 0.98) {
        // New Moon: completely dark with subtle ethereal crescent rim
        drawCircle(
            color = glowColor.copy(alpha = 0.15f),
            radius = radius,
            center = center,
            style = Stroke(width = 1f)
        )
        return
    }

    if (ageFactor in 0.47..0.53) {
        // Full Moon: 100% radiant illumination
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFFFFD),
                    Color(0xFFF0F5FF),
                    Color(0xFFCCD8F0),
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

    // Waxing phases (0.0 .. 0.5): Right side illuminated
    // Waning phases (0.5 .. 1.0): Left side illuminated
    val isWaxing = ageFactor < 0.5
    // Normalized phase from 0 (New) to 1 (Full)
    val phaseRatio = if (isWaxing) ageFactor * 2.0 else (1.0 - ageFactor) * 2.0
    // Terminator curve width factor (-1 = New, 0 = Quarter, +1 = Full)
    val terminatorXFactor = ((phaseRatio - 0.5) * 2.0).toFloat()

    val litBrush = Brush.radialGradient(
        colors = listOf(
            Color(0xFFFFFFFF),
            Color(0xFFE8EEFA),
            Color(0xFFB0C4DE),
            glowColor.copy(alpha = 0.65f)
        ),
        center = Offset(
            center.x + (if (isWaxing) radius * 0.3f else -radius * 0.3f),
            center.y - radius * 0.2f
        ),
        radius = radius
    )

    // Build the illuminated path
    val path = Path()
    val top = Offset(center.x, center.y - radius)
    val bottom = Offset(center.x, center.y + radius)

    if (isWaxing) {
        // Right outer semicircle
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
        // Inner terminator ellipse back to top
        path.quadraticTo(
            center.x + radius * terminatorXFactor,
            center.y,
            top.x,
            top.y
        )
    } else {
        // Left outer semicircle
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
        // Inner terminator ellipse back to bottom
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
