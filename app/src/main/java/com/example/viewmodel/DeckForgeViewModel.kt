package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CardUpgrade
import com.example.data.SavedDeck
import com.example.data.TarotRepository
import com.example.data.UserProfile
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DeckForgeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TarotRepository.getInstance(application)

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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
}
