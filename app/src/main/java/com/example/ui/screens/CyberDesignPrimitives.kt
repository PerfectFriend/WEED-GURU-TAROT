package com.example.ui.screens

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import android.view.WindowManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun SecureScreenWindowEffect(enabled: Boolean) {
    val context = LocalContext.current
    DisposableEffect(enabled, context) {
        val window = (context as? android.app.Activity)?.window
        if (enabled && window != null) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
        onDispose {
            if (enabled && window != null) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }
}

internal fun copySensitiveTextWithAutoClear(
    context: Context,
    scope: CoroutineScope,
    label: String,
    secretText: String,
    ttlMs: Long = 45_000L
) {
    if (secretText.isBlank()) return
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        ?: return
    val clip = ClipData.newPlainText(label, secretText).apply {
        description.extras = PersistableBundle().apply {
            putBoolean("android.content.extra.IS_SENSITIVE", true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
            }
        }
    }
    cm.setPrimaryClip(clip)
    scope.launch {
        delay(ttlMs)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            runCatching { cm.clearPrimaryClip() }
        } else {
            runCatching { cm.setPrimaryClip(ClipData.newPlainText("", "")) }
        }
    }
}

fun Modifier.circuitBackground(color: Color = EmeraldGlow.copy(alpha = 0.08f)) = drawBehind {
    val width = size.width
    val height = size.height
    val path = Path().apply {
        moveTo(width * 0.5f, height)
        quadraticTo(width * 0.45f, height * 0.5f, width * 0.65f, 0f)

        moveTo(width * 0.48f, height * 0.6f)
        quadraticTo(width * 0.25f, height * 0.45f, width * 0.10f, height * 0.35f)

        moveTo(width * 0.52f, height * 0.5f)
        quadraticTo(width * 0.75f, height * 0.40f, width * 0.90f, height * 0.30f)

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
            drawLine(color, Offset(0f, 0f), Offset(0f, size.height), strokeWidth)
            drawLine(color, Offset(0f, 0f), Offset(length, 0f), strokeWidth)
            drawLine(color, Offset(0f, size.height), Offset(length, size.height), strokeWidth)

            drawLine(color, Offset(size.width, 0f), Offset(size.width, size.height), strokeWidth)
            drawLine(color, Offset(size.width, 0f), Offset(size.width - length, 0f), strokeWidth)
            drawLine(color, Offset(size.width, size.height), Offset(size.width - length, size.height), strokeWidth)
        }
        CyberSkin.MOROCCO -> {
            val dotRadius = 3.dp.toPx()
            drawCircle(color, dotRadius, Offset(0f, 0f))
            drawCircle(color, dotRadius, Offset(size.width, 0f))
            drawCircle(color, dotRadius, Offset(0f, size.height))
            drawCircle(color, dotRadius, Offset(size.width, size.height))

            drawLine(secColor, Offset(length, 0f), Offset(size.width - length, 0f), strokeWidth / 2f)
            drawLine(secColor, Offset(length, size.height), Offset(size.width - length, size.height), strokeWidth / 2f)
        }
        CyberSkin.BARCELONA -> {
            drawLine(color, Offset(0f, 0f), Offset(length, 0f), strokeWidth)
            drawLine(color, Offset(0f, 0f), Offset(0f, length), strokeWidth)

            drawLine(secColor, Offset(size.width, size.height), Offset(size.width - length, size.height), strokeWidth)
            drawLine(secColor, Offset(size.width, size.height), Offset(size.width, size.height - length), strokeWidth)

            drawCircle(color, radius = 2.dp.toPx(), center = Offset(length, length))
            drawCircle(secColor, radius = 2.dp.toPx(), center = Offset(size.width - length, size.height - length))
        }
        CyberSkin.AMSTERDAM, CyberSkin.GAUDI_VANGOGH -> {
            drawArc(
                color = color,
                startAngle = 180f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(-length / 2, -length / 2),
                size = Size(length, length),
                style = Stroke(strokeWidth)
            )
            drawArc(
                color = color,
                startAngle = 270f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(size.width - length / 2, -length / 2),
                size = Size(length, length),
                style = Stroke(strokeWidth)
            )
            drawArc(
                color = secColor,
                startAngle = 90f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(-length / 2, size.height - length / 2),
                size = Size(length, length),
                style = Stroke(strokeWidth)
            )
            drawArc(
                color = secColor,
                startAngle = 0f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(size.width - length / 2, size.height - length / 2),
                size = Size(length, length),
                style = Stroke(strokeWidth)
            )
        }
        CyberSkin.COFFESHOP -> {
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
                size = Size(size.width - 6.dp.toPx(), size.height - 6.dp.toPx()),
                style = Stroke(1.dp.toPx())
            )
        }
        CyberSkin.BUDS -> {
            fun drawStarCross(cx: Float, cy: Float, col: Color) {
                drawLine(col, Offset(cx - 5.dp.toPx(), cy), Offset(cx + 5.dp.toPx(), cy), 1.5.dp.toPx())
                drawLine(col, Offset(cx, cy - 5.dp.toPx()), Offset(cx, cy + 5.dp.toPx()), 1.5.dp.toPx())
            }
            drawStarCross(length, length, color)
            drawStarCross(size.width - length, length, color)
            drawStarCross(length, size.height - length, secColor)
            drawStarCross(size.width - length, size.height - length, secColor)
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

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primaryColor.copy(alpha = 0.22f * pulseScale), Color.Transparent),
                center = Offset(centerX, centerY),
                radius = width * 0.45f
            ),
            radius = width * 0.45f
        )

        drawCircle(
            color = secondaryColor.copy(alpha = 0.25f),
            radius = width * 0.4f,
            center = Offset(centerX, centerY),
            style = Stroke(
                width = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
            )
        )

        val scale = 0.8f * openProgress + 0.1f
        val path = Path().apply {
            moveTo(centerX, centerY + 80.dp.toPx() * scale)
            quadraticTo(centerX - 35.dp.toPx() * scale, centerY - 10.dp.toPx() * scale, centerX - 10.dp.toPx() * scale, centerY - 100.dp.toPx() * scale)
            quadraticTo(centerX, centerY - 120.dp.toPx() * scale, centerX, centerY - 125.dp.toPx() * scale)
            quadraticTo(centerX, centerY - 120.dp.toPx() * scale, centerX + 10.dp.toPx() * scale, centerY - 100.dp.toPx() * scale)
            quadraticTo(centerX + 35.dp.toPx() * scale, centerY - 10.dp.toPx() * scale, centerX, centerY + 80.dp.toPx() * scale)

            moveTo(centerX, centerY + 50.dp.toPx() * scale)
            quadraticTo(centerX - 60.dp.toPx() * scale, centerY + 10.dp.toPx() * scale, centerX - 90.dp.toPx() * scale, centerY - 40.dp.toPx() * scale)
            quadraticTo(centerX - 80.dp.toPx() * scale, centerY - 50.dp.toPx() * scale, centerX - 70.dp.toPx() * scale, centerY - 45.dp.toPx() * scale)
            quadraticTo(centerX - 35.dp.toPx() * scale, centerY + 10.dp.toPx() * scale, centerX, centerY + 50.dp.toPx() * scale)

            moveTo(centerX, centerY + 50.dp.toPx() * scale)
            quadraticTo(centerX + 60.dp.toPx() * scale, centerY + 10.dp.toPx() * scale, centerX + 90.dp.toPx() * scale, centerY - 40.dp.toPx() * scale)
            quadraticTo(centerX + 80.dp.toPx() * scale, centerY - 50.dp.toPx() * scale, centerX + 70.dp.toPx() * scale, centerY - 45.dp.toPx() * scale)
            quadraticTo(centerX + 35.dp.toPx() * scale, centerY + 10.dp.toPx() * scale, centerX, centerY + 50.dp.toPx() * scale)

            if (openProgress > 0.4f) {
                moveTo(centerX, centerY + 65.dp.toPx() * scale)
                quadraticTo(centerX - 70.dp.toPx() * scale, centerY + 60.dp.toPx() * scale, centerX - 80.dp.toPx() * scale, centerY + 20.dp.toPx() * scale)
                quadraticTo(centerX - 65.dp.toPx() * scale, centerY + 15.dp.toPx() * scale, centerX, centerY + 65.dp.toPx() * scale)

                moveTo(centerX, centerY + 65.dp.toPx() * scale)
                quadraticTo(centerX + 70.dp.toPx() * scale, centerY + 60.dp.toPx() * scale, centerX + 80.dp.toPx() * scale, centerY + 20.dp.toPx() * scale)
                quadraticTo(centerX + 65.dp.toPx() * scale, centerY + 15.dp.toPx() * scale, centerX, centerY + 65.dp.toPx() * scale)
            }
        }

        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(primaryColor, secondaryColor.copy(alpha = 0.5f)),
                startY = centerY - 120.dp.toPx() * scale,
                endY = centerY + 80.dp.toPx() * scale
            )
        )

        drawPath(
            path = path,
            color = primaryColor.copy(alpha = 0.95f),
            style = Stroke(width = 2.5.dp.toPx())
        )

        drawLine(
            color = primaryColor.copy(alpha = 0.8f),
            start = Offset(centerX, centerY + 95.dp.toPx() * scale),
            end = Offset(centerX, centerY - 110.dp.toPx() * scale),
            strokeWidth = 2.dp.toPx()
        )

        if (openProgress > 0.3f) {
            drawLine(primaryColor.copy(alpha = 0.7f), Offset(centerX, centerY + 20.dp.toPx() * scale), Offset(centerX - 40.dp.toPx() * scale, centerY - 20.dp.toPx() * scale), 1.5.dp.toPx())
            drawLine(primaryColor.copy(alpha = 0.7f), Offset(centerX, centerY + 20.dp.toPx() * scale), Offset(centerX + 40.dp.toPx() * scale, centerY - 20.dp.toPx() * scale), 1.5.dp.toPx())
            drawLine(primaryColor.copy(alpha = 0.7f), Offset(centerX, centerY - 30.dp.toPx() * scale), Offset(centerX - 25.dp.toPx() * scale, centerY - 65.dp.toPx() * scale), 1.5.dp.toPx())
            drawLine(primaryColor.copy(alpha = 0.7f), Offset(centerX, centerY - 30.dp.toPx() * scale), Offset(centerX + 25.dp.toPx() * scale, centerY - 65.dp.toPx() * scale), 1.5.dp.toPx())
        }

        val trichomeGlow = secondaryColor.copy(alpha = 0.9f)
        val tRadius = 4.dp.toPx() * pulseScale

        drawCircle(trichomeGlow, tRadius, Offset(centerX, centerY - 125.dp.toPx() * scale))
        drawCircle(trichomeGlow, tRadius, Offset(centerX - 90.dp.toPx() * scale, centerY - 40.dp.toPx() * scale))
        drawCircle(trichomeGlow, tRadius, Offset(centerX + 90.dp.toPx() * scale, centerY - 40.dp.toPx() * scale))

        if (openProgress > 0.4f) {
            drawCircle(trichomeGlow, tRadius * 0.7f, Offset(centerX - 80.dp.toPx() * scale, centerY + 20.dp.toPx() * scale))
            drawCircle(trichomeGlow, tRadius * 0.7f, Offset(centerX + 80.dp.toPx() * scale, centerY + 20.dp.toPx() * scale))
        }
    }
}

fun isColorDark(color: Color): Boolean {
    val l = 0.2126f * color.red + 0.7152f * color.green + 0.0722f * color.blue
    return l < 0.45f
}

fun getHighContrastTextStyle(color: Color): TextStyle {
    val shadowColor = if (isColorDark(color)) Color.White else Color.Black
    return TextStyle(
        shadow = Shadow(
            color = shadowColor,
            offset = Offset(0f, 0f),
            blurRadius = 10f
        )
    )
}

@Composable
fun ContrastText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Cyber.colors.textHigh,
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
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Space.xs)
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
            modifier = Modifier
                .size(18.dp)
                .padding(horizontal = Space.xs)
        )
        ContrastText(
            text = title.uppercase(),
            color = Cyber.colors.textHigh,
            fontSize = MaterialTheme.typography.titleSmall.fontSize,
            fontFamily = Exo2FontFamily,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = Space.sm)
        )
        Icon(
            imageVector = Icons.Default.Adjust,
            contentDescription = null,
            tint = skin.primaryColor,
            modifier = Modifier
                .size(18.dp)
                .padding(horizontal = Space.xs)
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
    color: Color = Cyber.colors.textHigh,
    fontFamily: FontFamily? = null,
    fontWeight: FontWeight? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    maxLines: Int = 1,
    targetTextSize: TextUnit = 16.sp,
    minTextSize: TextUnit = 10.sp,
    textAlign: TextAlign? = null
) {
    var textSize by remember(text, targetTextSize) { mutableStateOf(targetTextSize) }
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
            if (textLayoutResult.hasVisualOverflow && textSize.value > minTextSize.value) {
                textSize = (textSize.value - 1f).sp
            } else {
                readyToDraw = true
            }
        }
    )
}

@Composable
fun getContrastColor(skin: CyberSkin, customColor: Color?): Color = Cyber.colors.textHigh

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
                    colors = listOf(PanelDark, PanelRaised)
                )
            )
            val bracketLength = 10.dp.toPx()
            val strokeW = 1.5.dp.toPx()
            drawLine(col1.copy(alpha = 0.45f), Offset(4.dp.toPx(), 4.dp.toPx()), Offset(4.dp.toPx(), size.height - 4.dp.toPx()), strokeW)
            drawLine(col1.copy(alpha = 0.45f), Offset(4.dp.toPx(), 4.dp.toPx()), Offset(4.dp.toPx() + bracketLength, 4.dp.toPx()), strokeW)
            drawLine(col1.copy(alpha = 0.45f), Offset(4.dp.toPx(), size.height - 4.dp.toPx()), Offset(4.dp.toPx() + bracketLength, size.height - 4.dp.toPx()), strokeW)

            drawLine(col1.copy(alpha = 0.45f), Offset(size.width - 4.dp.toPx(), 4.dp.toPx()), Offset(size.width - 4.dp.toPx(), size.height - 4.dp.toPx()), strokeW)
            drawLine(col1.copy(alpha = 0.45f), Offset(size.width - 4.dp.toPx(), 4.dp.toPx()), Offset(size.width - 4.dp.toPx() - bracketLength, 4.dp.toPx()), strokeW)
            drawLine(col1.copy(alpha = 0.45f), Offset(size.width - 4.dp.toPx(), size.height - 4.dp.toPx()), Offset(size.width - 4.dp.toPx() - bracketLength, size.height - 4.dp.toPx()), strokeW)
        }
        CyberSkin.MOROCCO, CyberSkin.COFFESHOP -> {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(PanelRaised, InkDeep)
                )
            )
            val slashOffset = 15.dp.toPx()
            drawCircle(col1.copy(alpha = 0.35f), radius = 2.5.dp.toPx(), center = Offset(slashOffset + 8.dp.toPx(), 6.dp.toPx()))
            drawCircle(col1.copy(alpha = 0.35f), radius = 2.5.dp.toPx(), center = Offset(size.width - slashOffset - 8.dp.toPx(), size.height - 6.dp.toPx()))
        }
        CyberSkin.BARCELONA, CyberSkin.AMSTERDAM -> {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(PanelRaised, InkDeep),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = size.width.coerceAtLeast(1f) / 1.2f
                )
            )
            drawCircle(col1.copy(alpha = 0.5f), radius = 3.dp.toPx(), center = Offset(6.dp.toPx(), 6.dp.toPx()))
            drawCircle(col2.copy(alpha = 0.5f), radius = 3.dp.toPx(), center = Offset(size.width - 6.dp.toPx(), size.height - 6.dp.toPx()))
        }
        CyberSkin.BUDS, CyberSkin.GAUDI_VANGOGH -> {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(InkDeep, PanelDark)
                )
            )
            drawCircle(col1.copy(alpha = 0.16f), radius = size.height * 0.45f, center = Offset(size.width / 2f, size.height / 2f), style = Stroke(1.dp.toPx()))
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
    shape: Shape = CyberShapes.control,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    content: @Composable RowScope.() -> Unit
) {
    val defaultContent = contentColor ?: Cyber.colors.textHigh
    val haptic = LocalHapticFeedback.current

    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = defaultContent,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = defaultContent.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(shape)
            .cyberButtonBackground(skin, enabled, containerColor)
            .border(1.5.dp, defaultContent.copy(alpha = 0.78f), shape),
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
    shape: Shape = CyberShapes.control,
    targetTextSize: TextUnit = 13.sp,
    minTextSize: TextUnit = 10.sp,
    letterSpacing: TextUnit = 0.5.sp,
    fontWeight: FontWeight = FontWeight.Bold
) {
    val effectiveTextColor = contentColor ?: Cyber.colors.textHigh
    CyberButton(
        onClick = onClick,
        modifier = modifier,
        skin = skin,
        enabled = enabled,
        containerColor = containerColor,
        contentColor = effectiveTextColor,
        shape = shape
    ) {
        AutoResizingText(
            text = text,
            color = effectiveTextColor,
            fontFamily = Exo2FontFamily,
            fontWeight = fontWeight,
            letterSpacing = letterSpacing,
            targetTextSize = targetTextSize,
            minTextSize = minTextSize,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
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
        val rx = w / 2f
        val ry = h / 2f + h * 0.25f

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
    val c = Cyber.colors
    Box(modifier = modifier) {
        Image(
            painter = painterResource(id = bgRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.65f
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            skin.primaryColor.copy(alpha = 0.15f),
                            skin.secondaryColor.copy(alpha = 0.22f),
                            c.ink.copy(alpha = 0.52f)
                        )
                    )
                )
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

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
                    color = c.goldLeaf.copy(alpha = 0.25f),
                    radius = rBase * 1.5f,
                    center = center,
                    style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f), 0f))
                )
                drawCircle(
                    color = c.goldLeaf.copy(alpha = 0.78f),
                    radius = rBase * 0.6f,
                    center = center
                )
            }
        }

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
                    contentDescription = null,
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
