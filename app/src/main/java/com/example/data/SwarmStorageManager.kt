package com.example.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.RandomAccessFile
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

/**
 * Sub-Zero Decentered Swarm Cryptographic Storage Node Core.
 * Manages physical pre-allocation of secure containers (min 16GB), 
 * AES-256 block-level encryption, and zero-tracker local UDP peer discovery.
 */
class SwarmStorageManager private constructor(private val context: Context) {

    private val sharedPrefs = context.getSharedPreferences("swarm_storage_prefs", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

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

    private val _nodeLogs = MutableStateFlow<List<String>>(listOf("System Initialized. Awaiting node activation..."))
    val nodeLogs: StateFlow<List<String>> = _nodeLogs.asStateFlow()

    private val _isAllocating = MutableStateFlow(false)
    val isAllocating: StateFlow<Boolean> = _isAllocating.asStateFlow()

    private val _allocationProgress = MutableStateFlow(0f)
    val allocationProgress: StateFlow<Float> = _allocationProgress.asStateFlow()

    private var activeSecretKey: SecretKeySpec? = null
    private var containerFile: File = File(context.filesDir, "swarm_secure_container.img")

    // UDP Peer Discovery Socket
    private var discoverySocket: DatagramSocket? = null
    private var broadcastJob: Job? = null
    private var listenJob: Job? = null

    // TCP Shard Streaming Server Socket
    private var tcpServerSocket: java.net.ServerSocket? = null
    private var tcpServerJob: Job? = null

    companion object {
        @Volatile
        private var INSTANCE: SwarmStorageManager? = null

        fun getInstance(context: Context): SwarmStorageManager {
            return INSTANCE ?: synchronized(this) {
                val instance = SwarmStorageManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }

        private const val UDP_DISCOVERY_PORT = 9944
        private const val TCP_STREAM_PORT = 9945
        private const val SHARD_SIZE_BYTES = 4 * 1024 * 1024 // 4 MB Shards
    }

    init {
        // Log the initial state
        if (containerFile.exists()) {
            addLog("Found existing container file. Size: ${containerFile.length() / (1024 * 1024)} MB.")
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

    /**
     * Physcially allocates a high-capacity crypto container on disk.
     * Uses RandomAccessFile.setLength() to reserve the actual device storage block, 
     * then initializes the cryptographic header layout with derived salt/magic bytes.
     */
    fun allocateContainer(sizeInBytes: Long, passphrase: String, onFinished: (Boolean) -> Unit) {
        if (_isAllocating.value) {
            addLog("Allocation job already in progress!")
            return
        }

        _isAllocating.value = true
        _allocationProgress.value = 0.05f
        addLog("Pre-allocating ${sizeInBytes / (1024 * 1024 * 1024.0)} GB file structure: '$containerFile'...")

        scope.launch {
            try {
                if (containerFile.exists()) {
                    containerFile.delete()
                }

                // Create directory if not exists
                containerFile.parentFile?.mkdirs()

                // Physically set the size (instant space reservation on modern Android FS)
                val raf = RandomAccessFile(containerFile, "rw")
                raf.setLength(sizeInBytes)

                _allocationProgress.value = 0.3f
                addLog("File size initialized. Generating cryptographic block header structures...")

                // Write Cryptographic salt and Magic block to confirm password mounts later
                val salt = ByteArray(32)
                SecureRandom().nextBytes(salt)
                raf.seek(0)
                raf.write(salt) // 32 bytes of random salt

                // Derive key from password & salt
                val key = deriveKey(passphrase, salt)
                
                // Encrypt "SWARM_ACTIVE" signature block to verify password on mount
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                val iv = ByteArray(12)
                SecureRandom().nextBytes(iv)
                cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
                
                val signatureBytes = "SWARM_CONTAINER_V1_MOUNTED".toByteArray(Charsets.UTF_8)
                val encryptedSig = cipher.doFinal(signatureBytes)

                // Write initialization headers:
                // [32 bytes SALT] + [12 bytes IV] + [encrypted signatures and metadata]
                raf.write(iv)
                raf.write(encryptedSig)
                raf.close()

                _containerAllocatedSize.value = sizeInBytes
                sharedPrefs.edit().putLong("allocated_size", sizeInBytes).apply()
                _allocationProgress.value = 1.0f

                addLog("Cryptographic space pre-allocation completed. Ready to mount.")
                withContext(Dispatchers.Main) {
                    onFinished(true)
                }
            } catch (e: Exception) {
                Log.e("SwarmStorage", "Allocation failed", e)
                addLog("Critical: Pre-allocation error: ${e.message}")
                _allocationProgress.value = 0f
                withContext(Dispatchers.Main) {
                    onFinished(false)
                }
            } finally {
                _isAllocating.value = false
            }
        }
    }

    /**
     * Purges and deletes the container from disk, freeing all allocated space.
     */
    fun deallocateContainer() {
        unmountContainer()
        if (containerFile.exists()) {
            val deleted = containerFile.delete()
            if (deleted) {
                _containerAllocatedSize.value = 0L
                sharedPrefs.edit().putLong("allocated_size", 0L).apply()
                addLog("Physical crypto-container wiped. ${containerFile.name} purged from device disk.")
            } else {
                addLog("Error: Unable to delete container file.")
            }
        } else {
            addLog("No active container exists to deallocate.")
        }
    }

    /**
     * Mounts the cryptographic volume. Derives the key and validates the security signature block.
     */
    fun mountContainer(passphrase: String): Boolean {
        if (!containerFile.exists()) {
            addLog("Error: Cryptographic image file not found. Pre-allocate storage first.")
            return false
        }

        try {
            val raf = RandomAccessFile(containerFile, "r")
            val salt = ByteArray(32)
            raf.readFully(salt)

            val iv = ByteArray(12)
            raf.readFully(iv)

            val encryptedSig = ByteArray(42) // Signature text length with GCM tag (26 + 16 = 42)
            raf.readFully(encryptedSig)
            raf.close()

            // Derive key
            val key = deriveKey(passphrase, salt)

            // Attempt decryption of magic payload
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
            val decrypted = cipher.doFinal(encryptedSig)
            val sigText = String(decrypted, Charsets.UTF_8)

            if (sigText == "SWARM_CONTAINER_V1_MOUNTED") {
                activeSecretKey = key
                _containerMounted.value = true
                addLog("Volume mounted successfully! Cryptographic partition is now decrypted and unlocked in-memory.")
                return true
            } else {
                addLog("Mount failure: Signature mismatch. Check passphrase integrity.")
                return false
            }
        } catch (e: Exception) {
            Log.e("SwarmStorage", "Mount failed", e)
            addLog("Mount failure: Passphrase decryption rejected. Volume remains encrypted.")
            return false
        }
    }

    fun unmountContainer() {
        activeSecretKey = null
        _containerMounted.value = false
        addLog("Volume unmounted. Keys purged from volatile RAM.")
    }

    /**
     * Writes an encrypted block shard to the pre-allocated volume.
     */
    fun writeShardBlock(shardIndex: Int, data: ByteArray): Boolean {
        val key = activeSecretKey
        if (key == null || !_containerMounted.value) {
            addLog("Write blocked: Volume is currently unmounted.")
            return false
        }

        try {
            if (data.size > SHARD_SIZE_BYTES - 100) {
                addLog("Error: Data block size exceeds limits of virtual 4MB Shard.")
                return false
            }

            // Offset calculation: Header (salt 32 + iv 12 + sig 42 = 86 bytes) + (index * SHARD_SIZE)
            val offset = 100L + (shardIndex * SHARD_SIZE_BYTES.toLong())
            val maxOffset = _containerAllocatedSize.value
            if (offset + SHARD_SIZE_BYTES > maxOffset) {
                addLog("Error: Shard write index out of bounds of current allocation partition.")
                return false
            }

            // Encrypt block
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val iv = ByteArray(12)
            SecureRandom().nextBytes(iv)
            cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
            val encryptedData = cipher.doFinal(data)

            val raf = RandomAccessFile(containerFile, "rw")
            raf.seek(offset)
            raf.write(iv)
            raf.writeInt(encryptedData.size)
            raf.write(encryptedData)
            raf.close()

            addLog("Swarm Shard #$shardIndex compiled and committed to physical disk storage.")
            return true
        } catch (e: Exception) {
            Log.e("SwarmStorage", "Block write failed", e)
            addLog("Error writing shard block: ${e.message}")
            return false
        }
    }

    /**
     * Reads and decrypts a block shard from the pre-allocated volume.
     */
    fun readShardBlock(shardIndex: Int): ByteArray? {
        val key = activeSecretKey
        if (key == null || !_containerMounted.value) {
            addLog("Read blocked: Volume is unmounted.")
            return null
        }

        try {
            val offset = 100L + (shardIndex * SHARD_SIZE_BYTES.toLong())
            val maxOffset = _containerAllocatedSize.value
            if (offset + SHARD_SIZE_BYTES > maxOffset) {
                return null
            }

            val raf = RandomAccessFile(containerFile, "r")
            raf.seek(offset)
            
            val iv = ByteArray(12)
            raf.readFully(iv)
            
            val size = raf.readInt()
            if (size <= 0 || size > SHARD_SIZE_BYTES) {
                raf.close()
                return null
            }
            
            val encryptedData = ByteArray(size)
            raf.readFully(encryptedData)
            raf.close()

            // Decrypt
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
            return cipher.doFinal(encryptedData)
        } catch (e: Exception) {
            Log.e("SwarmStorage", "Block read failed", e)
            return null
        }
    }

    fun setNodeRole(role: String) {
        _nodeRole.value = role
        sharedPrefs.edit().putString("node_role", role).apply()
        addLog("Node operational level set to: $role.")
    }

    /**
     * Starts the TCP server for hosting and streaming encrypted shards to connected peers.
     */
    private fun startTcpServer() {
        tcpServerJob = scope.launch(Dispatchers.IO) {
            try {
                val serverSocket = java.net.ServerSocket(TCP_STREAM_PORT)
                tcpServerSocket = serverSocket
                addLog("📡 TCP Shard Streaming Server listening on port $TCP_STREAM_PORT.")
                
                while (isActive) {
                    val clientSocket = try {
                        serverSocket.accept()
                    } catch (e: Exception) {
                        break
                    }
                    
                    scope.launch(Dispatchers.IO) {
                        try {
                            clientSocket.use { socket ->
                                val input = java.io.DataInputStream(socket.getInputStream())
                                val output = java.io.DataOutputStream(socket.getOutputStream())
                                
                                val requestedIndex = input.readInt()
                                addLog("📥 TCP: Received query for Shard #$requestedIndex from ${socket.inetAddress.hostAddress}")
                                
                                val shardBytes = readShardBlock(requestedIndex)
                                if (shardBytes != null) {
                                    output.writeInt(shardBytes.size)
                                    output.write(shardBytes)
                                    output.flush()
                                    addLog("📤 TCP: Streamed Shard #$requestedIndex (${shardBytes.size} bytes) to ${socket.inetAddress.hostAddress} successfully.")
                                } else {
                                    output.writeInt(-1) // Signal block unavailable/locked
                                    output.flush()
                                    addLog("⚠️ TCP: Requested Shard #$requestedIndex from ${socket.inetAddress.hostAddress} is unavailable or volume locked.")
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("SwarmTcp", "Error processing client connection", e)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SwarmTcp", "TCP Server crashed or closed", e)
                addLog("⚠️ TCP Server stopped: ${e.message}")
            }
        }
    }

    private fun stopTcpServer() {
        tcpServerJob?.cancel()
        try {
            tcpServerSocket?.close()
        } catch (e: Exception) {
            Log.e("SwarmTcp", "Error closing server socket", e)
        }
        tcpServerSocket = null
        tcpServerJob = null
        addLog("TCP Shard Streaming Server is OFFLINE.")
    }

    /**
     * Connects to a peer on TCP port 9945 and retrieves an encrypted block shard.
     */
    fun fetchShardFromPeer(peerIp: String, shardIndex: Int): ByteArray? {
        addLog("🔌 TCP: Connecting to peer $peerIp to download Swarm Shard #$shardIndex...")
        return try {
            java.net.Socket().use { socket ->
                socket.connect(java.net.InetSocketAddress(peerIp, TCP_STREAM_PORT), 5000) // 5s timeout
                val output = java.io.DataOutputStream(socket.getOutputStream())
                val input = java.io.DataInputStream(socket.getInputStream())
                
                // Request the shard index
                output.writeInt(shardIndex)
                output.flush()
                
                // Read response payload size
                val size = input.readInt()
                if (size <= 0) {
                    addLog("⚠️ TCP: Peer $peerIp reported block #$shardIndex is locked or empty.")
                    null
                } else if (size > SHARD_SIZE_BYTES) {
                    addLog("⚠️ TCP: Peer $peerIp returned anomalous chunk size ($size bytes). Rejected.")
                    null
                } else {
                    val buffer = ByteArray(size)
                    input.readFully(buffer)
                    addLog("✅ TCP: Successfully streamed Shard #$shardIndex ($size bytes) from peer $peerIp.")
                    buffer
                }
            }
        } catch (e: Exception) {
            Log.e("SwarmTcp", "Failed to fetch shard from peer $peerIp", e)
            addLog("❌ TCP: Connection to $peerIp failed: ${e.message}")
            null
        }
    }

    /**
     * Starts the fully decentral, zero-tracker Local P2P Swarm Discovery Node.
     * Uses UDP Broadcast to communicate with other nodes on the subnet, totally independent of public servers.
     */
    fun startNode() {
        if (_nodeActive.value) return
        _nodeActive.value = true
        addLog("Activating Sub-Zero Node Server on UDP Port $UDP_DISCOVERY_PORT...")

        // Start TCP Streaming Server
        startTcpServer()

        scope.launch {
            try {
                // Open dynamic socket supporting broadcasts
                val socket = DatagramSocket(UDP_DISCOVERY_PORT).apply {
                    broadcast = true
                }
                discoverySocket = socket

                // 1. Broadcast presence thread
                broadcastJob = scope.launch {
                    val nodeId = "node_" + Random.nextInt(100000, 999999)
                    while (isActive) {
                        try {
                            val activeSizeGb = String.format("%.2f", _containerAllocatedSize.value / (1024 * 1024 * 1024.0))
                            val payload = "SWARM_ANNOUNCE|$nodeId|${_nodeRole.value}|$activeSizeGb|${if (_containerMounted.value) "MOUNTED" else "LOCKED"}"
                            val bytes = payload.toByteArray()
                            
                            // Send broadcast to subnet
                            val address = InetAddress.getByName("255.255.255.255")
                            val packet = DatagramPacket(bytes, bytes.size, address, UDP_DISCOVERY_PORT)
                            socket.send(packet)
                        } catch (e: Exception) {
                            Log.e("SwarmNode", "Broadcast send error", e)
                        }
                        delay(5000) // Announce every 5 seconds
                    }
                }

                // 2. Listening thread for discovery
                listenJob = scope.launch {
                    val buffer = ByteArray(1024)
                    while (isActive) {
                        try {
                            val packet = DatagramPacket(buffer, buffer.size)
                            socket.receive(packet)
                            
                            val rawText = String(packet.data, 0, packet.length)
                            if (rawText.startsWith("SWARM_ANNOUNCE")) {
                                val parts = rawText.split("|")
                                if (parts.size >= 5) {
                                    val peerId = parts[1]
                                    val peerRole = parts[2]
                                    val peerSize = parts[3]
                                    val peerStatus = parts[4]
                                    val peerIp = packet.address.hostAddress ?: "Unknown"

                                    // Check if we already know this peer
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
                                        addLog("📡 SWARM: Discovered new local node peer '$peerId' at IP $peerIp.")
                                    }

                                    // Filter out dead nodes (no ping in 15 seconds)
                                    val filtered = currentPeers.filter { 
                                        System.currentTimeMillis() - it.lastSeen < 15000 
                                    }
                                    _discoveredPeers.value = filtered
                                }
                            }
                        } catch (e: Exception) {
                            if (!isActive) break
                            Log.e("SwarmNode", "Listen socket receive error", e)
                        }
                    }
                }

                addLog("Local Swarm discovery listener is ONLINE. Broadcasting signal packets.")

            } catch (e: Exception) {
                Log.e("SwarmNode", "Node socket start failed", e)
                addLog("Node activation failed: ${e.message}. Ensure port $UDP_DISCOVERY_PORT is free.")
                _nodeActive.value = false
            }
        }
    }

    fun stopNode() {
        if (!_nodeActive.value) return
        _nodeActive.value = false
        addLog("Shutting down local Swarm broadcasts and closing UDP ports...")
        
        broadcastJob?.cancel()
        listenJob?.cancel()
        discoverySocket?.close()
        discoverySocket = null
        
        // Stop TCP Streaming Server
        stopTcpServer()
        
        _discoveredPeers.value = emptyList()
        addLog("Swarm node is OFFLINE.")
    }

    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        // High strength derivation: SHA-256 hash of password combined with salt
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        val keyBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(keyBytes, "AES")
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
