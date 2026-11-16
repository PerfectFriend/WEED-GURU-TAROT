package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*
import com.example.viewmodel.TarotViewModel

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
    var showCheckoutSheet by remember { mutableStateOf<Map<String, Any>?>(null) }

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
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = Color(0xFFB0B0C5).copy(alpha = 0.2f))
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
                    HorizontalDivider(color = Color(0xFFB0B0C5).copy(alpha = 0.2f))
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
                    Text(
                        text = if (lang == "ru") "АВТОРИЗАЦИЯ ТРАНЗАКЦИИ..." else "AUTHORIZING ENCRYPTED TUNNEL...",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        fontFamily = RajdhaniFontFamily
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    CircularProgressIndicator(
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
