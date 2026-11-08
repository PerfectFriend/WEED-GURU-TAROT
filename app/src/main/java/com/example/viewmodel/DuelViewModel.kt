package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.TarotRepository
import com.example.duel.BattleCard
import com.example.duel.DuelCombatEngine
import com.example.duel.DuelState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DuelViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TarotRepository.getInstance(application)
    val engine = DuelCombatEngine()

    val duelState: StateFlow<DuelState> = engine.state

    val activeDeck = repository.activeDeck
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun startMatch(difficulty: String = "Normal") {
        viewModelScope.launch {
            val customCards = repository.getActiveBattleDeckSync()
            val deckName = repository.getActiveDeckSync()?.name ?: "Genesis Cyber Deck"
            engine.startMatch(difficulty, customCards, deckName)
        }
    }

    fun playCard(card: BattleCard, slotIndex: Int): Boolean {
        return engine.playCard(card, slotIndex)
    }

    fun attackWithSlot(attackerSlotIndex: Int) {
        engine.attackWithSlot(attackerSlotIndex)
    }

    fun endTurn() {
        engine.endPlayerTurn(viewModelScope) { credits, _ ->
            rewardPlayerVictory(credits)
        }
    }

    fun rewardPlayerVictory(credits: Int = 150) {
        viewModelScope.launch {
            val profile = repository.getUserProfileSync()
            if (profile != null) {
                repository.updateProfile(profile.copy(weed = profile.weed + credits))
            }
        }
    }
}
