package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.R
import com.example.data.CardLocalizationHelper
import com.example.data.CyberTarotCard
import com.example.data.CyberTarotDeck
import com.example.data.WatermarkStateStore
import com.example.ui.screens.getCardBackImage
import com.example.ui.screens.getCardBackImageForCard
import com.example.ui.screens.getCardImage
import com.example.ui.screens.isCardUnderConstruction
import com.example.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Roman numeral mapping for authentic Tarot Arcana numbering (0..30).
 */
fun getArcanaRomanNumeral(index: Int): String {
    return when (index) {
        0 -> "0"
        1 -> "I"
        2 -> "II"
        3 -> "III"
        4 -> "IV"
        5 -> "V"
        6 -> "VI"
        7 -> "VII"
        8 -> "VIII"
        9 -> "IX"
        10 -> "X"
        11 -> "XI"
        12 -> "XII"
        13 -> "XIII"
        14 -> "XIV"
        15 -> "XV"
        16 -> "XVI"
        17 -> "XVII"
        18 -> "XVIII"
        19 -> "XIX"
        20 -> "XX"
        21 -> "XXI"
        22 -> "GEN·I"
        23 -> "GEN·II"
        24 -> "GEN·III"
        25 -> "GEN·IV"
        26 -> "GEN·V"
        27 -> "AMB·I"
        28 -> "AMB·II"
        29 -> "OCN·I"
        30 -> "OCN·II"
        31 -> "GEN·Ω"
        in 32..87 -> {
            val rank = when ((index - 32) % 14) {
                0 -> "A"
                1 -> "II"
                2 -> "III"
                3 -> "IV"
                4 -> "V"
                5 -> "VI"
                6 -> "VII"
                7 -> "VIII"
                8 -> "IX"
                9 -> "X"
                10 -> "PG"
                11 -> "KN"
                12 -> "QN"
                else -> "KG"
            }
            val suitTag = when (index) {
                in 32..45 -> "GNT"
                in 46..59 -> "CNP"
                in 60..73 -> "RSN"
                else -> "ALC"
            }
            "$suitTag·$rank"
        }
        else -> "#$index"
    }
}

/**
 * The Four 420 Culture Suits / Disciplines (reimagining classic Tarot suits:
 * Pentacles/Earth -> Genetics & Soil, Wands/Fire -> Canopy & Light,
 * Cups/Water -> Resin & Extraction, Swords/Air -> Curing & Alchemy).
 */
enum class Tarot420Discipline(
    val icon: String,
    val labelEn: String,
    val labelRu: String,
    val traditionalElement: String,
    val tint: Color
) {
    GENETICS("🌱", "GENETICS", "ГЕНЕТИКА", "Earth / Pentacles", Color(0xFF00FF88)),
    CANOPY("☀", "CANOPY", "КРОНА И СВЕТ", "Fire / Wands", Color(0xFFFFB800)),
    RESIN("💧", "RESIN", "СМОЛА", "Water / Cups", Color(0xFF00F0FF)),
    ALCHEMY("✦", "ALCHEMY", "ПРОЛЕЧКА", "Air / Swords", Color(0xFFC084FC))
}

fun getCard420Discipline(cardIndex: Int): Tarot420Discipline {
    return when (cardIndex) {
        in 32..45 -> Tarot420Discipline.GENETICS
        in 46..59 -> Tarot420Discipline.CANOPY
        in 60..73 -> Tarot420Discipline.RESIN
        in 74..87 -> Tarot420Discipline.ALCHEMY
        0, 3, 5, 6, 10, 18, 21, 25 -> Tarot420Discipline.GENETICS
        4, 7, 8, 13, 15, 16, 19 -> Tarot420Discipline.CANOPY
        2, 12, 17, 27, 28, 29, 30 -> Tarot420Discipline.RESIN
        else -> Tarot420Discipline.ALCHEMY
    }
}

data class TarotSeriesTheme(
    val seriesName: String,
    val goldPrimary: Color,
    val goldDeep: Color,
    val accentColor: Color,
    val cardBgTop: Color,
    val cardBgBottom: Color,
    val badgeEn: String,
    val badgeRu: String
)

fun getTarotSeriesTheme(series: String): TarotSeriesTheme {
    return when (series) {
        "Genesis Series" -> TarotSeriesTheme(
            seriesName = series,
            goldPrimary = Color(0xFFFFE57F),
            goldDeep = Color(0xFF9C7A24),
            accentColor = Color(0xFFC084FC),
            cardBgTop = Color(0xFF140B29),
            cardBgBottom = Color(0xFF070510),
            badgeEn = "GENESIS ARCANA",
            badgeRu = "ГЕНЕЗИС"
        )
        "Amber Series" -> TarotSeriesTheme(
            seriesName = series,
            goldPrimary = Color(0xFFFFD56B),
            goldDeep = Color(0xFFA65E0A),
            accentColor = Color(0xFFFF9900),
            cardBgTop = Color(0xFF1F1105),
            cardBgBottom = Color(0xFF0B0602),
            badgeEn = "AMBER RESIN",
            badgeRu = "ЯНТАРЬ"
        )
        "Seas & Oceans" -> TarotSeriesTheme(
            seriesName = series,
            goldPrimary = Color(0xFFE0F7FA),
            goldDeep = Color(0xFF2D7A8C),
            accentColor = Color(0xFF00F0FF),
            cardBgTop = Color(0xFF051822),
            cardBgBottom = Color(0xFF02090E),
            badgeEn = "ABYSSAL HYDRO",
            badgeRu = "ОКЕАН"
        )
        else -> TarotSeriesTheme(
            seriesName = "Cyber Botanica",
            goldPrimary = Color(0xFFFFDF73),
            goldDeep = Color(0xFF8C6D1F),
            accentColor = Color(0xFF00FF88),
            cardBgTop = Color(0xFF0A1712),
            cardBgBottom = Color(0xFF050A08),
            badgeEn = "MAJOR 420 ARCANA",
            badgeRu = "АРКАН 420"
        )
    }
}

fun getCardFrameTheme(cardIndex: Int, series: String): TarotSeriesTheme {
    if (cardIndex == CyberTarotDeck.GENESIS_ELIMINATOR_INDEX) {
        return getTarotSeriesTheme("Genesis Series").copy(
            accentColor = NeonGold,
            badgeEn = "GENESIS ELIMINATOR",
            badgeRu = "ГЕНЕЗИС"
        )
    }
    if (series != "Cyber Botanica" || cardIndex < 0) {
        return getTarotSeriesTheme(series)
    }
    val rawBase = getTarotSeriesTheme("Cyber Botanica")
    val base = if (cardIndex in 32..87) {
        rawBase.copy(badgeEn = "MINOR 420 ARCANA", badgeRu = "МЛАДШИЙ АРКАН")
    } else {
        rawBase
    }
    return when (getCard420Discipline(cardIndex)) {
        Tarot420Discipline.GENETICS -> base.copy(
            accentColor = Tarot420Discipline.GENETICS.tint
        )
        Tarot420Discipline.CANOPY -> {
            val amber = getTarotSeriesTheme("Amber Series")
            base.copy(
                goldPrimary = NeonAmber,
                goldDeep = amber.goldDeep,
                accentColor = Tarot420Discipline.CANOPY.tint,
                cardBgTop = amber.cardBgTop,
                cardBgBottom = amber.cardBgBottom
            )
        }
        Tarot420Discipline.RESIN -> {
            val ocean = getTarotSeriesTheme("Seas & Oceans")
            base.copy(
                goldPrimary = ocean.goldPrimary,
                goldDeep = ocean.goldDeep,
                accentColor = Tarot420Discipline.RESIN.tint,
                cardBgTop = ocean.cardBgTop,
                cardBgBottom = ocean.cardBgBottom
            )
        }
        Tarot420Discipline.ALCHEMY -> {
            val genesis = getTarotSeriesTheme("Genesis Series")
            base.copy(
                goldPrimary = genesis.goldPrimary,
                goldDeep = genesis.goldDeep,
                accentColor = Tarot420Discipline.ALCHEMY.tint,
                cardBgTop = genesis.cardBgTop,
                cardBgBottom = genesis.cardBgBottom
            )
        }
    }
}

/**
 * Art Nouveau & Gaudi Trencadis Filigree border & suit-specific corner flourish overlay drawn via Canvas.
 * Unifies all 32 cards + all suit & series card backs into one unmistakable Tarot deck.
 */
@Composable
fun TarotArtNouveauFrameOverlay(
    seriesTheme: TarotSeriesTheme,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    accentOverride: Color? = null,
    discipline: Tarot420Discipline? = null
) {
    val accent = accentOverride ?: seriesTheme.accentColor
    val gold = seriesTheme.goldPrimary

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val inset = if (compact) 4.dp.toPx() else 7.dp.toPx()
        val cornerLen = (w.coerceAtMost(h) * (if (compact) 0.14f else 0.16f)).coerceAtLeast(8.dp.toPx())
        val strokeThin = if (compact) 0.9.dp.toPx() else 1.25.dp.toPx()
        val strokeBold = if (compact) 1.4.dp.toPx() else 2.0.dp.toPx()

        // Outer hairline metallic frame matching suit/series palette
        drawRoundRect(
            brush = Brush.linearGradient(
                listOf(gold.copy(alpha = 0.82f), accent.copy(alpha = 0.65f), gold.copy(alpha = 0.82f))
            ),
            topLeft = Offset(inset, inset),
            size = Size(w - inset * 2, h - inset * 2),
            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
            style = Stroke(width = strokeThin)
        )

        // Helper for Gaudi Art Nouveau corner scroll + suit-matched geometric emblem
        fun drawCornerFlourish(cx: Float, cy: Float, dirX: Float, dirY: Float) {
            val l = cornerLen
            val path = Path().apply {
                moveTo(cx + dirX * l, cy)
                quadraticTo(cx, cy, cx, cy + dirY * l)
            }
            drawPath(
                path = path,
                color = gold.copy(alpha = 0.92f),
                style = Stroke(width = strokeBold, cap = StrokeCap.Round)
            )
            // Inner curling leaf tendril
            val tendril = Path().apply {
                moveTo(cx + dirX * (l * 0.65f), cy + dirY * (l * 0.18f))
                quadraticTo(
                    cx + dirX * (l * 0.25f),
                    cy + dirY * (l * 0.25f),
                    cx + dirX * (l * 0.18f),
                    cy + dirY * (l * 0.65f)
                )
            }
            drawPath(
                path = tendril,
                color = accent.copy(alpha = 0.78f),
                style = Stroke(width = strokeThin, cap = StrokeCap.Round)
            )

            val jx = cx + dirX * (l * 0.17f)
            val jy = cy + dirY * (l * 0.17f)
            val r = if (compact) 2.0.dp.toPx() else 3.0.dp.toPx()

            when (discipline) {
                Tarot420Discipline.CANOPY -> {
                    // Solar ray burst + amber jewel
                    drawLine(gold, Offset(jx, jy), Offset(jx + dirX * r * 1.4f, jy + dirY * r * 1.4f), strokeThin)
                    drawCircle(color = accent, radius = r * 0.85f, center = Offset(jx, jy))
                }
                Tarot420Discipline.RESIN -> {
                    // Concentric crystal droplet halo
                    drawCircle(color = accent.copy(alpha = 0.85f), radius = r * 1.25f, center = Offset(jx, jy), style = Stroke(width = strokeThin))
                    drawCircle(color = gold, radius = r * 0.55f, center = Offset(jx, jy))
                }
                Tarot420Discipline.ALCHEMY -> {
                    // Astrolabe four-point star diamond
                    val star = Path().apply {
                        moveTo(jx, jy - r * 1.2f)
                        lineTo(jx + r * 1.1f, jy)
                        lineTo(jx, jy + r * 1.2f)
                        lineTo(jx - r * 1.1f, jy)
                        close()
                    }
                    drawPath(star, color = accent.copy(alpha = 0.9f))
                    drawCircle(color = gold, radius = r * 0.4f, center = Offset(jx, jy))
                }
                else -> {
                    // GENETICS or default: Earth pentacle seed stud
                    drawCircle(color = gold, radius = r, center = Offset(jx, jy))
                    drawCircle(color = accent.copy(alpha = 0.85f), radius = r * 0.5f, center = Offset(jx, jy))
                }
            }
        }

        val cOff = inset + 2.dp.toPx()
        drawCornerFlourish(cOff, cOff, 1f, 1f)
        drawCornerFlourish(w - cOff, cOff, -1f, 1f)
        drawCornerFlourish(cOff, h - cOff, 1f, -1f)
        drawCornerFlourish(w - cOff, h - cOff, -1f, -1f)
    }
}

/**
 * Translucent diagonal security watermark stamped across the illustration window of every card.
 * Keeps underlying artwork crisp while making "WEED GURU PROPERTY" unmistakable across the entire deck.
 */
@Composable
fun WeedGuruDiagonalWatermarkOverlay(
    modifier: Modifier = Modifier,
    goldColor: Color = NeonGold,
    accentColor: Color = NeonGreen,
    compact: Boolean = false
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .rotate(-18f)
                .border(
                    width = if (compact) 0.7.dp else 1.dp,
                    color = goldColor.copy(alpha = 0.30f),
                    shape = RoundedCornerShape(6.dp)
                )
                .background(Color.Black.copy(alpha = 0.16f), RoundedCornerShape(6.dp))
                .padding(
                    horizontal = if (compact) 6.dp else 12.dp,
                    vertical = if (compact) 2.dp else 4.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "WEED GURU PROPERTY",
                    fontFamily = OrbitronFontFamily,
                    fontSize = if (compact) 7.sp else 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = if (compact) 0.8.sp else 1.6.sp,
                    color = goldColor.copy(alpha = 0.36f),
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
                if (!compact) {
                    Text(
                        text = "420 TAROT ARCHIVE SEAL",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp,
                        color = accentColor.copy(alpha = 0.30f),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Stylized 7-leaflet botanical cannabis leaf drawn procedurally on Compose Canvas.
 * Used along fullscreen watermark edges and flanking the WEED GURU PROPERTY stamp.
 */
@Composable
fun CannabisLeafGlyph(
    modifier: Modifier = Modifier,
    fillColor: Color = NeonGreen,
    strokeColor: Color = NeonGold,
    alpha: Float = 0.92f
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val base = Offset(w * 0.50f, h * 0.72f)

        val leaflets = listOf(
            Triple(0f, 0.64f, 0.11f),     // Center spear
            Triple(-32f, 0.54f, 0.095f), // Upper-left
            Triple(32f, 0.54f, 0.095f),  // Upper-right
            Triple(-63f, 0.42f, 0.082f), // Mid-left
            Triple(63f, 0.42f, 0.082f),  // Mid-right
            Triple(-96f, 0.27f, 0.062f), // Basal-left
            Triple(96f, 0.27f, 0.062f)   // Basal-right
        )

        leaflets.forEach { (angleDeg, lengthRatio, widthRatio) ->
            val len = h * lengthRatio
            val halfW = w * widthRatio
            rotate(degrees = angleDeg, pivot = base) {
                val leafPath = Path().apply {
                    moveTo(base.x, base.y)
                    cubicTo(
                        base.x - halfW, base.y - len * 0.34f,
                        base.x - halfW * 0.78f, base.y - len * 0.72f,
                        base.x, base.y - len
                    )
                    cubicTo(
                        base.x + halfW * 0.78f, base.y - len * 0.72f,
                        base.x + halfW, base.y - len * 0.34f,
                        base.x, base.y
                    )
                    close()
                }
                drawPath(
                    path = leafPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            strokeColor.copy(alpha = alpha * 0.95f),
                            fillColor.copy(alpha = alpha * 0.92f),
                            fillColor.copy(alpha = alpha * 0.78f)
                        ),
                        startY = base.y - len,
                        endY = base.y
                    )
                )
                drawPath(
                    path = leafPath,
                    color = strokeColor.copy(alpha = alpha * 0.85f),
                    style = Stroke(width = (w * 0.028f).coerceAtLeast(0.8f))
                )
                // Central golden vein
                drawLine(
                    color = strokeColor.copy(alpha = alpha * 0.78f),
                    start = base,
                    end = Offset(base.x, base.y - len * 0.86f),
                    strokeWidth = (w * 0.022f).coerceAtLeast(0.7f),
                    cap = StrokeCap.Round
                )
            }
        }

        // Curved botanical stem
        val stemPath = Path().apply {
            moveTo(base.x, base.y)
            quadraticTo(w * 0.46f, h * 0.86f, w * 0.50f, h * 0.96f)
        }
        drawPath(
            path = stemPath,
            color = strokeColor.copy(alpha = alpha * 0.9f),
            style = Stroke(width = (w * 0.048f).coerceAtLeast(1.1f), cap = StrokeCap.Round)
        )
    }
}

/**
 * Fullscreen ornate edge watermark with botanical cannabis leaves framing all edges/corners
 * so every full-bleed card front & card back displays unmistakable WEED GURU branding.
 */
@Composable
fun WeedGuruFullscreenEdgeLeavesOverlay(
    modifier: Modifier = Modifier,
    goldColor: Color = NeonGold,
    accentColor: Color = NeonGreen
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Subtle perimeter stained-glass hairline framing the fullscreen view
        Canvas(modifier = Modifier.fillMaxSize()) {
            val pad = 8.dp.toPx()
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        goldColor.copy(alpha = 0.55f),
                        accentColor.copy(alpha = 0.48f),
                        goldColor.copy(alpha = 0.55f)
                    )
                ),
                topLeft = Offset(pad, pad),
                size = Size(size.width - pad * 2, size.height - pad * 2),
                cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
                style = Stroke(width = 1.2.dp.toPx())
            )
        }

        // Top-left & Top-right corner edge leaves (positioned along the card edges below status bar)
        CannabisLeafGlyph(
            fillColor = accentColor,
            strokeColor = goldColor,
            alpha = 0.82f,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(top = 56.dp, start = 6.dp)
                .size(34.dp)
                .rotate(132f)
        )
        CannabisLeafGlyph(
            fillColor = accentColor,
            strokeColor = goldColor,
            alpha = 0.82f,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 56.dp, end = 6.dp)
                .size(34.dp)
                .rotate(-132f)
        )

        // Left-center & Right-center vertical edge leaves
        CannabisLeafGlyph(
            fillColor = accentColor,
            strokeColor = goldColor,
            alpha = 0.78f,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 4.dp)
                .size(32.dp)
                .rotate(90f)
        )
        CannabisLeafGlyph(
            fillColor = accentColor,
            strokeColor = goldColor,
            alpha = 0.78f,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp)
                .size(32.dp)
                .rotate(-90f)
        )

        // Bottom-left & Bottom-right edge leaves framing the WEED GURU PROPERTY banner
        CannabisLeafGlyph(
            fillColor = accentColor,
            strokeColor = goldColor,
            alpha = 0.88f,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .navigationBarsPadding()
                .padding(bottom = 14.dp, start = 8.dp)
                .size(36.dp)
                .rotate(38f)
        )
        CannabisLeafGlyph(
            fillColor = accentColor,
            strokeColor = goldColor,
            alpha = 0.88f,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(bottom = 14.dp, end = 8.dp)
                .size(36.dp)
                .rotate(-38f)
        )
    }
}

/**
 * Universal Official Collector Watermark Stamp for all WEED GURU Tarot cards (Front & Back).
 * Flanked on both edges by stylized cannabis leaf emblems.
 */
@Composable
fun WeedGuruPropertyWatermarkStamp(
    modifier: Modifier = Modifier,
    alpha: Float = 0.78f,
    compact: Boolean = false,
    accentColor: Color = NeonGreen,
    goldColor: Color = NeonGold
) {
    val stampBrush = Brush.linearGradient(
        colors = listOf(
            goldColor.copy(alpha = alpha),
            accentColor.copy(alpha = alpha * 0.9f),
            goldColor.copy(alpha = alpha)
        )
    )

    Box(
        modifier = modifier
            .testTag("weed_guru_property_watermark")
            .clip(RoundedCornerShape(if (compact) 5.dp else 10.dp))
            .background(Color.Black.copy(alpha = (alpha * 0.80f).coerceIn(0.45f, 0.90f)))
            .border(if (compact) 0.9.dp else 1.4.dp, stampBrush, RoundedCornerShape(if (compact) 5.dp else 10.dp))
            .padding(
                horizontal = if (compact) 5.dp else 10.dp,
                vertical = if (compact) 2.dp else 5.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            CannabisLeafGlyph(
                fillColor = accentColor,
                strokeColor = goldColor,
                alpha = alpha,
                modifier = Modifier
                    .size(if (compact) 11.dp else 20.dp)
                    .rotate(-22f)
            )
            Spacer(modifier = Modifier.width(if (compact) 3.dp else 6.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "WEED GURU PROPERTY Verified Seal",
                        tint = goldColor.copy(alpha = alpha),
                        modifier = Modifier.size(if (compact) 8.dp else 13.dp)
                    )
                    Spacer(modifier = Modifier.width(if (compact) 3.dp else 4.dp))
                    Text(
                        text = "WEED GURU PROPERTY",
                        fontFamily = OrbitronFontFamily,
                        fontSize = if (compact) 7.sp else 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = if (compact) 0.6.sp else 1.2.sp,
                        color = goldColor.copy(alpha = alpha),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!compact) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = accentColor.copy(alpha = alpha),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
                if (!compact) {
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = "OFFICIAL 420 TAROT • VERIFIED GENETICS",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = Color.White.copy(alpha = (alpha * 0.92f).coerceIn(0.5f, 1f)),
                        maxLines = 1
                    )
                }
            }
            Spacer(modifier = Modifier.width(if (compact) 3.dp else 6.dp))
            CannabisLeafGlyph(
                fillColor = accentColor,
                strokeColor = goldColor,
                alpha = alpha,
                modifier = Modifier
                    .size(if (compact) 11.dp else 20.dp)
                    .rotate(22f)
            )
        }
    }
}

/**
 * Reusable Framed Tarot Artwork Window with 420 Orientation (Upright vs 180° Reversed),
 * subtle Art Nouveau inner border, and mandatory WEED GURU PROPERTY watermark stamp.
 * Used across Deck Grid, Tarot Reading, Vault, Market, and Forge previews so every card looks like one deck.
 */
@Composable
fun FramedTarotArtworkWindow(
    card: CyberTarotCard,
    modifier: Modifier = Modifier,
    isUpright: Boolean = true,
    compactWatermark: Boolean = true,
    showDiagonalWatermark: Boolean = true,
    rarityBorderColor: Color? = null,
    cornerRadius: Int = 8
) {
    val seriesTheme = remember(card.index, card.series) { getCardFrameTheme(card.index, card.series) }
    val borderColor = rarityBorderColor ?: seriesTheme.goldPrimary.copy(alpha = 0.78f)
    val imageRes = getCardImage(card.index)
    val context = LocalContext.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius.dp))
            .background(seriesTheme.cardBgBottom)
            .border(1.3.dp, borderColor, RoundedCornerShape(cornerRadius.dp))
    ) {
        if (imageRes != null) {
            val painter = if (compactWatermark) {
                rememberAsyncImagePainter(
                    model = remember(imageRes, context) {
                        ImageRequest.Builder(context)
                            .data(imageRes)
                            .size(360, 480)
                            .crossfade(false)
                            .build()
                    }
                )
            } else {
                painterResource(id = imageRes)
            }
            Image(
                painter = painter,
                contentDescription = "${card.name} - ${card.traditionalName}",
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(if (isUpright) 0f else 180f),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = card.name,
                    color = seriesTheme.accentColor,
                    fontFamily = OrbitronFontFamily,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        // Subtle inner Art Nouveau hairline framing the art plate
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(3.dp)
                .border(
                    width = 0.6.dp,
                    color = seriesTheme.goldPrimary.copy(alpha = 0.42f),
                    shape = RoundedCornerShape((cornerRadius - 2).coerceAtLeast(4).dp)
                )
        )

        // Universal Bottom Seal Stamp (always at the very bottom, never in the middle)
        WeedGuruPropertyWatermarkStamp(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 4.dp, end = 4.dp, bottom = if (compactWatermark) 2.dp else 4.dp),
            compact = compactWatermark,
            alpha = 0.86f,
            accentColor = seriesTheme.accentColor,
            goldColor = seriesTheme.goldPrimary
        )
    }
}

/**
 * Reusable Framed Series Card Back with Art Nouveau filigree frame, suit/series cartouche,
 * and universal WEED GURU PROPERTY stamp.
 */
@Composable
fun FramedTarotCardBack(
    series: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    promptLabel: String? = null,
    cardIndex: Int? = null
) {
    val seriesTheme = remember(cardIndex, series) {
        if (cardIndex != null) getCardFrameTheme(cardIndex, series) else getTarotSeriesTheme(series)
    }
    val discipline = remember(cardIndex) {
        if (cardIndex != null && cardIndex >= 0) getCard420Discipline(cardIndex) else null
    }
    val backDrawable = getCardBackImageForCard(cardIndex ?: -1, series)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(seriesTheme.cardBgBottom, RoundedCornerShape(10.dp))
    ) {
        Image(
            painter = painterResource(id = backDrawable),
            contentDescription = "$series Card Back",
            modifier = Modifier
                .fillMaxSize()
                .padding(if (compact) 4.dp else 7.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        // Subtle vignette so the gold filigree and stamp pop cleanly
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.15f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.65f)
                        )
                    )
                )
        )

        TarotArtNouveauFrameOverlay(
            seriesTheme = seriesTheme,
            compact = compact,
            discipline = discipline
        )

        // Bottom Series Cartouche + Universal WEED GURU PROPERTY Stamp at the very bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = if (compact) 4.dp else 7.dp, start = 6.dp, end = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (promptLabel != null) {
                Text(
                    text = promptLabel,
                    fontFamily = RajdhaniFontFamily,
                    fontSize = if (compact) 10.sp else 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(3.dp))
            }
            Box(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.82f), RoundedCornerShape(10.dp))
                    .border(0.9.dp, seriesTheme.goldPrimary.copy(alpha = 0.85f), RoundedCornerShape(10.dp))
                    .padding(horizontal = if (compact) 6.dp else 10.dp, vertical = if (compact) 2.dp else 3.dp)
            ) {
                Text(
                    text = series.uppercase(),
                    fontFamily = OrbitronFontFamily,
                    fontSize = if (compact) 7.sp else 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = seriesTheme.goldPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(if (compact) 3.dp else 5.dp))
            WeedGuruPropertyWatermarkStamp(
                compact = compact,
                alpha = 0.88f,
                accentColor = seriesTheme.accentColor,
                goldColor = seriesTheme.goldPrimary
            )
        }
    }
}

/**
 * Physical-style authentic collectible 420 Tarot Card component.
 * Renders a unified deck card with:
 * - Series-themed Art Nouveau double gold filigree border & corner ornaments
 * - Roman numeral, Traditional Arcana label & 420 Discipline badge
 * - Artwork window with upright / 180°-reversed Tarot rules & WEED GURU PROPERTY watermark
 * - Cultivar nameplate plaque & 420 keywords
 * - Optional rarity foil shader overlay and 3D flip to series card back
 */
@Composable
fun WeedGuruTarotCard(
    card: CyberTarotCard,
    modifier: Modifier = Modifier,
    isUpright: Boolean = true,
    isFlipped: Boolean? = null,
    initiallyFlipped: Boolean = false,
    compact: Boolean = false,
    systemLanguage: String = "en",
    showTraditionalAsPrimary: Boolean = false,
    showOrientationBadge: Boolean = false,
    showRarityShader: Boolean = false,
    glowColorOverride: Color? = null,
    mintBadgeText: String? = null,
    backPromptLabel: String? = null,
    onFlipClick: (() -> Unit)? = null,
    onCardClick: (() -> Unit)? = null
) {
    var internalFlipped by remember(card.index, initiallyFlipped) { mutableStateOf(initiallyFlipped) }
    val effectiveFlipped = isFlipped ?: internalFlipped
    val motionEnabled = rememberMotionEnabled()
    val haptic = LocalHapticFeedback.current
    val c = Cyber.colors
    val isHighTier = remember(card.defaultRarity) {
        card.defaultRarity in listOf("Epic", "Legend", "Genesis")
    }

    val breatheScale = if (motionEnabled && effectiveFlipped) {
        val infiniteTransition = rememberInfiniteTransition(label = "cardBackBreathe")
        infiniteTransition.animateFloat(
            initialValue = 1.00f,
            targetValue = 1.015f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 6000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "cardBackBreatheScale"
        ).value
    } else {
        1.0f
    }

    var wasFlipped by remember(card.index) { mutableStateOf(effectiveFlipped) }
    val revealHaloAlpha = remember(card.index) { Animatable(0f) }
    LaunchedEffect(effectiveFlipped, motionEnabled) {
        if (wasFlipped && !effectiveFlipped && isHighTier) {
            com.example.audio.CyberNoirSynthPlayer.triggerFX(
                com.example.audio.CyberNoirSynthPlayer.FXType.SUCCESS_CHIME
            )
            if (motionEnabled) {
                revealHaloAlpha.snapTo(0.88f)
                revealHaloAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing)
                )
            }
        }
        wasFlipped = effectiveFlipped
    }

    val rotationY by animateFloatAsState(
        targetValue = if (effectiveFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = if (motionEnabled) 480 else 0, easing = FastOutSlowInEasing),
        label = "cardFlipAnimation"
    )

    val seriesTheme = remember(card.index, card.series) { getCardFrameTheme(card.index, card.series) }
    val discipline = remember(card.index) { getCard420Discipline(card.index) }

    val rarityColor = remember(card.defaultRarity) {
        when (card.defaultRarity) {
            "Genesis" -> NeonGold
            "Legend" -> NeonAmber
            "Epic" -> NeonMagenta
            "Rare" -> NeonCyan
            else -> NeonGreen
        }
    }
    val glowColor = glowColorOverride ?: rarityColor

    val localizedName = remember(card.index, systemLanguage) {
        CardLocalizationHelper.getLocalizedName(card.index, systemLanguage)
    }
    val localizedTraditional = remember(card.index, systemLanguage) {
        CardLocalizationHelper.getLocalizedTraditionalName(card.index, systemLanguage)
    }
    val primaryTitle = if (showTraditionalAsPrimary) localizedTraditional else localizedName
    val secondaryTitle = if (showTraditionalAsPrimary) localizedName else localizedTraditional

    val goldBevelBorder = Brush.linearGradient(
        colors = listOf(
            seriesTheme.goldPrimary,
            seriesTheme.goldDeep,
            (glowColorOverride ?: seriesTheme.accentColor).copy(alpha = 0.85f),
            seriesTheme.goldDeep,
            seriesTheme.goldPrimary
        )
    )

    val shape = if (compact) CyberShapes.cardCompact else CyberShapes.card

    Box(
        modifier = modifier
            .aspectRatio(CARD_ASPECT)
            .graphicsLayer {
                this.rotationY = rotationY
                this.scaleX = breatheScale
                this.scaleY = breatheScale
                cameraDistance = 12f * density
            }
            .shadow(
                elevation = if (compact) 6.dp else 12.dp,
                shape = shape,
                ambientColor = glowColor,
                spotColor = glowColor
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(listOf(seriesTheme.cardBgTop, seriesTheme.cardBgBottom))
            )
            .border(if (compact) 1.8.dp else 2.5.dp, goldBevelBorder, shape)
            .clickable(role = Role.Button) {
                haptic.performHapticFeedback(
                    if (isHighTier) {
                        HapticFeedbackType.LongPress
                    } else {
                        HapticFeedbackType.TextHandleMove
                    }
                )
                com.example.audio.CyberNoirSynthPlayer.triggerFX(
                    com.example.audio.CyberNoirSynthPlayer.FXType.CARD_DRAW
                )
                if (onCardClick != null) {
                    onCardClick()
                } else {
                    internalFlipped = !internalFlipped
                }
            }
            .testTag("tarot_card_${card.index}")
    ) {
        if (rotationY <= 90f) {
            // FRONT OF CARD
            Box(modifier = Modifier.fillMaxSize()) {
                if (revealHaloAlpha.value > 0.01f) {
                    val pulseProgress = 1f - revealHaloAlpha.value
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val haloRadius = (size.minDimension * (0.28f + pulseProgress * 0.42f))
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    glowColor.copy(alpha = revealHaloAlpha.value * 0.42f),
                                    seriesTheme.goldPrimary.copy(alpha = revealHaloAlpha.value * 0.65f),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = haloRadius
                            ),
                            radius = haloRadius,
                            center = center
                        )
                        drawCircle(
                            color = seriesTheme.goldPrimary.copy(alpha = revealHaloAlpha.value),
                            radius = haloRadius * 0.85f,
                            center = center,
                            style = Stroke(width = 2.5.dp.toPx())
                        )
                    }
                }
                if (showRarityShader) {
                    CyberRarityShaderOverlay(
                        rarity = card.defaultRarity,
                        modifier = Modifier.fillMaxSize(),
                        isAnimated = motionEnabled && !compact
                    )
                }

                // Art Nouveau & Gaudi suit-matched corner filigree frame
                TarotArtNouveauFrameOverlay(
                    seriesTheme = seriesTheme,
                    compact = compact,
                    accentOverride = glowColorOverride ?: seriesTheme.accentColor,
                    discipline = discipline
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(if (compact) 7.dp else 11.dp)
                ) {
                    // 1. TOP HEADER RIBBON: Roman Numeral + Discipline / Traditional Arcana + Rarity Gem / Flip Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.72f), RoundedCornerShape(6.dp))
                            .border(
                                0.8.dp,
                                seriesTheme.goldPrimary.copy(alpha = 0.65f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(
                                horizontal = if (compact) 5.dp else 8.dp,
                                vertical = if (compact) 2.dp else 4.dp
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = getArcanaRomanNumeral(card.index),
                            fontFamily = OrbitronFontFamily,
                            fontSize = if (compact) 9.sp else 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = seriesTheme.goldPrimary,
                            maxLines = 1
                        )

                        Text(
                            text = if (compact) {
                                "${discipline.icon} ${mintBadgeText ?: card.defaultRarity.uppercase()}"
                            } else {
                                "${discipline.icon} ${secondaryTitle.uppercase()}"
                            },
                            fontFamily = RajdhaniFontFamily,
                            fontSize = if (compact) 9.sp else 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp),
                            textAlign = TextAlign.Center
                        )

                        if (onFlipClick != null) {
                            Box(
                                modifier = Modifier
                                    .size(if (compact) 18.dp else 22.dp)
                                    .clip(CircleShape)
                                    .background(seriesTheme.goldPrimary.copy(alpha = 0.16f))
                                    .border(0.7.dp, seriesTheme.goldPrimary.copy(alpha = 0.6f), CircleShape)
                                    .clickable { onFlipClick() }
                                    .testTag("tarot_card_flip_${card.index}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.RotateRight,
                                    contentDescription = "Flip Card",
                                    tint = seriesTheme.goldPrimary,
                                    modifier = Modifier.size(if (compact) 11.dp else 13.dp)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(if (compact) 6.dp else 8.dp)
                                    .clip(CircleShape)
                                    .background(rarityColor)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(if (compact) 4.dp else 6.dp))

                    // 2. CENTRAL ARTWORK PLATE + ORIENTATION + WEED GURU PROPERTY STAMP
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        FramedTarotArtworkWindow(
                            card = card,
                            isUpright = isUpright,
                            compactWatermark = compact,
                            showDiagonalWatermark = true,
                            rarityBorderColor = seriesTheme.goldPrimary.copy(alpha = 0.78f),
                            modifier = Modifier.fillMaxSize()
                        )

                        if (showOrientationBadge) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 4.dp)
                                    .background(
                                        Color.Black.copy(alpha = 0.80f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        0.8.dp,
                                        if (isUpright) NeonGreen else NeonRed,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 7.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = if (isUpright) {
                                        if (systemLanguage == "ru") "▲ ПРЯМАЯ" else "▲ UPRIGHT"
                                    } else {
                                        if (systemLanguage == "ru") "▼ ПЕРЕВЁРНУТА" else "▼ REVERSED"
                                    },
                                    fontFamily = OrbitronFontFamily,
                                    fontSize = if (compact) 7.sp else 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUpright) NeonGreen else NeonRed,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(if (compact) 4.dp else 6.dp))

                    // 3. BOTTOM TAROT CARTOUCHE PLAQUE: Title & 420 Keywords / Subtitle
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.80f), RoundedCornerShape(6.dp))
                            .border(
                                0.8.dp,
                                seriesTheme.goldPrimary.copy(alpha = 0.68f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(
                                horizontal = if (compact) 5.dp else 8.dp,
                                vertical = if (compact) 3.dp else 5.dp
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = primaryTitle.uppercase(),
                            fontFamily = PhilosopherFontFamily,
                            fontSize = if (compact) 10.sp else 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.4.sp,
                            color = if (isUpright) seriesTheme.accentColor else NeonAmber,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                        if (!compact) {
                            Spacer(modifier = Modifier.height(1.dp))
                            val subline = if (showOrientationBadge) {
                                (if (isUpright) card.uprightKeywords else card.reversedKeywords)
                                    .take(2).joinToString(" • ")
                            } else {
                                "${card.series} • ${discipline.icon} ${if (systemLanguage == "ru") discipline.labelRu else discipline.labelEn}"
                            }
                            Text(
                                text = subline,
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = c.textHigh,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            Text(
                                text = secondaryTitle,
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                color = c.textMid,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        } else {
            // BACK OF CARD (Flipped)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { this.rotationY = 180f }
            ) {
                FramedTarotCardBack(
                    series = card.series,
                    compact = compact,
                    promptLabel = backPromptLabel,
                    cardIndex = card.index
                )
                if (onFlipClick != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(if (compact) 7.dp else 10.dp)
                            .size(if (compact) 20.dp else 24.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.78f))
                            .border(0.9.dp, seriesTheme.goldPrimary, CircleShape)
                            .clickable { onFlipClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.RotateRight,
                            contentDescription = "Flip Back to Front",
                            tint = seriesTheme.goldPrimary,
                            modifier = Modifier.size(if (compact) 12.dp else 14.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * True edge-to-edge, borderless fullscreen card viewer (Front ⇄ Back / Рубашка)
 * with WEED GURU PROPERTY watermark anchored at the very bottom.
 */
@Composable
fun WeedGuruFullscreenCardViewer(
    card: CyberTarotCard,
    isUpright: Boolean = true,
    initiallyFlipped: Boolean = false,
    systemLanguage: String = "en",
    showOrientation: Boolean = false,
    mintBadgeText: String? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var isFlipped by remember(card.index, initiallyFlipped) { mutableStateOf(initiallyFlipped) }
    var showLoreSheet by remember { mutableStateOf(false) }
    val motionEnabled = rememberMotionEnabled()
    val haptic = LocalHapticFeedback.current

    val rotationY by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = if (motionEnabled) 480 else 0, easing = FastOutSlowInEasing),
        label = "fullscreenCardFlip"
    )

    val seriesTheme = remember(card.index, card.series) { getCardFrameTheme(card.index, card.series) }
    val discipline = remember(card.index) { getCard420Discipline(card.index) }
    val localizedName = remember(card.index, systemLanguage) {
        CardLocalizationHelper.getLocalizedName(card.index, systemLanguage)
    }
    val localizedTraditional = remember(card.index, systemLanguage) {
        CardLocalizationHelper.getLocalizedTraditionalName(card.index, systemLanguage)
    }
    val localizedDesc = remember(card.index, systemLanguage) {
        CardLocalizationHelper.getLocalizedDescription(card.index, systemLanguage)
    }
    val localizedCircuitry = remember(card.index, systemLanguage) {
        CardLocalizationHelper.getLocalizedCircuitryElement(card.index, systemLanguage)
    }
    val localizedAdvice = remember(card.index, isUpright, systemLanguage) {
        if (isUpright) {
            CardLocalizationHelper.getLocalizedUprightAdvice(card.index, systemLanguage)
        } else {
            CardLocalizationHelper.getLocalizedReversedAdvice(card.index, systemLanguage)
        }
    }

    val frontRes = getCardImage(card.index)
    val backRes = getCardBackImageForCard(card.index, card.series)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isWatermarkUnlocked by WatermarkStateStore.isUnlocked.collectAsStateWithLifecycle()
    val isWatermarkHidden by WatermarkStateStore.isWatermarkHidden.collectAsStateWithLifecycle()
    val showWatermark = !(isWatermarkUnlocked && isWatermarkHidden)
    var showWatermarkUpgradeDialog by remember { mutableStateOf(false) }
    var upgradeFeedback by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .testTag("fullscreen_card_viewer")
        ) {
            // Fullscreen Flippable Card Surface (Front <-> Card Back / Рубашка)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        this.rotationY = rotationY
                        cameraDistance = 14f * density
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isFlipped = !isFlipped
                        com.example.audio.CyberNoirSynthPlayer.triggerFX(
                            com.example.audio.CyberNoirSynthPlayer.FXType.CARD_DRAW
                        )
                    }
                    .testTag("fullscreen_card_surface"),
                contentAlignment = Alignment.Center
            ) {
                if (rotationY <= 90f) {
                    // FRONT FACE
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (frontRes != null) {
                            Image(
                                painter = painterResource(id = frontRes),
                                contentDescription = "$localizedName - $localizedTraditional",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .rotate(if (isUpright) 0f else 180f),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = localizedName,
                                    color = seriesTheme.accentColor,
                                    fontFamily = OrbitronFontFamily,
                                    fontSize = 22.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        if (isCardUnderConstruction(card.index)) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .statusBarsPadding()
                                    .padding(top = 66.dp, start = 14.dp)
                                    .background(Color.Black.copy(alpha = 0.78f), RoundedCornerShape(10.dp))
                                    .border(1.dp, NeonAmber.copy(alpha = 0.85f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (systemLanguage == "ru") "🚧 В РАЗРАБОТКЕ • ЕДИНЫЙ СТИЛЬ" else "🚧 UNDER CONSTRUCTION • REWORK",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = OrbitronFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonAmber
                                )
                            }
                        }

                        if (showWatermark) {
                            WeedGuruFullscreenEdgeLeavesOverlay(
                                goldColor = seriesTheme.goldPrimary,
                                accentColor = seriesTheme.accentColor
                            )

                            // Subtle bottom gradient for watermark legibility
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f))
                                        )
                                    )
                            )

                            // Watermark strictly at the VERY BOTTOM with cannabis leaves on edges
                            WeedGuruPropertyWatermarkStamp(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .navigationBarsPadding()
                                    .padding(bottom = 10.dp),
                                compact = false,
                                alpha = 0.90f,
                                accentColor = seriesTheme.accentColor,
                                goldColor = seriesTheme.goldPrimary
                            )
                        }
                    }
                } else {
                    // CARD BACK / РУБАШКА
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { this.rotationY = 180f }
                    ) {
                        Image(
                            painter = painterResource(id = backRes),
                            contentDescription = "${card.series} Card Back",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )

                        if (showWatermark) {
                            WeedGuruFullscreenEdgeLeavesOverlay(
                                goldColor = seriesTheme.goldPrimary,
                                accentColor = seriesTheme.accentColor
                            )

                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.78f))
                                        )
                                    )
                            )

                            // Series badge + Watermark strictly at the VERY BOTTOM of the card back
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .navigationBarsPadding()
                                    .padding(bottom = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (card.index == CyberTarotDeck.GENESIS_ELIMINATOR_INDEX) {
                                        "GENESIS OF ELEMENTS"
                                    } else {
                                        card.series.uppercase()
                                    },
                                    fontFamily = OrbitronFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp,
                                    color = seriesTheme.goldPrimary
                                )
                                Spacer(modifier = Modifier.height(5.dp))
                                WeedGuruPropertyWatermarkStamp(
                                    compact = false,
                                    alpha = 0.92f,
                                    accentColor = seriesTheme.accentColor,
                                    goldColor = seriesTheme.goldPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Minimal Floating Top Overlay HUD (title pill + watermark toggle + flip + lore + close)
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .background(Color.Black.copy(alpha = 0.56f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = buildString {
                            append(getArcanaRomanNumeral(card.index))
                            append(" • ")
                            append(localizedName.uppercase())
                            if (mintBadgeText != null) {
                                append(" $mintBadgeText")
                            }
                        },
                        fontFamily = OrbitronFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (showOrientation) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isUpright) "▲" else "▼",
                            fontSize = 11.sp,
                            color = if (isUpright) NeonGreen else NeonRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (isWatermarkUnlocked) {
                                WatermarkStateStore.setWatermarkHidden(!isWatermarkHidden, context)
                            } else {
                                upgradeFeedback = null
                                showWatermarkUpgradeDialog = true
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.Black.copy(alpha = 0.58f), CircleShape)
                            .border(
                                width = 1.dp,
                                color = if (isWatermarkUnlocked) NeonGold.copy(alpha = 0.85f) else NeonGreen.copy(alpha = 0.55f),
                                shape = CircleShape
                            )
                            .testTag("fullscreen_watermark_toggle")
                    ) {
                        Icon(
                            imageVector = when {
                                !isWatermarkUnlocked -> Icons.Default.AutoAwesome
                                isWatermarkHidden -> Icons.Default.Visibility
                                else -> Icons.Default.VisibilityOff
                            },
                            contentDescription = when {
                                !isWatermarkUnlocked -> if (systemLanguage == "ru") "Убрать вотермарку (Genesis $5.99)" else "Remove Watermark (Genesis $5.99)"
                                isWatermarkHidden -> if (systemLanguage == "ru") "Показать вотермарку" else "Show watermark"
                                else -> if (systemLanguage == "ru") "Скрыть вотермарку" else "Hide watermark"
                            },
                            tint = if (isWatermarkUnlocked) seriesTheme.goldPrimary else NeonGold
                        )
                    }

                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isFlipped = !isFlipped
                            com.example.audio.CyberNoirSynthPlayer.triggerFX(
                                com.example.audio.CyberNoirSynthPlayer.FXType.CARD_DRAW
                            )
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.Black.copy(alpha = 0.58f), CircleShape)
                            .testTag("fullscreen_flip_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.RotateRight,
                            contentDescription = if (isFlipped) {
                                if (systemLanguage == "ru") "Показать лицо карты" else "Show card front"
                            } else {
                                if (systemLanguage == "ru") "Показать рубашку" else "Show card back"
                            },
                            tint = if (isFlipped) seriesTheme.goldPrimary else NeonCyan
                        )
                    }

                    IconButton(
                        onClick = { showLoreSheet = !showLoreSheet },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.Black.copy(alpha = 0.58f), CircleShape)
                            .testTag("fullscreen_info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = if (systemLanguage == "ru") "Описание карты" else "Card details",
                            tint = if (showLoreSheet) seriesTheme.accentColor else Color.White
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.Black.copy(alpha = 0.58f), CircleShape)
                            .testTag("fullscreen_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (systemLanguage == "ru") "Закрыть" else "Close",
                            tint = Color.White
                        )
                    }
                }
            }

            // Optional floating action pill (for Market buy / Vault NFT actions) above bottom watermark
            if (actionLabel != null && onActionClick != null && !showLoreSheet) {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onActionClick()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = seriesTheme.goldPrimary.copy(alpha = 0.92f),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 66.dp)
                        .heightIn(min = 48.dp)
                        .testTag("fullscreen_action_button")
                ) {
                    Text(
                        text = actionLabel,
                        fontFamily = Exo2FontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (showWatermarkUpgradeDialog) {
                AlertDialog(
                    onDismissRequest = { showWatermarkUpgradeDialog = false },
                    containerColor = Cyber.colors.panel,
                    title = {
                        Text(
                            text = "GENESIS WATERMARK ELIMINATOR",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = NeonGold
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.card_genesis_eliminator_1795033578202),
                                    contentDescription = "Genesis Watermark Eliminator Front",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(0.75f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.5.dp, NeonGold, RoundedCornerShape(8.dp))
                                )
                                Image(
                                    painter = painterResource(id = R.drawable.back_genesis_elements_1795033577974),
                                    contentDescription = "Genesis Emergence of Life Card Back",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(0.75f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.5.dp, NeonGold, RoundedCornerShape(8.dp))
                                )
                            }
                            Text(
                                text = if (systemLanguage == "ru") {
                                    "Приобретите эксклюзивную золотую карту серии Genesis (экв. \$5.99 = 5.99 USDC или 60 WEED), чтобы открывать и скрывать полноэкранную вотермарку WEED GURU PROPERTY. Включает рубашку Genesis: Рождение Жизни (росток) из Стихий."
                                } else {
                                    "Acquire the gold-framed Genesis Watermark Eliminator card (\$5.99 eq. = 5.99 USDC or 60 WEED) to toggle the fullscreen WEED GURU PROPERTY watermark ON/OFF. Features the Genesis Elements Emergence-of-Life back."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = IbmPlexSansFontFamily,
                                color = Cyber.colors.textHigh
                            )
                            upgradeFeedback?.let { msg ->
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonRed
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            val ok = WatermarkStateStore.requestPurchase(payWithUsdc = true)
                                            if (ok) {
                                                showWatermarkUpgradeDialog = false
                                            } else {
                                                upgradeFeedback = if (systemLanguage == "ru") {
                                                    "Недостаточно USDC (нужно 5.99 USDC)"
                                                } else {
                                                    "Insufficient USDC (needs 5.99 USDC)"
                                                }
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonGold, contentColor = Color.Black),
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp)
                                ) {
                                    Text(
                                        text = "5.99 USDC ($5.99)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            val ok = WatermarkStateStore.requestPurchase(payWithUsdc = false)
                                            if (ok) {
                                                showWatermarkUpgradeDialog = false
                                            } else {
                                                upgradeFeedback = if (systemLanguage == "ru") {
                                                    "Недостаточно WEED (нужно 60 WEED)"
                                                } else {
                                                    "Insufficient WEED (needs 60 WEED)"
                                                }
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp)
                                ) {
                                    Text(
                                        text = "60 WEED",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(
                            onClick = { showWatermarkUpgradeDialog = false },
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text(if (systemLanguage == "ru") "ОТМЕНА" else "CLOSE", color = Cyber.colors.textMid)
                        }
                    }
                )
            }

            // Expandable translucent bottom sheet for 420 Tarot description & advice (hidden by default)
            AnimatedVisibility(
                visible = showLoreSheet,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Surface(
                    color = Cyber.colors.panel.copy(alpha = 0.94f),
                    shape = CyberShapes.sheet,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 18.dp, vertical = 14.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${getArcanaRomanNumeral(card.index)} • $localizedName ($localizedTraditional)",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = seriesTheme.accentColor,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${discipline.icon} ${card.defaultRarity.uppercase()}",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = seriesTheme.goldPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = localizedDesc,
                            fontFamily = IbmPlexSansFontFamily,
                            fontSize = 14.sp,
                            color = Cyber.colors.textHigh,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val activeKeywords = (if (isUpright) card.uprightKeywords else card.reversedKeywords).joinToString(" • ")
                        Text(
                            text = if (isUpright) "▲ $activeKeywords" else "▼ $activeKeywords",
                            fontFamily = Exo2FontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUpright) NeonGreen else NeonAmber
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isUpright) {
                                if (systemLanguage == "ru") "Прямое положение: $localizedAdvice" else "Upright: $localizedAdvice"
                            } else {
                                if (systemLanguage == "ru") "Перевёрнутое положение: $localizedAdvice" else "Reversed: $localizedAdvice"
                            },
                            fontFamily = IbmPlexSansFontFamily,
                            fontSize = 13.sp,
                            color = if (isUpright) NeonGreen else NeonAmber,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${if (systemLanguage == "ru") "Снаряжение / Алхимия" else "Craft / Equipment"}: $localizedCircuitry",
                            fontFamily = IbmPlexSansFontFamily,
                            fontSize = 12.sp,
                            color = Cyber.colors.textMid
                        )
                    }
                }
            }
        }
    }
}

/**
 * Full-screen Card Inspector wrapper delegating to frameless WeedGuruFullscreenCardViewer.
 */
@Composable
fun WeedGuruCardInspectorDialog(
    card: CyberTarotCard,
    isUpright: Boolean = true,
    systemLanguage: String = "en",
    onDismiss: () -> Unit
) {
    WeedGuruFullscreenCardViewer(
        card = card,
        isUpright = isUpright,
        initiallyFlipped = false,
        systemLanguage = systemLanguage,
        showOrientation = true,
        onDismiss = onDismiss
    )
}
