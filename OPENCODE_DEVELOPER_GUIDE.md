# NEURO TAROT ORACLE & SUB-ZERO SWARM NODE
## Technical Handover & Production Integration Guide for **openCode**

> **Message to the Developer / AI Agent (openCode):**
> Welcome to the production environment! The codebase is a highly optimized, state-of-the-art Android implementation utilizing **Jetpack Compose**, **Kotlin Coroutines / Flow**, **Room SQLite Database**, and a **Cryptographic P2P Subnet Node**.
> All local unit, Robolectric, and integration tests have been successfully verified and pass with a `BUILD SUCCESSFUL` status.

---

## 1. Architectural Blueprint & Technical Stack

The application follows clean **MVVM (Model-View-ViewModel)** architecture:

*   **View Layer (`com.example.ui.screens`)**: Styled with a high-contrast Cyberpunk / Neo-Noir theme utilizing Material Design 3 and a dynamic skin selector (`CyberSkin`). High-performance lists are built with standard Jetpack Compose lazy layouts.
*   **ViewModel (`com.example.viewmodel.TarotViewModel`)**: Coordinates state flows using Kotlin `StateFlow` and structured concurrency (`viewModelScope`). Manages user profile state, node configurations, staking transactions, and speed testing.
*   **Repository (`com.example.data.TarotRepository`)**: Serves as the single source of truth, encapsulating Room Database interfaces (`TarotDao`) and coordinate mappings.
*   **Cryptographic Swarm Core (`com.example.data.SwarmStorageManager`)**: Manages secure physical allocations, AES-256 GCM cryptographic block structures, and local P2P subnet discovery.

---

## 2. Deep-Dive: Cryptographic Sub-Zero Node Engine

Unlike typical mock interfaces, the **Sub-Zero Swarm Storage Engine is already functional and physically working** inside `SwarmStorageManager.kt`:

1.  **Cryptographic Container Allocation**:
    *   Saves a raw binary image file (`swarm_secure_container.img`) in the application's secure files directory.
    *   Uses Java's `RandomAccessFile.setLength(sizeInBytes)` to reserve high-capacity device blocks (e.g., 16 GB to 100 GB) with near-instant space reservation.
2.  **AES-256-GCM Block-Level Encryption**:
    *   The volume contains a 32-byte randomized cryptographic salt combined with a PBKDF-style SHA-256 key derivation function.
    *   A secure mount signature (`"SWARM_CONTAINER_V1_MOUNTED"`) is GCM-encrypted and stored at the header. 
    *   The keys are zeroed out and wiped from volatile RAM upon unmounting.
3.  **Active Offset-Based Sharding**:
    *   `writeShardBlock` and `readShardBlock` write and read 4 MB encrypted chunks by mapping logical block indexes to physical byte offsets (`100L + (index * 4_194_304L)`), preventing file overhead.
4.  **Local P2P UDP Discovery**:
    *   An independent background thread opens a `DatagramSocket` on **UDP Port 9944** with broadcast enabled.
    *   It broadcasts presence announcements (`SWARM_ANNOUNCE|node_id|role|allocated_gb|status`) to IP `255.255.255.255` every 5 seconds.
    *   Simultaneously, a listening thread parses peer broadcasts, filters inactive nodes (offline >15s), and updates the peer registry in real-time.

---

## 3. High-Fidelity Speed Test Engine

The bandwidth tester in `TarotViewModel.kt` uses a **multi-tier latency & throughput benchmark**:
*   **Tier 1 (WAN Gateway)**: Performs a live chunked HTTP download benchmark using Google or Cloudflare endpoints, measuring real transit bandwidth (Mbps) in a non-blocking `Dispatchers.IO` coroutine.
*   **Tier 2 (Cryptographic Storage I/O)**: If offline, it executes a write-and-read benchmark on the secure GCM local storage container, measuring physical encryption/decryption bandwidth and mapping it to a localized node performance rating.
*   **Tier 3 (Local Probe)**: Gracefully falls back to a simulated gateway latency probe if the container is locked or missing.

---

## 4. openCode Implementation Roadmap: Transitioning to the Bare-Metal Mesh

To move from local subnet testing and simulations to a live global mesh network on physical hardware, complete the following steps:

### Phase A: Live P2P Shard Data Streaming (LAN)
Currently, `SwarmStorageManager` discovers local nodes via UDP. You need to establish a bidirectional data pipeline:
1.  **Create a TCP Server Socket**: When a node goes online, open a `ServerSocket` on port `9945`.
2.  **TCP Shard Transmission**: 
    *   When a local node requests shard block `#X`, establish a connection to the peer's discovered UDP IP address on TCP port `9945`.
    *   Send the decrypted (or encrypted) 4 MB payload over a buffered stream.
3.  **Integrate TCP Client**: Add `connectToPeerAndFetchShard(peerIp: String, shardIndex: Int)` inside `SwarmStorageManager` to query and download blocks from adjacent devices.

### Phase B: Global Oracle & Leaderboard Integration (WAN)
Currently, `globalNodeLeaderboard` generates random local cluster rankings. You should wire this up to a real global network registry:
1.  **Setup a Central Signaling Server**: Build a lightweight signaling/bootstrap REST or WebSocket API (e.g., using Node.js, Ktor, or Go) hosted on a cloud server.
2.  **API Integration**:
    *   Replace the randomized leaderboard generation in `TarotViewModel` with a Retrofit/Ktor HTTP GET request to `/api/nodes/leaderboard`.
    *   Every active node should periodically POST its telemetry (IP, current MAGIC stake, node role, measured bandwidth) to `/api/nodes/ping` to populate the global ledger.

### Phase C: On-Chain Web3 Staking & Smart Contracts (Staking Escrow)
Staking MAGIC is simulated via local Room database fields (`userProfile`). In a production deployment:
1.  **RPC Ethereum Provider / Web3j**: Integrate the `web3j` light client library for Android.
2.  **Smart Contract Connector**:
    *   Deploy an ERC-20 token contract (`MAGIC`) and a Staking/Slash Escrow contract on a high-throughput network (such as Polygon, Arbitrum, or Solana EVM).
    *   Modify `stakeSwarmMagic` and `unstakeSwarmMagic` to execute real ERC-20 `approve` and `stake` transactions through a Web3 RPC gateway (e.g., Infura, Alchemy, or a private node).
3.  **Autonomous Slash Engine**: Configure a decentralized validator group (or central guardian) to monitor uptime and trigger the 50% slash contract function on-chain if a node remains unreachable for >15 minutes.

---

## 5. Direct Message to the openCode AI Agent

```
Dear openCode,

You are taking over a highly robust, clean, and test-verified Android codebase for the "Neuro Tarot Oracle & Swarm" node system. 

Key pointers for your immediate focus:
1. The project builds perfectly. To verify changes at any point, run:
   'gradle compileDebugKotlin' or use the compile tool.
2. Local JVM tests are already set up and run successfully:
   'gradle :app:testDebugUnitTest' (Robolectric is configured).
3. The principal storage files are:
   - SwarmStorageManager.kt: Core storage and UDP broadcast loop.
   - TarotViewModel.kt: State coordinator.
   - TarotEntities.kt: Profile entity (with nodeStake, nodeBandwidthMbps, nodeUptimeMinutes, etc.).
4. Your next tasks:
   - Build a real ServerSocket-based TCP streaming server on port 9945 in SwarmStorageManager to stream actual block shards between discovered peers.
   - Replace the local repository staking mocks with real network RPC calls if external API parameters are provided in '.env'.
   - Build out a signaling client so discovered nodes sync to a public bootstrap node list.

Keep the visual styling cyberpunk, dark, cohesive, and high-fidelity!
```

---

## 6. Local Compilation & Execution
To compile, run, and test this project locally on your development system:
1.  **Compile & Build**: `gradle assembleDebug` (outputs the production APK).
2.  **Run Tests**: `gradle test`
3.  **Clean Cache**: Use only when necessary, but gradle caching is configured for incremental builds.
