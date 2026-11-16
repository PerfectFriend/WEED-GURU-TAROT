package com.example.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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
    version = 16,
    exportSchema = true
)
abstract class TarotDatabase : RoomDatabase() {
    abstract fun tarotDao(): TarotDao

    companion object {
        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE user_profile ADD COLUMN cloudRewardClaimed INTEGER NOT NULL DEFAULT 0"
                )
            }
        }
    }
}
