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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

@Composable
internal fun SecureScreenWindowEffect(enabled: Boolean) {
    val context = LocalContext.current
    DisposableEffect(enabled, context) {
        val window = (context as? android.app.Activity)?.window
        if (enabled && window != null) {
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        }
        onDispose {
            if (enabled && window != null) {
                window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }
}

internal fun copySensitiveTextWithAutoClear(
    context: android.content.Context,
    scope: kotlinx.coroutines.CoroutineScope,
    label: String,
    secretText: String,
    ttlMs: Long = 45_000L
) {
    if (secretText.isBlank()) return
    val cm = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
        ?: return
    val clip = android.content.ClipData.newPlainText(label, secretText).apply {
        description.extras = android.os.PersistableBundle().apply {
            putBoolean("android.content.extra.IS_SENSITIVE", true)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                putBoolean(android.content.ClipDescription.EXTRA_IS_SENSITIVE, true)
            }
        }
    }
    cm.setPrimaryClip(clip)
    scope.launch {
        delay(ttlMs)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            runCatching { cm.clearPrimaryClip() }
        } else {
            runCatching { cm.setPrimaryClip(android.content.ClipData.newPlainText("", "")) }
        }
    }
}

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

// CryptoHubScreen extracted to CryptoHubScreen.kt
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
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
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
                                            imageVector = if (isSpeaking) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
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

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(184.dp)
    ) {
        WeedGuruTarotCard(
            card = card,
            isUpright = isUpright,
            isFlipped = !isRevealed,
            compact = false,
            systemLanguage = lang,
            showOrientationBadge = true,
            showRarityShader = true,
            backPromptLabel = if (lang == "ru") "[НАЖМИТЕ ДЛЯ ОТКРЫТИЯ]" else "[TAP TO REVEAL]",
            onCardClick = {
                if (!isRevealed) {
                    isRevealed = true
                    com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.CARD_DRAW)
                } else {
                    showFullScreenReview = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reading_tarot_card_${card.index}")
        )

        Spacer(modifier = Modifier.height(10.dp))

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
                            contentDescription = "Mint $localizedName",
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
                isUpright = isUpright,
                showOrientation = true,
                onClose = { showFullScreenReview = false }
            )
        }
    }
}

// VaultScreen extracted to VaultAndMarketScreens.kt

// MarketScreen extracted to VaultAndMarketScreens.kt

// Deck helpers, DeckScreen, CardSynthesisView, and SynthesisRecipeCard extracted to DeckAndSynthesisScreens.kt

// CustomBigKeypad, PinLockScreen, and SafeBotanicalModeScreen extracted to PinLockAndBotanicalScreens.kt

// OnboardingWizard extracted to OnboardingWizard.kt



// SecurityScreen extracted to SecurityScreen.kt

// NetOpsScreen, GrowerLunarCalendarPanel, BridgeTunnelPanel, and SecureCommsPanel extracted to NetOpsAndCommsScreens.kt



// TokenAsset and P2pLedgerPanel extracted to NetOpsBlockchainPanels.kt

// OnChainNftPanel extracted to NetOpsBlockchainPanels.kt

// WeedProtocolPanel extracted to NetOpsBlockchainPanels.kt

// NeuralAdsPanel extracted to NetOpsBlockchainPanels.kt

// FullScreenCardReview, radial analytics charts, and CyberAdBanner extracted to AnalyticsAndCardReview.kt

// SwarmStoragePanel extracted to SwarmStoragePanel.kt



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

// BlessingQuestsHubDialog and checkout helpers extracted to QuestsAndCheckoutSheets.kt




