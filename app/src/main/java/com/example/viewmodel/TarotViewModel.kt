package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random
import java.net.Socket
import java.net.InetSocketAddress
import java.net.Proxy

import android.content.Context
import com.example.ui.theme.CyberSkin

sealed interface DivinationState {
    object Idle : DivinationState
    object Scanning : DivinationState
    data class Success(
        val question: String,
        val cards: List<Pair<CyberTarotCard, Boolean>>, // Card and isUpright
        val report: String
    ) : DivinationState
    data class Error(val message: String) : DivinationState
}

data class GlobalNodeRank(
    val name: String,
    val role: String,
    val stake: Int,
    val speedMbps: Double,
    val status: String,
    val isUser: Boolean = false
)

class TarotViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TarotRepository.getInstance(application)

    private val sharedPrefs = application.getSharedPreferences("neuro_tarot_prefs", Context.MODE_PRIVATE)

    private val _currentSkin = MutableStateFlow(
        CyberSkin.fromId(sharedPrefs.getString("selected_skin", "jamaica") ?: "jamaica")
    )
    val currentSkin: StateFlow<CyberSkin> = _currentSkin.asStateFlow()

    fun updateSkin(skin: CyberSkin) {
        sharedPrefs.edit().putString("selected_skin", skin.id).apply()
        _currentSkin.value = skin
    }

    // Database flows exposed to UI
    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val mintedCards: StateFlow<List<MintedCard>> = repository.mintedCards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deckApproxCost: StateFlow<Int> = mintedCards.map { cards ->
        cards.sumOf { card ->
            when (card.rarity.lowercase()) {
                "common" -> 150
                "rare" -> 450
                "epic" -> 1200
                "legend" -> 3500
                else -> 100
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val deckRating: StateFlow<Int> = mintedCards.map { cards ->
        cards.sumOf { card ->
            when (card.rarity.lowercase()) {
                "common" -> 5
                "rare" -> 15
                "epic" -> 40
                "legend" -> 100
                else -> 1
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val marketListings: StateFlow<List<MarketListing>> = repository.marketListings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val divinationReadings: StateFlow<List<DivinationReading>> = repository.divinationReadings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteDivinationReading(id: Int) {
        viewModelScope.launch {
            repository.deleteDivinationReading(id)
        }
    }

    val transactionLogs: StateFlow<List<TransactionLog>> = repository.transactionLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // TCG Deck Builder & Card Forge State
    val savedDecks: StateFlow<List<SavedDeck>> = repository.savedDecks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeDeck: StateFlow<SavedDeck?> = repository.activeDeck
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val cardUpgrades: StateFlow<Map<Int, CardUpgrade>> = repository.cardUpgrades
        .map { list -> list.associateBy { it.cardIndex } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun saveDeck(name: String, icon: String, cardIndices: List<Int>, makeActive: Boolean, description: String = "", deckId: Int = 0) {
        viewModelScope.launch {
            repository.saveDeck(name, icon, cardIndices, makeActive, description, deckId)
        }
    }

    fun setActiveDeck(deckId: Int) {
        viewModelScope.launch {
            repository.setActiveDeck(deckId)
        }
    }

    fun deleteDeck(deck: SavedDeck) {
        viewModelScope.launch {
            repository.deleteDeck(deck)
        }
    }

    fun upgradeCardStat(cardIndex: Int, stat: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val success = repository.upgradeCardStat(cardIndex, stat)
            onResult(success)
        }
    }

    fun setCardSkin(cardIndex: Int, skin: String) {
        viewModelScope.launch {
            repository.setCardSkin(cardIndex, skin)
        }
    }

    suspend fun getActiveBattleDeck(): List<com.example.duel.BattleCard> {
        return repository.getActiveBattleDeckSync()
    }

    // Active Divination Reading State
    private val _divinationState = MutableStateFlow<DivinationState>(DivinationState.Idle)
    val divinationState: StateFlow<DivinationState> = _divinationState.asStateFlow()

    // Temporary drawn cards before they are fully read or minted
    private val _activeDrawnCards = MutableStateFlow<List<Pair<CyberTarotCard, Boolean>>>(emptyList())
    val activeDrawnCards: StateFlow<List<Pair<CyberTarotCard, Boolean>>> = _activeDrawnCards.asStateFlow()

    // Interactive Core Node Mining Cooldown (in seconds)
    private val _miningCooldown = MutableStateFlow(0)
    val miningCooldown: StateFlow<Int> = _miningCooldown.asStateFlow()

    // Dialog & UI Notification System
    private val _uiNotification = MutableStateFlow<String?>(null)
    val uiNotification: StateFlow<String?> = _uiNotification.asStateFlow()

    // Search and filtering for market
    private val _marketQuery = MutableStateFlow("")
    val marketQuery: StateFlow<String> = _marketQuery.asStateFlow()

    private val _marketRarityFilter = MutableStateFlow("All")
    val marketRarityFilter: StateFlow<String> = _marketRarityFilter.asStateFlow()

    // Filtered Market Listings combining state
    val filteredMarketListings: StateFlow<List<MarketListing>> = combine(
        marketListings,
        _marketQuery,
        _marketRarityFilter
    ) { listings, query, rarity ->
        listings.filter { listing ->
            val card = CyberTarotDeck.getCard(listing.cardIndex)
            val matchesQuery = card.name.contains(query, ignoreCase = true) ||
                    card.traditionalName.contains(query, ignoreCase = true) ||
                    listing.sellerName.contains(query, ignoreCase = true)
            val matchesRarity = rarity == "All" || listing.rarity == rarity
            matchesQuery && matchesRarity
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // BIP39 Security State Flows
    private val _generatedMnemonic = MutableStateFlow<List<String>?>(null)
    val generatedMnemonic: StateFlow<List<String>?> = _generatedMnemonic.asStateFlow()

    private val _verificationIndices = MutableStateFlow<List<Int>>(emptyList())
    val verificationIndices: StateFlow<List<Int>> = _verificationIndices.asStateFlow()

    private val _verificationOptions = MutableStateFlow<Map<Int, List<String>>>(emptyMap())
    val verificationOptions: StateFlow<Map<Int, List<String>>> = _verificationOptions.asStateFlow()

    private val _verificationAnswers = MutableStateFlow<Map<Int, String>>(emptyMap())
    val verificationAnswers: StateFlow<Map<Int, String>> = _verificationAnswers.asStateFlow()

    // 8-digit PIN & App lock security flows
    private val _isUnlockedThisSession = MutableStateFlow(false)
    private val _isSafeBotanicalMode = MutableStateFlow(false)
    val isSafeBotanicalMode: StateFlow<Boolean> = _isSafeBotanicalMode.asStateFlow()

    val isAppLocked: StateFlow<Boolean> = combine(userProfile, _isUnlockedThisSession) { profile, unlocked ->
        if (profile == null) {
            false
        } else {
            profile.isSecured && !profile.pinCode.isNullOrBlank() && !unlocked
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _securityRegistrationStep = MutableStateFlow(0) // 0: Form, 1: Mnemonic reveal, 2: Challenge, 3: PIN Setup
    val securityRegistrationStep: StateFlow<Int> = _securityRegistrationStep.asStateFlow()

    // Decentrallized Cloud Node logs
    private val _cloudLogs = MutableStateFlow<List<String>>(listOf(
        "System: Initializing decentralized cloud node listener...",
        "System: Local storage block allocated (1.00 GB maximum limit).",
        "System: Secure TLS tunnel opened to relay-alpha.cybercloud.net",
        "System: Awaiting incoming encrypted client containers..."
    ))
    val cloudLogs: StateFlow<List<String>> = _cloudLogs.asStateFlow()

    // --- WEED PROTOCOL & DECENTRALIZED AD PLATFORM FLOWS ---
    val adBanners: StateFlow<List<AdBanner>> = repository.adBanners
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _enabledAdThemes = MutableStateFlow<Set<String>>(setOf(
        "Cybernetics", "Tarot", "Biotech", "Darknet", "AI Overlords", "Quantum Finance"
    ))
    val enabledAdThemes: StateFlow<Set<String>> = _enabledAdThemes.asStateFlow()

    private val _treasurerLogs = MutableStateFlow<List<String>>(listOf(
        "AI Treasurer: Ledger initialized. Liquidity peg online.",
        "AI Treasurer: Exchange peg confirmed: 100 WEED = 1 USDT (Pegged to 1 cent).",
        "AI Treasurer: Active grid monitoring started. Burning 100% of expired ad slots."
    ))
    val treasurerLogs: StateFlow<List<String>> = _treasurerLogs.asStateFlow()

    private val _topExchangesTickers = MutableStateFlow<Map<String, Map<String, Double>>>(emptyMap())
    val topExchangesTickers: StateFlow<Map<String, Map<String, Double>>> = _topExchangesTickers.asStateFlow()

    private val _compiledAverageRates = MutableStateFlow<Map<String, Double>>(mapOf(
        "BTC" to 58000.0,
        "ETH" to 3100.0,
        "SOL" to 140.0,
        "WEED" to 0.01
    ))
    val compiledAverageRates: StateFlow<Map<String, Double>> = _compiledAverageRates.asStateFlow()

    private val exchangesList = listOf(
        "Binance", "Coinbase", "Kraken", "OKX", "Bybit", 
        "Huobi", "Gate.io", "Bitfinex", "KuCoin", "Gemini"
    )

    init {
        // Start background market simulation loop
        viewModelScope.launch {
            while (true) {
                delay(12000) // update market activity every 12 seconds
                repository.tickMarketSimulation()
            }
        }

        // Start background hosting simulation loop
        viewModelScope.launch {
            while (true) {
                delay(8000) // tick every 8 seconds
                val profile = userProfile.value
                if (profile?.isCloudAllocated == true && profile.allocatedDiskSpaceGb > 0f) {
                    val earned = (profile.allocatedDiskSpaceGb * 12).toInt().coerceAtLeast(1)
                    repository.mineCredits(earned)
                    
                    val containers = listOf("user_98a2.enc", "backup_441c.bin", "state_f910.db", "keys_7b1d.key")
                    val updatedLogs = _cloudLogs.value.toMutableList()
                    updatedLogs.add(0, "💾 Backup replicated [${containers.random()}] -> Local node space +$earned WEED")
                    if (updatedLogs.size > 20) {
                        updatedLogs.removeAt(updatedLogs.lastIndex)
                    }
                    _cloudLogs.value = updatedLogs
                }
            }
        }

        // Start background Exchange rates simulation (Binance, Coinbase, Kraken, OKX, Bybit, Huobi, Gate.io, Bitfinex, KuCoin, Gemini)
        viewModelScope.launch {
            var btcBase = 58000.0
            var ethBase = 3100.0
            var solBase = 140.0
            var weedBase = 0.01 // pegged around $0.01

            while (true) {
                // Apply small random walk to base rates
                btcBase += (Random.nextDouble() - 0.5) * 40.0
                ethBase += (Random.nextDouble() - 0.5) * 3.0
                solBase += (Random.nextDouble() - 0.5) * 0.25
                weedBase = 0.01 + (Random.nextDouble() - 0.5) * 0.0001 // Peg holding steady

                val newTickers = mutableMapOf<String, Map<String, Double>>()
                val sums = mutableMapOf("BTC" to 0.0, "ETH" to 0.0, "SOL" to 0.0, "WEED" to 0.0)

                exchangesList.forEach { exchange ->
                    // generate unique variance per exchange
                    val btcVar = btcBase * (1.0 + (Random.nextDouble() - 0.5) * 0.002)
                    val ethVar = ethBase * (1.0 + (Random.nextDouble() - 0.5) * 0.002)
                    val solVar = solBase * (1.0 + (Random.nextDouble() - 0.5) * 0.002)
                    val weedVar = weedBase * (1.0 + (Random.nextDouble() - 0.5) * 0.0015)

                    newTickers[exchange] = mapOf(
                        "BTC" to btcVar,
                        "ETH" to ethVar,
                        "SOL" to solVar,
                        "WEED" to weedVar
                    )

                    sums["BTC"] = sums["BTC"]!! + btcVar
                    sums["ETH"] = sums["ETH"]!! + ethVar
                    sums["SOL"] = sums["SOL"]!! + solVar
                    sums["WEED"] = sums["WEED"]!! + weedVar
                }

                _topExchangesTickers.value = newTickers

                // Compute exact averages
                val avgBtc = sums["BTC"]!! / exchangesList.size
                val avgEth = sums["ETH"]!! / exchangesList.size
                val avgSol = sums["SOL"]!! / exchangesList.size
                val avgWeed = sums["WEED"]!! / exchangesList.size

                _compiledAverageRates.value = mapOf(
                    "BTC" to avgBtc,
                    "ETH" to avgEth,
                    "SOL" to avgSol,
                    "WEED" to avgWeed
                )

                // Occasional AI Treasurer arbitrage logging
                if (Random.nextInt(100) < 15) {
                    val randomExchange = exchangesList.random()
                    val randomToken = listOf("BTC", "ETH", "SOL", "WEED").random()
                    val rate = newTickers[randomExchange]?.get(randomToken) ?: 0.0
                    val avg = _compiledAverageRates.value[randomToken] ?: 0.0
                    val diffPct = ((rate - avg) / avg) * 100.0

                    val logs = _treasurerLogs.value.toMutableList()
                    val action = if (diffPct > 0) "MINTING/BURNING ARBITRAGE" else "LIQUIDITY INJECTION"
                    logs.add(0, "AI Treasurer [$action]: Arbitrage gap detected on $randomExchange for $randomToken (${String.format("%.2f%%", diffPct)} vs avg). Adjusted liquidity buffers.")
                    if (logs.size > 20) logs.removeAt(logs.lastIndex)
                    _treasurerLogs.value = logs
                }

                delay(4000) // tick every 4 seconds
            }
        }

        // ----------------- SUB-ZERO SWARM NODE DYNAMIC METRIC ENGINE -----------------
        viewModelScope.launch {
            // Wait for user profile to load
            var profileLoaded = false
            while (!profileLoaded) {
                val profile = userProfile.value
                if (profile != null) {
                    profileLoaded = true
                    
                    // Startup check: was the node active but now offline for a long period?
                    if (profile.nodeIsOnline && profile.nodeLastActiveTime > 0L) {
                        val offlineDurationMs = System.currentTimeMillis() - profile.nodeLastActiveTime
                        if (offlineDurationMs > 15 * 60 * 1000L) { // 15 minutes
                            val penalty = repository.applyNodeOfflinePenalty()
                            repository.updateNodeOnlineState(false)
                            swarmManager.stopNode()
                            postNotification("🔮 COLD REBOOT PENALTY: Node was offline for ${offlineDurationMs / (60 * 1000L)} minutes. Slashed $penalty WEED (50% penalty).")
                            swarmManager.addLog("❌ [CRITICAL] Node went offline for too long (${offlineDurationMs / (60 * 1000L)}m) while app was terminated. Slashed $penalty WEED.")
                        } else {
                            // If it wasn't offline for 15 minutes, we can keep the node online and refresh presence
                            repository.updateNodeOnlineState(true)
                            swarmManager.addLog("📡 Node presence verified on cold reboot. Uptime resumed.")
                        }
                    }
                }
                delay(1000)
            }

            // Periodic presence tick & live offline check
            while (true) {
                delay(5000) // tick every 5 seconds
                val profile = userProfile.value ?: continue
                
                if (profile.nodeIsOnline) {
                    // Node is active: update presence and increment uptime (simulate 1 minute of uptime every 5 real seconds for speed and satisfying UI!)
                    repository.incrementNodeUptime(1)
                    repository.updateNodeOnlineState(true)
                } else {
                    // Node is offline: check if it's been offline for >15 minutes in this live session
                    if (profile.nodeLastActiveTime > 0L && profile.nodeStake > 0) {
                        val durationMs = System.currentTimeMillis() - profile.nodeLastActiveTime
                        if (durationMs > 15 * 60 * 1000L) {
                            val penalty = repository.applyNodeOfflinePenalty()
                            postNotification("🔮 OFFLINE OUTAGE PENALTY: Slashed $penalty WEED (50% penalty) for being offline >15 minutes!")
                            swarmManager.addLog("❌ [CRITICAL] AI Treasurer: Node has been offline for ${durationMs / (60 * 1000L)} minutes! Slashed $penalty WEED.")
                        }
                    }
                }
            }
        }
    }

    // Dismiss notification helper
    fun dismissNotification() {
        _uiNotification.value = null
    }

    fun postNotification(message: String) {
        _uiNotification.value = message
    }

    // Set search query
    fun setMarketQuery(query: String) {
        _marketQuery.value = query
    }

    // Set rarity filter
    fun setMarketRarityFilter(rarity: String) {
        _marketRarityFilter.value = rarity
    }

    // Mining Action
    fun startMining() {
        if (_miningCooldown.value > 0) return

        viewModelScope.launch {
            repository.mineCredits(350)
            _uiNotification.value = "⚡ Core Nodes harvested successfully. +350 WEED added to local wallet."
            
            // Start 30 seconds cooldown
            _miningCooldown.value = 30
            while (_miningCooldown.value > 0) {
                delay(1000)
                _miningCooldown.value -= 1
            }
        }
    }

    // Trigger Divination Scan (Draws card, reads, and queries Gemini)
    fun runDivinationScan(question: String, cardCount: Int) {
        if (question.isBlank()) {
            _uiNotification.value = "⚠️ Please input a cyberquery prompt for the Matrix decoder."
            return
        }

        viewModelScope.launch {
            _divinationState.value = DivinationState.Scanning
            
            // Choose random unique card indices
            val indices = (0..21).shuffled().take(cardCount)
            val drawn = indices.map { index ->
                val card = CyberTarotDeck.getCard(index)
                val isUpright = Random.nextBoolean()
                card to isUpright
            }

            _activeDrawnCards.value = drawn

            // Simulate neural link sync delay for futuristic user experience
            delay(2000)

            // Submit cards and prompt to Gemini Core with proxy settings from profile
            val profile = userProfile.value
            val currentLanguage = profile?.systemLanguage ?: "en"
            val report = if (profile != null) {
                GeminiService.decodeTarotMatrix(
                    userQuestion = question,
                    drawnCards = drawn,
                    systemLanguage = currentLanguage,
                    proxyType = profile.proxyType,
                    proxyHost = profile.proxyHost,
                    proxyPort = profile.proxyPort,
                    isProxyActive = profile.isProxyActive
                )
            } else {
                GeminiService.decodeTarotMatrix(question, drawn, systemLanguage = currentLanguage)
            }
            
            // Reward user credits for scanning the matrix
            repository.recordDivinationDraw(cardCount)
            completeScanQuest()

            // Save the divination reading to local database history
            repository.saveDivinationReading(
                question = question,
                cardIndices = drawn.map { it.first.index },
                isUpright = drawn.map { it.second },
                report = report
            )

            _divinationState.value = DivinationState.Success(
                question = question,
                cards = drawn,
                report = report
            )
        }
    }

    // Reset divination screen
    fun resetDivination() {
        _divinationState.value = DivinationState.Idle
        _activeDrawnCards.value = emptyList()
    }

    // Mint a Cyber Tarot NFT
    fun mintNFT(cardIndex: Int, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val userCredits = userProfile.value?.weed ?: 0
            if (userCredits < 200) {
                _uiNotification.value = "❌ Insufficient WEED. Minting requires 200 WEED. Harvest grids or trade cards."
                onComplete(false)
                return@launch
            }

            val minted = repository.mintTarotNFT(cardIndex)
            if (minted != null) {
                val cardName = CyberTarotDeck.getCard(cardIndex).name
                _uiNotification.value = "🎉 NFT Minted! Successfully generated '$cardName' #${String.format("%04d", minted.mintNumber)} [Rarity: ${minted.rarity}]. Spent 200 WEED."
                onComplete(true)
            } else {
                _uiNotification.value = "❌ System anomaly during NFT generation."
                onComplete(false)
            }
        }
    }

    // Synthesize / Fuse cards into higher tier
    fun fuseCards(fusionType: TarotRepository.FusionType, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val minted = repository.fuseCards(fusionType)
            if (minted != null) {
                val card = CyberTarotDeck.getCard(minted.cardIndex)
                _uiNotification.value = "⚡ SYNTHESIS SUCCESS! Fused cards into '${card.name}' [${card.defaultRarity.uppercase()}] #${String.format("%04d", minted.mintNumber)}"
                onComplete(true)
            } else {
                val reqMsg = when (fusionType) {
                    TarotRepository.FusionType.COMMON_TO_RARE -> "Requires 4 unlisted Common cards"
                    TarotRepository.FusionType.RARE_TO_EPIC -> "Requires 4 unlisted Rare cards"
                    TarotRepository.FusionType.EPIC_TO_LEGEND -> "Requires 4 unlisted Epic cards"
                    TarotRepository.FusionType.EPIC_TO_GENESIS -> "Requires 10 unlisted Epic cards for Genesis Gold"
                }
                _uiNotification.value = "❌ Fusion Failed: $reqMsg"
                onComplete(false)
            }
        }
    }

    // List owned card on the market
    fun listCardOnMarket(cardId: Int, price: Int) {
        if (price <= 0) {
            _uiNotification.value = "⚠️ Listing price must exceed 0 WEED."
            return
        }

        viewModelScope.launch {
            val success = repository.listNFT(cardId, price)
            if (success) {
                _uiNotification.value = "💾 Card listed on Exchange Ledger at $price WEED."
            } else {
                _uiNotification.value = "❌ Failed to list card."
            }
        }
    }

    // Cancel card listing
    fun cancelCardListing(cardIndex: Int, mintNumber: Int) {
        viewModelScope.launch {
            val success = repository.cancelListing(cardIndex, mintNumber)
            if (success) {
                _uiNotification.value = "🔓 Listing cancelled. Card returned to hardware storage."
            } else {
                _uiNotification.value = "❌ Failed to cancel listing."
            }
        }
    }

    // Sell instantly to Broker
    fun sellInstantly(cardId: Int) {
        viewModelScope.launch {
            val brokerPrice = repository.sellInstantlyToBroker(cardId)
            if (brokerPrice > 0) {
                _uiNotification.value = "💰 Instant sale verified. Net Broker bought card for $brokerPrice WEED."
            } else {
                _uiNotification.value = "❌ Instant sale declined by Broker."
            }
        }
    }

    // Buy card from exchange
    fun buyCardFromMarket(listing: MarketListing) {
        viewModelScope.launch {
            val userCredits = userProfile.value?.weed ?: 0
            if (userCredits < listing.price) {
                _uiNotification.value = "❌ Insufficient WEED. Need ${listing.price} WEED."
                return@launch
            }

            val success = repository.buyNFTFromExchange(listing.id)
            if (success) {
                val card = CyberTarotDeck.getCard(listing.cardIndex)
                _uiNotification.value = "💳 Buy order filled! Successfully acquired ${card.name} #${String.format("%04d", listing.mintNumber)} for ${listing.price} WEED."
            } else {
                _uiNotification.value = "❌ Transaction error on Exchange."
            }
        }
    }

    fun getBaseMarketPrice(rarity: String): Int {
        return repository.getBaseMarketPrice(rarity)
    }

    // --- BIP39 Cryptographic & Verification Actions ---

    fun generateSecureSeed(birthdate: String, firstName: String, lastName: String, entropyWords: List<String>) {
        if (birthdate.isBlank() || firstName.isBlank() || lastName.isBlank()) {
            val lang = userProfile.value?.systemLanguage ?: "en"
            _uiNotification.value = if (lang == "ru") {
                "⚠️ Пожалуйста, заполните Имя, Фамилию и Дату рождения."
            } else {
                "⚠️ Please complete First Name, Last Name, and Date of Birth fields."
            }
            return
        }
        if (entropyWords.size != 5 || entropyWords.any { it.isBlank() || it.length > 16 || !it.all { c -> c.isLetter() } }) {
            val lang = userProfile.value?.systemLanguage ?: "en"
            _uiNotification.value = if (lang == "ru") {
                "⚠️ Укажите ровно 5 слов энтропии, каждое длиной до 16 букв (только буквы, без пробелов)."
            } else {
                "⚠️ Please provide exactly 5 entropy words, each up to 16 letters (letters only, no spaces)."
            }
            return
        }
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val entropy = Bip39Helper.generateEntropyFromWords(entropyWords)
                val words = Bip39Helper.generateMnemonic(entropy)
                
                // Pick 3 random distinct word positions for verification (0 to 23)
                val testIndices = (0..23).shuffled().take(3).sorted()
                val optionsMap = testIndices.associateWith { correctIdx ->
                    val correctWord = words[correctIdx]
                    // Gather 3 other random words from standard wordlist
                    val others = (1..3).map {
                        var w = Bip39Wordlist.words.random()
                        while (w == correctWord) { w = Bip39Wordlist.words.random() }
                        w
                    }
                    (others + correctWord).shuffled()
                }
                
                _generatedMnemonic.value = words
                _verificationIndices.value = testIndices
                _verificationOptions.value = optionsMap
                _verificationAnswers.value = emptyMap()
                _securityRegistrationStep.value = 1 // Transition to display seed phrase immediately!
            } catch (e: Exception) {
                _uiNotification.value = "❌ Security generator error: ${e.message}"
            }
        }
    }

    fun selectVerificationWord(index: Int, word: String) {
        val current = _verificationAnswers.value.toMutableMap()
        current[index] = word
        _verificationAnswers.value = current
    }

    fun proceedToVerification() {
        if (_generatedMnemonic.value == null) return
        _securityRegistrationStep.value = 2 // Transition to memory challenge
    }

    fun verifyAndSaveSecureSeed() {
        val words = _generatedMnemonic.value ?: return
        val testIndices = _verificationIndices.value
        val userAnswers = _verificationAnswers.value

        if (userAnswers.size < testIndices.size) {
            _uiNotification.value = "⚠️ Please complete all 3 verification word checks."
            return
        }

        var allCorrect = true
        for (idx in testIndices) {
            val correctWord = words[idx]
            val userWord = userAnswers[idx]
            if (userWord != correctWord) {
                allCorrect = false
                break
            }
        }

        if (allCorrect) {
            _securityRegistrationStep.value = 3 // Transition to PIN Setup!
            _uiNotification.value = "✔️ Neural Verification Successful! Please set up your 8-digit secure entry PIN."
        } else {
            _uiNotification.value = "❌ Verification failed. Mismatched words. Please review your seed and try again."
        }
    }

    fun saveSecuredProfileWithPin(dob: String, first: String, last: String, pin: String, pinSafe: String = "11111111", pinPanic: String = "22222222", aliasVal: String = "", avatarIdx: Int = 0, customWords: List<String>? = null) {
        if (pin.length != 8 || !pin.all { it.isDigit() }) {
            _uiNotification.value = "⚠️ PIN must be exactly 8 digits."
            return
        }
        val words = customWords ?: _generatedMnemonic.value ?: listOf("weed", "guru", "botany", "decentralized", "cloud", "security", "node", "tor", "onion", "bip39", "encryption", "wallet")
        viewModelScope.launch {
            val seedString = words.joinToString(" ")
            repository.saveSecuredProfile(dob, first, last, seedString, pin, pinSafe, pinPanic, aliasVal, avatarIdx)
            _isUnlockedThisSession.value = true // automatically unlock
            _isSafeBotanicalMode.value = false
            _securityRegistrationStep.value = 0 // reset wizard
            _uiNotification.value = "🔒 Identity secured with BIP39 seed & 8-digit PIN! Terminal secured."
            resetMnemonicForm()
        }
    }

    fun triggerPanicReset() {
        viewModelScope.launch {
            repository.triggerPanicResetComplete()
            _isUnlockedThisSession.value = false
            _isSafeBotanicalMode.value = false
            _uiNotification.value = "🚨 EMERGENCY PANIC WIPE COMPLETED. DATABASE SCRUBBED."
        }
    }

    fun unlockApp(pin: String): Boolean {
        val profile = userProfile.value ?: return false
        return when (pin) {
            profile.pinCode -> {
                _isUnlockedThisSession.value = true
                _isSafeBotanicalMode.value = false
                _uiNotification.value = "🔓 Decryption keys unlocked. Welcome back, ${profile.alias}."
                true
            }
            profile.pinSafeMode -> {
                _isUnlockedThisSession.value = true
                _isSafeBotanicalMode.value = true
                _uiNotification.value = "🛡️ Harmless Mode unlocked. Safe environment loaded."
                true
            }
            profile.pinPanicMode -> {
                triggerPanicReset()
                true
            }
            else -> {
                _uiNotification.value = "❌ INVALID SECURITY PIN CODE. ATTEMPT LOGGED."
                false
            }
        }
    }

    fun lockApp() {
        _isUnlockedThisSession.value = false
    }

    fun setSecurityRegistrationStep(step: Int) {
        _securityRegistrationStep.value = step
    }

    fun resetMnemonicForm() {
        _generatedMnemonic.value = null
        _verificationIndices.value = emptyList()
        _verificationOptions.value = emptyMap()
        _verificationAnswers.value = emptyMap()
        _securityRegistrationStep.value = 0
    }

    fun resetSecurity() {
        viewModelScope.launch {
            repository.resetSecurity()
            _isUnlockedThisSession.value = false
            _securityRegistrationStep.value = 0
            _uiNotification.value = "🔓 Mnemonic credentials cleared from local hardware. Secure backup deactivated."
        }
    }

    fun updateProxySettings(type: String, host: String, port: Int, config: String, active: Boolean) {
        viewModelScope.launch {
            repository.updateProxySettings(type, host, port, config, active)
            if (active) {
                _uiNotification.value = "🛡️ Network Gateway activated: Routing traffic through $type proxy ($host:$port)."
                completeProxyQuest()
            } else {
                _uiNotification.value = "🔌 Network Gateway deactivated: Routing traffic directly."
            }
        }
    }

    fun updateSystemLanguage(lang: String) {
        viewModelScope.launch {
            repository.updateSystemLanguage(lang)
            _uiNotification.value = "🌐 Interface localization dialect updated to: $lang."
        }
    }

    fun getFreeDiskSpaceGb(): Float {
        return try {
            val file = getApplication<Application>().filesDir
            val freeBytes = file.usableSpace
            freeBytes.toFloat() / (1024f * 1024f * 1024f)
        } catch (e: Exception) {
            5.2f // fallback
        }
    }

    fun getMaxCloudAllocationGb(): Float {
        return getFreeDiskSpaceGb() * 0.5f
    }

    fun testProxyConnection(type: String, host: String, port: Int, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val socket = Socket()
                val timeoutMs = 4000
                socket.connect(InetSocketAddress(host, port), timeoutMs)
                socket.close()
                onResult(true, "⚡ Gateway Success: TCP Handshake successful with proxy at $host:$port.")
            } catch (e: Exception) {
                onResult(false, "❌ Gateway Error: Failed TCP connection to $host:$port. Reason: ${e.localizedMessage}")
            }
        }
    }

    fun updateCloudAllocation(allocated: Boolean, spaceGb: Float) {
        viewModelScope.launch {
            repository.updateCloudAllocation(allocated, spaceGb)
            if (allocated) {
                _uiNotification.value = "⚡ Disk space allocated! Your node is now online. +100 WEED awarded."
            } else {
                _uiNotification.value = "🔌 Local node taken offline. Disk space reclaimed."
            }
        }
    }

    // --- NEW CRYPTO, P2P & BRIDGE VIEWMODEL INTEGRATIONS ---

    val peerContacts: StateFlow<List<PeerContact>> = repository.peerContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val p2pTransactions: StateFlow<List<P2pTransaction>> = repository.p2pTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getMessagesForContact(contactId: Int): Flow<List<EncryptedMessage>> {
        return repository.getEncryptedMessages(contactId)
    }

    // Bridge config flows
    private val _bridgeHost = MutableStateFlow("127.0.0.1")
    val bridgeHost: StateFlow<String> = _bridgeHost.asStateFlow()

    private val _bridgePort = MutableStateFlow("9050")
    val bridgePort: StateFlow<String> = _bridgePort.asStateFlow()

    private val _bridgeType = MutableStateFlow("SOCKS") // SOCKS vs HTTP
    val bridgeType: StateFlow<String> = _bridgeType.asStateFlow()

    private val _bridgeStatus = MutableStateFlow("DISCONNECTED") // DISCONNECTED, TESTING, ONLINE, OFFLINE
    val bridgeStatus: StateFlow<String> = _bridgeStatus.asStateFlow()

    private val _bridgeLog = MutableStateFlow<List<String>>(listOf(
        "BRIDGE: Interface initialized. Awaiting user route test...",
        "TOR: SOCKS5 listener listening on local port 9050."
    ))
    val bridgeLog: StateFlow<List<String>> = _bridgeLog.asStateFlow()

    fun updateBridgeConfig(host: String, port: String, type: String) {
        _bridgeHost.value = host
        _bridgePort.value = port
        _bridgeType.value = type
    }

    fun testBridge() {
        viewModelScope.launch {
            _bridgeStatus.value = "TESTING"
            val host = _bridgeHost.value
            val portInt = _bridgePort.value.toIntOrNull() ?: 9050
            val type = _bridgeType.value
            
            val logs = _bridgeLog.value.toMutableList()
            logs.add("BRIDGE: Initiating handshake via $type proxy at $host:$portInt...")
            logs.add("TOR: Resolving obfs4 bridge relays...")
            _bridgeLog.value = logs
            
            val (success, message) = repository.testBridgeConnection(host, portInt, type)
            
            val updatedLogs = _bridgeLog.value.toMutableList()
            updatedLogs.add("BRIDGE: $message")
            if (success) {
                _bridgeStatus.value = "ONLINE"
                updatedLogs.add("TOR: Circuit established. 100% bootstrapped.")
                _uiNotification.value = "🟢 Bridge route secured successfully!"
            } else {
                _bridgeStatus.value = "OFFLINE"
                updatedLogs.add("TOR: Circuit failed. Connection refused or route timed out.")
                _uiNotification.value = "🔴 Bridge route failed. Check proxy client."
            }
            _bridgeLog.value = updatedLogs
        }
    }

    // Address Book methods
    fun addContact(name: String, onionAddress: String) {
        viewModelScope.launch {
            val fakePublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA" + 
                repository.sha256(name + System.currentTimeMillis()).take(32) + "..."
            repository.addPeerContact(name, onionAddress, fakePublicKey)
            _uiNotification.value = "👤 Added peer contact: $name"
        }
    }

    fun deleteContact(contactId: Int) {
        viewModelScope.launch {
            repository.deletePeerContact(contactId)
            _uiNotification.value = "👤 Removed contact from terminal address book."
        }
    }

    fun sendEncryptedMessage(contactId: Int, plaintext: String) {
        if (plaintext.isBlank()) return
        viewModelScope.launch {
            val key = "SECURE_KEY_CH_${contactId}"
            repository.sendEncryptedMessage(contactId, "Me", plaintext, key)
        }
    }

    // P2P Transactions state
    private val _p2pStatus = MutableStateFlow<String?>(null)
    val p2pStatus: StateFlow<String?> = _p2pStatus.asStateFlow()

    private val _miningProgress = MutableStateFlow<Float?>(null)
    val miningProgress: StateFlow<Float?> = _miningProgress.asStateFlow()

    fun sendP2pTransaction(
        recipientAddress: String,
        amountWeed: Int,
        cardIndex: Int,
        mintNumber: Int,
        seedPhrase: String
    ) {
        viewModelScope.launch {
            _p2pStatus.value = "MINING"
            _miningProgress.value = 0.1f
            
            delay(500)
            _miningProgress.value = 0.5f
            delay(500)
            _miningProgress.value = 0.9f
            delay(200)
            
            val (success, hashOrError) = repository.broadcastP2pTransaction(
                recipientAddress,
                amountWeed,
                cardIndex,
                mintNumber,
                seedPhrase
            )
            
            _miningProgress.value = null
            if (success) {
                _p2pStatus.value = "SUCCESS"
                _uiNotification.value = "⛓️ Block mined & broadcasted! Hash: ${hashOrError.take(12)}..."
            } else {
                _p2pStatus.value = "FAILED"
                _uiNotification.value = "❌ P2P Broadcast Failed: $hashOrError"
            }
        }
    }

    fun resetP2pStatus() {
        _p2pStatus.value = null
    }

    fun clearNotification() {
        _uiNotification.value = null
    }

    // --- SMART CONTRACT / TESTNET MINTING INTEGRATIONS ---

    private val smartContractService = com.example.data.EvmTestnetSmartContractService()

    private val _smartContractRpcUrl = MutableStateFlow("https://ethereum-sepolia-rpc.publicnode.com")
    val smartContractRpcUrl: StateFlow<String> = _smartContractRpcUrl.asStateFlow()

    private val _smartContractAddress = MutableStateFlow("0xC72a1068E370A8D6e326C7cf65749be9e7ab9CF2")
    val smartContractAddress: StateFlow<String> = _smartContractAddress.asStateFlow()

    private val _onChainMints = MutableStateFlow<List<com.example.data.NftMintResult>>(emptyList())
    val onChainMints: StateFlow<List<com.example.data.NftMintResult>> = _onChainMints.asStateFlow()

    private val _gasPriceGwei = MutableStateFlow(21.4)
    val gasPriceGwei: StateFlow<Double> = _gasPriceGwei.asStateFlow()

    private val _contractDetails = MutableStateFlow<com.example.data.ContractDetails?>(null)
    val contractDetails: StateFlow<com.example.data.ContractDetails?> = _contractDetails.asStateFlow()

    private val _smartContractStatus = MutableStateFlow("IDLE") // IDLE, MINTING, SUCCESS, FAILED
    val smartContractStatus: StateFlow<String> = _smartContractStatus.asStateFlow()

    private val _smartContractLog = MutableStateFlow<List<String>>(listOf(
        "CONTRACT: Terminal initialized. Awaiting user on-chain action...",
        "CONTRACT: Ready to query Sepolia/Holesky Testnets."
    ))
    val smartContractLog: StateFlow<List<String>> = _smartContractLog.asStateFlow()

    init {
        // Query initial values
        fetchContractDetails()
    }

    fun updateRpcAndContractAddress(rpcUrl: String, contractAddr: String) {
        _smartContractRpcUrl.value = rpcUrl
        _smartContractAddress.value = contractAddr
        fetchContractDetails()
    }

    fun fetchContractDetails() {
        viewModelScope.launch {
            try {
                val details = smartContractService.getContractDetails(_smartContractRpcUrl.value, _smartContractAddress.value)
                _contractDetails.value = details
                val gas = smartContractService.getGasPrice(_smartContractRpcUrl.value)
                _gasPriceGwei.value = gas
                
                val logs = _smartContractLog.value.toMutableList()
                logs.add("CONTRACT: Connection established. Network: ${details.networkName}. gasPrice: %.2f Gwei".format(gas))
                _smartContractLog.value = logs
            } catch (e: Exception) {
                val logs = _smartContractLog.value.toMutableList()
                logs.add("CONTRACT ERROR: Connection failed. Fallback simulation active.")
                _smartContractLog.value = logs
            }
        }
    }

    fun mintTarotInterpretationOnChain(
        cardIndex: Int,
        mintNumber: Int,
        rarity: String,
        interpretation: String,
        walletAddress: String
    ) {
        viewModelScope.launch {
            _smartContractStatus.value = "MINTING"
            val logs = _smartContractLog.value.toMutableList()
            logs.add("TX: Initiating mint transaction of Tarot module NFT #${mintNumber} (${rarity}) to $walletAddress...")
            logs.add("TX: Generating ABI-encoded call: mintTarotNFT(address,uint256,string)...")
            _smartContractLog.value = logs

            delay(1500) // Realistic RPC handshake block time
            
            val card = com.example.data.CyberTarotDeck.getCard(cardIndex)
            val metadata = com.example.data.TarotNftMetadata(
                cardIndex = cardIndex,
                name = card.name,
                rarity = rarity,
                mintNumber = mintNumber,
                interpretation = interpretation
            )

            val result = smartContractService.mintTarotNFT(
                ownerAddress = walletAddress,
                metadata = metadata,
                rpcUrl = _smartContractRpcUrl.value,
                contractAddress = _smartContractAddress.value
            )

            val updatedLogs = _smartContractLog.value.toMutableList()
            if (result.success) {
                _smartContractStatus.value = "SUCCESS"
                _onChainMints.value = _onChainMints.value + result
                updatedLogs.add("TX CONFIRMED: Block #${result.blockNumber} successfully mined!")
                updatedLogs.add("TX HASH: ${result.txHash}")
                updatedLogs.add("TOKEN ID: #${result.tokenId} successfully allocated to address.")
                updatedLogs.add("METADATA URI: ${result.tokenUri}")
                _uiNotification.value = "🚀 On-chain NFT successfully minted! Token: #${result.tokenId}"
            } else {
                _smartContractStatus.value = "FAILED"
                updatedLogs.add("TX FAILED: ${result.errorMessage ?: "Unknown smart contract error"}")
                _uiNotification.value = "❌ Smart contract mint failed!"
            }
            _smartContractLog.value = updatedLogs
        }
    }

    fun resetSmartContractStatus() {
        _smartContractStatus.value = "IDLE"
    }

    fun updateUserProfileSetup(alias: String, avatarIndex: Int, bio: String, customAvatarUri: String? = null) {
        viewModelScope.launch {
            repository.updateUserProfileSetup(alias, avatarIndex, bio, customAvatarUri)
            _uiNotification.value = "👤 Neural Alias & Profile Matrix updated successfully!"
        }
    }

    fun compileAndSaveCustomAvatar(context: android.content.Context, alias: String, style: String) {
        viewModelScope.launch {
            _uiNotification.value = "⚡ Initializing Neural Avatar Compiler..."
            try {
                // Call Gemini to get the spec
                val specJson = GeminiService.generateAvatarSpecification(alias, style)
                _uiNotification.value = "🧠 Compiling styling coordinates from Gemini..."
                
                // Render avatar to offscreen bitmap and write to file
                val file = GeminiService.renderAvatarToBitmap(context, alias, style, specJson)
                if (file != null && file.exists()) {
                    val uriString = file.toURI().toString()
                    val profile = userProfile.value
                    if (profile != null) {
                        repository.updateUserProfileSetup(
                            alias = alias,
                            avatarIndex = profile.avatarIndex,
                            bio = profile.bio,
                            customAvatarUri = uriString
                        )
                        _uiNotification.value = "🌌 AI Avatar generated and integrated into profile successfully!"
                    }
                } else {
                    _uiNotification.value = "⚠️ Failed to render compiled vector matrix."
                }
            } catch (e: Exception) {
                _uiNotification.value = "❌ Compiler error: ${e.message}"
            }
        }
    }

    fun saveDeviceAvatarImage(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch {
            _uiNotification.value = "⚡ Transferring profile matrix data..."
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val file = java.io.File(context.filesDir, "custom_avatar.png")
                    java.io.FileOutputStream(file).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                    if (file.exists()) {
                        val uriString = file.toURI().toString()
                        val profile = userProfile.value
                        if (profile != null) {
                            repository.updateUserProfileSetup(
                                alias = profile.alias,
                                avatarIndex = profile.avatarIndex,
                                bio = profile.bio,
                                customAvatarUri = uriString
                            )
                            _uiNotification.value = "👤 Custom device avatar integrated successfully!"
                        }
                    } else {
                        _uiNotification.value = "⚠️ Failed to secure local file allocation."
                    }
                }
            } catch (e: Exception) {
                _uiNotification.value = "❌ Profile upgrade interrupted: ${e.message}"
            }
        }
    }

    fun clearCustomAvatar() {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            repository.updateUserProfileSetup(
                alias = profile.alias,
                avatarIndex = profile.avatarIndex,
                bio = profile.bio,
                customAvatarUri = null
            )
            _uiNotification.value = "👤 Reverted profile avatar to standard preset."
        }
    }

    fun performAssetTransaction(assetName: String, amount: Double, type: String, destinationAddress: String = "") {
        viewModelScope.launch {
            val success = repository.performAssetTransaction(assetName, amount, type, destinationAddress)
            if (success) {
                if (amount > 0) {
                    _uiNotification.value = "⚡ Deposit of ${String.format("%.4f", amount)} $assetName was successful!"
                } else {
                    _uiNotification.value = "🚀 Sent ${String.format("%.4f", -amount)} $assetName to $destinationAddress!"
                }
            } else {
                _uiNotification.value = "❌ Asset transaction failed! Check your balance."
            }
        }
    }

    fun swapAssets(fromAsset: String, toAsset: String, fromAmount: Double, toAmount: Double) {
        viewModelScope.launch {
            val success = repository.swapAssets(fromAsset, toAsset, fromAmount, toAmount)
            if (success) {
                _uiNotification.value = "🔄 Swap successful: ${String.format("%.4f", fromAmount)} $fromAsset -> ${String.format("%.4f", toAmount)} $toAsset"
                
                // Add AI Treasurer swap activity log
                val logs = _treasurerLogs.value.toMutableList()
                logs.add(0, "AI Treasurer [SWAP]: Swapped ${String.format("%.4f", fromAmount)} $fromAsset -> ${String.format("%.4f", toAmount)} $toAsset. Liquidity rebalanced.")
                if (logs.size > 20) logs.removeAt(logs.lastIndex)
                _treasurerLogs.value = logs
            } else {
                _uiNotification.value = "❌ Swap failed! Insufficient funds in your balance."
            }
        }
    }

    // Claim daily/6-hourly activity Weed
    fun claimActivityWeed() {
        viewModelScope.launch {
            repository.claimActivityMana(
                onSuccess = { manaRewarded, newStreak ->
                    _uiNotification.value = "🔮 WEED CLAIMED! Injected +$manaRewarded WEED to your activity node. Current streak: $newStreak."
                    
                    val logs = _treasurerLogs.value.toMutableList()
                    logs.add(0, "AI Treasurer [MINT]: Minted +$manaRewarded WEED for user ${userProfile.value?.alias ?: "Netrunner"} (Streak: $newStreak).")
                    if (logs.size > 20) logs.removeAt(logs.lastIndex)
                    _treasurerLogs.value = logs
                },
                onFailed = { errorMsg ->
                    _uiNotification.value = errorMsg
                }
            )
        }
    }

    // Purchase an ad banner
    fun purchaseAdBanner(prompt: String, theme: String, colorHex: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _uiNotification.value = "🧠 Contacting Gemini neural terminal to compile banner copy..."
            val adContent = GeminiService.generateAdBannerContent(prompt)
            val title = adContent.first
            val content = adContent.second
            
            val success = repository.purchaseAdBanner(title, content, theme, colorHex, 100)
            if (success) {
                _uiNotification.value = "📡 Broadcaster updated! Banner '$title' registered in themes."
                
                val logs = _treasurerLogs.value.toMutableList()
                logs.add(0, "AI Treasurer [BURN]: Burned 100 WEED. User registered slot: '$title'. Theme: $theme.")
                if (logs.size > 20) logs.removeAt(logs.lastIndex)
                _treasurerLogs.value = logs
                onComplete(true)
            } else {
                _uiNotification.value = "❌ Registration failed. Ensure you have at least 100 WEED."
                onComplete(false)
            }
        }
    }

    // Subscribe to Ad-Shield ad-free protocol
    fun subscribeAdFree(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.subscribeAdFree(500)
            if (success) {
                _uiNotification.value = "🛡️ AD-SHIELD ACTIVATED! 30 days of pristine experience unlocked."
                
                val logs = _treasurerLogs.value.toMutableList()
                logs.add(0, "AI Treasurer [BURN]: Burned 500 WEED. User subscribed to monthly Ad-Shield.")
                if (logs.size > 20) logs.removeAt(logs.lastIndex)
                _treasurerLogs.value = logs
                onComplete(true)
            } else {
                _uiNotification.value = "❌ Activation failed! Requires 500 WEED."
                onComplete(false)
            }
        }
    }

    // Click tracker
    fun incrementAdClicks(adId: Int) {
        viewModelScope.launch {
            repository.incrementAdClicks(adId)
        }
    }

    // Toggle ad theme filters (must maintain at least 3)
    fun toggleAdTheme(theme: String) {
        val currentSet = _enabledAdThemes.value
        if (currentSet.contains(theme)) {
            if (currentSet.size > 3) {
                _enabledAdThemes.value = currentSet - theme
                _uiNotification.value = "📡 Ad queue filtered: disabled theme '$theme'."
            } else {
                _uiNotification.value = "⚠️ Queue Balance Constraint! You must keep at least 3 active themes."
            }
        } else {
            _enabledAdThemes.value = currentSet + theme
            _uiNotification.value = "📡 Ad queue updated: enabled theme '$theme'."
        }
    }

    // ======================== SWARM CRYPTO-STORAGE CORES ========================
    val swarmManager = SwarmStorageManager.getInstance(getApplication())

    val swarmContainerAllocatedSize = swarmManager.containerAllocatedSize
    val swarmContainerMounted = swarmManager.containerMounted
    val swarmNodeRole = swarmManager.nodeRole
    val swarmNodeActive = swarmManager.nodeActive
    val swarmDiscoveredPeers = swarmManager.discoveredPeers
    val swarmNodeLogs = swarmManager.nodeLogs
    val isSwarmAllocating = swarmManager.isAllocating
    val swarmAllocationProgress = swarmManager.allocationProgress

    fun allocateSwarmContainer(sizeInBytes: Long, passphrase: String) {
        swarmManager.allocateContainer(sizeInBytes, passphrase) { success ->
            if (success) {
                postNotification("💾 SWARM: Secure physical container allocated successfully!")
            } else {
                postNotification("❌ SWARM: Physical allocation failed. Check device free storage space.")
            }
        }
    }

    fun deallocateSwarmContainer() {
        swarmManager.deallocateContainer()
        postNotification("🧹 SWARM: Container deleted and zeroed.")
    }

    fun mountSwarmContainer(passphrase: String) {
        val success = swarmManager.mountContainer(passphrase)
        if (success) {
            postNotification("🔓 SWARM: Volume decrypted & mounted in RAM.")
        } else {
            postNotification("❌ SWARM: Password invalid. Cryptographic volume remains locked.")
        }
    }

    fun unmountSwarmContainer() {
        swarmManager.unmountContainer()
        postNotification("🔒 SWARM: Container unmounted. Keys purged.")
    }

    private val _isSpeedTesting = MutableStateFlow(false)
    val isSpeedTesting: StateFlow<Boolean> = _isSpeedTesting.asStateFlow()

    val globalNodeLeaderboard: StateFlow<List<GlobalNodeRank>> = userProfile.map { profile ->
        if (profile == null) return@map emptyList()
        
        // Base mock nodes
        val mockPeers = mutableListOf(
            GlobalNodeRank("Satoshi_Vault_01", "Oracle", 2500, 12.5, "ONLINE"),
            GlobalNodeRank("Tokyo_Transit_99", "Oracle", 1800, 9.4, "ONLINE"),
            GlobalNodeRank("Berlin_Relay_42", "Validator", 1200, 8.1, "ONLINE"),
            GlobalNodeRank("CyberWalk_Storage_07", "Validator", 850, 6.2, "ONLINE"),
            GlobalNodeRank("Zero_Knowledge_Node", "Validator", 700, 4.5, "ONLINE"),
            GlobalNodeRank("Hacker_Basement_X", "Relay", 450, 5.5, "ONLINE"),
            GlobalNodeRank("Proxy_War_Relay", "Relay", 300, 3.8, "ONLINE"),
            GlobalNodeRank("Netrunner_Core", "Storage", 200, 4.1, "ONLINE"),
            GlobalNodeRank("Sol_Gate_Storage", "Storage", 150, 1.8, "LOW_SPEED"),
            GlobalNodeRank("Alpha_Web_Transit", "Storage", 120, 3.2, "ONLINE")
        )
        
        val userNode = GlobalNodeRank(
            name = "${profile.alias} (YOU)",
            role = swarmNodeRole.value,
            stake = profile.nodeStake,
            speedMbps = profile.nodeBandwidthMbps,
            status = if (!profile.nodeIsOnline) "OFFLINE" else if (profile.nodeBandwidthMbps < 3.0) "LOW_SPEED" else "ONLINE",
            isUser = true
        )
        
        // Include user if node stake is > 0 and node speed is >= 3.0 Mbps and node is active/online
        val isEligible = profile.nodeIsOnline && profile.nodeBandwidthMbps >= 3.0 && profile.nodeStake >= 100
        
        if (isEligible) {
            mockPeers.add(userNode)
        }
        
        mockPeers.sortByDescending { it.stake }
        mockPeers
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getRequiredStakeForRole(role: String): Int {
        return when (role.trim().lowercase()) {
            "storage" -> 100
            "relay" -> 250
            "validator" -> 500
            "oracle" -> 1000
            else -> 100
        }
    }

    fun stakeSwarmWeed(amount: Int) {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            if (profile.weed < amount) {
                postNotification("⚠️ Insufficient WEED in main balance: ${profile.weed}. Claim more or trade cards!")
                return@launch
            }
            val success = repository.stakeWeed(amount)
            if (success) {
                postNotification("🔒 Successfully staked $amount WEED onto your Node balance!")
                swarmManager.addLog("🔒 Staked $amount WEED. New node stake: ${profile.nodeStake + amount} WEED.")
            }
        }
    }

    fun unstakeSwarmWeed(amount: Int) {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            if (profile.nodeStake < amount) {
                postNotification("⚠️ Cannot unstake more than currently staked: ${profile.nodeStake} WEED.")
                return@launch
            }
            val success = repository.unstakeWeed(amount)
            if (success) {
                val newStake = profile.nodeStake - amount
                postNotification("🔓 Successfully unstaked $amount WEED.")
                swarmManager.addLog("🔓 Unstaked $amount WEED. Remaining node stake: $newStake WEED.")
                
                // Check if current stake is still enough for current role
                val minReq = getRequiredStakeForRole(swarmNodeRole.value)
                if (newStake < minReq) {
                    if (newStake >= 500) {
                        repository.updateNodeOnlineState(profile.nodeIsOnline)
                        swarmManager.setNodeRole("Validator")
                        postNotification("⚠️ Node stake reduced. Automatically demoted to VALIDATOR role.")
                        swarmManager.addLog("⚠️ Role automatically adjusted to VALIDATOR due to stake reduction.")
                    } else if (newStake >= 250) {
                        repository.updateNodeOnlineState(profile.nodeIsOnline)
                        swarmManager.setNodeRole("Relay")
                        postNotification("⚠️ Node stake reduced. Automatically demoted to RELAY role.")
                        swarmManager.addLog("⚠️ Role automatically adjusted to RELAY due to stake reduction.")
                    } else if (newStake >= 100) {
                        repository.updateNodeOnlineState(profile.nodeIsOnline)
                        swarmManager.setNodeRole("Storage")
                        postNotification("⚠️ Node stake reduced. Automatically demoted to STORAGE role.")
                        swarmManager.addLog("⚠️ Role automatically adjusted to STORAGE due to stake reduction.")
                    } else {
                        swarmManager.stopNode()
                        repository.updateNodeOnlineState(false)
                        swarmManager.setNodeRole("Storage")
                        postNotification("🔴 CRITICAL: Node stake falls below 100 WEED! Node deactivated.")
                        swarmManager.addLog("🔴 Node shutdown. Min stake of 100 WEED required to run any node mode.")
                    }
                }
            }
        }
    }

    fun runBandwidthSpeedTest() {
        if (_isSpeedTesting.value) return
        _isSpeedTesting.value = true
        viewModelScope.launch {
            postNotification("📡 Speed Test: Initializing multi-tier network transit benchmark...")
            swarmManager.addLog("📡 Starting Network Bandwidth Speed Test...")
            delay(1000)
            swarmManager.addLog("📡 Probing public WAN gateways for transit route latency...")
            delay(1000)
            
            var measuredSpeedMbps = 0.0
            var tierUsed = "NONE"
            
            // Tier 1: Real WAN Bandwidth measurement
            withContext(Dispatchers.IO) {
                try {
                    val targetUrl = java.net.URL("https://www.google.com")
                    val startTime = System.currentTimeMillis()
                    val connection = targetUrl.openConnection() as java.net.HttpURLConnection
                    connection.connectTimeout = 3000
                    connection.readTimeout = 3000
                    connection.useCaches = false
                    connection.connect()
                    
                    var totalBytes = 0L
                    connection.inputStream.use { stream ->
                        val buffer = ByteArray(4096)
                        var read: Int
                        while (stream.read(buffer).also { read = it } != -1 && totalBytes < 512 * 1024) {
                            totalBytes += read
                        }
                    }
                    val durationMs = System.currentTimeMillis() - startTime
                    if (totalBytes > 0 && durationMs > 0) {
                        val durationSec = durationMs.coerceAtLeast(50L) / 1000.0
                        // (bytes * 8) / (seconds * 1,000,000) = Mbps
                        measuredSpeedMbps = (totalBytes * 8.0) / (durationSec * 1_000_000.0)
                        tierUsed = "WAN"
                    }
                } catch (e: Exception) {
                    android.util.Log.d("TarotViewModel", "WAN speed test failed, falling back to local storage Tier. Error: ${e.message}")
                }
            }
            
            // Tier 2: Fallback to Cryptographic Storage I/O throughput
            if (measuredSpeedMbps <= 0.0) {
                swarmManager.addLog("⚠️ WAN gateway unreachable. Activating local storage micro-benchmark...")
                delay(1000)
                withContext(Dispatchers.IO) {
                    try {
                        val testSize = 256 * 1024 // 256 KB
                        val testBytes = ByteArray(testSize)
                        Random.nextBytes(testBytes)
                        
                        val isMounted = swarmManager.containerMounted.value
                        val wasAllocated = swarmManager.containerAllocatedSize.value > 0L
                        
                        if (wasAllocated && isMounted) {
                            val startTime = System.currentTimeMillis()
                            // Write and encrypt to container shard block #99 (reserved for testing)
                            val writeSuccess = swarmManager.writeShardBlock(99, testBytes)
                            // Read and decrypt
                            val readBytes = swarmManager.readShardBlock(99)
                            val durationMs = System.currentTimeMillis() - startTime
                            
                            if (writeSuccess && readBytes != null && readBytes.size == testSize && durationMs > 0) {
                                val durationSec = durationMs.coerceAtLeast(10L) / 1000.0
                                // Translate storage I/O (MB/s) to equivalent connection capacity
                                val ioMegabytesPerSec = (testSize * 2.0 / (1024 * 1024)) / durationSec
                                measuredSpeedMbps = (ioMegabytesPerSec * 8.0).coerceIn(1.5, 24.5)
                                tierUsed = "CRYPTO_IO"
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.d("TarotViewModel", "Local Crypto IO fallback benchmark failed: ${e.message}")
                    }
                }
            }
            
            // Tier 3: Hard-safety simulation fallback if both tests fail (e.g. no container mounted and offline)
            if (measuredSpeedMbps <= 0.0) {
                swarmManager.addLog("⚠️ Local container locked. Generating simulated node gateway transit telemetry...")
                delay(800)
                measuredSpeedMbps = if (Random.nextFloat() < 0.8f) {
                    Random.nextDouble(3.1, 15.0)
                } else {
                    Random.nextDouble(0.5, 2.9)
                }
                tierUsed = "SIMULATED_PROBE"
            }
            
            repository.updateNodeSpeed(measuredSpeedMbps)
            _isSpeedTesting.value = false
            
            val formattedSpeed = String.format("%.2f", measuredSpeedMbps)
            val benchmarkSource = when (tierUsed) {
                "WAN" -> "Global WAN gateway"
                "CRYPTO_IO" -> "Local secure crypto disk interface"
                else -> "Local network gateway probe"
            }
            
            if (measuredSpeedMbps < 3.0) {
                postNotification("⚠️ Speed Test: $formattedSpeed Mbps ($benchmarkSource). Bandwidth too low! Node excluded from public ratings.")
                swarmManager.addLog("⚠️ Speed Test: $formattedSpeed Mbps ($benchmarkSource). Warning: Bandwidth below 3.0 Mbps baseline. Demoted from global rating tables.")
            } else {
                postNotification("📡 Speed Test Complete: $formattedSpeed Mbps ($benchmarkSource). Excellent transit capabilities!")
                swarmManager.addLog("📡 Speed Test Complete: $formattedSpeed Mbps ($benchmarkSource). Bandwidth is stable & high-throughput.")
            }
        }
    }

    fun simulateFifteenMinutesOffline() {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            if (profile.nodeStake <= 0) {
                postNotification("⚠️ Staking Balance is 0. Nothing to slash!")
                return@launch
            }
            swarmManager.addLog("🔌 Simulating 15-minute connection drop...")
            delay(1000)
            val penaltySlashed = repository.applyNodeOfflinePenalty()
            repository.updateNodeOnlineState(false)
            swarmManager.stopNode()
            postNotification("🔮 OUTAGE TRIGGERED! Node was offline for >15 mins. Slashed $penaltySlashed WEED (50% penalty).")
            swarmManager.addLog("❌ [CRITICAL] AI Treasurer: Node has been offline for >15 minutes! Slashed $penaltySlashed WEED as penalty! Rating decreased.")
        }
    }

    fun setSwarmNodeRole(role: String) {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            val req = getRequiredStakeForRole(role)
            if (profile.nodeStake < req) {
                postNotification("❌ Role $role requires at least $req WEED staked. Current stake is ${profile.nodeStake} WEED.")
                swarmManager.addLog("❌ Rejected change to $role: insufficient stake (needs $req, current ${profile.nodeStake}).")
            } else {
                swarmManager.setNodeRole(role)
                postNotification("📡 Swarm operational mode set to $role.")
            }
        }
    }

    fun startSwarmNode() {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            val req = getRequiredStakeForRole(swarmNodeRole.value)
            if (profile.nodeStake < req) {
                postNotification("❌ Cannot start node: Current role ${swarmNodeRole.value} requires $req WEED staked. (Staked: ${profile.nodeStake} WEED).")
                swarmManager.addLog("❌ Node activation blocked: stake below requirement for role ${swarmNodeRole.value}.")
            } else {
                swarmManager.startNode()
                repository.updateNodeOnlineState(true)
                postNotification("🟢 Swarm Node is now ONLINE.")
            }
        }
    }

    fun stopSwarmNode() {
        viewModelScope.launch {
            swarmManager.stopNode()
            repository.updateNodeOnlineState(false)
            postNotification("🔴 Swarm Node is now OFFLINE.")
        }
    }

    fun writeSwarmShardBlock(index: Int, content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = swarmManager.writeShardBlock(index, content.toByteArray(Charsets.UTF_8))
            if (success) {
                postNotification("📝 SWARM: Shard #$index encrypted & committed.")
            } else {
                postNotification("❌ SWARM: Write block #$index failed. Ensure volume is mounted!")
            }
        }
    }

    fun readSwarmShardBlock(index: Int, onComplete: (String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val bytes = swarmManager.readShardBlock(index)
            if (bytes != null) {
                val text = String(bytes, Charsets.UTF_8)
                postNotification("📖 SWARM: Decrypted Shard #$index: \"$text\"")
                withContext(Dispatchers.Main) {
                    onComplete(text)
                }
            } else {
                postNotification("❌ SWARM: Read block #$index failed.")
                withContext(Dispatchers.Main) {
                    onComplete(null)
                }
            }
        }
    }

    fun fetchSwarmShardBlockFromPeer(peerIp: String, index: Int, onComplete: (String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val bytes = swarmManager.fetchShardFromPeer(peerIp, index)
            if (bytes != null) {
                val text = String(bytes, Charsets.UTF_8)
                postNotification("⚡ SWARM: TCP Streamed Shard #$index from peer $peerIp.")
                
                // If mounted, replicate it locally
                if (swarmManager.containerMounted.value) {
                    val localSuccess = swarmManager.writeShardBlock(index, bytes)
                    if (localSuccess) {
                        swarmManager.addLog("Replicated downloaded Shard #$index to local secure container.")
                    }
                }
                
                withContext(Dispatchers.Main) {
                    onComplete(text)
                }
            } else {
                postNotification("❌ SWARM: Failed to stream shard #$index from peer $peerIp.")
                withContext(Dispatchers.Main) {
                    onComplete(null)
                }
            }
        }
    }

    // --- MONETIZATION, BLESSINGS, TTS, AND DAILY QUESTS SYSTEM ---

    private var tts: android.speech.tts.TextToSpeech? = null
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _blessingUptimeBuff = MutableStateFlow(sharedPrefs.getBoolean("blessing_uptime_buff", false))
    val blessingUptimeBuff: StateFlow<Boolean> = _blessingUptimeBuff.asStateFlow()

    private val _blessingRareCardBuff = MutableStateFlow(sharedPrefs.getBoolean("blessing_rare_card_buff", false))
    val blessingRareCardBuff: StateFlow<Boolean> = _blessingRareCardBuff.asStateFlow()

    private val _isVipUnlocked = MutableStateFlow(sharedPrefs.getBoolean("vip_unlocked", false))
    val isVipUnlocked: StateFlow<Boolean> = _isVipUnlocked.asStateFlow()

    private val _showBlessingsPortal = MutableStateFlow(false)
    val showBlessingsPortal: StateFlow<Boolean> = _showBlessingsPortal.asStateFlow()

    fun setBlessingsPortalVisible(visible: Boolean) {
        _showBlessingsPortal.value = visible
    }

    // Daily Quests States
    private val _questCompletedScan = MutableStateFlow(sharedPrefs.getBoolean("quest_completed_scan", false))
    val questCompletedScan = _questCompletedScan.asStateFlow()

    private val _questClaimedScan = MutableStateFlow(sharedPrefs.getBoolean("quest_claimed_scan", false))
    val questClaimedScan = _questClaimedScan.asStateFlow()

    private val _questCompletedProxy = MutableStateFlow(sharedPrefs.getBoolean("quest_completed_proxy", false))
    val questCompletedProxy = _questCompletedProxy.asStateFlow()

    private val _questClaimedProxy = MutableStateFlow(sharedPrefs.getBoolean("quest_claimed_proxy", false))
    val questClaimedProxy = _questClaimedProxy.asStateFlow()

    private val _questCompletedTip = MutableStateFlow(sharedPrefs.getBoolean("quest_completed_tip", false))
    val questCompletedTip = _questCompletedTip.asStateFlow()

    private val _questClaimedTip = MutableStateFlow(sharedPrefs.getBoolean("quest_claimed_tip", false))
    val questClaimedTip = _questClaimedTip.asStateFlow()

    fun completeScanQuest() {
        sharedPrefs.edit().putBoolean("quest_completed_scan", true).apply()
        _questCompletedScan.value = true
    }

    fun completeProxyQuest() {
        sharedPrefs.edit().putBoolean("quest_completed_proxy", true).apply()
        _questCompletedProxy.value = true
    }

    fun completeTipQuest() {
        sharedPrefs.edit().putBoolean("quest_completed_tip", true).apply()
        _questCompletedTip.value = true
    }

    fun claimQuestReward(questType: String, reward: Int) {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            val updated = profile.copy(weed = profile.weed + reward)
            repository.updateProfile(updated)

            when (questType) {
                "scan" -> {
                    sharedPrefs.edit().putBoolean("quest_claimed_scan", true).apply()
                    _questClaimedScan.value = true
                }
                "proxy" -> {
                    sharedPrefs.edit().putBoolean("quest_claimed_proxy", true).apply()
                    _questClaimedProxy.value = true
                }
                "tip" -> {
                    sharedPrefs.edit().putBoolean("quest_claimed_tip", true).apply()
                    _questClaimedTip.value = true
                }
            }

            val lang = profile.systemLanguage
            val msg = if (lang == "ru") "🎁 Награда +$reward WEED получена!" else "🎁 Quest reward +$reward WEED claimed!"
            postNotification(msg)
            com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
        }
    }

    fun resetDailyQuests() {
        sharedPrefs.edit()
            .putBoolean("quest_completed_scan", false)
            .putBoolean("quest_claimed_scan", false)
            .putBoolean("quest_completed_proxy", false)
            .putBoolean("quest_claimed_proxy", false)
            .putBoolean("quest_completed_tip", false)
            .putBoolean("quest_claimed_tip", false)
            .apply()

        _questCompletedScan.value = false
        _questClaimedScan.value = false
        _questCompletedProxy.value = false
        _questClaimedProxy.value = false
        _questCompletedTip.value = false
        _questClaimedTip.value = false

        val profile = userProfile.value
        val lang = profile?.systemLanguage ?: "en"
        val msg = if (lang == "ru") "🔄 Ежедневные квесты матрицы сброшены." else "🔄 Daily matrix quests reset."
        postNotification(msg)
    }

    fun purchaseWeedBlessing(type: String, cost: Int) {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            if (profile.weed < cost) {
                val lang = profile.systemLanguage
                val msg = if (lang == "ru") "❌ Недостаточно кредитов WEED." else "❌ Insufficient WEED credits."
                postNotification(msg)
                return@launch
            }
            // Deduct credits
            val updated = profile.copy(weed = profile.weed - cost)
            repository.updateProfile(updated)

            // Apply buff
            when (type) {
                "uptime" -> {
                    sharedPrefs.edit().putBoolean("blessing_uptime_buff", true).apply()
                    _blessingUptimeBuff.value = true
                    val lang = profile.systemLanguage
                    val msg = if (lang == "ru") "🔮 Благословение Накопления активировано! Доход узлов удвоен!" else "🔮 Harmony Accumulator activated! Node passive mining is doubled!"
                    postNotification(msg)
                    completeTipQuest()
                }
                "rare_card" -> {
                    sharedPrefs.edit().putBoolean("blessing_rare_card_buff", true).apply()
                    _blessingRareCardBuff.value = true
                    val lang = profile.systemLanguage
                    val msg = if (lang == "ru") "🔮 Благословение Редких Карт активировано! Шанс минта редких NFT повышен!" else "🔮 Trichome Catalyst activated! Rare card drop rate increased!"
                    postNotification(msg)
                    completeTipQuest()
                }
                "vip" -> {
                    sharedPrefs.edit().putBoolean("vip_unlocked", true).apply()
                    _isVipUnlocked.value = true
                    val lang = profile.systemLanguage
                    val msg = if (lang == "ru") "👑 Все премиальные расклады разблокированы навсегда!" else "👑 Permanent VIP access! All premium spreads unlocked forever!"
                    postNotification(msg)
                    completeTipQuest()
                }
            }
            com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
        }
    }

    fun addWeedCredits(amount: Int) {
        val currentProfile = userProfile.value ?: return
        viewModelScope.launch {
            val updated = currentProfile.copy(weed = currentProfile.weed + amount)
            repository.updateProfile(updated)
            val lang = currentProfile.systemLanguage
            val msg = if (lang == "ru") "⚔️ Награда за дуэль: +$amount WEED Credits!" else "⚔️ Duel reward: +$amount WEED Credits!"
            postNotification(msg)
        }
    }

    fun deductWeedCredits(amount: Int): Boolean {
        val currentProfile = userProfile.value ?: return false
        if (currentProfile.weed >= amount) {
            viewModelScope.launch {
                val updated = currentProfile.copy(weed = currentProfile.weed - amount)
                repository.updateProfile(updated)
            }
            return true
        }
        return false
    }

    fun simulateMicroPayment(tier: String, priceUsd: Double, creditReward: Int) {
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            val updated = profile.copy(weed = profile.weed + creditReward)
            repository.updateProfile(updated)

            if (tier == "cosmic" || tier == "architect") {
                sharedPrefs.edit().putBoolean("vip_unlocked", true).apply()
                _isVipUnlocked.value = true
            }

            val lang = profile.systemLanguage
            val msg = if (lang == "ru") {
                "💳 Платеж на \$${String.format("%.2f", priceUsd)} успешно обработан! Начислено +$creditReward WEED. Статус обновлен."
            } else {
                "💳 Payment of \$${String.format("%.2f", priceUsd)} processed successfully! Credited +$creditReward WEED. Status synchronized."
            }
            postNotification(msg)
            com.example.audio.CyberNoirSynthPlayer.triggerFX(com.example.audio.CyberNoirSynthPlayer.FXType.SUCCESS_CHIME)
        }
    }

    fun speakText(text: String) {
        if (tts == null) {
            tts = android.speech.tts.TextToSpeech(getApplication()) { status ->
                if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                    tts?.language = java.util.Locale.getDefault()
                    // Glitchy voice setup
                    tts?.setPitch(0.7f) // lower pitch for mechanical cyber sound
                    tts?.setSpeechRate(0.9f) // slower rate for oracle feel
                    speakOut(text)
                }
            }
        } else {
            speakOut(text)
        }
    }

    private fun speakOut(text: String) {
        _isSpeaking.value = true
        val params = android.os.Bundle()
        params.putString(android.speech.tts.TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "OracleSpeechID")
        tts?.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, params, "OracleSpeechID")
        
        tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
            }
            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
            }
        })
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
    }

    override fun onCleared() {
        super.onCleared()
        tts?.shutdown()
    }
}

