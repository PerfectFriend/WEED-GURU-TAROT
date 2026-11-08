package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TarotDao {

    // Profile queries
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileSync(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Update
    suspend fun updateProfile(profile: UserProfile)

    // Minted cards queries
    @Query("SELECT * FROM minted_cards ORDER BY timestamp DESC")
    fun getMintedCards(): Flow<List<MintedCard>>

    @Query("SELECT * FROM minted_cards WHERE id = :id LIMIT 1")
    suspend fun getMintedCardById(id: Int): MintedCard?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMintedCard(card: MintedCard): Long

    @Update
    suspend fun updateMintedCard(card: MintedCard)

    @Delete
    suspend fun deleteMintedCard(card: MintedCard)

    // Market listings queries
    @Query("SELECT * FROM market_listings ORDER BY price ASC")
    fun getMarketListings(): Flow<List<MarketListing>>

    @Query("SELECT * FROM market_listings WHERE id = :id LIMIT 1")
    suspend fun getMarketListingById(id: Int): MarketListing?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketListing(listing: MarketListing): Long

    @Delete
    suspend fun deleteMarketListing(listing: MarketListing)

    @Query("DELETE FROM market_listings WHERE cardIndex = :cardIndex AND mintNumber = :mintNumber")
    suspend fun deleteMarketListingByCard(cardIndex: Int, mintNumber: Int)

    // Transaction Log queries
    @Query("SELECT * FROM transaction_logs ORDER BY timestamp DESC LIMIT 50")
    fun getTransactionLogs(): Flow<List<TransactionLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionLog(log: TransactionLog)

    // Peer Contacts queries
    @Query("SELECT * FROM peer_contacts ORDER BY name ASC")
    fun getPeerContacts(): Flow<List<PeerContact>>

    @Query("SELECT * FROM peer_contacts WHERE id = :id LIMIT 1")
    suspend fun getPeerContactById(id: Int): PeerContact?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeerContact(contact: PeerContact): Long

    @Delete
    suspend fun deletePeerContact(contact: PeerContact)

    // Encrypted Messages queries
    @Query("SELECT * FROM encrypted_messages WHERE contactId = :contactId ORDER BY timestamp ASC")
    fun getEncryptedMessagesForContact(contactId: Int): Flow<List<EncryptedMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEncryptedMessage(message: EncryptedMessage): Long

    // P2P Transactions queries
    @Query("SELECT * FROM p2p_transactions ORDER BY blockNumber DESC, timestamp DESC")
    fun getP2pTransactions(): Flow<List<P2pTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertP2pTransaction(transaction: P2pTransaction): Long

    // Ad Banners queries
    @Query("SELECT * FROM ad_banners ORDER BY timestamp DESC")
    fun getAdBanners(): Flow<List<AdBanner>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdBanner(banner: AdBanner): Long

    @Delete
    suspend fun deleteAdBanner(banner: AdBanner)

    @Query("UPDATE ad_banners SET clicks = clicks + 1 WHERE id = :id")
    suspend fun incrementAdClicks(id: Int)

    // Divination history queries
    @Query("SELECT * FROM divination_spreads ORDER BY timestamp DESC")
    fun getDivinationReadings(): Flow<List<DivinationReading>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDivinationReading(reading: DivinationReading): Long

    @Query("DELETE FROM divination_spreads WHERE id = :id")
    suspend fun deleteDivinationReading(id: Int)

    // Localization items
    @Query("SELECT * FROM localization_items")
    suspend fun getLocalizationItems(): List<LocalizationItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocalizationItems(items: List<LocalizationItem>)

    // Card localization items
    @Query("SELECT * FROM card_localization_items")
    suspend fun getCardLocalizationItems(): List<CardLocalizationItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCardLocalizationItems(items: List<CardLocalizationItem>)

    // Saved Decks (TCG)
    @Query("SELECT * FROM saved_decks ORDER BY createdAt ASC")
    fun getSavedDecks(): Flow<List<SavedDeck>>

    @Query("SELECT * FROM saved_decks WHERE isActive = 1 LIMIT 1")
    fun getActiveDeck(): Flow<SavedDeck?>

    @Query("SELECT * FROM saved_decks WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveDeckSync(): SavedDeck?

    @Query("SELECT COUNT(*) FROM saved_decks")
    suspend fun getSavedDeckCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDeck(deck: SavedDeck): Long

    @Delete
    suspend fun deleteDeck(deck: SavedDeck)

    @Query("UPDATE saved_decks SET isActive = 0")
    suspend fun clearActiveDecks()

    @Query("UPDATE saved_decks SET isActive = 1 WHERE id = :deckId")
    suspend fun setActiveDeckById(deckId: Int)

    // Card Upgrades (TCG Forge)
    @Query("SELECT * FROM card_upgrades")
    fun getCardUpgrades(): Flow<List<CardUpgrade>>

    @Query("SELECT * FROM card_upgrades WHERE cardIndex = :cardIndex LIMIT 1")
    suspend fun getCardUpgrade(cardIndex: Int): CardUpgrade?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCardUpgrade(upgrade: CardUpgrade)
}
