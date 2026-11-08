package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.layout.ContentScale

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource

import androidx.compose.foundation.Image
import com.example.R
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import com.example.ui.components.*
import com.example.viewmodel.DivinationState
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.Shadow
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.example.viewmodel.TarotViewModel
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import android.view.animation.DecelerateInterpolator
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

// Drawing organic botanical leaf veins and dew drops on component background
fun Modifier.circuitBackground(color: Color = Color(0xFF00FF66).copy(alpha = 0.08f)) = drawBehind {
    val width = size.width
    val height = size.height
    val path = Path().apply {
        // Main stem/vein curving from bottom center to top right
        moveTo(width * 0.5f, height)
        quadraticTo(width * 0.45f, height * 0.5f, width * 0.65f, 0f)

        // Side vein 1 (left upper)
        moveTo(width * 0.48f, height * 0.6f)
        quadraticTo(width * 0.25f, height * 0.45f, width * 0.10f, height * 0.35f)

        // Side vein 2 (right upper)
        moveTo(width * 0.52f, height * 0.5f)
        quadraticTo(width * 0.75f, height * 0.40f, width * 0.90f, height * 0.30f)

        // Side vein 3 (left lower)
        moveTo(width * 0.49f, height * 0.8f)
        quadraticTo(width * 0.28f, height * 0.75f, width * 0.15f, height * 0.70f)
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = 1.2.dp.toPx(),
            cap = StrokeCap.Round
        )
    )

    // Small dew drops or glowing resin glands
    drawCircle(color = color, radius = 3.5.dp.toPx(), center = Offset(width * 0.10f, height * 0.35f))
    drawCircle(color = color, radius = 3.5.dp.toPx(), center = Offset(width * 0.90f, height * 0.30f))
    drawCircle(color = color, radius = 3.5.dp.toPx(), center = Offset(width * 0.65f, 0f))
}

fun Modifier.cyberOrnaments(skin: CyberSkin): Modifier = this.drawWithContent {
    drawContent()
    
    val strokeWidth = 2.dp.toPx()
    val length = 12.dp.toPx()
    val color = skin.primaryColor
    val secColor = skin.secondaryColor
    
    when (skin) {
        CyberSkin.JAMAICA -> {
            // Sunny reggae bracket corners
            drawLine(color, Offset(0f, 0f), Offset(0f, size.height), strokeWidth)
            drawLine(color, Offset(0f, 0f), Offset(length, 0f), strokeWidth)
            drawLine(color, Offset(0f, size.height), Offset(length, size.height), strokeWidth)
            
            // Right bracket
            drawLine(color, Offset(size.width, 0f), Offset(size.width, size.height), strokeWidth)
            drawLine(color, Offset(size.width, 0f), Offset(size.width - length, 0f), strokeWidth)
            drawLine(color, Offset(size.width, size.height), Offset(size.width - length, size.height), strokeWidth)
        }
        CyberSkin.MOROCCO -> {
            // Warm amber points and subtle dashed lines
            val dotRadius = 3.dp.toPx()
            drawCircle(color, dotRadius, Offset(0f, 0f))
            drawCircle(color, dotRadius, Offset(size.width, 0f))
            drawCircle(color, dotRadius, Offset(0f, size.height))
            drawCircle(color, dotRadius, Offset(size.width, size.height))
            
            drawLine(secColor, Offset(length, 0f), Offset(size.width - length, 0f), strokeWidth / 2f)
            drawLine(secColor, Offset(length, size.height), Offset(size.width - length, size.height), strokeWidth / 2f)
        }
        CyberSkin.BARCELONA -> {
            // Sharp split-color tech corners
            drawLine(color, Offset(0f, 0f), Offset(length, 0f), strokeWidth)
            drawLine(color, Offset(0f, 0f), Offset(0f, length), strokeWidth)
            
            drawLine(secColor, Offset(size.width, size.height), Offset(size.width - length, size.height), strokeWidth)
            drawLine(secColor, Offset(size.width, size.height), Offset(size.width, size.height - length), strokeWidth)
            
            drawCircle(color, radius = 2.dp.toPx(), center = Offset(length, length))
            drawCircle(secColor, radius = 2.dp.toPx(), center = Offset(size.width - length, size.height - length))
        }
        CyberSkin.AMSTERDAM -> {
            // Rounded orchid neon orbital arcs
            drawArc(
                color = color,
                startAngle = 180f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(-length/2, -length/2),
                size = androidx.compose.ui.geometry.Size(length, length),
                style = Stroke(strokeWidth)
            )
            drawArc(
                color = color,
                startAngle = 270f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(size.width - length/2, -length/2),
                size = androidx.compose.ui.geometry.Size(length, length),
                style = Stroke(strokeWidth)
            )
            drawArc(
                color = secColor,
                startAngle = 90f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(-length/2, size.height - length/2),
                size = androidx.compose.ui.geometry.Size(length, length),
                style = Stroke(strokeWidth)
            )
            drawArc(
                color = secColor,
                startAngle = 0f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(size.width - length/2, size.height - length/2),
                size = androidx.compose.ui.geometry.Size(length, length),
                style = Stroke(strokeWidth)
            )
        }
        CyberSkin.COFFESHOP -> {
            // Cozy caramel diamonds at the corners and fine sub-border
            val dSize = 3.dp.toPx()
            fun drawCosmicDiamond(cx: Float, cy: Float, col: Color) {
                val dPath = Path().apply {
                    moveTo(cx, cy - dSize)
                    lineTo(cx + dSize, cy)
                    lineTo(cx, cy + dSize)
                    lineTo(cx - dSize, cy)
                    close()
                }
                drawPath(dPath, col)
            }
            drawCosmicDiamond(length, length, color)
            drawCosmicDiamond(size.width - length, length, color)
            drawCosmicDiamond(length, size.height - length, secColor)
            drawCosmicDiamond(size.width - length, size.height - length, secColor)
            
            drawRect(
                color = color.copy(alpha = 0.35f),
                topLeft = Offset(3.dp.toPx(), 3.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(size.width - 6.dp.toPx(), size.height - 6.dp.toPx()),
                style = Stroke(1.dp.toPx())
            )
        }
        CyberSkin.BUDS -> {
            // Rich botanical/celestial cross stars at corners
            fun drawStarCross(cx: Float, cy: Float, col: Color) {
                drawLine(col, Offset(cx - 5.dp.toPx(), cy), Offset(cx + 5.dp.toPx(), cy), 1.5.dp.toPx())
                drawLine(col, Offset(cx, cy - 5.dp.toPx()), Offset(cx, cy + 5.dp.toPx()), 1.5.dp.toPx())
            }
            drawStarCross(length, length, color)
            drawStarCross(size.width - length, length, color)
            drawStarCross(length, size.height - length, secColor)
            drawStarCross(size.width - length, size.height - length, secColor)
        }
        CyberSkin.GAUDI_VANGOGH -> {
            // Elegant organic Gaudi curves and Van Gogh starry arcs
            drawArc(
                color = color,
                startAngle = 180f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(-length / 2, -length / 2),
                size = androidx.compose.ui.geometry.Size(length, length),
                style = Stroke(strokeWidth)
            )
            drawArc(
                color = color,
                startAngle = 270f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(size.width - length / 2, -length / 2),
                size = androidx.compose.ui.geometry.Size(length, length),
                style = Stroke(strokeWidth)
            )
            drawArc(
                color = secColor,
                startAngle = 90f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(-length / 2, size.height - length / 2),
                size = androidx.compose.ui.geometry.Size(length, length),
                style = Stroke(strokeWidth)
            )
            drawArc(
                color = secColor,
                startAngle = 0f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(size.width - length / 2, size.height - length / 2),
                size = androidx.compose.ui.geometry.Size(length, length),
                style = Stroke(strokeWidth)
            )
        }
        else -> {
            drawLine(color, Offset(0f, 0f), Offset(length, 0f), strokeWidth)
            drawLine(color, Offset(0f, 0f), Offset(0f, length), strokeWidth)
            drawLine(secColor, Offset(size.width, size.height), Offset(size.width - length, size.height), strokeWidth)
            drawLine(secColor, Offset(size.width, size.height), Offset(size.width, size.height - length), strokeWidth)
        }
    }
}

enum class SplashStep {
    EYE_PULSING,
    EYE_OPENING,
    CONNECT_SCREEN,
    DONE
}

@Composable
fun CyberEyeAnimation(
    openProgress: Float,
    pulseScale: Float,
    primaryColor: Color,
    secondaryColor: Color
) {
    Canvas(
        modifier = Modifier
            .size(280.dp)
            .testTag("cyber_eye_canvas")
    ) {
        val width = size.width
        val height = size.height
        val centerX = width / 2f
        val centerY = height / 2f

        // Draw background organic circular radial glow (breathing of the plant)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primaryColor.copy(alpha = 0.22f * pulseScale), Color.Transparent),
                center = Offset(centerX, centerY),
                radius = width * 0.45f
            ),
            radius = width * 0.45f
        )

        // Draw outer orbital circular rings representing the moon's grower path
        drawCircle(
            color = secondaryColor.copy(alpha = 0.25f),
            radius = width * 0.4f,
            center = Offset(centerX, centerY),
            style = Stroke(
                width = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
            )
        )

        // If the seedling is growing, draw the multi-lobe cannabis leaf
        val scale = 0.8f * openProgress + 0.1f // Ensure some base visibility
        val path = Path().apply {
            // Draw Main Central Lobe
            moveTo(centerX, centerY + 80.dp.toPx() * scale)
            quadraticTo(centerX - 35.dp.toPx() * scale, centerY - 10.dp.toPx() * scale, centerX - 10.dp.toPx() * scale, centerY - 100.dp.toPx() * scale)
            quadraticTo(centerX, centerY - 120.dp.toPx() * scale, centerX, centerY - 125.dp.toPx() * scale) // tip
            quadraticTo(centerX, centerY - 120.dp.toPx() * scale, centerX + 10.dp.toPx() * scale, centerY - 100.dp.toPx() * scale)
            quadraticTo(centerX + 35.dp.toPx() * scale, centerY - 10.dp.toPx() * scale, centerX, centerY + 80.dp.toPx() * scale)

            // Draw Left Lobe
            moveTo(centerX, centerY + 50.dp.toPx() * scale)
            quadraticTo(centerX - 60.dp.toPx() * scale, centerY + 10.dp.toPx() * scale, centerX - 90.dp.toPx() * scale, centerY - 40.dp.toPx() * scale)
            quadraticTo(centerX - 80.dp.toPx() * scale, centerY - 50.dp.toPx() * scale, centerX - 70.dp.toPx() * scale, centerY - 45.dp.toPx() * scale)
            quadraticTo(centerX - 35.dp.toPx() * scale, centerY + 10.dp.toPx() * scale, centerX, centerY + 50.dp.toPx() * scale)

            // Draw Right Lobe
            moveTo(centerX, centerY + 50.dp.toPx() * scale)
            quadraticTo(centerX + 60.dp.toPx() * scale, centerY + 10.dp.toPx() * scale, centerX + 90.dp.toPx() * scale, centerY - 40.dp.toPx() * scale)
            quadraticTo(centerX + 80.dp.toPx() * scale, centerY - 50.dp.toPx() * scale, centerX + 70.dp.toPx() * scale, centerY - 45.dp.toPx() * scale)
            quadraticTo(centerX + 35.dp.toPx() * scale, centerY + 10.dp.toPx() * scale, centerX, centerY + 50.dp.toPx() * scale)

            // Draw Bottom Left Small Lobe
            if (openProgress > 0.4f) {
                moveTo(centerX, centerY + 65.dp.toPx() * scale)
                quadraticTo(centerX - 70.dp.toPx() * scale, centerY + 60.dp.toPx() * scale, centerX - 80.dp.toPx() * scale, centerY + 20.dp.toPx() * scale)
                quadraticTo(centerX - 65.dp.toPx() * scale, centerY + 15.dp.toPx() * scale, centerX, centerY + 65.dp.toPx() * scale)
            }

            // Draw Bottom Right Small Lobe
            if (openProgress > 0.4f) {
                moveTo(centerX, centerY + 65.dp.toPx() * scale)
                quadraticTo(centerX + 70.dp.toPx() * scale, centerY + 60.dp.toPx() * scale, centerX + 80.dp.toPx() * scale, centerY + 20.dp.toPx() * scale)
                quadraticTo(centerX + 65.dp.toPx() * scale, centerY + 15.dp.toPx() * scale, centerX, centerY + 65.dp.toPx() * scale)
            }
        }

        // Fill leaf with lush plant gradient
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(primaryColor, secondaryColor.copy(alpha = 0.5f)),
                startY = centerY - 120.dp.toPx() * scale,
                endY = centerY + 80.dp.toPx() * scale
            )
        )

        // Draw leaf boundary outline
        drawPath(
            path = path,
            color = primaryColor.copy(alpha = 0.95f),
            style = Stroke(width = 2.5.dp.toPx())
        )

        // Draw organic central stems & veins
        drawLine(
            color = primaryColor.copy(alpha = 0.8f),
            start = Offset(centerX, centerY + 95.dp.toPx() * scale),
            end = Offset(centerX, centerY - 110.dp.toPx() * scale),
            strokeWidth = 2.dp.toPx()
        )

        if (openProgress > 0.3f) {
            // Draw vein branches
            drawLine(primaryColor.copy(alpha = 0.7f), Offset(centerX, centerY + 20.dp.toPx() * scale), Offset(centerX - 40.dp.toPx() * scale, centerY - 20.dp.toPx() * scale), 1.5.dp.toPx())
            drawLine(primaryColor.copy(alpha = 0.7f), Offset(centerX, centerY + 20.dp.toPx() * scale), Offset(centerX + 40.dp.toPx() * scale, centerY - 20.dp.toPx() * scale), 1.5.dp.toPx())
            drawLine(primaryColor.copy(alpha = 0.7f), Offset(centerX, centerY - 30.dp.toPx() * scale), Offset(centerX - 25.dp.toPx() * scale, centerY - 65.dp.toPx() * scale), 1.5.dp.toPx())
            drawLine(primaryColor.copy(alpha = 0.7f), Offset(centerX, centerY - 30.dp.toPx() * scale), Offset(centerX + 25.dp.toPx() * scale, centerY - 65.dp.toPx() * scale), 1.5.dp.toPx())
        }

        // Glowing golden resin droplets (trichomes) pulsing on the leaf tips!
        val trichomeGlow = secondaryColor.copy(alpha = 0.9f)
        val tRadius = 4.dp.toPx() * pulseScale

        drawCircle(trichomeGlow, tRadius, Offset(centerX, centerY - 125.dp.toPx() * scale)) // Main tip
        drawCircle(trichomeGlow, tRadius, Offset(centerX - 90.dp.toPx() * scale, centerY - 40.dp.toPx() * scale)) // Left lobe tip
        drawCircle(trichomeGlow, tRadius, Offset(centerX + 90.dp.toPx() * scale, centerY - 40.dp.toPx() * scale)) // Right lobe tip

        if (openProgress > 0.4f) {
            drawCircle(trichomeGlow, tRadius * 0.7f, Offset(centerX - 80.dp.toPx() * scale, centerY + 20.dp.toPx() * scale))
            drawCircle(trichomeGlow, tRadius * 0.7f, Offset(centerX + 80.dp.toPx() * scale, centerY + 20.dp.toPx() * scale))
        }
    }
}

fun isColorDark(color: Color): Boolean {
    val r = color.red
    val g = color.green
    val b = color.blue
    val l = 0.2126f * r + 0.7152f * g + 0.0722f * b
    return l < 0.45f
}

fun getHighContrastTextStyle(color: Color): TextStyle {
    // Dynamic contrast shadow: Light colors get a thick black shadow, Dark colors get a bright glowing shadow
    val isDark = isColorDark(color)
    val shadowColor = if (isDark) Color.White else Color.Black
    return TextStyle(
        shadow = Shadow(
            color = shadowColor,
            offset = Offset(0f, 0f),
            blurRadius = 14f
        )
    )
}

@Composable
fun ContrastText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    fontSize: TextUnit = TextUnit.Unspecified,
    lineHeight: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    Text(
        text = text,
        modifier = modifier,
        style = getHighContrastTextStyle(color).copy(
            color = color,
            fontSize = fontSize,
            lineHeight = lineHeight,
            fontWeight = fontWeight,
            fontFamily = fontFamily
        ),
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow
    )
}

@Composable
fun CyberOrnamentalHeader(title: String, skin: CyberSkin, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxWidth().padding(vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, skin.primaryColor)
                    )
                )
        )
        Icon(
            imageVector = Icons.Default.Adjust,
            contentDescription = null,
            tint = skin.primaryColor,
            modifier = Modifier.size(18.dp).padding(horizontal = 4.dp)
        )
        ContrastText(
            text = title.uppercase(),
            color = Color.White,
            fontSize = 16.sp,
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Icon(
            imageVector = Icons.Default.Adjust,
            contentDescription = null,
            tint = skin.primaryColor,
            modifier = Modifier.size(18.dp).padding(horizontal = 4.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(skin.primaryColor, Color.Transparent)
                    )
                )
        )
    }
}

@Composable
fun AutoResizingText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    fontFamily: FontFamily? = null,
    fontWeight: FontWeight? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    maxLines: Int = 1,
    targetTextSize: TextUnit = 16.sp,
    minTextSize: TextUnit = 8.sp,
    textAlign: TextAlign? = null
) {
    val finalTargetSize = targetTextSize
    val finalMinSize = minTextSize

    var textSize by remember(text, finalTargetSize) { mutableStateOf<TextUnit>(finalTargetSize) }
    var readyToDraw by remember(text) { mutableStateOf(false) }

    Text(
        text = text,
        modifier = modifier.drawWithContent {
            if (readyToDraw) drawContent()
        },
        color = color,
        fontFamily = fontFamily,
        fontWeight = fontWeight,
        letterSpacing = letterSpacing,
        maxLines = maxLines,
        fontSize = textSize,
        textAlign = textAlign,
        overflow = TextOverflow.Clip,
        style = getHighContrastTextStyle(color),
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.hasVisualOverflow && textSize.value > finalMinSize.value) {
                textSize = (textSize.value - 1f).sp
            } else {
                readyToDraw = true
            }
        }
    )
}

@Composable
fun getContrastColor(skin: CyberSkin, customColor: Color?): Color {
    return Color.White
}

fun Modifier.cyberButtonBackground(
    skin: CyberSkin,
    enabled: Boolean,
    customColor: Color? = null
): Modifier = this.drawWithContent {
    val baseColor = customColor ?: skin.primaryColor
    val secColor = skin.secondaryColor
    
    val col1 = if (enabled) baseColor else baseColor.copy(alpha = 0.4f)
    val col2 = if (enabled) secColor else secColor.copy(alpha = 0.4f)
    
    when (skin) {
        CyberSkin.JAMAICA -> {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0C1D0E), Color(0xFF1E0505))
                )
            )
            val gridSpacing = 8.dp.toPx()
            val columns = (size.width / gridSpacing).toInt()
            
            for (i in 0..columns) {
                drawLine(
                    color = col1.copy(alpha = 0.05f),
                    start = Offset(i * gridSpacing, 0f),
                    end = Offset(i * gridSpacing, size.height),
                    strokeWidth = 1f
                )
            }
            
            val bracketLength = 10.dp.toPx()
            val strokeW = 1.5.dp.toPx()
            drawLine(col1.copy(alpha = 0.4f), Offset(4.dp.toPx(), 4.dp.toPx()), Offset(4.dp.toPx(), size.height - 4.dp.toPx()), strokeW)
            drawLine(col1.copy(alpha = 0.4f), Offset(4.dp.toPx(), 4.dp.toPx()), Offset(4.dp.toPx() + bracketLength, 4.dp.toPx()), strokeW)
            drawLine(col1.copy(alpha = 0.4f), Offset(4.dp.toPx(), size.height - 4.dp.toPx()), Offset(4.dp.toPx() + bracketLength, size.height - 4.dp.toPx()), strokeW)
            
            drawLine(col1.copy(alpha = 0.4f), Offset(size.width - 4.dp.toPx(), 4.dp.toPx()), Offset(size.width - 4.dp.toPx(), size.height - 4.dp.toPx()), strokeW)
            drawLine(col1.copy(alpha = 0.4f), Offset(size.width - 4.dp.toPx(), 4.dp.toPx()), Offset(size.width - 4.dp.toPx() - bracketLength, 4.dp.toPx()), strokeW)
            drawLine(col1.copy(alpha = 0.4f), Offset(size.width - 4.dp.toPx(), size.height - 4.dp.toPx()), Offset(size.width - 4.dp.toPx() - bracketLength, size.height - 4.dp.toPx()), strokeW)
        }
        CyberSkin.MOROCCO -> {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1F120A), Color(0xFF0F0905))
                )
            )
            val slashOffset = 15.dp.toPx()
            drawCircle(col1.copy(alpha = 0.3f), radius = 2.5.dp.toPx(), center = Offset(slashOffset + 8.dp.toPx(), 6.dp.toPx()))
            drawCircle(col1.copy(alpha = 0.3f), radius = 2.5.dp.toPx(), center = Offset(size.width - slashOffset - 8.dp.toPx(), size.height - 6.dp.toPx()))
        }
        CyberSkin.BARCELONA -> {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF1D0935), Color(0xFF05020B)),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = size.width / 1.2f
                )
            )
            drawCircle(col1.copy(alpha = 0.5f), radius = 3.dp.toPx(), center = Offset(6.dp.toPx(), 6.dp.toPx()))
            drawCircle(col1.copy(alpha = 0.5f), radius = 3.dp.toPx(), center = Offset(size.width - 6.dp.toPx(), size.height - 6.dp.toPx()))
        }
        CyberSkin.AMSTERDAM -> {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF030D1A), Color(0xFF140502))
                )
            )
            drawArc(
                color = col1.copy(alpha = 0.35f),
                startAngle = 180f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(12.dp.toPx(), 12.dp.toPx()),
                style = Stroke(1.5.dp.toPx())
            )
            drawArc(
                color = col1.copy(alpha = 0.35f),
                startAngle = 0f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(size.width - 16.dp.toPx(), size.height - 16.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(12.dp.toPx(), 12.dp.toPx()),
                style = Stroke(1.5.dp.toPx())
            )
        }
        CyberSkin.COFFESHOP -> {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A0F09), Color(0xFF0A0604))
                )
            )
            val pathLeft = Path().apply {
                moveTo(0f, 0f)
                lineTo(12.dp.toPx(), 0f)
                lineTo(0f, 12.dp.toPx())
                close()
            }
            drawPath(pathLeft, col1.copy(alpha = 0.25f))
            
            val pathRight = Path().apply {
                moveTo(size.width, size.height)
                lineTo(size.width - 12.dp.toPx(), size.height)
                lineTo(size.width, size.height - 12.dp.toPx())
                close()
            }
            drawPath(pathRight, col1.copy(alpha = 0.25f))
        }
        CyberSkin.BUDS -> {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF021204), Color(0xFF04220A))
                )
            )
            val midX = size.width / 2f
            val midY = size.height / 2f
            drawCircle(col1.copy(alpha = 0.12f), radius = size.height * 0.45f, center = Offset(midX, midY), style = Stroke(1.dp.toPx()))
        }
        CyberSkin.GAUDI_VANGOGH -> {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF04060E), Color(0xFF0B1226))
                )
            )
            val midX = size.width / 2f
            val midY = size.height / 2f
            drawCircle(col1.copy(alpha = 0.18f), radius = size.height * 0.45f, center = Offset(midX, midY), style = Stroke(1.dp.toPx()))
        }
        else -> {
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(col1.copy(alpha = 0.22f), col2.copy(alpha = 0.28f))
                )
            )
        }
    }
    
    drawContent()
}

@Composable
fun CyberButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    skin: CyberSkin,
    enabled: Boolean = true,
    containerColor: Color? = null,
    contentColor: Color? = null,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(24.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    content: @Composable RowScope.() -> Unit
) {
    val defaultContent = Color.White
    
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = defaultContent,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = defaultContent.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .clip(shape)
            .cyberButtonBackground(skin, enabled, containerColor)
            .border(2.dp, defaultContent.copy(alpha = 0.85f), shape),
        contentPadding = contentPadding,
        content = content
    )
}

@Composable
fun CyberTextButton(
    onClick: () -> Unit,
    text: String,
    skin: CyberSkin,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color? = null,
    contentColor: Color? = null,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(24.dp),
    targetTextSize: TextUnit = 13.sp,
    minTextSize: TextUnit = 9.sp,
    letterSpacing: TextUnit = 0.5.sp,
    fontWeight: FontWeight = FontWeight.Bold
) {
    val doubledTarget = targetTextSize
    val doubledMin = minTextSize

    CyberButton(
        onClick = onClick,
        modifier = modifier,
        skin = skin,
        enabled = enabled,
        containerColor = containerColor,
        contentColor = Color.White,
        shape = shape
    ) {
        AutoResizingText(
            text = text,
            color = Color.White,
            fontFamily = RajdhaniFontFamily,
            fontWeight = fontWeight,
            letterSpacing = letterSpacing,
            targetTextSize = doubledTarget,
            minTextSize = doubledMin,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CyberTarotDashboard(viewModel: TarotViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    var splashStep by remember { mutableStateOf(SplashStep.CONNECT_SCREEN) }

    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val notification by viewModel.uiNotification.collectAsStateWithLifecycle()
    val isLocked by viewModel.isAppLocked.collectAsStateWithLifecycle()
    val isSafeMode by viewModel.isSafeBotanicalMode.collectAsStateWithLifecycle()
    val showBlessingsPortal by viewModel.showBlessingsPortal.collectAsStateWithLifecycle()
    var showOnboarding by remember { mutableStateOf(false) }
    val isSecured = profile?.isSecured ?: false
    val lang = profile?.systemLanguage ?: "en"

    // Detect system language and determine app title: "ШМАЛЬ" for ru & uk, "SHMALL" for all others
    val detectedSystemLang = remember { LocalizationHelper.getSystemDefaultLanguage() }
    val isRuOrUk = detectedSystemLang in listOf("ru", "uk") || lang in listOf("ru", "uk")
    val appBrandTitle = "CYBERTAROT"
    val appBrandSubtitle = "(ORACLE & COSMIC MYSTIC)"
    var splashStatusText by remember { mutableStateOf(if (isRuOrUk) "Проверка профиля BIP-39..." else "Validating BIP-39 profile...") }

    // Automatic profile verification during splash
    LaunchedEffect(splashStep, profile) {
        if (splashStep == SplashStep.CONNECT_SCREEN) {
            viewModel.updateSystemLanguage(detectedSystemLang)
            delay(1200)
            splashStatusText = if (isRuOrUk) "Синхронизация мастер-ключа..." else "Synchronizing master key..."
            delay(1000)
            val profileSecured = profile?.isSecured == true && !profile?.pinCode.isNullOrBlank()
            if (!profileSecured) {
                splashStatusText = if (isRuOrUk) "Профиль не найден. Запуск создания..." else "No profile detected. Launching setup..."
                delay(400)
                showOnboarding = true
                splashStep = SplashStep.DONE
            } else {
                splashStatusText = if (isRuOrUk) "Профиль подтвержден. Вход..." else "Profile confirmed. Unlocking..."
                delay(400)
                splashStep = SplashStep.DONE
            }
        }
    }

    // 5 Streamlined Primary Navigation Tabs: ORACLE, CARDS, ARENA, CRYPTO, SETTINGS
    val tabs = listOf(
        LocalizationHelper.getString(lang, "tab_divination") to Icons.Default.AutoAwesome,
        LocalizationHelper.getString(lang, "tab_cards") to Icons.Default.Style,
        LocalizationHelper.getString(lang, "tab_arena") to Icons.Default.Shield,
        LocalizationHelper.getString(lang, "tab_crypto") to Icons.Default.CurrencyBitcoin,
        LocalizationHelper.getString(lang, "tab_settings") to Icons.Default.Settings
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val radioState by com.example.audio.CyberRadioPlayer.state.collectAsStateWithLifecycle()

        if (isSafeMode) {
            SafeBotanicalModeScreen(viewModel)
        } else if (showOnboarding) {
            OnboardingWizard(viewModel, onComplete = {
                showOnboarding = false
                splashStep = SplashStep.DONE
            })
        } else {
            // Full screen skin wallpaper in post-impressionist Van Gogh style
            val tabBackgroundRes = when (selectedTab) {
                0 -> R.drawable.img_bg_tarot_altar
                1 -> R.drawable.img_bg_oracle_temple
                2 -> R.drawable.img_bg_synth_lab
                3 -> R.drawable.img_bg_lunar_calendar
                else -> currentSkin.backgroundResId
            }
            VanGoghGanjaBackground(
                skin = currentSkin,
                modifier = Modifier.fillMaxSize(),
                overrideBgResId = tabBackgroundRes
            )
            // Translucent overlay shroud (frosted glass/matte shading layer)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.28f)
                            )
                        )
                    )
            )

            Scaffold(
                contentWindowInsets = WindowInsets.statusBars,
                bottomBar = {
                    if (!isLocked) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Floating Cyber Radio Mini Bar if stream is active or buffering
                            if (radioState.isPlaying || radioState.isBuffering) {
                                com.example.ui.components.CyberRadioMiniBar(
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp, vertical = 4.dp),
                                    onOpenConsole = { com.example.audio.CyberRadioPlayer.openConsole() }
                                )
                            }

                            NavigationBar(
                                containerColor = Color(0xDD0A0A12), // translucent navigation bar
                                tonalElevation = 0.dp,
                                windowInsets = NavigationBarDefaults.windowInsets,
                                modifier = Modifier
                                    .testTag("bottom_nav_bar")
                                    .drawBehind {
                                        drawLine(
                                            color = currentSkin.primaryColor.copy(alpha = 0.15f),
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
                                        onClick = { selectedTab = index },
                                        icon = {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = title,
                                                tint = if (isSelected) currentSkin.primaryColor else Color(0xFFB0B0C5)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = title,
                                                fontFamily = OrbitronFontFamily,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) currentSkin.primaryColor else Color(0xFFB0B0C5),
                                                letterSpacing = 0.5.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            indicatorColor = Color.Transparent
                                        )
                                    )
                                }
                            }
                        }
                    }
                },
                containerColor = Color.Transparent // Scaffold is transparent to let wallpaper bleed through
            ) { innerPadding ->
            CompositionLocalProvider(
                LocalTextStyle provides LocalTextStyle.current.merge(
                    TextStyle(
                        shadow = Shadow(
                            color = Color(0xFF010103),
                            offset = Offset(0f, 0f),
                            blurRadius = 15f
                        )
                    )
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .circuitBackground(currentSkin.primaryColor.copy(alpha = 0.08f))
                ) {
                    // Content according to selected tab, blocked by lock screen if enabled
                    if (isLocked) {
                        PinLockScreen(viewModel, onRestoreRequest = { showOnboarding = true })
                    } else {
                        when (selectedTab) {
                            0 -> DivinationScreen(viewModel)
                            1 -> UnifiedCardsScreen(viewModel)
                            2 -> com.example.duel.DuelArenaScreen(viewModel = viewModel, onBack = { selectedTab = 0 })
                            3 -> CryptoHubScreen(viewModel)
                            4 -> SecurityScreen(viewModel)
                        }
                    }

                // Top Status Bar Overlay (User Name & Credits) - only shown when unlocked and not in duel
                if (!isLocked && selectedTab != 2) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xF00A0A12), Color(0xCC0A0A12), Color.Transparent)
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🌿 $appBrandTitle",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentSkin.primaryColor,
                            letterSpacing = 1.2.sp,
                            fontFamily = OrbitronFontFamily,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = when (selectedTab) {
                                0 -> if (isRuOrUk) "Ботанический Оракул" else "Botanical Oracle"
                                1 -> if (isRuOrUk) "Карты и Священные Колоды" else "Cards & Sacred Decks"
                                2 -> if (isRuOrUk) "Боевая Арена Дуэлей" else "Combat Duel Arena"
                                3 -> if (isRuOrUk) "Лунный Календарь и Хаб" else "Lunar Calendar & Hub"
                                else -> if (isRuOrUk) "Настройки и Безопасность" else "Settings & Security"
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeonCyan,
                            fontFamily = RajdhaniFontFamily,
                            letterSpacing = 0.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        // DI.FM Cyber Radio Console Launcher Button
                        IconButton(
                            onClick = {
                                com.example.audio.CyberRadioPlayer.toggleConsole()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    if (radioState.isPlaying) currentSkin.primaryColor.copy(alpha = 0.25f) else Color(0x10FFFFFF),
                                    CircleShape
                                )
                                .border(
                                    1.dp,
                                    if (radioState.isPlaying) currentSkin.primaryColor else Color(0x20FFFFFF),
                                    CircleShape
                                )
                                .testTag("cyber_radio_toggle_button")
                        ) {
                            if (radioState.isPlaying) {
                                Row(
                                    modifier = Modifier
                                        .width(16.dp)
                                        .height(13.dp),
                                    horizontalArrangement = Arrangement.spacedBy(1.5.dp),
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    radioState.visualizerBars.take(3).forEach { bar ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight(bar.coerceIn(0.25f, 1f))
                                                .background(currentSkin.primaryColor)
                                        )
                                    }
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Radio,
                                    contentDescription = "DI.FM Cyber Radio",
                                    tint = if (radioState.isBuffering) Color(0xFFFFB800) else Color.LightGray,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(ElegantIndigo, RoundedCornerShape(14.dp))
                                .border(1.dp, Color(0x15FFFFFF), RoundedCornerShape(14.dp))
                                .clickable { viewModel.setBlessingsPortalVisible(true) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Wallet",
                                tint = currentSkin.primaryColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${profile?.weed ?: 0} WEED",
                                color = Color.White,
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                }

                // Centralized Notification Snackbar / Banner
                notification?.let { msg ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                top = 16.dp,
                                bottom = if (radioState.isPlaying || radioState.isBuffering) 120.dp else 68.dp
                            )
                            .align(Alignment.BottomCenter)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                            border = BorderStroke(1.5.dp, currentSkin.primaryColor),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = msg,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontFamily = RajdhaniFontFamily,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { viewModel.dismissNotification() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(16.dp)
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

        if (showBlessingsPortal) {
            BlessingQuestsHubDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.setBlessingsPortalVisible(false) }
            )
        }

        if (radioState.isConsoleOpen) {
            com.example.ui.components.CyberRadioConsoleDialog(
                onDismiss = { com.example.audio.CyberRadioPlayer.closeConsole() }
            )
        }

        // Full Screen Splash / Welcome Screen overlay with static Van Gogh + Gaudi cannabis bouquet
        if (splashStep != SplashStep.DONE && !showOnboarding && !isSafeMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF020205)),
                contentAlignment = Alignment.Center
            ) {
                // Static Masterpiece: Cannabis bouquet in Van Gogh's Sunflowers style in a Gaudi mosaic vase
                Image(
                    painter = painterResource(id = R.drawable.img_splash_ganja_sunflowers),
                    contentDescription = "Van Gogh Ganja Sunflowers in Gaudi Vase",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Translucent contrast shroud & cyber vignette
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.65f),
                                    Color(0xFF04060E).copy(alpha = 0.45f),
                                    Color.Black.copy(alpha = 0.88f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 28.dp)
                        .windowInsetsPadding(WindowInsets.systemBars),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Title overlay determined dynamically after system language check
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text(
                            text = appBrandTitle,
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 46.sp,
                            color = currentSkin.primaryColor,
                            letterSpacing = 4.sp,
                            textAlign = TextAlign.Center,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color(0xFF00FF88).copy(alpha = 0.7f),
                                    offset = Offset(0f, 0f),
                                    blurRadius = 24f
                                )
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = appBrandSubtitle,
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White.copy(alpha = 0.95f),
                            letterSpacing = 2.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0x66000000),
                            border = BorderStroke(1.dp, currentSkin.primaryColor.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "CYBERPUNK • VAN GOGH • GAUDÍ",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700),
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Bottom Area: Status & Quick Action Button
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Status indicator
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xB30B0F1C),
                            border = BorderStroke(1.dp, Color(0x33FFFFFF))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = currentSkin.primaryColor
                                )
                                Text(
                                    text = splashStatusText,
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 16.sp,
                                    color = Color.LightGray
                                )
                            }
                        }

                        // Enter Button
                        CyberButton(
                            onClick = {
                                com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
                                val profileSecured = profile?.isSecured == true && !profile?.pinCode.isNullOrBlank()
                                if (!profileSecured) {
                                    showOnboarding = true
                                }
                                splashStep = SplashStep.DONE
                            },
                            skin = currentSkin,
                            modifier = Modifier
                                .fillMaxWidth(0.94f)
                                .height(64.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (isRuOrUk) "⚡ ВОЙТИ • ENTER" else "⚡ ENTER • CHILL LINK",
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Black,
                                    color = currentSkin.primaryColor,
                                    fontSize = 16.sp,
                                    letterSpacing = 2.sp,
                                    textAlign = TextAlign.Center,
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
}

/**
 * Unified Cards Hub: seamlessly toggle between Card Vault Gallery and Battle Deck Builder
 */
@Composable
fun UnifiedCardsScreen(viewModel: TarotViewModel) {
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val lang = profile?.systemLanguage ?: "en"
    val isRu = lang in listOf("ru", "uk")
    var cardSubTab by remember { mutableStateOf(0) } // 0: Vault (Collection), 1: Deck (Forge)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 54.dp)
    ) {
        // Segmented selector: Collection vs Deck
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .background(Color(0xFF0D0D18), RoundedCornerShape(12.dp))
                .border(1.dp, currentSkin.primaryColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val subTabs = if (isRu) {
                listOf("🎴 МОЯ КОЛЛЕКЦИЯ" to 0, "⚔️ БОЕВАЯ КОЛОДА" to 1)
            } else {
                listOf("🎴 CARD VAULT" to 0, "⚔️ BATTLE DECK" to 1)
            }
            subTabs.forEach { (title, idx) ->
                val isSelected = cardSubTab == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) currentSkin.primaryColor.copy(alpha = 0.2f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (isSelected) currentSkin.primaryColor else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.CLICK)
                            cardSubTab = idx
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) currentSkin.primaryColor else Color.LightGray
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (cardSubTab == 0) {
                VaultScreen(viewModel)
            } else {
                DeckScreen(viewModel)
            }
        }
    }
}

/**
 * Unified Crypto Hub: Decentralized Wallet Balances, Instant AMM Swap, NFT Market, and On-chain Ledger
 */
@Composable
fun CryptoHubScreen(viewModel: TarotViewModel) {
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val lang = profile?.systemLanguage ?: "en"
    val isRu = lang in listOf("ru", "uk")
    var cryptoTab by remember { mutableStateOf(0) } // 0: Wallet & Swap, 1: NFT Market & Mint, 2: Ledger

    // AMM DEX Swap state
    var fromToken by remember { mutableStateOf("WEED") }
    var toToken by remember { mutableStateOf("TON") }
    var swapAmountInput by remember { mutableStateOf("100") }
    var slippageTolerance by remember { mutableStateOf("0.5%") }
    val userWeed = profile?.weed ?: 0
    val userTon = profile?.balanceTon ?: 50.0
    val userEth = profile?.balanceEth ?: 0.5
    val userSol = profile?.balanceSol ?: 2.0
    val userUsdc = 250.0

    // Swap rate calculation
    val inputNum = swapAmountInput.toDoubleOrNull() ?: 0.0
    val estimatedOutput = remember(fromToken, toToken, inputNum) {
        when {
            fromToken == "WEED" && toToken == "TON" -> inputNum * 0.005
            fromToken == "WEED" && toToken == "ETH" -> inputNum * 0.00035
            fromToken == "WEED" && toToken == "USDC" -> inputNum * 0.1
            fromToken == "TON" && toToken == "WEED" -> inputNum * 200.0
            fromToken == "ETH" && toToken == "WEED" -> inputNum * 2850.0
            fromToken == "USDC" && toToken == "WEED" -> inputNum * 10.0
            else -> inputNum * 1.0
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 54.dp)
    ) {
        // Top Crypto Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .background(Color(0xFF0C0E1A), RoundedCornerShape(12.dp))
                .border(1.dp, currentSkin.primaryColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val subTabs = if (isRu) {
                listOf("💰 КОШЕЛЕК & ОБМЕН" to 0, "💎 NFT МАРКЕТ" to 1, "📜 РЕЕСТР" to 2)
            } else {
                listOf("💰 WALLET & SWAP" to 0, "💎 NFT MARKET" to 1, "📜 LEDGER" to 2)
            }
            subTabs.forEach { (title, idx) ->
                val isSelected = cryptoTab == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) currentSkin.primaryColor.copy(alpha = 0.2f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (isSelected) currentSkin.primaryColor else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.CLICK)
                            cryptoTab = idx
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) currentSkin.primaryColor else Color.LightGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        when (cryptoTab) {
            0 -> {
                // Wallet Balances & Instant AMM DEX Swap
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Balances Overview Card
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1222)),
                            border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(currentSkin.primaryColor, Color(0xFF00E5FF)))),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = if (isRu) "НЕКАСТОДИАЛЬНЫЙ КОШЕЛЕК" else "SELF-CUSTODIAL WALLET",
                                            fontFamily = RajdhaniFontFamily,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = currentSkin.primaryColor,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = "${profile?.weed ?: 0} \$WEED",
                                            fontFamily = RajdhaniFontFamily,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                    }
                                    CyberButton(
                                        onClick = {
                                            viewModel.addWeedCredits(50)
                                            com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
                                        },
                                        skin = currentSkin,
                                        modifier = Modifier.height(38.dp)
                                    ) {
                                        Text(
                                            text = if (isRu) "+50 ХАРВЕСТ 🌿" else "+50 HARVEST 🌿",
                                            fontFamily = RajdhaniFontFamily,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                HorizontalDivider(color = Color(0x22FFFFFF))

                                // Multi-token asset balances
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("TON", fontSize = 15.sp, color = Color(0xFFB0B0C5), fontFamily = RajdhaniFontFamily)
                                        Text(String.format(Locale.US, "%.1f", userTon), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0098EA), fontFamily = RajdhaniFontFamily)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("ETH", fontSize = 15.sp, color = Color(0xFFB0B0C5), fontFamily = RajdhaniFontFamily)
                                        Text(String.format(Locale.US, "%.3f", userEth), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8C8C8C), fontFamily = RajdhaniFontFamily)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("SOL", fontSize = 15.sp, color = Color(0xFFB0B0C5), fontFamily = RajdhaniFontFamily)
                                        Text(String.format(Locale.US, "%.2f", userSol), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF14F195), fontFamily = RajdhaniFontFamily)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("USDC", fontSize = 15.sp, color = Color(0xFFB0B0C5), fontFamily = RajdhaniFontFamily)
                                        Text(String.format(Locale.US, "%.1f", userUsdc), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2775CA), fontFamily = RajdhaniFontFamily)
                                    }
                                }
                            }
                        }
                    }

                    // AMM DEX Instant Swap Card
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF090D18)),
                            border = BorderStroke(1.dp, Color(0x3300FF88)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isRu) "⚡ МГНОВЕННЫЙ ОБМЕН (AMM SWAP)" else "⚡ INSTANT DEX SWAP",
                                        fontFamily = RajdhaniFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = NeonGreen
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf("0.5%", "1.0%", "2.5%").forEach { slip ->
                                            val isSelected = slippageTolerance == slip
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(if (isSelected) NeonGreen.copy(alpha = 0.2f) else Color(0xFF161A28))
                                                    .border(1.dp, if (isSelected) NeonGreen else Color.Transparent, RoundedCornerShape(4.dp))
                                                    .clickable { slippageTolerance = slip }
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(slip, fontSize = 14.sp, fontFamily = RajdhaniFontFamily, color = Color.White)
                                            }
                                        }
                                    }
                                }

                                // From Token
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF131726), RoundedCornerShape(10.dp))
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(if (isRu) "Вы отдаете:" else "You Pay:", fontSize = 16.sp, color = Color(0xFFB0B0C5), fontFamily = RajdhaniFontFamily)
                                        Text(
                                            text = if (isRu) "Баланс: $userWeed" else "Bal: $userWeed",
                                            fontSize = 16.sp,
                                            color = currentSkin.primaryColor,
                                            fontFamily = RajdhaniFontFamily,
                                            modifier = Modifier.clickable { swapAmountInput = userWeed.toString() }
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = swapAmountInput,
                                            onValueChange = { swapAmountInput = it },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f).height(50.dp),
                                            textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 16.sp, color = Color.White),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedBorderColor = Color.Transparent,
                                                focusedBorderColor = Color.Transparent
                                            )
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF1E2438),
                                            border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                                            modifier = Modifier.clickable {
                                                fromToken = if (fromToken == "WEED") "TON" else "WEED"
                                            }
                                        ) {
                                            Text(
                                                text = "🪙 $fromToken ▾",
                                                fontFamily = RajdhaniFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }

                                // Swap direction toggle icon
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    IconButton(
                                        onClick = {
                                            val tmp = fromToken
                                            fromToken = toToken
                                            toToken = tmp
                                            com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.CLICK)
                                        },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color(0xFF1E2438), CircleShape)
                                            .border(1.dp, currentSkin.primaryColor, CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SwapVert,
                                            contentDescription = "Swap",
                                            tint = currentSkin.primaryColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                // To Token
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF131726), RoundedCornerShape(10.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(if (isRu) "Вы получаете (оценочно):" else "You Receive (estimated):", fontSize = 16.sp, color = Color(0xFFB0B0C5), fontFamily = RajdhaniFontFamily)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = String.format(Locale.US, "%.4f", estimatedOutput),
                                            fontFamily = RajdhaniFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = NeonGreen
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF1E2438),
                                            border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                                            modifier = Modifier.clickable {
                                                toToken = if (toToken == "TON") "ETH" else if (toToken == "ETH") "USDC" else "TON"
                                            }
                                        ) {
                                            Text(
                                                text = "🪙 $toToken ▾",
                                                fontFamily = RajdhaniFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }

                                // Execute Swap Button
                                CyberButton(
                                    onClick = {
                                        if (inputNum > 0) {
                                            if (fromToken == "WEED") {
                                                if (userWeed >= inputNum.toInt()) {
                                                    viewModel.deductWeedCredits(inputNum.toInt())
                                                    com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
                                                    viewModel.postNotification(
                                                        if (isRu) "✔️ Обмен завершен: -$inputNum \$WEED ➔ +${String.format(Locale.US, "%.4f", estimatedOutput)} $toToken"
                                                        else "✔️ Swap executed: -$inputNum \$WEED ➔ +${String.format(Locale.US, "%.4f", estimatedOutput)} $toToken"
                                                    )
                                                } else {
                                                    viewModel.postNotification(if (isRu) "❌ Недостаточно \$WEED для обмена" else "❌ Insufficient \$WEED for swap")
                                                }
                                            } else {
                                                viewModel.addWeedCredits(estimatedOutput.toInt())
                                                com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
                                                viewModel.postNotification(
                                                    if (isRu) "✔️ Обмен завершен: -$inputNum $fromToken ➔ +${estimatedOutput.toInt()} \$WEED"
                                                    else "✔️ Swap executed: -$inputNum $fromToken ➔ +${estimatedOutput.toInt()} \$WEED"
                                                )
                                            }
                                        }
                                    },
                                    skin = currentSkin,
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                ) {
                                    Text(
                                        text = if (isRu) "⚡ СОВЕРШИТЬ ОБМЕН В ПУЛЕ" else "⚡ EXECUTE AMM SWAP",
                                        fontFamily = RajdhaniFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // NFT Market & Minting
                MarketScreen(viewModel)
            }
            2 -> {
                // Ledger & On-Chain History
                val transactionLogs by viewModel.transactionLogs.collectAsStateWithLifecycle()
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = if (isRu) "📜 ДЕЦЕНТРАЛИЗОВАННЫЙ РЕЕСТР" else "📜 ON-CHAIN LEDGER",
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = NeonCyan
                        )
                        Text(
                            text = if (isRu) "Хроника транзакций, смарт-контрактов и минтинга карт в сети." else "Audit trail of cryptographically verified NFT mints & transfers.",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            color = Color(0xFFB0B0C5)
                        )
                    }

                    if (transactionLogs.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(150.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isRu) "Реестр пуст. Совершите первую сделку или минт." else "Ledger clear. Execute a swap or mint to record.",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 16.sp,
                                    color = Color(0xFFB0B0C5)
                                )
                            }
                        }
                    } else {
                        items(transactionLogs) { log ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C101D)),
                                border = BorderStroke(1.dp, Color(0x22FFFFFF)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = log.type,
                                            fontFamily = RajdhaniFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = currentSkin.primaryColor
                                        )
                                        Text(
                                            text = "${log.targetParty} • ${log.rarity}",
                                            fontFamily = RajdhaniFontFamily,
                                            fontSize = 15.sp,
                                            color = Color.LightGray
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${log.price} WEED",
                                            fontFamily = RajdhaniFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = NeonGreen
                                        )
                                        Text(
                                            text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp)),
                                            fontFamily = RajdhaniFontFamily,
                                            fontSize = 14.sp,
                                            color = Color(0xFFB0B0C5)
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
}
@Composable
fun DivinationScreen(viewModel: TarotViewModel) {
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val divinationState by viewModel.divinationState.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val lang = profile?.systemLanguage ?: "en"
    var questionInput by remember { mutableStateOf("") }
    val historyReadings by viewModel.divinationReadings.collectAsStateWithLifecycle()
    var expandedReadingId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(divinationState) {
        when (divinationState) {
            is DivinationState.Scanning -> {
                com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.SCAN_BEEP)
            }
            is DivinationState.Success -> {
                com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
            }
            else -> {}
        }
    }
    var selectedCardsCount by remember { mutableStateOf(1) } // 1, 3, 5, or 10
    var showVipPromptForCards by remember { mutableStateOf<Int?>(null) }
    val isVipUnlocked by viewModel.isVipUnlocked.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 54.dp)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main reading frame
        when (val state = divinationState) {
            is DivinationState.Idle -> {
                LazyColumn(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (lang == "ru" || lang == "uk") "🔮 КИБЕР-ОРАКУЛ // CYBERTAROT" else "🔮 CYBER ORACLE // CYBERTAROT",
                            fontSize = 26.sp,
                            color = currentSkin.primaryColor,
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.0.sp,
                            modifier = Modifier.testTag("divination_title")
                        )
                        Text(
                            text = if (lang == "ru" || lang == "uk") "Священная матрица Таро • Лунный гровер-оракул" else "Sacred Tarot Matrix • Lunar Grower Oracle",
                            fontSize = 16.sp,
                            color = NeonCyan,
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )

                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                            border = BorderStroke(2.5.dp, currentSkin.primaryColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .shadow(
                                    elevation = 12.dp,
                                    spotColor = currentSkin.primaryColor,
                                    ambientColor = currentSkin.primaryColor
                                )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                CyberOrnamentalHeader(
                                    title = "SECURE PROMPT INPUT",
                                    skin = currentSkin
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = questionInput,
                                    onValueChange = { questionInput = it },
                                    placeholder = {
                                        Text(
                                            "Describe your query, situation, or question for the oracle...",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    },
                                    textStyle = TextStyle(
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = RajdhaniFontFamily
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = currentSkin.primaryColor,
                                        unfocusedBorderColor = currentSkin.secondaryColor.copy(alpha = 0.8f),
                                        focusedContainerColor = Color(0xFF1E1C2B),
                                        unfocusedContainerColor = Color(0xFF14121E)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(90.dp)
                                        .testTag("question_input_field"),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(onDone = {
                                        keyboardController?.hide()
                                    })
                                )
                            }
                        }

                        // Spread Selector
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                            border = BorderStroke(2.5.dp, currentSkin.secondaryColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .shadow(
                                    elevation = 12.dp,
                                    spotColor = currentSkin.secondaryColor,
                                    ambientColor = currentSkin.secondaryColor
                                )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                CyberOrnamentalHeader(
                                    title = "CHOOSE SPREAD DEPTH",
                                    skin = currentSkin
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Option 1: 1 Card
                                        CyberButton(
                                            onClick = { selectedCardsCount = 1 },
                                            skin = currentSkin,
                                            containerColor = if (selectedCardsCount == 1) NeonCyan else ElegantIndigo,
                                            contentColor = if (selectedCardsCount == 1) CyberObsidian else Color.White,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                AutoResizingText(
                                                    text = "SINGLE (1)",
                                                    fontFamily = RajdhaniFontFamily,
                                                    fontWeight = FontWeight.Black,
                                                    maxLines = 1,
                                                    targetTextSize = 13.sp,
                                                    textAlign = TextAlign.Center
                                                )
                                                Text(
                                                    text = if (lang == "ru") "Быстрый разбор" else "Quick Diagnosis",
                                                    fontSize = 14.sp,
                                                    fontFamily = RajdhaniFontFamily,
                                                    color = if (selectedCardsCount == 1) CyberObsidian.copy(alpha = 0.8f) else Color.LightGray
                                                )
                                            }
                                        }
                                        // Option 2: 3 Cards
                                        CyberButton(
                                            onClick = { selectedCardsCount = 3 },
                                            skin = currentSkin,
                                            containerColor = if (selectedCardsCount == 3) NeonCyan else ElegantIndigo,
                                            contentColor = if (selectedCardsCount == 3) CyberObsidian else Color.White,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                AutoResizingText(
                                                    text = "FLOW (3)",
                                                    fontFamily = RajdhaniFontFamily,
                                                    fontWeight = FontWeight.Black,
                                                    maxLines = 1,
                                                    targetTextSize = 13.sp,
                                                    textAlign = TextAlign.Center
                                                )
                                                Text(
                                                    text = if (lang == "ru") "Потоки времени" else "Timeline Mapping",
                                                    fontSize = 14.sp,
                                                    fontFamily = RajdhaniFontFamily,
                                                    color = if (selectedCardsCount == 3) CyberObsidian.copy(alpha = 0.8f) else Color.LightGray
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Option 3: 5 Cards (Romantic)
                                        CyberButton(
                                            onClick = {
                                                if (isVipUnlocked) {
                                                    selectedCardsCount = 5
                                                } else {
                                                    showVipPromptForCards = 5
                                                }
                                            },
                                            skin = currentSkin,
                                            containerColor = if (selectedCardsCount == 5) NeonCyan else if (isVipUnlocked) ElegantIndigo else ElegantIndigo.copy(alpha = 0.5f),
                                            contentColor = if (selectedCardsCount == 5) CyberObsidian else Color.White,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "ROMANTIC (5)",
                                                        fontFamily = RajdhaniFontFamily,
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 16.sp,
                                                        color = if (selectedCardsCount == 5) CyberObsidian else if (isVipUnlocked) Color.White else NeonMagenta
                                                    )
                                                    if (!isVipUnlocked) {
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Icon(
                                                            imageVector = Icons.Default.Lock,
                                                            contentDescription = "Premium",
                                                            tint = NeonMagenta,
                                                            modifier = Modifier.size(10.dp)
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = if (lang == "ru") "Совместимость" else "Relational Sync",
                                                    fontSize = 14.sp,
                                                    fontFamily = RajdhaniFontFamily,
                                                    color = if (selectedCardsCount == 5) CyberObsidian.copy(alpha = 0.8f) else Color.LightGray
                                                )
                                            }
                                        }
                                        // Option 4: 10 Cards (Celtic Cross)
                                        CyberButton(
                                            onClick = {
                                                if (isVipUnlocked) {
                                                    selectedCardsCount = 10
                                                } else {
                                                    showVipPromptForCards = 10
                                                }
                                            },
                                            skin = currentSkin,
                                            containerColor = if (selectedCardsCount == 10) NeonCyan else if (isVipUnlocked) ElegantIndigo else ElegantIndigo.copy(alpha = 0.5f),
                                            contentColor = if (selectedCardsCount == 10) CyberObsidian else Color.White,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "CELTIC (10)",
                                                        fontFamily = RajdhaniFontFamily,
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 16.sp,
                                                        color = if (selectedCardsCount == 10) CyberObsidian else if (isVipUnlocked) Color.White else NeonMagenta
                                                    )
                                                    if (!isVipUnlocked) {
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Icon(
                                                            imageVector = Icons.Default.Lock,
                                                            contentDescription = "Premium",
                                                            tint = NeonMagenta,
                                                            modifier = Modifier.size(10.dp)
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = if (lang == "ru") "Кельтский Крест" else "Celtic Cross Matrix",
                                                    fontSize = 14.sp,
                                                    fontFamily = RajdhaniFontFamily,
                                                    color = if (selectedCardsCount == 10) CyberObsidian.copy(alpha = 0.8f) else Color.LightGray
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = when (selectedCardsCount) {
                                        1 -> if (lang == "ru") "⚡ Награда +50 WEED. Быстрый разбор ответов." else "⚡ Rewards +50 WEED. Instant troubleshooting."
                                        3 -> if (lang == "ru") "⚡ Награда +150 WEED. Карта временных потоков." else "⚡ Rewards +150 WEED. Maps timeline threads."
                                        5 -> if (lang == "ru") "🔮 Награда +250 WEED. Глубокий анализ отношений." else "🔮 Premium Channel. Rewards +250 WEED. Full relational sync."
                                        else -> if (lang == "ru") "👑 Награда +500 WEED. Абсолютная расшифровка." else "👑 Master Oracle Spread. Rewards +500 WEED. Ultimate comprehensive decryption."
                                    },
                                    fontSize = 16.sp,
                                    color = if (selectedCardsCount >= 5) NeonMagenta else NeonGreen,
                                    fontFamily = RajdhaniFontFamily
                                )
                            }
                        }

                        // Oracle Blessing Portal & Quests Hub Entry Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                            border = BorderStroke(2.dp, NeonMagenta),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { viewModel.setBlessingsPortalVisible(true) }
                                .shadow(
                                    elevation = 10.dp,
                                    spotColor = NeonMagenta,
                                    ambientColor = NeonMagenta
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (lang == "ru") "🔮 КВЕСТЫ И ДАРЫ ОРАКУЛА" else "🔮 ORACLE BLESSINGS & QUESTS",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontFamily = RajdhaniFontFamily
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (lang == "ru") "Забирайте награды за квесты, бустите майнинг и открывайте Premium расклады!" else "Claim quest rewards, boost passive income, and unlock VIP channels.",
                                        fontSize = 16.sp,
                                        color = Color.LightGray,
                                        fontFamily = RajdhaniFontFamily
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight,
                                    contentDescription = "Go",
                                    tint = NeonMagenta,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // VIP Spreads Prompter Dialog
                        showVipPromptForCards?.let { count ->
                            androidx.compose.material3.AlertDialog(
                                onDismissRequest = { showVipPromptForCards = null },
                                title = {
                                    Text(
                                        text = if (lang == "ru") "ТРЕБУЕТСЯ ДЕКРИПТОР PREMIUM" else "PREMIUM DECRYPTOR KEY REQUIRED",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonMagenta,
                                        fontFamily = RajdhaniFontFamily
                                    )
                                },
                                text = {
                                    Text(
                                        text = if (lang == "ru") {
                                            "Этот расклад (${count} карт) является премиальной услугой связи. Вы можете списать ${if (count == 5) 100 else 200} WEED за разовое сканирование или открыть Портал Благословений для перманентного VIP доступа."
                                        } else {
                                            "This premium layout (${count} cards) requires advanced node bandwidth. You can deduct ${if (count == 5) 100 else 200} WEED credits for a single decrypt scan, or open the Blessings Portal for permanent VIP channel access."
                                        },
                                        fontSize = 16.sp,
                                        color = Color.LightGray,
                                        fontFamily = RajdhaniFontFamily
                                    )
                                },
                                confirmButton = {
                                    CyberButton(
                                        onClick = {
                                            val cost = if (count == 5) 100 else 200
                                            val success = viewModel.deductWeedCredits(cost)
                                            if (success) {
                                                viewModel.postNotification(
                                                    if (lang == "ru") "🔮 Разовый доступ куплен! Начинаем сканирование..." else "🔮 Single-scan lease acquired! Processing..."
                                                )
                                                selectedCardsCount = count
                                                showVipPromptForCards = null
                                                viewModel.runDivinationScan(questionInput, count)
                                            } else {
                                                viewModel.postNotification(
                                                    if (lang == "ru") "❌ Недостаточно WEED для списания." else "❌ Insufficient WEED credits."
                                                )
                                            }
                                        },
                                        skin = currentSkin,
                                        containerColor = NeonCyan,
                                        contentColor = CyberObsidian,
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Text(
                                            text = if (lang == "ru") "Списать ${if (count == 5) 100 else 200}" else "Spend ${if (count == 5) 100 else 200}",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = RajdhaniFontFamily
                                        )
                                    }
                                },
                                dismissButton = {
                                    CyberButton(
                                        onClick = {
                                            showVipPromptForCards = null
                                            viewModel.setBlessingsPortalVisible(true)
                                        },
                                        skin = currentSkin,
                                        containerColor = ElegantIndigo,
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Text(
                                            text = if (lang == "ru") "Портал Даров" else "Blessing Portal",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = RajdhaniFontFamily
                                        )
                                    }
                                },
                                containerColor = CyberObsidian,
                                tonalElevation = 6.dp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Trigger Button
                        CyberButton(
                            onClick = {
                                keyboardController?.hide()
                                viewModel.runDivinationScan(questionInput, selectedCardsCount)
                            },
                            skin = currentSkin,
                            containerColor = NeonCyan,
                            contentColor = CyberObsidian,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .shadow(
                                    elevation = 8.dp,
                                    spotColor = NeonCyan,
                                    ambientColor = NeonCyan,
                                    clip = false
                                )
                                .testTag("initialize_scan_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = CyberObsidian,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                AutoResizingText(
                                    text = "INITIALIZE SCAN",
                                    fontWeight = FontWeight.Black,
                                    fontFamily = RajdhaniFontFamily,
                                    targetTextSize = 24.sp,
                                    letterSpacing = 1.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        CyberAdBanner(viewModel)
                    }

                    if (historyReadings.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            HorizontalDivider(color = currentSkin.secondaryColor.copy(alpha = 0.25f))
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "History",
                                    tint = NeonMagenta,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (lang == "ru") "ИСТОРИЯ РАСКЛАДОВ" else "DIVINATION HISTORY",
                                    fontSize = 14.sp,
                                    color = NeonMagenta,
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        items(historyReadings, key = { it.id }) { reading ->
                            val isExpanded = expandedReadingId == reading.id
                            
                            // Reconstruct the drawn cards
                            val indices = remember(reading.cardIndicesString) {
                                reading.cardIndicesString.split(",").filter { it.isNotEmpty() }.map { it.toInt() }
                            }
                            val uprights = remember(reading.isUprightString) {
                                reading.isUprightString.split(",").filter { it.isNotEmpty() }.map { it.toBoolean() }
                            }
                            val drawnCards = remember(indices, uprights) {
                                indices.zip(uprights).map { (index, isUpright) ->
                                    CyberTarotDeck.getCard(index) to isUpright
                                }
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                                border = BorderStroke(1.5.dp, if (isExpanded) NeonMagenta else currentSkin.secondaryColor.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clickable {
                                        expandedReadingId = if (isExpanded) null else reading.id
                                    }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = reading.question.ifEmpty { if (lang == "ru") "Без вопроса" else "No question" },
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(reading.timestamp)),
                                                fontSize = 15.sp,
                                                fontFamily = RajdhaniFontFamily,
                                                color = Color(0xFFB0B0C5)
                                            )
                                        }
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Toggle",
                                            tint = if (isExpanded) NeonMagenta else Color(0xFFB0B0C5),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    // Small preview of drawn cards when collapsed
                                    if (!isExpanded) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            drawnCards.forEach { (card, isUpright) ->
                                                val cardName = CardLocalizationHelper.getLocalizedName(card.index, lang)
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(ElegantIndigo.copy(alpha = 0.4f))
                                                        .border(1.5.dp, currentSkin.primaryColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = cardName + (if (isUpright) "" else " (Rev)"),
                                                        fontSize = 14.sp,
                                                        color = currentSkin.primaryColor,
                                                        fontFamily = RajdhaniFontFamily
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Fully expanded visual details & Gemini analysis report
                                    if (isExpanded) {
                                        Spacer(modifier = Modifier.height(14.dp))
                                        
                                        // Drawn Cards Preview Visual
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState())
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                                        ) {
                                            drawnCards.forEach { (card, isUpright) ->
                                                InteractiveCyberCard(card, isUpright, viewModel)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))
                                        HorizontalDivider(color = currentSkin.primaryColor.copy(alpha = 0.2f))
                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Interpreted matrix output text
                                        Text(
                                            text = reading.report,
                                            fontSize = 17.sp,
                                            color = Color.White.copy(alpha = 0.95f),
                                            fontFamily = RajdhaniFontFamily,
                                            lineHeight = 18.sp
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        // Delete button
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            CyberTextButton(
                                                onClick = {
                                                    viewModel.deleteDivinationReading(reading.id)
                                                    expandedReadingId = null
                                                },
                                                text = if (lang == "ru") "УДАЛИТЬ" else "DELETE",
                                                skin = currentSkin,
                                                containerColor = NeonRed.copy(alpha = 0.2f),
                                                contentColor = NeonRed,
                                                shape = RoundedCornerShape(8.dp),
                                                targetTextSize = 10.sp,
                                                modifier = Modifier.height(32.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            is DivinationState.Scanning -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val infiniteTransition = rememberInfiniteTransition()
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 0.9f,
                        targetValue = 1.1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )

                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = "Scanning",
                        tint = NeonMagenta,
                        modifier = Modifier
                            .size(84.dp)
                            .shadow(
                                elevation = 24.dp * pulseScale,
                                ambientColor = NeonMagenta,
                                spotColor = NeonMagenta,
                                clip = false
                            )
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "SYNCING NEURAL CONNECTIVITY...",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 14.sp,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )

                    // Diagnostic Logs Typing Animation simulation
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .background(CyberCharcoal, shape = RoundedCornerShape(8.dp))
                            .border(1.dp, CyberMutedBlue, shape = RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        var logText by remember { mutableStateOf("Initializing scanner core...") }
                        LaunchedEffect(Unit) {
                            val logs = listOf(
                                "Querying secure Oracle database...",
                                "Opening TCP/IP ports to Gemini Node...",
                                "Retrieving encrypted data packets...",
                                "Analyzing digital circuitry analogies...",
                                "Synthesizing consensus report..."
                            )
                            for (log in logs) {
                                delay(400)
                                logText = log
                            }
                        }
                        Text(
                            text = "> $logText",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            color = NeonGreen
                        )
                    }
                }
            }

            is DivinationState.Success -> {
                // Interactive Card Decryption Layout
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "DECRYPTION COMPLETE",
                                fontSize = 16.sp,
                                color = NeonGreen,
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = FontWeight.Bold
                            )

                            CyberButton(
                                onClick = { viewModel.resetDivination() },
                                skin = currentSkin,
                                containerColor = CyberMutedBlue,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Reset",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    AutoResizingText(
                                        text = "New Scan",
                                        targetTextSize = 11.sp,
                                        color = Color.White,
                                        fontFamily = RajdhaniFontFamily,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        
                        Text(
                            text = "Question: \"${state.question}\"",
                            color = Color.LightGray,
                            fontSize = 16.sp,
                            fontFamily = RajdhaniFontFamily,
                            textAlign = TextAlign.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Drawn Cards Container (Grid or Row depending on size)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                        ) {
                            state.cards.forEachIndexed { index, (card, isUpright) ->
                                InteractiveCyberCard(card, isUpright, viewModel)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Decrypted Reading Report (Gemini output)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                            border = BorderStroke(2.5.dp, currentSkin.primaryColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .shadow(
                                    elevation = 14.dp,
                                    spotColor = currentSkin.primaryColor,
                                    ambientColor = currentSkin.primaryColor
                                )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                CyberOrnamentalHeader(
                                    title = "ORACLE INTERPRETATION",
                                    skin = currentSkin
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))

                                // TextToSpeech Oracle voice synthesis button
                                CyberButton(
                                    onClick = {
                                        if (isSpeaking) {
                                            viewModel.stopSpeaking()
                                        } else {
                                            viewModel.speakText(state.report)
                                        }
                                    },
                                    skin = currentSkin,
                                    containerColor = if (isSpeaking) NeonMagenta else ElegantIndigo,
                                    modifier = Modifier.fillMaxWidth().height(40.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isSpeaking) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                            contentDescription = "Speak",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isSpeaking) {
                                                if (lang == "ru") "ОСТАНОВИТЬ ГОЛОС ОРАКУЛА" else "STOP ORACLE TRANSMISSION"
                                            } else {
                                                if (lang == "ru") "СИНТЕЗИРОВАТЬ ГОЛОС ОРАКУЛА" else "SYNTHESIZE ORACLE VOICE"
                                            },
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = RajdhaniFontFamily,
                                            color = Color.White
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                ContrastText(
                                    text = state.report,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp,
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(64.dp))
                    }
                }
            }
            is DivinationState.Error -> {
                // simple error recovery
                viewModel.resetDivination()
            }
        }
    }
}

@Composable
fun InteractiveCyberCard(
    card: CyberTarotCard,
    isUpright: Boolean,
    viewModel: TarotViewModel
) {
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val lang = profile?.systemLanguage ?: "en"
    var isRevealed by remember { mutableStateOf(false) }
    var wasMinted by remember { mutableStateOf(false) }
    var showFullScreenReview by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (isRevealed) 180f else 0f,
        animationSpec = tween(
            durationMillis = 600,
            easing = FastOutSlowInEasing
        ),
        label = "card_flip"
    )

    val rarityColor = remember(card.defaultRarity) {
        when (card.defaultRarity) {
            "Legend" -> NeonGold
            "Epic" -> NeonMagenta
            "Rare" -> NeonCyan
            else -> NeonGreen
        }
    }

    val localizedName = CardLocalizationHelper.getLocalizedName(card.index, lang)
    val localizedTraditional = CardLocalizationHelper.getLocalizedTraditionalName(card.index, lang)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(170.dp)
    ) {
        val cardBgBrush = if (isRevealed) {
            Brush.verticalGradient(
                colors = listOf(
                    rarityColor.copy(alpha = 0.28f),
                    Color(0xFF0C091F).copy(alpha = 0.95f)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    currentSkin.primaryColor.copy(alpha = 0.20f),
                    Color(0xFF06060E).copy(alpha = 0.95f)
                )
            )
        }

        Box(
            modifier = Modifier
                .width(170.dp)
                .height(250.dp)
                .shadow(
                    elevation = if (isRevealed) 16.dp else 6.dp,
                    ambientColor = if (isRevealed) rarityColor else currentSkin.primaryColor,
                    spotColor = if (isRevealed) rarityColor else currentSkin.primaryColor
                )
                .clip(RoundedCornerShape(12.dp))
                .background(cardBgBrush)
                .border(
                    width = 2.5.dp,
                    color = if (isRevealed) rarityColor else currentSkin.primaryColor.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable {
                    if (!isRevealed) {
                        isRevealed = true
                        com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.CARD_DRAW)
                    } else {
                        showFullScreenReview = true
                    }
                }
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12 * density
                },
            contentAlignment = Alignment.Center
        ) {
            if (rotation <= 90f) {
                // Card Back with Series Artwork
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    val backImgRes = getCardBackImage(card.series)
                    Image(
                        painter = painterResource(id = backImgRes),
                        contentDescription = "${card.series} Card Back",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color(0xDD0A0918))
                                )
                            )
                            .padding(bottom = 8.dp, top = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = card.series.uppercase(),
                                fontSize = 14.sp,
                                fontFamily = RajdhaniFontFamily,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            ContrastText(
                                text = "[TAP TO REVEAL]",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 16.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                // Card Front (Decrypted Node)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationY = 180f
                        }
                ) {
                    CyberRarityShaderOverlay(
                        rarity = card.defaultRarity,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Header (Rarity Badge & Index)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ContrastText(
                                text = "NODE #${String.format("%02d", card.index)}",
                                fontSize = 16.sp,
                                fontFamily = RajdhaniFontFamily,
                                color = Color.White
                            )
                            CyberRarityGlowBadge(
                                rarity = card.defaultRarity
                            )
                        }

                    val imageResId = getCardImage(card.index)
                    if (imageResId != null) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.5.dp, rarityColor, RoundedCornerShape(8.dp))
                                .background(CyberCharcoal),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = imageResId),
                                contentDescription = "${card.name} - Van Gogh styled",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .rotate(if (isUpright) 0f else 180f),
                                contentScale = ContentScale.Crop
                            )
                        }
                    } else {
                        // Card Art / Symbolic Botanical Sprout & Glowing Resin Aura
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .drawBehind {
                                    val center = Offset(size.width / 2, size.height / 2)
                                    // Background organic aura
                                    drawCircle(
                                        color = rarityColor.copy(alpha = 0.15f),
                                        radius = size.width / 2
                                    )
                                    // Concentric organic glow rings
                                    drawCircle(
                                        color = rarityColor.copy(alpha = 0.35f),
                                        radius = 24.dp.toPx(),
                                        style = Stroke(width = 1.5.dp.toPx())
                                    )
                                    drawCircle(
                                        color = rarityColor,
                                        radius = 12.dp.toPx(),
                                        style = Stroke(width = 2.5.dp.toPx())
                                    )
                                    // Dynamic grower root veins / branch stems
                                    drawLine(
                                        color = rarityColor.copy(alpha = 0.8f),
                                        start = Offset(center.x, center.y + 12.dp.toPx()),
                                        end = Offset(center.x, center.y + 36.dp.toPx()),
                                        strokeWidth = 3.dp.toPx()
                                    )
                                    drawLine(
                                        color = rarityColor.copy(alpha = 0.8f),
                                        start = Offset(center.x, center.y + 20.dp.toPx()),
                                        end = Offset(center.x - 22.dp.toPx(), center.y - 12.dp.toPx()),
                                        strokeWidth = 2.dp.toPx()
                                    )
                                    drawLine(
                                        color = rarityColor.copy(alpha = 0.8f),
                                        start = Offset(center.x, center.y + 20.dp.toPx()),
                                        end = Offset(center.x + 22.dp.toPx(), center.y - 12.dp.toPx()),
                                        strokeWidth = 2.dp.toPx()
                                    )
                                    // Glowing flower resin nodes
                                    drawCircle(
                                        color = rarityColor,
                                        radius = 5.dp.toPx(),
                                        center = Offset(center.x - 22.dp.toPx(), center.y - 12.dp.toPx())
                                    )
                                    drawCircle(
                                        color = rarityColor,
                                        radius = 5.dp.toPx(),
                                        center = Offset(center.x + 22.dp.toPx(), center.y - 12.dp.toPx())
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (card.defaultRarity) {
                                    "Legend" -> Icons.Default.Spa
                                    "Epic" -> Icons.Default.Eco
                                    "Rare" -> Icons.Default.LocalFlorist
                                    else -> Icons.Default.Grass
                                },
                                contentDescription = localizedName,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    // Card Name & Rider-Waite Mapping
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ContrastText(
                            text = localizedName.uppercase(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = RajdhaniFontFamily,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        ContrastText(
                            text = "($localizedTraditional)",
                            fontSize = 15.sp,
                            fontFamily = RajdhaniFontFamily,
                            color = Color.Yellow,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Orientation Status
                    ContrastText(
                        text = if (isUpright) "UPRIGHT" else "REVERSED",
                        fontSize = 15.sp,
                        fontFamily = RajdhaniFontFamily,
                        color = if (isUpright) Color(0xFF39FF14) else Color(0xFFFF3366),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Mint Button (visible only when decrypted)
        if (isRevealed) {
            if (!wasMinted) {
                CyberButton(
                    onClick = {
                        viewModel.mintNFT(card.index) { success ->
                            if (success) wasMinted = true
                        }
                    },
                    skin = currentSkin,
                    containerColor = NeonCyan,
                    contentColor = CyberObsidian,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(0.95f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Mint",
                            tint = CyberObsidian,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        AutoResizingText(
                            text = "MINT NFT (200 N$)",
                            targetTextSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = RajdhaniFontFamily,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(NeonGreen.copy(alpha = 0.15f))
                        .border(1.dp, NeonGreen, RoundedCornerShape(24.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        "MINTED SECURELY",
                        color = NeonGreen,
                        fontSize = 14.sp,
                        fontFamily = RajdhaniFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (showFullScreenReview) {
            FullScreenCardReview(
                card = card,
                isMarketView = false,
                systemLanguage = lang,
                skin = currentSkin,
                onClose = { showFullScreenReview = false }
            )
        }
    }
}

// ==========================================
// 2. SECURE VAULT (NFT Collection Screen)
// ==========================================
@Composable
fun VaultScreen(viewModel: TarotViewModel) {
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val lang = profile?.systemLanguage ?: "en"
    val mintedCards by viewModel.mintedCards.collectAsStateWithLifecycle()
    var selectedMintedCard by remember { mutableStateOf<MintedCard?>(null) }
    var listPriceInput by remember { mutableStateOf("") }
    var isListingMode by remember { mutableStateOf(false) }
    var isNftMintingMode by remember { mutableStateOf(false) }
    var testnetWalletInput by remember { mutableStateOf("0x9E88d6C7BaF8F4f3183f233f6329686259e7ab9C") }
    var cardToInspectReview by remember { mutableStateOf<MintedCard?>(null) }
    var showLunarCycleModal by remember { mutableStateOf(false) }

    val todayCal = remember { java.util.Calendar.getInstance() }
    val todayLunarDay = remember(lang) {
        LunarCalendarService.getLunarDayInfo(
            todayCal.get(java.util.Calendar.YEAR),
            todayCal.get(java.util.Calendar.MONTH) + 1,
            todayCal.get(java.util.Calendar.DAY_OF_MONTH),
            lang
        )
    }
    val todayLunarMinting = remember(lang) {
        LunarCalendarService.getTarotMintingWindow(
            todayCal.get(java.util.Calendar.YEAR),
            todayCal.get(java.util.Calendar.MONTH) + 1,
            todayCal.get(java.util.Calendar.DAY_OF_MONTH),
            lang
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 54.dp)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "VAULT",
            fontSize = 24.sp,
            color = NeonCyan,
            fontFamily = RajdhaniFontFamily,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("vault_title")
        )
        Text(
            text = "Cryptographic Nodes",
            fontSize = 16.sp,
            color = Color(0xFFB0B0C5),
            fontFamily = RajdhaniFontFamily,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        // Auspicious Lunar Cycle Minting Bar
        val lunarStatusColor = when (todayLunarMinting.tier) {
            MintingAuspiciousnessTier.AUSPICIOUS -> Color(0xFF00FF66)
            MintingAuspiciousnessTier.BALANCED -> Color(0xFF00F5FF)
            MintingAuspiciousnessTier.CHALLENGING -> Color(0xFFFF3366)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF070A14))
                .border(1.dp, lunarStatusColor.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                .clickable { showLunarCycleModal = true }
                .padding(horizontal = 12.dp, vertical = 9.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = todayLunarDay.phaseIcon, fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${todayLunarDay.phaseName.uppercase()} • ${todayLunarDay.illumination}%",
                            fontSize = 16.sp,
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = todayLunarMinting.badgeLabel,
                            fontSize = 14.sp,
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = lunarStatusColor
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (lang == "ru") "ЦИКЛ" else "CYCLE",
                        fontSize = 15.sp,
                        fontFamily = RajdhaniFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Lunar Cycle",
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        if (mintedCards.isEmpty()) {
            // Empty Vault state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 64.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Empty",
                    tint = Color.DarkGray,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "VAULT SECURED - 0 CARDS OWNED",
                    fontSize = 16.sp,
                    fontFamily = RajdhaniFontFamily,
                    color = Color(0xFFB0B0C5),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Perform a divination scan and mint drawn cards to get started.",
                    fontSize = 15.sp,
                    fontFamily = RajdhaniFontFamily,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp).padding(top = 4.dp)
                )
            }
        } else {
            // Cards Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(mintedCards) { mintedCard ->
                    val card = CyberTarotDeck.getCard(mintedCard.cardIndex)
                    val rarityColor = remember(mintedCard.rarity) {
                        when (mintedCard.rarity) {
                            "Legend" -> NeonGold
                            "Epic" -> NeonMagenta
                            "Rare" -> NeonCyan
                            else -> NeonGreen
                        }
                    }

                    val cardBgBrush = Brush.verticalGradient(
                        colors = listOf(
                            rarityColor.copy(alpha = 0.22f),
                            Color(0xFF0A0918).copy(alpha = 0.95f)
                        )
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        border = BorderStroke(2.5.dp, rarityColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(cardBgBrush, RoundedCornerShape(12.dp))
                            .clickable {
                                cardToInspectReview = mintedCard
                            }
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            CyberRarityShaderOverlay(
                                rarity = mintedCard.rarity,
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                // Rarity Badge & Number
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CyberRarityGlowBadge(
                                        rarity = mintedCard.rarity
                                    )

                                    ContrastText(
                                        text = "#${String.format("%04d", mintedCard.mintNumber)}",
                                        fontSize = 16.sp,
                                        fontFamily = RajdhaniFontFamily,
                                        color = Color.White
                                    )
                                }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Card Art Preview
                            val imageResId = getCardImage(card.index)
                            if (imageResId != null) {
                                Image(
                                    painter = painterResource(id = imageResId),
                                    contentDescription = "${card.name} - Garden Art",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(2.dp, rarityColor, RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(2.dp, rarityColor, RoundedCornerShape(8.dp))
                                        .background(CyberCharcoal),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CardSymbolIcon(cardIndex = card.index, tintColor = rarityColor, modifier = Modifier.size(36.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Card Title
                            val localizedName = CardLocalizationHelper.getLocalizedName(card.index, lang)
                            val localizedTraditional = CardLocalizationHelper.getLocalizedTraditionalName(card.index, lang)
                            ContrastText(
                                text = localizedName.uppercase(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = RajdhaniFontFamily,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            ContrastText(
                                text = "Mapped: $localizedTraditional",
                                fontSize = 16.sp,
                                fontFamily = RajdhaniFontFamily,
                                color = Color.Yellow
                            )

                            HorizontalDivider(color = rarityColor.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

                            // Market Listing Status
                            if (mintedCard.isListed) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    ContrastText(
                                        "LISTED",
                                        fontSize = 15.sp,
                                        color = NeonGold,
                                        fontFamily = RajdhaniFontFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                    ContrastText(
                                        "${mintedCard.listPrice} N$",
                                        fontSize = 15.sp,
                                        color = NeonGold,
                                        fontFamily = RajdhaniFontFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                ContrastText(
                                    "SECURED IN COLD VAULT",
                                    fontSize = 15.sp,
                                    color = NeonCyan,
                                    fontFamily = RajdhaniFontFamily
                                )
                            }
                        }
                    }
                }
            }
            }
        }
    }

    // Detail Modal for selected owned card
    selectedMintedCard?.let { mintedCard ->
        val card = CyberTarotDeck.getCard(mintedCard.cardIndex)
        val localizedName = CardLocalizationHelper.getLocalizedName(card.index, lang)
        val localizedTraditional = CardLocalizationHelper.getLocalizedTraditionalName(card.index, lang)
        val localizedDesc = CardLocalizationHelper.getLocalizedDescription(card.index, lang)
        val localizedCircuitry = CardLocalizationHelper.getLocalizedCircuitryElement(card.index, lang)
        val rarityColor = when (mintedCard.rarity) {
            "Legend" -> NeonGold
            "Epic" -> NeonMagenta
            "Rare" -> NeonCyan
            else -> NeonGreen
        }

        AlertDialog(
            onDismissRequest = { selectedMintedCard = null },
            containerColor = CyberCharcoal,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = localizedName.uppercase(),
                            color = Color.White,
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Rider-Waite: $localizedTraditional",
                            color = Color(0xFFB0B0C5),
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp
                        )
                    }

                    Text(
                        text = "#${String.format("%04d", mintedCard.mintNumber)}",
                        color = Color.White,
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 14.sp
                    )
                }
            },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    item {
                        HorizontalDivider(color = CyberMutedBlue, modifier = Modifier.padding(bottom = 12.dp))

                        // Hardware Specs
                        Text(
                            "NODE SPECIFICATION LORE",
                            color = rarityColor,
                            fontSize = 16.sp,
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = localizedDesc,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontFamily = RajdhaniFontFamily,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )

                        // Hardware Analogy
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "Hardware Core: ",
                                color = Color(0xFFB0B0C5),
                                fontSize = 16.sp,
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "`" + localizedCircuitry + "`",
                                color = NeonCyan,
                                fontSize = 16.sp,
                                fontFamily = RajdhaniFontFamily
                            )
                        }

                        HorizontalDivider(color = CyberMutedBlue, modifier = Modifier.padding(vertical = 12.dp))

                        if (!isListingMode) {
                            // Display Standard Actions
                            Column(modifier = Modifier.fillMaxWidth()) {
                                if (mintedCard.isListed) {
                                    // Cancel Listing Option
                                    CyberTextButton(
                                        onClick = {
                                            viewModel.cancelCardListing(mintedCard.cardIndex, mintedCard.mintNumber)
                                            selectedMintedCard = null
                                        },
                                        text = "CANCEL MARKET LISTING",
                                        skin = currentSkin,
                                        containerColor = NeonRed,
                                        contentColor = Color.White,
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    // List on Market
                                    CyberTextButton(
                                        onClick = { isListingMode = true },
                                        text = "LIST ON NEURAL EXCHANGE",
                                        skin = currentSkin,
                                        containerColor = NeonCyan,
                                        contentColor = CyberObsidian,
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp)
                                    )

                                    // Instant liquidation to Net Broker
                                    val basePrice = viewModel.getBaseMarketPrice(mintedCard.rarity)
                                    val brokerPrice = ((basePrice * 3) / 4).coerceAtLeast(15)

                                    CyberButton(
                                        onClick = {
                                            viewModel.sellInstantly(mintedCard.id)
                                            selectedMintedCard = null
                                        },
                                        skin = currentSkin,
                                        containerColor = ElegantIndigo,
                                        contentColor = Color.White,
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Bolt,
                                                contentDescription = null,
                                                tint = NeonCyan,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            AutoResizingText(
                                                text = "INSTANT SELL TO BROKER ($brokerPrice N$)",
                                                fontFamily = RajdhaniFontFamily,
                                                targetTextSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    if (!isNftMintingMode) {
                                        CyberButton(
                                            onClick = { isNftMintingMode = true },
                                            skin = currentSkin,
                                            containerColor = NeonMagenta,
                                            contentColor = Color.White,
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier.fillMaxWidth().testTag("mint_testnet_nft_button")
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Bolt,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                AutoResizingText(
                                                    text = "MINT AS TESTNET NFT",
                                                    fontFamily = RajdhaniFontFamily,
                                                    targetTextSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    } else {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFF09090E), RoundedCornerShape(8.dp))
                                                .border(1.dp, NeonMagenta.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                .padding(12.dp)
                                        ) {
                                            Text(
                                                "ON-CHAIN WALLET DESTINATION",
                                                color = NeonMagenta,
                                                fontSize = 15.sp,
                                                fontFamily = RajdhaniFontFamily,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            OutlinedTextField(
                                                value = testnetWalletInput,
                                                onValueChange = { testnetWalletInput = it },
                                                placeholder = { Text("0x...", color = Color.White.copy(alpha = 0.6f), fontSize = 17.sp, fontWeight = FontWeight.Bold) },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White,
                                                    focusedBorderColor = currentSkin.primaryColor,
                                                    unfocusedBorderColor = currentSkin.secondaryColor.copy(alpha = 0.5f),
                                                    focusedContainerColor = Color.Black.copy(alpha = 0.65f),
                                                    unfocusedContainerColor = Color.Black.copy(alpha = 0.45f)
                                                ),
                                                textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 17.sp, fontWeight = FontWeight.Bold),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            
                                            Spacer(modifier = Modifier.height(8.dp))
                                            
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                CyberTextButton(
                                                    onClick = { isNftMintingMode = false },
                                                    text = "CANCEL",
                                                    skin = currentSkin,
                                                    containerColor = Color.Transparent,
                                                    contentColor = Color.Gray,
                                                    shape = RoundedCornerShape(16.dp),
                                                    modifier = Modifier.weight(1f)
                                                )
                                                CyberTextButton(
                                                    onClick = {
                                                        viewModel.mintTarotInterpretationOnChain(
                                                            cardIndex = mintedCard.cardIndex,
                                                            mintNumber = mintedCard.mintNumber,
                                                            rarity = mintedCard.rarity,
                                                            interpretation = card.description,
                                                            walletAddress = testnetWalletInput
                                                        )
                                                        isNftMintingMode = false
                                                        selectedMintedCard = null
                                                    },
                                                    text = "MINT NFT",
                                                    skin = currentSkin,
                                                    containerColor = NeonMagenta,
                                                    contentColor = Color.White,
                                                    shape = RoundedCornerShape(16.dp),
                                                    modifier = Modifier.weight(1.5f).testTag("confirm_nft_mint_btn")
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Display Listing Input Frame
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    "SET ASK PRICE (WEED)",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = listPriceInput,
                                    onValueChange = { listPriceInput = it },
                                    placeholder = { Text("e.g. 500", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = currentSkin.primaryColor,
                                        unfocusedBorderColor = currentSkin.secondaryColor.copy(alpha = 0.5f),
                                        focusedContainerColor = Color.Black.copy(alpha = 0.65f),
                                        unfocusedContainerColor = Color.Black.copy(alpha = 0.45f)
                                    ),
                                    textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CyberTextButton(
                                        onClick = { isListingMode = false },
                                        text = "BACK",
                                        skin = currentSkin,
                                        containerColor = ElegantIndigo,
                                        contentColor = Color.White,
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.weight(1f)
                                    )

                                    CyberTextButton(
                                        onClick = {
                                            val price = listPriceInput.toIntOrNull() ?: 0
                                            viewModel.listCardOnMarket(mintedCard.id, price)
                                            selectedMintedCard = null
                                        },
                                        text = "EXECUTE LIST",
                                        skin = currentSkin,
                                        containerColor = NeonCyan,
                                        contentColor = CyberObsidian,
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.weight(1.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedMintedCard = null }) {
                    Text("CLOSE", color = NeonCyan, fontFamily = RajdhaniFontFamily)
                }
            }
        )
    }

    // Modal Fullscreen card review overlay for VaultScreen
    cardToInspectReview?.let { mintedCard ->
        FullScreenCardReview(
            card = CyberTarotDeck.getCard(mintedCard.cardIndex),
            mintedCard = mintedCard,
            isMarketView = false,
            systemLanguage = lang,
            skin = currentSkin,
            onClose = { cardToInspectReview = null },
            onActionClick = {
                selectedMintedCard = mintedCard
                isListingMode = false
                listPriceInput = ""
                cardToInspectReview = null
            }
        )
    }

    if (showLunarCycleModal) {
        AlertDialog(
            onDismissRequest = { showLunarCycleModal = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            containerColor = Color(0xFF070812),
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center
                ) {
                    CyberLunarCycleWidget(
                        systemLanguage = lang,
                        currentSkin = currentSkin,
                        onMintClick = {
                            showLunarCycleModal = false
                            viewModel.resetDivination()
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showLunarCycleModal = false }) {
                    Text(
                        text = if (lang == "ru") "ЗАКРЫТЬ" else "CLOSE",
                        color = NeonCyan,
                        fontFamily = RajdhaniFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}

// ==========================================
// 3. NEURAL MARKET & MINER (Exchange Screen)
// ==========================================
@Composable
fun MarketScreen(viewModel: TarotViewModel) {
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val lang = profile?.systemLanguage ?: "en"
    val listings by viewModel.filteredMarketListings.collectAsStateWithLifecycle()
    val searchBy by viewModel.marketQuery.collectAsStateWithLifecycle()
    val filterByRarity by viewModel.marketRarityFilter.collectAsStateWithLifecycle()
    val transactionLogs by viewModel.transactionLogs.collectAsStateWithLifecycle()
    val miningCooldown by viewModel.miningCooldown.collectAsStateWithLifecycle()

    var cardToBuyConfirm by remember { mutableStateOf<MarketListing?>(null) }
    var cardToInspectReview by remember { mutableStateOf<MarketListing?>(null) }

    val rarities = listOf("All", "Common", "Rare", "Epic", "Legend")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 54.dp)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "EXCHANGE",
                fontSize = 24.sp,
                color = NeonGold,
                fontFamily = RajdhaniFontFamily,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("exchange_title")
            )
            Text(
                text = "P2P Transactions",
                fontSize = 16.sp,
                color = Color(0xFFB0B0C5),
                fontFamily = RajdhaniFontFamily,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            // Dynamic CORE NODE HARVESTER / Miner
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "HARVESTER",
                                color = NeonGreen,
                                fontSize = 17.sp,
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Harvest Weed credits",
                                color = Color(0xFFB0B0C5),
                                fontSize = 15.sp,
                                fontFamily = RajdhaniFontFamily
                            )
                        }

                        if (miningCooldown > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(NeonGold.copy(alpha = 0.15f))
                                    .border(1.dp, NeonGold, RoundedCornerShape(24.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "00:${String.format("%02d", miningCooldown)} COOLDOWN",
                                    fontSize = 15.sp,
                                    color = NeonGold,
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            CyberButton(
                                onClick = { viewModel.startMining() },
                                skin = currentSkin,
                                containerColor = NeonGreen,
                                contentColor = CyberObsidian,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = CyberObsidian,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    AutoResizingText(
                                        text = "HARVEST (+350 N$)",
                                        targetTextSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = RajdhaniFontFamily,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    if (miningCooldown > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { (30f - miningCooldown.toFloat()) / 30f },
                            color = NeonGreen,
                            trackColor = CyberMutedBlue,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Search and filters
            OutlinedTextField(
                value = searchBy,
                onValueChange = { viewModel.setMarketQuery(it) },
                placeholder = { Text("Search by name, traditional, or seller...", color = Color.White.copy(alpha = 0.65f), fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = currentSkin.primaryColor, modifier = Modifier.size(20.dp)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = currentSkin.primaryColor,
                    unfocusedBorderColor = currentSkin.secondaryColor.copy(alpha = 0.5f),
                    focusedContainerColor = Color.Black.copy(alpha = 0.65f),
                    unfocusedContainerColor = Color.Black.copy(alpha = 0.45f)
                ),
                textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("market_search_input"),
                shape = RoundedCornerShape(12.dp)
            )

            // Rarity horizontally scrolling row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rarities.forEach { r ->
                    val isSelected = filterByRarity == r
                    val badgeColor = when (r) {
                        "Legend" -> NeonGold
                        "Epic" -> NeonMagenta
                        "Rare" -> NeonCyan
                        "Common" -> NeonGreen
                        else -> Color.White
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (isSelected) badgeColor.copy(alpha = 0.2f) else CyberCharcoal)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) badgeColor else CyberMutedBlue,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .clickable { viewModel.setMarketRarityFilter(r) }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = r.uppercase(),
                            fontSize = 15.sp,
                            color = if (isSelected) badgeColor else Color(0xFFB0B0C5),
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Active Listings Grid/List
        if (listings.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(imageVector = Icons.Default.Storefront, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "NO TRANSACTIONS IN DOCK",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        color = Color(0xFFB0B0C5),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            items(listings) { listing ->
                val card = CyberTarotDeck.getCard(listing.cardIndex)
                val rarityColor = remember(listing.rarity) {
                    when (listing.rarity) {
                        "Legend" -> NeonGold
                        "Epic" -> NeonMagenta
                        "Rare" -> NeonCyan
                        else -> NeonGreen
                    }
                }

                val cardBgBrush = Brush.horizontalGradient(
                    colors = listOf(
                        rarityColor.copy(alpha = 0.22f),
                        Color(0xFF0F0E24).copy(alpha = 0.95f)
                    )
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(2.5.dp, rarityColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                        .background(cardBgBrush, RoundedCornerShape(12.dp))
                        .clickable { cardToInspectReview = listing }
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        CyberRarityShaderOverlay(
                            rarity = listing.rarity,
                            modifier = Modifier
                                .matchParentSize()
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                        // Card Illustration occupies the whole card field width (full-bleed top banner)
                        val thumbnailResId = getCardImage(card.index)
                        if (thumbnailResId != null) {
                            Image(
                                painter = painterResource(id = thumbnailResId),
                                contentDescription = "${card.name} - Illustration",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .background(CyberCharcoal)
                                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                CardSymbolIcon(cardIndex = card.index, tintColor = rarityColor, modifier = Modifier.size(72.dp))
                            }
                        }

                        // Labels are below, and do not compete for width
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CyberRarityGlowBadge(
                                        rarity = listing.rarity
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    ContrastText(
                                        text = "#${String.format("%04d", listing.mintNumber)}",
                                        fontSize = 17.sp,
                                        fontFamily = RajdhaniFontFamily,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                ContrastText(
                                    text = "${listing.price} N$",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = RajdhaniFontFamily,
                                    color = NeonGold
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            val localizedName = CardLocalizationHelper.getLocalizedName(card.index, lang)
                            ContrastText(
                                text = localizedName.uppercase(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = RajdhaniFontFamily,
                                color = Color.White
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            ContrastText(
                                text = "Seller: ${listing.sellerName}",
                                fontSize = 16.sp,
                                fontFamily = RajdhaniFontFamily,
                                color = NeonGold,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            if (listing.sellerName == "User (You)") {
                                CyberTextButton(
                                    onClick = { viewModel.cancelCardListing(listing.cardIndex, listing.mintNumber) },
                                    text = "DELIST",
                                    skin = currentSkin,
                                    containerColor = NeonRed,
                                    contentColor = Color.White,
                                    shape = RoundedCornerShape(12.dp),
                                    targetTextSize = 12.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                )
                            } else {
                                CyberTextButton(
                                    onClick = { cardToBuyConfirm = listing },
                                    text = "BUY NFT",
                                    skin = currentSkin,
                                    containerColor = NeonCyan,
                                    contentColor = CyberObsidian,
                                    shape = RoundedCornerShape(12.dp),
                                    targetTextSize = 12.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                )
                            }
                        }
                    }
                }
            }
            }
        }

        // Transaction Logs Title
        item {
            HorizontalDivider(color = CyberMutedBlue, modifier = Modifier.padding(vertical = 16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Terminal, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "NEURAL LEDGER SYNCED",
                    fontSize = 16.sp,
                    color = NeonCyan,
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Transaction history entries
        if (transactionLogs.isEmpty()) {
            item {
                Text(
                    "Initializing ledger sync records...",
                    fontSize = 15.sp,
                    fontFamily = RajdhaniFontFamily,
                    color = Color.DarkGray
                )
            }
        } else {
            items(transactionLogs.take(15)) { log ->
                val cardName = if (log.cardIndex >= 0) CyberTarotDeck.getCard(log.cardIndex).name else "CREDIT NODE"
                val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
                val dateString = dateFormat.format(Date(log.timestamp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "[$dateString] ${log.type}: $cardName -> ${log.targetParty}",
                        fontSize = 14.sp,
                        fontFamily = RajdhaniFontFamily,
                        color = when (log.type) {
                            "BUY" -> NeonCyan
                            "SELL" -> NeonGold
                            "MINT" -> NeonMagenta
                            "MINE" -> NeonGreen
                            else -> Color.Gray
                        },
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${if (log.type == "BUY" || log.type == "MINT") "-" else "+"}${log.price} N$",
                        fontSize = 14.sp,
                        fontFamily = RajdhaniFontFamily,
                        color = if (log.type == "BUY" || log.type == "MINT") NeonRed else NeonGreen
                    )
                }
            }
        }
    }

    // Modal Overlays for Market Screen
    cardToInspectReview?.let { listing ->
        FullScreenCardReview(
            card = CyberTarotDeck.getCard(listing.cardIndex),
            mintedCard = MintedCard(cardIndex = listing.cardIndex, rarity = listing.rarity, mintNumber = listing.mintNumber),
            isMarketView = true,
            systemLanguage = lang,
            skin = currentSkin,
            onClose = { cardToInspectReview = null },
            onActionClick = {
                cardToBuyConfirm = listing
                cardToInspectReview = null
            }
        )
    }

    cardToBuyConfirm?.let { listing ->
        val card = CyberTarotDeck.getCard(listing.cardIndex)
        AlertDialog(
            onDismissRequest = { cardToBuyConfirm = null },
            containerColor = CyberCharcoal,
            title = {
                Text(
                    text = "AUTHORIZE TRANSACTION",
                    color = NeonGold,
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Do you want to authorize secure credit escrow transfer for this node?",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF09090E), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "NODE: ${card.name.uppercase()}",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 16.sp,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "SERIAL: #${String.format("%04d", listing.mintNumber)}",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 15.sp,
                                color = Color(0xFFB0B0C5)
                            )
                            Text(
                                text = "RARITY: ${listing.rarity.uppercase()}",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 15.sp,
                                color = when (listing.rarity) {
                                    "Legend" -> NeonGold
                                    "Epic" -> NeonMagenta
                                    "Rare" -> NeonCyan
                                    else -> NeonGreen
                                }
                            )
                            HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "ESCROW DEBT:",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 16.sp,
                                    color = Color(0xFFB0B0C5)
                                )
                                Text(
                                    "${listing.price} N$",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 16.sp,
                                    color = NeonGold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                CyberTextButton(
                    onClick = {
                        viewModel.buyCardFromMarket(listing)
                        cardToBuyConfirm = null
                    },
                    text = "CONFIRM DECREE",
                    skin = currentSkin,
                    containerColor = NeonCyan,
                    contentColor = CyberObsidian,
                    shape = RoundedCornerShape(12.dp)
                )
            },
            dismissButton = {
                CyberTextButton(
                    onClick = { cardToBuyConfirm = null },
                    text = "ABORT",
                    skin = currentSkin,
                    containerColor = Color.Transparent,
                    contentColor = Color.Gray,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        )
    }
}

fun getCardImage(cardIndex: Int): Int? {
    return when (cardIndex) {
        0 -> R.drawable.img_the_fool_1782996863113
        1 -> R.drawable.img_the_netrunner_1783468576249
        2 -> R.drawable.img_cyber_tarot_witch_1783983295253
        3 -> R.drawable.img_van_gogh_starry_herb_1783981881044
        4 -> R.drawable.img_van_gogh_sunflowers_bud_1783981894692
        5 -> R.drawable.img_van_gogh_irises_leaves_1783981907333
        6 -> R.drawable.img_the_ai_oracle_1783468589540
        7 -> R.drawable.img_the_sysadmin_1783468611107
        8 -> R.drawable.img_the_mainframe_1783468599161
        9 -> R.drawable.img_cyber_alchemy_lab_1783983282176
        10 -> R.drawable.img_cyber_fantasy_neon_garden_1783983267886
        11 -> R.drawable.img_the_global_network_1783468623225
        12 -> R.drawable.img_card_12_flushing_stage_1784330116067
        13 -> R.drawable.img_card_13_harvest_1784330129988
        14 -> R.drawable.img_card_14_climate_controller_1784330140630
        15 -> R.drawable.img_card_15_spider_mite_attack_1784330152550
        16 -> R.drawable.img_card_16_crop_collapse_1784330162213
        17 -> R.drawable.img_card_17_trichome_sparkle_1784330173456
        18 -> R.drawable.img_card_18_guerrilla_growing_1784330187119
        19 -> R.drawable.img_card_19_grow_light_1784330200766
        20 -> R.drawable.img_card_20_cup_submission_1784330210940
        21 -> R.drawable.img_card_21_green_planet_1784330222449
        22 -> R.drawable.img_card_22_electron_1784675315017
        23 -> R.drawable.img_card_23_proton_core_1784675327500
        24 -> R.drawable.img_card_24_hydrogen_atom_1784675346980
        25 -> R.drawable.img_card_25_dna_helix_1784675358888
        26 -> R.drawable.img_card_26_genesis_singularity_1784675370658
        27 -> R.drawable.amber_baltic_resin_1784675823170
        28 -> R.drawable.amber_trichomes_1784675836926
        29 -> R.drawable.ocean_coral_reef_1784675848971
        30 -> R.drawable.ocean_leviathan_1784675860484
        else -> null
    }
}

fun getCardBackImage(series: String): Int {
    return when (series) {
        "Genesis Series" -> R.drawable.back_genesis_series_1792151368530
        "Amber Series" -> R.drawable.back_amber_series_1792151382337
        "Seas & Oceans" -> R.drawable.back_ocean_series_1792151396531
        else -> R.drawable.img_card_back_cyber_luxury
    }
}

// ==========================================
// 4. CYBER DECK (Rider-Waite Reference Screen)
// ==========================================

@Composable
fun CardSymbolIcon(cardIndex: Int, tintColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when (cardIndex) {
            0 -> Icon(Icons.Default.Spa, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            1 -> Icon(Icons.Default.Science, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            2 -> Icon(Icons.Default.AcUnit, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            3 -> Icon(Icons.Default.Eco, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            4 -> Icon(Icons.Default.Build, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            5 -> Icon(Icons.Default.Agriculture, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            6 -> Icon(Icons.Default.Favorite, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            7 -> Icon(Icons.Default.Speed, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            8 -> Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            9 -> Icon(Icons.Default.FilterVintage, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            10 -> Icon(Icons.Default.Widgets, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            11 -> Icon(Icons.Default.DoneAll, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            12 -> Icon(Icons.Default.WaterDrop, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            13 -> Icon(Icons.Default.ContentCut, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            14 -> Icon(Icons.Default.Thermostat, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            15 -> Icon(Icons.Default.BugReport, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            16 -> Icon(Icons.Default.FlashOff, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            17 -> Icon(Icons.Default.Star, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            18 -> Icon(Icons.Default.Bedtime, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            19 -> Icon(Icons.Default.WbSunny, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            20 -> Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            21 -> Icon(Icons.Default.Public, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            22 -> Icon(Icons.Default.Bolt, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            23 -> Icon(Icons.Default.Adjust, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            24 -> Icon(Icons.Default.BubbleChart, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            25 -> Icon(Icons.Default.Biotech, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            26 -> Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            27 -> Icon(Icons.Default.Diamond, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            28 -> Icon(Icons.Default.WaterDrop, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            29 -> Icon(Icons.Default.Waves, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            30 -> Icon(Icons.Default.Tsunami, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
            else -> Icon(Icons.Default.Eco, contentDescription = null, tint = tintColor, modifier = Modifier.fillMaxSize())
        }
    }
}

fun Modifier.neonGlowEffect(
    color: Color,
    radius: Dp,
    pulseFactor: Float = 1.0f,
    cornerRadius: Dp = 12.dp
): Modifier = drawBehind {
    val sizePx = size
    val radiusPx = radius.toPx() * pulseFactor
    val layers = 4
    for (i in 1..layers) {
        val fraction = i.toFloat() / layers
        val currentRadius = radiusPx * fraction
        val alpha = 0.18f * (1.0f - fraction)
        drawRoundRect(
            color = color.copy(alpha = alpha),
            size = sizePx,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx()),
            style = Stroke(width = currentRadius)
        )
    }
}

@Composable
fun DeckScreen(viewModel: TarotViewModel) {
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val lang = profile?.systemLanguage ?: "en"
    val mintedCards by viewModel.mintedCards.collectAsStateWithLifecycle()
    val marketListings by viewModel.marketListings.collectAsStateWithLifecycle()

    var selectedInspectCard by remember { mutableStateOf<CyberTarotCard?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedRarityFilter by remember { mutableStateOf("All") }
    var selectedSeriesFilter by remember { mutableStateOf("All Series") }
    val flippedCardsMap = remember { mutableStateMapOf<Int, Boolean>() }

    var currentDeckSubTab by remember { mutableStateOf(0) } // 0: Registry, 1: Analytics

    // Interactive CSS Grid simulation controls
    var columnCount by remember { mutableStateOf(2) } // 1, 2, 3, or 4 columns
    var gridGap by remember { mutableStateOf(10f) } // 4dp to 24dp spacing
    var glowSpread by remember { mutableStateOf(6f) } // 2dp to 14dp glow radius
    var glowColorPreset by remember { mutableStateOf("Rarity Matched") } // "Cyber Cyan", "Matrix Green", "Sunset Magenta", "Royal Gold", "Rarity Matched"
    var presetStyle by remember { mutableStateOf("Standard CSS Grid") } // "Standard CSS Grid", "Neon Matrix Grid", "Holographic Command Table"
    var infoMode by remember { mutableStateOf("Digital Analog") } // "Digital Analog", "Traditional Rider-Waite"
    var showGridConfig by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseFactor by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_anim"
    )

    val filteredCards = remember(searchQuery, selectedRarityFilter, selectedSeriesFilter, lang) {
        CyberTarotDeck.cards.filter { card ->
            val localizedName = CardLocalizationHelper.getLocalizedName(card.index, lang)
            val localizedTraditional = CardLocalizationHelper.getLocalizedTraditionalName(card.index, lang)
            val localizedDesc = CardLocalizationHelper.getLocalizedDescription(card.index, lang)
            val matchesSearch = localizedName.contains(searchQuery, ignoreCase = true) ||
                    localizedTraditional.contains(searchQuery, ignoreCase = true) ||
                    localizedDesc.contains(searchQuery, ignoreCase = true)
            val matchesRarity = selectedRarityFilter == "All" || card.defaultRarity.equals(selectedRarityFilter, ignoreCase = true)
            val matchesSeries = selectedSeriesFilter == "All Series" || card.series.equals(selectedSeriesFilter, ignoreCase = true)
            matchesSearch && matchesRarity && matchesSeries
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 54.dp)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (currentDeckSubTab == 0) {
            Text(
                text = "DECK",
                fontSize = 24.sp,
                color = NeonMagenta,
                fontFamily = RajdhaniFontFamily,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("deck_title")
            )
            Text(
                text = "Rider-Waite Symbols",
                fontSize = 16.sp,
                color = Color(0xFFB0B0C5),
                fontFamily = RajdhaniFontFamily,
                modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(4.dp))
        }

        // SubTab Selection Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .background(Color(0xFF09090F), RoundedCornerShape(24.dp))
                .border(1.dp, Color(0x1F7F7F7F), RoundedCornerShape(24.dp))
                .padding(3.dp)
        ) {
            val tabs = listOf(
                Pair(0, "REGISTRY"),
                Pair(1, "BUILDER"),
                Pair(2, "FORGE"),
                Pair(3, "DUEL"),
                Pair(4, "ANALYTICS")
            )
            val tabColors = listOf(
                NeonMagenta,
                Color(0xFF00F5FF),
                Color(0xFFFFD700),
                Color(0xFF00FF66),
                NeonCyan
            )

            tabs.forEachIndexed { idx, (tabIndex, title) ->
                val isSelected = currentDeckSubTab == tabIndex
                val color = tabColors[idx]
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) color.copy(alpha = 0.2f) else Color.Transparent)
                        .border(1.dp, if (isSelected) color else Color.Transparent, RoundedCornerShape(16.dp))
                        .clickable { currentDeckSubTab = tabIndex }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        title,
                        color = if (isSelected) Color.White else Color(0xFFB0B0C5),
                        fontFamily = RajdhaniFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        if (currentDeckSubTab == 0) {
            // Registry controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Cyber Deck", fontFamily = RajdhaniFontFamily) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedLabelColor = NeonCyan,
                        unfocusedLabelColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 16.sp),
                    modifier = Modifier.weight(1f)
                )

                // Toggle CSS Grid Config Panel
                IconButton(
                    onClick = { showGridConfig = !showGridConfig },
                    modifier = Modifier
                        .size(54.dp)
                        .background(if (showGridConfig) NeonCyan.copy(alpha = 0.15f) else CyberCharcoal, RoundedCornerShape(8.dp))
                        .border(1.dp, if (showGridConfig) NeonCyan else Color.DarkGray, RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Grid System Config",
                        tint = if (showGridConfig) NeonCyan else Color.White
                    )
                }
            }

            // Expanded CSS Grid Config Panel
            AnimatedVisibility(
                visible = showGridConfig,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A14)),
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "CSS GRID ENGINE CONFIGURATION",
                                color = NeonCyan,
                                fontSize = 14.sp,
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                TextButton(
                                    onClick = {
                                        CyberTarotDeck.cards.forEach { card ->
                                            flippedCardsMap[card.index] = true
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("FLIP ALL", fontSize = 17.sp, color = NeonGreen, fontFamily = RajdhaniFontFamily, fontWeight = FontWeight.Bold)
                                }
                                TextButton(
                                    onClick = {
                                        flippedCardsMap.clear()
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("LOCK ALL", fontSize = 17.sp, color = NeonRed, fontFamily = RajdhaniFontFamily, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // grid-template-columns Row
                        Text(
                            text = "grid-template-columns: repeat(${columnCount}, 1fr);",
                            color = Color.LightGray,
                            fontSize = 14.sp,
                            fontFamily = RajdhaniFontFamily
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(1, 2, 3, 4).forEach { col ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (columnCount == col) NeonCyan.copy(alpha = 0.2f) else CyberCharcoal, RoundedCornerShape(4.dp))
                                        .border(1.dp, if (columnCount == col) NeonCyan else Color.DarkGray, RoundedCornerShape(4.dp))
                                        .clickable { columnCount = col }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("${col} Col", fontSize = 17.sp, color = if (columnCount == col) NeonCyan else Color(0xFFB0B0C5), fontFamily = RajdhaniFontFamily, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // grid-gap and glow radius Slider Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "grid-gap: ${gridGap.toInt()}dp;",
                                    color = Color.LightGray,
                                    fontSize = 16.sp,
                                    fontFamily = RajdhaniFontFamily
                                )
                                Slider(
                                    value = gridGap,
                                    onValueChange = { gridGap = it },
                                    valueRange = 4f..24f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = NeonCyan,
                                        activeTrackColor = NeonCyan,
                                        inactiveTrackColor = Color.DarkGray
                                    ),
                                    modifier = Modifier.height(24.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "box-shadow: ${glowSpread.toInt()}dp glow;",
                                    color = Color.LightGray,
                                    fontSize = 16.sp,
                                    fontFamily = RajdhaniFontFamily
                                )
                                Slider(
                                    value = glowSpread,
                                    onValueChange = { glowSpread = it },
                                    valueRange = 2f..14f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = NeonCyan,
                                        activeTrackColor = NeonCyan,
                                        inactiveTrackColor = Color.DarkGray
                                    ),
                                    modifier = Modifier.height(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Theme Preset selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("GRID-STYLE", color = Color(0xFFB0B0C5), fontSize = 16.sp, fontFamily = RajdhaniFontFamily)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf("Standard CSS Grid", "Neon Matrix Grid", "Holographic Command Table").forEach { style ->
                                        val isSel = presetStyle == style
                                        val abbr = when (style) {
                                            "Standard CSS Grid" -> "CSS"
                                            "Neon Matrix Grid" -> "Neon"
                                            else -> "Holo"
                                        }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(if (isSel) NeonMagenta.copy(alpha = 0.2f) else CyberCharcoal, RoundedCornerShape(4.dp))
                                                .border(1.dp, if (isSel) NeonMagenta else Color.DarkGray, RoundedCornerShape(4.dp))
                                                .clickable { presetStyle = style }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(abbr, fontSize = 16.sp, color = if (isSel) NeonMagenta else Color(0xFFB0B0C5), fontFamily = RajdhaniFontFamily)
                                        }
                                    }
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text("GLOW-COLOR", color = Color(0xFFB0B0C5), fontSize = 16.sp, fontFamily = RajdhaniFontFamily)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf("Rarity Matched", "Cyber Cyan", "Sunset Magenta").forEach { colorPreset ->
                                        val isSel = glowColorPreset == colorPreset
                                        val abbr = when (colorPreset) {
                                            "Rarity Matched" -> "Rare"
                                            "Cyber Cyan" -> "Cyan"
                                            else -> "Mage"
                                        }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(if (isSel) NeonGreen.copy(alpha = 0.2f) else CyberCharcoal, RoundedCornerShape(4.dp))
                                                .border(1.dp, if (isSel) NeonGreen else Color.DarkGray, RoundedCornerShape(4.dp))
                                                .clickable { glowColorPreset = colorPreset }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(abbr, fontSize = 16.sp, color = if (isSel) NeonGreen else Color(0xFFB0B0C5), fontFamily = RajdhaniFontFamily)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Info mode toggler
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("CARD DISPLAY MODE:", color = Color(0xFFB0B0C5), fontSize = 16.sp, fontFamily = RajdhaniFontFamily)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Digital Analog", "Traditional Rider-Waite").forEach { mode ->
                                    val isSel = infoMode == mode
                                    Box(
                                        modifier = Modifier
                                            .background(if (isSel) ElegantViolet.copy(alpha = 0.3f) else CyberCharcoal, RoundedCornerShape(4.dp))
                                            .border(1.dp, if (isSel) ElegantViolet else Color.DarkGray, RoundedCornerShape(4.dp))
                                            .clickable { infoMode = mode }
                                            .padding(horizontal = 10.dp, vertical = 8.dp)
                                    ) {
                                        Text(mode, fontSize = 16.sp, color = if (isSel) Color.White else Color(0xFFB0B0C5), fontFamily = RajdhaniFontFamily)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Series Selection Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All Series", "Cyber Botanica", "Genesis Series", "Amber Series", "Seas & Oceans").forEach { seriesName ->
                    val isSelected = selectedSeriesFilter == seriesName
                    val seriesColor = when (seriesName) {
                        "Genesis Series" -> Color(0xFFFFD700)
                        "Amber Series" -> Color(0xFFFF9900)
                        "Seas & Oceans" -> NeonCyan
                        "Cyber Botanica" -> NeonGreen
                        else -> Color.White
                    }
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) seriesColor.copy(alpha = 0.25f) else CyberCharcoal,
                                RoundedCornerShape(16.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) seriesColor else Color.DarkGray,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedSeriesFilter = seriesName }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = seriesName.uppercase(),
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 15.sp,
                            color = if (isSelected) seriesColor else Color(0xFFB0B0C5),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Scrollable Rarity Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Common", "Rare", "Epic", "Legend", "Genesis").forEach { rarity ->
                    val isSelected = selectedRarityFilter == rarity
                    val filterColor = when (rarity) {
                        "Genesis" -> Color(0xFFFFD700)
                        "Legend" -> NeonGold
                        "Epic" -> NeonMagenta
                        "Rare" -> NeonCyan
                        "Common" -> NeonGreen
                        else -> Color.White
                    }
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) filterColor.copy(alpha = 0.2f) else CyberCharcoal,
                                RoundedCornerShape(16.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) filterColor else Color.DarkGray,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedRarityFilter = rarity }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = rarity.uppercase(),
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 15.sp,
                            color = if (isSelected) filterColor else Color(0xFFB0B0C5),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // CSS Grid Matrix Container
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (presetStyle == "Holographic Command Table") {
                            Color(0xFF030307)
                        } else Color.Transparent
                    )
                    .drawBehind {
                        if (presetStyle == "Holographic Command Table") {
                            // Draw glowing cyber grid board
                            val gridSpacing = 60.dp.toPx()
                            val width = size.width
                            val height = size.height
                            
                            // Verticals
                            var x = 0f
                            while (x < width) {
                                drawLine(
                                    color = NeonCyan.copy(alpha = 0.08f),
                                    start = Offset(x, 0f),
                                    end = Offset(x, height),
                                    strokeWidth = 1f
                                )
                                x += gridSpacing
                            }
                            
                            // Horizontals
                            var y = 0f
                            while (y < height) {
                                drawLine(
                                    color = NeonCyan.copy(alpha = 0.08f),
                                    start = Offset(0f, y),
                                    end = Offset(width, y),
                                    strokeWidth = 1f
                                )
                                y += gridSpacing
                            }
                        }
                    }
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columnCount),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    horizontalArrangement = Arrangement.spacedBy(gridGap.dp),
                    verticalArrangement = Arrangement.spacedBy(gridGap.dp)
                ) {
                    items(filteredCards) { card ->
                        val rarityColor = remember(card.defaultRarity) {
                            when (card.defaultRarity) {
                                "Legend" -> NeonGold
                                "Epic" -> NeonMagenta
                                "Rare" -> NeonCyan
                                else -> NeonGreen
                            }
                        }

                        val chosenGlowColor = remember(glowColorPreset, card.defaultRarity) {
                            when (glowColorPreset) {
                                "Cyber Cyan" -> NeonCyan
                                "Matrix Green" -> NeonGreen
                                "Sunset Magenta" -> NeonMagenta
                                "Royal Gold" -> NeonGold
                                else -> rarityColor
                            }
                        }

                        val isFlipped = flippedCardsMap[card.index] ?: false
                        val rotation by animateFloatAsState(
                            targetValue = if (isFlipped) 180f else 0f,
                            animationSpec = tween(
                                durationMillis = 600,
                                easing = FastOutSlowInEasing
                            ),
                            label = "deck_card_flip"
                        )

                        // Compute dynamic grid coordinate
                        val rowIdx = card.index / columnCount
                        val colIdx = card.index % columnCount
                        val coordLabel = "R${String.format("%02d", rowIdx + 1)}:C${String.format("%02d", colIdx + 1)}"

                        val cardOuterGlow = if (presetStyle == "Neon Matrix Grid") glowSpread.dp * 1.5f else glowSpread.dp

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (presetStyle == "Neon Matrix Grid") CyberCharcoal.copy(alpha = 0.75f) else CyberCharcoal
                            ),
                            border = BorderStroke(
                                width = if (presetStyle == "Standard CSS Grid") 1.dp else 1.5.dp,
                                color = if (isFlipped) chosenGlowColor else CyberMutedBlue
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .neonGlowEffect(
                                    color = if (isFlipped) chosenGlowColor else chosenGlowColor.copy(alpha = 0.3f),
                                    radius = cardOuterGlow,
                                    pulseFactor = if (presetStyle == "Neon Matrix Grid") pulseFactor else 1.0f,
                                    cornerRadius = 12.dp
                                )
                                .clickable {
                                    selectedInspectCard = card
                                    com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.CARD_DRAW)
                                }
                                .graphicsLayer {
                                    rotationY = rotation
                                    cameraDistance = 12 * density
                                }
                        ) {
                            if (rotation <= 90f) {
                                // Card Front view (default in deck)
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (presetStyle == "Holographic Command Table") "[$coordLabel]" else "NODE #${String.format("%02d", card.index)}",
                                                fontSize = 14.sp,
                                                fontFamily = RajdhaniFontFamily,
                                                color = if (presetStyle == "Holographic Command Table") NeonCyan else Color(0xFFB0B0C5)
                                            )
                                            Text(
                                                text = card.defaultRarity.uppercase(),
                                                fontSize = 17.sp,
                                                fontFamily = RajdhaniFontFamily,
                                                color = rarityColor,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Central Artwork Area (Post-Impressionist/Van Gogh Tarot Artwork)
                                        val imgResId = getCardImage(card.index)
                                        if (imgResId != null) {
                                            Image(
                                                painter = painterResource(id = imgResId),
                                                contentDescription = "${card.name} - Art Preview",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(68.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .border(1.dp, rarityColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(68.dp)
                                                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                                    .border(0.5.dp, rarityColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CardSymbolIcon(
                                                    cardIndex = card.index,
                                                    tintColor = rarityColor,
                                                    modifier = Modifier.size(32.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        val localizedName = CardLocalizationHelper.getLocalizedName(card.index, lang)
                                        val localizedTraditional = CardLocalizationHelper.getLocalizedTraditionalName(card.index, lang)
                                        
                                        Text(
                                            text = if (infoMode == "Digital Analog") localizedName else localizedTraditional,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFamily = RajdhaniFontFamily,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (infoMode == "Digital Analog") "($localizedTraditional)" else "($localizedName)",
                                            fontSize = 16.sp,
                                            fontFamily = RajdhaniFontFamily,
                                            color = Color.Yellow,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    CyberButton(
                                        onClick = {
                                            selectedInspectCard = card
                                            com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.CARD_DRAW)
                                        },
                                        skin = currentSkin,
                                        containerColor = rarityColor,
                                        contentColor = CyberObsidian,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(30.dp)
                                    ) {
                                        AutoResizingText(
                                            text = "INSPECT ⛶",
                                            targetTextSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = RajdhaniFontFamily,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            } else {
                                // Card Back view with Ornate Series Design (flipped)
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer { rotationY = 180f }
                                        .clip(RoundedCornerShape(12.dp))
                                ) {
                                    val backImgRes = getCardBackImage(card.series)
                                    Image(
                                        painter = painterResource(id = backImgRes),
                                        contentDescription = "${card.series} Back",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .fillMaxWidth()
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(Color.Transparent, Color(0xCC05050A))
                                                )
                                            )
                                            .padding(bottom = 6.dp, top = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = card.series.uppercase(),
                                            fontSize = 14.sp,
                                            fontFamily = RajdhaniFontFamily,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (currentDeckSubTab == 1) {
            com.example.deckforge.DeckBuilderScreen(
                viewModel = viewModel,
                onLaunchDuel = { currentDeckSubTab = 3 }
            )
        } else if (currentDeckSubTab == 2) {
            com.example.deckforge.CardForgeScreen(
                viewModel = viewModel
            )
        } else if (currentDeckSubTab == 3) {
            com.example.duel.DuelArenaScreen(
                viewModel = viewModel,
                onBack = { currentDeckSubTab = 0 }
            )
        } else {
            NeuralAnalyticsView(mintedCards = mintedCards, marketListings = marketListings)
        }
    }

    // Detail Modal for selected inspect card (Styled with full Neon and cyber grid aesthetic)
    selectedInspectCard?.let { card ->
        FullScreenCardReview(
            card = card,
            mintedCard = mintedCards.find { it.cardIndex == card.index },
            isMarketView = false,
            systemLanguage = lang,
            skin = currentSkin,
            onClose = { selectedInspectCard = null },
            onActionClick = { selectedInspectCard = null }
        )
    }
}

@Composable
fun CardSynthesisView(viewModel: TarotViewModel) {
    val mintedCards by viewModel.mintedCards.collectAsStateWithLifecycle()
    val unlistedOwned = remember(mintedCards) { mintedCards.filter { !it.isListed } }

    val commonCards = remember(unlistedOwned) { unlistedOwned.filter { it.rarity.equals("Common", ignoreCase = true) } }
    val rareCards = remember(unlistedOwned) { unlistedOwned.filter { it.rarity.equals("Rare", ignoreCase = true) } }
    val epicCards = remember(unlistedOwned) { unlistedOwned.filter { it.rarity.equals("Epic", ignoreCase = true) } }

    var isFusing by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0B18)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, NeonGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NeonGold, modifier = Modifier.size(24.dp))
                        Text(
                            "SYNTHESIS MATRIX / СИНТЕЗ КАРТ",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGold,
                            fontFamily = RajdhaniFontFamily
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Fuse lower rarity cards into higher tiers according to the Genesis Formula. 10 Epic cards fuse into 1 Gold Genesis Series card ('Начало Начал').",
                        fontSize = 16.sp,
                        color = Color.LightGray,
                        fontFamily = RajdhaniFontFamily,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // 1. Common -> Rare (4 Commons -> 1 Rare)
        item {
            SynthesisRecipeCard(
                title = "COMMON ➔ RARE",
                subtitle = "4 Common Cards ➔ 1 Rare NFT Card",
                ownedCount = commonCards.size,
                requiredCount = 4,
                accentColor = NeonGreen,
                targetColor = NeonCyan,
                buttonText = "FUSE 4 COMMONS ➔ 1 RARE",
                isFusing = isFusing,
                onFuse = {
                    isFusing = true
                    viewModel.fuseCards(com.example.data.TarotRepository.FusionType.COMMON_TO_RARE) {
                        isFusing = false
                    }
                }
            )
        }

        // 2. Rare -> Epic (4 Rares -> 1 Epic)
        item {
            SynthesisRecipeCard(
                title = "RARE ➔ EPIC",
                subtitle = "4 Rare Cards ➔ 1 Epic NFT Card",
                ownedCount = rareCards.size,
                requiredCount = 4,
                accentColor = NeonCyan,
                targetColor = NeonMagenta,
                buttonText = "FUSE 4 RARES ➔ 1 EPIC",
                isFusing = isFusing,
                onFuse = {
                    isFusing = true
                    viewModel.fuseCards(com.example.data.TarotRepository.FusionType.RARE_TO_EPIC) {
                        isFusing = false
                    }
                }
            )
        }

        // 3. Epic -> Legend (4 Epics -> 1 Legend)
        item {
            SynthesisRecipeCard(
                title = "EPIC ➔ LEGEND",
                subtitle = "4 Epic Cards ➔ 1 Legend NFT Card",
                ownedCount = epicCards.size,
                requiredCount = 4,
                accentColor = NeonMagenta,
                targetColor = NeonGold,
                buttonText = "FUSE 4 EPICS ➔ 1 LEGEND",
                isFusing = isFusing,
                onFuse = {
                    isFusing = true
                    viewModel.fuseCards(com.example.data.TarotRepository.FusionType.EPIC_TO_LEGEND) {
                        isFusing = false
                    }
                }
            )
        }

        // 4. Epic -> Genesis Gold (10 Epics -> 1 Genesis Gold)
        item {
            SynthesisRecipeCard(
                title = "EPIC ➔ GENESIS GOLD (НАЧАЛО НАЧАЛ)",
                subtitle = "10 Epic Cards ➔ 1 Genesis Gold Card (#22 Electron, #23 Proton, #24 Hydrogen...)",
                ownedCount = epicCards.size,
                requiredCount = 10,
                accentColor = NeonMagenta,
                targetColor = Color(0xFFFFD700),
                buttonText = "FUSE 10 EPICS ➔ 1 GENESIS GOLD",
                isFusing = isFusing,
                onFuse = {
                    isFusing = true
                    viewModel.fuseCards(com.example.data.TarotRepository.FusionType.EPIC_TO_GENESIS) {
                        isFusing = false
                    }
                }
            )
        }
    }
}

@Composable
fun SynthesisRecipeCard(
    title: String,
    subtitle: String,
    ownedCount: Int,
    requiredCount: Int,
    accentColor: Color,
    targetColor: Color,
    buttonText: String,
    isFusing: Boolean,
    onFuse: () -> Unit
) {
    val canFuse = ownedCount >= requiredCount && !isFusing

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF12121A)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (canFuse) targetColor else Color(0x33FFFFFF)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = targetColor,
                    fontFamily = RajdhaniFontFamily
                )
                Text(
                    "AVAILABLE: $ownedCount / $requiredCount",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (canFuse) NeonGreen else Color(0xFFB0B0C5),
                    fontFamily = RajdhaniFontFamily
                )
            }

            Text(
                subtitle,
                fontSize = 15.sp,
                color = Color.LightGray,
                fontFamily = RajdhaniFontFamily
            )

            Button(
                onClick = onFuse,
                enabled = canFuse,
                colors = ButtonDefaults.buttonColors(
                    containerColor = targetColor,
                    contentColor = Color.Black,
                    disabledContainerColor = Color(0xFF222230),
                    disabledContentColor = Color.DarkGray
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
            ) {
                Text(
                    if (isFusing) "SYNTHESIZING..." else buttonText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = RajdhaniFontFamily
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomBigKeypad(
    skin: com.example.ui.theme.CyberSkin,
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("C", "0", "⌫")
    )

    Column(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        keys.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { key ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1B1B2B))
                            .border(1.5.dp, if (key == "⌫" || key == "C") Color.DarkGray else skin.primaryColor.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
                            .clickable {
                                com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.CLICK)
                                when (key) {
                                    "⌫" -> onDeleteClick()
                                    "C" -> onClearClick()
                                    else -> onDigitClick(key)
                                }
                            }
                            .testTag("keypad_key_$key"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = key,
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = if (key == "⌫") NeonRed else if (key == "C") Color.LightGray else skin.primaryColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PinLockScreen(viewModel: TarotViewModel, onRestoreRequest: () -> Unit = {}) {
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val lang = profile?.systemLanguage ?: "en"
    val isRu = lang in listOf("ru", "uk")
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030307))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top area
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Lock",
                tint = NeonRed,
                modifier = Modifier.size(52.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = LocalizationHelper.getString(lang, "pin_required_title"),
                fontFamily = RajdhaniFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = NeonRed,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = LocalizationHelper.getString(lang, "pin_required_desc"),
                fontFamily = RajdhaniFontFamily,
                fontSize = 16.sp,
                color = Color(0xFFB0B0C5),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Mid area (dots + status)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Glow circle indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                for (i in 0 until 8) {
                    val filled = pinInput.length > i
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(if (filled) currentSkin.primaryColor else Color(0x22FFFFFF))
                            .border(1.5.dp, if (filled) currentSkin.primaryColor else Color(0xFFB0B0C5), CircleShape)
                    )
                }
            }

            if (pinError) {
                Text(
                    text = LocalizationHelper.getString(lang, "pin_error"),
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 16.sp,
                    color = NeonRed,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Keypad area
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            CustomBigKeypad(
                skin = currentSkin,
                onDigitClick = { digit ->
                    if (pinInput.length < 8) {
                        pinInput += digit
                        pinError = false
                        if (pinInput.length == 8) {
                            val success = viewModel.unlockApp(pinInput)
                            if (!success) {
                                pinError = true
                                pinInput = ""
                                com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.SCAN_BEEP)
                            } else {
                                com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
                            }
                        }
                    }
                },
                onDeleteClick = {
                    if (pinInput.isNotEmpty()) {
                        pinInput = pinInput.dropLast(1)
                    }
                },
                onClearClick = {
                    pinInput = ""
                    pinError = false
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            TextButton(
                onClick = onRestoreRequest,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = if (isRu) "🔑 Забыли ПИН? Восстановить из сид-фразы" else "🔑 Forgot PIN? Restore from Seed Phrase",
                    color = currentSkin.primaryColor,
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SafeBotanicalModeScreen(viewModel: TarotViewModel) {
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    var chatInput by remember { mutableStateOf("") }
    val chatHistory = remember { mutableStateListOf<Pair<String, Boolean>>() } // message to isUser

    LaunchedEffect(Unit) {
        if (chatHistory.isEmpty()) {
            chatHistory.add("Welcome to the Greenhouse Botanical Assistant! 🌿 Ask me anything about soil health, watering schedules, organic compost, or herbal horticulture. How can I help your garden bloom today?" to false)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C1610))
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Safe Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Eco,
                contentDescription = "Safe Mode",
                tint = NeonGreen,
                modifier = Modifier.size(36.dp)
            )
            Column {
                Text(
                    text = "BOTANICAL SAFE ASSISTANT",
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
                Text(
                    text = "Encrypted Local Herbology Core v2.4",
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 15.sp,
                    color = Color(0xFFB0B0C5)
                )
            }
        }

        HorizontalDivider(color = Color(0x3300FF00), thickness = 1.dp, modifier = Modifier.padding(bottom = 12.dp))

        // Chats list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(chatHistory) { (msg, isUser) ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) Color(0xFF1B3B2B) else Color(0xFF161F1B)
                        ),
                        border = BorderStroke(1.dp, if (isUser) Color(0x6600FF00) else Color(0x3300FF00)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        Text(
                            text = msg,
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 17.sp,
                            color = Color.White,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick tips Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Soil Nutrients 🧪", "Watering 💧", "Lighting ☀️", "Pruning ✂️").forEach { topic ->
                Box(
                    modifier = Modifier
                        .background(Color(0xFF14241C), RoundedCornerShape(20.dp))
                        .border(1.dp, Color(0x4400FF00), RoundedCornerShape(20.dp))
                        .clickable {
                            com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.CLICK)
                            val prompt = "Tell me about $topic best practices."
                            chatHistory.add(prompt to true)
                            val reply = when (topic) {
                                "Soil Nutrients 🧪" -> "Organic soils thrive on nitrogen, phosphorus, and potassium (N-P-K). For home gardens, compost tea, bone meal, and worm castings are exceptional ways to enrich soil microbiomes naturally without artificial burn!"
                                "Watering 💧" -> "The golden rule of watering is: check soil dryness 1 inch deep. It is far safer to underwater than overwater. Root rot is caused by stagnant water cutting off root oxygen. Always use well-drained pots with bottom drainage."
                                "Lighting ☀️" -> "Herbs and flowering foliage typically require 6-8 hours of direct sunlight. Indoors, placing a full-spectrum LED grow-light 12-18 inches above the canopy can perfectly simulate midday solar radiation for compact, healthy nodes."
                                else -> "Pruning encourages lateral branching! Always use sanitized scissors. Snip stems directly above node pairs to double your future flower canopy size and improve airflow, preventing mold development."
                            }
                            chatHistory.add(reply to false)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = topic,
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        color = NeonGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Input Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = chatInput,
                onValueChange = { chatInput = it },
                placeholder = { Text("Ask about botanical care...", color = Color(0xFFB0B0C5), fontSize = 16.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonGreen,
                    unfocusedBorderColor = Color(0x3300FF00),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 17.sp),
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = {
                    if (chatInput.isNotBlank()) {
                        com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
                        val query = chatInput
                        chatHistory.add(query to true)
                        chatInput = ""
                        // Generate botanical response
                        val response = generateBotanicalReply(query)
                        chatHistory.add(response to false)
                    }
                },
                modifier = Modifier
                    .background(NeonGreen, RoundedCornerShape(8.dp))
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.Black
                )
            }
        }
    }
}

private fun generateBotanicalReply(query: String): String {
    val q = query.lowercase()
    return when {
        q.contains("water") || q.contains("hydro") || q.contains("dry") -> {
            "Maintain a steady cycle. Water thoroughly until it drains, then allow the top soil layers to dry out. Excess humidity promotes root fungi."
        }
        q.contains("light") || q.contains("sun") || q.contains("spectrum") -> {
            "Flowering plants love light! Aim for 12-18 hours of spectrum lighting during the growth cycle, dialing back during resting periods."
        }
        q.contains("soil") || q.contains("fertilizer") || q.contains("nutrient") || q.contains("npk") -> {
            "An aerated soil mix with perlite allows roots to breathe. Organic compost feeds helpful microbes, keeping nutrient absorption smooth."
        }
        else -> {
            "That sounds like a fascinating pursuit! Remember, plants respond beautifully to stable temperatures (21-26°C), gentle clean airflow, and proper care. Keep the environment clean!"
        }
    }
}

@Composable
fun OnboardingWizard(viewModel: TarotViewModel, onComplete: () -> Unit) {
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    var currentStep by remember { mutableStateOf(0) } // 0: Lang & Security Notice, 1: Mnemonic Seed, 2: Seed Verification, 3: Alias/Avatar, 4: Container/P2P, 5: PIN setup
    var isRestoreMode by remember { mutableStateOf(false) }
    var restoreSeedInput by remember { mutableStateOf("") }
    var restorePinInput by remember { mutableStateOf("") }
    var restoreError by remember { mutableStateOf("") }

    val tempLang = remember { mutableStateOf(profile?.systemLanguage ?: "en") }
    val aliasInput = remember { mutableStateOf("") }
    val selectedAvatarIdx = remember { mutableStateOf(0) }
    val allocatedStorageGb = remember { mutableStateOf(16f) }
    
    // Generate real cryptographically secure random BIP39 phrase once
    val randomEntropy = remember {
        val arr = ByteArray(32)
        java.security.SecureRandom().nextBytes(arr)
        arr
    }
    val generatedMnemonicList = remember {
        try {
            com.example.data.Bip39Helper.generateMnemonic(randomEntropy)
        } catch (e: Exception) {
            listOf("weed", "guru", "botany", "decentralized", "cloud", "security", "node", "tor", "onion", "bip39", "encryption", "wallet")
        }
    }

    // Challenge verification states
    val testIndices = remember {
        (0..23).shuffled().take(3).sorted()
    }
    val verificationOptions = remember {
        testIndices.associateWith { correctIdx ->
            val correctWord = generatedMnemonicList[correctIdx]
            val others = (1..3).map {
                var w = com.example.data.Bip39Wordlist.words.random()
                while (w == correctWord) { w = com.example.data.Bip39Wordlist.words.random() }
                w
            }
            (others + correctWord).shuffled()
        }
    }
    val verificationAnswers = remember { mutableStateMapOf<Int, String>() }

    // Keypad states for PINs
    val masterPin = remember { mutableStateOf("") }
    val safePin = remember { mutableStateOf("11111111") }
    val panicPin = remember { mutableStateOf("") } // User must set their own Panic Pin!
    val pinSubStep = remember { mutableStateOf(0) } // 0: Master PIN, 1: Panic PIN
    var setupError by remember { mutableStateOf("") }

    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    // Set temporary language dynamically
    LaunchedEffect(tempLang.value) {
        viewModel.updateSystemLanguage(tempLang.value)
    }

    val t = { key: String -> LocalizationHelper.getString(tempLang.value, key) }

    if (isRestoreMode) {
        // Dedicated Seed Phrase Restore Screen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF030308))
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (tempLang.value in listOf("ru", "uk")) "🔑 ВОССТАНОВЛЕНИЕ ИЗ СИД-ФРАЗЫ" else "🔑 BIP-39 SEED RESTORATION",
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = NeonCyan
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (tempLang.value in listOf("ru", "uk")) "Введите 12 или 24 слова вашей мнемоники и задайте новый 8-значный ПИН для быстрого входа." else "Enter your 12 or 24-word seed phrase and set a new 8-digit PIN for quick access.",
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 16.sp,
                    color = Color(0xFFB0B0C5),
                    textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = restoreSeedInput,
                    onValueChange = {
                        restoreSeedInput = it
                        restoreError = ""
                    },
                    placeholder = {
                        Text(
                            if (tempLang.value in listOf("ru", "uk")) "Вставьте или введите 12/24 слов через пробел..." else "Paste or type 12/24 words separated by spaces...",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            color = Color(0xFFB0B0C5)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 16.sp, color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color(0x33FFFFFF)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val wordCount = restoreSeedInput.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
                    Text(
                        text = if (tempLang.value in listOf("ru", "uk")) "Слов: $wordCount / 24" else "Words: $wordCount / 24",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        color = if (wordCount == 12 || wordCount == 24) NeonGreen else Color.LightGray
                    )
                    TextButton(
                        onClick = {
                            val clip = clipboardManager.getText()?.text ?: ""
                            if (clip.isNotBlank()) {
                                restoreSeedInput = clip.trim()
                            }
                        }
                    ) {
                        Text(
                            text = if (tempLang.value in listOf("ru", "uk")) "📋 ВСТАВИТЬ" else "📋 PASTE",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // PIN indicator
                Text(
                    text = if (tempLang.value in listOf("ru", "uk")) "🔢 ЗАДАЙТЕ 8-ЗНАЧНЫЙ ПИН-КОД ДЛЯ ВХОДА" else "🔢 CONFIGURE 8-DIGIT ENTRY PIN",
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    for (i in 0 until 8) {
                        val filled = restorePinInput.length > i
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(if (filled) NeonCyan else Color(0x22FFFFFF))
                                .border(1.5.dp, if (filled) NeonCyan else Color(0xFFB0B0C5), CircleShape)
                        )
                    }
                }

                if (restoreError.isNotBlank()) {
                    Text(
                        text = restoreError,
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        color = NeonRed,
                        fontWeight = FontWeight.Bold
                    )
                }

                CustomBigKeypad(
                    skin = currentSkin,
                    onDigitClick = { digit ->
                        if (restorePinInput.length < 8) {
                            restorePinInput += digit
                            restoreError = ""
                        }
                    },
                    onDeleteClick = {
                        if (restorePinInput.isNotEmpty()) {
                            restorePinInput = restorePinInput.dropLast(1)
                        }
                    },
                    onClearClick = {
                        restorePinInput = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Bottom Actions for Restore
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CyberButton(
                    onClick = { isRestoreMode = false },
                    skin = currentSkin,
                    modifier = Modifier
                        .width(120.dp)
                        .height(44.dp)
                ) {
                    Text(
                        text = if (tempLang.value in listOf("ru", "uk")) "⬅️ НАЗАД" else "⬅️ BACK",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                val wordsList = restoreSeedInput.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
                val isRestoreValid = (wordsList.size == 12 || wordsList.size == 24) && restorePinInput.length == 8

                CyberButton(
                    onClick = {
                        if (!com.example.data.Bip39Helper.validateWords(wordsList)) {
                            restoreError = if (tempLang.value in listOf("ru", "uk")) "❌ Некорректные слова мнемоники BIP-39" else "❌ Invalid BIP-39 mnemonic words"
                        } else if (restorePinInput.length != 8) {
                            restoreError = if (tempLang.value in listOf("ru", "uk")) "❌ ПИН должен содержать ровно 8 цифр" else "❌ PIN must be exactly 8 digits"
                        } else {
                            viewModel.saveSecuredProfileWithPin(
                                dob = "01-01-1970",
                                first = "Restored",
                                last = "Secured",
                                pin = restorePinInput,
                                pinSafe = "11111111",
                                pinPanic = "22222222",
                                aliasVal = "Grower",
                                avatarIdx = 0,
                                customWords = wordsList
                            )
                            com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
                            onComplete()
                        }
                    },
                    skin = currentSkin,
                    modifier = Modifier
                        .width(160.dp)
                        .height(44.dp)
                        .graphicsLayer { alpha = if (isRestoreValid) 1f else 0.4f },
                    enabled = isRestoreValid
                ) {
                    Text(
                        text = if (tempLang.value in listOf("ru", "uk")) "ВОССТАНОВИТЬ 🔓" else "RESTORE 🔓",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030308))
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Step progress header
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STEP ${currentStep + 1} OF 6",
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = currentSkin.primaryColor
                )
                Text(
                    text = when (currentStep) {
                        0 -> "🌐 LANGUAGE & RESPONSIBILITY"
                        1 -> "🌱 SEED MEMORY CORE"
                        2 -> "🧠 NEURAL CHALLENGE"
                        3 -> "👤 LOCAL GROWER IDENT"
                        4 -> "📦 DECENTRALIZED CONTAINERS"
                        else -> "🔢 SECURITY INTERLOCK"
                    },
                    fontFamily = RajdhaniFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFFB0B0C5)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (i in 0 until 6) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (currentStep >= i) currentSkin.primaryColor else Color(0x33FFFFFF))
                    )
                }
            }
        }

        // Mid section scrollable
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (currentStep) {
                0 -> {
                    // Step 0: Language Selecting & Security Disclaimer
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "🌐 LOCALIZATION DISCOVERY SCAN",
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(currentSkin.primaryColor.copy(alpha = 0.05f))
                                .border(1.5.dp, currentSkin.primaryColor.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Lang",
                                tint = currentSkin.primaryColor,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        // Supported languages grid 2 columns
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            LocalizationHelper.languages.chunked(2).forEach { pair ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    pair.forEach { (code, name) ->
                                        val isSelected = tempLang.value == code
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) currentSkin.primaryColor.copy(alpha = 0.15f) else Color(0xFF161622))
                                                .border(1.5.dp, if (isSelected) currentSkin.primaryColor else Color(0x1AFFFFFF), RoundedCornerShape(10.dp))
                                                .clickable {
                                                    com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.CLICK)
                                                    tempLang.value = code
                                                }
                                                .padding(horizontal = 12.dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) currentSkin.primaryColor else Color(0xFFB0B0C5))
                                                )
                                                Text(
                                                    text = name,
                                                    fontFamily = RajdhaniFontFamily,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.White else Color.LightGray
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Self-Custodial Security Responsibility Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1111)),
                            border = BorderStroke(1.5.dp, NeonRed.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Warning",
                                        tint = NeonRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = if (tempLang.value == "ru") "⚠️ ВАЖНОЕ ПРЕДУПРЕЖДЕНИЕ БЕЗОПАСНОСТИ" else "⚠️ CRITICAL SECURITY WARNING",
                                        fontFamily = RajdhaniFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = NeonRed
                                    )
                                }
                                Text(
                                    text = if (tempLang.value == "ru") {
                                        "Это полностью децентрализованное приложение с некастодиальным кошельком. Здесь НЕТ центрального сервера, службы поддержки клиентов или механизма восстановления паролей.\n\n" +
                                        "• Только ВЫ отвечаете за сохранность вашей сид-фразы и ПИН-кодов.\n" +
                                        "• Если вы потеряете сид-фразу, никто и никогда не сможет восстановить ваш кошелек или ваши токены WEED.\n" +
                                        "• При утере данных они исчезнут навсегда."
                                    } else {
                                        "This is a fully decentralized, self-custodial neural application. There is NO central server, no customer support, and no password reset.\n\n" +
                                        "• ONLY YOU are responsible for saving your security credentials and PINs.\n" +
                                        "• If you lose your seed phrase, NO ONE can recover your wallet or access your WEED assets.\n" +
                                        "• All lost data is gone forever."
                                    },
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 16.sp,
                                    color = Color.LightGray,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick restore seed phrase trigger
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0C1425),
                            border = BorderStroke(1.5.dp, NeonCyan.copy(alpha = 0.7f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.CLICK)
                                    isRestoreMode = true
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = "Restore",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (tempLang.value in listOf("ru", "uk")) "🔑 У МЕНЯ ЕСТЬ СИД • ВОССТАНОВИТЬ" else "🔑 HAVE A SEED PHRASE? RESTORE",
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = NeonCyan
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // Step 1: BIP39 Seed Phrase Screen (24 words!)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = t("bip39_header"),
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (tempLang.value == "ru") "Запишите эту мнемоническую фразу из 24 слов на бумагу и сохраните в надежном месте." else "Please write down this 24-word mnemonic seed phrase on paper and save it securely.",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            color = Color(0xFFB0B0C5),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Seed Card grid
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF11111E)),
                            border = BorderStroke(1.5.dp, currentSkin.primaryColor.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "⚠️ " + t("seed_warning"),
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonRed,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                HorizontalDivider(color = Color(0x22FFFFFF), thickness = 1.dp)

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    generatedMnemonicList.chunked(3).forEach { chunk ->
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            chunk.forEach { word ->
                                                val idx = generatedMnemonicList.indexOf(word) + 1
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(34.dp)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color(0xFF1B1B2B))
                                                        .padding(horizontal = 8.dp),
                                                    contentAlignment = Alignment.CenterStart
                                                ) {
                                                    Text(
                                                        text = "$idx. $word",
                                                        fontFamily = RajdhaniFontFamily,
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Button(
                                    onClick = {
                                        com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
                                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(generatedMnemonicList.joinToString(" ")))
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = currentSkin.primaryColor.copy(alpha = 0.2f)),
                                    border = BorderStroke(1.dp, currentSkin.primaryColor),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().height(44.dp)
                                ) {
                                    Text(
                                        text = t("btn_copy"),
                                        fontFamily = RajdhaniFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Step 2: Seed Phrase Verification Challenge (Ensure they saved it!)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = if (tempLang.value == "ru") "🧠 ПРОВЕРКА СИД-ФРАЗЫ" else "🧠 NEURAL SEED VERIFICATION",
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (tempLang.value == "ru") "Выберите правильные слова для проверки, что вы действительно сохранили мнемонику." else "Select the correct words to verify you have securely saved your seed phrase.",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            color = Color(0xFFB0B0C5),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        testIndices.forEach { testIdx ->
                            val correctWord = generatedMnemonicList[testIdx]
                            val options = verificationOptions[testIdx] ?: emptyList()
                            val selected = verificationAnswers[testIdx]

                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F1A)),
                                border = BorderStroke(1.dp, if (selected == correctWord) NeonGreen.copy(alpha = 0.4f) else if (selected != null) NeonRed.copy(alpha = 0.4f) else Color.DarkGray),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = if (tempLang.value == "ru") "Какое слово идет под номером #${testIdx + 1}?" else "Identify Word #${testIdx + 1}:",
                                        fontFamily = RajdhaniFontFamily,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        options.take(2).forEach { option ->
                                            val isChosen = selected == option
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(38.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (isChosen) (if (option == correctWord) NeonGreen.copy(alpha = 0.2f) else NeonRed.copy(alpha = 0.2f)) else Color(0xFF1B1B2B))
                                                    .border(1.dp, if (isChosen) (if (option == correctWord) NeonGreen else NeonRed) else Color.Transparent, RoundedCornerShape(6.dp))
                                                    .clickable {
                                                        com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.CLICK)
                                                        verificationAnswers[testIdx] = option
                                                    }
                                                    .padding(horizontal = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = option,
                                                    fontFamily = RajdhaniFontFamily,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        options.drop(2).take(2).forEach { option ->
                                            val isChosen = selected == option
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(38.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (isChosen) (if (option == correctWord) NeonGreen.copy(alpha = 0.2f) else NeonRed.copy(alpha = 0.2f)) else Color(0xFF1B1B2B))
                                                    .border(1.dp, if (isChosen) (if (option == correctWord) NeonGreen else NeonRed) else Color.Transparent, RoundedCornerShape(6.dp))
                                                    .clickable {
                                                        com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.CLICK)
                                                        verificationAnswers[testIdx] = option
                                                    }
                                                    .padding(horizontal = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = option,
                                                    fontFamily = RajdhaniFontFamily,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // Step 3: Alias & Avatar Setup
                    val avatars = listOf(
                        "Neon Botanist",
                        "Cyber Farmer",
                        "Solar Harvester",
                        "Quantum Grower",
                        "Ganja Guru",
                        "Netrunner Alchemist"
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "👤 CHOOSE LOCAL GROWER IDENTITY",
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // Local Alias Input Field
                        OutlinedTextField(
                            value = aliasInput.value,
                            onValueChange = { aliasInput.value = it },
                            label = { Text("Local Alias (Username)", color = Color(0xFFB0B0C5), fontSize = 16.sp, fontFamily = RajdhaniFontFamily) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = currentSkin.primaryColor,
                                unfocusedBorderColor = Color(0x33FFFFFF),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 14.sp),
                            modifier = Modifier.fillMaxWidth().testTag("onboarding_alias_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Avatar List Grid
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            avatars.chunked(2).forEachIndexed { rowIdx, pair ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    pair.forEach { avName ->
                                        val curIdx = rowIdx * 2 + pair.indexOf(avName)
                                        val isSelected = selectedAvatarIdx.value == curIdx
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(56.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) currentSkin.primaryColor.copy(alpha = 0.15f) else Color(0xFF13131F))
                                                .border(1.5.dp, if (isSelected) currentSkin.primaryColor else Color(0x1AFFFFFF), RoundedCornerShape(10.dp))
                                                .clickable {
                                                    com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.CLICK)
                                                    selectedAvatarIdx.value = curIdx
                                                }
                                                .padding(horizontal = 12.dp, vertical = 6.dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) currentSkin.primaryColor else Color(0xFFB0B0C5)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Face,
                                                        contentDescription = "Avatar",
                                                        tint = Color.Black,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                Text(
                                                    text = avName,
                                                    fontFamily = RajdhaniFontFamily,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.White else Color.LightGray
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                4 -> {
                    // Step 4: Decentralized Storage Container Allocation Screen
                    val cloudEarned = allocatedStorageGb.value / 4f // 1:4 ratio
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "📦 DECENTRALIZED CRYPTO-CONTAINER",
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Choose your storage allocation to connect to the peer-to-peer Tor .onion grower grid network.",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            color = Color(0xFFB0B0C5),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Multi-choice Slider Container
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF10101C)),
                            border = BorderStroke(1.dp, Color(0x22FFFFFF)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "ALLOCATE STORAGE SPACE: ${allocatedStorageGb.value.toInt()} GB",
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = currentSkin.primaryColor
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Slider(
                                    value = allocatedStorageGb.value,
                                    onValueChange = {
                                        val stepped = when {
                                            it < 24 -> 16f
                                            it < 48 -> 32f
                                            it < 96 -> 64f
                                            else -> 128f
                                        }
                                        allocatedStorageGb.value = stepped
                                    },
                                    valueRange = 16f..128f,
                                    steps = 3,
                                    colors = SliderDefaults.colors(
                                        activeTrackColor = currentSkin.primaryColor,
                                        thumbColor = currentSkin.primaryColor
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("P2P SHARED", fontSize = 15.sp, color = Color(0xFFB0B0C5), fontFamily = RajdhaniFontFamily)
                                        Text("${allocatedStorageGb.value.toInt()} GB", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White, fontFamily = RajdhaniFontFamily)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("CLOUD BACKUP", fontSize = 15.sp, color = Color(0xFFB0B0C5), fontFamily = RajdhaniFontFamily)
                                        Text("${cloudEarned.toInt()} GB", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = NeonGreen, fontFamily = RajdhaniFontFamily)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Breathtaking Technical Study
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF121A16)),
                            border = BorderStroke(1.dp, Color(0x3300FF00)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "📖 DECENTRALIZED CRYPTO-CLOUD STUDY",
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = NeonGreen
                                )
                                Text(
                                    text = "This application hosts redundant botanical logs securely over a peer-to-peer Tor .onion address overlay network using modern concepts similar to Filecoin, IPFS, and Storj. No centralized logging server exists.",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 15.sp,
                                    color = Color.LightGray
                                )
                                HorizontalDivider(color = Color(0x2200FF00), thickness = 1.dp)
                                Text(
                                    text = "• INTEGRITY: All archives are cryptographically hashed and duplicated into 5 remote replicas + 1 standby replica across the global node grid.\n" +
                                           "• MONITORING: Node connection cycles are evaluated every 15 minutes. An offline duration >15m shifts node status to \"Questionable\".\n" +
                                           "• OUTAGES: If a peer node remains offline for over 1 hour, it is classified as \"Frozen\". All hosted replicas on that node are flagged for deletion to maintain network health, penalizing reputation and WEED.\n" +
                                           "• PENALTY CONTROL: Closing or migrating nodes safely without penalties can be initialized inside Settings at any time.",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 14.sp,
                                    color = Color(0xFFB0B0C5)
                                )
                            }
                        }
                    }
                }
                else -> {
                    // Step 5: Keyboard PIN Setup
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val currentPinState = when (pinSubStep.value) {
                            0 -> masterPin
                            else -> panicPin
                        }

                        Text(
                            text = when (pinSubStep.value) {
                                0 -> "🔢 1. MASTER ENTRY PIN"
                                else -> "🚨 2. EMERGENCY PANIC PIN"
                            },
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (pinSubStep.value == 1) NeonRed else currentSkin.primaryColor
                        )

                        Text(
                            text = when (pinSubStep.value) {
                                0 -> "Set up your main 8-digit PIN code to lock, decrypt, and authorize fast entry into the app."
                                else -> "Set an emergency PIN that immediately wipes all database tables, wipes RAM cache, and restarts instantly."
                            },
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            color = Color(0xFFB0B0C5),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        // 8 dot indicators
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(vertical = 10.dp)
                        ) {
                            for (i in 0 until 8) {
                                val filled = currentPinState.value.length > i
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(if (filled) currentSkin.primaryColor else Color(0x22FFFFFF))
                                        .border(1.5.dp, if (filled) currentSkin.primaryColor else Color(0xFFB0B0C5), CircleShape)
                                )
                            }
                        }

                        if (setupError.isNotBlank()) {
                            Text(
                                text = setupError,
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 16.sp,
                                color = NeonRed,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Massive key entry
                        CustomBigKeypad(
                            skin = currentSkin,
                            onDigitClick = { digit ->
                                if (currentPinState.value.length < 8) {
                                    currentPinState.value += digit
                                    setupError = ""
                                    if (currentPinState.value.length == 8) {
                                        // Auto proceed sub-step
                                        if (pinSubStep.value < 1) {
                                            pinSubStep.value += 1
                                            com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
                                        }
                                    }
                                }
                            },
                            onDeleteClick = {
                                if (currentPinState.value.isNotEmpty()) {
                                    currentPinState.value = currentPinState.value.dropLast(1)
                                }
                            },
                            onClearClick = {
                                currentPinState.value = ""
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Bottom navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentStep > 0) {
                CyberButton(
                    onClick = {
                        com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.CLICK)
                        if (currentStep == 5 && pinSubStep.value > 0) {
                            pinSubStep.value -= 1
                        } else {
                            currentStep -= 1
                        }
                    },
                    skin = currentSkin,
                    modifier = Modifier.width(110.dp).height(44.dp)
                ) {
                    Text(
                        text = "⬅️ BACK",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(110.dp))
            }

            val nextText = if (currentStep == 5 && pinSubStep.value == 1 && panicPin.value.length == 8) "ACTIVATE 🔒" else "NEXT ➡️"
            val isNextEnabled = when (currentStep) {
                1 -> true
                2 -> {
                    // Verification step: check if all 3 answers are filled correctly
                    testIndices.all { idx ->
                        verificationAnswers[idx] == generatedMnemonicList[idx]
                    }
                }
                3 -> aliasInput.value.isNotBlank()
                4 -> true
                5 -> {
                    val curState = when (pinSubStep.value) {
                        0 -> masterPin
                        else -> panicPin
                    }
                    curState.value.length == 8
                }
                else -> true
            }

            CyberButton(
                onClick = {
                    com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
                    when {
                        currentStep < 5 -> {
                            currentStep += 1
                        }
                        pinSubStep.value < 1 -> {
                            pinSubStep.value += 1
                        }
                        else -> {
                            // Verify pins uniqueness
                            if (masterPin.value == panicPin.value) {
                                setupError = "❌ PIN CODES MUST BE ENTIRELY UNIQUE."
                            } else {
                                // Save profile
                                viewModel.saveSecuredProfileWithPin(
                                    dob = "01-01-1970",
                                    first = aliasInput.value,
                                    last = "Secured",
                                    pin = masterPin.value,
                                    pinSafe = safePin.value,
                                    pinPanic = panicPin.value,
                                    aliasVal = aliasInput.value,
                                    avatarIdx = selectedAvatarIdx.value,
                                    customWords = generatedMnemonicList
                                )
                                onComplete()
                            }
                        }
                    }
                },
                skin = currentSkin,
                modifier = Modifier.width(130.dp).height(44.dp).graphicsLayer { alpha = if (isNextEnabled) 1f else 0.4f },
                enabled = isNextEnabled
            ) {
                Text(
                    text = nextText,
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

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

    val lang = profile?.systemLanguage ?: "en"
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

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

    // Proxy Configurations States (pre-populate on profile load)
    var proxyType by remember { mutableStateOf(profile?.proxyType ?: "NONE") }
    var proxyHost by remember { mutableStateOf(profile?.proxyHost ?: "127.0.0.1") }
    var proxyPortString by remember { mutableStateOf((profile?.proxyPort ?: 9050).toString()) }
    var proxyConfig by remember { mutableStateOf(profile?.proxyConfig ?: "") }
    var isProxyActive by remember { mutableStateOf(profile?.isProxyActive ?: false) }
    var proxyTestStatus by remember { mutableStateOf<String?>(null) }
    var isTestingProxy by remember { mutableStateOf(false) }

    // Sync state when profile is received
    LaunchedEffect(profile) {
        profile?.let {
            proxyType = it.proxyType
            proxyHost = it.proxyHost
            proxyPortString = it.proxyPort.toString()
            proxyConfig = it.proxyConfig
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
                        items(com.example.ui.theme.CyberSkin.values()) { skin ->
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

                val context = LocalContext.current
                
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
                                // Save profile
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
                        // Avatar graphic with glowing circle background or custom image
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
                                        androidx.activity.result.PickVisualMediaRequest(
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
                            text = "Owner: ${profile?.securedFirst?.replaceRange(1, (profile?.securedFirst?.length ?: 2) - 1, "*")} " +
                                   "${profile?.securedLast?.replaceRange(1, (profile?.securedLast?.length ?: 2) - 1, "*")}",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            color = Color.LightGray
                        )
                        Text(
                            text = "Birthdate: ${profile?.securedDob?.replaceRange(5, (profile?.securedDob?.length ?: 6) - 1, "**-**")}",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            color = Color.LightGray
                        )
                        
                        val rawSeed = profile?.seedPhrase ?: ""
                        val seedHash = remember(rawSeed) {
                            if (rawSeed.isNotEmpty()) {
                                Bip39Helper.generateEntropyHash(
                                    profile?.securedDob ?: "",
                                    profile?.securedFirst ?: "",
                                    profile?.securedLast ?: "",
                                    "SECURE_HASH"
                                ).joinToString("") { "%02x".format(it) }.take(24) + "..."
                            } else "Unknown"
                        }
                        Text(
                            text = "Seed Hash: $seedHash",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            color = Color.DarkGray
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
                                        "RECOVERY MNEMONIC (24 WORDS):",
                                        fontFamily = RajdhaniFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = NeonCyan
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        profile?.seedPhrase ?: "Error loading mnemonic",
                                        fontFamily = RajdhaniFontFamily,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        CyberTextButton(
                                            onClick = {
                                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(profile?.seedPhrase ?: ""))
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
                                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = RajdhaniFontFamily),
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
                                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = RajdhaniFontFamily),
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
                                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = RajdhaniFontFamily),
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
                                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 16.sp),
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
                                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(seedText))
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
                                    textStyle = androidx.compose.ui.text.TextStyle(
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
                        // CLOUD ACTIVE VIEW
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
                                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 15.sp),
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
                                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 15.sp),
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
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 15.sp),
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
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color.DarkGray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = RajdhaniFontFamily),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (seedRequestPasswordInput.isNotEmpty()) {
                            showSeedRequestPassword = false
                            showSeedPhraseReveal = true
                            seedRequestPasswordInput = ""
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

// ======================== NET-OPS CONTROL CENTER SYSTEM ========================

@Composable
fun NetOpsScreen(viewModel: TarotViewModel) {
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    var activeSubTab by remember { mutableStateOf(0) } // 0: Lunar Calendar, 1: Comms...
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val lang = profile?.systemLanguage ?: "en"
    var showNetworkSettingsDialog by remember { mutableStateOf(false) }

    if (showNetworkSettingsDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showNetworkSettingsDialog = false }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.5.dp, NeonCyan, RoundedCornerShape(16.dp)),
                color = CyberObsidian
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Network Settings",
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == "ru") "СЕТЕВЫЕ НАСТРОЙКИ" else "NETWORK SETTINGS",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = RajdhaniFontFamily,
                                color = Color.White
                            )
                        }
                        IconButton(onClick = { showNetworkSettingsDialog = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.Gray
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Tunnel configuration
                    Box(modifier = Modifier.weight(1f)) {
                        BridgeTunnelPanel(viewModel)
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 54.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        // Botanical Top Section with Gear Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = LocalizationHelper.getString(lang, "tab_netops"),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = RajdhaniFontFamily,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (lang == "ru") "Системы оранжерей и лунные циклы" else if (lang == "es") "Sistemas de invernadero y ciclos lunares" else "Greenhouse Systems & Lunar Cycles",
                    fontSize = 17.sp,
                    fontFamily = RajdhaniFontFamily,
                    color = Color(0xFFB0B0C5)
                )
            }
            
            // Gear button for Network Settings
            IconButton(
                onClick = {
                    com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.CLICK)
                    showNetworkSettingsDialog = true
                },
                modifier = Modifier
                    .size(44.dp)
                    .background(NeonCyan.copy(alpha = 0.1f), CircleShape)
                    .border(1.dp, NeonCyan.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Network Settings Gear",
                    tint = NeonCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Monospace Tab Buttons
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .background(Color(0xFF0D0D15), RoundedCornerShape(4.dp))
                .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = if (lang == "ru") {
                listOf("ЛУНА 🌙", "СВЯЗЬ", "РЕЕСТР", "ДИСПЕНСЕР", "СЕМЕНА", "РЕКЛАМА", "ЗАНАЧКА")
            } else if (lang == "es") {
                listOf("LUNAR 🌙", "COMMS", "LEDGER", "DISPENSARIO", "SEMILLAS", "ANUNCIOS", "ALMACÉN")
            } else {
                listOf("LUNAR 🌙", "COMMS", "LEDGER", "DISPENSARY", "SEEDS", "ADS", "STASH")
            }
            items(tabs.size) { index ->
                val title = tabs[index]
                val isSelected = activeSubTab == index
                val tabColor = when (index) {
                    0 -> NeonGreen
                    3 -> NeonMagenta
                    4 -> NeonGreen
                    5 -> NeonGold
                    6 -> NeonCyan
                    else -> NeonCyan
                }
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) tabColor.copy(alpha = 0.15f) else Color.Transparent,
                            RoundedCornerShape(3.dp)
                        )
                        .clickable { activeSubTab = index }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) tabColor else Color(0xFFB0B0C5)
                    )
                }
            }
        }

        // Sub-panels
        Box(modifier = Modifier.weight(1f)) {
            when (activeSubTab) {
                0 -> GrowerLunarCalendarPanel(viewModel)
                1 -> SecureCommsPanel(viewModel)
                2 -> P2pLedgerPanel(viewModel)
                3 -> OnChainNftPanel(viewModel)
                4 -> WeedProtocolPanel(viewModel)
                5 -> NeuralAdsPanel(viewModel)
                6 -> SwarmStoragePanel(viewModel)
            }
        }
    }
}

@Composable
fun GrowerLunarCalendarPanel(viewModel: TarotViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val lang = profile?.systemLanguage ?: "en"
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    var lunarSubTab by remember { mutableIntStateOf(0) } // 0: Tarot Minting Oracle, 1: Botanical Greenhouse
    val calendarDays = remember(lang) { LunarCalendarService.getLunarCalendarForCurrentMonth(lang) }
    val currentDayIndex = remember {
        val today = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH)
        (today - 1).coerceIn(0, calendarDays.size - 1)
    }
    val todayInfo = calendarDays[currentDayIndex]

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("grower_lunar_calendar_panel"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Selector: Tarot Minting Oracle vs Botanical Greenhouse
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0C0E1A), RoundedCornerShape(10.dp))
                    .border(1.dp, currentSkin.primaryColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(4.dp)
            ) {
                // Tab 0: Tarot Minting Oracle
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (lunarSubTab == 0) currentSkin.primaryColor.copy(alpha = 0.25f) else Color.Transparent)
                        .clickable { lunarSubTab = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (lang == "ru") "🔮 ОРАКУЛ ЧЕКАНКИ" else if (lang == "es") "🔮 ACUÑACIÓN TAROT" else "🔮 MINTING ORACLE",
                        fontSize = 16.sp,
                        fontFamily = RajdhaniFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = if (lunarSubTab == 0) currentSkin.primaryColor else Color(0xFFB0B0C5)
                    )
                }

                // Tab 1: Botanical Greenhouse
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (lunarSubTab == 1) Color(0xFF00FF66).copy(alpha = 0.25f) else Color.Transparent)
                        .clickable { lunarSubTab = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (lang == "ru") "🌱 ОРАНЖЕРЕЯ" else if (lang == "es") "🌱 INVERNADERO" else "🌱 GREENHOUSE",
                        fontSize = 16.sp,
                        fontFamily = RajdhaniFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = if (lunarSubTab == 1) Color(0xFF00FF66) else Color(0xFFB0B0C5)
                    )
                }
            }
        }

        if (lunarSubTab == 0) {
            item {
                CyberLunarCycleWidget(
                    systemLanguage = lang,
                    currentSkin = currentSkin,
                    onMintClick = {
                        viewModel.resetDivination()
                    }
                )
            }
        } else {
            // Today's Moon Phase Hero Card
            item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0x66004D1A)),
                border = BorderStroke(1.5.dp, Color(0xFF00FF66).copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = LocalizationHelper.getString(lang, "daily_advice_header"),
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00FF66)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = todayInfo.phaseName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = todayInfo.phaseIcon,
                            fontSize = 44.sp,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = LocalizationHelper.getString(lang, "illumination_label"),
                                fontSize = 15.sp,
                                color = Color(0xFFB0B0C5),
                                fontFamily = RajdhaniFontFamily
                            )
                            Text(
                                text = "${todayInfo.illumination}%",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = LocalizationHelper.getString(lang, "moon_zodiac"),
                                fontSize = 15.sp,
                                color = Color(0xFFB0B0C5),
                                fontFamily = RajdhaniFontFamily
                            )
                            Text(
                                text = todayInfo.astrologicalSign,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Column(modifier = Modifier.weight(1.3f)) {
                            Text(
                                text = LocalizationHelper.getString(lang, "soil_fertility"),
                                fontSize = 15.sp,
                                color = Color(0xFFB0B0C5),
                                fontFamily = RajdhaniFontFamily
                            )
                            Text(
                                text = if (todayInfo.isFertile) LocalizationHelper.getString(lang, "high_fertile") else LocalizationHelper.getString(lang, "barren_dry"),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (todayInfo.isFertile) Color(0xFF00FF66) else Color(0xFFFF5500)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Recommended Action Badge
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🌿", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = todayInfo.recommendedAction.uppercase(),
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00FF66)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = todayInfo.detailInstructions,
                                fontSize = 16.sp,
                                color = Color.LightGray,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // Partner Links Section
        item {
            val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0x3300FFCC)),
                border = BorderStroke(1.5.dp, Color(0xFF00FFCC).copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (lang == "ru") "ПАРТНЕРСКИЕ РЕСУРСЫ" else "PARTNER RESOURCES",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00FFCC)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // SeedFinder
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                            border = BorderStroke(1.dp, Color(0xFF00FFCC).copy(alpha = 0.3f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    try {
                                        uriHandler.openUri("https://www.seedfinder.eu")
                                    } catch (e: Exception) {
                                        // Ignore
                                    }
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🌱", fontSize = 24.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "SeedFinder.eu",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (lang == "ru") "Генетика сортов" else "Strain Database",
                                    fontSize = 14.sp,
                                    color = Color(0xFFB0B0C5),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Rasta Prosto
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                            border = BorderStroke(1.dp, Color(0xFF00FFCC).copy(alpha = 0.3f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    try {
                                        uriHandler.openUri("https://rasta_prosto.r.me")
                                    } catch (e: Exception) {
                                        // Ignore
                                    }
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🛒", fontSize = 24.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Rasta Prosto",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (lang == "ru") "Магазин семян" else "Seed Shop",
                                    fontSize = 14.sp,
                                    color = Color(0xFFB0B0C5),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // Monthly Forecast Section Title
        item {
            Text(
                text = LocalizationHelper.getString(lang, "monthly_grower_calendar"),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = RajdhaniFontFamily,
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        // Daily table rows
        items(calendarDays) { day ->
            val isToday = day.dayOfMonth == (currentDayIndex + 1)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isToday) Color(0x3300FF66) else Color(0xFF0F0F16)
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isToday) Color(0xFF00FF66) else Color.DarkGray.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Text(
                            text = day.phaseIcon,
                            fontSize = 24.sp
                        )
                        Column {
                            Text(
                                text = day.dateString,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isToday) Color(0xFF00FF66) else Color.White
                            )
                            Text(
                                text = "${day.illumination}% lit | ${day.astrologicalSign.substringBefore(" (")}",
                                fontSize = 14.sp,
                                color = Color(0xFFB0B0C5),
                                fontFamily = RajdhaniFontFamily
                            )
                        }
                    }

                    // Task badge
                    Box(
                        modifier = Modifier
                            .background(
                                if (day.isFertile) Color(0x2200FF66) else Color(0x22FF5500),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .weight(0.8f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.recommendedAction,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (day.isFertile) Color(0xFF00FF66) else Color(0xFFFF9900),
                            textAlign = TextAlign.Center,
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

@Composable
fun BridgeTunnelPanel(viewModel: TarotViewModel) {
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val host by viewModel.bridgeHost.collectAsStateWithLifecycle()
    val port by viewModel.bridgePort.collectAsStateWithLifecycle()
    val type by viewModel.bridgeType.collectAsStateWithLifecycle()
    val status by viewModel.bridgeStatus.collectAsStateWithLifecycle()
    val logs by viewModel.bridgeLog.collectAsStateWithLifecycle()

    var editingHost by remember { mutableStateOf(host) }
    var editingPort by remember { mutableStateOf(port) }
    var editingType by remember { mutableStateOf(type) }

    val statusColor = when (status) {
        "ONLINE" -> NeonGreen
        "TESTING" -> NeonGold
        "OFFLINE" -> Color.Red
        else -> Color.Gray
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                border = BorderStroke(1.dp, Color.DarkGray),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TUNNEL CONFIGURATION",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Host Input
                    OutlinedTextField(
                        value = editingHost,
                        onValueChange = {
                            editingHost = it
                            viewModel.updateBridgeConfig(it, editingPort, editingType)
                        },
                        label = { Text("Proxy Host IP", fontFamily = RajdhaniFontFamily) },
                        singleLine = true,
                        textStyle = TextStyle(fontFamily = RajdhaniFontFamily, color = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Port Input
                        OutlinedTextField(
                            value = editingPort,
                            onValueChange = {
                                editingPort = it
                                viewModel.updateBridgeConfig(editingHost, it, editingType)
                            },
                            label = { Text("Port", fontFamily = RajdhaniFontFamily) },
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = RajdhaniFontFamily, color = Color.White),
                            modifier = Modifier.weight(1f)
                        )

                        // SOCKS / HTTP Toggle
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "PROXY TYPE",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 14.sp,
                                color = Color(0xFFB0B0C5),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .background(Color.Black, RoundedCornerShape(4.dp))
                                    .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("SOCKS", "HTTP").forEach { pType ->
                                    val isSel = editingType == pType
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .background(
                                                if (isSel) NeonCyan.copy(alpha = 0.2f) else Color.Transparent,
                                                RoundedCornerShape(3.dp)
                                            )
                                            .clickable {
                                                editingType = pType
                                                viewModel.updateBridgeConfig(editingHost, editingPort, pType)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = pType,
                                            fontFamily = RajdhaniFontFamily,
                                            fontSize = 16.sp,
                                            color = if (isSel) NeonCyan else Color(0xFFB0B0C5)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Status display
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black, RoundedCornerShape(4.dp))
                            .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(statusColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "GATEWAY STATUS: $status",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                            Text(
                                text = "OBFS4 Bridge connection & Tor circuitry active",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 17.sp,
                                color = Color(0xFFB0B0C5)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    CyberButton(
                        onClick = { viewModel.testBridge() },
                        skin = currentSkin,
                        enabled = status != "TESTING",
                        containerColor = NeonCyan,
                        contentColor = CyberObsidian,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AutoResizingText(
                            text = if (status == "TESTING") "CONNECTING CIRC... " else "ESTABLISH BRIDGE ROUTE",
                            targetTextSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = RajdhaniFontFamily,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                border = BorderStroke(1.dp, Color.DarkGray),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TERMINAL NET-TUNNEL LOGS",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .background(Color.Black, RoundedCornerShape(4.dp))
                            .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                            .padding(10.dp)
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(logs) { log ->
                                Text(
                                    text = log,
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 14.sp,
                                    color = if (log.contains("ERROR") || log.contains("failed") || log.contains("failed.")) Color.Red else if (log.contains("ONLINE") || log.contains("secured")) NeonGreen else Color.LightGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SecureCommsPanel(viewModel: TarotViewModel) {
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val contacts by viewModel.peerContacts.collectAsStateWithLifecycle()
    var selectedContact by remember { mutableStateOf<PeerContact?>(null) }
    var showAddContactDialog by remember { mutableStateOf(false) }

    if (selectedContact != null) {
        val messages by viewModel.getMessagesForContact(selectedContact!!.id).collectAsStateWithLifecycle(emptyList())
        var textInput by remember { mutableStateOf("") }

        Column(modifier = Modifier.fillMaxSize()) {
            // Chat Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberCharcoal, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .border(1.dp, Color.DarkGray, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(NeonMagenta.copy(alpha = 0.2f), CircleShape)
                            .border(1.dp, NeonMagenta, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = selectedContact!!.name.take(1).uppercase(),
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = NeonMagenta,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = selectedContact!!.name,
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Text(
                            text = selectedContact!!.onionAddress,
                            fontFamily = RajdhaniFontFamily,
                            color = Color(0xFFB0B0C5),
                            fontSize = 17.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.width(180.dp)
                        )
                    }
                }

                Text(
                    text = "CLOSE CHANNEL",
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 14.sp,
                    color = Color(0xFFB0B0C5),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { selectedContact = null }
                )
            }

            // Message List
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.Black)
                    .border(1.dp, Color.DarkGray)
                    .padding(8.dp)
            ) {
                if (messages.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "CHANNEL SECURED. AWAITING TELEMETRY HANDSHAKE...",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                    }
                } else {
                    val listState = rememberLazyListState()
                    LaunchedEffect(messages.size) {
                        if (messages.isNotEmpty()) {
                            listState.animateScrollToItem(messages.size - 1)
                        }
                    }
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(messages) { msg ->
                            val isMe = msg.senderName == "Me"
                            val bubbleColor = if (isMe) NeonCyan.copy(alpha = 0.08f) else NeonMagenta.copy(alpha = 0.08f)
                            val borderColor = if (isMe) NeonCyan.copy(alpha = 0.4f) else NeonMagenta.copy(alpha = 0.4f)
                            val align = if (isMe) Alignment.End else Alignment.Start

                            Column(modifier = Modifier.fillMaxWidth()) {
                                Box(
                                    modifier = Modifier
                                        .align(align)
                                        .widthIn(max = 260.dp)
                                        .background(bubbleColor, RoundedCornerShape(6.dp))
                                        .border(1.dp, borderColor, RoundedCornerShape(6.dp))
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = msg.senderName.uppercase(),
                                            fontFamily = RajdhaniFontFamily,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isMe) NeonCyan else NeonMagenta
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = msg.plaintext,
                                            fontFamily = RajdhaniFontFamily,
                                            fontSize = 16.sp,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f), thickness = 0.5.dp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "ENC_CIPHERTEXT: ${msg.ciphertext}",
                                            fontFamily = RajdhaniFontFamily,
                                            fontSize = 16.sp,
                                            color = Color(0xFFB0B0C5),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "ALGORITHM: AES-256-ECB / PKCS5",
                                            fontFamily = RajdhaniFontFamily,
                                            fontSize = 16.sp,
                                            color = Color(0xFFB0B0C5)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Message Input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberCharcoal, RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                    .border(1.dp, Color.DarkGray, RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Send encrypted message...", fontFamily = RajdhaniFontFamily, fontSize = 16.sp) },
                    singleLine = true,
                    textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 16.sp, color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color.DarkGray
                    ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            viewModel.sendEncryptedMessage(selectedContact!!.id, textInput)
                            textInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(NeonCyan, RoundedCornerShape(4.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = CyberObsidian
                    )
                }
            }
        }
    } else {
        // Address Book Contact list
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PEER CONTACTS",
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                CyberTextButton(
                    onClick = { showAddContactDialog = true },
                    text = "+ ADD NODE",
                    skin = currentSkin,
                    containerColor = NeonCyan,
                    contentColor = CyberObsidian,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (contacts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.Black, RoundedCornerShape(8.dp))
                        .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "TERMINAL ADDRESS BOOK EMPTY",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            color = Color(0xFFB0B0C5),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add peer nodes to initiate handshakes.",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(contacts) { contact ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                            border = BorderStroke(1.dp, Color.DarkGray),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedContact = contact }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(NeonCyan.copy(alpha = 0.1f), CircleShape)
                                            .border(1.dp, NeonCyan, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = contact.name.take(1).uppercase(),
                                            fontFamily = RajdhaniFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = NeonCyan
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = contact.name,
                                            fontFamily = RajdhaniFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            text = "ADDR: ${contact.onionAddress}",
                                            fontFamily = RajdhaniFontFamily,
                                            color = Color(0xFFB0B0C5),
                                            fontSize = 17.sp
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(
                                        onClick = { selectedContact = contact },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Chat,
                                            contentDescription = "Message",
                                            tint = NeonCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteContact(contact.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color.Red,
                                            modifier = Modifier.size(16.dp)
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

    // Add contact dialog
    if (showAddContactDialog) {
        var nameInput by remember { mutableStateOf("") }
        var onionInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddContactDialog = false },
            title = {
                Text(
                    text = "REGISTER NEW PEER NODE",
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Code Name", fontFamily = RajdhaniFontFamily) },
                        singleLine = true,
                        textStyle = TextStyle(fontFamily = RajdhaniFontFamily),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = onionInput,
                        onValueChange = { onionInput = it },
                        placeholder = { Text("e.g. jx7v9pxmno...onion", fontFamily = RajdhaniFontFamily, fontSize = 16.sp) },
                        label = { Text("Onion / Cyber Address", fontFamily = RajdhaniFontFamily) },
                        singleLine = true,
                        textStyle = TextStyle(fontFamily = RajdhaniFontFamily),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (nameInput.isNotBlank() && onionInput.isNotBlank()) {
                            viewModel.addContact(nameInput, onionInput)
                            showAddContactDialog = false
                        }
                    }
                ) {
                    Text("REGISTER", fontFamily = RajdhaniFontFamily, color = NeonCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddContactDialog = false }) {
                    Text("CANCEL", fontFamily = RajdhaniFontFamily, color = Color(0xFFB0B0C5))
                }
            },
            containerColor = CyberCharcoal,
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }
}

data class TokenAsset(
    val symbol: String,
    val name: String,
    val balance: Double,
    val depositAddress: String,
    val priceUsd: Double,
    val color: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun P2pLedgerPanel(viewModel: TarotViewModel) {
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val contacts by viewModel.peerContacts.collectAsStateWithLifecycle()
    val transactions by viewModel.p2pTransactions.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val mintedCards by viewModel.mintedCards.collectAsStateWithLifecycle()

    val p2pStatus by viewModel.p2pStatus.collectAsStateWithLifecycle()
    val miningProgress by viewModel.miningProgress.collectAsStateWithLifecycle()

    var showTransferDialog by remember { mutableStateOf(false) }
    var showDepositDialog by remember { mutableStateOf(false) }
    var showSendDialog by remember { mutableStateOf(false) }
    var showSwapDialog by remember { mutableStateOf(false) }

    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Wallet Balances
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                border = BorderStroke(1.dp, NeonCyan),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "P2P HARDWARE WALLET",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 15.sp,
                                color = Color(0xFFB0B0C5)
                            )
                            Text(
                                text = "${profile?.username ?: "Netrunner"}_SECURE_CHIP",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        CyberTextButton(
                            onClick = { showTransferDialog = true },
                            text = "BROADCAST TX",
                            skin = currentSkin,
                            containerColor = NeonCyan,
                            contentColor = CyberObsidian,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color.DarkGray, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "WEED BALANCE",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 14.sp,
                                color = Color(0xFFB0B0C5)
                            )
                            Text(
                                text = "${profile?.weed ?: 0} WEED",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "TAROT MODULES OWNED",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 14.sp,
                                color = Color(0xFFB0B0C5)
                            )
                            Text(
                                text = "${mintedCards.size} NODES",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonMagenta
                            )
                        }
                    }
                }
            }
        }

        // Tokenized Actives Section (ETH, TON, SOL, TRX, USDC)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                border = BorderStroke(1.dp, NeonMagenta.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DECENTRALIZED TOKENIZED ACTIVES",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonMagenta
                    )
                    Text(
                        text = "Multi-chain asset balances integrated via secure bridge protocol",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 14.sp,
                        color = Color(0xFFB0B0C5),
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    // Draw each of the 5 assets
                    val activeTokens = listOf(
                        TokenAsset("ETH", "Ethereum Core", profile?.balanceEth ?: 0.5, "0x9E88d6C7BaF8F4f3183f233f6329686259e7ab9C", 3250.0, NeonCyan, Icons.Default.CurrencyExchange),
                        TokenAsset("TON", "The Open Network (Gram)", profile?.balanceTon ?: 50.0, "UQBgN6V569MhR18ePoPnXJpL_7XbA1f_4b9C9e7aB_90C7", 7.20, NeonCyan, Icons.Default.AirlineStops),
                        TokenAsset("SOL", "Solana Network", profile?.balanceSol ?: 2.0, "8Fx3C4WqpQePy79CbC9e7aB90C733f6329686259e7a", 142.50, NeonMagenta, Icons.Default.Bolt),
                        TokenAsset("TRX", "TRON Protocol", profile?.balanceTrx ?: 250.0, "TY48Xm9qYePy79CbC9e7aB90C733f6329686259e7ab9C", 0.12, NeonRed, Icons.Default.Timeline),
                        TokenAsset("USDC", "Circle USD Coin", profile?.balanceUsdc ?: 100.0, "0x9E88d6C7BaF8F4f3183f233f6329686259e7ab9C", 1.00, Color.White, Icons.Default.RadioButtonChecked)
                    )

                    activeTokens.forEach { token ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(Color(0xFF0D0D15), RoundedCornerShape(6.dp))
                                .border(1.dp, Color.DarkGray, RoundedCornerShape(6.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(token.color.copy(alpha = 0.1f), CircleShape)
                                        .border(1.dp, token.color.copy(alpha = 0.3f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = token.icon,
                                        contentDescription = token.symbol,
                                        tint = token.color,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = token.symbol,
                                            fontFamily = RajdhaniFontFamily,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        if (token.symbol == "TON") {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "GRAM",
                                                fontFamily = RajdhaniFontFamily,
                                                fontSize = 16.sp,
                                                color = Color.Yellow,
                                                modifier = Modifier
                                                    .background(Color.Yellow.copy(alpha = 0.15f), RoundedCornerShape(2.dp))
                                                    .padding(horizontal = 3.dp, vertical = 0.5.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = token.name,
                                        fontFamily = RajdhaniFontFamily,
                                        fontSize = 14.sp,
                                        color = Color(0xFFB0B0C5)
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${String.format("%.4f", token.balance)} ${token.symbol}",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = token.color
                                )
                                Text(
                                    text = "$${String.format("%.2f", token.balance * token.priceUsd)} USD",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 14.sp,
                                    color = Color(0xFFB0B0C5)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Buttons for actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CyberTextButton(
                            onClick = { showDepositDialog = true },
                            text = "DEPOSIT",
                            skin = currentSkin,
                            containerColor = NeonGreen.copy(alpha = 0.1f),
                            contentColor = NeonGreen,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )

                        CyberTextButton(
                            onClick = { showSendDialog = true },
                            text = "SEND",
                            skin = currentSkin,
                            containerColor = NeonRed.copy(alpha = 0.1f),
                            contentColor = NeonRed,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )

                        CyberTextButton(
                            onClick = { showSwapDialog = true },
                            text = "SWAP / DEX",
                            skin = currentSkin,
                            containerColor = NeonCyan.copy(alpha = 0.1f),
                            contentColor = NeonCyan,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.2f)
                        )
                    }
                }
            }
        }

        // Ledger Block Explorer title
        item {
            Text(
                text = "BLOCKCHAIN LEDGER EXPLORER",
                fontFamily = RajdhaniFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 1.sp
            )
        }

        if (transactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color.Black, RoundedCornerShape(8.dp))
                        .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AWAITING GENESIS BLOCK PROTOCOLS...",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 15.sp,
                        color = Color(0xFFB0B0C5)
                    )
                }
            }
        } else {
            items(transactions) { tx ->
                val payloadText = if (tx.cardIndex != -1) {
                    val card = CyberTarotDeck.getCard(tx.cardIndex)
                    "Transferred Card MODULE #${String.format("%02d", tx.cardIndex)} (${card.name})"
                } else {
                    "Sent Weed allocation: ${tx.amountWeed} WEED"
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF07070F)),
                    border = BorderStroke(1.dp, Color.DarkGray),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "BLOCK #${String.format("%04d", tx.blockNumber)}",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                            Text(
                                text = "NONCE: ${tx.nonce}",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 17.sp,
                                color = Color(0xFFB0B0C5)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "HASH: ${tx.blockHash}",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 17.sp,
                            color = NeonMagenta
                        )
                        Text(
                            text = "PREV: ${tx.previousHash}",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 17.sp,
                            color = Color(0xFFB0B0C5)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "FROM: ${tx.senderAddress}",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "TO: ${tx.recipientAddress}",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "DATA: $payloadText",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 15.sp,
                            color = NeonGold,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "SIGNATURE: ${tx.signature}",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 17.sp,
                            color = Color(0xFFB0B0C5)
                        )
                    }
                }
            }
        }
    }

    // Interactive mining overlay
    if (p2pStatus == "MINING" && miningProgress != null) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(
                    text = "MINING TRANSACTIONS BLOCK...",
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Executing cryptographical Proof-of-Work hashes sequentially. Finding SHA-256 block hash prefix matching difficulty targets...",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 14.sp,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { miningProgress!! },
                        color = NeonCyan,
                        trackColor = Color.DarkGray,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "NONCE SCANS: ${(miningProgress!! * 100000).toInt()}/100000",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 17.sp,
                        color = Color(0xFFB0B0C5)
                    )
                }
            },
            confirmButton = {},
            dismissButton = {},
            containerColor = CyberObsidian,
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }

    // Success dialog
    if (p2pStatus == "SUCCESS") {
        AlertDialog(
            onDismissRequest = { viewModel.resetP2pStatus() },
            title = {
                Text(
                    text = "BLOCK MINED & LINKED",
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonGreen
                )
            },
            text = {
                Text(
                     text = "Proof of Work verification succeeded! The P2P ledger was successfully updated, signatures matched, and the card/Weed have been transferred on-chain.",
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 15.sp,
                    color = Color.LightGray
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.resetP2pStatus() }) {
                    Text("OK", fontFamily = RajdhaniFontFamily, color = NeonGreen)
                }
            },
            containerColor = CyberCharcoal
        )
    }

    // Broadcast Dialog
    if (showTransferDialog) {
        var selectedRecipient by remember { mutableStateOf<PeerContact?>(null) }
        var isCardTransfer by remember { mutableStateOf(false) }
        var creditsAmount by remember { mutableStateOf("100") }
        var selectedCardToTransfer by remember { mutableStateOf<MintedCard?>(null) }
        var passwordInput by remember { mutableStateOf("") }
        var dropdownExpanded by remember { mutableStateOf(false) }
        var cardDropdownExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showTransferDialog = false },
            title = {
                Text(
                    text = "CREATE P2P TRANSFER BLOCK",
                    fontFamily = RajdhaniFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Recipient Selector
                    Column {
                        Text(
                            text = "RECIPIENT PEER NODE",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 17.sp,
                            color = Color(0xFFB0B0C5)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black, RoundedCornerShape(4.dp))
                                .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                                .clickable { dropdownExpanded = true }
                                .padding(12.dp)
                        ) {
                            Text(
                                text = selectedRecipient?.name ?: "SELECT PEER NODE...",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 16.sp,
                                color = if (selectedRecipient != null) Color.White else Color(0xFFB0B0C5)
                            )
                        }

                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier.background(CyberCharcoal)
                        ) {
                            contacts.forEach { contact ->
                                DropdownMenuItem(
                                    text = { Text(contact.name, fontFamily = RajdhaniFontFamily, color = Color.White) },
                                    onClick = {
                                        selectedRecipient = contact
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Asset type selector (Credits or Card NFT)
                    Column {
                        Text(
                            text = "ASSET CLASS TO BROADCAST",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 17.sp,
                            color = Color(0xFFB0B0C5)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black, RoundedCornerShape(4.dp))
                                .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                                .padding(4.dp)
                        ) {
                            listOf(false to "WEED", true to "TAROT NFT MODULE").forEach { (isCard, label) ->
                                val isSel = isCardTransfer == isCard
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (isSel) NeonCyan.copy(alpha = 0.2f) else Color.Transparent,
                                            RoundedCornerShape(3.dp)
                                        )
                                        .clickable { isCardTransfer = isCard }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontFamily = RajdhaniFontFamily,
                                        fontSize = 15.sp,
                                        color = if (isSel) NeonCyan else Color(0xFFB0B0C5)
                                    )
                                }
                            }
                        }
                    }

                    if (isCardTransfer) {
                        // Card selection
                        Column {
                            Text(
                                text = "SELECT TAROT MODULE",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 17.sp,
                                color = Color(0xFFB0B0C5)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black, RoundedCornerShape(4.dp))
                                    .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                                    .clickable { cardDropdownExpanded = true }
                                    .padding(12.dp)
                            ) {
                                val cardName = selectedCardToTransfer?.let {
                                    val card = CyberTarotDeck.getCard(it.cardIndex)
                                    "NODE #${String.format("%02d", it.cardIndex)}: ${card.name} (#${String.format("%04d", it.mintNumber)})"
                                } ?: "SELECT OWNED NFT..."
                                Text(
                                    text = cardName,
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 16.sp,
                                    color = if (selectedCardToTransfer != null) Color.White else Color(0xFFB0B0C5)
                                )
                            }

                            DropdownMenu(
                                expanded = cardDropdownExpanded,
                                onDismissRequest = { cardDropdownExpanded = false },
                                modifier = Modifier.background(CyberCharcoal)
                            ) {
                                mintedCards.forEach { card ->
                                    val info = CyberTarotDeck.getCard(card.cardIndex)
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "NODE #${String.format("%02d", card.cardIndex)}: ${info.name} (#${String.format("%04d", card.mintNumber)})",
                                                fontFamily = RajdhaniFontFamily,
                                                color = Color.White
                                            )
                                        },
                                        onClick = {
                                            selectedCardToTransfer = card
                                            cardDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        // Credits inputs
                        OutlinedTextField(
                            value = creditsAmount,
                            onValueChange = { creditsAmount = it },
                            label = { Text("Amount Weed", fontFamily = RajdhaniFontFamily) },
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = RajdhaniFontFamily),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Private BIP39 Signature field
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("BIP39 Seed Phrase (Signature)", fontFamily = RajdhaniFontFamily) },
                        placeholder = { Text("Enter private keys/seed to authorize", fontFamily = RajdhaniFontFamily, fontSize = 14.sp) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        textStyle = TextStyle(fontFamily = RajdhaniFontFamily),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val recipient = selectedRecipient?.name
                        if (recipient != null && passwordInput.isNotBlank()) {
                            val weedVal = if (isCardTransfer) 0 else (creditsAmount.toIntOrNull() ?: 100)
                            val cardIdx = selectedCardToTransfer?.cardIndex ?: -1
                            val mintNum = selectedCardToTransfer?.mintNumber ?: -1
                            viewModel.sendP2pTransaction(
                                recipient,
                                weedVal,
                                cardIdx,
                                mintNum,
                                passwordInput
                            )
                            showTransferDialog = false
                        }
                    }
                ) {
                    Text("SIGN & BROADCAST", fontFamily = RajdhaniFontFamily, color = NeonCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTransferDialog = false }) {
                    Text("CANCEL", fontFamily = RajdhaniFontFamily, color = Color(0xFFB0B0C5))
                }
            },
            containerColor = CyberCharcoal,
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }

    if (showDepositDialog) {
        var selectedAsset by remember { mutableStateOf("ETH") }
        var depositAmount by remember { mutableStateOf("0.5") }
        
        val addresses = mapOf(
            "ETH" to "0x9E88d6C7BaF8F4f3183f233f6329686259e7ab9C",
            "TON" to "UQBgN6V569MhR18ePoPnXJpL_7XbA1f_4b9C9e7aB_90C7",
            "SOL" to "8Fx3C4WqpQePy79CbC9e7aB90C733f6329686259e7a",
            "TRX" to "TY48Xm9qYePy79CbC9e7aB90C733f6329686259e7ab9C",
            "USDC" to "0x9E88d6C7BaF8F4f3183f233f6329686259e7ab9C"
        )
        
        val currentAddress = addresses[selectedAsset] ?: ""

        AlertDialog(
            onDismissRequest = { showDepositDialog = false },
            title = {
                Text("BRIDGE DEPOSIT PROTOCOL", fontFamily = RajdhaniFontFamily, fontWeight = FontWeight.Bold, color = NeonCyan, fontSize = 16.sp)
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Deposit external tokenized assets via decentralized smart-contract gateways.",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 15.sp,
                        color = Color(0xFFB0B0C5)
                    )

                    // Asset Selector
                    Text("SELECT TARGET ASSET:", fontFamily = RajdhaniFontFamily, fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("ETH", "TON", "SOL", "TRX", "USDC").forEach { symbol ->
                            val isSel = selectedAsset == symbol
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSel) NeonCyan.copy(alpha = 0.2f) else Color(0xFF0D0D15), RoundedCornerShape(4.dp))
                                    .border(1.dp, if (isSel) NeonCyan else Color.DarkGray, RoundedCornerShape(4.dp))
                                    .clickable { selectedAsset = symbol }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(symbol, fontFamily = RajdhaniFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isSel) NeonCyan else Color(0xFFB0B0C5))
                            }
                        }
                    }

                    // Display deposit address with Copy button
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF09090E), RoundedCornerShape(6.dp))
                            .border(1.dp, Color.DarkGray, RoundedCornerShape(6.dp))
                            .padding(10.dp)
                    ) {
                        Text("DEPOSIT ADDRESS:", fontFamily = RajdhaniFontFamily, fontSize = 17.sp, color = Color(0xFFB0B0C5))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentAddress,
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 14.sp,
                            color = Color.LightGray,
                            modifier = Modifier.clickable {
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(currentAddress))
                                viewModel.postNotification("📋 Address copied to clipboard!")
                            }
                        )
                    }

                    // Simulated QR Code Graphic (Cyberpunk style terminal-drawn pattern!)
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(100.dp)
                            .background(Color.White, RoundedCornerShape(4.dp))
                            .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Drawing a fake techie cyber QR grid
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cells = 10
                            val cellSize = size.width / cells
                            for (r in 0 until cells) {
                                for (c in 0 until cells) {
                                    val isFilled = (r == 0 && c < 3) || (r < 3 && c == 0) || 
                                                    (r == cells - 1 && c < 3) || (r > cells - 4 && c == 0) ||
                                                    (r == 0 && c > cells - 4) || (r < 3 && c == cells - 1) ||
                                                    ((r + c) % 3 == 0) || (r > 3 && r < 7 && c > 3 && c < 7)
                                    if (isFilled) {
                                        drawRect(
                                            color = Color.Black,
                                            topLeft = androidx.compose.ui.geometry.Offset(c * cellSize, r * cellSize),
                                            size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Faucet Deposit Input
                    OutlinedTextField(
                        value = depositAmount,
                        onValueChange = { depositAmount = it },
                        label = { Text("Simulate Faucet Amount", fontFamily = RajdhaniFontFamily, fontSize = 15.sp) },
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
                }
            },
            confirmButton = {
                CyberTextButton(
                    onClick = {
                        val amt = depositAmount.toDoubleOrNull()
                        if (amt != null && amt > 0) {
                            viewModel.performAssetTransaction(selectedAsset, amt, "DEPOSIT")
                            showDepositDialog = false
                        } else {
                            viewModel.postNotification("❌ Please enter a valid amount!")
                        }
                    },
                    text = "MOCK NETWORK DEPOSIT",
                    skin = currentSkin,
                    containerColor = NeonGreen,
                    contentColor = CyberObsidian,
                    shape = RoundedCornerShape(12.dp)
                )
            },
            dismissButton = {
                TextButton(onClick = { showDepositDialog = false }) {
                    Text("CLOSE", fontFamily = RajdhaniFontFamily, color = Color(0xFFB0B0C5), fontSize = 15.sp)
                }
            },
            containerColor = CyberCharcoal,
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }

    if (showSendDialog) {
        var selectedAsset by remember { mutableStateOf("ETH") }
        var sendAmount by remember { mutableStateOf("0.1") }
        var recipientAddr by remember { mutableStateOf("") }
        var pinVal by remember { mutableStateOf("") }
        
        val maxBalance = when (selectedAsset) {
            "ETH" -> profile?.balanceEth ?: 0.5
            "TON" -> profile?.balanceTon ?: 50.0
            "SOL" -> profile?.balanceSol ?: 2.0
            "TRX" -> profile?.balanceTrx ?: 250.0
            "USDC" -> profile?.balanceUsdc ?: 100.0
            else -> 0.0
        }

        AlertDialog(
            onDismissRequest = { showSendDialog = false },
            title = {
                Text("SEND TOKENIZED ASSETS", fontFamily = RajdhaniFontFamily, fontWeight = FontWeight.Bold, color = NeonRed, fontSize = 16.sp)
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Broadcast withdrawal/transfer requests to decentralized mainnets.",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 15.sp,
                        color = Color(0xFFB0B0C5)
                    )

                    // Asset Selector
                    Text("SELECT ASSET TO SEND:", fontFamily = RajdhaniFontFamily, fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("ETH", "TON", "SOL", "TRX", "USDC").forEach { symbol ->
                            val isSel = selectedAsset == symbol
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSel) NeonRed.copy(alpha = 0.2f) else Color(0xFF0D0D15), RoundedCornerShape(4.dp))
                                    .border(1.dp, if (isSel) NeonRed else Color.DarkGray, RoundedCornerShape(4.dp))
                                    .clickable { selectedAsset = symbol }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(symbol, fontFamily = RajdhaniFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isSel) NeonRed else Color(0xFFB0B0C5))
                            }
                        }
                    }

                    Text("AVAILABLE BALANCE: $maxBalance $selectedAsset", fontFamily = RajdhaniFontFamily, fontSize = 14.sp, color = Color(0xFFB0B0C5))

                    // Recipient Address
                    OutlinedTextField(
                        value = recipientAddr,
                        onValueChange = { recipientAddr = it },
                        label = { Text("Recipient Address", fontFamily = RajdhaniFontFamily, fontSize = 15.sp) },
                        placeholder = { Text("e.g. 0x... or UQ...", fontSize = 14.sp, color = Color.DarkGray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonRed,
                            unfocusedBorderColor = Color.DarkGray
                        ),
                        textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 16.sp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Amount
                    OutlinedTextField(
                        value = sendAmount,
                        onValueChange = { sendAmount = it },
                        label = { Text("Amount to Transfer", fontFamily = RajdhaniFontFamily, fontSize = 15.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonRed,
                            unfocusedBorderColor = Color.DarkGray
                        ),
                        textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 16.sp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // PIN
                    val hasPin = profile?.pinCode?.isNotBlank() == true
                    OutlinedTextField(
                        value = pinVal,
                        onValueChange = { pinVal = it },
                        label = { Text(if (hasPin) "Verify 8-Digit Security PIN" else "PIN Verification (Bypassable - PIN not set)", fontFamily = RajdhaniFontFamily, fontSize = 15.sp) },
                        singleLine = true,
                        placeholder = { Text("Enter security PIN to sign tx", fontSize = 14.sp, color = Color.DarkGray) },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonRed,
                            unfocusedBorderColor = Color.DarkGray
                        ),
                        textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 16.sp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                CyberTextButton(
                    onClick = {
                        val amt = sendAmount.toDoubleOrNull()
                        val hasPin = profile?.pinCode?.isNotBlank() == true
                        val pinCorrect = !hasPin || pinVal == profile?.pinCode

                        if (amt == null || amt <= 0) {
                            viewModel.postNotification("❌ Invalid transaction amount!")
                        } else if (amt > maxBalance) {
                            viewModel.postNotification("❌ Insufficient balance!")
                        } else if (recipientAddr.isBlank()) {
                            viewModel.postNotification("❌ Recipient address cannot be empty!")
                        } else if (!pinCorrect) {
                            viewModel.postNotification("❌ Incorrect Security PIN signature!")
                        } else {
                            viewModel.performAssetTransaction(selectedAsset, -amt, "SEND", recipientAddr)
                            showSendDialog = false
                        }
                    },
                    text = "SIGN & WITHDRAW",
                    skin = currentSkin,
                    containerColor = NeonRed,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp)
                )
            },
            dismissButton = {
                TextButton(onClick = { showSendDialog = false }) {
                    Text("CANCEL", fontFamily = RajdhaniFontFamily, color = Color(0xFFB0B0C5), fontSize = 15.sp)
                }
            },
            containerColor = CyberCharcoal,
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }

    if (showSwapDialog) {
        var fromAsset by remember { mutableStateOf("USDC") }
        var toAsset by remember { mutableStateOf("ETH") }
        var fromAmountInput by remember { mutableStateOf("10.0") }

        val rateMap = mapOf(
            "WEED" to 1.0,
            "USDC" to 1.0,
            "ETH" to 3250.0,
            "TON" to 7.2,
            "SOL" to 142.5,
            "TRX" to 0.12
        )

        val balanceMap = mapOf(
            "WEED" to (profile?.weed?.toDouble() ?: 1000.0),
            "ETH" to (profile?.balanceEth ?: 0.5),
            "TON" to (profile?.balanceTon ?: 50.0),
            "SOL" to (profile?.balanceSol ?: 2.0),
            "TRX" to (profile?.balanceTrx ?: 250.0),
            "USDC" to (profile?.balanceUsdc ?: 100.0)
        )

        val fromRate = rateMap[fromAsset] ?: 1.0
        val toRate = rateMap[toAsset] ?: 1.0
        val fromBalance = balanceMap[fromAsset] ?: 0.0

        val fromAmountDouble = fromAmountInput.toDoubleOrNull() ?: 0.0
        val toAmountCalculated = if (fromRate > 0 && toRate > 0) {
            (fromAmountDouble * fromRate) / toRate
        } else {
            0.0
        }

        AlertDialog(
            onDismissRequest = { showSwapDialog = false },
            title = {
                Text("DEX BRIDGE SWAP PROTOCOL", fontFamily = RajdhaniFontFamily, fontWeight = FontWeight.Bold, color = NeonCyan, fontSize = 16.sp)
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Swap assets in peer-to-peer liquidity pools instantly. Automated market maker quotes apply.",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 15.sp,
                        color = Color(0xFFB0B0C5)
                    )

                    // FROM ASSET SELECTION
                    Text("FROM ASSET:", fontFamily = RajdhaniFontFamily, fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("WEED", "USDC", "ETH", "TON", "SOL", "TRX").forEach { symbol ->
                            val isSel = fromAsset == symbol
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSel) NeonCyan.copy(alpha = 0.2f) else Color(0xFF0D0D15), RoundedCornerShape(4.dp))
                                    .border(1.dp, if (isSel) NeonCyan else Color.DarkGray, RoundedCornerShape(4.dp))
                                    .clickable {
                                        fromAsset = symbol
                                        if (symbol != "WEED") {
                                            toAsset = "WEED"
                                        } else if (toAsset == "WEED") {
                                            toAsset = "USDC"
                                        }
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (symbol == "WEED") "WED" else symbol,
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) NeonCyan else Color(0xFFB0B0C5)
                                )
                            }
                        }
                    }

                    Text("AVAILABLE BALANCE: ${String.format("%.4f", fromBalance)} ${if (fromAsset == "WEED") "WED" else fromAsset}", fontFamily = RajdhaniFontFamily, fontSize = 14.sp, color = Color(0xFFB0B0C5))

                    // FROM AMOUNT INPUT
                    OutlinedTextField(
                        value = fromAmountInput,
                        onValueChange = { fromAmountInput = it },
                        label = { Text("Amount to Swap", fontFamily = RajdhaniFontFamily, fontSize = 15.sp) },
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

                    Spacer(modifier = Modifier.height(2.dp))

                    // TO ASSET SELECTION
                    Text("TO ASSET:", fontFamily = RajdhaniFontFamily, fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("WEED", "USDC", "ETH", "TON", "SOL", "TRX").forEach { symbol ->
                            val isSel = toAsset == symbol
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSel) NeonMagenta.copy(alpha = 0.2f) else Color(0xFF0D0D15), RoundedCornerShape(4.dp))
                                    .border(1.dp, if (isSel) NeonMagenta else Color.DarkGray, RoundedCornerShape(4.dp))
                                    .clickable {
                                        toAsset = symbol
                                        if (symbol != "WEED") {
                                            fromAsset = "WEED"
                                        } else if (fromAsset == "WEED") {
                                            fromAsset = "USDC"
                                        }
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (symbol == "WEED") "WED" else symbol,
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) NeonMagenta else Color(0xFFB0B0C5)
                                )
                            }
                        }
                    }

                    // CALCULATED ESTIMATE DISPLAY
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF09090E), RoundedCornerShape(6.dp))
                            .border(1.dp, NeonMagenta.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(10.dp)
                    ) {
                        Text("ESTIMATED CONVERSION:", fontFamily = RajdhaniFontFamily, fontSize = 17.sp, color = Color(0xFFB0B0C5))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${String.format("%.6f", toAmountCalculated)} ${if (toAsset == "WEED") "WED" else toAsset}",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonMagenta
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val directRate = fromRate / toRate
                        Text(
                            text = "Rate: 1 ${if (fromAsset == "WEED") "WED" else fromAsset} = ${String.format("%.6f", directRate)} ${if (toAsset == "WEED") "WED" else toAsset}",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 17.sp,
                            color = Color.LightGray
                        )
                    }
                }
            },
            confirmButton = {
                CyberTextButton(
                    onClick = {
                        val amt = fromAmountInput.toDoubleOrNull()
                        if (amt == null || amt <= 0) {
                            viewModel.postNotification("❌ Invalid swap amount!")
                        } else if (amt > fromBalance) {
                            viewModel.postNotification("❌ Insufficient balance to swap!")
                        } else {
                            viewModel.swapAssets(fromAsset, toAsset, amt, toAmountCalculated)
                            showSwapDialog = false
                        }
                    },
                    text = "EXECUTE SWAP",
                    skin = currentSkin,
                    containerColor = NeonCyan,
                    contentColor = CyberObsidian,
                    shape = RoundedCornerShape(12.dp)
                )
            },
            dismissButton = {
                TextButton(onClick = { showSwapDialog = false }) {
                    Text("CANCEL", fontFamily = RajdhaniFontFamily, color = Color(0xFFB0B0C5), fontSize = 15.sp)
                }
            },
            containerColor = CyberCharcoal,
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }
}

@Composable
fun OnChainNftPanel(viewModel: TarotViewModel) {
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val rpcUrl by viewModel.smartContractRpcUrl.collectAsStateWithLifecycle()
    val contractAddress by viewModel.smartContractAddress.collectAsStateWithLifecycle()
    val onChainMints by viewModel.onChainMints.collectAsStateWithLifecycle()
    val gasPrice by viewModel.gasPriceGwei.collectAsStateWithLifecycle()
    val contractDetails by viewModel.contractDetails.collectAsStateWithLifecycle()
    val status by viewModel.smartContractStatus.collectAsStateWithLifecycle()
    val logs by viewModel.smartContractLog.collectAsStateWithLifecycle()

    var editingRpc by remember { mutableStateOf(rpcUrl) }
    var editingAddress by remember { mutableStateOf(contractAddress) }

    val statusColor = when (status) {
        "SUCCESS" -> NeonGreen
        "MINTING" -> NeonGold
        "FAILED" -> Color.Red
        else -> Color.Gray
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Contract configuration & connection stats
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                border = BorderStroke(1.dp, NeonMagenta),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TESTNET SMART CONTRACT INTERFACE",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonMagenta
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editingRpc,
                        onValueChange = { editingRpc = it },
                        label = { Text("RPC Endpoint URL", fontSize = 15.sp, fontFamily = RajdhaniFontFamily) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonMagenta,
                            unfocusedBorderColor = Color.DarkGray
                        ),
                        textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 16.sp),
                        modifier = Modifier.fillMaxWidth().testTag("contract_rpc_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editingAddress,
                        onValueChange = { editingAddress = it },
                        label = { Text("Smart Contract Address", fontSize = 15.sp, fontFamily = RajdhaniFontFamily) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonMagenta,
                            unfocusedBorderColor = Color.DarkGray
                        ),
                        textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 16.sp),
                        modifier = Modifier.fillMaxWidth().testTag("contract_address_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "GAS: ${String.format("%.2f", gasPrice)} Gwei",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 15.sp,
                                color = NeonCyan
                            )
                            contractDetails?.let {
                                Text(
                                    text = "NET: ${it.networkName}",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 15.sp,
                                    color = Color(0xFFB0B0C5)
                                )
                            }
                        }

                        CyberTextButton(
                            onClick = { viewModel.updateRpcAndContractAddress(editingRpc, editingAddress) },
                            text = "CONNECT / UPDATE",
                            skin = currentSkin,
                            containerColor = NeonMagenta,
                            contentColor = Color.White,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("save_contract_config_btn")
                        )
                    }
                }
            }
        }

        // Live Logs Console
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberObsidian),
                border = BorderStroke(1.dp, Color.DarkGray),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ON-CHAIN KERNEL LOGS",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            color = Color.LightGray
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = status,
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 15.sp,
                                color = statusColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Log output list
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(Color.Black, RoundedCornerShape(4.dp))
                            .border(1.dp, Color(0xFF1F1F2E), RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    ) {
                        LazyColumn(
                            reverseLayout = true,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(logs.reversed()) { log ->
                                Text(
                                    text = "> $log",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 14.sp,
                                    color = if (log.contains("ERROR")) Color.Red else if (log.contains("CONFIRMED")) NeonGreen else Color.LightGray,
                                    modifier = Modifier.padding(vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // List of on-chain minted NFTs
        item {
            Text(
                text = "MINTER DEPLOYMENTS (${onChainMints.size} CONTRACT TOKENS)",
                fontFamily = RajdhaniFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        if (onChainMints.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NO DEPLOYED NFTs YET.\nGo to SECURE DECK VAULT, select a card, and mint to chain.",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 15.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(onChainMints) { nft ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                    border = BorderStroke(1.dp, Color(0xFF1F1F2E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "TOKEN ID: #${nft.tokenId}",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonMagenta
                                )
                                Text(
                                    text = "TX HASH: ${nft.txHash.take(16)}...",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 14.sp,
                                    color = Color(0xFFB0B0C5)
                                )
                            }
                            
                            val localUri = android.net.Uri.parse(nft.explorerUrl)
                            val context = androidx.compose.ui.platform.LocalContext.current
                            CyberTextButton(
                                onClick = {
                                    try {
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, localUri)
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // Silent catch if no browser installed
                                    }
                                },
                                text = "EXPLORER",
                                skin = currentSkin,
                                containerColor = Color.Transparent,
                                contentColor = NeonCyan,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ======================== WEED PROTOCOL PANEL ========================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeedProtocolPanel(viewModel: TarotViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val averageRates by viewModel.compiledAverageRates.collectAsStateWithLifecycle()
    val exchangeTickers by viewModel.topExchangesTickers.collectAsStateWithLifecycle()
    val treasurerLogs by viewModel.treasurerLogs.collectAsStateWithLifecycle()
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()

    var showTickersDetail by remember { mutableStateOf(false) }
    var selectedTickerCoin by remember { mutableStateOf("BTC") }

    // Reactive Countdown timer
    var currentTimeMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTimeMillis = System.currentTimeMillis()
        }
    }

    val lastClaim = profile?.lastWeedClaimTime ?: 0L
    val claimInterval = 6 * 60 * 60 * 1000L // 6 hours
    val timePassed = currentTimeMillis - lastClaim
    val isClaimReady = lastClaim == 0L || timePassed >= claimInterval
    val remainingMs = claimInterval - timePassed

    // Swap State
    var swapFromAsset by remember { mutableStateOf("WEED") }
    var swapToAsset by remember { mutableStateOf("USDC") }
    var swapAmountInput by remember { mutableStateOf("10") }

    val fromRate = averageRates[swapFromAsset] ?: 0.01
    val toRate = averageRates[swapToAsset] ?: 0.01
    val calculatedReceive = remember(swapAmountInput, fromRate, toRate) {
        val amt = swapAmountInput.toDoubleOrNull() ?: 0.0
        if (toRate > 0) (amt * fromRate) / toRate else 0.0
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // 1. WEED MINING CORE NODE
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                border = BorderStroke(1.dp, currentSkin.primaryColor.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "WEED HARVEST CORE",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = currentSkin.primaryColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Current Streak Balance",
                                fontSize = 15.sp,
                                fontFamily = RajdhaniFontFamily,
                                color = Color(0xFFB0B0C5)
                            )
                            Text(
                                text = "${profile?.weedStreak ?: 0} Consecutive Claims",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = RajdhaniFontFamily,
                                color = Color.White
                            )
                        }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Black),
                            border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.3f)),
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text(
                                text = "Multiplier: +${profile?.weedStreak?.plus(1) ?: 1}x WEED",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = RajdhaniFontFamily,
                                color = NeonGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Timer or Status box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black, RoundedCornerShape(4.dp))
                            .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(if (isClaimReady) NeonGreen else NeonGold, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isClaimReady) "CORE HARVESTER ARMED" else "COOLANT FLUSH IN PROGRESS",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isClaimReady) NeonGreen else NeonGold
                            )
                        }

                        Text(
                            text = if (isClaimReady) {
                                "READY"
                            } else {
                                val remainingSec = (remainingMs / 1000).toInt()
                                val hours = remainingSec / 3600
                                val minutes = (remainingSec % 3600) / 60
                                val seconds = remainingSec % 60
                                "%02d:%02d:%02d".format(hours, minutes, seconds)
                            },
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isClaimReady) NeonGreen else Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    CyberButton(
                        onClick = { viewModel.claimActivityWeed() },
                        skin = currentSkin,
                        enabled = isClaimReady,
                        containerColor = NeonGreen,
                        contentColor = CyberObsidian,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        AutoResizingText(
                            text = "EXTRACT ACTIVITY WEED",
                            targetTextSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = RajdhaniFontFamily,
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Claim once every 6 hours. Starts at 1 WEED, increments by +1 each claim. Miss the 6h window after readiness and streak resets to 1!",
                        fontSize = 17.sp,
                        fontFamily = RajdhaniFontFamily,
                        color = Color(0xFFB0B0C5),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 2. AMM SWAP & PRICE FEED
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                border = BorderStroke(1.dp, currentSkin.secondaryColor.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AI TREASURER AMM SWAP & TICKERS",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = currentSkin.primaryColor
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Live Tickers Row
                    Text(
                        text = "AGGREGATED AVERAGES (TOP 10 EXCHANGES)",
                        fontSize = 14.sp,
                        fontFamily = RajdhaniFontFamily,
                        color = Color(0xFFB0B0C5)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("BTC", "ETH", "SOL", "WEED").forEach { coin ->
                            val isSelectedCoin = selectedTickerCoin == coin
                            val rate = averageRates[coin] ?: 0.0
                            val displayPrice = if (rate >= 1.0) "$${String.format("%.2f", rate)}" else "$${String.format("%.4f", rate)}"
                            
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { 
                                        selectedTickerCoin = coin
                                        showTickersDetail = true
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelectedCoin) Color.Black else Color(0xFF0A0A10)
                                ),
                                border = BorderStroke(
                                    1.dp, 
                                    if (isSelectedCoin) currentSkin.primaryColor else Color.DarkGray
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = coin,
                                        fontSize = 17.sp,
                                        fontFamily = RajdhaniFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelectedCoin) currentSkin.primaryColor else Color.White
                                    )
                                    Text(
                                        text = displayPrice,
                                        fontSize = 17.sp,
                                        fontFamily = RajdhaniFontFamily,
                                        color = Color.LightGray
                                    )
                                }
                            }
                        }
                    }

                    // Tickers Detail Table
                    if (showTickersDetail) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Black),
                            border = BorderStroke(1.dp, Color.DarkGray)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "$selectedTickerCoin LIVE ARBITRAGE ARRAYS",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = RajdhaniFontFamily,
                                        color = currentSkin.primaryColor
                                    )
                                    Text(
                                        text = "[X] CLOSE",
                                        fontSize = 17.sp,
                                        fontFamily = RajdhaniFontFamily,
                                        color = Color.Red,
                                        modifier = Modifier.clickable { showTickersDetail = false }
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                val exchanges = listOf(
                                    "Binance", "Coinbase", "Kraken", "OKX", "Bybit", 
                                    "Huobi", "Gate.io", "Bitfinex", "KuCoin", "Gemini"
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    exchanges.forEach { ex ->
                                        val price = exchangeTickers[ex]?.get(selectedTickerCoin) ?: averageRates[selectedTickerCoin] ?: 0.0
                                        val displayPrice = if (price >= 1.0) "$${String.format("%.3f", price)}" else "$${String.format("%.5f", price)}"
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "• $ex",
                                                fontSize = 17.sp,
                                                fontFamily = RajdhaniFontFamily,
                                                color = Color(0xFFB0B0C5)
                                            )
                                            Text(
                                                text = displayPrice,
                                                fontSize = 17.sp,
                                                fontFamily = RajdhaniFontFamily,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Swap Box
                    Text(
                        text = "ROUTER SWAP UTILITY (100% UNISWAP ROUTER SIM)",
                        fontSize = 14.sp,
                        fontFamily = RajdhaniFontFamily,
                        color = Color(0xFFB0B0C5),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // From Asset Field
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SELL ASSET",
                                fontSize = 17.sp,
                                fontFamily = RajdhaniFontFamily,
                                color = Color(0xFFB0B0C5)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black, RoundedCornerShape(4.dp))
                                    .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                                    .clickable {
                                        val next = when (swapFromAsset) {
                                            "WEED" -> "USDC"
                                            "USDC" -> "SOL"
                                            "SOL" -> "ETH"
                                            else -> "WEED"
                                        }
                                        swapFromAsset = next
                                        if (next != "WEED") {
                                            swapToAsset = "WEED"
                                        } else if (swapToAsset == "WEED") {
                                            swapToAsset = "USDC"
                                        }
                                    }
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = swapFromAsset,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = RajdhaniFontFamily,
                                    color = Color.White
                                )
                                Text(text = "▼", fontSize = 17.sp, color = Color(0xFFB0B0C5))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = swapAmountInput,
                                onValueChange = { swapAmountInput = it },
                                singleLine = true,
                                textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 16.sp, color = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Text(
                            text = "➔",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB0B0C5)
                        )

                        // To Asset Field
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "RECEIVE ASSET",
                                fontSize = 17.sp,
                                fontFamily = RajdhaniFontFamily,
                                color = Color(0xFFB0B0C5)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black, RoundedCornerShape(4.dp))
                                    .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                                    .clickable {
                                        val next = when (swapToAsset) {
                                            "WEED" -> "USDC"
                                            "USDC" -> "SOL"
                                            "SOL" -> "ETH"
                                            else -> "WEED"
                                        }
                                        swapToAsset = next
                                        if (next != "WEED") {
                                            swapFromAsset = "WEED"
                                        } else if (swapFromAsset == "WEED") {
                                            swapFromAsset = "USDC"
                                        }
                                    }
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = swapToAsset,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = RajdhaniFontFamily,
                                    color = Color.White
                                )
                                Text(text = "▼", fontSize = 17.sp, color = Color(0xFFB0B0C5))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .background(Color(0xFF0F0F16), RoundedCornerShape(4.dp))
                                    .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                val outPrice = if (calculatedReceive >= 1.0) String.format("%.2f", calculatedReceive) else String.format("%.4f", calculatedReceive)
                                Text(
                                    text = outPrice,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = RajdhaniFontFamily,
                                    color = NeonGreen
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    CyberButton(
                        onClick = {
                            val amt = swapAmountInput.toDoubleOrNull() ?: 0.0
                            viewModel.swapAssets(swapFromAsset, swapToAsset, amt, calculatedReceive)
                        },
                        skin = currentSkin,
                        containerColor = currentSkin.secondaryColor,
                        contentColor = CyberObsidian,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        AutoResizingText(
                            text = "EXECUTE AUTOMATED AMM ROUTER",
                            targetTextSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = RajdhaniFontFamily,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // 3. AI TREASURER LEDGER
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                border = BorderStroke(1.dp, Color.DarkGray),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AI TREASURER INTERNALS LEDGER",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(Color.Black, RoundedCornerShape(4.dp))
                            .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(treasurerLogs) { log ->
                                val color = when {
                                    log.contains("[MINT]") -> NeonGreen
                                    log.contains("[BURN]") -> NeonMagenta
                                    log.contains("[SWAP]") -> NeonCyan
                                    else -> Color.Gray
                                }
                                Text(
                                    text = log,
                                    fontSize = 17.sp,
                                    fontFamily = RajdhaniFontFamily,
                                    color = color,
                                    lineHeight = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ======================== NEURAL ADS PANEL ========================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeuralAdsPanel(viewModel: TarotViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val enabledThemes by viewModel.enabledAdThemes.collectAsStateWithLifecycle()
    val banners by viewModel.adBanners.collectAsStateWithLifecycle()
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()

    var adPromptInput by remember { mutableStateOf("") }
    var selectedAdTheme by remember { mutableStateOf("Cybernetics") }
    var selectedColorPreset by remember { mutableStateOf("#FF00FF") } // Default Neon Magenta

    val isAdFreeSubscribed = profile?.isAdFreeSubscribed == true && (profile?.adFreeExpiryTime ?: 0L) > System.currentTimeMillis()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // 1. AD-SHIELD STATUS & OPT-IN
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                border = BorderStroke(1.dp, if (isAdFreeSubscribed) NeonGreen else Color.DarkGray),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AD-SHIELD DEFLATIONARY PROTOCOL",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAdFreeSubscribed) NeonGreen else Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Status Indicator",
                                fontSize = 14.sp,
                                fontFamily = RajdhaniFontFamily,
                                color = Color(0xFFB0B0C5)
                            )
                            Text(
                                text = if (isAdFreeSubscribed) "🛡️ ACTIVE PROTECTION LAYER" else "⚠️ UNPROTECTED TUNNELS",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = RajdhaniFontFamily,
                                color = if (isAdFreeSubscribed) NeonGreen else NeonGold
                            )
                        }
                        
                        if (isAdFreeSubscribed) {
                            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                            val expDate = sdf.format(Date(profile?.adFreeExpiryTime ?: 0L))
                            Text(
                                text = "Expires: $expDate",
                                fontSize = 17.sp,
                                fontFamily = RajdhaniFontFamily,
                                color = Color(0xFFB0B0C5)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    CyberButton(
                        onClick = { viewModel.subscribeAdFree {} },
                        skin = currentSkin,
                        containerColor = ElegantIndigo,
                        contentColor = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        AutoResizingText(
                            text = "BUY 30-DAY AD-SHIELD (500 WEED)",
                            targetTextSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = RajdhaniFontFamily,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ad-Shield filters all incoming sponsored broadcast cards instantly. 100% of WEED spent is BURNED by the AI Treasurer.",
                        fontSize = 17.sp,
                        fontFamily = RajdhaniFontFamily,
                        color = Color(0xFFB0B0C5),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 2. BROADCAST A NEW BANNER (GEMINI POWERED)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                border = BorderStroke(1.dp, currentSkin.primaryColor.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "REGISTER BROADCAST INTERNALS SLOT",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = currentSkin.primaryColor
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "AD PROMPT (What are you advertising? AI writes it!)",
                        fontSize = 14.sp,
                        fontFamily = RajdhaniFontFamily,
                        color = Color(0xFFB0B0C5)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = adPromptInput,
                        onValueChange = { adPromptInput = it },
                        placeholder = { Text("e.g., Sell custom cyberdecks with preloaded software tunnels.", fontSize = 16.sp, color = Color.DarkGray) },
                        textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 16.sp, color = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Ad Theme Row Selector
                    Text(
                        text = "BROADCAST TOPIC/THEME",
                        fontSize = 14.sp,
                        fontFamily = RajdhaniFontFamily,
                        color = Color(0xFFB0B0C5)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    val themes = listOf("Cybernetics", "Tarot", "Biotech", "Darknet", "AI Overlords", "Quantum Finance")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(themes) { th ->
                            val isSel = selectedAdTheme == th
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isSel) currentSkin.primaryColor.copy(alpha = 0.2f) else Color.Transparent,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .border(1.dp, if (isSel) currentSkin.primaryColor else Color.DarkGray, RoundedCornerShape(4.dp))
                                    .clickable { selectedAdTheme = th }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = th.uppercase(),
                                    fontSize = 17.sp,
                                    fontFamily = RajdhaniFontFamily,
                                    color = if (isSel) currentSkin.primaryColor else Color(0xFFB0B0C5)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Color Preset Selector
                    Text(
                        text = "GLOW ACCENT COLOR PRESET",
                        fontSize = 14.sp,
                        fontFamily = RajdhaniFontFamily,
                        color = Color(0xFFB0B0C5)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    val colors = listOf(
                        "#FF00FF" to "Magenta", 
                        "#00FF00" to "Green", 
                        "#00FFFF" to "Cyan", 
                        "#FFD700" to "Gold", 
                        "#FF4500" to "Red"
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        colors.forEach { (hex, name) ->
                            val isSel = selectedColorPreset == hex
                            val col = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { currentSkin.primaryColor }
                            
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(col, CircleShape)
                                    .border(
                                        width = 2.dp,
                                        color = if (isSel) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColorPreset = hex }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    CyberButton(
                        onClick = {
                            viewModel.purchaseAdBanner(adPromptInput, selectedAdTheme, selectedColorPreset) { success ->
                                if (success) adPromptInput = ""
                            }
                        },
                        skin = currentSkin,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        AutoResizingText(
                            text = "COMPILE & BROADCAST SLOT (100 WEED)",
                            targetTextSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = RajdhaniFontFamily,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // 3. SEGMENT FILTERS / QUEUE BALANCE
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                border = BorderStroke(1.dp, Color.DarkGray),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "QUEUE SEGMENT FILTERS (MINIMUM 3 ACTIVE)",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "To balance decentralized broad-cast channels, you must keep at least 3 active sectors enabled at all times.",
                        fontSize = 17.sp,
                        fontFamily = RajdhaniFontFamily,
                        color = Color(0xFFB0B0C5)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val themes = listOf("Cybernetics", "Tarot", "Biotech", "Darknet", "AI Overlords", "Quantum Finance")
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        themes.forEach { th ->
                            val isEnabled = enabledThemes.contains(th)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black, RoundedCornerShape(4.dp))
                                    .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                                    .clickable { viewModel.toggleAdTheme(th) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = th.uppercase(),
                                    fontSize = 14.sp,
                                    fontFamily = RajdhaniFontFamily,
                                    color = if (isEnabled) Color.White else Color(0xFFB0B0C5)
                                )
                                Text(
                                    text = if (isEnabled) "ACTIVE (TUNNEL ON)" else "SHUTDOWN",
                                    fontSize = 17.sp,
                                    fontFamily = RajdhaniFontFamily,
                                    color = if (isEnabled) NeonGreen else Color.Red,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// FULL-SCREEN IMMERSIVE CARD REVIEW SYSTEM
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenCardReview(
    card: CyberTarotCard,
    mintedCard: MintedCard? = null,
    isMarketView: Boolean = false,
    systemLanguage: String = "en",
    skin: CyberSkin = CyberSkin.JAMAICA,
    onClose: () -> Unit,
    onActionClick: () -> Unit = {}
) {
    var isFlipped by remember { mutableStateOf(false) }

    val localizedName = CardLocalizationHelper.getLocalizedName(card.index, systemLanguage)
    val localizedTraditional = CardLocalizationHelper.getLocalizedTraditionalName(card.index, systemLanguage)
    val localizedDesc = CardLocalizationHelper.getLocalizedDescription(card.index, systemLanguage)
    val localizedCircuitry = CardLocalizationHelper.getLocalizedCircuitryElement(card.index, systemLanguage)

    // Dialog layout styled as full screen/immersive
    AlertDialog(
        onDismissRequest = onClose,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false // this enables full-screen dialog layout
        ),
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE605050A)), // solid dark neon wash
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background matrix particle visual simulation
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Draw clean background glowing rings
                    drawCircle(
                        color = NeonCyan.copy(alpha = 0.05f),
                        radius = size.minDimension / 1.5f,
                        center = center,
                        style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                    )
                    drawCircle(
                        color = NeonMagenta.copy(alpha = 0.03f),
                        radius = size.minDimension / 2f,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Header / Top controls
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "NEURAL ANALOG REVIEW",
                                color = NeonCyan,
                                fontSize = 15.sp,
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                localizedName.uppercase(),
                                color = Color.White,
                                fontSize = 24.sp,
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.05f), CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    // Rotating Cyber Card Container
                    val rotationState by animateFloatAsState(
                        targetValue = if (isFlipped) 180f else 0f,
                        animationSpec = tween(600, easing = LinearOutSlowInEasing),
                        label = "cardFlip"
                    )

                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                rotationY = rotationState
                                cameraDistance = 12 * density
                            }
                            .clickable {
                                if (!isFlipped) {
                                    // 2nd tap: flip card to show back/shirt
                                    isFlipped = true
                                    com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.CARD_DRAW)
                                } else {
                                    // 3rd tap: return from full-screen back to original screen
                                    com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.CLICK)
                                    onClose()
                                }
                            }
                            .width(290.dp)
                            .height(440.dp)
                            .shadow(24.dp, RoundedCornerShape(16.dp), ambientColor = NeonCyan, spotColor = NeonMagenta)
                            .background(CyberCharcoal, RoundedCornerShape(16.dp))
                            .border(
                                width = 2.dp,
                                brush = Brush.linearGradient(
                                    colors = if (mintedCard?.rarity == "Legend") listOf(NeonGold, NeonMagenta)
                                            else if (mintedCard?.rarity == "Epic") listOf(NeonMagenta, NeonCyan)
                                            else if (mintedCard?.rarity == "Rare") listOf(NeonCyan, NeonGreen)
                                            else listOf(NeonGreen, Color.DarkGray)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Card Content based on rotation
                        if (rotationState <= 90f) {
                            // FRONT SIDE
                            Box(modifier = Modifier.fillMaxSize()) {
                                CyberRarityShaderOverlay(
                                    rarity = mintedCard?.rarity ?: card.defaultRarity,
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clip(RoundedCornerShape(16.dp))
                                )
                                Column(
                                    modifier = Modifier
                                            .fillMaxSize()
                                            .padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Top status indicators
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CyberRarityGlowBadge(
                                            rarity = mintedCard?.rarity ?: card.defaultRarity
                                        )
                                    Text(
                                        text = mintedCard?.let { "#${String.format("%04d", it.mintNumber)}" } ?: "SYS_REF",
                                        fontSize = 14.sp,
                                        fontFamily = RajdhaniFontFamily,
                                        color = Color(0xFFB0B0C5)
                                    )
                                }

                                // Central Premium Cyberpunk Artwork
                                val cardRes = getCardImage(card.index)
                                if (cardRes != null) {
                                    Image(
                                        painter = painterResource(id = cardRes),
                                        contentDescription = localizedName,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(250.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(
                                                width = 1.5.dp,
                                                color = when (mintedCard?.rarity ?: card.defaultRarity) {
                                                    "Legend" -> NeonGold
                                                    "Epic" -> NeonMagenta
                                                    "Rare" -> NeonCyan
                                                    else -> NeonGreen
                                                },
                                                shape = RoundedCornerShape(12.dp)
                                            ),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(250.dp)
                                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CardSymbolIcon(
                                            cardIndex = card.index,
                                            tintColor = when (mintedCard?.rarity ?: card.defaultRarity) {
                                                "Legend" -> NeonGold
                                                "Epic" -> NeonMagenta
                                                "Rare" -> NeonCyan
                                                else -> NeonGreen
                                            },
                                            modifier = Modifier.size(64.dp)
                                        )
                                    }
                                }

                                // Bottom identity block & Flip hint
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = localizedName.uppercase(),
                                        fontSize = 15.sp,
                                        fontFamily = RajdhaniFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = localizedCircuitry.uppercase(),
                                        fontSize = 14.sp,
                                        fontFamily = RajdhaniFontFamily,
                                        color = NeonCyan,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.White.copy(alpha = 0.08f))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (systemLanguage == "ru") "ТАП: ПЕРЕВЕРНУТЬ (РУБАШКА) ↻" else "TAP: FLIP TO SHIRT ↻",
                                            color = NeonCyan,
                                            fontSize = 14.sp,
                                            fontFamily = RajdhaniFontFamily,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                        } else {
                            // BACK SIDE: Ornate Series-specific card back artwork
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { rotationY = 180f }
                                    .clip(RoundedCornerShape(16.dp))
                            ) {
                                val backResId = getCardBackImage(card.series)
                                Image(
                                    painter = painterResource(id = backResId),
                                    contentDescription = "${card.series} Card Back",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                // Overlay badge with series name and tap hint
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color.Transparent, Color(0xDD0A0918))
                                            )
                                        )
                                        .padding(bottom = 12.dp, top = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = card.series.uppercase(),
                                            color = when (card.series) {
                                                "Genesis Series" -> Color(0xFFFFD700)
                                                "Amber Series" -> Color(0xFFFF9900)
                                                "Seas & Oceans" -> NeonCyan
                                                else -> NeonGreen
                                            },
                                            fontSize = 16.sp,
                                            fontFamily = RajdhaniFontFamily,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (systemLanguage == "ru") "3-Й ТАП: ВЕРНУТЬСЯ В ИСХОДНЫЙ ВИД ✕" else "3RD TAP: RETURN TO VIEW ✕",
                                            color = NeonGold,
                                            fontSize = 14.sp,
                                            fontFamily = RajdhaniFontFamily,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Node Specification Lore section
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                        border = BorderStroke(1.dp, Color(0x1FFFFFFF)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "CORE SPECIFICATION MATRIX",
                                fontSize = 16.sp,
                                color = NeonCyan,
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = localizedDesc,
                                fontSize = 16.sp,
                                color = Color.White,
                                fontFamily = RajdhaniFontFamily,
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Traditional Equiv: ",
                                    fontSize = 15.sp,
                                    color = Color(0xFFB0B0C5),
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = localizedTraditional,
                                    fontSize = 15.sp,
                                    color = NeonMagenta,
                                    fontFamily = RajdhaniFontFamily
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                                Text(
                                    text = "Circuitry Bus: ",
                                    fontSize = 15.sp,
                                    color = Color(0xFFB0B0C5),
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = localizedCircuitry,
                                    fontSize = 15.sp,
                                    color = NeonGreen,
                                    fontFamily = RajdhaniFontFamily
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Actions block depending on the view
                    if (isMarketView) {
                        CyberButton(
                            onClick = onActionClick,
                            skin = skin,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .height(48.dp)
                        ) {
                            AutoResizingText(
                                text = "ACQUIRE NODE SYSTEM",
                                targetTextSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = RajdhaniFontFamily,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        CyberButton(
                            onClick = onActionClick,
                            skin = skin,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .height(48.dp)
                        ) {
                            AutoResizingText(
                                text = "NODE SYSTEM UTILITIES",
                                targetTextSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = RajdhaniFontFamily,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    )
}

// ==========================================
// INTERACTIVE RADIAL CIRCUIT DIAGNOSTICS SYSTEM
// ==========================================

data class RadialChartSlice(
    val label: String,
    val value: Float,
    val color: Color,
    val description: String = ""
)

@Composable
fun InteractiveCircuitRadialChart(
    slices: List<RadialChartSlice>,
    title: String,
    modifier: Modifier = Modifier,
    onSliceSelected: (Int) -> Unit,
    selectedIndex: Int
) {
    // Sum the values
    val total = remember(slices) { slices.sumOf { it.value.toDouble() }.toFloat() }
    
    // Animate angles for extra sleek sci-fi transition
    val transitionState = remember { MutableTransitionState(0f) }
    LaunchedEffect(slices) {
        transitionState.targetState = 1f
    }
    val animScale by animateFloatAsState(
        targetValue = if (transitionState.currentState == 1f) 1f else 0f,
        animationSpec = tween(1000, easing = { input -> DecelerateInterpolator().getInterpolation(input) }),
        label = "radialAnimation"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(CyberCharcoal, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0x11FFFFFF), RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title.uppercase(),
            fontSize = 16.sp,
            fontFamily = RajdhaniFontFamily,
            color = Color(0xFFB0B0C5),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (total == 0f) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(Color(0xFF07070C), CircleShape)
                    .border(1.dp, Color.DarkGray.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "NO TELEMETRY",
                        fontSize = 15.sp,
                        fontFamily = RajdhaniFontFamily,
                        color = Color(0xFFB0B0C5),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            var canvasSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

            Box(
                modifier = Modifier
                    .size(220.dp)
                    .pointerInput(slices, total) {
                        detectTapGestures { offset ->
                            val cx = canvasSize.width / 2f
                            val cy = canvasSize.height / 2f
                            val dx = offset.x - cx
                            val dy = offset.y - cy
                            val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                            
                            val outerRadius = canvasSize.minDimension / 2f
                            val innerRadius = outerRadius * 0.55f

                            if (dist in innerRadius..outerRadius) {
                                val angleRad = Math.atan2(dy.toDouble(), dx.toDouble())
                                var angleDeg = Math.toDegrees(angleRad)
                                if (angleDeg < 0) angleDeg += 360.0
                                
                                var accum = 0f
                                slices.forEachIndexed { idx, slice ->
                                    val sweep = (slice.value / total) * 360f
                                    if (angleDeg >= accum && angleDeg < accum + sweep) {
                                        onSliceSelected(idx)
                                        return@detectTapGestures
                                    }
                                    accum += sweep
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    canvasSize = size
                    val width = size.width
                    val height = size.height
                    val center = Offset(width / 2f, height / 2f)
                    val outerRadius = size.minDimension / 2f
                    val innerRadius = outerRadius * 0.55f
                    val ringWidth = outerRadius - innerRadius

                    drawCircle(
                        color = Color.White.copy(alpha = 0.03f),
                        radius = outerRadius,
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.02f),
                        radius = innerRadius,
                        style = Stroke(width = 1.dp.toPx())
                    )

                    var currentAngle = 0f
                    slices.forEachIndexed { idx, slice ->
                        val sweepAngle = (slice.value / total) * 360f * animScale
                        val isSelected = idx == selectedIndex

                        val arcRadius = innerRadius + (ringWidth / 2f)
                        val arcStrokeWidth = ringWidth - (if (isSelected) 0.dp.toPx() else 4.dp.toPx())

                        if (isSelected) {
                            drawArc(
                                color = slice.color.copy(alpha = 0.15f),
                                startAngle = currentAngle + 1.5f,
                                sweepAngle = sweepAngle - 3f,
                                useCenter = false,
                                style = Stroke(width = arcStrokeWidth + 12.dp.toPx(), cap = StrokeCap.Butt),
                                topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
                                size = androidx.compose.ui.geometry.Size(arcRadius * 2, arcRadius * 2)
                            )
                        }

                        drawArc(
                            color = if (isSelected) slice.color else slice.color.copy(alpha = 0.65f),
                            startAngle = currentAngle + 1.5f,
                            sweepAngle = sweepAngle - 3f,
                            useCenter = false,
                            style = Stroke(width = arcStrokeWidth, cap = StrokeCap.Butt),
                            topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
                            size = androidx.compose.ui.geometry.Size(arcRadius * 2, arcRadius * 2)
                        )

                        if (isSelected && sweepAngle > 10f) {
                            val midAngleRad = Math.toRadians((currentAngle + sweepAngle / 2f).toDouble())
                            val cos = Math.cos(midAngleRad).toFloat()
                            val sin = Math.sin(midAngleRad).toFloat()

                            val lineStart = Offset(center.x + outerRadius * cos, center.y + outerRadius * sin)
                            val lineEnd = Offset(center.x + (outerRadius + 18.dp.toPx()) * cos, center.y + (outerRadius + 18.dp.toPx()) * sin)
                            
                            drawLine(
                                color = slice.color,
                                start = lineStart,
                                end = lineEnd,
                                strokeWidth = 1.5.dp.toPx()
                            )
                            drawCircle(
                                color = slice.color,
                                radius = 4.dp.toPx(),
                                center = lineEnd
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 1.5.dp.toPx(),
                                center = lineEnd
                            )
                        }

                        currentAngle += (slice.value / total) * 360f
                    }

                    drawLine(
                        color = Color.White.copy(alpha = 0.05f),
                        start = Offset(center.x - 12.dp.toPx(), center.y),
                        end = Offset(center.x + 12.dp.toPx(), center.y),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.05f),
                        start = Offset(center.x, center.y - 12.dp.toPx()),
                        end = Offset(center.x, center.y + 12.dp.toPx()),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(32.dp)
                ) {
                    val activeSlice = slices.getOrNull(selectedIndex)
                    if (activeSlice != null) {
                        Text(
                            text = "${((activeSlice.value / total) * 100).toInt()}%",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = RajdhaniFontFamily,
                            color = activeSlice.color
                        )
                        Text(
                            text = "${activeSlice.value.toInt()} UNIT" + (if (activeSlice.value == 1f) "" else "S"),
                            fontSize = 14.sp,
                            fontFamily = RajdhaniFontFamily,
                            color = Color(0xFFB0B0C5),
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "TOTAL",
                            fontSize = 16.sp,
                            fontFamily = RajdhaniFontFamily,
                            color = Color(0xFFB0B0C5),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${total.toInt()}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = RajdhaniFontFamily,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            slices.forEachIndexed { index, slice ->
                val isSelected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) slice.color.copy(alpha = 0.15f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (isSelected) slice.color else Color.DarkGray.copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onSliceSelected(index) }
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(slice.color, CircleShape)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = slice.label.uppercase(),
                            fontSize = 17.sp,
                            fontFamily = RajdhaniFontFamily,
                            color = if (isSelected) Color.White else Color(0xFFB0B0C5),
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "${slice.value.toInt()}",
                            fontSize = 15.sp,
                            fontFamily = RajdhaniFontFamily,
                            color = slice.color,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NeuralAnalyticsView(
    mintedCards: List<com.example.data.MintedCard>,
    marketListings: List<com.example.data.MarketListing>
) {
    var selectedCollectionSlice by remember { mutableStateOf(0) }
    var selectedMarketSlice by remember { mutableStateOf(0) }

    val collectionSlices = remember(mintedCards) {
        val totalMints = mintedCards.size
        if (totalMints == 0) {
            listOf(
                RadialChartSlice("LOGIC GATES", 5f, NeonCyan, "Central processors, logic networks, registers, and cryptographic units."),
                RadialChartSlice("SILICON MEMORY", 3f, NeonGreen, "Flash ROM, volatile RAM arrays, and persistent sector matrices."),
                RadialChartSlice("NEURAL COMMS", 4f, NeonMagenta, "Optoelectronic fiber, bridge gates, and global network transceivers."),
                RadialChartSlice("POWER ENGINE", 2f, NeonGold, "Internal clock phase-locked loops, thermal conduits, and master relays.")
            )
        } else {
            val elementCounts = mutableMapOf(
                "LOGIC GATES" to 0f,
                "SILICON MEMORY" to 0f,
                "NEURAL COMMS" to 0f,
                "POWER ENGINE" to 0f
            )
            mintedCards.forEach { mint ->
                val category = when (mint.cardIndex % 4) {
                    0 -> "LOGIC GATES"
                    1 -> "SILICON MEMORY"
                    2 -> "NEURAL COMMS"
                    else -> "POWER ENGINE"
                }
                elementCounts[category] = (elementCounts[category] ?: 0f) + 1f
            }
            listOf(
                RadialChartSlice("LOGIC GATES", elementCounts["LOGIC GATES"] ?: 0f, NeonCyan, "Central processors, logic networks, registers, and cryptographic units."),
                RadialChartSlice("SILICON MEMORY", elementCounts["SILICON MEMORY"] ?: 0f, NeonGreen, "Flash ROM, volatile RAM arrays, and persistent sector matrices."),
                RadialChartSlice("NEURAL COMMS", elementCounts["NEURAL COMMS"] ?: 0f, NeonMagenta, "Optoelectronic fiber, bridge gates, and global network transceivers."),
                RadialChartSlice("POWER ENGINE", elementCounts["POWER ENGINE"] ?: 0f, NeonGold, "Internal clock phase-locked loops, thermal conduits, and master relays.")
            )
        }
    }

    val marketSlices = remember(marketListings) {
        val totalListings = marketListings.size
        if (totalListings == 0) {
            listOf(
                RadialChartSlice("COMMON", 32f, NeonGreen, "Standard utility subroutines, high availability index, low-cost execution nodes."),
                RadialChartSlice("RARE", 16f, NeonCyan, "Performance drivers, system kernel controllers, medium-level access nodes."),
                RadialChartSlice("EPIC", 8f, NeonMagenta, "Highly complex mainframe clusters, deep learning matrices, restricted credentials."),
                RadialChartSlice("LEGEND", 4f, NeonGold, "Sovereign global mainframe roots, self-evolving AI oracle systems, ultra-scarce nodes.")
            )
        } else {
            val rarityCounts = mutableMapOf(
                "COMMON" to 0f,
                "RARE" to 0f,
                "EPIC" to 0f,
                "LEGEND" to 0f
            )
            marketListings.forEach { listing ->
                val key = listing.rarity.uppercase()
                rarityCounts[key] = (rarityCounts[key] ?: 0f) + 1f
            }
            listOf(
                RadialChartSlice("COMMON", rarityCounts["COMMON"] ?: 0f, NeonGreen, "Standard utility subroutines, high availability index, low-cost execution nodes."),
                RadialChartSlice("RARE", rarityCounts["RARE"] ?: 0f, NeonCyan, "Performance drivers, system kernel controllers, medium-level access nodes."),
                RadialChartSlice("EPIC", rarityCounts["EPIC"] ?: 0f, NeonMagenta, "Highly complex mainframe clusters, deep learning matrices, restricted credentials."),
                RadialChartSlice("LEGEND", rarityCounts["LEGEND"] ?: 0f, NeonGold, "Sovereign global mainframe roots, self-evolving AI oracle systems, ultra-scarce nodes.")
            )
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF09090E)),
                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "scannerAlpha"
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(NeonCyan.copy(alpha = pulseAlpha), CircleShape)
                            .border(1.dp, NeonCyan, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "ACTIVE NET-DECK SCANNER: STATUS STABLE | METRICS SYNC ONLINE",
                        fontSize = 14.sp,
                        fontFamily = RajdhaniFontFamily,
                        color = Color.LightGray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            InteractiveCircuitRadialChart(
                slices = collectionSlices,
                title = "Card Collection Frequency (Elements)",
                selectedIndex = selectedCollectionSlice,
                onSliceSelected = { selectedCollectionSlice = it },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            collectionSlices.getOrNull(selectedCollectionSlice)?.let { slice ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                    border = BorderStroke(1.dp, slice.color.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(slice.color, CircleShape))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${slice.label} TELEMETRY",
                                fontSize = 16.sp,
                                fontFamily = RajdhaniFontFamily,
                                color = slice.color,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = slice.description,
                            fontSize = 16.sp,
                            fontFamily = RajdhaniFontFamily,
                            color = Color.White,
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "STATUS: Compiled | Allocated density: ${slice.value.toInt()} nodes.",
                            fontSize = 15.sp,
                            fontFamily = RajdhaniFontFamily,
                            color = Color(0xFFB0B0C5)
                        )
                    }
                }
            }
        }

        item {
            InteractiveCircuitRadialChart(
                slices = marketSlices,
                title = "Market Rarity Distribution",
                selectedIndex = selectedMarketSlice,
                onSliceSelected = { selectedMarketSlice = it },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            marketSlices.getOrNull(selectedMarketSlice)?.let { slice ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                    border = BorderStroke(1.dp, slice.color.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(slice.color, CircleShape))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${slice.label} DEPLOYMENT STATS",
                                fontSize = 16.sp,
                                fontFamily = RajdhaniFontFamily,
                                color = slice.color,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = slice.description,
                            fontSize = 16.sp,
                            fontFamily = RajdhaniFontFamily,
                            color = Color.White,
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "COMPLIANCE LEVEL: SECURE | Listed active node exchange instances: ${slice.value.toInt()} systems.",
                            fontSize = 15.sp,
                            fontFamily = RajdhaniFontFamily,
                            color = Color(0xFFB0B0C5)
                        )
                    }
                }
            }
        }
    }
}

// ======================== CYBER AD BANNER COMPOSABLE ========================
@Composable
fun CyberAdBanner(viewModel: TarotViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val isAdFree = profile?.isAdFreeSubscribed == true && (profile?.adFreeExpiryTime ?: 0L) > System.currentTimeMillis()
    if (isAdFree) return

    val banners by viewModel.adBanners.collectAsStateWithLifecycle()
    val enabledThemes by viewModel.enabledAdThemes.collectAsStateWithLifecycle()
    
    // Filter banners matching user's enabled themes
    val activeBanners = remember(banners, enabledThemes) {
        banners.filter { enabledThemes.contains(it.theme) }
    }
    
    if (activeBanners.isEmpty()) return

    // Cycle every 7 seconds through active banners
    var currentIndex by remember { mutableStateOf(0) }
    LaunchedEffect(activeBanners.size) {
        if (activeBanners.size > 1) {
            while (true) {
                delay(7000)
                currentIndex = (currentIndex + 1) % activeBanners.size
            }
        }
    }
    
    val banner = activeBanners.getOrNull(currentIndex % activeBanners.size) ?: return
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(
                width = 1.dp,
                color = try { Color(android.graphics.Color.parseColor(banner.colorHex)).copy(alpha = 0.8f) } catch (e: Exception) { currentSkin.primaryColor },
                shape = RoundedCornerShape(8.dp)
            )
            .clickable {
                viewModel.incrementAdClicks(banner.id)
                viewModel.postNotification("📡 Relay Signal Sent: Initiating contact request to ${banner.authorContactOnion}. (Fee paid: ${banner.messagePriceWeed} WEED).")
            },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xE60A0A12)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(try { Color(android.graphics.Color.parseColor(banner.colorHex)) } catch (e: Exception) { currentSkin.primaryColor }, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = banner.theme.uppercase(),
                        fontSize = 17.sp,
                        fontFamily = RajdhaniFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = try { Color(android.graphics.Color.parseColor(banner.colorHex)) } catch (e: Exception) { currentSkin.primaryColor }
                    )
                }
                Text(
                    text = "SPONSORED (BY ${banner.authorName.uppercase()})",
                    fontSize = 16.sp,
                    fontFamily = RajdhaniFontFamily,
                    color = Color(0xFFB0B0C5)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = banner.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = RajdhaniFontFamily,
                color = Color.White
            )
            Text(
                text = banner.content,
                fontSize = 15.sp,
                fontFamily = RajdhaniFontFamily,
                color = Color.LightGray,
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔗 CLICK TO NEGOTIATE TERM DEALS",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = RajdhaniFontFamily,
                    color = try { Color(android.graphics.Color.parseColor(banner.colorHex)) } catch (e: Exception) { currentSkin.primaryColor }
                )
                Text(
                    text = "Clicks: ${banner.clicks}",
                    fontSize = 17.sp,
                    fontFamily = RajdhaniFontFamily,
                    color = Color.LightGray
                )
            }
        }
    }
}

// ======================== SWARM DECENTRALIZED STORAGE PANEL ========================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwarmStoragePanel(viewModel: TarotViewModel) {
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val allocatedSize by viewModel.swarmContainerAllocatedSize.collectAsStateWithLifecycle()
    val isMounted by viewModel.swarmContainerMounted.collectAsStateWithLifecycle()
    val nodeRole by viewModel.swarmNodeRole.collectAsStateWithLifecycle()
    val nodeActive by viewModel.swarmNodeActive.collectAsStateWithLifecycle()
    val peers by viewModel.swarmDiscoveredPeers.collectAsStateWithLifecycle()
    val logs by viewModel.swarmNodeLogs.collectAsStateWithLifecycle()
    val isAllocating by viewModel.isSwarmAllocating.collectAsStateWithLifecycle()
    val allocationProgress by viewModel.swarmAllocationProgress.collectAsStateWithLifecycle()

    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val isSpeedTesting by viewModel.isSpeedTesting.collectAsStateWithLifecycle()
    val leaderboard by viewModel.globalNodeLeaderboard.collectAsStateWithLifecycle()

    var passphraseInput by remember { mutableStateOf("") }
    var sizeSelectionGb by remember { mutableStateOf(16f) } // default 16GB
    var selectedShardIndex by remember { mutableStateOf(0) }
    var shardPayloadInput by remember { mutableStateOf("Netrunner Encrypted Payload Block") }
    var decryptedShardOutput by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // 1. PHYSICAL STORAGE ALLOCATION CONSOLE
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                border = BorderStroke(1.dp, currentSkin.primaryColor.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SECURE SPACE PROVISIONING & PHYSICAL ALLOCATION",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = currentSkin.primaryColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "This allocates a physical encrypted storage volume (container file) using instant space reservation. The file resides on your local device storage, operating on a torrent-like scheme for maximum autonomy and peer resilience.",
                        fontSize = 16.sp,
                        fontFamily = RajdhaniFontFamily,
                        color = Color.LightGray,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black, RoundedCornerShape(4.dp))
                            .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STATUS:",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            color = Color(0xFFB0B0C5)
                        )
                        Text(
                            text = if (allocatedSize > 0) {
                                "${String.format("%.2f", allocatedSize / (1024 * 1024 * 1024.0))} GB RESERVED"
                            } else {
                                "NO ACTIVE ALLOCATION"
                            },
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (allocatedSize > 0) NeonGreen else Color.Red
                        )
                    }

                    if (allocatedSize == 0L) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Passphrase Input
                        Text(
                            text = "CONTAINER MASTER PASSPHRASE (derived via PBKDF2/SHA-256)",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 15.sp,
                            color = Color(0xFFB0B0C5)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = passphraseInput,
                            onValueChange = { passphraseInput = it },
                            placeholder = { Text("Enter strong node security key", fontSize = 16.sp, color = Color.DarkGray) },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 16.sp, color = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Size Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "PROVISIONED CAPACITY:",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 15.sp,
                                color = Color(0xFFB0B0C5)
                            )
                            Text(
                                text = "${sizeSelectionGb.toInt()} GB",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = currentSkin.primaryColor
                            )
                        }
                        Slider(
                            value = sizeSelectionGb,
                            onValueChange = { sizeSelectionGb = it },
                            valueRange = 1f..32f,
                            steps = 31,
                            colors = SliderDefaults.colors(
                                thumbColor = currentSkin.primaryColor,
                                activeTrackColor = currentSkin.primaryColor,
                                inactiveTrackColor = Color.DarkGray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (isAllocating) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                LinearProgressIndicator(
                                    progress = { allocationProgress },
                                    color = currentSkin.primaryColor,
                                    trackColor = Color.DarkGray,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Reserving Disk Blocks... ${String.format("%.0f", allocationProgress * 100)}%",
                                    fontSize = 16.sp,
                                    fontFamily = RajdhaniFontFamily,
                                    color = Color.LightGray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        } else {
                            CyberButton(
                                onClick = {
                                    if (passphraseInput.isNotBlank()) {
                                        val bytes = sizeSelectionGb.toLong() * 1024 * 1024 * 1024L
                                        viewModel.allocateSwarmContainer(bytes, passphraseInput)
                                    } else {
                                        viewModel.postNotification("⚠️ Please enter a secure passphrase before generating container.")
                                    }
                                },
                                skin = currentSkin,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                AutoResizingText(
                                    text = "PRE-ALLOCATE & ENCRYPT CRYPTO-VOLUME",
                                    targetTextSize = 11.sp,
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        // Allocated state, show purge option
                        Spacer(modifier = Modifier.height(12.dp))
                        CyberButton(
                            onClick = { viewModel.deallocateSwarmContainer() },
                            skin = currentSkin,
                            containerColor = Color.Red,
                            contentColor = Color.White,
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            AutoResizingText(
                                text = "PURGE & SECURE-ERASE CONTAINER FROM DISK",
                                targetTextSize = 11.sp,
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // 2. CRYPTOGRAPHIC MOUNT & DECRYPTION VAULT
        if (allocatedSize > 0) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                    border = BorderStroke(1.dp, if (isMounted) NeonGreen.copy(alpha = 0.4f) else Color.DarkGray),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "SECURE VOLUME DECRYPTION & MOUNT ENGINE",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMounted) NeonGreen else Color.White
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black, RoundedCornerShape(4.dp))
                                .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(if (isMounted) NeonGreen else Color.Red, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "MOUNT STATUS:",
                                    fontFamily = RajdhaniFontFamily,
                                    fontSize = 16.sp,
                                    color = Color(0xFFB0B0C5)
                                )
                            }
                            Text(
                                text = if (isMounted) "RAM MOUNTED (UNLOCKED)" else "LOCKED (ENCRYPTED)",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isMounted) NeonGreen else Color.Red
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (!isMounted) {
                            Text(
                                text = "ENTER MASTER PASSPHRASE TO MOUNT VOLUME:",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 15.sp,
                                color = Color(0xFFB0B0C5)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = passphraseInput,
                                onValueChange = { passphraseInput = it },
                                placeholder = { Text("Verify passphrase", fontSize = 16.sp, color = Color.DarkGray) },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 16.sp, color = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            CyberButton(
                                onClick = {
                                    if (passphraseInput.isNotBlank()) {
                                        viewModel.mountSwarmContainer(passphraseInput)
                                        passphraseInput = ""
                                    } else {
                                        viewModel.postNotification("⚠️ Passphrase is required to unlock container.")
                                    }
                                },
                                skin = currentSkin,
                                containerColor = NeonGreen,
                                contentColor = CyberObsidian,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                AutoResizingText(
                                    text = "UNLOCK & MOUNT PARTITION",
                                    targetTextSize = 11.sp,
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            CyberButton(
                                onClick = { viewModel.unmountSwarmContainer() },
                                skin = currentSkin,
                                containerColor = Color.DarkGray,
                                contentColor = Color.White,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                AutoResizingText(
                                    text = "UNMOUNT & PURGE VOLATILE KEYS FROM RAM",
                                    targetTextSize = 11.sp,
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. SECURE BLOCK SHARD WRITER & AUDITOR
        if (isMounted) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                    border = BorderStroke(1.dp, currentSkin.secondaryColor.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "SECURE BLOCK SHARD TESTING MATRIX",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentSkin.primaryColor
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Shard Index Row Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SELECT VIRTUAL SHARD INDEX:",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 15.sp,
                                color = Color(0xFFB0B0C5)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                (0..4).forEach { idx ->
                                    val isSel = selectedShardIndex == idx
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (isSel) currentSkin.secondaryColor.copy(alpha = 0.2f) else Color.Transparent,
                                                RoundedCornerShape(4.dp)
                                            )
                                            .border(1.dp, if (isSel) currentSkin.primaryColor else Color.DarkGray, RoundedCornerShape(4.dp))
                                            .clickable { selectedShardIndex = idx }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "#$idx",
                                            fontSize = 16.sp,
                                            fontFamily = RajdhaniFontFamily,
                                            color = if (isSel) currentSkin.primaryColor else Color(0xFFB0B0C5)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "SHARD DATA CARGO:",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 15.sp,
                            color = Color(0xFFB0B0C5)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = shardPayloadInput,
                            onValueChange = { shardPayloadInput = it },
                            textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 16.sp, color = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CyberButton(
                                onClick = { viewModel.writeSwarmShardBlock(selectedShardIndex, shardPayloadInput) },
                                skin = currentSkin,
                                containerColor = currentSkin.secondaryColor,
                                contentColor = CyberObsidian,
                                modifier = Modifier.weight(1f).height(46.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                AutoResizingText(
                                    text = "WRITE SHARD",
                                    targetTextSize = 11.sp,
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                            CyberButton(
                                onClick = { 
                                    viewModel.readSwarmShardBlock(selectedShardIndex) { result ->
                                        decryptedShardOutput = result ?: "EMPTY BLOCK"
                                    }
                                },
                                skin = currentSkin,
                                containerColor = Color.Black,
                                contentColor = Color.White,
                                modifier = Modifier.weight(1f).height(46.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                AutoResizingText(
                                    text = "READ SHARD",
                                    targetTextSize = 11.sp,
                                    fontFamily = RajdhaniFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        decryptedShardOutput?.let { output ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black, RoundedCornerShape(4.dp))
                                    .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "DECRYPTED SHARD #$selectedShardIndex:",
                                        fontSize = 15.sp,
                                        fontFamily = RajdhaniFontFamily,
                                        color = Color(0xFFB0B0C5)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = output,
                                        fontSize = 16.sp,
                                        fontFamily = RajdhaniFontFamily,
                                        color = NeonGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3a. SUB-ZERO NODE STAKING & UPTIME MONITOR
        item {
            val userP = profile ?: UserProfile()
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                border = BorderStroke(1.dp, currentSkin.primaryColor.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SUB-ZERO NODE STAKING & UPTIME INSURANCE",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = currentSkin.primaryColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Operating a node is a critical network privilege. It requires locking (freezing) WEED tokens in a dedicated node escrow. Higher stake levels unlock advanced server roles and boost your global network priority ranking.",
                        fontSize = 16.sp,
                        fontFamily = RajdhaniFontFamily,
                        color = Color.LightGray,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Black),
                            border = BorderStroke(1.dp, Color.DarkGray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("WALLET BALANCE", fontSize = 15.sp, fontFamily = RajdhaniFontFamily, color = Color(0xFFB0B0C5))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${userP.weed} WEED", fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = RajdhaniFontFamily, color = Color.White)
                            }
                        }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Black),
                            border = BorderStroke(1.dp, currentSkin.primaryColor.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("STAKED NODE ESCROW", fontSize = 15.sp, fontFamily = RajdhaniFontFamily, color = currentSkin.primaryColor)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${userP.nodeStake} WEED", fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = RajdhaniFontFamily, color = NeonGreen)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF07070F), RoundedCornerShape(4.dp))
                            .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "PRIVILEGE ESCROW LEVELS:",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = RajdhaniFontFamily,
                            color = Color(0xFFB0B0C5)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("STORAGE: 100 WEED", fontSize = 16.sp, fontFamily = RajdhaniFontFamily, color = if (userP.nodeStake >= 100) NeonGreen else Color.LightGray)
                            Text("RELAY: 250 WEED", fontSize = 16.sp, fontFamily = RajdhaniFontFamily, color = if (userP.nodeStake >= 250) NeonGreen else Color.LightGray)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("VALIDATOR: 500 WEED", fontSize = 16.sp, fontFamily = RajdhaniFontFamily, color = if (userP.nodeStake >= 500) NeonGreen else Color.LightGray)
                            Text("ORACLE: 1000 WEED", fontSize = 16.sp, fontFamily = RajdhaniFontFamily, color = if (userP.nodeStake >= 1000) NeonGreen else Color.LightGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    var stakeInputText by remember { mutableStateOf("100") }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = stakeInputText,
                            onValueChange = { stakeInputText = it },
                            label = { Text("Weed Amount", fontSize = 16.sp, color = Color(0xFFB0B0C5)) },
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = RajdhaniFontFamily, fontSize = 16.sp, color = Color.White),
                            modifier = Modifier.weight(1f)
                        )
                        CyberButton(
                            onClick = {
                                val amt = stakeInputText.toIntOrNull() ?: 0
                                if (amt > 0) viewModel.stakeSwarmWeed(amt)
                            },
                            skin = currentSkin,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            AutoResizingText("STAKE", targetTextSize = 11.sp, fontFamily = RajdhaniFontFamily, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }
                        CyberButton(
                            onClick = {
                                val amt = stakeInputText.toIntOrNull() ?: 0
                                if (amt > 0) viewModel.unstakeSwarmWeed(amt)
                            },
                            skin = currentSkin,
                            containerColor = Color.Black,
                            contentColor = Color.White,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            AutoResizingText("UNSTAKE", targetTextSize = 11.sp, fontFamily = RajdhaniFontFamily, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black, RoundedCornerShape(4.dp))
                            .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("NODE LIVE UPTIME", fontSize = 15.sp, fontFamily = RajdhaniFontFamily, color = Color(0xFFB0B0C5))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("${userP.nodeUptimeMinutes} mins", fontSize = 17.sp, fontWeight = FontWeight.Bold, fontFamily = RajdhaniFontFamily, color = NeonGreen)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("OFFLINE PENALTY ENGINE", fontSize = 15.sp, fontFamily = RajdhaniFontFamily, color = Color.Red)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("50% Slashed if Offline >15m", fontSize = 16.sp, fontFamily = RajdhaniFontFamily, color = Color.LightGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    CyberButton(
                        onClick = { viewModel.simulateFifteenMinutesOffline() },
                        skin = currentSkin,
                        containerColor = Color(0xFF3A1010),
                        contentColor = Color.Red,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        AutoResizingText("SIMULATE 15-MINUTE OUTAGE (PENALTY SLASH TEST)", targetTextSize = 10.sp, fontFamily = RajdhaniFontFamily, color = Color.Red, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    }
                }
            }
        }

        // 3b. CHANNEL BANDWIDTH SPEED TESTER
        item {
            val userP = profile ?: UserProfile()
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                border = BorderStroke(1.dp, if (userP.nodeBandwidthMbps >= 3.0) NeonCyan.copy(alpha = 0.4f) else Color.Red.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DECENTRALIZED TRANSIT CHANNEL BANDWIDTH",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (userP.nodeBandwidthMbps >= 3.0) NeonCyan else Color.Red
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Nodes must maintain a minimum transit channel speed of 3.0 Mbps to assist global routing maps. Nodes falling below this threshold are temporarily isolated from public rating leaderboards, remaining reserved exclusively for private personal projects.",
                        fontSize = 16.sp,
                        fontFamily = RajdhaniFontFamily,
                        color = Color.LightGray,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black, RoundedCornerShape(4.dp))
                            .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("MEASURED TRANSIT SPEED", fontSize = 15.sp, fontFamily = RajdhaniFontFamily, color = Color(0xFFB0B0C5))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("${String.format("%.2f", userP.nodeBandwidthMbps)} Mbps", fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = RajdhaniFontFamily, color = if (userP.nodeBandwidthMbps >= 3.0) NeonGreen else Color.Red)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("ROUTING PROTOCOL STATE", fontSize = 15.sp, fontFamily = RajdhaniFontFamily, color = Color(0xFFB0B0C5))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (userP.nodeBandwidthMbps >= 3.0) "🟢 ELIGIBLE" else "⚠️ LOCAL ONLY",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = RajdhaniFontFamily,
                                color = if (userP.nodeBandwidthMbps >= 3.0) NeonGreen else Color.Yellow
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isSpeedTesting) {
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = NeonCyan, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Testing network transit speed... Please stand by.", fontSize = 16.sp, fontFamily = RajdhaniFontFamily, color = Color.LightGray)
                        }
                    } else {
                        CyberButton(
                            onClick = { viewModel.runBandwidthSpeedTest() },
                            skin = currentSkin,
                            containerColor = NeonCyan,
                            contentColor = CyberObsidian,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            AutoResizingText("RUN CHANNEL BANDWIDTH SPEED TEST", targetTextSize = 11.sp, fontFamily = RajdhaniFontFamily, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }

        // 4. ZERO-TRACKER DECENTRALIZED P2P NETWORK CORE
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                border = BorderStroke(1.dp, if (nodeActive) NeonCyan.copy(alpha = 0.4f) else Color.DarkGray),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DECENTRAL SWARM P2P NETWORK COORDINATOR",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (nodeActive) NeonCyan else Color.White
                        )
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(if (nodeActive) NeonCyan else Color.Red, CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Configures local discovery, routing, and transit protocols. Zero external tracking. Uses UDP local socket broadcasts to exchange network maps directly between devices.",
                        fontSize = 16.sp,
                        fontFamily = RajdhaniFontFamily,
                        color = Color.LightGray,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Node Role Selector
                    Text(
                        text = "SELECT SWARM OPERATIONAL MODE / ROLE LEVEL:",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 15.sp,
                        color = Color(0xFFB0B0C5)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    val roles = listOf("Storage", "Relay", "Validator", "Oracle")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        roles.forEach { r ->
                            val isSel = nodeRole == r
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSel) NeonCyan.copy(alpha = 0.15f) else Color.Transparent,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .border(1.dp, if (isSel) NeonCyan else Color.DarkGray, RoundedCornerShape(4.dp))
                                    .clickable { viewModel.setSwarmNodeRole(r) }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = r.uppercase(),
                                    fontSize = 16.sp,
                                    fontFamily = RajdhaniFontFamily,
                                    color = if (isSel) NeonCyan else Color(0xFFB0B0C5),
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Start/Stop Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black, RoundedCornerShape(4.dp))
                            .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SWARM NODE PROTOCOL:",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 15.sp,
                                color = Color(0xFFB0B0C5)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (nodeActive) "UDP BROADCASTER ONLINE" else "OFFLINE",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (nodeActive) NeonCyan else Color.Red
                            )
                        }
                        Switch(
                            checked = nodeActive,
                            onCheckedChange = { active ->
                                if (active) viewModel.startSwarmNode() else viewModel.stopSwarmNode()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonCyan,
                                checkedTrackColor = NeonCyan.copy(alpha = 0.3f),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.Black
                            )
                        )
                    }

                    // Discovered Swarm Peers List
                    if (nodeActive) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "DISCOVERED SWARM NEIGHBORS (PEERS ON SUBNET):",
                            fontFamily = RajdhaniFontFamily,
                            fontSize = 15.sp,
                            color = Color(0xFFB0B0C5)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        if (peers.isEmpty()) {
                            Text(
                                text = "Scanning local subnet... UDP Port 9944 broadcast active. Ensure other nodes are running on the network.",
                                fontSize = 16.sp,
                                fontFamily = RajdhaniFontFamily,
                                color = Color(0xFFB0B0C5),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                peers.forEach { peer ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF0D0D15), RoundedCornerShape(4.dp))
                                            .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${peer.id} (${peer.ip})",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = RajdhaniFontFamily,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Role: ${peer.role} | Cap: ${peer.allocatedGb} GB",
                                                fontSize = 15.sp,
                                                fontFamily = RajdhaniFontFamily,
                                                color = Color(0xFFB0B0C5)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            CyberButton(
                                                onClick = {
                                                    viewModel.fetchSwarmShardBlockFromPeer(peer.ip, selectedShardIndex) { result ->
                                                        decryptedShardOutput = result ?: "EMPTY CHUNK"
                                                    }
                                                },
                                                skin = currentSkin,
                                                containerColor = Color.Black,
                                                contentColor = currentSkin.primaryColor,
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                AutoResizingText(
                                                    text = "STREAM SHARD #$selectedShardIndex",
                                                    targetTextSize = 9.sp,
                                                    fontFamily = RajdhaniFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color.Black),
                                            border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.3f))
                                        ) {
                                            Text(
                                                text = peer.status,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = RajdhaniFontFamily,
                                                color = NeonGreen,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Console logs
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "NODE REAL-TIME SIGNAL STREAM:",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 15.sp,
                        color = Color(0xFFB0B0C5)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(Color.Black, RoundedCornerShape(4.dp))
                            .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                            .padding(10.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(logs) { log ->
                                Text(
                                    text = log,
                                    fontSize = 15.sp,
                                    fontFamily = RajdhaniFontFamily,
                                    color = if (log.contains("Critical") || log.contains("Error")) Color.Red else if (log.contains("📡") || log.contains("SWARM")) NeonCyan else Color.LightGray,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4a. GLOBAL DECENTRALIZED NODE LEADERBOARD
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCharcoal),
                border = BorderStroke(1.dp, Color.DarkGray),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "GLOBAL DECENTRALIZED CLUSTER RANKINGS",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Real-time ranking of active nodes on the Sub-Zero swarm. Rank index is determined primarily by locked WEED escrow stake, rewarding high-fidelity uptime and commitment to the collective storage grid.",
                        fontSize = 16.sp,
                        fontFamily = RajdhaniFontFamily,
                        color = Color.LightGray,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        leaderboard.forEachIndexed { idx, rankNode ->
                            val isUser = rankNode.isUser
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isUser) NeonCyan.copy(alpha = 0.08f) else Color(0xFF0A0A0F),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isUser) NeonCyan else Color.DarkGray,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "#${idx + 1}",
                                        fontSize = 16.sp,
                                        fontFamily = RajdhaniFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUser) NeonCyan else Color(0xFFB0B0C5),
                                        modifier = Modifier.width(32.dp)
                                    )
                                    Column {
                                        Text(
                                            text = rankNode.name,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = RajdhaniFontFamily,
                                            color = if (isUser) NeonCyan else Color.White
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Role: ${rankNode.role} | Bandwidth: ${String.format("%.1f", rankNode.speedMbps)} Mbps",
                                            fontSize = 15.sp,
                                            fontFamily = RajdhaniFontFamily,
                                            color = Color(0xFFB0B0C5)
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${rankNode.stake} STAKED",
                                        fontSize = 16.sp,
                                        fontFamily = RajdhaniFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonGreen
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = rankNode.status,
                                        fontSize = 15.sp,
                                        fontFamily = RajdhaniFontFamily,
                                        color = if (rankNode.status == "ONLINE") NeonGreen else if (rankNode.status == "LOW_SPEED") Color.Yellow else Color.Red
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

@Composable
fun AutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontFamily: FontFamily? = null,
    fontWeight: FontWeight? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = 1,
    initialFontSize: TextUnit = 72.sp,
    minFontSize: TextUnit = 12.sp
) {
    var fontSize by remember(text) { mutableStateOf(initialFontSize) }
    var readyToDraw by remember(text) { mutableStateOf(false) }

    Text(
        text = text,
        modifier = modifier.graphicsLayer { alpha = if (readyToDraw) 1f else 0f },
        color = color,
        fontSize = fontSize,
        fontFamily = fontFamily,
        fontWeight = fontWeight,
        letterSpacing = letterSpacing,
        textAlign = textAlign,
        maxLines = maxLines,
        softWrap = false,
        style = getHighContrastTextStyle(color),
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.hasVisualOverflow) {
                val currentSizeSp = fontSize.value
                if (currentSizeSp > minFontSize.value) {
                    fontSize = (currentSizeSp - 2).sp
                } else {
                    readyToDraw = true
                }
            } else {
                readyToDraw = true
            }
        }
    )
}

@Composable
fun MarijuanaLeaf(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2
        val cy = h / 2
        
        // 7 lobes radiating from bottom center (cx, cy + h * 0.3)
        val rx = cx
        val ry = cy + h * 0.25f
        
        val angles = listOf(-90f, -120f, -150f, -175f, -60f, -30f, -5f)
        val lengths = listOf(h * 0.45f, h * 0.40f, h * 0.30f, h * 0.20f, h * 0.40f, h * 0.30f, h * 0.20f)
        val widths = listOf(w * 0.08f, w * 0.075f, w * 0.065f, w * 0.055f, w * 0.075f, w * 0.065f, w * 0.055f)
        
        angles.forEachIndexed { i, angleDegrees ->
            val angleRad = Math.toRadians(angleDegrees.toDouble())
            val len = lengths[i]
            val width = widths[i]
            
            val ax = Math.cos(angleRad).toFloat()
            val ay = Math.sin(angleRad).toFloat()
            val px = -ay
            val py = ax
            
            val path = Path().apply {
                moveTo(rx, ry)
                val midX = rx + ax * len * 0.5f
                val midY = ry + ay * len * 0.5f
                val tipX = rx + ax * len
                val tipY = ry + ay * len
                
                cubicTo(
                    rx + ax * len * 0.22f + px * width, ry + ay * len * 0.22f + py * width,
                    midX + px * width * 1.25f, midY + py * width * 1.25f,
                    tipX, tipY
                )
                cubicTo(
                    midX - px * width * 1.25f, midY - py * width * 1.25f,
                    rx + ax * len * 0.22f - px * width, ry + ay * len * 0.22f - py * width,
                    rx, ry
                )
                close()
            }
            drawPath(path, color = tint)
        }
        
        // Stem
        drawLine(
            color = tint,
            start = Offset(rx, ry),
            end = Offset(rx, ry + h * 0.15f),
            strokeWidth = (w * 0.03f).coerceAtLeast(3f)
        )
    }
}

@Composable
fun VanGoghGanjaBackground(
    skin: CyberSkin,
    modifier: Modifier = Modifier,
    overrideBgResId: Int? = null
) {
    val bgRes = overrideBgResId ?: skin.backgroundResId
    Box(modifier = modifier) {
        // Draw the authentic, colorful, and glowing cyberpunk-fantasy illustration background
        Image(
            painter = painterResource(id = bgRes),
            contentDescription = "Cyber Fantasy ${skin.displayName} background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.88f
        )
        
        // Saturated, colorful gradient mask that blends skin colors directly into the background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            skin.primaryColor.copy(alpha = 0.15f),
                            skin.secondaryColor.copy(alpha = 0.22f),
                            Color(0xFF020204).copy(alpha = 0.48f)
                        )
                    )
                )
        )
        
        // Interactive Canvas drawing Van Gogh's Starry orbs and wind-swirl currents in skin tones
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            
            // Swirl 1: Upper right starry spiral
            val swirlColor = skin.primaryColor.copy(alpha = 0.28f)
            val accentColor = skin.ambientGlowColor.copy(alpha = 0.38f)
        
            val swirlPath1 = Path().apply {
                val cx = w * 0.75f
                val cy = h * 0.22f
                moveTo(cx, cy)
                for (angle in 0..1080 step 12) {
                    val rad = Math.toRadians(angle.toDouble())
                    val r = (angle / 1080f) * (w * 0.38f)
                    val x = cx + Math.cos(rad).toFloat() * r
                    val y = cy + Math.sin(rad).toFloat() * r
                    if (angle == 0) moveTo(x, y) else lineTo(x, y)
                }
            }
            drawPath(
                path = swirlPath1,
                color = swirlColor,
                style = Stroke(
                    width = 5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                )
            )
            
            // Swirl 2: Lower left starry spiral
            val swirlPath2 = Path().apply {
                val cx = w * 0.25f
                val cy = h * 0.78f
                moveTo(cx, cy)
                for (angle in 0..720 step 15) {
                    val rad = Math.toRadians(-angle.toDouble())
                    val r = (angle / 720f) * (w * 0.28f)
                    val x = cx + Math.cos(rad).toFloat() * r
                    val y = cy + Math.sin(rad).toFloat() * r
                    if (angle == 0) moveTo(x, y) else lineTo(x, y)
                }
            }
            drawPath(
                path = swirlPath2,
                color = accentColor,
                style = Stroke(
                    width = 4.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            )
            
            // Star halos (Van Gogh style glowing orbs)
            val starCenters = listOf(
                Offset(w * 0.2f, h * 0.15f),
                Offset(w * 0.8f, h * 0.35f),
                Offset(w * 0.48f, h * 0.52f),
                Offset(w * 0.85f, h * 0.75f),
                Offset(w * 0.15f, h * 0.85f)
            )
            
            starCenters.forEachIndexed { idx, center ->
                val rBase = 11.dp.toPx() + (idx * 3).dp.toPx()
                drawCircle(
                    color = skin.primaryColor.copy(alpha = 0.12f),
                    radius = rBase * 2.2f,
                    center = center,
                    style = Stroke(width = 3.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 12f), 0f))
                )
                drawCircle(
                    color = Color(0xFFFFDD44).copy(alpha = 0.25f),
                    radius = rBase * 1.5f,
                    center = center,
                    style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f), 0f))
                )
                drawCircle(
                    color = Color(0xFFFFEE88),
                    radius = rBase * 0.6f,
                    center = center
                )
            }
        }
        
        // Swirling marijuana leaves (Qualitatively generated Raster Illustrations)
        val leafLeaves = listOf(
            Triple(Offset(0.28f, 0.32f), -40f, 0.65f),
            Triple(Offset(0.72f, 0.58f), 25f, 0.85f),
            Triple(Offset(0.16f, 0.48f), 105f, 0.55f),
            Triple(Offset(0.84f, 0.16f), -130f, 0.75f),
            Triple(Offset(0.52f, 0.78f), -10f, 0.95f)
        )
        
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val wDp = maxWidth
            val hDp = maxHeight
            leafLeaves.forEach { (pos, baseAngle, scale) ->
                Image(
                    painter = painterResource(id = R.drawable.img_marijuana_leaf_van_gogh_1784603006276),
                    contentDescription = "Swirling Ganja Leaf",
                    modifier = Modifier
                        .offset(x = wDp * pos.x - (36.dp * scale), y = hDp * pos.y - (36.dp * scale))
                        .size(72.dp * scale)
                        .rotate(baseAngle),
                    alpha = 0.65f
                )
            }
        }
    }
}

@Composable
fun BlessingQuestsHubDialog(
    viewModel: TarotViewModel,
    onDismiss: () -> Unit
) {
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val lang = profile?.systemLanguage ?: "en"

    // Quest states
    val completedScan by viewModel.questCompletedScan.collectAsStateWithLifecycle()
    val claimedScan by viewModel.questClaimedScan.collectAsStateWithLifecycle()
    val completedProxy by viewModel.questCompletedProxy.collectAsStateWithLifecycle()
    val claimedProxy by viewModel.questClaimedProxy.collectAsStateWithLifecycle()
    val completedTip by viewModel.questCompletedTip.collectAsStateWithLifecycle()
    val claimedTip by viewModel.questClaimedTip.collectAsStateWithLifecycle()

    // Buff states
    val uptimeBuff by viewModel.blessingUptimeBuff.collectAsStateWithLifecycle()
    val rareCardBuff by viewModel.blessingRareCardBuff.collectAsStateWithLifecycle()
    val isVipUnlocked by viewModel.isVipUnlocked.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0: Quests, 1: Blessings & Tips

    // Micro payment state
    var showCheckoutSheet by remember { mutableStateOf<Map<String, Any>?>(null) } // tier info if open

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberObsidian),
            border = BorderStroke(2.dp, currentSkin.primaryColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (lang == "ru") "МАТРИЦА УСЛУГ И КВЕСТОВ" else "MATRIX SERVICES & QUESTS",
                        fontSize = 16.sp,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold,
                        fontFamily = RajdhaniFontFamily
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                // Balance Info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ElegantIndigo.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (lang == "ru") "Текущий баланс:" else "Current Balance:",
                        fontSize = 16.sp,
                        color = Color.LightGray,
                        fontFamily = RajdhaniFontFamily
                    )
                    Text(
                        text = "${profile?.weed ?: 0} WEED",
                        fontSize = 14.sp,
                        color = NeonGreen,
                        fontWeight = FontWeight.Bold,
                        fontFamily = RajdhaniFontFamily
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tabs Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CyberButton(
                        onClick = { activeTab = 0 },
                        skin = currentSkin,
                        containerColor = if (activeTab == 0) currentSkin.primaryColor else ElegantIndigo,
                        contentColor = if (activeTab == 0) CyberObsidian else Color.White,
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Text(
                            text = if (lang == "ru") "Daily Квесты" else "Daily Quests",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = RajdhaniFontFamily
                        )
                    }
                    CyberButton(
                        onClick = { activeTab = 1 },
                        skin = currentSkin,
                        containerColor = if (activeTab == 1) currentSkin.primaryColor else ElegantIndigo,
                        contentColor = if (activeTab == 1) CyberObsidian else Color.White,
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Text(
                            text = if (lang == "ru") "Дары и Бусты" else "Blessings & Tips",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = RajdhaniFontFamily
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tab Content
                Box(modifier = Modifier.weight(1f)) {
                    if (activeTab == 0) {
                        // Quests list
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Text(
                                    text = if (lang == "ru") "Выполняйте задания для получения свободных кредитов:" else "Complete cyber tasks to harvest free WEED credits:",
                                    fontSize = 16.sp,
                                    color = Color(0xFFB0B0C5),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            // Quest 1
                            item {
                                QuestItemRow(
                                    title = if (lang == "ru") "Провести сканирование матрицы" else "Run Divination Scan",
                                    desc = if (lang == "ru") "Сделать любой расклад карт у Оракула" else "Analyze any layout in the Oracle terminal",
                                    reward = 100,
                                    isCompleted = completedScan,
                                    isClaimed = claimedScan,
                                    lang = lang,
                                    onClaim = { viewModel.claimQuestReward("scan", 100) }
                                )
                            }
                            // Quest 2
                            item {
                                QuestItemRow(
                                    title = if (lang == "ru") "Активировать защитный прокси" else "Secure Proxy Routing",
                                    desc = if (lang == "ru") "Включить VPN/Tor шлюз в настройках NetOps" else "Enable and activate any proxy tunnel in settings",
                                    reward = 100,
                                    isCompleted = completedProxy,
                                    isClaimed = claimedProxy,
                                    lang = lang,
                                    onClaim = { viewModel.claimQuestReward("proxy", 100) }
                                )
                            }
                            // Quest 3
                            item {
                                QuestItemRow(
                                    title = if (lang == "ru") "Благословение Оракула" else "Make an Offering to Oracle",
                                    desc = if (lang == "ru") "Принести любой дар или сделать взнос" else "Tip the Oracle or activate any luck blessing",
                                    reward = 100,
                                    isCompleted = completedTip,
                                    isClaimed = claimedTip,
                                    lang = lang,
                                    onClaim = { viewModel.claimQuestReward("tip", 100) }
                                )
                            }

                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                CyberButton(
                                    onClick = { viewModel.resetDailyQuests() },
                                    skin = currentSkin,
                                    containerColor = CyberMutedBlue,
                                    modifier = Modifier.fillMaxWidth().height(44.dp)
                                ) {
                                    Text(
                                        text = if (lang == "ru") "🔄 СБРОСИТЬ КВЕСТЫ (ДЕБАГ)" else "🔄 RESET QUESTS (DEBUG)",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = RajdhaniFontFamily,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    } else {
                        // Blessings & Tips list
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Text(
                                    text = if (lang == "ru") "ПОЖЕРТВОВАНИЯ КРЕДИТОВ (ВЫГОДНЫЕ БУСТЫ)" else "CREDIT BLESSINGS (IN-GAME BUFFS)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonMagenta,
                                    fontFamily = RajdhaniFontFamily
                                )
                            }

                            // Blessing 1
                            item {
                                BlessingItemRow(
                                    title = if (lang == "ru") "Накопитель Гармонии" else "Harmony Accumulator",
                                    desc = if (lang == "ru") "Доход от майнинга узлов удваивается" else "Doubles passive credit rewards from background node running",
                                    costText = "50 WEED",
                                    isActive = uptimeBuff,
                                    lang = lang,
                                    onBuy = { viewModel.purchaseWeedBlessing("uptime", 50) }
                                )
                            }

                            // Blessing 2
                            item {
                                BlessingItemRow(
                                    title = if (lang == "ru") "Катализатор Трихом" else "Trichome Catalyst",
                                    desc = if (lang == "ru") "Повышает шанс дропа Legend/Epic карт при минте NFT" else "Substantially boosts rare card minting rate on Sepolia",
                                    costText = "150 WEED",
                                    isActive = rareCardBuff,
                                    lang = lang,
                                    onBuy = { viewModel.purchaseWeedBlessing("rare_card", 150) }
                                )
                            }

                            // Blessing 3
                            item {
                                BlessingItemRow(
                                    title = if (lang == "ru") "Ключ EVM Синтеза" else "EVM Synthesis Key",
                                    desc = if (lang == "ru") "Разблокирует Premium 5-card и 10-card расклады навсегда" else "Permanently unlocks 5-card (Romantic Sync) and 10-card (Celtic Cross) layouts",
                                    costText = "200 WEED",
                                    isActive = isVipUnlocked,
                                    lang = lang,
                                    onBuy = { viewModel.purchaseWeedBlessing("vip", 200) }
                                )
                            }

                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                androidx.compose.material3.HorizontalDivider(color = Color(0xFFB0B0C5).copy(alpha = 0.2f))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (lang == "ru") "МИКРОТРАНЗАКЦИИ СВЯЗИ (МАССОВАЯ МОНЕТИЗАЦИЯ)" else "MATRIX GATEWAY MICRO-DONATIONS (MONETIZATION)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan,
                                    fontFamily = RajdhaniFontFamily
                                )
                                Text(
                                    text = if (lang == "ru") "Реалистичная симуляция платежного шлюза. Дешево, доступно, надежно." else "Simulated secure credit micro-payments. Ultra cheap, mass adoption model.",
                                    fontSize = 16.sp,
                                    color = Color(0xFFB0B0C5),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            // Micro donation Tiers
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    MicroPayCard(
                                        title = if (lang == "ru") "Микро-Искра" else "Spark Matrix",
                                        price = "$0.10",
                                        reward = "+1,000 WEED",
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            showCheckoutSheet = mapOf(
                                                "tier" to "spark",
                                                "title" to (if (lang == "ru") "Пакет 'Микро-Искра'" else "Spark Matrix Pack"),
                                                "price" to 0.10,
                                                "reward" to 1000
                                            )
                                        }
                                    )
                                    MicroPayCard(
                                        title = if (lang == "ru") "Космо-Канал" else "Cosmic Sync",
                                        price = "$0.25",
                                        reward = "+5,000 WEED\n+VIP СТАТУС",
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            showCheckoutSheet = mapOf(
                                                "tier" to "cosmic",
                                                "title" to (if (lang == "ru") "Пакет 'Космо-Канал'" else "Cosmic Sync Pack"),
                                                "price" to 0.25,
                                                "reward" to 5000
                                            )
                                        }
                                    )
                                    MicroPayCard(
                                        title = if (lang == "ru") "Архитектор" else "Architect",
                                        price = "$0.50",
                                        reward = "+25,000 WEED\n+VIP +GOD SKIN",
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            showCheckoutSheet = mapOf(
                                                "tier" to "architect",
                                                "title" to (if (lang == "ru") "Пакет 'Архитектор'" else "Architect Key"),
                                                "price" to 0.50,
                                                "reward" to 25000
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Checkout Sheet Simulator Overlay
    showCheckoutSheet?.let { info ->
        SimulatedCheckoutSheet(
            title = info["title"] as String,
            priceUsd = info["price"] as Double,
            onSuccess = {
                viewModel.simulateMicroPayment(
                    tier = info["tier"] as String,
                    priceUsd = info["price"] as Double,
                    creditReward = info["reward"] as Int
                )
                showCheckoutSheet = null
            },
            onDismiss = { showCheckoutSheet = null },
            lang = lang,
            currentSkin = currentSkin
        )
    }
}

@Composable
fun QuestItemRow(
    title: String,
    desc: String,
    reward: Int,
    isCompleted: Boolean,
    isClaimed: Boolean,
    lang: String,
    onClaim: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF14121E), RoundedCornerShape(12.dp))
            .border(1.dp, if (isCompleted && !isClaimed) NeonCyan.copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isCompleted) NeonGreen else Color(0xFFB0B0C5),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = RajdhaniFontFamily
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = desc,
                fontSize = 15.sp,
                color = Color.LightGray,
                fontFamily = RajdhaniFontFamily
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "🎁 +$reward WEED",
                fontSize = 16.sp,
                color = NeonGreen,
                fontWeight = FontWeight.Bold,
                fontFamily = RajdhaniFontFamily
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (isClaimed) {
            Text(
                text = if (lang == "ru") "ПОЛУЧЕНО" else "CLAIMED",
                fontSize = 16.sp,
                color = Color(0xFFB0B0C5),
                fontWeight = FontWeight.Bold,
                fontFamily = RajdhaniFontFamily,
                modifier = Modifier
                    .background(Color(0x10FFFFFF), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )
        } else if (isCompleted) {
            Button(
                onClick = onClaim,
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text(
                    text = if (lang == "ru") "ЗАБРАТЬ" else "CLAIM",
                    fontSize = 16.sp,
                    color = CyberObsidian,
                    fontWeight = FontWeight.Bold,
                    fontFamily = RajdhaniFontFamily
                )
            }
        } else {
            Text(
                text = if (lang == "ru") "В ПРОЦЕССЕ" else "IN PROGRESS",
                fontSize = 15.sp,
                color = Color(0xFFB0B0C5),
                fontFamily = RajdhaniFontFamily,
                modifier = Modifier
                    .background(Color(0x05FFFFFF), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun BlessingItemRow(
    title: String,
    desc: String,
    costText: String,
    isActive: Boolean,
    lang: String,
    onBuy: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF14121E), RoundedCornerShape(12.dp))
            .border(1.dp, if (isActive) NeonMagenta.copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) NeonMagenta else Color.White,
                fontFamily = RajdhaniFontFamily
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = desc,
                fontSize = 15.sp,
                color = Color.LightGray,
                fontFamily = RajdhaniFontFamily
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        if (isActive) {
            Text(
                text = if (lang == "ru") "АКТИВНО" else "ACTIVE",
                fontSize = 16.sp,
                color = NeonMagenta,
                fontWeight = FontWeight.Bold,
                fontFamily = RajdhaniFontFamily,
                modifier = Modifier
                    .background(NeonMagenta.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )
        } else {
            Button(
                onClick = onBuy,
                colors = ButtonDefaults.buttonColors(containerColor = ElegantIndigo),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text(
                    text = costText,
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontFamily = RajdhaniFontFamily
                )
            }
        }
    }
}

@Composable
fun MicroPayCard(
    title: String,
    price: String,
    reward: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(115.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF14121E)),
        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.LightGray,
                fontFamily = RajdhaniFontFamily,
                textAlign = TextAlign.Center
            )
            Text(
                text = reward,
                fontSize = 15.sp,
                color = NeonGreen,
                fontWeight = FontWeight.SemiBold,
                fontFamily = RajdhaniFontFamily,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp
            )
            Text(
                text = price,
                fontSize = 17.sp,
                color = CyberObsidian,
                fontWeight = FontWeight.Black,
                fontFamily = RajdhaniFontFamily,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NeonCyan, RoundedCornerShape(6.dp))
                    .padding(vertical = 4.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SimulatedCheckoutSheet(
    title: String,
    priceUsd: Double,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit,
    lang: String,
    currentSkin: CyberSkin
) {
    var step by remember { mutableStateOf(0) } // 0: Payment Method, 1: Processing, 2: Done

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0A15)),
            border = BorderStroke(2.5.dp, NeonCyan),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (step == 0) {
                    Text(
                        text = if (lang == "ru") "ЗАЩИЩЕННЫЙ ПЛАТЕЖ" else "SECURE GATEWAY CHECKOUT",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        fontFamily = RajdhaniFontFamily,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    androidx.compose.material3.HorizontalDivider(color = Color(0xFFB0B0C5).copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontFamily = RajdhaniFontFamily
                    )
                    Text(
                        text = "\$${String.format("%.2f", priceUsd)} USD",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonGreen,
                        fontFamily = RajdhaniFontFamily,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Simulated Card selection
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ElegantIndigo.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = "Card",
                                tint = NeonCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "VISA •••• 4012",
                                fontSize = 14.sp,
                                color = Color.White,
                                fontFamily = RajdhaniFontFamily
                            )
                        }
                        Text(
                            text = "GPAY",
                            fontSize = 16.sp,
                            color = CyberObsidian,
                            fontWeight = FontWeight.Bold,
                            fontFamily = RajdhaniFontFamily,
                            modifier = Modifier
                                .background(Color.White, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CyberButton(
                            onClick = onDismiss,
                            skin = currentSkin,
                            containerColor = Color.Gray.copy(alpha = 0.2f),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Text(
                                text = if (lang == "ru") "ОТМЕНА" else "CANCEL",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = RajdhaniFontFamily
                            )
                        }
                        CyberButton(
                            onClick = {
                                step = 1
                            },
                            skin = currentSkin,
                            containerColor = NeonGreen,
                            contentColor = CyberObsidian,
                            modifier = Modifier.weight(1.5f).height(44.dp)
                        ) {
                            Text(
                                text = if (lang == "ru") "ПОДТВЕРДИТЬ И ОПЛАТИТЬ" else "CONFIRM & PAY",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = RajdhaniFontFamily
                            )
                        }
                    }
                } else if (step == 1) {
                    // Processing animation
                    Text(
                        text = if (lang == "ru") "АВТОРИЗАЦИЯ ТРАНЗАКЦИИ..." else "AUTHORIZING ENCRYPTED TUNNEL...",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        fontFamily = RajdhaniFontFamily
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    androidx.compose.material3.CircularProgressIndicator(
                        color = NeonCyan,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = if (lang == "ru") "Инициализация подписи контракта..." else "Securing financial quantum node consensus...",
                        fontSize = 16.sp,
                        color = Color(0xFFB0B0C5),
                        fontFamily = RajdhaniFontFamily
                    )
                    
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(2500)
                        step = 2
                    }
                } else {
                    // Success step
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = NeonGreen,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (lang == "ru") "ОПЛАТА УСПЕШНО ЗАВЕРШЕНА" else "TRANSACTION VERIFIED",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen,
                        fontFamily = RajdhaniFontFamily
                    )
                    Text(
                        text = if (lang == "ru") "Ваш баланс был обновлен!" else "Your cybernetic ledger has been synchronized!",
                        fontSize = 16.sp,
                        color = Color.LightGray,
                        fontFamily = RajdhaniFontFamily,
                        modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                    )
                    CyberButton(
                        onClick = {
                            onSuccess()
                        },
                        skin = currentSkin,
                        containerColor = NeonCyan,
                        contentColor = CyberObsidian,
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text(
                            text = if (lang == "ru") "ОТЛИЧНО" else "ACKNOWLEDGE",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = RajdhaniFontFamily
                        )
                    }
                }
            }
        }
    }
}




