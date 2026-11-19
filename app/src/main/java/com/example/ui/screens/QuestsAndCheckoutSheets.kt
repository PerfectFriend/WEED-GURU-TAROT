package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CyberActionButton
import com.example.ui.components.CyberNeonButton
import com.example.ui.components.CyberPanel
import com.example.ui.theme.Cyber
import com.example.ui.theme.CyberObsidian
import com.example.ui.theme.CyberShapes
import com.example.ui.theme.CyberSkin
import com.example.ui.theme.IbmPlexMonoFontFamily
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.Space
import com.example.viewmodel.TarotViewModel
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun BlessingQuestsHubDialog(
    viewModel: TarotViewModel,
    onDismiss: () -> Unit
) {
    val c = Cyber.colors
    val currentSkin by viewModel.currentSkin.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val lang = profile?.systemLanguage ?: "en"

    val completedScan by viewModel.questCompletedScan.collectAsStateWithLifecycle()
    val claimedScan by viewModel.questClaimedScan.collectAsStateWithLifecycle()
    val completedProxy by viewModel.questCompletedProxy.collectAsStateWithLifecycle()
    val claimedProxy by viewModel.questClaimedProxy.collectAsStateWithLifecycle()
    val completedTip by viewModel.questCompletedTip.collectAsStateWithLifecycle()
    val claimedTip by viewModel.questClaimedTip.collectAsStateWithLifecycle()

    val uptimeBuff by viewModel.blessingUptimeBuff.collectAsStateWithLifecycle()
    val rareCardBuff by viewModel.blessingRareCardBuff.collectAsStateWithLifecycle()
    val isVipUnlocked by viewModel.isVipUnlocked.collectAsStateWithLifecycle()

    var activeTab by remember { mutableIntStateOf(0) }
    var showCheckoutSheet by remember { mutableStateOf<Map<String, Any>?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(Space.sm),
            colors = CardDefaults.cardColors(containerColor = c.panel),
            border = BorderStroke(1.5.dp, currentSkin.primaryColor),
            shape = CyberShapes.card
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Space.lg)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (lang == "ru") "МАТРИЦА УСЛУГ И КВЕСТОВ" else "MATRIX SERVICES & QUESTS",
                        style = MaterialTheme.typography.titleMedium,
                        color = c.goldLeaf,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = c.textMid)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(c.panelRaised, CyberShapes.chip)
                        .padding(Space.md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (lang == "ru") "Текущий баланс:" else "Current Balance:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.textMid
                    )
                    Text(
                        text = "${profile?.weed ?: 0} WEED",
                        style = MaterialTheme.typography.titleSmall.copy(fontFamily = IbmPlexMonoFontFamily),
                        color = c.accent
                    )
                }

                Spacer(modifier = Modifier.height(Space.md))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Space.sm)
                ) {
                    CyberActionButton(
                        text = if (lang == "ru") "Daily Квесты" else "Daily Quests",
                        onClick = { activeTab = 0 },
                        secondary = activeTab != 0,
                        modifier = Modifier.weight(1f)
                    )
                    CyberActionButton(
                        text = if (lang == "ru") "Дары и Бусты" else "Blessings & Tips",
                        onClick = { activeTab = 1 },
                        secondary = activeTab != 1,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(Space.md))

                Box(modifier = Modifier.weight(1f)) {
                    if (activeTab == 0) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(Space.md),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Text(
                                    text = if (lang == "ru") "Выполняйте задания для получения свободных кредитов:" else "Complete cyber tasks to harvest free WEED credits:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = c.textMid
                                )
                            }
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
                                Spacer(modifier = Modifier.height(Space.sm))
                                CyberActionButton(
                                    text = if (lang == "ru") "СБРОСИТЬ КВЕСТЫ (ДЕБАГ)" else "RESET QUESTS (DEBUG)",
                                    onClick = { viewModel.resetDailyQuests() },
                                    secondary = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(Space.md),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Text(
                                    text = if (lang == "ru") "ПОЖЕРТВОВАНИЯ КРЕДИТОВ (БУСТЫ)" else "CREDIT BLESSINGS (IN-GAME BUFFS)",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = NeonMagenta
                                )
                            }

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
                                Spacer(modifier = Modifier.height(Space.xs))
                                HorizontalDivider(color = c.goldAntique.copy(alpha = 0.25f))
                                Spacer(modifier = Modifier.height(Space.xs))
                                Text(
                                    text = if (lang == "ru") "МИКРОТРАНЗАКЦИИ СВЯЗИ" else "MATRIX GATEWAY MICRO-DONATIONS",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = NeonCyan
                                )
                                Text(
                                    text = if (lang == "ru") "Реалистичная симуляция платежного шлюза. Дешево, доступно, надежно." else "Simulated secure credit micro-payments. Ultra cheap, mass adoption model.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = c.textMid,
                                    modifier = Modifier.padding(top = Space.xxs)
                                )
                            }

                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(Space.sm)
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
    val c = Cyber.colors
    CyberPanel(
        borderColor = if (isCompleted && !isClaimed) NeonCyan.copy(alpha = 0.55f) else c.goldAntique.copy(alpha = 0.2f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isCompleted) NeonGreen else c.textMid,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(Space.xs))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = c.textHigh
                    )
                }
                Spacer(modifier = Modifier.height(Space.xxs))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textMid
                )
                Spacer(modifier = Modifier.height(Space.xxs))
                Text(
                    text = "+$reward WEED",
                    style = MaterialTheme.typography.labelLarge.copy(fontFamily = IbmPlexMonoFontFamily),
                    color = c.accent
                )
            }

            Spacer(modifier = Modifier.width(Space.sm))

            if (isClaimed) {
                Text(
                    text = if (lang == "ru") "ПОЛУЧЕНО" else "CLAIMED",
                    style = MaterialTheme.typography.labelMedium,
                    color = c.textMid,
                    modifier = Modifier
                        .background(c.ink, CyberShapes.chip)
                        .padding(horizontal = Space.sm, vertical = Space.xs)
                )
            } else if (isCompleted) {
                CyberActionButton(
                    text = if (lang == "ru") "ЗАБРАТЬ" else "CLAIM",
                    onClick = onClaim
                )
            } else {
                Text(
                    text = if (lang == "ru") "В ПРОЦЕССЕ" else "IN PROGRESS",
                    style = MaterialTheme.typography.labelMedium,
                    color = c.textMid,
                    modifier = Modifier
                        .background(c.ink, CyberShapes.chip)
                        .padding(horizontal = Space.sm, vertical = Space.xs)
                )
            }
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
    val c = Cyber.colors
    CyberPanel(
        borderColor = if (isActive) NeonMagenta.copy(alpha = 0.55f) else c.goldAntique.copy(alpha = 0.2f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isActive) NeonMagenta else c.textHigh
                )
                Spacer(modifier = Modifier.height(Space.xxs))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textMid
                )
            }

            Spacer(modifier = Modifier.width(Space.md))

            if (isActive) {
                Text(
                    text = if (lang == "ru") "АКТИВНО" else "ACTIVE",
                    style = MaterialTheme.typography.labelLarge,
                    color = NeonMagenta,
                    modifier = Modifier
                        .background(NeonMagenta.copy(alpha = 0.15f), CyberShapes.chip)
                        .padding(horizontal = Space.sm, vertical = Space.xs)
                )
            } else {
                CyberActionButton(
                    text = costText,
                    onClick = onBuy,
                    secondary = true
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
    val c = Cyber.colors
    Card(
        modifier = modifier
            .heightIn(min = 120.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = c.panelRaised),
        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.35f)),
        shape = CyberShapes.control
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Space.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = c.textHigh,
                textAlign = TextAlign.Center
            )
            Text(
                text = reward,
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = IbmPlexMonoFontFamily),
                color = c.accent,
                textAlign = TextAlign.Center
            )
            Text(
                text = price,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = IbmPlexMonoFontFamily,
                    fontWeight = FontWeight.Black
                ),
                color = CyberObsidian,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NeonCyan, CyberShapes.chip)
                    .padding(vertical = Space.xs),
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
    val c = Cyber.colors
    var step by remember { mutableIntStateOf(0) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Space.sm),
            colors = CardDefaults.cardColors(containerColor = c.panel),
            border = BorderStroke(2.dp, currentSkin.primaryColor),
            shape = CyberShapes.card
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Space.xl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (step) {
                    0 -> {
                        Text(
                            text = if (lang == "ru") "ЗАЩИЩЕННЫЙ ПЛАТЕЖ" else "SECURE GATEWAY CHECKOUT",
                            style = MaterialTheme.typography.titleMedium,
                            color = NeonCyan,
                            modifier = Modifier.padding(bottom = Space.md)
                        )
                        HorizontalDivider(color = c.goldAntique.copy(alpha = 0.25f))
                        Spacer(modifier = Modifier.height(Space.md))

                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = c.textHigh
                        )
                        Text(
                            text = "\$${String.format(Locale.US, "%.2f", priceUsd)} USD",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = IbmPlexMonoFontFamily,
                                fontWeight = FontWeight.Black
                            ),
                            color = c.accent,
                            modifier = Modifier.padding(vertical = Space.sm)
                        )

                        Spacer(modifier = Modifier.height(Space.sm))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(c.panelRaised, CyberShapes.control)
                                .padding(Space.md),
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
                                Spacer(modifier = Modifier.width(Space.sm))
                                Text(
                                    text = "VISA •••• 4012",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = IbmPlexMonoFontFamily),
                                    color = c.textHigh
                                )
                            }
                            Text(
                                text = "GPAY",
                                style = MaterialTheme.typography.labelLarge,
                                color = CyberObsidian,
                                modifier = Modifier
                                    .background(Color.White, CyberShapes.chip)
                                    .padding(horizontal = Space.xs, vertical = Space.xxs)
                            )
                        }

                        Spacer(modifier = Modifier.height(Space.lg))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Space.sm)
                        ) {
                            CyberActionButton(
                                text = if (lang == "ru") "ОТМЕНА" else "CANCEL",
                                onClick = onDismiss,
                                secondary = true,
                                modifier = Modifier.weight(1f)
                            )
                            CyberActionButton(
                                text = if (lang == "ru") "ОПЛАТИТЬ" else "CONFIRM & PAY",
                                onClick = { step = 1 },
                                modifier = Modifier.weight(1.4f)
                            )
                        }
                    }
                    1 -> {
                        Text(
                            text = if (lang == "ru") "АВТОРИЗАЦИЯ ТРАНЗАКЦИИ..." else "AUTHORIZING ENCRYPTED TUNNEL...",
                            style = MaterialTheme.typography.titleMedium,
                            color = NeonCyan
                        )
                        Spacer(modifier = Modifier.height(Space.xl))
                        CircularProgressIndicator(
                            color = NeonCyan,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(Space.xl))
                        Text(
                            text = if (lang == "ru") "Инициализация подписи контракта..." else "Securing financial quantum node consensus...",
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textMid,
                            textAlign = TextAlign.Center
                        )

                        LaunchedEffect(Unit) {
                            delay(2500)
                            step = 2
                        }
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = c.accent,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(Space.md))
                        Text(
                            text = if (lang == "ru") "ОПЛАТА УСПЕШНО ЗАВЕРШЕНА" else "TRANSACTION VERIFIED",
                            style = MaterialTheme.typography.titleMedium,
                            color = c.accent
                        )
                        Text(
                            text = if (lang == "ru") "Ваш баланс был обновлен!" else "Your cybernetic ledger has been synchronized!",
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textMid,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = Space.xs, bottom = Space.lg)
                        )
                        CyberNeonButton(
                            text = if (lang == "ru") "ОТЛИЧНО" else "ACKNOWLEDGE",
                            onClick = onSuccess,
                            color = NeonCyan,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
