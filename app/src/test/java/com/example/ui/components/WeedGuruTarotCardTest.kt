package com.example.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.data.CyberTarotDeck
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WeedGuruTarotCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun cardFrontDisplaysRomanNumeralTitleAndBottomWeedGuruWatermark() {
        val sproutCard = CyberTarotDeck.getCard(0)

        composeTestRule.setContent {
            MyApplicationTheme {
                WeedGuruTarotCard(
                    card = sproutCard,
                    isUpright = true,
                    isFlipped = false,
                    showOrientationBadge = true
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("tarot_card_0").assertIsDisplayed()
        composeTestRule.onNodeWithText("THE SPROUT").assertIsDisplayed()
        composeTestRule.onNodeWithText("0").assertIsDisplayed()
        composeTestRule.onNodeWithText("▲ UPRIGHT").assertIsDisplayed()

        // Exactly one watermark stamp (bottom only, no middle diagonal duplicate)
        val watermarkTextNodes = composeTestRule.onAllNodesWithText("WEED GURU PROPERTY", useUnmergedTree = true).fetchSemanticsNodes()
        assertTrue("Expected single bottom WEED GURU PROPERTY watermark on card front", watermarkTextNodes.size == 1)

        val cardBounds = composeTestRule.onNodeWithTag("tarot_card_0").fetchSemanticsNode().boundsInRoot
        val watermarkBounds = composeTestRule.onNodeWithTag("weed_guru_property_watermark", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        assertTrue(
            "Expected watermark to sit in bottom half of card, not center",
            watermarkBounds.center.y > cardBounds.center.y
        )
    }

    @Test
    fun reversedCardShowsReversedBadge() {
        val devilCard = CyberTarotDeck.getCard(15)

        composeTestRule.setContent {
            MyApplicationTheme {
                WeedGuruTarotCard(
                    card = devilCard,
                    isUpright = false,
                    isFlipped = false,
                    showOrientationBadge = true
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("XV", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("▼ REVERSED", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun flippedCardShowsSeriesBackAndBottomWatermark() {
        val genesisCard = CyberTarotDeck.getCard(26)

        composeTestRule.setContent {
            MyApplicationTheme {
                WeedGuruTarotCard(
                    card = genesisCard,
                    isFlipped = true
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("GENESIS SERIES", useUnmergedTree = true).assertIsDisplayed()
        val cardBounds = composeTestRule.onNodeWithTag("tarot_card_26").fetchSemanticsNode().boundsInRoot
        val watermarkBounds = composeTestRule.onNodeWithTag("weed_guru_property_watermark", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        assertTrue(
            "Expected watermark at bottom of card back (not centered), cardCenter=${cardBounds.center.y}, wmCenter=${watermarkBounds.center.y}",
            watermarkBounds.center.y > cardBounds.center.y + (cardBounds.height * 0.25f)
        )
    }

    @Test
    fun fullscreenViewerDisplaysCardWithoutFramesAndFlipsToCardBackWithBottomWatermark() {
        val sproutCard = CyberTarotDeck.getCard(0)

        composeTestRule.setContent {
            MyApplicationTheme {
                WeedGuruFullscreenCardViewer(
                    card = sproutCard,
                    isUpright = true,
                    systemLanguage = "ru",
                    onDismiss = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("fullscreen_card_viewer").assertIsDisplayed()
        composeTestRule.onNodeWithTag("fullscreen_card_surface").assertIsDisplayed()

        // Verify front watermark is strictly at the very bottom
        val viewerBoundsFront = composeTestRule.onNodeWithTag("fullscreen_card_viewer").fetchSemanticsNode().boundsInRoot
        val frontWatermarkBounds = composeTestRule.onNodeWithTag("weed_guru_property_watermark", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        assertTrue(
            "Expected fullscreen front watermark near bottom edge",
            frontWatermarkBounds.center.y > viewerBoundsFront.center.y + (viewerBoundsFront.height * 0.25f)
        )

        // Flip to card back (рубашка)
        composeTestRule.onNodeWithTag("fullscreen_flip_button").performClick()
        composeTestRule.mainClock.advanceTimeBy(600)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Cyber Botanica Card Back", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("CYBER BOTANICA", useUnmergedTree = true).assertIsDisplayed()

        val backWatermarkBounds = composeTestRule.onNodeWithTag("weed_guru_property_watermark", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        assertTrue(
            "Expected fullscreen card-back watermark near bottom edge",
            backWatermarkBounds.center.y > viewerBoundsFront.center.y + (viewerBoundsFront.height * 0.25f)
        )
    }
}
