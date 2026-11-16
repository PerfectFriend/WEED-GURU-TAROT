# Privacy Policy, Google Play Data Safety & Compliance Notice
**App:** WEED GURU / Cyber Tarot  
**Last Updated:** 2026-11-15  

---

## 1. Nature of Application & Entertainment / Sandbox Disclaimer
- **Entertainment & Creative Oracle:** WEED GURU / Cyber Tarot is a cyberpunk & botanical-themed tarot divination, collectible card game, and educational cryptography sandbox.
- **No Real Money / No Mainnet Custody:** All in-app `WEED` / `MAGIC` points, multi-chain Sandbox balances (`ETH`, `TON`, `SOL`, `TRX`, `USDC`), NFT mint receipts, AMM quotes, and ledger blocks are **local simulation state** unless a user explicitly configures an external read-only JSON-RPC URL (`NftMintingService`, read-only queries only). No real-money gambling, fiat deposits, or custodial crypto transfers are performed.
- **Controlled Substances Policy Compliance:** Botanical themes, strain lore, and "420 Cyber Botanica" card names are strictly fictional creative content. The application **does not promote, facilitate, broker, or enable the sale, purchase, or delivery of cannabis or any regulated substance**.

---

## 2. Age Gating & Target Audience
- This application is intended **solely for adult users (18+ / 21+ depending on local jurisdiction)** due to mature thematic references (botanical/cannabis culture lore and simulated Web3 terminology).
- During initial onboarding, users must explicitly accept terms/disclaimers and are advised of regional age restrictions. Not directed to children under 13 (or applicable regional age of digital consent).

---

## 3. Data Collected, Local Storage & Transmission (Google Play Data Safety)

### A. User-Provided Local Data (Stored Encrypted / On-Device by Default)
| Data Category | Examples | Storage & Protection | Transmitted Off-Device? |
| :--- | :--- | :--- | :--- |
| **Personal Profile Inputs** | Display nickname, date of birth (DOB), optional name/personal words for card entropy/astrology | Room SQLite (`TarotDatabase`) with `AndroidKeyStore` / encrypted preferences; `android:allowBackup="false"` | Only if user explicitly opts into optional Cloud Sync (Firestore) or Gemini AI |
| **Media / Avatar Selection** | User-selected profile avatar image | Zero-permission Android **Photo Picker** (`PickVisualMedia`); URI persisted locally | No (stays on device) |
| **Authentication & Security Secrets** | 8-digit PIN hash (`PBKDF2WithHmacSHA256`, 210k iterations), BIP-39 mnemonic, ECDSA keypair | Protected by Android Keystore AES-GCM & PBKDF2; never logged | **Never transmitted** |

### B. Optional Network Integrations (User-Initiated)
1. **Gemini AI (`generativelanguage.googleapis.com`):**
   - Triggered only when the user requests an AI reading/analysis and supplies a valid Gemini API key (otherwise runs offline fallback oracle).
   - Transmits reading question, drawn tarot cards, and selected response language over HTTPS.
2. **Firebase Authentication & Cloud Firestore (Optional Cloud Backup/Sync):**
   - Authenticated via Google Sign-In (`CredentialManager`).
   - Enforced by owner-scoped zero-trust `firestore.rules` (`request.auth.uid == userId`).
3. **EVM JSON-RPC (Optional Read-Only Check):**
   - Sends public `eth_chainId` / `eth_gasPrice` HTTPS requests only if the user enters an HTTPS RPC endpoint.
4. **LAN Swarm Storage Node (`SwarmStorageManager`, Disabled by Default):**
   - Activates only upon explicit user action in the Swarm panel.
   - Exchanges HMAC-SHA256-signed UDP discovery beacons (port `9944`) and serves **ciphertext-only AES-256-GCM** blocks over HMAC-authenticated TCP (port `9945`) on the local network. No plaintext shards or user PII are exposed on network sockets.

---

## 4. Android Permissions (Least Privilege)
- `android.permission.INTERNET` (required for optional Gemini API, Firebase sync, and read-only JSON-RPC checks).
- **No** `READ_EXTERNAL_STORAGE`, `READ_MEDIA_IMAGES`, `CAMERA`, `ACCESS_FINE_LOCATION`, or `RECORD_AUDIO` permissions are declared or requested.

---

## 5. User Controls & Data Deletion
- Users can wipe their local PIN, BIP-39 keys, and profile data directly from the Security / Onboarding reset flow or by clearing app storage / uninstalling.
- Cloud backup via ADB/Google Drive Auto Backup is disabled (`android:allowBackup="false"` in `AndroidManifest.xml`).
- Users utilizing optional Firebase Cloud Sync may sign out and delete their Firestore user document (`users/{uid}`) at any time.
