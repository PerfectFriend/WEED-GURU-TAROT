package com.example.ui

import android.content.Context
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.example.R
import com.example.data.CyberTarotDeck
import com.example.ui.components.CyberActionButton
import com.example.ui.components.CyberChip
import com.example.ui.components.CyberLunarCycleWidget
import com.example.ui.components.CyberPanel
import com.example.ui.components.CyberRadioMiniBar
import com.example.ui.components.CyberSectionHeader
import com.example.ui.components.CyberTopBar
import com.example.ui.components.RarityTag
import com.example.ui.components.SkinBackdrop
import com.example.ui.components.WeedGuruFullscreenCardViewer
import com.example.ui.components.WeedGuruTarotCard
import com.example.ui.components.localizedString
import com.example.ui.screens.CustomBigKeypad
import com.example.ui.screens.InteractiveCircuitRadialChart
import com.example.ui.screens.QuestItemRow
import com.example.ui.screens.RadialChartSlice
import com.example.ui.screens.SynthesisRecipeCard
import com.example.ui.theme.Cyber
import com.example.ui.theme.CyberSkin
import com.example.ui.theme.CyberTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.Space
import com.example.ui.theme.rememberMotionEnabled
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CyberThemeScreensTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun oracleTempleSkinRendersTopBarLocalizedStringsAndTouchTargets() {
        var needsDenseScrim = true
        composeTestRule.setContent {
            CyberTheme(skin = CyberSkin.ORACLE_TEMPLE) {
                needsDenseScrim = Cyber.colors.needsDenseScrim
                Column {
                    CyberTopBar(
                        brandTitle = "CYBERTAROT",
                        subtitle = localizedString(R.string.tab_sub_divination, "en"),
                        weedBalance = 420,
                        isRadioPlaying = false,
                        isRadioBuffering = false,
                        visualizerBars = listOf(0.4f, 0.8f, 0.5f),
                        onToggleRadio = {},
                        onOpenBlessings = {},
                        systemLanguage = "en"
                    )
                    CyberPanel {
                        CyberSectionHeader(
                            title = localizedString(R.string.divination_title, "en"),
                            subtitle = localizedString(R.string.divination_subtitle, "en")
                        )
                        CyberActionButton(
                            text = localizedString(R.string.divination_initialize_scan, "en"),
                            icon = Icons.Default.AutoAwesome,
                            onClick = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("preview_scan_btn")
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()

        assertFalse("ORACLE_TEMPLE should not force dense Jamaica scrim", needsDenseScrim)
        composeTestRule.onNodeWithText("Botanical Oracle").assertIsDisplayed()
        composeTestRule.onNodeWithText("Consult the Oracle").assertIsDisplayed()
        composeTestRule.onNodeWithText("420 WEED").assertIsDisplayed()
        composeTestRule.onNodeWithTag("cyber_radio_toggle_button").assertHeightIsAtLeast(48.dp)
        composeTestRule.onNodeWithTag("wallet_credits_button").assertHeightIsAtLeast(48.dp)
        composeTestRule.onNodeWithTag("preview_scan_btn").assertHeightIsAtLeast(48.dp)
    }

    @Test
    fun jamaicaSkinActivatesDenseScrim() {
        var needsDenseScrim = false
        composeTestRule.setContent {
            CyberTheme(skin = CyberSkin.JAMAICA) {
                needsDenseScrim = Cyber.colors.needsDenseScrim
                Box {
                    SkinBackdrop(skin = CyberSkin.JAMAICA)
                    CyberPanel {
                        CyberChip(
                            label = localizedString(R.string.spread_flow_title, "en"),
                            selected = true,
                            onClick = {},
                            modifier = Modifier.testTag("jamaica_chip")
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()

        assertTrue("JAMAICA skin must activate needsDenseScrim", needsDenseScrim)
        composeTestRule.onNodeWithTag("jamaica_chip").assertIsDisplayed().assertHeightIsAtLeast(48.dp)
    }

    @Test
    fun gaudiVanGoghLargeFontScaleRussianStringsAndSubcardsRenderCleanly() {
        composeTestRule.setContent {
            val currentDensity = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(
                    density = currentDensity.density,
                    fontScale = 1.5f
                )
            ) {
                CyberTheme(skin = CyberSkin.GAUDI_VANGOGH) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(Space.md),
                        verticalArrangement = Arrangement.spacedBy(Space.sm)
                    ) {
                        CyberTopBar(
                            brandTitle = "CYBERTAROT",
                            subtitle = localizedString(R.string.tab_sub_divination, "ru"),
                            weedBalance = 999,
                            isRadioPlaying = false,
                            isRadioBuffering = false,
                            visualizerBars = listOf(0.5f, 0.7f, 0.6f),
                            onToggleRadio = {},
                            onOpenBlessings = {},
                            systemLanguage = "ru"
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                            RarityTag("Genesis")
                            CyberChip(
                                label = localizedString(R.string.spread_celtic_title, "ru"),
                                selected = false,
                                onClick = {}
                            )
                        }
                        QuestItemRow(
                            title = "Провести сканирование матрицы",
                            desc = "Сделать любой расклад карт у Оракула",
                            reward = 100,
                            isCompleted = true,
                            isClaimed = false,
                            lang = "ru",
                            onClaim = {}
                        )
                        SynthesisRecipeCard(
                            title = "COMMON -> RARE",
                            subtitle = "4 Common Cards -> 1 Rare NFT Card",
                            ownedCount = 4,
                            requiredCount = 4,
                            targetColor = NeonCyan,
                            buttonText = "FUSE 4 COMMONS",
                            isFusing = false,
                            onFuse = {}
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("999 WEED").assertIsDisplayed()
        composeTestRule.onNodeWithText("GENESIS", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("ЗАБРАТЬ", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("FUSE 4 COMMONS", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun reducedMotionDisablesAnimatedLoopAndRendersCardFrontBackAndViewerStatically() {
        val context: Context = ApplicationProvider.getApplicationContext()
        Settings.Global.putFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            0f
        )

        var motionEnabled = true
        val genesisCard = CyberTarotDeck.getCard(26)

        try {
            composeTestRule.setContent {
                CyberTheme(skin = CyberSkin.ORACLE_TEMPLE) {
                    motionEnabled = rememberMotionEnabled()
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(
                            text = if (motionEnabled) "MOTION_ON" else "MOTION_OFF",
                            modifier = Modifier.testTag("motion_status_label")
                        )
                        WeedGuruTarotCard(
                            card = genesisCard,
                            isUpright = true,
                            isFlipped = false,
                            systemLanguage = "ru",
                            showOrientationBadge = true
                        )
                    }
                }
            }
            composeTestRule.waitForIdle()

            assertFalse("rememberMotionEnabled must return false when ANIMATOR_DURATION_SCALE == 0f", motionEnabled)
            composeTestRule.onNodeWithText("MOTION_OFF").assertIsDisplayed()
            composeTestRule.onNodeWithTag("tarot_card_26").performScrollTo().assertIsDisplayed()
        } finally {
            Settings.Global.putFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            )
        }
    }

    @Test
    fun reducedMotionFullscreenViewerRendersCleanly() {
        val context: Context = ApplicationProvider.getApplicationContext()
        Settings.Global.putFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            0f
        )

        val sproutCard = CyberTarotDeck.getCard(0)
        try {
            composeTestRule.setContent {
                CyberTheme(skin = CyberSkin.ORACLE_TEMPLE) {
                    WeedGuruFullscreenCardViewer(
                        card = sproutCard,
                        isUpright = true,
                        systemLanguage = "en",
                        onDismiss = {}
                    )
                }
            }
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithTag("fullscreen_card_viewer").assertIsDisplayed()
            composeTestRule.onNodeWithTag("fullscreen_flip_button").assertHeightIsAtLeast(48.dp)
        } finally {
            Settings.Global.putFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            )
        }
    }

    @Test
    fun lunarWidgetRadioMiniBarKeypadAndRadialChartRenderWithAccessibleTouchTargets() {
        composeTestRule.setContent {
            CyberTheme(skin = CyberSkin.ORACLE_TEMPLE) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(Space.md),
                    verticalArrangement = Arrangement.spacedBy(Space.md)
                ) {
                    CyberLunarCycleWidget(
                        systemLanguage = "ru",
                        currentSkin = CyberSkin.ORACLE_TEMPLE,
                        onMintClick = {}
                    )
                    InteractiveCircuitRadialChart(
                        slices = listOf(
                            RadialChartSlice("ЛОГИКА", 4f, NeonCyan),
                            RadialChartSlice("ПАМЯТЬ", 2f, Cyber.colors.emeraldGlow)
                        ),
                        title = "Стихии",
                        selectedIndex = 0,
                        onSliceSelected = {},
                        systemLanguage = "ru"
                    )
                    CustomBigKeypad(
                        skin = CyberSkin.ORACLE_TEMPLE,
                        onDigitClick = {},
                        onDeleteClick = {},
                        onClearClick = {}
                    )
                    CyberRadioMiniBar(
                        systemLanguage = "ru",
                        onOpenConsole = {}
                    )
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("cyber_lunar_cycle_widget").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("lunar_mint_action_button").performScrollTo().assertHeightIsAtLeast(48.dp)
        composeTestRule.onNodeWithText("66%", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("ЛОГИКА", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("keypad_key_1").performScrollTo().assertHeightIsAtLeast(48.dp)
        composeTestRule.onNodeWithTag("cyber_radio_mini_bar").performScrollTo().assertHeightIsAtLeast(48.dp)
    }
}
