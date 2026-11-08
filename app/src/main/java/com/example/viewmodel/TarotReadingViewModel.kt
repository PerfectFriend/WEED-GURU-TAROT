package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DivinationReading
import com.example.data.GeminiService
import com.example.data.TarotRepository
import com.example.model.CardOrientation
import com.example.model.CyberTarotCardModel
import com.example.model.CyberTarotDeckFactory
import com.example.model.DrawnCard
import com.example.model.ReadingPhase
import com.example.model.ReadingSession
import com.example.model.ShuffleMethod
import com.example.model.SpreadType
import com.example.model.TarotSpread
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.security.SecureRandom
import kotlin.random.Random

class TarotReadingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TarotRepository.getInstance(application)

    // Current reading flow phase
    private val _readingPhase = MutableStateFlow(ReadingPhase.SETUP)
    val readingPhase: StateFlow<ReadingPhase> = _readingPhase.asStateFlow()

    // Query / prompt
    private val _userQuestion = MutableStateFlow("")
    val userQuestion: StateFlow<String> = _userQuestion.asStateFlow()

    // Active spread configuration
    private val _selectedSpread = MutableStateFlow(TarotSpread(SpreadType.CYBER_TRINITY))
    val selectedSpread: StateFlow<TarotSpread> = _selectedSpread.asStateFlow()

    // Full deck available for drawing (facedown fan)
    private val _availableDeck = MutableStateFlow<List<CyberTarotCardModel>>(emptyList())
    val availableDeck: StateFlow<List<CyberTarotCardModel>> = _availableDeck.asStateFlow()

    // Drawn cards mapped to spread positions
    private val _drawnCards = MutableStateFlow<List<DrawnCard>>(emptyList())
    val drawnCards: StateFlow<List<DrawnCard>> = _drawnCards.asStateFlow()

    // Shuffling animation & telemetry
    private val _isShuffling = MutableStateFlow(false)
    val isShuffling: StateFlow<Boolean> = _isShuffling.asStateFlow()

    private val _shuffleProgress = MutableStateFlow(0f)
    val shuffleProgress: StateFlow<Float> = _shuffleProgress.asStateFlow()

    private val _shufflePhaseDescription = MutableStateFlow("")
    val shufflePhaseDescription: StateFlow<String> = _shufflePhaseDescription.asStateFlow()

    private val _activeShuffleMethod = MutableStateFlow(ShuffleMethod.QUANTUM_ENTROPY)
    val activeShuffleMethod: StateFlow<ShuffleMethod> = _activeShuffleMethod.asStateFlow()

    // Custom reversal probability (0.0 = only upright, 0.5 = 50% chance of reversal)
    private val _inversionProbability = MutableStateFlow(0.35f)
    val inversionProbability: StateFlow<Float> = _inversionProbability.asStateFlow()

    // Reading Interpretation
    private val _interpretation = MutableStateFlow("")
    val interpretation: StateFlow<String> = _interpretation.asStateFlow()

    private val _isInterpreting = MutableStateFlow(false)
    val isInterpreting: StateFlow<Boolean> = _isInterpreting.asStateFlow()

    // Completed active session
    private val _activeSession = MutableStateFlow<ReadingSession?>(null)
    val activeSession: StateFlow<ReadingSession?> = _activeSession.asStateFlow()

    // History from database
    val divinationReadings: StateFlow<List<DivinationReading>> = repository.divinationReadings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        resetDeck()
    }

    /**
     * Resets the deck to pristine factory ordering.
     */
    fun resetDeck() {
        _availableDeck.value = CyberTarotDeckFactory.createDefaultDeck()
        _drawnCards.value = emptyList()
        _interpretation.value = ""
        _activeSession.value = null
        _readingPhase.value = ReadingPhase.SETUP
    }

    fun setQuestion(query: String) {
        _userQuestion.value = query
    }

    fun setSpread(type: SpreadType) {
        _selectedSpread.value = TarotSpread(type)
        // If cards were already drawn, reset them to avoid mismatch
        if (_drawnCards.value.isNotEmpty()) {
            _drawnCards.value = emptyList()
            _readingPhase.value = ReadingPhase.CARD_SELECTION
        }
    }

    fun setInversionProbability(probability: Float) {
        _inversionProbability.value = probability.coerceIn(0f, 1f)
    }

    /**
     * Executes authentic deck shuffling according to selected algorithm.
     */
    fun shuffleDeck(method: ShuffleMethod = ShuffleMethod.QUANTUM_ENTROPY) {
        if (_isShuffling.value) return

        _activeShuffleMethod.value = method
        viewModelScope.launch {
            _isShuffling.value = true
            _readingPhase.value = ReadingPhase.SHUFFLING
            _shuffleProgress.value = 0.05f

            val current = _availableDeck.value.ifEmpty { CyberTarotDeckFactory.createDefaultDeck() }
            val mutableList = current.toMutableList()

            when (method) {
                ShuffleMethod.QUANTUM_ENTROPY -> {
                    _shufflePhaseDescription.value = "Generating quantum entropy seed..."
                    delay(300)
                    _shuffleProgress.value = 0.35f

                    val secureRandom = SecureRandom()
                    // Fisher-Yates shuffle with cryptographic entropy
                    for (i in mutableList.size - 1 downTo 1) {
                        val j = secureRandom.nextInt(i + 1)
                        val temp = mutableList[i]
                        mutableList[i] = mutableList[j]
                        mutableList[j] = temp
                    }
                    _shufflePhaseDescription.value = "Permuting node matrix..."
                    delay(400)
                    _shuffleProgress.value = 0.85f
                    delay(300)
                }

                ShuffleMethod.RIFFLE -> {
                    // Simulate physical riffle shuffle: splitting in halves and interleaving
                    _shufflePhaseDescription.value = "Splitting deck into left and right streams..."
                    delay(350)
                    _shuffleProgress.value = 0.3f

                    repeat(3) { pass ->
                        val mid = mutableList.size / 2 + Random.nextInt(-2, 3)
                        val left = mutableList.take(mid).toMutableList()
                        val right = mutableList.drop(mid).toMutableList()
                        val interleaved = mutableListOf<CyberTarotCardModel>()

                        while (left.isNotEmpty() || right.isNotEmpty()) {
                            val takeLeft = when {
                                left.isEmpty() -> false
                                right.isEmpty() -> true
                                else -> Random.nextBoolean()
                            }
                            if (takeLeft) {
                                interleaved.add(left.removeAt(0))
                            } else {
                                interleaved.add(right.removeAt(0))
                            }
                        }
                        mutableList.clear()
                        mutableList.addAll(interleaved)
                        _shuffleProgress.value = 0.3f + (pass + 1) * 0.2f
                        _shufflePhaseDescription.value = "Riffle pass ${pass + 1} of 3..."
                        delay(250)
                    }
                }

                ShuffleMethod.OVERHAND -> {
                    _shufflePhaseDescription.value = "Overhand cascading packet shuffle..."
                    delay(250)
                    repeat(4) { pass ->
                        val reordered = mutableListOf<CyberTarotCardModel>()
                        var index = 0
                        while (index < mutableList.size) {
                            val chunkSize = Random.nextInt(4, 9).coerceAtMost(mutableList.size - index)
                            val chunk = mutableList.subList(index, index + chunkSize)
                            reordered.addAll(0, chunk) // prepend block
                            index += chunkSize
                        }
                        mutableList.clear()
                        mutableList.addAll(reordered)
                        _shuffleProgress.value = 0.25f * (pass + 1)
                        _shufflePhaseDescription.value = "Cascading packet pass ${pass + 1} of 4..."
                        delay(200)
                    }
                }

                ShuffleMethod.CORONA_WASH -> {
                    _shufflePhaseDescription.value = "Chaotic vortex table wash..."
                    delay(300)
                    _shuffleProgress.value = 0.5f
                    mutableList.shuffle(Random)
                    delay(400)
                    _shuffleProgress.value = 0.9f
                    delay(200)
                }
            }

            _availableDeck.value = mutableList
            _drawnCards.value = emptyList()
            _shuffleProgress.value = 1f
            _shufflePhaseDescription.value = "Deck ready. Proceeding to cut or card selection."
            delay(250)
            _isShuffling.value = false
            _readingPhase.value = ReadingPhase.CUTTING
        }
    }

    /**
     * Cuts the deck at a specified ratio (e.g., 0.5 for middle), swapping top and bottom halves.
     */
    fun cutDeck(cutRatio: Float = 0.5f) {
        val current = _availableDeck.value
        if (current.isEmpty()) return

        val clampedRatio = cutRatio.coerceIn(0.15f, 0.85f)
        val cutIndex = (current.size * clampedRatio).toInt()

        val topHalf = current.take(cutIndex)
        val bottomHalf = current.drop(cutIndex)

        // Swap packets
        _availableDeck.value = bottomHalf + topHalf
        _readingPhase.value = ReadingPhase.CARD_SELECTION
    }

    /**
     * Skips deck cutting and goes directly to card selection.
     */
    fun skipCutting() {
        _readingPhase.value = ReadingPhase.CARD_SELECTION
    }

    /**
     * Allows user to manually select a card by index from the available deck.
     */
    fun selectCardFromDeck(deckIndex: Int) {
        val deck = _availableDeck.value
        if (deckIndex !in deck.indices) return

        val spread = _selectedSpread.value
        val currentDrawn = _drawnCards.value
        if (currentDrawn.size >= spread.cardCount) return

        val selectedCard = deck[deckIndex]
        val remainingDeck = deck.toMutableList().apply { removeAt(deckIndex) }
        _availableDeck.value = remainingDeck

        val nextPosition = spread.positions[currentDrawn.size]
        val isReversed = Random.nextFloat() < _inversionProbability.value
        val orientation = if (isReversed) CardOrientation.REVERSED else CardOrientation.UPRIGHT

        val newDrawnCard = DrawnCard(
            card = selectedCard,
            orientation = orientation,
            position = nextPosition,
            isRevealed = false,
            selectionOrder = currentDrawn.size + 1
        )

        val updatedDrawn = currentDrawn + newDrawnCard
        _drawnCards.value = updatedDrawn

        // If spread is fully dealt, advance to reveal phase
        if (updatedDrawn.size == spread.cardCount) {
            _readingPhase.value = ReadingPhase.REVEALING
        }
    }

    /**
     * Automatically deals cards until the spread is completely filled.
     */
    fun autoDealRemaining() {
        val spread = _selectedSpread.value
        var currentDrawn = _drawnCards.value
        var remainingDeck = _availableDeck.value.toMutableList()

        while (currentDrawn.size < spread.cardCount && remainingDeck.isNotEmpty()) {
            val randomIndex = Random.nextInt(remainingDeck.size)
            val card = remainingDeck.removeAt(randomIndex)
            val nextPosition = spread.positions[currentDrawn.size]
            val isReversed = Random.nextFloat() < _inversionProbability.value
            val orientation = if (isReversed) CardOrientation.REVERSED else CardOrientation.UPRIGHT

            val drawnCard = DrawnCard(
                card = card,
                orientation = orientation,
                position = nextPosition,
                isRevealed = false,
                selectionOrder = currentDrawn.size + 1
            )
            currentDrawn = currentDrawn + drawnCard
        }

        _availableDeck.value = remainingDeck
        _drawnCards.value = currentDrawn
        _readingPhase.value = ReadingPhase.REVEALING
    }

    /**
     * Reveals (flips face-up) a card at the given spread position.
     */
    fun revealCard(positionIndex: Int) {
        val current = _drawnCards.value
        if (positionIndex !in current.indices) return

        _drawnCards.value = current.mapIndexed { i, card ->
            if (i == positionIndex) card.copy(isRevealed = true) else card
        }
    }

    /**
     * Reveals all dealt cards in the spread at once.
     */
    fun revealAllCards() {
        _drawnCards.value = _drawnCards.value.map { it.copy(isRevealed = true) }
    }

    /**
     * Toggles orientation of a card between Upright and Reversed.
     */
    fun toggleOrientation(positionIndex: Int) {
        val current = _drawnCards.value
        if (positionIndex !in current.indices) return

        _drawnCards.value = current.mapIndexed { i, card ->
            if (i == positionIndex) {
                val newOrientation = if (card.orientation == CardOrientation.UPRIGHT) {
                    CardOrientation.REVERSED
                } else {
                    CardOrientation.UPRIGHT
                }
                card.copy(orientation = newOrientation)
            } else {
                card
            }
        }
    }

    /**
     * Generates an interpretation for the reading and saves it to repository history.
     */
    fun interpretReading(systemLanguage: String = "en") {
        val drawn = _drawnCards.value
        val spread = _selectedSpread.value
        val question = _userQuestion.value.ifBlank { "General Guidance" }

        if (drawn.isEmpty()) return

        // Ensure all cards are revealed
        revealAllCards()

        viewModelScope.launch {
            _isInterpreting.value = true
            _readingPhase.value = ReadingPhase.SYNTHESIS

            // Convert to format accepted by GeminiService
            val cardPairs = drawn.map { drawnCard ->
                drawnCard.card.toCyberTarotCard() to drawnCard.isUpright
            }

            // Generate report through Gemini or synthetic breakdown
            val report = try {
                GeminiService.decodeTarotMatrix(
                    userQuestion = question,
                    drawnCards = cardPairs,
                    systemLanguage = systemLanguage
                )
            } catch (e: Exception) {
                buildLocalSynthesisReport(question, spread, drawn, systemLanguage)
            }

            _interpretation.value = report

            // Save to room database
            val readingId = repository.saveDivinationReading(
                question = question,
                cardIndices = drawn.map { it.card.id },
                isUpright = drawn.map { it.isUpright },
                report = report
            )

            // Reward credits in repository
            repository.recordDivinationDraw(spread.cardCount)

            _activeSession.value = ReadingSession(
                question = question,
                spread = spread,
                drawnCards = drawn,
                interpretation = report,
                isSaved = true
            )

            _isInterpreting.value = false
            _readingPhase.value = ReadingPhase.COMPLETED
        }
    }

    fun saveUserNotes(notes: String) {
        _activeSession.value = _activeSession.value?.copy(userNotes = notes)
    }

    fun resetReading() {
        resetDeck()
    }

    private fun buildLocalSynthesisReport(
        question: String,
        spread: TarotSpread,
        cards: List<DrawnCard>,
        lang: String
    ): String = buildString {
        val isRu = lang in listOf("ru", "uk")
        append(if (isRu) "🔮 [МАТРИЧНЫЙ ОТЧЕТ КИБЕР-ТАРО]\n" else "🔮 [CYBER TAROT MATRIX REPORT]\n")
        append(if (isRu) "Запрос: \"$question\"\n" else "Query: \"$question\"\n")
        append(if (isRu) "Расклад: ${spread.type.titleRu}\n\n" else "Spread: ${spread.type.titleEn}\n\n")

        cards.forEachIndexed { i, drawn ->
            val pos = drawn.position
            val posTitle = if (isRu) pos.titleRu else pos.titleEn
            val orientationLabel = if (drawn.isUpright) {
                if (isRu) "Прямое положение" else "Upright"
            } else {
                if (isRu) "Перевернутое положение" else "Reversed"
            }
            append("${i + 1}. $posTitle — ${drawn.card.name} [$orientationLabel]\n")
            append("   ${drawn.card.circuitryElement}\n")
            append("   ${if (isRu) "Совет" else "Advice"}: ${drawn.effectiveAdvice}\n\n")
        }
    }
}
