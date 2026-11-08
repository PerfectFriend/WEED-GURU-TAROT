package com.example.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.*
import kotlin.random.Random

/**
 * Normalized rarity categories supported across Cyber Tarot.
 */
enum class CyberRarityTier {
    COMMON,
    RARE,
    EPIC,
    LEGENDARY,
    GENESIS;

    companion object {
        fun fromString(rarity: String?): CyberRarityTier {
            return when (rarity?.trim()?.lowercase()) {
                "rare" -> RARE
                "epic" -> EPIC
                "legend", "legendary" -> LEGENDARY
                "genesis", "singularity", "genesis singularity" -> GENESIS
                else -> COMMON
            }
        }
    }
}

/**
 * Color and visual configuration for each rarity tier.
 */
data class RarityShaderPalette(
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val darkBase: Color,
    val glowColor: Color,
    val borderColors: List<Color>,
    val holographicColors: List<Color>
)

object CyberRarityPalettes {
    val Common = RarityShaderPalette(
        primary = Color(0xFF00FF66), // Neon Emerald
        secondary = Color(0xFF008833),
        accent = Color(0xFF88FFAA),
        darkBase = Color(0xFF041208),
        glowColor = Color(0x3300FF66),
        borderColors = listOf(Color(0xFF00FF66), Color(0xFF005522), Color(0xFF00FF66)),
        holographicColors = listOf(Color(0xFF00FF66), Color(0xFF33FFAA), Color(0xFF00AA44))
    )

    val Rare = RarityShaderPalette(
        primary = Color(0xFF00F5FF), // Electric Cyan
        secondary = Color(0xFF0055FF), // Pulse Blue
        accent = Color(0xFF80FAFF),
        darkBase = Color(0xFF030D1A),
        glowColor = Color(0x4400F5FF),
        borderColors = listOf(Color(0xFF00F5FF), Color(0xFF0044BB), Color(0xFF00F5FF)),
        holographicColors = listOf(Color(0xFF00F5FF), Color(0xFF0088FF), Color(0xFF44FFFF))
    )

    val Epic = RarityShaderPalette(
        primary = Color(0xFFBD00FF), // Ultraviolet Magenta
        secondary = Color(0xFFFF007F), // Neon Pink
        accent = Color(0xFFE088FF),
        darkBase = Color(0xFF140321),
        glowColor = Color(0x55BD00FF),
        borderColors = listOf(Color(0xFFBD00FF), Color(0xFFFF007F), Color(0xFFBD00FF)),
        holographicColors = listOf(
            Color(0xFFFF007F),
            Color(0xFFBD00FF),
            Color(0xFF00F5FF),
            Color(0xFFFFD700),
            Color(0xFFFF007F)
        )
    )

    val Legendary = RarityShaderPalette(
        primary = Color(0xFFFFD700), // Solar Amber Gold
        secondary = Color(0xFFFF7700), // Molten Bronze
        accent = Color(0xFFFFF4B8),
        darkBase = Color(0xFF1F1200),
        glowColor = Color(0x66FFD700),
        borderColors = listOf(Color(0xFFFFD700), Color(0xFFFF8800), Color(0xFFFFEE55), Color(0xFFFFD700)),
        holographicColors = listOf(
            Color(0xFFFFD700),
            Color(0xFFFF5500),
            Color(0xFFFFEE88),
            Color(0xFFFF9900)
        )
    )

    val Genesis = RarityShaderPalette(
        primary = Color(0xFFFFFFFF), // Singularity White
        secondary = Color(0xFF00F5FF),
        accent = Color(0xFFFFD700),
        darkBase = Color(0xFF0A0A14),
        glowColor = Color(0x77FFFFFF),
        borderColors = listOf(
            Color(0xFFFFFFFF),
            Color(0xFF00F5FF),
            Color(0xFFBD00FF),
            Color(0xFFFFD700),
            Color(0xFFFFFFFF)
        ),
        holographicColors = listOf(
            Color(0xFFFFFFFF),
            Color(0xFF00F5FF),
            Color(0xFFBD00FF),
            Color(0xFFFFD700),
            Color(0xFF00FF66),
            Color(0xFFFFFFFF)
        )
    )

    fun get(tier: CyberRarityTier): RarityShaderPalette = when (tier) {
        CyberRarityTier.COMMON -> Common
        CyberRarityTier.RARE -> Rare
        CyberRarityTier.EPIC -> Epic
        CyberRarityTier.LEGENDARY -> Legendary
        CyberRarityTier.GENESIS -> Genesis
    }
}

/**
 * Procedural AGSL Shader source definitions for Android 13+ (API 33+).
 * Falls back transparently on older APIs or if compilation fails.
 */
object AgslShaderSources {
    // AGSL Scanline Shader for Common cards
    const val COMMON_CRT_AGSL = """
        uniform float2 uResolution;
        uniform float uTime;
        uniform float4 uColor;

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / uResolution;
            float scanline = sin((uv.y + uTime * 0.15) * 240.0) * 0.5 + 0.5;
            float vignette = uv.x * (1.0 - uv.x) * uv.y * (1.0 - uv.y) * 15.0;
            vignette = clamp(vignette, 0.0, 1.0);
            float alpha = (0.04 + scanline * 0.08) * vignette;
            return half4(uColor.rgb * alpha, alpha);
        }
    """

    // AGSL Circuit Ripple Shader for Rare cards
    const val RARE_CIRCUIT_AGSL = """
        uniform float2 uResolution;
        uniform float uTime;
        uniform float4 uColor;

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / uResolution;
            float2 center = float2(0.5, 0.5);
            float dist = distance(uv, center);
            float wave = sin(dist * 25.0 - uTime * 4.0);
            float grid = step(0.96, frac(uv.x * 12.0)) + step(0.96, frac(uv.y * 18.0));
            float intensity = clamp(wave * 0.08 + grid * 0.05, 0.0, 0.35);
            return half4(uColor.rgb * intensity, intensity);
        }
    """

    // AGSL Holographic Iridescent Foil for Epic cards
    const val EPIC_HOLO_AGSL = """
        uniform float2 uResolution;
        uniform float uTime;
        uniform float4 uColor;

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / uResolution;
            float diag = (uv.x + uv.y) * 3.0 + uTime * 1.2;
            float r = sin(diag) * 0.5 + 0.5;
            float g = sin(diag + 2.094) * 0.5 + 0.5;
            float b = sin(diag + 4.188) * 0.5 + 0.5;
            float sheen = pow(sin(diag * 0.8), 6.0) * 0.25;
            half3 holo = half3(r, g, b) * sheen;
            return half4(holo + uColor.rgb * 0.06, sheen + 0.06);
        }
    """

    // AGSL Golden Vortex Shader for Legendary cards
    const val LEGENDARY_VORTEX_AGSL = """
        uniform float2 uResolution;
        uniform float uTime;
        uniform float4 uColor;

        half4 main(float2 fragCoord) {
            float2 uv = (fragCoord - uResolution * 0.5) / min(uResolution.x, uResolution.y);
            float r = length(uv);
            float angle = atan2(uv.y, uv.x);
            float spiral = sin(r * 18.0 - angle * 4.0 + uTime * 2.0);
            float glow = smoothstep(0.7, 0.1, r) * 0.15;
            float pattern = clamp(spiral * 0.1 + glow, 0.0, 0.4);
            return half4(uColor.rgb * pattern, pattern);
        }
    """
}

/**
 * Cache for AGSL RuntimeShaders on API 33+
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private object AgslShaderCache {
    private val shaders = mutableMapOf<CyberRarityTier, android.graphics.RuntimeShader>()

    fun getShader(tier: CyberRarityTier): android.graphics.RuntimeShader? {
        return shaders.getOrPut(tier) {
            try {
                val source = when (tier) {
                    CyberRarityTier.COMMON -> AgslShaderSources.COMMON_CRT_AGSL
                    CyberRarityTier.RARE -> AgslShaderSources.RARE_CIRCUIT_AGSL
                    CyberRarityTier.EPIC -> AgslShaderSources.EPIC_HOLO_AGSL
                    CyberRarityTier.LEGENDARY, CyberRarityTier.GENESIS -> AgslShaderSources.LEGENDARY_VORTEX_AGSL
                }
                android.graphics.RuntimeShader(source)
            } catch (e: Throwable) {
                return null
            }
        }
    }
}

/**
 * Dynamic Visual Shader Overlay that renders animated textures, glowing matrix grids,
 * and holographic sheens based on the card's rarity tier.
 *
 * Runs efficiently across all Android versions (API 24 to 36) via Compose DrawScope,
 * enhanced with hardware AGSL when on Android 13+.
 */
@Composable
fun CyberRarityShaderOverlay(
    rarity: String,
    modifier: Modifier = Modifier,
    isAnimated: Boolean = true
) {
    val tier = remember(rarity) { CyberRarityTier.fromString(rarity) }
    val palette = remember(tier) { CyberRarityPalettes.get(tier) }

    val infiniteTransition = rememberInfiniteTransition(label = "rarity_shader_transition")

    val timeProgress by if (isAnimated) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 4000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "time_progress"
        )
    } else {
        remember { mutableFloatStateOf(0.5f) }
    }

    val pulseGlow by if (isAnimated) {
        infiniteTransition.animateFloat(
            initialValue = 0.75f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_glow"
        )
    } else {
        remember { mutableFloatStateOf(1f) }
    }

    val foilOffset by if (isAnimated) {
        infiniteTransition.animateFloat(
            initialValue = -0.3f,
            targetValue = 1.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "foil_offset"
        )
    } else {
        remember { mutableFloatStateOf(0.5f) }
    }

    // Canvas procedural shader renderer
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        when (tier) {
            CyberRarityTier.COMMON -> {
                drawCommonCrtShader(palette, width, height, timeProgress, pulseGlow)
            }
            CyberRarityTier.RARE -> {
                drawRareCircuitShader(palette, width, height, timeProgress, pulseGlow)
            }
            CyberRarityTier.EPIC -> {
                drawEpicHolographicShader(palette, width, height, timeProgress, foilOffset, pulseGlow)
            }
            CyberRarityTier.LEGENDARY -> {
                drawLegendaryVortexShader(palette, width, height, timeProgress, pulseGlow)
            }
            CyberRarityTier.GENESIS -> {
                drawGenesisSingularityShader(palette, width, height, timeProgress, foilOffset, pulseGlow)
            }
        }
    }
}

/**
 * COMMON SHADER: CRT Phosphor scanlines, digital raster dots, and green matrix sweep.
 */
private fun DrawScope.drawCommonCrtShader(
    palette: RarityShaderPalette,
    width: Float,
    height: Float,
    progress: Float,
    pulse: Float
) {
    // 1. CRT Horizontal Scanlines
    val lineSpacing = 6f
    val scanlineCount = (height / lineSpacing).toInt()
    val scanPhase = progress * lineSpacing

    for (i in 0 until scanlineCount) {
        val y = (i * lineSpacing + scanPhase) % height
        val lineAlpha = (0.05f + 0.04f * sin(i * 0.2f + progress * 6.28f)) * pulse
        drawLine(
            color = palette.primary.copy(alpha = lineAlpha.coerceIn(0.02f, 0.16f)),
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 1.2f
        )
    }

    // 2. Refresh beam sweep moving downwards
    val beamY = (progress * height * 1.2f) % height
    val beamBrush = Brush.verticalGradient(
        colors = listOf(
            Color.Transparent,
            palette.primary.copy(alpha = 0.12f * pulse),
            palette.accent.copy(alpha = 0.22f * pulse),
            Color.Transparent
        ),
        startY = beamY - 40f,
        endY = beamY + 40f
    )
    drawRect(
        brush = beamBrush,
        topLeft = Offset(0f, (beamY - 40f).coerceAtLeast(0f)),
        size = Size(width, 80f)
    )

    // 3. Matrix tech grid dots along corners
    val dotStep = 18f
    for (x in 0..4) {
        for (y in 0..4) {
            val px = 8f + x * dotStep
            val py = 8f + y * dotStep
            val dotAlpha = (0.06f + 0.05f * sin((x + y) * 0.8f + progress * 4f))
            drawCircle(
                color = palette.primary.copy(alpha = dotAlpha),
                radius = 1.5f,
                center = Offset(px, py)
            )
        }
    }
}

/**
 * RARE SHADER: Neon circuit board traces, pulsating solder nodes, and expanding cyan ripple waves.
 */
private fun DrawScope.drawRareCircuitShader(
    palette: RarityShaderPalette,
    width: Float,
    height: Float,
    progress: Float,
    pulse: Float
) {
    // 1. Concentric ripple wave from center
    val center = Offset(width * 0.5f, height * 0.5f)
    val maxRadius = hypot(width * 0.5f, height * 0.5f)
    for (ring in 0..2) {
        val ringPhase = (progress + ring * 0.333f) % 1.0f
        val currentRadius = ringPhase * maxRadius
        val ringAlpha = ((1f - ringPhase) * 0.18f * pulse).coerceIn(0f, 0.25f)
        drawCircle(
            color = palette.primary.copy(alpha = ringAlpha),
            radius = currentRadius,
            center = center,
            style = Stroke(width = 1.8f)
        )
    }

    // 2. Circuit Board Tracks (Geometric PCB lines)
    val traceColor = palette.primary.copy(alpha = 0.15f * pulse)
    val nodeColor = palette.accent.copy(alpha = 0.28f * pulse)

    // Top-left circuit branch
    drawLine(traceColor, Offset(16f, 20f), Offset(width * 0.35f, 20f), strokeWidth = 2f)
    drawLine(traceColor, Offset(width * 0.35f, 20f), Offset(width * 0.5f, 40f), strokeWidth = 2f)
    drawCircle(nodeColor, radius = 3.5f, center = Offset(width * 0.5f, 40f))

    // Bottom-right circuit branch
    drawLine(traceColor, Offset(width - 16f, height - 20f), Offset(width * 0.65f, height - 20f), strokeWidth = 2f)
    drawLine(traceColor, Offset(width * 0.65f, height - 20f), Offset(width * 0.5f, height - 40f), strokeWidth = 2f)
    drawCircle(nodeColor, radius = 3.5f, center = Offset(width * 0.5f, height - 40f))

    // Left vertical rail with angled spurs
    drawLine(traceColor, Offset(24f, 60f), Offset(24f, height - 60f), strokeWidth = 1.5f)
    drawLine(traceColor, Offset(24f, height * 0.4f), Offset(44f, height * 0.4f + 20f), strokeWidth = 1.5f)
    drawCircle(nodeColor, radius = 3f, center = Offset(44f, height * 0.4f + 20f))

    // 3. Traveling Light Packet (Data Pulse along circuit)
    val packetX = 16f + (width * 0.35f - 16f) * ((progress * 2f) % 1f)
    drawCircle(
        color = Color.White.copy(alpha = 0.75f * pulse),
        radius = 2.5f,
        center = Offset(packetX, 20f)
    )
    drawCircle(
        color = palette.primary.copy(alpha = 0.4f),
        radius = 6f,
        center = Offset(packetX, 20f)
    )
}

/**
 * EPIC SHADER: Holographic foil sweep, prismatic spectral dispersion, and floating quantum sparkles.
 */
private fun DrawScope.drawEpicHolographicShader(
    palette: RarityShaderPalette,
    width: Float,
    height: Float,
    progress: Float,
    foilOffset: Float,
    pulse: Float
) {
    // 1. Diagonal Holographic Sheen Sweep (45-degree rainbow foil)
    val sweepCenter = width * foilOffset
    val holoBrush = Brush.linearGradient(
        colors = listOf(
            Color.Transparent,
            Color(0xFFFF007F).copy(alpha = 0.12f * pulse),
            Color(0xFFBD00FF).copy(alpha = 0.22f * pulse),
            Color(0xFF00F5FF).copy(alpha = 0.28f * pulse),
            Color(0xFFFFD700).copy(alpha = 0.18f * pulse),
            Color.Transparent
        ),
        start = Offset(sweepCenter - 120f, -40f),
        end = Offset(sweepCenter + 120f, height + 40f)
    )
    drawRect(brush = holoBrush)

    // 2. Chromatic Aberration Fringe Lines at top and bottom
    val shift = 2.5f * sin(progress * 6.28f)
    drawLine(
        color = Color(0xFFFF0055).copy(alpha = 0.25f),
        start = Offset(10f - shift, 12f),
        end = Offset(width - 10f - shift, 12f),
        strokeWidth = 1.5f
    )
    drawLine(
        color = Color(0xFF00F5FF).copy(alpha = 0.25f),
        start = Offset(10f + shift, 14f),
        end = Offset(width - 10f + shift, 14f),
        strokeWidth = 1.5f
    )

    // 3. Floating Quantum Starlight Sparkles
    val sparklePositions = listOf(
        Offset(width * 0.22f, height * 0.18f),
        Offset(width * 0.78f, height * 0.26f),
        Offset(width * 0.35f, height * 0.65f),
        Offset(width * 0.82f, height * 0.74f),
        Offset(width * 0.15f, height * 0.85f)
    )

    sparklePositions.forEachIndexed { idx, pos ->
        val sparklePhase = (progress * 3f + idx * 0.4f) % 1f
        val sparkleAlpha = (sin(sparklePhase * 3.14159f) * 0.6f * pulse).coerceIn(0f, 0.8f)
        val size = 3f + 2f * sin(sparklePhase * 3.14159f)

        // 4-point diamond sparkle
        drawLine(
            color = Color.White.copy(alpha = sparkleAlpha),
            start = Offset(pos.x - size * 2, pos.y),
            end = Offset(pos.x + size * 2, pos.y),
            strokeWidth = 1.2f
        )
        drawLine(
            color = Color.White.copy(alpha = sparkleAlpha),
            start = Offset(pos.x, pos.y - size * 2),
            end = Offset(pos.x, pos.y + size * 2),
            strokeWidth = 1.2f
        )
        drawCircle(
            color = palette.accent.copy(alpha = sparkleAlpha * 0.5f),
            radius = size * 1.5f,
            center = pos
        )
    }
}

/**
 * LEGENDARY SHADER: Van Gogh golden swirling vortex arcs, Gaudí fractal curves,
 * and rising golden stardust embers.
 */
private fun DrawScope.drawLegendaryVortexShader(
    palette: RarityShaderPalette,
    width: Float,
    height: Float,
    progress: Float,
    pulse: Float
) {
    val center = Offset(width * 0.5f, height * 0.42f)
    val baseAngle = progress * 360f

    // 1. Swirling Van Gogh Golden Vortex Arcs
    rotate(degrees = baseAngle * 0.4f, pivot = center) {
        val numArms = 4
        for (arm in 0 until numArms) {
            val armOffset = (arm * 360f / numArms)
            val path = Path()
            val maxR = width * 0.45f
            var first = true

            for (step in 0..24) {
                val t = step / 24f
                val r = t * maxR
                val theta = (armOffset + t * 240f) * (PI.toFloat() / 180f)
                val px = center.x + r * cos(theta)
                val py = center.y + r * sin(theta)

                if (first) {
                    path.moveTo(px, py)
                    first = false
                } else {
                    path.lineTo(px, py)
                }
            }

            drawPath(
                path = path,
                color = palette.primary.copy(alpha = (0.16f + arm * 0.03f) * pulse),
                style = Stroke(
                    width = 2.2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), phase = progress * 30f)
                )
            )
        }
    }

    // 2. Gaudí Architectural Parabolic Arch at bottom
    val archPath = Path().apply {
        moveTo(14f, height - 10f)
        quadraticTo(
            width * 0.5f,
            height - 75f - 10f * sin(progress * 6.28f),
            width - 14f,
            height - 10f
        )
    }
    drawPath(
        path = archPath,
        brush = Brush.horizontalGradient(palette.borderColors),
        style = Stroke(width = 2.5f)
    )

    // Inner echoing arch
    val innerArch = Path().apply {
        moveTo(30f, height - 12f)
        quadraticTo(
            width * 0.5f,
            height - 55f - 8f * sin(progress * 6.28f),
            width - 30f,
            height - 12f
        )
    }
    drawPath(
        path = innerArch,
        color = palette.secondary.copy(alpha = 0.35f * pulse),
        style = Stroke(width = 1.8f)
    )

    // 3. Rising Golden Stardust Embers
    for (i in 0..7) {
        val seed = i * 47
        val emberPhase = (progress + i * 0.125f) % 1f
        val ex = (width * 0.15f) + ((seed * 19) % (width * 0.7f).toInt()) + sin(emberPhase * 6f + i) * 12f
        val ey = height - (emberPhase * height * 0.9f)
        val alpha = (sin(emberPhase * 3.14159f) * 0.65f * pulse).coerceIn(0f, 0.85f)

        drawCircle(
            color = Color(0xFFFFF0A0).copy(alpha = alpha),
            radius = 2.5f,
            center = Offset(ex, ey)
        )
        drawCircle(
            color = palette.primary.copy(alpha = alpha * 0.4f),
            radius = 6f,
            center = Offset(ex, ey)
        )
    }

    // 4. Radiant Corner Sunburst Accents
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(palette.primary.copy(alpha = 0.35f * pulse), Color.Transparent),
            center = Offset(0f, 0f),
            radius = 45f
        ),
        radius = 45f,
        center = Offset(0f, 0f)
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(palette.primary.copy(alpha = 0.35f * pulse), Color.Transparent),
            center = Offset(width, 0f),
            radius = 45f
        ),
        radius = 45f,
        center = Offset(width, 0f)
    )
}

/**
 * GENESIS SHADER: Pure crystalline singularity, dynamic rainbow prism dispersion,
 * and multi-spectral sacred geometry.
 */
private fun DrawScope.drawGenesisSingularityShader(
    palette: RarityShaderPalette,
    width: Float,
    height: Float,
    progress: Float,
    foilOffset: Float,
    pulse: Float
) {
    // 1. Rainbow Prism Dispersion Across Entire Face
    val angle = (progress * 360f) * (PI.toFloat() / 180f)
    val prismBrush = Brush.sweepGradient(
        colors = palette.holographicColors,
        center = Offset(width * 0.5f, height * 0.5f)
    )
    drawRect(
        brush = prismBrush,
        alpha = 0.14f * pulse
    )

    // 2. Sacred Geometry Star (Octagram) at center
    val center = Offset(width * 0.5f, height * 0.5f)
    val starR = width * 0.38f
    rotate(degrees = progress * 90f, pivot = center) {
        val numPoints = 8
        val path = Path()
        for (i in 0 until numPoints) {
            val theta = i * (2f * PI.toFloat() / numPoints)
            val px = center.x + starR * cos(theta)
            val py = center.y + starR * sin(theta)
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()
        drawPath(
            path = path,
            color = Color.White.copy(alpha = 0.20f * pulse),
            style = Stroke(width = 1.8f)
        )

        // Crossed square inside
        rotate(degrees = 45f, pivot = center) {
            val innerPath = Path()
            for (i in 0 until numPoints) {
                val theta = i * (2f * PI.toFloat() / numPoints)
                val px = center.x + starR * 0.7f * cos(theta)
                val py = center.y + starR * 0.7f * sin(theta)
                if (i == 0) innerPath.moveTo(px, py) else innerPath.lineTo(px, py)
            }
            innerPath.close()
            drawPath(
                path = innerPath,
                color = palette.secondary.copy(alpha = 0.22f * pulse),
                style = Stroke(width = 1.2f)
            )
        }
    }
}

/**
 * Interactive Cyber Card Container that dynamically wraps any card content
 * with the glowing animated rarity shader, custom border, and depth effects.
 */
@Composable
fun CyberRarityShaderCard(
    rarity: String,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
    borderWidth: Dp = 2.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val tier = remember(rarity) { CyberRarityTier.fromString(rarity) }
    val palette = remember(tier) { CyberRarityPalettes.get(tier) }

    val infiniteTransition = rememberInfiniteTransition(label = "card_glow_pulse")
    val borderPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border_pulse"
    )

    // Animated dynamic border brush
    val borderBrush = remember(palette, borderPulse) {
        Brush.sweepGradient(
            colors = palette.borderColors.map { it.copy(alpha = (it.alpha * borderPulse).coerceIn(0.4f, 1f)) }
        )
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        palette.primary.copy(alpha = 0.20f * borderPulse),
                        palette.darkBase.copy(alpha = 0.95f),
                        Color(0xFF04040A).copy(alpha = 0.98f)
                    )
                )
            )
            .border(
                width = borderWidth,
                brush = borderBrush,
                shape = shape
            )
    ) {
        // Dynamic visual shader effect layer
        CyberRarityShaderOverlay(
            rarity = rarity,
            modifier = Modifier.matchParentSize()
        )

        // Card Content on top
        content()
    }
}

/**
 * Animated Rarity Pill Badge with dynamic glow matching the card's rarity tier.
 */
@Composable
fun CyberRarityGlowBadge(
    rarity: String,
    modifier: Modifier = Modifier,
    text: String = rarity.uppercase()
) {
    val tier = remember(rarity) { CyberRarityTier.fromString(rarity) }
    val palette = remember(tier) { CyberRarityPalettes.get(tier) }

    val infiniteTransition = rememberInfiniteTransition(label = "badge_glow")
    val alphaGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badge_alpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(palette.primary.copy(alpha = 0.18f))
            .border(
                width = 1.2.dp,
                color = palette.primary.copy(alpha = alphaGlow),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 7.dp, vertical = 2.5.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text = text,
            color = palette.accent,
            fontSize = androidx.compose.ui.unit.TextUnit(9f, androidx.compose.ui.unit.TextUnitType.Sp),
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
        )
    }
}
