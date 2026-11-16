package com.example.data.crypto

import android.util.Base64
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.Mac
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Cryptographic operations:
 * - PBKDF2-HMAC-SHA256 key derivation & salted PIN hashing (210,000 iterations, OWASP compliant)
 * - AES-256-GCM authenticated encryption (12-byte IV, 128-bit authentication tag)
 * - ECDH (P-256 / secp256r1) + HKDF-SHA256 cross-device key agreement
 * - ECDSA (SHA256withECDSA) asymmetric signatures & verification
 * - Constant-time comparisons to prevent timing side-channel attacks
 */
object CryptoHelper {

    const val PBKDF2_ITERATIONS = 210_000
    const val KEY_LENGTH_BITS = 256
    const val GCM_IV_LENGTH_BYTES = 12
    const val GCM_TAG_LENGTH_BITS = 128
    private const val PIN_HASH_PREFIX = "PBKDF2_V1:"

    private val secureRandom = SecureRandom()

    data class EncryptedResult(
        val ciphertextBase64: String,
        val ivBase64: String
    )

    data class EcdhKeyPair(
        val publicKeyBase64: String,
        val privateKeyBase64: String
    )

    /**
     * Derives a 256-bit AES key from a passphrase and salt using PBKDF2-HMAC-SHA256.
     */
    fun deriveKeyPbkdf2(
        passphrase: CharArray,
        salt: ByteArray,
        iterations: Int = PBKDF2_ITERATIONS,
        keyLengthBits: Int = KEY_LENGTH_BITS
    ): SecretKeySpec {
        require(salt.size >= 16) { "Salt must be at least 16 bytes" }
        require(iterations >= 1_000) { "Iterations must be at least 1,000" }

        val spec = PBEKeySpec(passphrase, salt, iterations, keyLengthBits)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        return try {
            SecretKeySpec(keyBytes, "AES")
        } finally {
            keyBytes.fill(0)
            spec.clearPassword()
        }
    }

    /**
     * Generates a salted PBKDF2-HMAC-SHA256 hash string for storing PINs safely without plaintext.
     * Format: PBKDF2_V1:<iterations>:<saltBase64>:<hashBase64>
     */
    fun hashPin(pin: String, iterations: Int = PBKDF2_ITERATIONS): String {
        require(pin.isNotBlank()) { "PIN must not be blank" }
        val salt = generateSalt(16)
        val spec = PBEKeySpec(pin.toCharArray(), salt, iterations, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val derived = factory.generateSecret(spec).encoded
        return try {
            val saltB64 = Base64.encodeToString(salt, Base64.NO_WRAP)
            val hashB64 = Base64.encodeToString(derived, Base64.NO_WRAP)
            "$PIN_HASH_PREFIX$iterations:$saltB64:$hashB64"
        } finally {
            derived.fill(0)
            spec.clearPassword()
        }
    }

    fun isPinHashed(stored: String?): Boolean {
        return stored != null && stored.startsWith(PIN_HASH_PREFIX)
    }

    /**
     * Verifies a candidate PIN against either a PBKDF2_V1 hash or (for seamless migration only)
     * a legacy unhashed PIN using constant-time comparison.
     */
    fun verifyPin(candidatePin: String, storedValue: String?): Boolean {
        if (storedValue.isNullOrBlank() || candidatePin.isBlank()) return false
        if (!storedValue.startsWith(PIN_HASH_PREFIX)) {
            return constantTimeEquals(candidatePin, storedValue)
        }
        val payload = storedValue.removePrefix(PIN_HASH_PREFIX)
        val parts = payload.split(":")
        if (parts.size != 3) return false
        val iterations = parts[0].toIntOrNull() ?: return false
        if (iterations < 1_000) return false
        val salt = runCatching { Base64.decode(parts[1], Base64.NO_WRAP) }.getOrNull() ?: return false
        val expectedHash = runCatching { Base64.decode(parts[2], Base64.NO_WRAP) }.getOrNull() ?: return false
        if (salt.size < 16 || expectedHash.isEmpty()) return false

        val spec = PBEKeySpec(candidatePin.toCharArray(), salt, iterations, expectedHash.size * 8)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val actualHash = factory.generateSecret(spec).encoded
        return try {
            constantTimeEquals(actualHash, expectedHash)
        } finally {
            actualHash.fill(0)
            spec.clearPassword()
        }
    }

    fun generateSalt(lengthBytes: Int = 16): ByteArray {
        val salt = ByteArray(lengthBytes)
        secureRandom.nextBytes(salt)
        return salt
    }

    fun generateGcmIv(): ByteArray {
        val iv = ByteArray(GCM_IV_LENGTH_BYTES)
        secureRandom.nextBytes(iv)
        return iv
    }

    fun encryptAesGcm(plaintext: String, secretKey: SecretKeySpec): EncryptedResult {
        val iv = generateGcmIv()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

        val plaintextBytes = plaintext.toByteArray(Charsets.UTF_8)
        val ciphertextBytes = cipher.doFinal(plaintextBytes)

        return EncryptedResult(
            ciphertextBase64 = Base64.encodeToString(ciphertextBytes, Base64.NO_WRAP),
            ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        )
    }

    fun decryptAesGcm(
        ciphertextBase64: String,
        ivBase64: String,
        secretKey: SecretKeySpec
    ): Result<String> {
        return try {
            val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
            val ciphertextBytes = Base64.decode(ciphertextBase64, Base64.NO_WRAP)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

            val decryptedBytes = cipher.doFinal(ciphertextBytes)
            Result.success(String(decryptedBytes, Charsets.UTF_8))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generates an EC P-256 (secp256r1) keypair suitable for ECDH key agreement and ECDSA signatures.
     */
    fun generateEcKeyPair(): EcdhKeyPair {
        val kpg = KeyPairGenerator.getInstance("EC")
        kpg.initialize(ECGenParameterSpec("secp256r1"), secureRandom)
        val pair = kpg.generateKeyPair()
        val pubB64 = Base64.encodeToString(pair.public.encoded, Base64.NO_WRAP)
        val privB64 = Base64.encodeToString(pair.private.encoded, Base64.NO_WRAP)
        return EcdhKeyPair(publicKeyBase64 = pubB64, privateKeyBase64 = privB64)
    }

    /**
     * Performs cross-device ECDH (P-256) key agreement + HKDF-SHA256 to derive a shared 256-bit
     * AES channel key. Both parties arrive at the exact same key from (privA, pubB) and (privB, pubA).
     */
    fun deriveSharedChannelKey(
        localPrivateKeyBase64: String,
        peerPublicKeyBase64: String,
        contextInfo: String = "WEEDGURU_CHANNEL_E2E_V1"
    ): SecretKeySpec {
        val keyFactory = KeyFactory.getInstance("EC")
        val privBytes = Base64.decode(localPrivateKeyBase64, Base64.NO_WRAP)
        val pubBytes = Base64.decode(peerPublicKeyBase64, Base64.NO_WRAP)
        val privateKey = keyFactory.generatePrivate(PKCS8EncodedKeySpec(privBytes))
        val peerPublicKey = keyFactory.generatePublic(X509EncodedKeySpec(pubBytes))

        val agreement = KeyAgreement.getInstance("ECDH")
        agreement.init(privateKey)
        agreement.doPhase(peerPublicKey, true)
        val sharedSecret = agreement.generateSecret()

        return try {
            val salt = MessageDigest.getInstance("SHA-256")
                .digest("WEEDGURU_ECDH_SALT_V1".toByteArray(Charsets.UTF_8))
            val aesKeyBytes = hkdfSha256(
                ikm = sharedSecret,
                salt = salt,
                info = contextInfo.toByteArray(Charsets.UTF_8),
                outputLength = 32
            )
            try {
                SecretKeySpec(aesKeyBytes, "AES")
            } finally {
                aesKeyBytes.fill(0)
            }
        } finally {
            sharedSecret.fill(0)
        }
    }

    /**
     * HKDF-SHA256 (RFC 5869) extract + expand.
     */
    fun hkdfSha256(
        ikm: ByteArray,
        salt: ByteArray,
        info: ByteArray,
        outputLength: Int = 32
    ): ByteArray {
        require(outputLength in 1..(255 * 32)) { "Invalid HKDF output length" }
        val effectiveSalt = if (salt.isEmpty()) ByteArray(32) else salt
        val prkMac = Mac.getInstance("HmacSHA256")
        prkMac.init(SecretKeySpec(effectiveSalt, "HmacSHA256"))
        val prk = prkMac.doFinal(ikm)

        val result = ByteArray(outputLength)
        var t = ByteArray(0)
        var offset = 0
        var counter = 1
        while (offset < outputLength) {
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(SecretKeySpec(prk, "HmacSHA256"))
            mac.update(t)
            mac.update(info)
            mac.update(counter.toByte())
            t = mac.doFinal()
            val copyLen = minOf(t.size, outputLength - offset)
            System.arraycopy(t, 0, result, offset, copyLen)
            offset += copyLen
            counter++
        }
        prk.fill(0)
        return result
    }

    /**
     * Signs payload bytes with SHA256withECDSA using a PKCS#8 Base64-encoded EC private key.
     */
    fun signEcdsa(payload: ByteArray, privateKeyBase64: String): String {
        val kf = KeyFactory.getInstance("EC")
        val privBytes = Base64.decode(privateKeyBase64, Base64.NO_WRAP)
        val privateKey = kf.generatePrivate(PKCS8EncodedKeySpec(privBytes))
        val sig = Signature.getInstance("SHA256withECDSA")
        sig.initSign(privateKey, secureRandom)
        sig.update(payload)
        return Base64.encodeToString(sig.sign(), Base64.NO_WRAP)
    }

    /**
     * Verifies a SHA256withECDSA signature against a X.509 Base64-encoded EC public key.
     */
    fun verifyEcdsa(payload: ByteArray, signatureBase64: String, publicKeyBase64: String): Boolean {
        return try {
            val kf = KeyFactory.getInstance("EC")
            val pubBytes = Base64.decode(publicKeyBase64, Base64.NO_WRAP)
            val publicKey = kf.generatePublic(X509EncodedKeySpec(pubBytes))
            val sigBytes = Base64.decode(signatureBase64, Base64.NO_WRAP)
            val sig = Signature.getInstance("SHA256withECDSA")
            sig.initVerify(publicKey)
            sig.update(payload)
            sig.verify(sigBytes)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Derives a local storage channel key from high-entropy secret material using PBKDF2-HMAC-SHA256.
     * Rejects blank secret material rather than falling back to a predictable constant.
     */
    fun deriveChannelKey(
        masterSecret: String,
        contactId: Int,
        contactPublicKey: String
    ): SecretKeySpec {
        require(masterSecret.isNotBlank()) { "Master secret must not be blank for channel key derivation" }
        val saltSource = "CYBER_CHANNEL_SALT_${contactId}_$contactPublicKey"
        val md = MessageDigest.getInstance("SHA-256")
        val salt = md.digest(saltSource.toByteArray(Charsets.UTF_8)).copyOf(16)

        return deriveKeyPbkdf2(
            passphrase = masterSecret.toCharArray(),
            salt = salt,
            iterations = 50_000
        )
    }

    fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        return MessageDigest.isEqual(a, b)
    }

    fun constantTimeEquals(a: String, b: String): Boolean {
        return MessageDigest.isEqual(a.toByteArray(Charsets.UTF_8), b.toByteArray(Charsets.UTF_8))
    }
}
