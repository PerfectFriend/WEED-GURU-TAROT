package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Reactive store controlling fullscreen WEED GURU PROPERTY watermark visibility
 * and the $5.99-equivalent GENESIS WATERMARK ELIMINATOR unlock state.
 *
 * Watermark removal is locked by default until the user acquires Card #31
 * (Genesis Watermark Eliminator) and can be toggled on/off thereafter.
 */
object WatermarkStateStore {
    private const val PREFS_NAME = "weed_guru_watermark_prefs"
    private const val KEY_WATERMARK_HIDDEN = "fullscreen_watermark_hidden"

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private val _isWatermarkHidden = MutableStateFlow(false)
    val isWatermarkHidden: StateFlow<Boolean> = _isWatermarkHidden.asStateFlow()

    fun init(context: Context) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _isWatermarkHidden.value = prefs.getBoolean(KEY_WATERMARK_HIDDEN, false)
    }

    fun syncOwnedCards(mintedCards: List<MintedCard>) {
        val ownsEliminator = mintedCards.any { it.cardIndex == CyberTarotDeck.GENESIS_ELIMINATOR_INDEX }
        _isUnlocked.value = ownsEliminator
        if (!ownsEliminator && _isWatermarkHidden.value) {
            _isWatermarkHidden.value = false
        }
    }

    fun unlockAndHide(context: Context? = null) {
        _isUnlocked.value = true
        _isWatermarkHidden.value = true
        context?.applicationContext
            ?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?.edit()
            ?.putBoolean(KEY_WATERMARK_HIDDEN, true)
            ?.apply()
    }

    fun setWatermarkHidden(hidden: Boolean, context: Context? = null) {
        if (!_isUnlocked.value && hidden) return
        _isWatermarkHidden.value = hidden
        context?.applicationContext
            ?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?.edit()
            ?.putBoolean(KEY_WATERMARK_HIDDEN, hidden)
            ?.apply()
    }

    @Volatile
    private var purchaseHandler: (suspend (Boolean) -> Boolean)? = null

    fun registerPurchaseHandler(handler: suspend (Boolean) -> Boolean) {
        purchaseHandler = handler
    }

    suspend fun requestPurchase(payWithUsdc: Boolean = true): Boolean {
        val handler = purchaseHandler ?: return false
        return handler(payWithUsdc)
    }

    fun resetForTest() {
        _isUnlocked.value = false
        _isWatermarkHidden.value = false
        purchaseHandler = null
    }
}
