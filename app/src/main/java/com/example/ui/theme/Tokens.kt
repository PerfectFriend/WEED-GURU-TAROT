package com.example.ui.theme

import android.provider.Settings
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * Unified 4/8dp spatial rhythm tokens for Cyber Tarot.
 */
object Space {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}

/**
 * Curated shape hierarchy: chips, standard controls, bottom sheets, and signature chamfered panels
 * echoing the Art Nouveau corners of the deck artwork.
 */
object CyberShapes {
    val chip = RoundedCornerShape(6.dp)
    val control = RoundedCornerShape(12.dp)
    val sheet = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    val panel = CutCornerShape(topStart = 0.dp, topEnd = 14.dp, bottomEnd = 0.dp, bottomStart = 14.dp)
    val card = RoundedCornerShape(14.dp)
    val cardCompact = RoundedCornerShape(10.dp)
}

/**
 * Shared aspect ratio across card backs, artwork windows, and thumbnails.
 */
const val CARD_ASPECT = 0.64f

/**
 * Respects OS-level reduced motion ("Remove animations" / animator_duration_scale == 0).
 */
@Composable
fun rememberMotionEnabled(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        try {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            ) != 0f
        } catch (_: Throwable) {
            true
        }
    }
}
