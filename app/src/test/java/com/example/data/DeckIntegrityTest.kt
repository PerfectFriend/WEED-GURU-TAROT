package com.example.data

import com.example.R
import com.example.ui.components.Tarot420Discipline
import com.example.ui.components.getArcanaRomanNumeral
import com.example.ui.components.getCard420Discipline
import com.example.ui.components.getTarotSeriesTheme
import com.example.ui.screens.UNDER_CONSTRUCTION_CARD_INDICES
import com.example.ui.screens.getCardBackImage
import com.example.ui.screens.getCardBackImageForCard
import com.example.ui.screens.getCardImage
import com.example.ui.screens.getSuitCardBackImage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DeckIntegrityTest {

    @Test
    fun allCardsHaveSequentialIndicesAndValidArtOrUnderConstruction() {
        val cards = CyberTarotDeck.cards
        assertEquals("Expected 88 cards in deck (22 Major + 10 Collector + 56 Minor)", 88, cards.size)

        val seenCompletedDrawables = mutableSetOf<Int>()
        cards.forEachIndexed { expectedIdx, card ->
            assertEquals("Card index mismatch", expectedIdx, card.index)
            assertTrue("Card name blank at $expectedIdx", card.name.isNotBlank())
            assertTrue("Traditional name blank at $expectedIdx", card.traditionalName.isNotBlank())
            assertTrue("Description blank at $expectedIdx", card.description.isNotBlank())
            assertTrue("Upright keywords empty at $expectedIdx", card.uprightKeywords.isNotEmpty())
            assertTrue("Reversed keywords empty at $expectedIdx", card.reversedKeywords.isNotEmpty())
            assertTrue("Upright advice blank at $expectedIdx", card.uprightAdvice.isNotBlank())
            assertTrue("Reversed advice blank at $expectedIdx", card.reversedAdvice.isNotBlank())

            val imgRes = getCardImage(card.index)
            assertNotNull("Missing artwork drawable for card #${card.index} (${card.name})", imgRes)
            assertTrue(
                "Duplicate completed card artwork drawable at #${card.index} (${card.name})",
                seenCompletedDrawables.add(imgRes!!)
            )
        }
        assertEquals("Expected 0 cards left under construction (100% coverage)", 0, UNDER_CONSTRUCTION_CARD_INDICES.size)
        assertEquals("Expected 88 distinct completed artworks (100% deck coverage)", 88, seenCompletedDrawables.size)
    }

    @Test
    fun allSeriesHaveCorrectRangesAndDistinctCardBacks() {
        val cards = CyberTarotDeck.cards

        (0..21).forEach { idx ->
            assertEquals("Cyber Botanica", cards[idx].series)
        }
        (22..26).forEach { idx ->
            assertEquals("Genesis Series", cards[idx].series)
        }
        (27..28).forEach { idx ->
            assertEquals("Amber Series", cards[idx].series)
        }
        (29..30).forEach { idx ->
            assertEquals("Seas & Oceans", cards[idx].series)
        }
        assertEquals("Genesis Series", cards[31].series)
        assertEquals("GEN·Ω", getArcanaRomanNumeral(31))

        (32..87).forEach { idx ->
            assertEquals("Cyber Botanica", cards[idx].series)
        }
        (32..45).forEach { idx -> assertEquals(Tarot420Discipline.GENETICS, getCard420Discipline(idx)) }
        (46..59).forEach { idx -> assertEquals(Tarot420Discipline.CANOPY, getCard420Discipline(idx)) }
        (60..73).forEach { idx -> assertEquals(Tarot420Discipline.RESIN, getCard420Discipline(idx)) }
        (74..87).forEach { idx -> assertEquals(Tarot420Discipline.ALCHEMY, getCard420Discipline(idx)) }

        val expectedSeries = listOf("Cyber Botanica", "Genesis Series", "Amber Series", "Seas & Oceans")
        val backDrawables = expectedSeries.map { series -> getCardBackImage(series) }.toSet()
        assertEquals("Each series must map to a unique card back artwork", expectedSeries.size, backDrawables.size)

        val suitBacks = Tarot420Discipline.entries.map { getSuitCardBackImage(it) }.toSet()
        assertEquals("All 4 420 suits must map to unique reversible card backs", 4, suitBacks.size)

        val genesisEliminatorBack = getCardBackImageForCard(31, "Genesis Series")
        assertEquals(R.drawable.back_genesis_elements_1795033577974, genesisEliminatorBack)
        assertNotEquals(getCardBackImage("Genesis Series"), genesisEliminatorBack)

        expectedSeries.forEach { series ->
            val theme = getTarotSeriesTheme(series)
            assertEquals(series, theme.seriesName)
            assertTrue(theme.badgeEn.isNotBlank())
            assertTrue(theme.badgeRu.isNotBlank())
        }
    }

    @Test
    fun majorArcanaFollowCanonicalRiderWaiteMappingAnd420Rules() {
        val cards = CyberTarotDeck.cards
        assertEquals("The Fool", cards[0].traditionalName)
        assertEquals("The Magician", cards[1].traditionalName)
        assertEquals("The High Priestess", cards[2].traditionalName)
        assertEquals("Strength", cards[8].traditionalName)
        assertEquals("Death", cards[13].traditionalName)
        assertEquals("The Devil", cards[15].traditionalName)
        assertEquals("The World", cards[21].traditionalName)

        val seenNumerals = mutableSetOf<String>()
        val seenDisciplines = mutableSetOf<Tarot420Discipline>()
        cards.forEach { card ->
            val numeral = getArcanaRomanNumeral(card.index)
            assertTrue("Roman numeral blank at #${card.index}", numeral.isNotBlank())
            assertTrue("Duplicate numeral $numeral at #${card.index}", seenNumerals.add(numeral))

            val discipline = getCard420Discipline(card.index)
            seenDisciplines.add(discipline)
        }
        assertEquals("Deck should span all 4 420 Tarot disciplines", 4, seenDisciplines.size)
    }

    @Test
    fun localizationCoversAllCardsInEnglishAndRussian() {
        CyberTarotDeck.cards.forEach { card ->
            listOf("en", "ru").forEach { lang ->
                assertTrue(
                    "Missing localized name ($lang) for #${card.index}",
                    CardLocalizationHelper.getLocalizedName(card.index, lang).isNotBlank()
                )
                assertTrue(
                    "Missing localized traditional ($lang) for #${card.index}",
                    CardLocalizationHelper.getLocalizedTraditionalName(card.index, lang).isNotBlank()
                )
                assertTrue(
                    "Missing localized desc ($lang) for #${card.index}",
                    CardLocalizationHelper.getLocalizedDescription(card.index, lang).isNotBlank()
                )
                assertTrue(
                    "Missing localized upright advice ($lang) for #${card.index}",
                    CardLocalizationHelper.getLocalizedUprightAdvice(card.index, lang).isNotBlank()
                )
                assertTrue(
                    "Missing localized reversed advice ($lang) for #${card.index}",
                    CardLocalizationHelper.getLocalizedReversedAdvice(card.index, lang).isNotBlank()
                )
            }
        }
    }
}
