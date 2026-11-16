package com.example.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Typeface
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

/**
 * Procedural neural avatar specification and Canvas bitmap renderer (extracted from GeminiService for M1/M5).
 */
object AvatarGenerator {

    data class AvatarSpecs(
        val primary: String,
        val secondary: String,
        val bg: String,
        val glyph: String,
        val density: Int
    )

    fun generateLocalAvatarSpecification(alias: String, style: String): String {
        val hash = alias.hashCode().coerceAtLeast(0)
        val shortLabel = alias.take(6).uppercase()
        val cryptoHash = Integer.toHexString(hash).take(8).padStart(8, '0').uppercase()

        val specs = when (style.lowercase()) {
            "matrix", "matrix rain" -> {
                val glyphs = listOf("⎈", "⚡", "⌗", "⌬", "⌌", "⌎", "⍝")
                val selectedGlyph = glyphs[hash % glyphs.size]
                AvatarSpecs("#00FF66", "#003311", "#050A05", selectedGlyph, 25)
            }
            "neon", "neon synthwave" -> {
                val glyphs = listOf("☼", "❖", "⚡", "◆", "▲", "▼", "◄")
                val selectedGlyph = glyphs[hash % glyphs.size]
                AvatarSpecs("#FF007F", "#8B008B", "#0C020E", selectedGlyph, 30)
            }
            "neural", "neural synapses" -> {
                val glyphs = listOf("⬢", "⌬", "⚛", "⎉", "⎊", "⎋")
                val selectedGlyph = glyphs[hash % glyphs.size]
                AvatarSpecs("#00F0FF", "#004080", "#030810", selectedGlyph, 20)
            }
            "quantum", "quantum singularity" -> {
                val glyphs = listOf("⚙", "❂", "☉", "⊚", "⊛", "⊜")
                val selectedGlyph = glyphs[hash % glyphs.size]
                AvatarSpecs("#7A22FF", "#2E0854", "#04010A", selectedGlyph, 35)
            }
            else -> {
                val glyphs = listOf("⌘", "☯", "✖", "★", "✴", "✷", "✸")
                val selectedGlyph = glyphs[hash % glyphs.size]
                AvatarSpecs("#FFCC00", "#663300", "#0A0602", selectedGlyph, 15)
            }
        }

        return """
            {
                "primaryColorHex": "${specs.primary}",
                "secondaryColorHex": "${specs.secondary}",
                "backgroundHex": "${specs.bg}",
                "glyphChar": "${specs.glyph}",
                "visualDensity": ${specs.density},
                "cryptographicHash": "$cryptoHash",
                "nodeLabel": "$shortLabel"
            }
        """.trimIndent()
    }

    fun renderAvatarToBitmap(
        context: Context,
        alias: String,
        avatarStyle: String,
        specJson: String
    ): File? {
        return try {
            val width = 256
            val height = 256
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val json = try {
                if (specJson.isBlank()) {
                    JSONObject(generateLocalAvatarSpecification(alias, avatarStyle))
                } else {
                    JSONObject(specJson)
                }
            } catch (_: Exception) {
                JSONObject(generateLocalAvatarSpecification(alias, avatarStyle))
            }

            val primaryColor = AndroidColor.parseColor(json.optString("primaryColorHex", "#00FF00"))
            val secondaryColor = AndroidColor.parseColor(json.optString("secondaryColorHex", "#003300"))
            val bgColor = AndroidColor.parseColor(json.optString("backgroundHex", "#050508"))
            val glyphChar = json.optString("glyphChar", "⌘")
            val density = json.optInt("visualDensity", 20)
            val cryptoHash = json.optString("cryptographicHash", "88888888")
            val nodeLabel = json.optString("nodeLabel", alias.take(6).uppercase())

            val bgPaint = Paint().apply {
                color = bgColor
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            val linePaint = Paint().apply {
                color = primaryColor
                strokeWidth = 1.2f
                style = Paint.Style.STROKE
                alpha = 40
            }

            val hash = alias.hashCode().coerceAtLeast(0)
            val random = java.util.Random(hash.toLong())

            when (avatarStyle.lowercase()) {
                "matrix", "matrix rain" -> {
                    val textPaint = Paint().apply {
                        color = primaryColor
                        textSize = 10f
                        typeface = Typeface.MONOSPACE
                        alpha = 100
                    }
                    for (i in 0 until density) {
                        val x = random.nextInt(width).toFloat()
                        val y = random.nextInt(height).toFloat()
                        val binary = if (random.nextBoolean()) "1" else "0"
                        canvas.drawText(binary, x, y, textPaint)
                        canvas.drawText(binary, x, (y + 15) % height, textPaint)
                        canvas.drawText(binary, x, (y + 30) % height, textPaint)
                    }
                }
                "neon", "neon synthwave" -> {
                    val sunPaint = Paint().apply {
                        isAntiAlias = true
                        shader = LinearGradient(
                            0f, height * 0.2f, 0f, height * 0.7f,
                            primaryColor, secondaryColor, Shader.TileMode.CLAMP
                        )
                    }
                    canvas.drawCircle(width / 2f, height / 2f, 70f, sunPaint)

                    val cutPaint = Paint().apply {
                        color = bgColor
                        style = Paint.Style.FILL
                    }
                    var cutY = height * 0.45f
                    while (cutY < height * 0.75f) {
                        canvas.drawRect(0f, cutY, width.toFloat(), cutY + 4f, cutPaint)
                        cutY += 12f
                    }

                    for (i in 0..8) {
                        val xStart = (width / 8f) * i
                        canvas.drawLine(xStart, height * 0.7f, (width / 2f) + (xStart - width / 2f) * 2.5f, height.toFloat(), linePaint)
                    }
                    for (i in 0..5) {
                        val y = height * 0.7f + (height * 0.3f / 5) * i
                        canvas.drawLine(0f, y, width.toFloat(), y, linePaint)
                    }
                }
                "neural", "neural synapses" -> {
                    val nodePaint = Paint().apply {
                        color = primaryColor
                        style = Paint.Style.FILL
                        isAntiAlias = true
                    }
                    val pts = List(density / 2) {
                        Pair(random.nextFloat() * width, random.nextFloat() * height)
                    }
                    linePaint.alpha = 60
                    for (i in pts.indices) {
                        for (j in i + 1 until pts.size) {
                            val dist = Math.hypot((pts[i].first - pts[j].first).toDouble(), (pts[i].second - pts[j].second).toDouble())
                            if (dist < 80.0) {
                                canvas.drawLine(pts[i].first, pts[i].second, pts[j].first, pts[j].second, linePaint)
                            }
                        }
                    }
                    for (pt in pts) {
                        canvas.drawCircle(pt.first, pt.second, 4f, nodePaint)
                    }
                }
                "quantum", "quantum singularity" -> {
                    val circlePaint = Paint().apply {
                        style = Paint.Style.STROKE
                        isAntiAlias = true
                    }
                    for (i in 1..density / 2) {
                        circlePaint.apply {
                            color = primaryColor
                            strokeWidth = (i % 3 + 1).toFloat()
                            alpha = (255 / i).coerceIn(10, 200)
                        }
                        canvas.drawCircle(width / 2f, height / 2f, (i * 8).toFloat(), circlePaint)
                    }
                }
                else -> {
                    val glyphPaint = Paint().apply {
                        color = primaryColor
                        style = Paint.Style.STROKE
                        strokeWidth = 3f
                        isAntiAlias = true
                        alpha = 180
                    }
                    val path = Path()
                    path.moveTo(width / 2f, 40f)
                    path.lineTo(width - 50f, height / 2f)
                    path.lineTo(width / 2f, height - 40f)
                    path.lineTo(50f, height / 2f)
                    path.close()
                    canvas.drawPath(path, glyphPaint)

                    path.reset()
                    path.moveTo(width / 2f, 60f)
                    path.lineTo(width - 70f, height / 2f)
                    path.lineTo(width / 2f, height - 60f)
                    path.lineTo(70f, height / 2f)
                    path.close()
                    canvas.drawPath(path, glyphPaint)
                }
            }

            val glyphPaint = Paint().apply {
                color = primaryColor
                textSize = 54f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                setShadowLayer(15f, 0f, 0f, primaryColor)
            }
            val textBounds = Rect()
            glyphPaint.getTextBounds(glyphChar, 0, glyphChar.length, textBounds)
            val glyphY = (height / 2f) - textBounds.exactCenterY()
            canvas.drawText(glyphChar, width / 2f, glyphY, glyphPaint)

            val textPaint = Paint().apply {
                color = primaryColor
                textSize = 10f
                alpha = 180
                typeface = Typeface.MONOSPACE
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("NODE: $nodeLabel", width / 2f, 25f, textPaint)
            canvas.drawText("HASH: $cryptoHash", width / 2f, height - 15f, textPaint)

            val borderPaint = Paint().apply {
                color = primaryColor
                strokeWidth = 4f
                style = Paint.Style.STROKE
                isAntiAlias = true
                alpha = 150
            }
            canvas.drawRect(2f, 2f, width - 2f, height - 2f, borderPaint)

            val file = File(context.filesDir, "custom_avatar.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
