package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MarketListing
import com.example.data.MintedCard
import com.example.data.P2pTransaction
import com.example.data.TarotRepository
import com.example.data.TransactionLog
import com.example.data.UserProfile
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MarketViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TarotRepository.getInstance(application)

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val marketListings: StateFlow<List<MarketListing>> = repository.marketListings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mintedCards: StateFlow<List<MintedCard>> = repository.mintedCards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactionLogs: StateFlow<List<TransactionLog>> = repository.transactionLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val p2pTransactions: StateFlow<List<P2pTransaction>> = repository.p2pTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun buyNFT(listingId: Int, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val success = repository.buyNFTFromExchange(listingId)
            onResult(success)
        }
    }

    fun cancelListing(cardIndex: Int, mintNumber: Int, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val success = repository.cancelListing(cardIndex, mintNumber)
            onResult(success)
        }
    }

    fun sellToBroker(cardId: Int, onResult: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val price = repository.sellInstantlyToBroker(cardId)
            onResult(price)
        }
    }
}
