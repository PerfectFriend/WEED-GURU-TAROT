package com.example.data

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class WatermarkPurchaseTest {

    @Before
    fun setUp() {
        WatermarkStateStore.resetForTest()
    }

    @Test
    fun purchaseGenesisWatermarkEliminator_rejectsInsufficientUsdc_thenSucceedsWithUsdc() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val repository = TarotRepository.getInstance(app)
        repository.userProfile.first { it != null }
        repository.triggerPanicResetComplete()
        WatermarkStateStore.resetForTest()

        val initial = repository.getUserProfileSync()!!
        repository.updateProfile(initial.copy(balanceUsdc = 2.0))

        val rejected = repository.purchaseGenesisWatermarkEliminator(payWithUsdc = true)
        assertFalse("Must reject purchase when balanceUsdc < 5.99", rejected)
        assertFalse(WatermarkStateStore.isUnlocked.value)

        val afterReject = repository.getUserProfileSync()!!
        repository.updateProfile(afterReject.copy(balanceUsdc = 10.0))

        val purchased = repository.purchaseGenesisWatermarkEliminator(payWithUsdc = true)
        assertTrue("Purchase must succeed when balanceUsdc >= 5.99", purchased)

        val updated = repository.getUserProfileSync()!!
        assertEquals(4.01, updated.balanceUsdc, 0.001)
        assertTrue(WatermarkStateStore.isUnlocked.value)
        assertTrue(WatermarkStateStore.isWatermarkHidden.value)

        val minted = repository.mintedCards.first()
        val eliminator = minted.firstOrNull { it.cardIndex == CyberTarotDeck.GENESIS_ELIMINATOR_INDEX }
        assertTrue("Genesis Watermark Eliminator card #31 must be minted", eliminator != null)
        assertEquals("Genesis", eliminator?.rarity)
    }

    @Test
    fun purchaseGenesisWatermarkEliminator_succeedsWith60WeedEquivalent() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val repository = TarotRepository.getInstance(app)
        repository.userProfile.first { it != null }
        repository.triggerPanicResetComplete()
        WatermarkStateStore.resetForTest()

        val initial = repository.getUserProfileSync()!!
        repository.updateProfile(initial.copy(weed = 100, balanceUsdc = 0.0))

        val purchased = repository.purchaseGenesisWatermarkEliminator(payWithUsdc = false)
        assertTrue("Purchase with 60 WEED equivalent must succeed", purchased)

        val updated = repository.getUserProfileSync()!!
        assertEquals(40, updated.weed)
        assertTrue(WatermarkStateStore.isUnlocked.value)
        assertTrue(WatermarkStateStore.isWatermarkHidden.value)
    }
}
