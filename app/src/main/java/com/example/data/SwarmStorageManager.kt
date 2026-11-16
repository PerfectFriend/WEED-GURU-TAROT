package com.example.data

import android.content.Context
import android.util.Log
import com.example.data.crypto.CryptoHelper
import com.example.data.crypto.SecureVaultHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.RandomAccessFile
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.concurrent.Semaphore
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

/**
 * Local Cryptographic Storage & Authenticated Peer Shard Node.
 * Manages encrypted container files (AES-256-GCM), strict ciphertext-only TCP shard serving
 * with HMAC-SHA256 challenge-response authentication, and signed LAN UDP announcements.
 * Disabled by default; activates only on explicit user action.
 */
class SwarmStorageManager private constructor(private val context: Context) {

    private val appContext = context.applicationContext
    private val sharedPrefs = appContext.getSharedPreferences("swarm_storage_prefs", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val tcpConcurrencyLimit = Semaphore(4)
    private val containerLock = Any()

    // Node state flows
    private val _containerAllocatedSize = MutableStateFlow(sharedPrefs.getLong("allocated_size", 0L))
    val containerAllocatedSize: StateFlow<Long> = _containerAllocatedSize.asStateFlow()

    private val _containerMounted = MutableStateFlow(false)
    val containerMounted: StateFlow<Boolean> = _containerMounted.asStateFlow()

    private val _nodeRole = MutableStateFlow(sharedPrefs.getString("node_role", "Storage") ?: "Storage")
    val nodeRole: StateFlow<String> = _nodeRole.asStateFlow()

    private val _nodeActive = MutableStateFlow(false)
    val nodeActive: StateFlow<Boolean> = _nodeActive.asStateFlow()

    private val _discoveredPeers = MutableStateFlow<List<SwarmPeer>>(emptyList())
    val discoveredPeers: StateFlow<List<SwarmPeer>> = _discoveredPeers.asStateFlow()

    private val _nodeLogs = MutableStateFlow<List<String>>(listOf("System Initialized. Node idle (disabled by default)."))
    val nodeLogs: StateFlow<List<String>> = _nodeLogs.asStateFlow()

    private val _isAllocating = MutableStateFlow(false)
    val isAllocating: StateFlow<Boolean> = _isAllocating.asStateFlow()

    private val _allocationProgress = MutableStateFlow(0f)
    val allocationProgress: StateFlow<Float> = _allocationProgress.asStateFlow()

    private var activeSecretKey: SecretKeySpec? = null
    private var containerFile: File = File(appContext.filesDir, "swarm_secure_container.img")

    // UDP Peer Discovery Socket
    private var discoverySocket: DatagramSocket? = null
    private var broadcastJob: Job? = null
    private var listenJob: Job? = null

    // TCP Shard Streaming Server Socket
    private var tcpServerSocket: ServerSocket? = null
    private var tcpServerJob: Job? = null
    private var allocationJob: Job? = null

    companion object {
        @Volatile
        private var INSTANCE: SwarmStorageManager? = null

        fun getInstance(context: Context): SwarmStorageManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SwarmStorageManager(context.applicationContext).also { INSTANCE = it }
            }
        }

        private const val UDP_DISCOVERY_PORT = 9944
        const val TCP_STREAM_PORT = 9945
        private const val SHARD_SIZE_BYTES = 4 * 1024 * 1024 // 4 MB Shards
        private const val HEADER_OFFSET_BYTES = 100L
        private const val CHALLENGE_SIZE_BYTES = 32
        private const val HASH_SIZE_BYTES = 32
        private const val SOCKET_TIMEOUT_MS = 5_000
    }

    init {
        if (containerFile.exists()) {
            addLog("Found existing encrypted container. Size: ${containerFile.length() / (1024 * 1024)} MB.")
            if (_containerAllocatedSize.value == 0L) {
                _containerAllocatedSize.value = containerFile.length()
            }
        } else {
            addLog("No active cryptographic storage container detected.")
        }
    }

    fun addLog(msg: String) {
        val current = _nodeLogs.value.toMutableList()
        current.add(0, "[${System.currentTimeMillis() % 1000000}] $msg")
        if (current.size > 100) current.removeAt(current.lastIndex)
        _nodeLogs.value = current
    }

    fun allocateContainer(sizeInBytes: Long, passphrase: String, onFinished: (Boolean) -> Unit) {
        if (_isAllocating.value) {
            addLog("Allocation job already in progress!")
            return
        }

        _isAllocating.value = true
        _allocationProgress.value = 0.05f
        addLog("Preparing local encrypted storage container (${sizeInBytes / (1024 * 1024)} MB)...")

        allocationJob?.cancel()
        allocationJob = scope.launch {
            try {
                synchronized(containerLock) {
                    if (containerFile.exists()) {
                        containerFile.delete()
                    }
                    containerFile.parentFile?.mkdirs()

                    RandomAccessFile(containerFile, "rw").use { raf ->
                        raf.setLength(sizeInBytes)
                        _allocationProgress.value = 0.3f

                        val salt = ByteArray(32).also { SecureRandom().nextBytes(it) }
                        raf.seek(0)
                        raf.write(salt)

                        val key = deriveKey(passphrase, salt)
                        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                        val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
                        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
                        val encryptedSig = cipher.doFinal("SWARM_CONTAINER_V1_MOUNTED".toByteArray(Charsets.UTF_8))

                        raf.write(iv)
                        raf.write(encryptedSig)
                    }
                }

                _containerAllocatedSize.value = sizeInBytes
                sharedPrefs.edit().putLong("allocated_size", sizeInBytes).apply()
                _allocationProgress.value = 1.0f

                addLog("Cryptographic container initialized. Ready to mount.")
                withContext(Dispatchers.Main) { onFinished(true) }
            } catch (e: Exception) {
                Log.e("SwarmStorage", "Allocation failed", e)
                addLog("Critical: Container initialization error: ${e.message}")
                _allocationProgress.value = 0f
                withContext(Dispatchers.Main) { onFinished(false) }
            } finally {
                _isAllocating.value = false
            }
        }
    }

    fun deallocateContainer() {
        unmountContainer()
        synchronized(containerLock) {
            if (containerFile.exists()) {
                val deleted = containerFile.delete()
                if (deleted) {
                    _containerAllocatedSize.value = 0L
                    sharedPrefs.edit().putLong("allocated_size", 0L).apply()
                    addLog("Crypto-container purged from device disk.")
                } else {
                    addLog("Error: Unable to delete container file.")
                }
            } else {
                addLog("No active container exists to deallocate.")
            }
        }
    }

    fun mountContainer(passphrase: String): Boolean {
        synchronized(containerLock) {
            if (!containerFile.exists()) {
                addLog("Error: Cryptographic image file not found. Allocate container first.")
                return false
            }

            return try {
                val salt = ByteArray(32)
                val iv = ByteArray(12)
                val encryptedSig = ByteArray(42)

                RandomAccessFile(containerFile, "r").use { raf ->
                    raf.readFully(salt)
                    raf.readFully(iv)
                    raf.readFully(encryptedSig)
                }

                val key = deriveKey(passphrase, salt)
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
                val sigText = String(cipher.doFinal(encryptedSig), Charsets.UTF_8)

                if (sigText == "SWARM_CONTAINER_V1_MOUNTED") {
                    activeSecretKey = key
                    _containerMounted.value = true
                    addLog("Volume mounted. Decryption key active strictly in local memory.")
                    true
                } else {
                    addLog("Mount failure: Signature mismatch.")
                    false
                }
            } catch (e: Exception) {
                Log.e("SwarmStorage", "Mount failed", e)
                addLog("Mount failure: Passphrase rejected. Volume remains locked.")
                false
            }
        }
    }

    fun unmountContainer() {
        activeSecretKey = null
        _containerMounted.value = false
        addLog("Volume unmounted. Active key reference cleared.")
    }

    private fun maxShardCount(): Int {
        val usable = (_containerAllocatedSize.value - HEADER_OFFSET_BYTES).coerceAtLeast(0L)
        return (usable / SHARD_SIZE_BYTES).toInt().coerceAtLeast(0)
    }

    private fun isValidShardIndex(shardIndex: Int): Boolean {
        return shardIndex >= 0 && shardIndex < maxShardCount()
    }

    fun writeShardBlock(shardIndex: Int, data: ByteArray): Boolean {
        val key = activeSecretKey
        if (key == null || !_containerMounted.value) {
            addLog("Write blocked: Volume is currently unmounted.")
            return false
        }
        if (shardIndex < 0) {
            addLog("Write blocked: Negative shard index $shardIndex.")
            return false
        }

        return synchronized(containerLock) {
            try {
                if (data.size > SHARD_SIZE_BYTES - 100) {
                    addLog("Error: Data block exceeds 4MB shard limit.")
                    return@synchronized false
                }
                if (!isValidShardIndex(shardIndex)) {
                    addLog("Error: Shard index #$shardIndex out of bounds.")
                    return@synchronized false
                }

                val offset = HEADER_OFFSET_BYTES + (shardIndex * SHARD_SIZE_BYTES.toLong())
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
                cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
                val encryptedData = cipher.doFinal(data)

                RandomAccessFile(containerFile, "rw").use { raf ->
                    raf.seek(offset)
                    raf.write(iv)
                    raf.writeInt(encryptedData.size)
                    raf.write(encryptedData)
                }

                addLog("Swarm Shard #$shardIndex encrypted and committed locally.")
                true
            } catch (e: Exception) {
                Log.e("SwarmStorage", "Block write failed", e)
                addLog("Error writing shard block: ${e.message}")
                false
            }
        }
    }

    /**
     * Local-only decryption for the device owner. NEVER exposed over network sockets.
     */
    fun readShardBlock(shardIndex: Int): ByteArray? {
        val key = activeSecretKey
        if (key == null || !_containerMounted.value) {
            addLog("Read blocked: Volume is unmounted.")
            return null
        }
        if (!isValidShardIndex(shardIndex)) return null

        return synchronized(containerLock) {
            try {
                val offset = HEADER_OFFSET_BYTES + (shardIndex * SHARD_SIZE_BYTES.toLong())
                val iv = ByteArray(12)
                var size = 0
                val encryptedData: ByteArray

                RandomAccessFile(containerFile, "r").use { raf ->
                    raf.seek(offset)
                    raf.readFully(iv)
                    size = raf.readInt()
                    if (size <= 0 || size > SHARD_SIZE_BYTES) {
                        return@synchronized null
                    }
                    encryptedData = ByteArray(size)
                    raf.readFully(encryptedData)
                }

                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
                cipher.doFinal(encryptedData)
            } catch (e: Exception) {
                Log.e("SwarmStorage", "Block read failed", e)
                null
            }
        }
    }

    /**
     * Reads ONLY the raw encrypted frame ([12B IV][4B size][AES-GCM ciphertext]) for network replication.
     * Decryption never occurs here and plaintext never leaves the device.
     */
    fun readEncryptedShardFrame(shardIndex: Int): ByteArray? {
        if (!containerFile.exists() || !isValidShardIndex(shardIndex)) return null

        return synchronized(containerLock) {
            try {
                val offset = HEADER_OFFSET_BYTES + (shardIndex * SHARD_SIZE_BYTES.toLong())
                RandomAccessFile(containerFile, "r").use { raf ->
                    raf.seek(offset)
                    val iv = ByteArray(12)
                    raf.readFully(iv)
                    val size = raf.readInt()
                    if (size <= 0 || size > SHARD_SIZE_BYTES - 16) {
                        return@synchronized null
                    }
                    val encryptedData = ByteArray(size)
                    raf.readFully(encryptedData)

                    ByteBuffer.allocate(12 + 4 + size)
                        .put(iv)
                        .putInt(size)
                        .put(encryptedData)
                        .array()
                }
            } catch (e: Exception) {
                Log.e("SwarmStorage", "Encrypted frame read failed", e)
                null
            }
        }
    }

    /**
     * Stores an already-encrypted shard frame fetched from an authenticated peer directly to disk.
     */
    fun writeEncryptedShardFrame(shardIndex: Int, frame: ByteArray): Boolean {
        if (!containerFile.exists() || !isValidShardIndex(shardIndex)) return false
        if (frame.size <= 16 || frame.size > SHARD_SIZE_BYTES) return false

        val buf = ByteBuffer.wrap(frame)
        val iv = ByteArray(12)
        buf.get(iv)
        val encLen = buf.int
        if (encLen <= 0 || encLen != frame.size - 16) return false

        return synchronized(containerLock) {
            try {
                val offset = HEADER_OFFSET_BYTES + (shardIndex * SHARD_SIZE_BYTES.toLong())
                RandomAccessFile(containerFile, "rw").use { raf ->
                    raf.seek(offset)
                    raf.write(frame)
                }
                addLog("Stored encrypted replica frame for Shard #$shardIndex (${frame.size} bytes).")
                true
            } catch (e: Exception) {
                Log.e("SwarmStorage", "Failed storing encrypted shard frame", e)
                false
            }
        }
    }

    fun setNodeRole(role: String) {
        _nodeRole.value = role
        sharedPrefs.edit().putString("node_role", role).apply()
        addLog("Node operational level set to: $role.")
    }

    private fun buildChallengeHmac(clusterKey: ByteArray, challenge: ByteArray, shardIndex: Int): ByteArray {
        val msg = ByteBuffer.allocate(challenge.size + 4)
            .put(challenge)
            .putInt(shardIndex)
            .array()
        return SecureVaultHelper.computeHmacSha256(clusterKey, msg)
    }

    /**
     * Authenticated ciphertext-only TCP server on port 9945:
     * 1. Sends 32-byte random challenge.
     * 2. Verifies client HMAC-SHA256(clusterKey, challenge || shardIndex) in constant time.
     * 3. Validates shardIndex bounds and streams encrypted frame + SHA-256 digest (NEVER plaintext).
     */
    private fun startTcpServer() {
        val clusterKey = SecureVaultHelper.getOrCreateSwarmClusterKey(appContext)
        tcpServerJob = scope.launch(Dispatchers.IO) {
            try {
                val serverSocket = ServerSocket(TCP_STREAM_PORT)
                tcpServerSocket = serverSocket
                addLog("📡 Authenticated Encrypted TCP Shard Server listening on port $TCP_STREAM_PORT.")

                while (isActive) {
                    val clientSocket = try {
                        serverSocket.accept()
                    } catch (_: Exception) {
                        break
                    }

                    if (!tcpConcurrencyLimit.tryAcquire()) {
                        runCatching { clientSocket.close() }
                        continue
                    }

                    scope.launch(Dispatchers.IO) {
                        try {
                            clientSocket.use { socket ->
                                socket.soTimeout = SOCKET_TIMEOUT_MS
                                val input = DataInputStream(socket.getInputStream())
                                val output = DataOutputStream(socket.getOutputStream())

                                val challenge = ByteArray(CHALLENGE_SIZE_BYTES).also { SecureRandom().nextBytes(it) }
                                output.write(challenge)
                                output.flush()

                                val clientMac = ByteArray(HASH_SIZE_BYTES)
                                input.readFully(clientMac)
                                val requestedIndex = input.readInt()

                                val expectedMac = buildChallengeHmac(clusterKey, challenge, requestedIndex)
                                if (!MessageDigest.isEqual(expectedMac, clientMac)) {
                                    output.writeInt(-2) // Auth rejected
                                    output.flush()
                                    addLog("⛔ TCP: Rejected unauthenticated shard request for #$requestedIndex.")
                                    return@use
                                }

                                if (!isValidShardIndex(requestedIndex)) {
                                    output.writeInt(-1)
                                    output.flush()
                                    addLog("⚠️ TCP: Out-of-bounds shard request #$requestedIndex rejected.")
                                    return@use
                                }

                                val encryptedFrame = readEncryptedShardFrame(requestedIndex)
                                if (encryptedFrame != null) {
                                    val digest = MessageDigest.getInstance("SHA-256").digest(encryptedFrame)
                                    output.writeInt(encryptedFrame.size)
                                    output.write(encryptedFrame)
                                    output.write(digest)
                                    output.flush()
                                    addLog("📤 TCP: Served encrypted frame for Shard #$requestedIndex (${encryptedFrame.size} bytes).")
                                } else {
                                    output.writeInt(-1)
                                    output.flush()
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("SwarmTcp", "Error processing TCP peer connection", e)
                        } finally {
                            tcpConcurrencyLimit.release()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SwarmTcp", "TCP Server stopped", e)
                addLog("⚠️ TCP Server stopped: ${e.message}")
            }
        }
    }

    private fun stopTcpServer() {
        tcpServerJob?.cancel()
        runCatching { tcpServerSocket?.close() }
        tcpServerSocket = null
        tcpServerJob = null
        addLog("TCP Shard Server is OFFLINE.")
    }

    /**
     * Connects to an authenticated peer on TCP port 9945, completes HMAC-SHA256 handshake,
     * downloads encrypted shard frame, and verifies SHA-256 integrity hash.
     */
    fun fetchShardFromPeer(peerIp: String, shardIndex: Int): ByteArray? {
        if (shardIndex < 0) return null
        val clusterKey = SecureVaultHelper.getOrCreateSwarmClusterKey(appContext)
        addLog("🔌 TCP: Connecting to peer node for encrypted Shard #$shardIndex...")
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(peerIp, TCP_STREAM_PORT), SOCKET_TIMEOUT_MS)
                socket.soTimeout = SOCKET_TIMEOUT_MS
                val output = DataOutputStream(socket.getOutputStream())
                val input = DataInputStream(socket.getInputStream())

                val challenge = ByteArray(CHALLENGE_SIZE_BYTES)
                input.readFully(challenge)

                val mac = buildChallengeHmac(clusterKey, challenge, shardIndex)
                output.write(mac)
                output.writeInt(shardIndex)
                output.flush()

                val size = input.readInt()
                if (size == -2) {
                    addLog("⛔ TCP: Peer rejected cluster authentication.")
                    null
                } else if (size <= 0) {
                    addLog("⚠️ TCP: Peer reported encrypted shard #$shardIndex unavailable.")
                    null
                } else if (size > SHARD_SIZE_BYTES) {
                    addLog("⚠️ TCP: Peer returned anomalous payload size ($size bytes). Rejected.")
                    null
                } else {
                    val frame = ByteArray(size)
                    input.readFully(frame)
                    val peerDigest = ByteArray(HASH_SIZE_BYTES)
                    input.readFully(peerDigest)

                    val actualDigest = MessageDigest.getInstance("SHA-256").digest(frame)
                    if (!MessageDigest.isEqual(actualDigest, peerDigest)) {
                        addLog("⛔ TCP: Shard #$shardIndex integrity digest mismatch! Discarded.")
                        return null
                    }
                    addLog("✅ TCP: Verified & received encrypted Shard #$shardIndex ($size bytes).")
                    frame
                }
            }
        } catch (e: Exception) {
            Log.e("SwarmTcp", "Failed to fetch shard from peer", e)
            addLog("❌ TCP: Peer connection failed: ${e.message}")
            null
        }
    }

    fun startNode() {
        if (_nodeActive.value) return
        _nodeActive.value = true
        val clusterKey = SecureVaultHelper.getOrCreateSwarmClusterKey(appContext)
        addLog("Activating Local Swarm Node on UDP $UDP_DISCOVERY_PORT / TCP $TCP_STREAM_PORT...")

        startTcpServer()

        scope.launch {
            try {
                val socket = DatagramSocket(UDP_DISCOVERY_PORT).apply { broadcast = true }
                discoverySocket = socket

                broadcastJob = scope.launch {
                    val nodeId = "node_" + Random.nextInt(100000, 999999)
                    while (isActive) {
                        try {
                            val activeSizeGb = String.format("%.2f", _containerAllocatedSize.value / (1024 * 1024 * 1024.0))
                            val body = "SWARM_ANNOUNCE|$nodeId|${_nodeRole.value}|$activeSizeGb|${if (_containerMounted.value) "MOUNTED" else "LOCKED"}"
                            val sigHex = SecureVaultHelper.computeHmacSha256(clusterKey, body.toByteArray(Charsets.UTF_8))
                                .joinToString("") { "%02x".format(it) }
                            val bytes = "$body|$sigHex".toByteArray(Charsets.UTF_8)

                            val address = InetAddress.getByName("255.255.255.255")
                            val packet = DatagramPacket(bytes, bytes.size, address, UDP_DISCOVERY_PORT)
                            socket.send(packet)
                        } catch (e: Exception) {
                            Log.e("SwarmNode", "Broadcast send error", e)
                        }
                        delay(5000)
                    }
                }

                listenJob = scope.launch {
                    val buffer = ByteArray(1024)
                    while (isActive) {
                        try {
                            val packet = DatagramPacket(buffer, buffer.size)
                            socket.receive(packet)

                            val rawText = String(packet.data, 0, packet.length, Charsets.UTF_8)
                            if (rawText.startsWith("SWARM_ANNOUNCE|")) {
                                val parts = rawText.split("|")
                                if (parts.size >= 6) {
                                    val body = parts.subList(0, 5).joinToString("|")
                                    val expectedSigHex = SecureVaultHelper.computeHmacSha256(
                                        clusterKey,
                                        body.toByteArray(Charsets.UTF_8)
                                    ).joinToString("") { "%02x".format(it) }
                                    if (!CryptoHelper.constantTimeEquals(parts[5], expectedSigHex)) {
                                        continue
                                    }

                                    val peerId = parts[1]
                                    val peerRole = parts[2]
                                    val peerSize = parts[3]
                                    val peerStatus = parts[4]
                                    val peerIp = packet.address.hostAddress ?: "Unknown"

                                    val currentPeers = _discoveredPeers.value.toMutableList()
                                    val index = currentPeers.indexOfFirst { it.id == peerId }

                                    val updatedPeer = SwarmPeer(
                                        id = peerId,
                                        ip = peerIp,
                                        role = peerRole,
                                        allocatedGb = peerSize,
                                        status = peerStatus,
                                        lastSeen = System.currentTimeMillis()
                                    )

                                    if (index >= 0) {
                                        currentPeers[index] = updatedPeer
                                    } else {
                                        currentPeers.add(updatedPeer)
                                        addLog("📡 SWARM: Verified local peer '$peerId' discovered.")
                                    }

                                    _discoveredPeers.value = currentPeers.filter {
                                        System.currentTimeMillis() - it.lastSeen < 15_000
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            if (!isActive) break
                            Log.e("SwarmNode", "Listen socket receive error", e)
                        }
                    }
                }

                addLog("Authenticated LAN Swarm listener is ONLINE.")
            } catch (e: Exception) {
                Log.e("SwarmNode", "Node socket start failed", e)
                addLog("Node activation failed: ${e.message}.")
                _nodeActive.value = false
            }
        }
    }

    fun stopNode() {
        if (!_nodeActive.value) return
        _nodeActive.value = false
        addLog("Shutting down Swarm broadcasts and closing sockets...")

        broadcastJob?.cancel()
        listenJob?.cancel()
        runCatching { discoverySocket?.close() }
        discoverySocket = null

        stopTcpServer()

        _discoveredPeers.value = emptyList()
        addLog("Swarm node is OFFLINE.")
    }

    /**
     * Releases all background jobs, sockets, and mounted key references safely
     * while keeping the singleton's SupervisorJob usable if reactivated later.
     */
    fun shutdown() {
        stopNode()
        unmountContainer()
        allocationJob?.cancel()
        allocationJob = null
        _isAllocating.value = false
        scope.coroutineContext[Job]?.cancelChildren()
        addLog("Swarm manager background workers shut down.")
    }

    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        return CryptoHelper.deriveKeyPbkdf2(
            passphrase = password.toCharArray(),
            salt = salt,
            iterations = CryptoHelper.PBKDF2_ITERATIONS
        )
    }
}

data class SwarmPeer(
    val id: String,
    val ip: String,
    val role: String,
    val allocatedGb: String,
    val status: String,
    val lastSeen: Long
)
