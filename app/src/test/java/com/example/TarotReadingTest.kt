package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.model.ArcanaType
import com.example.model.CardOrientation
import com.example.model.CardRarity
import com.example.model.CyberTarotDeckFactory
import com.example.model.ReadingPhase
import com.example.model.ShuffleMethod
import com.example.model.SpreadType
import com.example.viewmodel.TarotReadingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class TarotReadingTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testDeckCreationAndCardProperties() {
        val deck = CyberTarotDeckFactory.createDefaultDeck()
        assertTrue("Deck should contain cards", deck.isNotEmpty())

        val sprout = deck.first { it.id == 0 }
        assertEquals("The Sprout", sprout.name)
        assertEquals(ArcanaType.MAJOR, sprout.arcana)
        assertEquals(CardRarity.COMMON, sprout.rarity)
        assertTrue(sprout.uprightKeywords.isNotEmpty())
        assertTrue(sprout.reversedKeywords.isNotEmpty())
    }

    @Test
    fun testSpreadDefinitions() {
        assertEquals(1, SpreadType.SINGLE_ORACLE.cardCount)
        assertEquals(3, SpreadType.CYBER_TRINITY.cardCount)
        assertEquals(5, SpreadType.FIVE_NODE_CROSS.cardCount)
        assertEquals(10, SpreadType.CELTIC_MATRIX.cardCount)

        SpreadType.values().forEach { spread ->
            assertEquals(spread.cardCount, spread.positions.size)
            spread.positions.forEachIndexed { index, pos ->
                assertEquals(index, pos.index)
                assertTrue(pos.titleEn.isNotBlank())
                assertTrue(pos.titleRu.isNotBlank())
            }
        }
    }

    @Test
    fun testViewModelShufflingAndCardSelection() = runTest(testDispatcher) {
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = TarotReadingViewModel(context)

        // Initial setup state
        assertEquals(ReadingPhase.SETUP, viewModel.readingPhase.value)
        val initialDeck = viewModel.availableDeck.value
        assertTrue(initialDeck.isNotEmpty())

        // Choose 3-card spread
        viewModel.setSpread(SpreadType.CYBER_TRINITY)
        assertEquals(3, viewModel.selectedSpread.value.cardCount)

        // Shuffle deck
        viewModel.shuffleDeck(ShuffleMethod.QUANTUM_ENTROPY)
        advanceUntilIdle()

        assertEquals(ReadingPhase.CUTTING, viewModel.readingPhase.value)
        assertFalse(viewModel.isShuffling.value)

        // Cut deck
        viewModel.cutDeck(0.5f)
        assertEquals(ReadingPhase.CARD_SELECTION, viewModel.readingPhase.value)

        // Manually select first card
        val deckSizeBefore = viewModel.availableDeck.value.size
        viewModel.selectCardFromDeck(0)

        assertEquals(1, viewModel.drawnCards.value.size)
        assertEquals(deckSizeBefore - 1, viewModel.availableDeck.value.size)
        assertEquals("Subroutine Origin", viewModel.drawnCards.value[0].position.titleEn)

        // Auto-deal remaining 2 cards to fill spread
        viewModel.autoDealRemaining()
        assertEquals(3, viewModel.drawnCards.value.size)
        assertEquals(ReadingPhase.REVEALING, viewModel.readingPhase.value)

        // Check card reveal
        assertFalse(viewModel.drawnCards.value.all { it.isRevealed })
        viewModel.revealAllCards()
        assertTrue(viewModel.drawnCards.value.all { it.isRevealed })

        // Toggle orientation
        val firstCardOrientation = viewModel.drawnCards.value[0].orientation
        viewModel.toggleOrientation(0)
        assertNotEquals(firstCardOrientation, viewModel.drawnCards.value[0].orientation)
    }
}
