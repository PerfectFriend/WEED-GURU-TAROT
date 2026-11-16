# WEED GURU / CYBER TAROT
## Technical Architecture & Reality Check Guide for Developers

> **Status Notice (Prototype / Local-First + Sandbox Build):**
> This codebase is an **Android (Kotlin + Jetpack Compose + Room)** client featuring real local cryptography (`PBKDF2WithHmacSHA256`, `AES-256-GCM`, `ECDSA P-256`, BIP-39 2048-word mnemonics), optional HTTPS Gemini integration, optional Firebase Auth + Firestore sync, and an explicit **Local Sandbox** layer for Web3/NFT/staking simulations.

---

## 1. Architectural Blueprint & Technical Stack

The application follows **MVVM (Model-View-ViewModel)** with modular Jetpack Compose screens:

*   **View Layer (`com.example.ui.screens`)**: Material Design 3 + Cyberpunk/Botanical themes (`CyberSkin`), split across domain screen files (`CyberTarotScreens.kt`, `VaultAndMarketScreens.kt`, `DeckAndSynthesisScreens.kt`, `NetOpsAndCommsScreens.kt`, `NetOpsBlockchainPanels.kt`, `CryptoHubScreen.kt`, `PinLockAndBotanicalScreens.kt`, `OnboardingWizard.kt`, `SecurityScreen.kt`, `SwarmStoragePanel.kt`, `AnalyticsAndCardReview.kt`, `QuestsAndCheckoutSheets.kt`).
*   **ViewModel (`com.example.viewmodel.TarotViewModel`)**: Coordinates reactive `StateFlow` state using structured concurrency (`viewModelScope`). Releases TTS and `SwarmStorageManager` workers via `onCleared()`.
*   **Repository & Persistence (`com.example.data`)**: Room database (`TarotDatabase` + `TarotDao` + `TarotRepository`) storing profiles, transactions, minted cards, peer contacts, P2P messages, and encrypted fields wrapped via `EncryptedDatabase` / Android Keystore helpers.
*   **Cloud Sync (Optional)**: `FirestoreSyncManager` with Google Sign-In via Jetpack Credential Manager and owner-scoped `firestore.rules`.
*   **Cryptographic Swarm Core (`com.example.data.SwarmStorageManager`)**: Manages local AES-256-GCM container storage, authenticated LAN peer discovery, and ciphertext-only shard serving (disabled by default until explicitly started by the user).

---

## 2. Cryptographic & Swarm Node Engine (Actual Implementation)

Inside `SwarmStorageManager.kt` and `CryptoHelper.kt`:

1.  **Key Derivation & Cryptography**:
    *   Container and PIN key derivation uses **`PBKDF2WithHmacSHA256` with `CryptoHelper.PBKDF2_ITERATIONS` (210,000 iterations)** and a random 32-byte cryptographic salt (not raw SHA-256).
    *   Header mount signature (`"SWARM_CONTAINER_V1_MOUNTED"`) and 4 MB shards are encrypted using **AES-256-GCM** (`AES/GCM/NoPadding`) with unique 12-byte random IVs per block.
    *   Unmounting (`unmountContainer()`) and `shutdown()` clear the active `SecretKeySpec` reference in JVM memory, cancel active coroutine children, and stop sockets. Note: JVM `SecretKeySpec` copies bytes internally, so clearing references releases them for GC rather than guaranteeing physical RAM scrubbing.
2.  **Active Offset-Based Sharding**:
    *   `writeShardBlock` and `readShardBlock` operate on bounded 4 MB shard offsets (`100L + (index * 4_194_304L)`) protected by `containerLock`.
    *   Local plaintext decryption (`readShardBlock`) is strictly owner-local and is **never** exposed on network sockets.
3.  **Authenticated LAN P2P Transport (Disabled by Default)**:
    *   **UDP Discovery (Port 9944)**: Broadcasts and verifies **HMAC-SHA256-signed** `SWARM_ANNOUNCE` frames using the node cluster key; invalid signatures are discarded and peer IPs are omitted from UI logs.
    *   **TCP Ciphertext Streaming (Port 9945)**: Already implemented in `SwarmStorageManager.kt` with `ServerSocket`, `Semaphore(4)` concurrency cap, 5s socket timeouts, 32-byte **HMAC-SHA256 challenge-response authentication**, and **strict ciphertext-only (`READ_RAW`)** shard responses (`connectToPeerAndFetchEncryptedShard`).

---

## 3. Sandbox Simulations vs. Live Network Integrations

Developers extending this codebase should clearly distinguish between **implemented network paths** and **local Sandbox simulations**:

*   **Live / Configurable Integrations**:
    *   **Gemini AI (`GeminiService.kt`)**: HTTPS requests to `generativelanguage.googleapis.com` (`gemini-2.5-flash`) with retry/backoff, `x-goog-api-key` header, and fail-closed proxy enforcement when `BuildConfig.GEMINI_API_KEY` or user key is configured; otherwise falls back to offline local readings.
    *   **Read-Only EVM JSON-RPC (`NftMintingService.kt`)**: Queries `eth_chainId` / `eth_gasPrice` on user-configured HTTPS RPC endpoints; **does not hold private keys to sign or broadcast real mainnet/testnet write transactions**.
    *   **Bandwidth Benchmark (`TarotViewModel.kt`)**: Measures real HTTP chunked throughput when online or local AES-GCM I/O benchmark offline.
    *   **Firebase Firestore (`FirestoreSyncManager.kt`)**: Optional authenticated cloud sync backed by `firestore.rules`.
*   **Local Sandbox Simulations (No Mainnet Broadcast / No Real Custody)**:
    *   **P2P Ledger & Multi-Chain Balances (`P2pLedgerPanel`)**: Local Room-backed ledger with ECDSA P-256 signatures and SHA-256 PoW simulation; `ETH/TON/SOL/TRX/USDC` balances and faucet deposits are local Sandbox state.
    *   **NFT Minting Receipts & Staking (`OnChainNftPanel`, `WeedProtocolPanel`)**: Local receipts, APY staking points, AMM router quotes, and exchange tickers run locally in Room/Sandbox.
    *   **Global Node Leaderboard**: Simulated local cluster telemetry unless connected to a future external signaling backend.

---

## 4. Roadmap for Full Mainnet / WAN Production

If transitioning from prototype/Sandbox to live production networks:
1.  **WAN Signaling Service**: Replace local leaderboard and LAN-only UDP discovery with a dedicated authenticated bootstrap/signaling backend (WebSocket / libp2p / gRPC).
2.  **Non-Custodial External Wallet Signer**: Replace Sandbox NFT receipts and staking balances with WalletConnect / hardware signer flows so private keys never leave external wallets during on-chain writes.
3.  **Release Hardening**: Supply release keystore secrets via CI environment variables (`KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD`) and audit R8-minified release artifacts.

---

## 5. Local Compilation & Testing
*   **Compile Kotlin / Build APK**: `gradle assembleDebug`
*   **Run Offline Unit & Robolectric Tests**: `gradle :app:testDebugUnitTest`
