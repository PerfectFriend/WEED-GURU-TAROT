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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CyberTarotCard
import com.example.data.MarketListing
import com.example.data.MintedCard
import com.example.ui.components.CyberPanel
import com.example.ui.components.CyberSectionHeader
import com.example.ui.components.WeedGuruFullscreenCardViewer
import com.example.ui.theme.*
import com.example.viewmodel.TarotViewModel
import kotlinx.coroutines.delay

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
    selectedIndex: Int,
    systemLanguage: String = "en"
) {
    val c = Cyber.colors
    val isRu = systemLanguage in listOf("ru", "uk")
    val total = remember(slices) { slices.sumOf { it.value.toDouble() }.toFloat() }
    val motionEnabled = rememberMotionEnabled()

    val transitionState = remember { MutableTransitionState(if (motionEnabled) 0f else 1f) }
    LaunchedEffect(slices, motionEnabled) {
        transitionState.targetState = 1f
    }
    val animScale by animateFloatAsState(
        targetValue = if (transitionState.currentState == 1f) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (motionEnabled) 1000 else 0,
            easing = { input -> DecelerateInterpolator().getInterpolation(input) }
        ),
        label = "radialAnimation"
    )

    CyberPanel(
        borderColor = c.goldAntique.copy(alpha = 0.3f),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = c.goldLeaf,
                modifier = Modifier.padding(bottom = Space.md)
            )

            if (total == 0f) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(c.ink, CircleShape)
                        .border(1.dp, c.goldAntique.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(Space.lg)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = c.textMid,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(Space.xs))
                        Text(
                            text = if (isRu) "НЕТ ДАННЫХ" else "NO TELEMETRY",
                            style = MaterialTheme.typography.titleSmall,
                            color = c.textMid
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
                            color = c.textHigh.copy(alpha = 0.05f),
                            radius = outerRadius,
                            style = Stroke(width = 1.dp.toPx())
                        )
                        drawCircle(
                            color = c.textHigh.copy(alpha = 0.04f),
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
                                val lineEnd = Offset(
                                    center.x + (outerRadius + 18.dp.toPx()) * cos,
                                    center.y + (outerRadius + 18.dp.toPx()) * sin
                                )

                                drawLine(
                                    color = slice.color,
                                    start = lineStart,
                                    end = lineEnd,
                                    strokeWidth = 1.5.dp.toPx()
                                )
                                drawCircle(color = slice.color, radius = 4.dp.toPx(), center = lineEnd)
                                drawCircle(color = c.textHigh, radius = 1.5.dp.toPx(), center = lineEnd)
                            }

                            currentAngle += (slice.value / total) * 360f
                        }

                        drawLine(
                            color = c.textHigh.copy(alpha = 0.06f),
                            start = Offset(center.x - 12.dp.toPx(), center.y),
                            end = Offset(center.x + 12.dp.toPx(), center.y),
                            strokeWidth = 1.dp.toPx()
                        )
                        drawLine(
                            color = c.textHigh.copy(alpha = 0.06f),
                            start = Offset(center.x, center.y - 12.dp.toPx()),
                            end = Offset(center.x, center.y + 12.dp.toPx()),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(Space.xl)
                    ) {
                        val activeSlice = slices.getOrNull(selectedIndex)
                        if (activeSlice != null) {
                            Text(
                                text = "${((activeSlice.value / total) * 100).toInt()}%",
                                style = MaterialTheme.typography.headlineSmall,
                                fontFamily = IbmPlexMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = activeSlice.color
                            )
                            Text(
                                text = if (isRu) {
                                    "${activeSlice.value.toInt()} ШТ."
                                } else {
                                    "${activeSlice.value.toInt()} UNIT" + (if (activeSlice.value == 1f) "" else "S")
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = c.textMid
                            )
                        } else {
                            Text(
                                text = if (isRu) "ВСЕГО" else "TOTAL",
                                style = MaterialTheme.typography.titleSmall,
                                color = c.textMid
                            )
                            Text(
                                text = "${total.toInt()}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontFamily = IbmPlexMonoFontFamily,
                                color = c.textHigh
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Space.lg))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Space.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                slices.forEachIndexed { index, slice ->
                    val isSelected = index == selectedIndex
                    Surface(
                        onClick = { onSliceSelected(index) },
                        shape = CyberShapes.chip,
                        color = if (isSelected) slice.color.copy(alpha = 0.16f) else c.panelRaised.copy(alpha = 0.55f),
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) slice.color else c.goldAntique.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = Space.xs, vertical = Space.sm)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(slice.color, CircleShape)
                            )
                            Spacer(modifier = Modifier.height(Space.xs))
                            Text(
                                text = slice.label.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) c.textHigh else c.textMid,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${slice.value.toInt()}",
                                style = MaterialTheme.typography.labelLarge,
                                fontFamily = IbmPlexMonoFontFamily,
                                color = slice.color
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NeuralAnalyticsView(
    mintedCards: List<MintedCard>,
    marketListings: List<MarketListing>,
    systemLanguage: String = "en"
) {
    val c = Cyber.colors
    val isRu = systemLanguage in listOf("ru", "uk")
    var selectedCollectionSlice by remember { mutableStateOf(0) }
    var selectedMarketSlice by remember { mutableStateOf(0) }

    val collectionSlices = remember(mintedCards, isRu) {
        val labelLogic = if (isRu) "ЛОГИКА" else "LOGIC GATES"
        val labelMem = if (isRu) "ПАМЯТЬ" else "SILICON MEMORY"
        val labelNet = if (isRu) "СВЯЗЬ" else "NEURAL COMMS"
        val labelPwr = if (isRu) "ЭНЕРГИЯ" else "POWER ENGINE"
        val descLogic = if (isRu) "Процессоры, логические матрицы и криптографические узлы." else "Central processors, logic networks, registers, and cryptographic units."
        val descMem = if (isRu) "Постоянная память ROM/RAM и сектора хранения арканов." else "Flash ROM, volatile RAM arrays, and persistent sector matrices."
        val descNet = if (isRu) "Оптоволоконные каналы, шлюзы и сетевые трансиверы." else "Optoelectronic fiber, bridge gates, and global network transceivers."
        val descPwr = if (isRu) "Фазовая синхронизация, теплоотводы и силовые реле." else "Internal clock phase-locked loops, thermal conduits, and master relays."

        if (mintedCards.isEmpty()) {
            listOf(
                RadialChartSlice(labelLogic, 5f, NeonCyan, descLogic),
                RadialChartSlice(labelMem, 3f, c.emeraldGlow, descMem),
                RadialChartSlice(labelNet, 4f, NeonMagenta, descNet),
                RadialChartSlice(labelPwr, 2f, c.goldLeaf, descPwr)
            )
        } else {
            val counts = FloatArray(4)
            mintedCards.forEach { mint -> counts[mint.cardIndex % 4] += 1f }
            listOf(
                RadialChartSlice(labelLogic, counts[0], NeonCyan, descLogic),
                RadialChartSlice(labelMem, counts[1], c.emeraldGlow, descMem),
                RadialChartSlice(labelNet, counts[2], NeonMagenta, descNet),
                RadialChartSlice(labelPwr, counts[3], c.goldLeaf, descPwr)
            )
        }
    }

    val marketSlices = remember(marketListings, isRu) {
        val descCommon = if (isRu) "Базовые подпрограммы, высокая доступность, минимальная стоимость узла." else "Standard utility subroutines, high availability index, low-cost execution nodes."
        val descRare = if (isRu) "Драйверы производительности и контроллеры ядра среднего уровня." else "Performance drivers, system kernel controllers, medium-level access nodes."
        val descEpic = if (isRu) "Сложные кластеры нейросетей, матрицы глубинного обучения." else "Highly complex mainframe clusters, deep learning matrices, restricted credentials."
        val descLegend = if (isRu) "Суверенные корневые узлы оракула, ультраредкие артефакты." else "Sovereign global mainframe roots, self-evolving AI oracle systems, ultra-scarce nodes."

        if (marketListings.isEmpty()) {
            listOf(
                RadialChartSlice("COMMON", 32f, c.emeraldGlow, descCommon),
                RadialChartSlice("RARE", 16f, NeonCyan, descRare),
                RadialChartSlice("EPIC", 8f, NeonMagenta, descEpic),
                RadialChartSlice("LEGEND", 4f, c.goldLeaf, descLegend)
            )
        } else {
            val rarityCounts = mutableMapOf("COMMON" to 0f, "RARE" to 0f, "EPIC" to 0f, "LEGEND" to 0f)
            marketListings.forEach { listing ->
                val key = listing.rarity.uppercase()
                rarityCounts[key] = (rarityCounts[key] ?: 0f) + 1f
            }
            listOf(
                RadialChartSlice("COMMON", rarityCounts["COMMON"] ?: 0f, c.emeraldGlow, descCommon),
                RadialChartSlice("RARE", rarityCounts["RARE"] ?: 0f, NeonCyan, descRare),
                RadialChartSlice("EPIC", rarityCounts["EPIC"] ?: 0f, NeonMagenta, descEpic),
                RadialChartSlice("LEGEND", rarityCounts["LEGEND"] ?: 0f, c.goldLeaf, descLegend)
            )
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(Space.lg)
    ) {
        item {
            CyberPanel(
                borderColor = NeonCyan.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val motionEnabled = rememberMotionEnabled()
                    val pulseAlpha = if (motionEnabled) {
                        val infiniteTransition = rememberInfiniteTransition(label = "scanner")
                        infiniteTransition.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1200, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "scannerAlpha"
                        ).value
                    } else {
                        0.85f
                    }
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(NeonCyan.copy(alpha = pulseAlpha), CircleShape)
                            .border(1.dp, NeonCyan, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(Space.sm))
                    Text(
                        text = if (isRu) {
                            "СКАНЕР КОЛОДЫ: СТАТУС СТАБИЛЕН | СИНХРОНИЗАЦИЯ"
                        } else {
                            "ACTIVE NET-DECK SCANNER: STATUS STABLE | METRICS SYNC ONLINE"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = IbmPlexMonoFontFamily,
                        color = c.textHigh
                    )
                }
            }
        }

        item {
            InteractiveCircuitRadialChart(
                slices = collectionSlices,
                title = if (isRu) "Частота стихий коллекции" else "Card Collection Frequency (Elements)",
                selectedIndex = selectedCollectionSlice,
                onSliceSelected = { selectedCollectionSlice = it },
                systemLanguage = systemLanguage,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            collectionSlices.getOrNull(selectedCollectionSlice)?.let { slice ->
                CyberPanel(
                    borderColor = slice.color.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CyberSectionHeader(
                        title = if (isRu) "${slice.label} • ТЕЛЕМЕТРИЯ" else "${slice.label} TELEMETRY",
                        color = slice.color
                    )
                    Spacer(modifier = Modifier.height(Space.xs))
                    Text(
                        text = slice.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.textHigh
                    )
                    Spacer(modifier = Modifier.height(Space.sm))
                    Text(
                        text = if (isRu) {
                            "Статус: скомпилировано • Активных узлов: ${slice.value.toInt()}"
                        } else {
                            "STATUS: Compiled | Allocated density: ${slice.value.toInt()} nodes."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = IbmPlexMonoFontFamily,
                        color = c.textMid
                    )
                }
            }
        }

        item {
            InteractiveCircuitRadialChart(
                slices = marketSlices,
                title = if (isRu) "Распределение редкости на маркете" else "Market Rarity Distribution",
                selectedIndex = selectedMarketSlice,
                onSliceSelected = { selectedMarketSlice = it },
                systemLanguage = systemLanguage,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            marketSlices.getOrNull(selectedMarketSlice)?.let { slice ->
                CyberPanel(
                    borderColor = slice.color.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CyberSectionHeader(
                        title = if (isRu) "${slice.label} • СВОДКА МАРКЕТА" else "${slice.label} DEPLOYMENT STATS",
                        color = slice.color
                    )
                    Spacer(modifier = Modifier.height(Space.xs))
                    Text(
                        text = slice.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.textHigh
                    )
                    Spacer(modifier = Modifier.height(Space.sm))
                    Text(
                        text = if (isRu) {
                            "Уровень защиты: БЕЗОПАСНО • Лотов в системе: ${slice.value.toInt()}"
                        } else {
                            "COMPLIANCE LEVEL: SECURE | Listed active node exchange instances: ${slice.value.toInt()} systems."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = IbmPlexMonoFontFamily,
                        color = c.textMid
                    )
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
    val lang = profile?.systemLanguage ?: "en"
    val isRu = lang in listOf("ru", "uk")
    val c = Cyber.colors

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
    val accentColor = remember(banner.colorHex, c.accent) {
        try {
            Color(android.graphics.Color.parseColor(banner.colorHex))
        } catch (_: Exception) {
            c.accent
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Space.sm)
            .border(
                width = 1.dp,
                color = accentColor.copy(alpha = 0.78f),
                shape = CyberShapes.control
            )
            .clickable {
                viewModel.incrementAdClicks(banner.id)
                viewModel.postNotification(
                    if (isRu) {
                        "Сигнал отправлен: запрос контакта к ${banner.authorContactOnion} (списано ${banner.messagePriceWeed} WEED)."
                    } else {
                        "Relay Signal Sent: Initiating contact request to ${banner.authorContactOnion}. (Fee paid: ${banner.messagePriceWeed} WEED)."
                    }
                )
            },
        colors = CardDefaults.cardColors(containerColor = c.panel.copy(alpha = 0.94f)),
        shape = CyberShapes.control
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Space.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Hub,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(Space.xs))
                    Text(
                        text = banner.theme.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = accentColor
                    )
                }
                Text(
                    text = if (isRu) {
                        "РЕКЛАМА (${banner.authorName.uppercase()})"
                    } else {
                        "SPONSORED (${banner.authorName.uppercase()})"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = c.textMid
                )
            }
            Spacer(modifier = Modifier.height(Space.xs))
            Text(
                text = banner.title,
                style = MaterialTheme.typography.titleMedium,
                color = c.textHigh
            )
            Text(
                text = banner.content,
                style = MaterialTheme.typography.bodySmall,
                color = c.textMid,
                modifier = Modifier.padding(top = Space.xxs)
            )
            Spacer(modifier = Modifier.height(Space.sm))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Launch,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(Space.xs))
                    Text(
                        text = if (isRu) "НАЖМИТЕ ДЛЯ СВЯЗИ" else "CLICK TO NEGOTIATE DEALS",
                        style = MaterialTheme.typography.labelMedium,
                        color = accentColor
                    )
                }
                Text(
                    text = if (isRu) "Клики: ${banner.clicks}" else "Clicks: ${banner.clicks}",
                    style = MaterialTheme.typography.labelSmall,
                    color = c.textMid
                )
            }
        }
    }
}
