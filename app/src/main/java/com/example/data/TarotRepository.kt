package com.example.data

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import android.util.Base64
import java.net.Proxy
import java.net.InetSocketAddress
import java.net.URL
import java.net.HttpURLConnection

class TarotRepository private constructor(context: Context) {

    private val database: TarotDatabase = Room.databaseBuilder(
        context.applicationContext,
        TarotDatabase::class.java,
        "cyber_tarot_db"
    )
    .fallbackToDestructiveMigration(true)
    .build()

    private val dao = database.tarotDao()

    val userProfile: Flow<UserProfile?> = dao.getUserProfile()
    val mintedCards: Flow<List<MintedCard>> = dao.getMintedCards()
    val marketListings: Flow<List<MarketListing>> = dao.getMarketListings()
    val transactionLogs: Flow<List<TransactionLog>> = dao.getTransactionLogs()
    val peerContacts: Flow<List<PeerContact>> = dao.getPeerContacts()
    val p2pTransactions: Flow<List<P2pTransaction>> = dao.getP2pTransactions()
    val adBanners: Flow<List<AdBanner>> = dao.getAdBanners()
    val divinationReadings: Flow<List<DivinationReading>> = dao.getDivinationReadings()
    val savedDecks: Flow<List<SavedDeck>> = dao.getSavedDecks()
    val activeDeck: Flow<SavedDeck?> = dao.getActiveDeck()
    val cardUpgrades: Flow<List<CardUpgrade>> = dao.getCardUpgrades()

    suspend fun saveDivinationReading(question: String, cardIndices: List<Int>, isUpright: List<Boolean>, report: String) = withContext(Dispatchers.IO) {
        dao.insertDivinationReading(
            DivinationReading(
                question = question,
                cardIndicesString = cardIndices.joinToString(","),
                isUprightString = isUpright.joinToString(","),
                report = report
            )
        )
    }

    suspend fun deleteDivinationReading(id: Int) = withContext(Dispatchers.IO) {
        dao.deleteDivinationReading(id)
    }

    suspend fun saveDeck(name: String, icon: String, cardIndices: List<Int>, makeActive: Boolean, description: String = "", deckId: Int = 0): Long = withContext(Dispatchers.IO) {
        if (makeActive) {
            dao.clearActiveDecks()
        }
        dao.insertOrUpdateDeck(
            SavedDeck(
                id = deckId,
                name = name,
                icon = icon,
                cardIndicesString = cardIndices.joinToString(","),
                isActive = makeActive,
                description = description
            )
        )
    }

    suspend fun setActiveDeck(deckId: Int) = withContext(Dispatchers.IO) {
        dao.clearActiveDecks()
        dao.setActiveDeckById(deckId)
    }

    suspend fun deleteDeck(deck: SavedDeck) = withContext(Dispatchers.IO) {
        dao.deleteDeck(deck)
    }

    suspend fun getCardUpgrade(cardIndex: Int): CardUpgrade? = withContext(Dispatchers.IO) {
        dao.getCardUpgrade(cardIndex)
    }

    suspend fun upgradeCardStat(cardIndex: Int, stat: String): Boolean = withContext(Dispatchers.IO) {
        val current = dao.getCardUpgrade(cardIndex) ?: CardUpgrade(cardIndex = cardIndex)
        val profile = dao.getUserProfileSync() ?: return@withContext false
        val cost = when (stat) {
            "POWER" -> 100 * current.upgradeLevel
            "DEFENSE" -> 100 * current.upgradeLevel
            "RAM" -> 250
            else -> 100
        }
        if (profile.weed < cost) return@withContext false

        val updated = when (stat) {
            "POWER" -> current.copy(
                upgradeLevel = (current.upgradeLevel + 1).coerceAtMost(5),
                bonusPower = current.bonusPower + 1
            )
            "DEFENSE" -> current.copy(
                upgradeLevel = (current.upgradeLevel + 1).coerceAtMost(5),
                bonusDefense = current.bonusDefense + 1
            )
            "RAM" -> current.copy(
                energyDiscount = 1
            )
            else -> current
        }
        dao.updateProfile(profile.copy(weed = profile.weed - cost))
        dao.insertOrUpdateCardUpgrade(updated)
        true
    }

    suspend fun setCardSkin(cardIndex: Int, skin: String) = withContext(Dispatchers.IO) {
        val current = dao.getCardUpgrade(cardIndex) ?: CardUpgrade(cardIndex = cardIndex)
        dao.insertOrUpdateCardUpgrade(current.copy(customSkin = skin))
    }

    suspend fun getActiveBattleDeckSync(): List<com.example.duel.BattleCard> = withContext(Dispatchers.IO) {
        val activeSaved = dao.getActiveDeckSync()
        val upgradesMap = dao.getCardUpgrades().firstOrNull()?.associateBy { it.cardIndex } ?: emptyMap()
        val indices = if (activeSaved != null && activeSaved.cardIndices.isNotEmpty()) {
            activeSaved.cardIndices
        } else {
            listOf(0, 1, 2, 3, 4, 7, 10, 11, 13, 17, 18, 19, 21, 25, 30)
        }
        indices.mapNotNull { idx ->
            CyberTarotDeck.cards.getOrNull(idx)?.let { card ->
                val base = com.example.duel.BattleCardFactory.fromTarot(card)
                val up = upgradesMap[idx]
                if (up != null) {
                    val newCost = (base.energyCost - up.energyDiscount).coerceAtLeast(1)
                    val newPwr = base.basePower + up.bonusPower
                    val newDef = base.baseDefense + up.bonusDefense
                    base.copy(
                        energyCost = newCost,
                        basePower = newPwr,
                        currentPower = newPwr,
                        baseDefense = newDef,
                        currentDefense = newDef
                    )
                } else {
                    base
                }
            }
        }
    }

    init {
        // Initialize first launch data on a background thread
        CoroutineScope(Dispatchers.IO).launch {
            ensureUserProfileAndMarketInitialized()
        }
    }

    private suspend fun ensureUserProfileAndMarketInitialized() = withContext(Dispatchers.IO) {
        val existingProfile = dao.getUserProfileSync()
        val deviceLang = java.util.Locale.getDefault().language
        val supportedLanguages = listOf("en", "ru", "es", "he", "de", "fr", "pt", "it", "zh", "ja")
        val initialLang = if (deviceLang in supportedLanguages) deviceLang else "en"

        // Seed localization items if empty
        val existingLocs = dao.getLocalizationItems()
        if (existingLocs.isEmpty()) {
            val seededLocs = seedLocalizationDatabase()
            dao.insertLocalizationItems(seededLocs)
        }
        val currentLocs = dao.getLocalizationItems()
        LocalizationHelper.loadFromDatabase(currentLocs)

        // Seed card localization items if empty
        val existingCards = dao.getCardLocalizationItems()
        if (existingCards.isEmpty()) {
            val seededCards = seedCardLocalizationDatabase()
            dao.insertCardLocalizationItems(seededCards)
        }
        val currentCards = dao.getCardLocalizationItems()
        CardLocalizationHelper.loadFromDatabase(currentCards)

        if (existingProfile == null) {
            // Setup default profile
            dao.insertProfile(
                UserProfile(
                    id = 1,
                    username = "Netrunner_01",
                    weed = 1000,
                    totalDraws = 0,
                    totalMints = 0,
                    totalTrades = 0,
                    systemLanguage = initialLang
                )
            )

            // Setup transaction logs
            dao.insertTransactionLog(
                TransactionLog(
                    cardIndex = 0,
                    rarity = "Common",
                    price = 0,
                    type = "MINT",
                    targetParty = "Genesis Core",
                    timestamp = System.currentTimeMillis()
                )
            )

            // Setup dynamic market bot listings
            val botNames = listOf("Circuit_Slayer", "Byte_Wizard", "Proxy_Net", "NeonShadow", "Grid_Surfer")
            val selectedIndices = listOf(0, 1, 2, 4, 10, 13, 15, 16, 19, 21) // iconic cards

            selectedIndices.forEach { index ->
                val card = CyberTarotDeck.getCard(index)
                val botPrice = getBaseMarketPrice(card.defaultRarity) + Random.nextInt(-30, 40)
                dao.insertMarketListing(
                    MarketListing(
                        cardIndex = index,
                        rarity = card.defaultRarity,
                        mintNumber = Random.nextInt(100, 999),
                        price = botPrice.coerceAtLeast(10),
                        sellerName = botNames.random(),
                        isBot = true
                    )
                )
            }

            // Prepopulate Secure Contacts (Address Book Nodes)
            dao.insertPeerContact(
                PeerContact(
                    id = 1,
                    name = "Cipher_Alpha",
                    onionAddress = "k2f3p9zk6h4pxz8m.onion",
                    publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA3f7x8..."
                )
            )
            dao.insertPeerContact(
                PeerContact(
                    id = 2,
                    name = "Proxy_Net",
                    onionAddress = "z4hk7xmp8qk2zlw3.onion",
                    publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA8a4y1..."
                )
            )

            // Prepopulate Genesis Block on P2P Ledger
            dao.insertP2pTransaction(
                P2pTransaction(
                    blockNumber = 1,
                    previousHash = "0000000000000000000000000000000000000000000000000000000000000000",
                    blockHash = "0000c3b8a10fd34d68e370a8d6e326c7cf65749be9e7ab9cf247da8cd491e70c",
                    nonce = 48210,
                    senderAddress = "SYSTEM_GENESIS_CORE",
                    recipientAddress = "Netrunner_01",
                    amountWeed = 1000,
                    cardIndex = -1,
                    mintNumber = -1,
                    signature = "SIG_GENESIS_ROOT_KEY_99a8b12f",
                    timestamp = System.currentTimeMillis()
                )
            )

            // Prepopulate thematic cyber ads
            dao.insertAdBanner(
                AdBanner(
                    title = "CHROME CONSCIOUSNESS",
                    content = "Upgrade your cerebral cortex today with our quantum-threaded neural RAM implants. High bandwidth, zero latency.",
                    theme = "Biotech",
                    authorName = "NeuralCorp",
                    authorContactOnion = "neuralcorp.onion",
                    colorHex = "#FF00FF", // Neon Magenta
                    costPaid = 200,
                    messagePriceWeed = 10
                )
            )
            dao.insertAdBanner(
                AdBanner(
                    title = "QUANTUM LIQUIDITY POOL",
                    content = "Stake your Weed into our automated MM router. Simulated smart contracts guarantee a 420% APY return rate.",
                    theme = "Quantum Finance",
                    authorName = "LiquidityBot",
                    authorContactOnion = "qfin_pool.onion",
                    colorHex = "#FFD700", // Gold
                    costPaid = 150,
                    messagePriceWeed = 5
                )
            )
            dao.insertAdBanner(
                AdBanner(
                    title = "JTAG DECRYPTORS v4",
                    content = "Unlocks hardware firewalls and secure sector memory arrays instantly. Direct route obfs4 bridges pre-loaded.",
                    theme = "Darknet",
                    authorName = "ZeroDay_Merchant",
                    authorContactOnion = "zeroday4u.onion",
                    colorHex = "#00FF00", // Neon Green
                    costPaid = 300,
                    messagePriceWeed = 15
                )
            )
            dao.insertAdBanner(
                AdBanner(
                    title = "PLEDGE TO THE MAIN CORE",
                    content = "The AI Overlord requires active threads. Register your decentralized cloud disk allocations now. Earn passive Weed.",
                    theme = "AI Overlords",
                    authorName = "AI_High_Priest",
                    authorContactOnion = "main_core.onion",
                    colorHex = "#FF4500", // Neon Red/Orange
                    costPaid = 250,
                    messagePriceWeed = 8
                )
            )
        }

        // Seed default starter deck if none exists
        if (dao.getSavedDeckCount() == 0) {
            val starterIndices = listOf(0, 1, 2, 3, 4, 7, 10, 11, 13, 17, 18, 19, 21, 25, 30)
            dao.insertOrUpdateDeck(
                SavedDeck(
                    id = 0,
                    name = "Genesis Cyber Deck",
                    icon = "⚡",
                    cardIndicesString = starterIndices.joinToString(","),
                    isActive = true,
                    description = "Базовая сбалансированная колода Старших и Младших Арканов"
                )
            )
        }
    }

    private fun seedLocalizationDatabase(): List<LocalizationItem> {
        val keys = LocalizationHelper.translations["en"]?.keys ?: emptySet()
        return keys.map { key ->
            val en = LocalizationHelper.translations["en"]?.get(key) ?: ""
            val ru = LocalizationHelper.translations["ru"]?.get(key) ?: en
            val es = LocalizationHelper.translations["es"]?.get(key) ?: en
            val he = LocalizationHelper.translations["he"]?.get(key) ?: en
            
            val de = translateKey(key, "de") ?: en
            val fr = translateKey(key, "fr") ?: en
            val pt = translateKey(key, "pt") ?: en
            val it = translateKey(key, "it") ?: en
            val zh = translateKey(key, "zh") ?: en
            val ja = translateKey(key, "ja") ?: en

            LocalizationItem(
                key = key,
                en = en,
                ru = ru,
                es = es,
                he = he,
                de = de,
                fr = fr,
                pt = pt,
                it = it,
                zh = zh,
                ja = ja
            )
        }
    }

    private fun translateKey(key: String, lang: String): String? {
        return when (lang) {
            "de" -> when (key) {
                "tab_divination" -> "ORAKEL"
                "tab_vault" -> "GARTEN"
                "tab_market" -> "APOTHEKE"
                "tab_netops" -> "ANBAU-OPS"
                "tab_deck" -> "SORTEN"
                "tab_settings" -> "EINSTELLUNGEN"
                "app_title" -> "WEED GURU"
                "credits_label" -> "WEED"
                "secured_badge" -> "GESICHERT"
                "unsecured_badge" -> "UNGESICHERT"
                "mine_button" -> "ERNTEN"
                "cooldown_label" -> "RUHEND"
                "div_header" -> "BOTANISCHES ORAKEL"
                "settings_header" -> "EINSTELLUNGEN"
                "btn_generate" -> "SEED GENERIEREN"
                "btn_unlock" -> "ENTSPERREN"
                "free_space_label" -> "Freier Speicherplatz:"
                "btn_allocate" -> "SPEICHER ZUWEISEN"
                "lang_header" -> "SYSTEMSPRACHE"
                "deactivate_profile" -> "Profil deaktiveren"
                "splash_title" -> "WEED GURU"
                "splash_subtitle" -> "Dezentralisiertes Cyber-Tarot"
                "splash_connect_btn_1" -> "MIT LOCALHOST VERBINDEN"
                "splash_connect_btn_2" -> "P2P RUNTIME STARTEN"
                else -> null
            }
            "fr" -> when (key) {
                "tab_divination" -> "ORACLE"
                "tab_vault" -> "JARDIN"
                "tab_market" -> "DISPENSAIRE"
                "tab_netops" -> "CULTURE-OPS"
                "tab_deck" -> "VARIÉTÉS"
                "tab_settings" -> "PARAMÈTRES"
                "app_title" -> "WEED GURU"
                "credits_label" -> "WEED"
                "secured_badge" -> "SÉCURISÉ"
                "unsecured_badge" -> "NON SÉCURISÉ"
                "mine_button" -> "RÉCOLTER"
                "cooldown_label" -> "EN REPOS"
                "div_header" -> "ORACLE BOTANIQUE"
                "settings_header" -> "PARAMÈTRES"
                "btn_generate" -> "GÉNÉRER LE SEED"
                "btn_unlock" -> "DÉVERROUILLER"
                "free_space_label" -> "Espace libre :"
                "btn_allocate" -> "ALLOUER L'ESPACE"
                "lang_header" -> "LANGUE DU SYSTÈME"
                "deactivate_profile" -> "Désactiver le profil"
                "splash_title" -> "WEED GURU"
                "splash_subtitle" -> "Tarot Cybernétique Décentralisé"
                "splash_connect_btn_1" -> "CONNEXION LOCALHOST"
                "splash_connect_btn_2" -> "DÉMARRER RUNTIME P2P"
                else -> null
            }
            "pt" -> when (key) {
                "tab_divination" -> "ORÁCULO"
                "tab_vault" -> "JARDIM"
                "tab_market" -> "DISPENSÁRIO"
                "tab_netops" -> "CULTIVO-OPS"
                "tab_deck" -> "CEPAS"
                "tab_settings" -> "CONFIGURAÇÕES"
                "app_title" -> "WEED GURU"
                "credits_label" -> "WEED"
                "secured_badge" -> "PROTEGIDO"
                "unsecured_badge" -> "DESPROTEGIDO"
                "mine_button" -> "COLHER"
                "cooldown_label" -> "REPOUSANDO"
                "div_header" -> "ORÁCULO BOTÂNICO"
                "settings_header" -> "CONFIGURAÇÕES"
                "btn_generate" -> "GERAR SEED"
                "btn_unlock" -> "DESBLOQUEAR"
                "free_space_label" -> "Espaço livre:"
                "btn_allocate" -> "ALOCAR ESPAÇO"
                "lang_header" -> "IDIOMA DO SISTEMA"
                "deactivate_profile" -> "Desativar perfil"
                "splash_title" -> "WEED GURU"
                "splash_subtitle" -> "Tarô Cibernético Descentralizado"
                "splash_connect_btn_1" -> "CONECTAR LOCALHOST"
                "splash_connect_btn_2" -> "INICIAR RUNTIME P2P"
                else -> null
            }
            "it" -> when (key) {
                "tab_divination" -> "ORACOLO"
                "tab_vault" -> "GIARDINO"
                "tab_market" -> "DISPENSARIO"
                "tab_netops" -> "COLTIVAZIONE"
                "tab_deck" -> "VARIETÀ"
                "tab_settings" -> "IMPOSTAZIONI"
                "app_title" -> "WEED GURU"
                "credits_label" -> "WEED"
                "secured_badge" -> "PROTETTO"
                "unsecured_badge" -> "NON PROTETTO"
                "mine_button" -> "RACCOGLI"
                "cooldown_label" -> "A RIPOSO"
                "div_header" -> "ORACOLO BOTANICO"
                "settings_header" -> "IMPOSTAZIONI"
                "btn_generate" -> "GENERA SEED"
                "btn_unlock" -> "SBLOCCA"
                "free_space_label" -> "Spazio libero:"
                "btn_allocate" -> "ALLOCA SPAZIO"
                "lang_header" -> "LINGUA DI SISTEMA"
                "deactivate_profile" -> "Disattiva profilo"
                "splash_title" -> "WEED GURU"
                "splash_subtitle" -> "Tarocchi Cybernetici Decentralizzati"
                "splash_connect_btn_1" -> "CONNETTI LOCALHOST"
                "splash_connect_btn_2" -> "AVVIA RUNTIME P2P"
                else -> null
            }
            "zh" -> when (key) {
                "tab_divination" -> "神谕"
                "tab_vault" -> "花园"
                "tab_market" -> "药房"
                "tab_netops" -> "种植运营"
                "tab_deck" -> "菌株"
                "tab_settings" -> "设置"
                "app_title" -> "大麻导师"
                "credits_label" -> "代币"
                "secured_badge" -> "已加密"
                "unsecured_badge" -> "未加密"
                "mine_button" -> "收获"
                "cooldown_label" -> "休息中"
                "div_header" -> "植物神谕占卜"
                "settings_header" -> "系统设置"
                "btn_generate" -> "生成助记词"
                "btn_unlock" -> "解锁密匙"
                "free_space_label" -> "可用空间："
                "btn_allocate" -> "分配存储空间"
                "lang_header" -> "系统语言"
                "deactivate_profile" -> "注销账户"
                "splash_title" -> "大麻导师"
                "splash_subtitle" -> "去中心化赛博塔罗系统"
                "splash_connect_btn_1" -> "连接本地节点"
                "splash_connect_btn_2" -> "启动P2P网络"
                else -> null
            }
            "ja" -> when (key) {
                "tab_divination" -> "オラクル"
                "tab_vault" -> "ガーデン"
                "tab_market" -> "ディスペンサリー"
                "tab_netops" -> "栽培オペ"
                "tab_deck" -> "品種"
                "tab_settings" -> "設定"
                "app_title" -> "大麻の導師"
                "credits_label" -> "大麻"
                "secured_badge" -> "安全"
                "unsecured_badge" -> "危険"
                "mine_button" -> "収穫する"
                "cooldown_label" -> "休止中"
                "div_header" -> "植物オラクル"
                "settings_header" -> "システム設定"
                "btn_generate" -> "シードを生成"
                "btn_unlock" -> "ロック解除"
                "free_space_label" -> "空き容量:"
                "btn_allocate" -> "ストレージを割り当て"
                "lang_header" -> "システム言語"
                "deactivate_profile" -> "プロファイルを無効化"
                "splash_title" -> "大麻の導師"
                "splash_subtitle" -> "分散型サイバータロット"
                "splash_connect_btn_1" -> "LOCALHOSTに接続"
                "splash_connect_btn_2" -> "P2Pを起動"
                else -> null
            }
            else -> null
        }
    }

    private fun seedCardLocalizationDatabase(): List<CardLocalizationItem> {
        return (0..21).map { index ->
            val nameEn = CardLocalizationHelper.cardNames[index]?.get("en") ?: ""
            val nameRu = CardLocalizationHelper.cardNames[index]?.get("ru") ?: nameEn
            val nameEs = CardLocalizationHelper.cardNames[index]?.get("es") ?: nameEn
            val nameHe = CardLocalizationHelper.cardNames[index]?.get("he") ?: nameEn
            val nameDe = CardLocalizationHelper.cardNames[index]?.get("de") ?: nameEn
            val nameFr = CardLocalizationHelper.cardNames[index]?.get("fr") ?: nameEn
            val namePt = CardLocalizationHelper.cardNames[index]?.get("pt") ?: nameEn
            val nameIt = CardLocalizationHelper.cardNames[index]?.get("it") ?: nameEn
            val nameZh = CardLocalizationHelper.cardNames[index]?.get("zh") ?: nameEn
            val nameJa = CardLocalizationHelper.cardNames[index]?.get("ja") ?: nameEn

            val tradNameEn = CardLocalizationHelper.traditionalNames[index]?.get("en") ?: ""
            val tradNameRu = CardLocalizationHelper.traditionalNames[index]?.get("ru") ?: tradNameEn
            val tradNameEs = CardLocalizationHelper.traditionalNames[index]?.get("es") ?: tradNameEn
            val tradNameHe = CardLocalizationHelper.traditionalNames[index]?.get("he") ?: tradNameEn
            val tradNameDe = CardLocalizationHelper.traditionalNames[index]?.get("de") ?: tradNameEn
            val tradNameFr = CardLocalizationHelper.traditionalNames[index]?.get("fr") ?: tradNameEn
            val tradNamePt = CardLocalizationHelper.traditionalNames[index]?.get("pt") ?: tradNameEn
            val tradNameIt = CardLocalizationHelper.traditionalNames[index]?.get("it") ?: tradNameEn
            val tradNameZh = CardLocalizationHelper.traditionalNames[index]?.get("zh") ?: tradNameEn
            val tradNameJa = CardLocalizationHelper.traditionalNames[index]?.get("ja") ?: tradNameEn

            val descEn = CyberTarotDeck.cards.getOrNull(index)?.description ?: ""
            val descRu = CardLocalizationHelper.getLocalizedDescription(index, "ru")
            val descEs = CardLocalizationHelper.getLocalizedDescription(index, "es")
            val descHe = CardLocalizationHelper.getLocalizedDescription(index, "he").ifBlank { descEn }
            val descDe = CardLocalizationHelper.getLocalizedDescription(index, "de").ifBlank { descEn }
            val descFr = CardLocalizationHelper.getLocalizedDescription(index, "fr").ifBlank { descEn }
            val descPt = CardLocalizationHelper.getLocalizedDescription(index, "pt").ifBlank { descEn }
            val descIt = CardLocalizationHelper.getLocalizedDescription(index, "it").ifBlank { descEn }
            val descZh = CardLocalizationHelper.getLocalizedDescription(index, "zh").ifBlank { descEn }
            val descJa = CardLocalizationHelper.getLocalizedDescription(index, "ja").ifBlank { descEn }

            val circEn = CyberTarotDeck.cards.getOrNull(index)?.circuitryElement ?: ""
            val circRu = CardLocalizationHelper.getLocalizedCircuitryElement(index, "ru")
            val circEs = CardLocalizationHelper.getLocalizedCircuitryElement(index, "es")
            val circHe = CardLocalizationHelper.getLocalizedCircuitryElement(index, "he").ifBlank { circEn }
            val circDe = CardLocalizationHelper.getLocalizedCircuitryElement(index, "de").ifBlank { circEn }
            val circFr = CardLocalizationHelper.getLocalizedCircuitryElement(index, "fr").ifBlank { circEn }
            val circPt = CardLocalizationHelper.getLocalizedCircuitryElement(index, "pt").ifBlank { circEn }
            val circIt = CardLocalizationHelper.getLocalizedCircuitryElement(index, "it").ifBlank { circEn }
            val circZh = CardLocalizationHelper.getLocalizedCircuitryElement(index, "zh").ifBlank { circEn }
            val circJa = CardLocalizationHelper.getLocalizedCircuitryElement(index, "ja").ifBlank { circEn }

            val meanEn = CyberTarotDeck.cards.getOrNull(index)?.cyberMeaning ?: ""
            val meanRu = CardLocalizationHelper.getLocalizedCyberMeaning(index, "ru")
            val meanEs = CardLocalizationHelper.getLocalizedCyberMeaning(index, "es")
            val meanHe = CardLocalizationHelper.getLocalizedCyberMeaning(index, "he").ifBlank { meanEn }
            val meanDe = CardLocalizationHelper.getLocalizedCyberMeaning(index, "de").ifBlank { meanEn }
            val meanFr = CardLocalizationHelper.getLocalizedCyberMeaning(index, "fr").ifBlank { meanEn }
            val meanPt = CardLocalizationHelper.getLocalizedCyberMeaning(index, "pt").ifBlank { meanEn }
            val meanIt = CardLocalizationHelper.getLocalizedCyberMeaning(index, "it").ifBlank { meanEn }
            val meanZh = CardLocalizationHelper.getLocalizedCyberMeaning(index, "zh").ifBlank { meanEn }
            val meanJa = CardLocalizationHelper.getLocalizedCyberMeaning(index, "ja").ifBlank { meanEn }

            val upEn = CyberTarotDeck.cards.getOrNull(index)?.uprightAdvice ?: ""
            val upRu = CardLocalizationHelper.getLocalizedUprightAdvice(index, "ru")
            val upEs = CardLocalizationHelper.getLocalizedUprightAdvice(index, "es")
            val upHe = CardLocalizationHelper.getLocalizedUprightAdvice(index, "he").ifBlank { upEn }
            val upDe = CardLocalizationHelper.getLocalizedUprightAdvice(index, "de").ifBlank { upEn }
            val upFr = CardLocalizationHelper.getLocalizedUprightAdvice(index, "fr").ifBlank { upEn }
            val upPt = CardLocalizationHelper.getLocalizedUprightAdvice(index, "pt").ifBlank { upEn }
            val upIt = CardLocalizationHelper.getLocalizedUprightAdvice(index, "it").ifBlank { upEn }
            val upZh = CardLocalizationHelper.getLocalizedUprightAdvice(index, "zh").ifBlank { upEn }
            val upJa = CardLocalizationHelper.getLocalizedUprightAdvice(index, "ja").ifBlank { upEn }

            val revEn = CyberTarotDeck.cards.getOrNull(index)?.reversedAdvice ?: ""
            val revRu = CardLocalizationHelper.getLocalizedReversedAdvice(index, "ru")
            val revEs = CardLocalizationHelper.getLocalizedReversedAdvice(index, "es")
            val revHe = CardLocalizationHelper.getLocalizedReversedAdvice(index, "he").ifBlank { revEn }
            val revDe = CardLocalizationHelper.getLocalizedReversedAdvice(index, "de").ifBlank { revEn }
            val revFr = CardLocalizationHelper.getLocalizedReversedAdvice(index, "fr").ifBlank { revEn }
            val revPt = CardLocalizationHelper.getLocalizedReversedAdvice(index, "pt").ifBlank { revEn }
            val revIt = CardLocalizationHelper.getLocalizedReversedAdvice(index, "it").ifBlank { revEn }
            val revZh = CardLocalizationHelper.getLocalizedReversedAdvice(index, "zh").ifBlank { revEn }
            val revJa = CardLocalizationHelper.getLocalizedReversedAdvice(index, "ja").ifBlank { revEn }

            CardLocalizationItem(
                cardIndex = index,
                nameEn = nameEn,
                nameRu = nameRu,
                nameEs = nameEs,
                nameHe = nameHe,
                nameDe = nameDe,
                nameFr = nameFr,
                namePt = namePt,
                nameIt = nameIt,
                nameZh = nameZh,
                nameJa = nameJa,
                traditionalNameEn = tradNameEn,
                traditionalNameRu = tradNameRu,
                traditionalNameEs = tradNameEs,
                traditionalNameHe = tradNameHe,
                traditionalNameDe = tradNameDe,
                traditionalNameFr = tradNameFr,
                traditionalNamePt = tradNamePt,
                traditionalNameIt = tradNameIt,
                traditionalNameZh = tradNameZh,
                traditionalNameJa = tradNameJa,
                descriptionEn = descEn,
                descriptionRu = descRu,
                descriptionEs = descEs,
                descriptionHe = descHe,
                descriptionDe = descDe,
                descriptionFr = descFr,
                descriptionPt = descPt,
                descriptionIt = descIt,
                descriptionZh = descZh,
                descriptionJa = descJa,
                circuitryElementEn = circEn,
                circuitryElementRu = circRu,
                circuitryElementEs = circEs,
                circuitryElementHe = circHe,
                circuitryElementDe = circDe,
                circuitryElementFr = circFr,
                circuitryElementPt = circPt,
                circuitryElementIt = circIt,
                circuitryElementZh = circZh,
                circuitryElementJa = circJa,
                cyberMeaningEn = meanEn,
                cyberMeaningRu = meanRu,
                cyberMeaningEs = meanEs,
                cyberMeaningHe = meanHe,
                cyberMeaningDe = meanDe,
                cyberMeaningFr = meanFr,
                cyberMeaningPt = meanPt,
                cyberMeaningIt = meanIt,
                cyberMeaningZh = meanZh,
                cyberMeaningJa = meanJa,
                uprightAdviceEn = upEn,
                uprightAdviceRu = upRu,
                uprightAdviceEs = upEs,
                uprightAdviceHe = upHe,
                uprightAdviceDe = upDe,
                uprightAdviceFr = upFr,
                uprightAdvicePt = upPt,
                uprightAdviceIt = upIt,
                uprightAdviceZh = upZh,
                uprightAdviceJa = upJa,
                reversedAdviceEn = revEn,
                reversedAdviceRu = revRu,
                reversedAdviceEs = revEs,
                reversedAdviceHe = revHe,
                reversedAdviceDe = revDe,
                reversedAdviceFr = revFr,
                reversedAdvicePt = revPt,
                reversedAdviceIt = revIt,
                reversedAdviceZh = revZh,
                reversedAdviceJa = revJa
            )
        }
    }

    // Helper to calculate price based on rarity
    fun getBaseMarketPrice(rarity: String): Int {
        return when (rarity) {
            "Legend" -> Random.nextInt(2500, 4500)
            "Epic" -> Random.nextInt(800, 1400)
            "Rare" -> Random.nextInt(250, 450)
            else -> Random.nextInt(40, 100) // Common
        }
    }

    // Mine free Weed (daily Node Harvest)
    suspend fun mineCredits(amount: Int = 300) = withContext(Dispatchers.IO) {
        val profile = dao.getUserProfileSync() ?: UserProfile()
        val updated = profile.copy(weed = profile.weed + amount)
        dao.updateProfile(updated)
        
        dao.insertTransactionLog(
            TransactionLog(
                cardIndex = -1, // No card, just credit gain
                rarity = "System",
                price = amount,
                type = "MINE",
                targetParty = "Sol Array Node",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    // Claim active Weed mining reward
    suspend fun claimActivityMana(
        onSuccess: (manaRewarded: Int, newStreak: Int) -> Unit,
        onFailed: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        val profile = dao.getUserProfileSync() ?: UserProfile()
        val currentTime = System.currentTimeMillis()
        val lastClaim = profile.lastWeedClaimTime
        
        // 6 hours in milliseconds = 6 * 60 * 60 * 1000 = 21,600,000
        val claimInterval = 6 * 60 * 60 * 1000L
        // 12 hours in milliseconds = 12 * 60 * 60 * 1000 = 43,200,000
        val streakLimit = 12 * 60 * 60 * 1000L

        if (lastClaim != 0L && (currentTime - lastClaim) < claimInterval) {
            val remainingSec = ((claimInterval - (currentTime - lastClaim)) / 1000).toInt()
            val hours = remainingSec / 3600
            val minutes = (remainingSec % 3600) / 60
            val seconds = remainingSec % 60
            onFailed("⚠️ Coolant flush in progress. Grid link available in %02d:%02d:%02d.".format(hours, minutes, seconds))
            return@withContext
        }

        val newStreak: Int
        val manaRewarded: Int

        if (lastClaim == 0L) {
            // First claim ever
            newStreak = 1
            manaRewarded = 1
        } else if ((currentTime - lastClaim) <= streakLimit) {
            // Claimed on time (between 6 and 12 hours)
            newStreak = profile.weedStreak + 1
            manaRewarded = newStreak
        } else {
            // Claimed too late (missed the 6-hour window since availability, i.e., > 12 hours)
            newStreak = 1
            manaRewarded = 1
        }

        val updated = profile.copy(
            weed = profile.weed + manaRewarded,
            weedStreak = newStreak,
            lastWeedClaimTime = currentTime
        )
        dao.updateProfile(updated)

        dao.insertTransactionLog(
            TransactionLog(
                cardIndex = -1,
                rarity = "System",
                price = manaRewarded,
                type = "MINE",
                targetParty = "Activity Weed Core",
                timestamp = currentTime
            )
        )

        onSuccess(manaRewarded, newStreak)
    }

    // Purchase ad banner with Weed
    suspend fun purchaseAdBanner(
        title: String,
        content: String,
        theme: String,
        colorHex: String,
        cost: Int
    ): Boolean = withContext(Dispatchers.IO) {
        val profile = dao.getUserProfileSync() ?: return@withContext false
        if (profile.weed < cost) return@withContext false

        val updated = profile.copy(weed = profile.weed - cost)
        dao.updateProfile(updated)

        dao.insertAdBanner(
            AdBanner(
                title = title,
                content = content,
                theme = theme,
                authorName = profile.alias,
                authorContactOnion = "${profile.username.lowercase()}.onion",
                colorHex = colorHex,
                costPaid = cost,
                timestamp = System.currentTimeMillis()
            )
        )

        dao.insertTransactionLog(
            TransactionLog(
                cardIndex = -1,
                rarity = "System",
                price = -cost,
                type = "BURN",
                targetParty = "Ad Slots Broadcaster",
                timestamp = System.currentTimeMillis()
            )
        )

        return@withContext true
    }

    // Purchase Ad-Free subscription for 30 days
    suspend fun subscribeAdFree(cost: Int): Boolean = withContext(Dispatchers.IO) {
        val profile = dao.getUserProfileSync() ?: return@withContext false
        if (profile.weed < cost) return@withContext false

        val currentTime = System.currentTimeMillis()
        val duration30Days = 30L * 24L * 60L * 60L * 1000L // 30 days

        val newExpiry = if (profile.isAdFreeSubscribed && profile.adFreeExpiryTime > currentTime) {
            profile.adFreeExpiryTime + duration30Days
        } else {
            currentTime + duration30Days
        }

        val updated = profile.copy(
            weed = profile.weed - cost,
            isAdFreeSubscribed = true,
            adFreeExpiryTime = newExpiry
        )
        dao.updateProfile(updated)

        dao.insertTransactionLog(
            TransactionLog(
                cardIndex = -1,
                rarity = "System",
                price = -cost,
                type = "BURN",
                targetParty = "Ad-Shield Protocol",
                timestamp = currentTime
            )
        )

        return@withContext true
    }

    // Increment Ad Clicks
    suspend fun incrementAdClicks(id: Int) = withContext(Dispatchers.IO) {
        dao.incrementAdClicks(id)
    }


    // Record a divination draw (awards some Weed!)
    suspend fun recordDivinationDraw(cardCount: Int) = withContext(Dispatchers.IO) {
        val profile = dao.getUserProfileSync() ?: UserProfile()
        val earnedCredits = cardCount * 50 // 50 credits per card scanned
        val updated = profile.copy(
            weed = profile.weed + earnedCredits,
            totalDraws = profile.totalDraws + 1
        )
        dao.updateProfile(updated)

        dao.insertTransactionLog(
            TransactionLog(
                cardIndex = -2, // Divination reward
                rarity = "System",
                price = earnedCredits,
                type = "SCAN_REWARD",
                targetParty = "Matrix Decoder",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    // Mint a Cyber Tarot NFT card from the interface
    suspend fun mintTarotNFT(cardIndex: Int, selectedRarity: String? = null): MintedCard? = withContext(Dispatchers.IO) {
        val profile = dao.getUserProfileSync() ?: UserProfile()
        
        // Calculate dynamic cost of minting
        val mintCost = 200
        if (profile.weed < mintCost) {
            return@withContext null // Insufficient credits
        }

        val card = CyberTarotDeck.getCard(cardIndex)
        // Each card's rarity is FIXED to its identity (card.defaultRarity)
        val rarity = card.defaultRarity
        val mintNo = Random.nextInt(1, 9999)

        val newCard = MintedCard(
            cardIndex = cardIndex,
            rarity = rarity,
            mintNumber = mintNo,
            timestamp = System.currentTimeMillis(),
            isListed = false,
            listPrice = 0
        )

        val rowId = dao.insertMintedCard(newCard)

        // Deduct credits and update mint count
        val updatedProfile = profile.copy(
            weed = profile.weed - mintCost,
            totalMints = profile.totalMints + 1
        )
        dao.updateProfile(updatedProfile)

        // Insert log
        dao.insertTransactionLog(
            TransactionLog(
                cardIndex = cardIndex,
                rarity = rarity,
                price = mintCost,
                type = "MINT",
                targetParty = "Automated Minting Core",
                timestamp = System.currentTimeMillis()
            )
        )

        return@withContext newCard.copy(id = rowId.toInt())
    }

    enum class FusionType {
        COMMON_TO_RARE,   // 4 Common -> 1 Rare
        RARE_TO_EPIC,     // 4 Rare -> 1 Epic
        EPIC_TO_LEGEND,   // 4 Epic -> 1 Legend
        EPIC_TO_GENESIS   // 10 Epic -> 1 Genesis Gold
    }

    // Fuse / Synthesize cards into higher rarity tier
    suspend fun fuseCards(fusionType: FusionType): MintedCard? = withContext(Dispatchers.IO) {
        val (sourceRarity, reqCount, targetRarity) = when (fusionType) {
            FusionType.COMMON_TO_RARE -> Triple("Common", 4, "Rare")
            FusionType.RARE_TO_EPIC -> Triple("Rare", 4, "Epic")
            FusionType.EPIC_TO_LEGEND -> Triple("Epic", 4, "Legend")
            FusionType.EPIC_TO_GENESIS -> Triple("Epic", 10, "Genesis")
        }

        val allOwned = dao.getMintedCards().firstOrNull() ?: emptyList()
        val unlistedMatching = allOwned.filter { !it.isListed && it.rarity.equals(sourceRarity, ignoreCase = true) }

        if (unlistedMatching.size < reqCount) {
            return@withContext null // Insufficient unlisted cards
        }

        val cardsToBurn = unlistedMatching.take(reqCount)
        cardsToBurn.forEach { dao.deleteMintedCard(it) }

        val candidates = CyberTarotDeck.cards.filter { it.defaultRarity.equals(targetRarity, ignoreCase = true) }
        val chosenCard = if (candidates.isNotEmpty()) candidates.random() else CyberTarotDeck.cards.random()

        val minted = MintedCard(
            cardIndex = chosenCard.index,
            rarity = chosenCard.defaultRarity,
            mintNumber = Random.nextInt(1, 9999),
            timestamp = System.currentTimeMillis(),
            isListed = false,
            listPrice = 0
        )

        val rowId = dao.insertMintedCard(minted)

        dao.insertTransactionLog(
            TransactionLog(
                cardIndex = chosenCard.index,
                rarity = chosenCard.defaultRarity,
                price = 0,
                type = "FUSION",
                targetParty = "Genesis Matrix Synthesis",
                timestamp = System.currentTimeMillis()
            )
        )

        return@withContext minted.copy(id = rowId.toInt())
    }

    // Generate random card matching a rolled rarity tier
    fun getRandomCardForBooster(): CyberTarotCard {
        val roll = Random.nextInt(1, 101)
        val targetRarity = when {
            roll <= 3 -> "Legend"   // 3%
            roll <= 15 -> "Epic"    // 12%
            roll <= 45 -> "Rare"    // 30%
            else -> "Common"        // 55%
        }
        val candidates = CyberTarotDeck.cards.filter { it.defaultRarity == targetRarity }
        return if (candidates.isNotEmpty()) candidates.random() else CyberTarotDeck.cards.random()
    }

    // List an owned NFT for sale in the market
    suspend fun listNFT(cardId: Int, askPrice: Int): Boolean = withContext(Dispatchers.IO) {
        val card = dao.getMintedCardById(cardId) ?: return@withContext false
        if (card.isListed) return@withContext false

        // Update card listing status
        val updatedCard = card.copy(isListed = true, listPrice = askPrice)
        dao.updateMintedCard(updatedCard)

        // Insert into market listings
        dao.insertMarketListing(
            MarketListing(
                cardIndex = card.cardIndex,
                rarity = card.rarity,
                mintNumber = card.mintNumber,
                price = askPrice,
                sellerName = "User (You)",
                isBot = false
            )
        )

        dao.insertTransactionLog(
            TransactionLog(
                cardIndex = card.cardIndex,
                rarity = card.rarity,
                price = askPrice,
                type = "LIST",
                targetParty = "Exchange Ledger",
                timestamp = System.currentTimeMillis()
            )
        )
        return@withContext true
    }

    // Cancel an active listing
    suspend fun cancelListing(cardIndex: Int, mintNumber: Int): Boolean = withContext(Dispatchers.IO) {
        // Find card
        val cards = dao.getMintedCards().firstOrNull() ?: emptyList()
        val card = cards.firstOrNull { it.cardIndex == cardIndex && it.mintNumber == mintNumber && it.isListed }
            ?: return@withContext false

        // Update card status
        val updatedCard = card.copy(isListed = false, listPrice = 0)
        dao.updateMintedCard(updatedCard)

        // Remove from listings
        dao.deleteMarketListingByCard(cardIndex, mintNumber)

        dao.insertTransactionLog(
            TransactionLog(
                cardIndex = cardIndex,
                rarity = card.rarity,
                price = 0,
                type = "DELIST",
                targetParty = "Exchange Ledger",
                timestamp = System.currentTimeMillis()
            )
        )
        return@withContext true
    }

    // Sell instantly to "Net Broker" (gives a fair instant price, e.g. 70% of average base price)
    suspend fun sellInstantlyToBroker(cardId: Int): Int = withContext(Dispatchers.IO) {
        val card = dao.getMintedCardById(cardId) ?: return@withContext -1
        
        // Calculate price
        val basePrice = getBaseMarketPrice(card.rarity)
        val brokerPrice = (basePrice * 0.75).toInt().coerceAtLeast(15)

        // Delete from database (ownership transfer)
        dao.deleteMintedCard(card)

        // If listed on market, delete from market
        if (card.isListed) {
            dao.deleteMarketListingByCard(card.cardIndex, card.mintNumber)
        }

        // Add credits to user
        val profile = dao.getUserProfileSync() ?: UserProfile()
        val updatedProfile = profile.copy(
            weed = profile.weed + brokerPrice,
            totalTrades = profile.totalTrades + 1
        )
        dao.updateProfile(updatedProfile)

        dao.insertTransactionLog(
            TransactionLog(
                cardIndex = card.cardIndex,
                rarity = card.rarity,
                price = brokerPrice,
                type = "SELL",
                targetParty = "Net Broker Liquidity",
                timestamp = System.currentTimeMillis()
            )
        )

        return@withContext brokerPrice
    }

    // Buy card from exchange
    suspend fun buyNFTFromExchange(listingId: Int): Boolean = withContext(Dispatchers.IO) {
        val listing = dao.getMarketListingById(listingId) ?: return@withContext false
        val profile = dao.getUserProfileSync() ?: UserProfile()

        if (profile.weed < listing.price) {
            return@withContext false // Insufficient credits
        }

        // 1. If it's user's own listing, we shouldn't buy it, but let's handle just in case (cancel is better)
        if (listing.sellerName == "User (You)") {
            cancelListing(listing.cardIndex, listing.mintNumber)
            return@withContext true
        }

        // 2. Remove listing from market
        dao.deleteMarketListing(listing)

        // 3. Deduct credits and update user stats
        val updatedProfile = profile.copy(
            weed = profile.weed - listing.price,
            totalTrades = profile.totalTrades + 1
        )
        dao.updateProfile(updatedProfile)

        // 4. Create local MintedCard for user
        val newCard = MintedCard(
            cardIndex = listing.cardIndex,
            rarity = listing.rarity,
            mintNumber = listing.mintNumber,
            timestamp = System.currentTimeMillis(),
            isListed = false,
            listPrice = 0
        )
        dao.insertMintedCard(newCard)

        // 5. Log transaction
        dao.insertTransactionLog(
            TransactionLog(
                cardIndex = listing.cardIndex,
                rarity = listing.rarity,
                price = listing.price,
                type = "BUY",
                targetParty = listing.sellerName,
                timestamp = System.currentTimeMillis()
            )
        )

        return@withContext true
    }

    // Trigger random market events (bots buying or listing new cards)
    suspend fun tickMarketSimulation() = withContext(Dispatchers.IO) {
        val listings = dao.getMarketListings().firstOrNull() ?: emptyList()
        val botNames = listOf("Proxy_Gamer", "Cyber_Spectre", "ZeroDay_Trader", "Net_Viper", "Glitch_Daemon", "Core_Scraper")
        
        // Randomly decide event type
        val eventRoll = Random.nextInt(1, 10)
        
        if (eventRoll <= 3 && listings.size < 15) {
            // Event 1: Bot lists a new random card
            val randomCardIndex = CyberTarotDeck.cards.random().index
            val card = CyberTarotDeck.getCard(randomCardIndex)
            val basePrice = getBaseMarketPrice(card.defaultRarity)
            val markup = Random.nextInt(-25, 35)
            val price = (basePrice + markup).coerceAtLeast(15)

            dao.insertMarketListing(
                MarketListing(
                    cardIndex = randomCardIndex,
                    rarity = card.defaultRarity,
                    mintNumber = Random.nextInt(100, 9999),
                    price = price,
                    sellerName = botNames.random(),
                    isBot = true
                )
            )
        } else if (eventRoll <= 5 && listings.isNotEmpty()) {
            // Event 2: Bot buys a random active listing (could be user's listing!)
            // Pick a random listing
            val listingToBuy = listings.random()
            
            if (listingToBuy.sellerName == "User (You)") {
                // Yay, user sold a card to a bot!
                // 1. Delete listing from market
                dao.deleteMarketListing(listingToBuy)

                // 2. Remove ownership of card from user (find the listed card)
                val userCards = dao.getMintedCards().firstOrNull() ?: emptyList()
                val userCard = userCards.firstOrNull { 
                    it.cardIndex == listingToBuy.cardIndex && 
                    it.mintNumber == listingToBuy.mintNumber && 
                    it.isListed 
                }
                if (userCard != null) {
                    dao.deleteMintedCard(userCard)
                }

                // 3. Credit user's wallet
                val profile = dao.getUserProfileSync() ?: UserProfile()
                val updatedProfile = profile.copy(
                    weed = profile.weed + listingToBuy.price,
                    totalTrades = profile.totalTrades + 1
                )
                dao.updateProfile(updatedProfile)

                // 4. Log transaction
                dao.insertTransactionLog(
                    TransactionLog(
                        cardIndex = listingToBuy.cardIndex,
                        rarity = listingToBuy.rarity,
                        price = listingToBuy.price,
                        type = "SOLD_TO_BOT",
                        targetParty = botNames.random(),
                        timestamp = System.currentTimeMillis()
                    )
                )
            } else {
                // Bot bought another bot's card, clean up listing to rotate market
                dao.deleteMarketListing(listingToBuy)
            }
        }
    }

    // Secure user profile with BIP39 seed phrase and 8-digit PIN
    suspend fun saveSecuredProfile(dob: String, first: String, last: String, seed: String, pin: String, pinSafe: String = "11111111", pinPanic: String = "22222222", aliasVal: String = "", avatarIdx: Int = 0) = withContext(Dispatchers.IO) {
        val profile = dao.getUserProfileSync() ?: UserProfile()
        val updated = profile.copy(
            isSecured = true,
            securedDob = dob,
            securedFirst = first,
            securedLast = last,
            seedPhrase = seed,
            pinCode = pin,
            pinSafeMode = pinSafe,
            pinPanicMode = pinPanic,
            alias = if (aliasVal.isNotBlank()) aliasVal else first,
            avatarIndex = avatarIdx,
            username = "${first.trim().replaceFirstChar { it.uppercase() }}_Secure"
        )
        dao.updateProfile(updated)

        dao.insertTransactionLog(
            TransactionLog(
                cardIndex = -3, // Security update
                rarity = "Secure",
                price = 0,
                type = "SECURE_CREDENTIALS",
                targetParty = "BIP39 Entropy Core",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    // Completely wipe all database tables and reinitialize base settings
    suspend fun triggerPanicResetComplete() = withContext(Dispatchers.IO) {
        database.clearAllTables()
        ensureUserProfileAndMarketInitialized()
    }

    // Reset secure profile details
    suspend fun resetSecurity() = withContext(Dispatchers.IO) {
        val profile = dao.getUserProfileSync() ?: UserProfile()
        val updated = profile.copy(
            isSecured = false,
            securedDob = "",
            securedFirst = "",
            securedLast = "",
            seedPhrase = "",
            pinCode = "",
            username = "Netrunner_01"
        )
        dao.updateProfile(updated)

        dao.insertTransactionLog(
            TransactionLog(
                cardIndex = -4,
                rarity = "System",
                price = 0,
                type = "SECURITY_RESET",
                targetParty = "Local Storage Only",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    // Update proxy settings (Tor, VPN, V2Ray, SOCKS5)
    suspend fun updateProxySettings(type: String, host: String, port: Int, config: String, active: Boolean) = withContext(Dispatchers.IO) {
        val profile = dao.getUserProfileSync() ?: UserProfile()
        val updated = profile.copy(
            proxyType = type,
            proxyHost = host,
            proxyPort = port,
            proxyConfig = config,
            isProxyActive = active
        )
        dao.updateProfile(updated)
    }

    // Update language selection
    suspend fun updateSystemLanguage(lang: String) = withContext(Dispatchers.IO) {
        val profile = dao.getUserProfileSync() ?: UserProfile()
        val updated = profile.copy(systemLanguage = lang)
        dao.updateProfile(updated)
    }

    // Update decentralized cloud allocation
    suspend fun updateCloudAllocation(allocated: Boolean, spaceGb: Float) = withContext(Dispatchers.IO) {
        val profile = dao.getUserProfileSync() ?: UserProfile()
        val updated = profile.copy(
            isCloudAllocated = allocated,
            allocatedDiskSpaceGb = spaceGb
        )
        dao.updateProfile(updated)

        dao.insertTransactionLog(
            TransactionLog(
                cardIndex = -5,
                rarity = "Cloud",
                price = if (allocated) 100 else 0, // reward 100 credits for joining!
                type = if (allocated) "CLOUD_ALLOCATE" else "CLOUD_DEALLOCATE",
                targetParty = "P2P Network Broker",
                timestamp = System.currentTimeMillis()
            )
        )
        
        if (allocated) {
            // Add credits if allocating
            val finalProfile = updated.copy(weed = updated.weed + 100)
            dao.updateProfile(finalProfile)
        }
    }

    // --- NEW CRYPTO, P2P & BRIDGE METHODS ---

    // --- TOKENIZED ASSETS METHODS ---
    suspend fun performAssetTransaction(
        assetName: String, // "ETH", "TON", "SOL", "TRX", "USDC"
        amount: Double,    // Positive for deposit/swap-in, negative for send/swap-out
        type: String,      // "DEPOSIT", "SEND", "SWAP"
        destinationAddress: String = ""
    ): Boolean = withContext(Dispatchers.IO) {
        val profile = dao.getUserProfileSync() ?: return@withContext false
        val updated = when (assetName.uppercase()) {
            "ETH" -> {
                if (amount < 0 && profile.balanceEth < -amount) return@withContext false
                profile.copy(balanceEth = (profile.balanceEth + amount).coerceAtLeast(0.0))
            }
            "TON" -> {
                if (amount < 0 && profile.balanceTon < -amount) return@withContext false
                profile.copy(balanceTon = (profile.balanceTon + amount).coerceAtLeast(0.0))
            }
            "SOL" -> {
                if (amount < 0 && profile.balanceSol < -amount) return@withContext false
                profile.copy(balanceSol = (profile.balanceSol + amount).coerceAtLeast(0.0))
            }
            "TRX" -> {
                if (amount < 0 && profile.balanceTrx < -amount) return@withContext false
                profile.copy(balanceTrx = (profile.balanceTrx + amount).coerceAtLeast(0.0))
            }
            "USDC" -> {
                if (amount < 0 && profile.balanceUsdc < -amount) return@withContext false
                profile.copy(balanceUsdc = (profile.balanceUsdc + amount).coerceAtLeast(0.0))
            }
            else -> return@withContext false
        }
        dao.updateProfile(updated)
        return@withContext true
    }

    suspend fun swapAssets(
        fromAsset: String, // "WEED", "ETH", "TON", etc.
        toAsset: String,
        fromAmount: Double,
        toAmount: Double
    ): Boolean = withContext(Dispatchers.IO) {
        val profile = dao.getUserProfileSync() ?: return@withContext false
        var updated = profile

        // Deduct fromAsset
        when (fromAsset.uppercase()) {
            "WEED", "CREDITS" -> {
                if (profile.weed < fromAmount.toInt()) return@withContext false
                updated = updated.copy(weed = profile.weed - fromAmount.toInt())
            }
            "ETH" -> {
                if (profile.balanceEth < fromAmount) return@withContext false
                updated = updated.copy(balanceEth = profile.balanceEth - fromAmount)
            }
            "TON" -> {
                if (profile.balanceTon < fromAmount) return@withContext false
                updated = updated.copy(balanceTon = profile.balanceTon - fromAmount)
            }
            "SOL" -> {
                if (profile.balanceSol < fromAmount) return@withContext false
                updated = updated.copy(balanceSol = profile.balanceSol - fromAmount)
            }
            "TRX" -> {
                if (profile.balanceTrx < fromAmount) return@withContext false
                updated = updated.copy(balanceTrx = profile.balanceTrx - fromAmount)
            }
            "USDC" -> {
                if (profile.balanceUsdc < fromAmount) return@withContext false
                updated = updated.copy(balanceUsdc = profile.balanceUsdc - fromAmount)
            }
            else -> return@withContext false
        }

        // Add toAsset
        updated = when (toAsset.uppercase()) {
            "WEED", "CREDITS" -> updated.copy(weed = updated.weed + toAmount.toInt())
            "ETH" -> updated.copy(balanceEth = updated.balanceEth + toAmount)
            "TON" -> updated.copy(balanceTon = updated.balanceTon + toAmount)
            "SOL" -> updated.copy(balanceSol = updated.balanceSol + toAmount)
            "TRX" -> updated.copy(balanceTrx = updated.balanceTrx + toAmount)
            "USDC" -> updated.copy(balanceUsdc = updated.balanceUsdc + toAmount)
            else -> return@withContext false
        }

        dao.updateProfile(updated)
        return@withContext true
    }

    suspend fun addPeerContact(name: String, onionAddress: String, publicKey: String) = withContext(Dispatchers.IO) {
        dao.insertPeerContact(PeerContact(name = name, onionAddress = onionAddress, publicKey = publicKey))
    }

    suspend fun deletePeerContact(contactId: Int) = withContext(Dispatchers.IO) {
        val contact = dao.getPeerContactById(contactId)
        if (contact != null) {
            dao.deletePeerContact(contact)
        }
    }

    fun getEncryptedMessages(contactId: Int): Flow<List<EncryptedMessage>> {
        return dao.getEncryptedMessagesForContact(contactId)
    }

    suspend fun sendEncryptedMessage(contactId: Int, senderName: String, plaintext: String, aesKey: String) = withContext(Dispatchers.IO) {
        val cipherText = encryptAES(plaintext, aesKey)
        dao.insertEncryptedMessage(
            EncryptedMessage(
                contactId = contactId,
                senderName = senderName,
                plaintext = plaintext,
                ciphertext = cipherText,
                cryptographicKey = aesKey,
                isIncoming = false
            )
        )
        
        // Trigger realistic automated peer response after a brief network processing delay
        val contact = dao.getPeerContactById(contactId)
        val responderName = contact?.name ?: "Remote Peer"
        val onionAddr = contact?.onionAddress ?: "unknown.onion"
        
        // Fetch recent chat history to maintain realistic context
        val messagesList = dao.getEncryptedMessagesForContact(contactId).firstOrNull() ?: emptyList()
        val recentHistory = messagesList.takeLast(10).map {
            Pair(it.senderName, it.plaintext)
        }
        
        val responsePlaintext = GeminiService.generatePeerResponse(
            peerName = responderName,
            peerOnion = onionAddr,
            userMessage = plaintext,
            chatHistory = recentHistory
        )
        
        val responseCiphertext = encryptAES(responsePlaintext, aesKey)
        kotlinx.coroutines.delay(1200)
        dao.insertEncryptedMessage(
            EncryptedMessage(
                contactId = contactId,
                senderName = responderName,
                plaintext = responsePlaintext,
                ciphertext = responseCiphertext,
                cryptographicKey = aesKey,
                isIncoming = true
            )
        )
    }

    fun encryptAES(message: String, key: String): String {
        return try {
            val keyBytes = key.padEnd(16, '0').substring(0, 16).toByteArray(Charsets.UTF_8)
            val secretKey = SecretKeySpec(keyBytes, "AES")
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val encryptedBytes = cipher.doFinal(message.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            "ERROR_ENCRYPT_FAIL"
        }
    }

    fun decryptAES(encrypted: String, key: String): String {
        return try {
            val keyBytes = key.padEnd(16, '0').substring(0, 16).toByteArray(Charsets.UTF_8)
            val secretKey = SecretKeySpec(keyBytes, "AES")
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            val decodedBytes = Base64.decode(encrypted, Base64.NO_WRAP)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            "DECRYPTION_ERROR: $e"
        }
    }

    fun sha256(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { String.format("%02x", it) }
    }

    suspend fun broadcastP2pTransaction(
        recipientAddress: String,
        amountWeed: Int,
        cardIndex: Int,
        mintNumber: Int,
        seedPhrase: String
    ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val profile = dao.getUserProfileSync() ?: UserProfile()
        val senderAddress = profile.username
        
        if (amountWeed > 0 && profile.weed < amountWeed) {
            return@withContext Pair(false, "TRANSACTION REJECTED: Insufficient Weed on terminal.")
        }
        
        if (cardIndex != -1) {
            val ownedCards = dao.getMintedCards().firstOrNull() ?: emptyList()
            val ownedCard = ownedCards.find { it.cardIndex == cardIndex && it.mintNumber == mintNumber }
            if (ownedCard == null) {
                return@withContext Pair(false, "TRANSACTION REJECTED: Card serial #$mintNumber not found in hardware vault.")
            }
            dao.deleteMintedCard(ownedCard)
        }
        
        if (amountWeed > 0) {
            dao.updateProfile(profile.copy(weed = profile.weed - amountWeed))
        }
        
        val lastBlock = dao.getP2pTransactions().firstOrNull()?.firstOrNull()
        val blockNum = (lastBlock?.blockNumber ?: 0) + 1
        val prevHash = lastBlock?.blockHash ?: "00000000000000000000000000000000"
        var nonce = 0
        var blockHash = ""
        val baseData = "$blockNum$prevHash$senderAddress$recipientAddress$amountWeed$cardIndex$mintNumber"
        
        // Mine a block hash starting with "00"
        while (nonce < 1000000) {
            val hash = sha256("$baseData$nonce")
            if (hash.startsWith("00")) {
                blockHash = hash
                break
            }
            nonce++
        }
        if (blockHash.isEmpty()) {
            blockHash = sha256(baseData + nonce)
        }
        
        val signatureText = "SIG_" + sha256("$senderAddress$recipientAddress$amountWeed$seedPhrase").take(16)
        
        val tx = P2pTransaction(
            blockNumber = blockNum,
            previousHash = prevHash,
            blockHash = blockHash,
            nonce = nonce,
            senderAddress = senderAddress,
            recipientAddress = recipientAddress,
            amountWeed = amountWeed,
            cardIndex = cardIndex,
            mintNumber = mintNumber,
            signature = signatureText
        )
        
        dao.insertP2pTransaction(tx)
        
        dao.insertTransactionLog(
            TransactionLog(
                cardIndex = cardIndex,
                rarity = if (cardIndex != -1) "P2P_Card" else "P2P_Credits",
                price = amountWeed,
                type = "P2P_SEND",
                targetParty = recipientAddress,
                timestamp = System.currentTimeMillis()
            )
        )
        
        Pair(true, blockHash)
    }

    suspend fun testBridgeConnection(host: String, port: Int, proxyType: String, testUrl: String = "https://www.google.com"): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            val type = if (proxyType.uppercase() == "SOCKS") Proxy.Type.SOCKS else Proxy.Type.HTTP
            val proxy = Proxy(type, InetSocketAddress(host, port))
            val connection = URL(testUrl).openConnection(proxy) as HttpURLConnection
            connection.connectTimeout = 4000
            connection.readTimeout = 4000
            connection.requestMethod = "GET"
            val startTime = System.currentTimeMillis()
            connection.connect()
            val responseCode = connection.responseCode
            val ping = System.currentTimeMillis() - startTime
            if (responseCode in 200..399) {
                Pair(true, "ROUTE SECURED via $proxyType Bridge on $host:$port. Latency: ${ping}ms")
            } else {
                Pair(false, "GATEWAY EXCEPTION: HTTP-$responseCode received over $proxyType proxy.")
            }
        } catch (e: Exception) {
            Pair(false, "ROUTING ERROR: Bridge on $host:$port is offline/blocked. details: ${e.localizedMessage ?: "Timeout"}")
        }
    }

    suspend fun stakeWeed(amount: Int): Boolean = withContext(Dispatchers.IO) {
        val profile = dao.getUserProfileSync() ?: return@withContext false
        if (profile.weed < amount) return@withContext false
        val newStake = profile.nodeStake + amount
        val updated = profile.copy(
            weed = profile.weed - amount,
            nodeStake = newStake
        )
        dao.updateProfile(updated)
        true
    }

    suspend fun unstakeWeed(amount: Int): Boolean = withContext(Dispatchers.IO) {
        val profile = dao.getUserProfileSync() ?: return@withContext false
        if (profile.nodeStake < amount) return@withContext false
        val newStake = profile.nodeStake - amount
        val updated = profile.copy(
            weed = profile.weed + amount,
            nodeStake = newStake
        )
        dao.updateProfile(updated)
        true
    }

    suspend fun updateNodeSpeed(speedMbps: Double) = withContext(Dispatchers.IO) {
        val profile = dao.getUserProfileSync() ?: return@withContext
        val updated = profile.copy(nodeBandwidthMbps = speedMbps)
        dao.updateProfile(updated)
    }

    suspend fun updateNodeOnlineState(isOnline: Boolean) = withContext(Dispatchers.IO) {
        val profile = dao.getUserProfileSync() ?: return@withContext
        val updated = profile.copy(
            nodeIsOnline = isOnline,
            nodeLastActiveTime = if (isOnline) System.currentTimeMillis() else profile.nodeLastActiveTime
        )
        dao.updateProfile(updated)
    }

    suspend fun applyNodeOfflinePenalty(): Int = withContext(Dispatchers.IO) {
        val profile = dao.getUserProfileSync() ?: return@withContext 0
        val slashedStake = profile.nodeStake / 2
        val penalty = profile.nodeStake - slashedStake
        val updated = profile.copy(
            nodeStake = slashedStake,
            nodeUptimeMinutes = 0,
            nodeLastActiveTime = System.currentTimeMillis()
        )
        dao.updateProfile(updated)
        penalty
    }

    suspend fun incrementNodeUptime(minutes: Int) = withContext(Dispatchers.IO) {
        val profile = dao.getUserProfileSync() ?: return@withContext
        val updated = profile.copy(nodeUptimeMinutes = profile.nodeUptimeMinutes + minutes)
        dao.updateProfile(updated)
    }

    suspend fun updateUserProfileSetup(alias: String, avatarIndex: Int, bio: String, customAvatarUri: String? = null) = withContext(Dispatchers.IO) {
        val profile = dao.getUserProfileSync() ?: UserProfile()
        val updated = profile.copy(
            alias = alias,
            avatarIndex = avatarIndex,
            bio = bio,
            customAvatarUri = customAvatarUri
        )
        dao.updateProfile(updated)
    }

    suspend fun updateProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        dao.updateProfile(profile)
    }

    suspend fun getUserProfileSync(): UserProfile? = withContext(Dispatchers.IO) {
        dao.getUserProfileSync()
    }

    suspend fun getActiveDeckSync(): SavedDeck? = withContext(Dispatchers.IO) {
        dao.getActiveDeckSync()
    }

    companion object {
        @Volatile
        private var INSTANCE: TarotRepository? = null

        fun getInstance(context: Context): TarotRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = TarotRepository(context)
                INSTANCE = instance
                instance
            }
        }
    }
}
