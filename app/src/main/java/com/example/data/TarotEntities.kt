package com.example.data

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val username: String = "Netrunner_01",
    val weed: Int = 1000,
    val totalDraws: Int = 0,
    val totalMints: Int = 0,
    val totalTrades: Int = 0,
    // BIP39 Security details
    val isSecured: Boolean = false,
    val securedDob: String = "",
    val securedFirst: String = "",
    val securedLast: String = "",
    val seedPhrase: String = "",
    val pinCode: String = "", // 8-digit PIN code
    val pinSafeMode: String = "11111111", // alternative pin 1 (Safe botanical/chat mode)
    val pinPanicMode: String = "22222222", // alternative pin 2 (Panic reset wipe)
    // Localization
    val systemLanguage: String = "en", // default to English
    // Proxy Configurations (Tor, VPN, V2Ray, etc.)
    val proxyType: String = "NONE", // NONE, TOR, V2RAY, VPN, SOCKS5
    val proxyHost: String = "127.0.0.1",
    val proxyPort: Int = 9050,
    val proxyConfig: String = "", // raw config URI/JSON/JSON format
    val isProxyActive: Boolean = false,
    // Decentralized Cloud Allocation
    val isCloudAllocated: Boolean = false,
    val allocatedDiskSpaceGb: Float = 0f, // Allocations up to 50% free space (min 2GB)
    val nodeRating: Float = 98.4f, // Node performance rating %
    // User info setup
    val alias: String = "Matrix_Walker",
    val avatarIndex: Int = 0, // 0 to 5 for preset cyberpunk avatars
    val customAvatarUri: String? = null,
    val bio: String = "Core system Netrunner navigating the decentralized neural web.",
    // Tokenized assets support (ETH, TON, SOL, TRX, USDC)
    val balanceEth: Double = 0.5,
    val balanceTon: Double = 50.0,
    val balanceSol: Double = 2.0,
    val balanceTrx: Double = 250.0,
    val balanceUsdc: Double = 100.0,
    // Weed mining & activity fields
    val lastWeedClaimTime: Long = 0L,
    val weedStreak: Int = 0,
    val isAdFreeSubscribed: Boolean = false,
    val adFreeExpiryTime: Long = 0L,
    val nodeStake: Int = 0,
    val nodeUptimeMinutes: Int = 0,
    val nodeBandwidthMbps: Double = 5.0,
    val nodeGlobalRank: Int = 999,
    val nodeLastActiveTime: Long = 0L,
    val nodeIsOnline: Boolean = false
)

@Immutable
@Entity(tableName = "minted_cards")
data class MintedCard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cardIndex: Int,
    val rarity: String, // "Common", "Rare", "Epic", "Legend"
    val mintNumber: Int, // The unique serial number, e.g., #0124
    val timestamp: Long = System.currentTimeMillis(),
    val isListed: Boolean = false,
    val listPrice: Int = 0
)

@Immutable
@Entity(tableName = "market_listings")
data class MarketListing(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cardIndex: Int,
    val rarity: String,
    val mintNumber: Int,
    val price: Int,
    val sellerName: String,
    val isBot: Boolean = true
)

@Immutable
@Entity(tableName = "transaction_logs")
data class TransactionLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cardIndex: Int,
    val rarity: String,
    val price: Int,
    val type: String, // "BUY", "SELL", "MINT"
    val targetParty: String, // e.g. "Automated Liquidity Core"
    val timestamp: Long = System.currentTimeMillis()
)

@Immutable
@Entity(tableName = "peer_contacts")
data class PeerContact(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val onionAddress: String,
    val publicKey: String,
    val status: String = "ACTIVE",
    val timestamp: Long = System.currentTimeMillis()
)

@Immutable
@Entity(tableName = "encrypted_messages")
data class EncryptedMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val contactId: Int,
    val senderName: String,
    val plaintext: String,
    val ciphertext: String,
    val cryptographicKey: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isIncoming: Boolean = false
)

@Immutable
@Entity(tableName = "p2p_transactions")
data class P2pTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val blockNumber: Int,
    val previousHash: String,
    val blockHash: String,
    val nonce: Int,
    val senderAddress: String,
    val recipientAddress: String,
    val amountWeed: Int,
    val cardIndex: Int = -1,
    val mintNumber: Int = -1,
    val signature: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Immutable
@Entity(tableName = "ad_banners")
data class AdBanner(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val theme: String, // "Cybernetics", "Tarot", "Biotech", "Darknet", "AI Overlords", "Quantum Finance"
    val authorName: String,
    val authorContactOnion: String,
    val colorHex: String = "#00FFFF", // Accent glow color
    val imageUrl: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val costPaid: Int = 100,
    val clicks: Int = 0,
    val messagePriceWeed: Int = 5
)

@Immutable
@Entity(tableName = "divination_spreads")
data class DivinationReading(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val question: String,
    val cardIndicesString: String, // Comma-separated indices
    val isUprightString: String,   // Comma-separated booleans
    val report: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "localization_items")
data class LocalizationItem(
    @PrimaryKey val key: String,
    val en: String,
    val ru: String,
    val es: String,
    val he: String,
    val de: String,
    val fr: String,
    val pt: String,
    val it: String,
    val zh: String,
    val ja: String
)

@Entity(tableName = "card_localization_items")
data class CardLocalizationItem(
    @PrimaryKey val cardIndex: Int,
    val nameEn: String,
    val nameRu: String,
    val nameEs: String,
    val nameHe: String,
    val nameDe: String,
    val nameFr: String,
    val namePt: String,
    val nameIt: String,
    val nameZh: String,
    val nameJa: String,

    val traditionalNameEn: String,
    val traditionalNameRu: String,
    val traditionalNameEs: String,
    val traditionalNameHe: String,
    val traditionalNameDe: String,
    val traditionalNameFr: String,
    val traditionalNamePt: String,
    val traditionalNameIt: String,
    val traditionalNameZh: String,
    val traditionalNameJa: String,

    val descriptionEn: String,
    val descriptionRu: String,
    val descriptionEs: String,
    val descriptionHe: String,
    val descriptionDe: String,
    val descriptionFr: String,
    val descriptionPt: String,
    val descriptionIt: String,
    val descriptionZh: String,
    val descriptionJa: String,

    val circuitryElementEn: String,
    val circuitryElementRu: String,
    val circuitryElementEs: String,
    val circuitryElementHe: String,
    val circuitryElementDe: String,
    val circuitryElementFr: String,
    val circuitryElementPt: String,
    val circuitryElementIt: String,
    val circuitryElementZh: String,
    val circuitryElementJa: String,

    val cyberMeaningEn: String,
    val cyberMeaningRu: String,
    val cyberMeaningEs: String,
    val cyberMeaningHe: String,
    val cyberMeaningDe: String,
    val cyberMeaningFr: String,
    val cyberMeaningPt: String,
    val cyberMeaningIt: String,
    val cyberMeaningZh: String,
    val cyberMeaningJa: String,

    val uprightAdviceEn: String,
    val uprightAdviceRu: String,
    val uprightAdviceEs: String,
    val uprightAdviceHe: String,
    val uprightAdviceDe: String,
    val uprightAdviceFr: String,
    val uprightAdvicePt: String,
    val uprightAdviceIt: String,
    val uprightAdviceZh: String,
    val uprightAdviceJa: String,

    val reversedAdviceEn: String,
    val reversedAdviceRu: String,
    val reversedAdviceEs: String,
    val reversedAdviceHe: String,
    val reversedAdviceDe: String,
    val reversedAdviceFr: String,
    val reversedAdvicePt: String,
    val reversedAdviceIt: String,
    val reversedAdviceZh: String,
    val reversedAdviceJa: String
)



