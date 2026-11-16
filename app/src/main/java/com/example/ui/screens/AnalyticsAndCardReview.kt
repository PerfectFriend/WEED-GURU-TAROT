package com.example.ui.screens

import android.view.animation.DecelerateInterpolator
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CyberTarotCard
import com.example.data.MarketListing
import com.example.data.MintedCard
import com.example.ui.components.WeedGuruFullscreenCardViewer
import com.example.ui.theme.CyberCharcoal
import com.example.ui.theme.CyberSkin
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.RajdhaniFontFamily
import com.example.viewmodel.TarotViewModel
import kotlinx.coroutines.delay

// ==========================================
// FULL-SCREEN IMMERSIVE CARD REVIEW SYSTEM
// ==========================================
@Composable
fun FullScreenCardReview(
    card: CyberTarotCard,
    mintedCard: MintedCard? = null,
    isMarketView: Boolean = false,
    systemLanguage: String = "en",
    skin: CyberSkin = CyberSkin.JAMAICA,
    isUpright: Boolean = true,
    showOrientation: Boolean = false,
    initiallyFlipped: Boolean = false,
    actionLabel: String? = if (isMarketView) "ACQUIRE NODE SYSTEM" else null,
    onClose: () -> Unit,
    onActionClick: () -> Unit = {}
) {
    WeedGuruFullscreenCardViewer(
        card = card,
        isUpright = isUpright,
        initiallyFlipped = initiallyFlipped,
        systemLanguage = systemLanguage,
        showOrientation = showOrientation,
        mintBadgeText = mintedCard?.let { "#${String.format("%04d", it.mintNumber)}" },
        actionLabel = actionLabel,
        onActionClick = if (actionLabel != null) onActionClick else null,
        onDismiss = onClose
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
    val total = remember(slices) { slices.sumOf { it.value.toDouble() }.toFloat() }

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
    mintedCards: List<MintedCard>,
    marketListings: List<MarketListing>
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

    val activeBanners = remember(banners, enabledThemes) {
        banners.filter { enabledThemes.contains(it.theme) }
    }

    if (activeBanners.isEmpty()) return

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
