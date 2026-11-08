package com.example.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProfile::class,
        MintedCard::class,
        MarketListing::class,
        TransactionLog::class,
        PeerContact::class,
        EncryptedMessage::class,
        P2pTransaction::class,
        AdBanner::class,
        DivinationReading::class,
        LocalizationItem::class,
        CardLocalizationItem::class,
        SavedDeck::class,
        CardUpgrade::class
    ],
    version = 14,
    exportSchema = false
)
abstract class TarotDatabase : RoomDatabase() {
    abstract fun tarotDao(): TarotDao
}
