package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CyberTarotCard
import com.example.data.CyberTarotDeck
import com.example.data.DivinationReading
import com.example.data.TarotRepository
import com.example.model.CardOrientation
import com.example.model.CyberTarotCardModel
import com.example.model.CyberTarotDeckFactory
import com.example.model.DrawnCard
import com.example.model.ReadingPhase
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

class OracleViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TarotRepository.getInstance(application)

    val divinationReadings: StateFlow<List<DivinationReading>> = repository.divinationReadings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _divinationState = MutableStateFlow<DivinationState>(DivinationState.Idle)
    val divinationState: StateFlow<DivinationState> = _divinationState.asStateFlow()

    private val _activeDrawnCards = MutableStateFlow<List<Pair<CyberTarotCard, Boolean>>>(emptyList())
    val activeDrawnCards: StateFlow<List<Pair<CyberTarotCard, Boolean>>> = _activeDrawnCards.asStateFlow()

    // New Deck Model & Reading Logic States
    private val _readingPhase = MutableStateFlow(ReadingPhase.SETUP)
    val readingPhase: StateFlow<ReadingPhase> = _readingPhase.asStateFlow()

    private val _selectedSpread = MutableStateFlow(TarotSpread(SpreadType.CYBER_TRINITY))
    val selectedSpread: StateFlow<TarotSpread> = _selectedSpread.asStateFlow()

    private val _availableDeck = MutableStateFlow<List<CyberTarotCardModel>>(CyberTarotDeckFactory.createDefaultDeck())
    val availableDeck: StateFlow<List<CyberTarotCardModel>> = _availableDeck.asStateFlow()

    private val _drawnCardsModel = MutableStateFlow<List<DrawnCard>>(emptyList())
    val drawnCardsModel: StateFlow<List<DrawnCard>> = _drawnCardsModel.asStateFlow()

    private val _isShuffling = MutableStateFlow(false)
    val isShuffling: StateFlow<Boolean> = _isShuffling.asStateFlow()

    private val _shuffleProgress = MutableStateFlow(0f)
    val shuffleProgress: StateFlow<Float> = _shuffleProgress.asStateFlow()

    fun setSpread(type: SpreadType) {
        _selectedSpread.value = TarotSpread(type)
        _drawnCardsModel.value = emptyList()
        _readingPhase.value = ReadingPhase.SETUP
    }

    /**
     * Executes deck shuffling with algorithm of choice.
     */
    fun shuffleDeck(method: ShuffleMethod = ShuffleMethod.QUANTUM_ENTROPY) {
        if (_isShuffling.value) return
        viewModelScope.launch {
            _isShuffling.value = true
            _readingPhase.value = ReadingPhase.SHUFFLING
            _shuffleProgress.value = 0.2f

            val current = _availableDeck.value.toMutableList()
            when (method) {
                ShuffleMethod.QUANTUM_ENTROPY -> {
                    delay(300)
                    val secureRandom = SecureRandom()
                    for (i in current.size - 1 downTo 1) {
                        val j = secureRandom.nextInt(i + 1)
                        val temp = current[i]
                        current[i] = current[j]
                        current[j] = temp
                    }
                    _shuffleProgress.value = 0.8f
                    delay(200)
                }
                ShuffleMethod.RIFFLE -> {
                    repeat(3) { pass ->
                        delay(200)
                        current.shuffle(Random)
                        _shuffleProgress.value = 0.3f * (pass + 1)
                    }
                }
                ShuffleMethod.OVERHAND, ShuffleMethod.CORONA_WASH -> {
                    delay(400)
                    current.shuffle(Random)
                    _shuffleProgress.value = 0.85f
                }
            }

            _availableDeck.value = current
            _drawnCardsModel.value = emptyList()
            _activeDrawnCards.value = emptyList()
            _shuffleProgress.value = 1f
            delay(150)
            _isShuffling.value = false
            _readingPhase.value = ReadingPhase.CUTTING
        }
    }

    /**
     * Cuts the deck at a specified fraction and swaps halves.
     */
    fun cutDeck(cutRatio: Float = 0.5f) {
        val current = _availableDeck.value
        if (current.isEmpty()) return
        val cutIndex = (current.size * cutRatio.coerceIn(0.15f, 0.85f)).toInt()
        _availableDeck.value = current.drop(cutIndex) + current.take(cutIndex)
        _readingPhase.value = ReadingPhase.CARD_SELECTION
    }

    /**
     * Selects a specific card from the available deck.
     */
    fun selectCard(deckIndex: Int) {
        val currentDeck = _availableDeck.value
        if (deckIndex !in currentDeck.indices) return

        val spread = _selectedSpread.value
        val drawn = _drawnCardsModel.value
        if (drawn.size >= spread.cardCount) return

        val card = currentDeck[deckIndex]
        val remaining = currentDeck.toMutableList().apply { removeAt(deckIndex) }
        _availableDeck.value = remaining

        val isReversed = Random.nextFloat() < 0.35f
        val orientation = if (isReversed) CardOrientation.REVERSED else CardOrientation.UPRIGHT
        val position = spread.positions[drawn.size]

        val drawnCard = DrawnCard(
            card = card,
            orientation = orientation,
            position = position,
            isRevealed = true,
            selectionOrder = drawn.size + 1
        )

        val updatedDrawn = drawn + drawnCard
        _drawnCardsModel.value = updatedDrawn
        _activeDrawnCards.value = updatedDrawn.map { it.card.toCyberTarotCard() to it.isUpright }

        if (updatedDrawn.size == spread.cardCount) {
            _readingPhase.value = ReadingPhase.REVEALING
        }
    }

    /**
     * Auto deals cards to complete the spread.
     */
    fun autoDeal() {
        val spread = _selectedSpread.value
        val currentDeck = _availableDeck.value.toMutableList()
        val drawn = _drawnCardsModel.value.toMutableList()

        while (drawn.size < spread.cardCount && currentDeck.isNotEmpty()) {
            val card = currentDeck.removeAt(Random.nextInt(currentDeck.size))
            val isReversed = Random.nextFloat() < 0.35f
            val orientation = if (isReversed) CardOrientation.REVERSED else CardOrientation.UPRIGHT
            val position = spread.positions[drawn.size]

            drawn.add(
                DrawnCard(
                    card = card,
                    orientation = orientation,
                    position = position,
                    isRevealed = true,
                    selectionOrder = drawn.size + 1
                )
            )
        }

        _availableDeck.value = currentDeck
        _drawnCardsModel.value = drawn
        _activeDrawnCards.value = drawn.map { it.card.toCyberTarotCard() to it.isUpright }
        _readingPhase.value = ReadingPhase.REVEALING
    }

    fun performDivination(question: String, spreadType: String = "CelticCross") {
        viewModelScope.launch {
            _divinationState.value = DivinationState.Scanning
            delay(1200)

            val count = if (spreadType == "ThreeCards") 3 else 10
            val drawn = CyberTarotDeck.cards.shuffled().take(count).map { card ->
                Pair(card, Random.nextBoolean())
            }
            _activeDrawnCards.value = drawn

            val report = buildString {
                append("🔮 [CYBER ORACLE SPREAD REPORT]\n")
                append("Query: \"$question\"\n\n")
                drawn.forEachIndexed { i, (c, up) ->
                    append("${i + 1}. ${c.name} (${if (up) "Upright" else "Reversed"}): ${if (up) c.uprightAdvice else c.reversedAdvice}\n")
                }
            }

            repository.saveDivinationReading(
                question = question,
                cardIndices = drawn.map { it.first.index },
                isUpright = drawn.map { it.second },
                report = report
            )

            _divinationState.value = DivinationState.Success(question, drawn, report)
        }
    }

    fun resetDivination() {
        _divinationState.value = DivinationState.Idle
        _activeDrawnCards.value = emptyList()
        _drawnCardsModel.value = emptyList()
        _availableDeck.value = CyberTarotDeckFactory.createDefaultDeck()
        _readingPhase.value = ReadingPhase.SETUP
    }

    fun deleteReading(id: Int) {
        viewModelScope.launch {
            repository.deleteDivinationReading(id)
        }
    }
}
