package com.example.data.crypto

import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Hardware-backed AndroidKeyStore AES-256-GCM vault + persistent PIN rate-limiting store.
 * On real Android devices (API 23+), master keys live inside AndroidKeyStore (non-exportable).
 * In local JVM/Robolectric test environments where AndroidKeyStore is absent, falls back cleanly
 * to a private per-app AES-256 key in SharedPreferences so tests remain deterministic.
 */
object SecureVaultHelper {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val MASTER_KEY_ALIAS = "weedguru_vault_master_v1"
    private const val ENC_PREFIX = "ENC_V1:"
    private const val VAULT_PREFS = "weedguru_secure_vault_prefs"
    private const val FALLBACK_KEY_PREF = "jvm_fallback_aes_key"
    private const val DEVICE_EC_PRIV_PREF = "device_ec_private_enc"
    private const val DEVICE_EC_PUB_PREF = "device_ec_public_b64"
    private const val SWARM_PSK_PREF = "swarm_cluster_psk_enc"
    private const val PIN_STATE_PREF = "pin_lockout_state_enc"
    private const val MAX_FREE_ATTEMPTS = 5

    @Synchronized
    private fun getOrCreateMasterKey(context: Context): SecretKey {
        return try {
            val ks = KeyStore.getInstance(ANDROID_KEYSTORE)
            ks.load(null)
            val existing = ks.getKey(MASTER_KEY_ALIAS, null) as? SecretKey
            if (existing != null) {
                existing
            } else {
                val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
                val builder = KeyGenParameterSpec.Builder(
                    MASTER_KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    // Do not require unlocked StrongBox strictly so all devices work cleanly
                    builder.setIsStrongBoxBacked(false)
                }
                keyGen.init(builder.build())
                keyGen.generateKey()
            }
        } catch (_: Exception) {
            // Pure JVM / Robolectric fallback when AndroidKeyStore provider is unavailable
            val prefs = context.applicationContext.getSharedPreferences(VAULT_PREFS, Context.MODE_PRIVATE)
            val existingB64 = prefs.getString(FALLBACK_KEY_PREF, null)
            val rawBytes = if (existingB64 != null) {
                Base64.decode(existingB64, Base64.NO_WRAP)
            } else {
                ByteArray(32).also {
                    SecureRandom().nextBytes(it)
                    prefs.edit().putString(
                        FALLBACK_KEY_PREF,
                        Base64.encodeToString(it, Base64.NO_WRAP)
                    ).apply()
                }
            }
            SecretKeySpec(rawBytes, "AES")
        }
    }

    fun isEncrypted(value: String?): Boolean = value?.startsWith(ENC_PREFIX) == true

    fun encryptSecret(context: Context, plaintext: String): String {
        if (plaintext.isBlank()) return ""
        if (isEncrypted(plaintext)) return plaintext
        val key = getOrCreateMasterKey(context)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        val ivB64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        val ctB64 = Base64.encodeToString(ciphertext, Base64.NO_WRAP)
        return "$ENC_PREFIX$ivB64:$ctB64"
    }

    fun decryptSecret(context: Context, stored: String?): String {
        if (stored.isNullOrBlank()) return ""
        if (!isEncrypted(stored)) return stored // Legacy plaintext returned cleanly for one-time migration
        return try {
            val payload = stored.removePrefix(ENC_PREFIX)
            val parts = payload.split(":", limit = 2)
            if (parts.size != 2) return ""
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val ct = Base64.decode(parts[1], Base64.NO_WRAP)
            val key = getOrCreateMasterKey(context)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
            String(cipher.doFinal(ct), Charsets.UTF_8)
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * Returns or generates this device's persistent EC P-256 identity keypair (private key sealed
     * with AndroidKeyStore; used for ECDSA signatures & ECDH channel key derivation).
     */
    @Synchronized
    fun getOrCreateDeviceEcKeyPair(context: Context): CryptoHelper.EcdhKeyPair {
        val prefs = context.applicationContext.getSharedPreferences(VAULT_PREFS, Context.MODE_PRIVATE)
        val storedPrivEnc = prefs.getString(DEVICE_EC_PRIV_PREF, null)
        val storedPub = prefs.getString(DEVICE_EC_PUB_PREF, null)
        if (!storedPrivEnc.isNullOrBlank() && !storedPub.isNullOrBlank()) {
            val privDec = decryptSecret(context, storedPrivEnc)
            if (privDec.isNotBlank()) {
                return CryptoHelper.EcdhKeyPair(publicKeyBase64 = storedPub, privateKeyBase64 = privDec)
            }
        }
        val generated = CryptoHelper.generateEcKeyPair()
        val encPriv = encryptSecret(context, generated.privateKeyBase64)
        prefs.edit()
            .putString(DEVICE_EC_PRIV_PREF, encPriv)
            .putString(DEVICE_EC_PUB_PREF, generated.publicKeyBase64)
            .apply()
        return generated
    }

    /**
     * Returns a local high-entropy vault secret for local message/channel storage when no mnemonic
     * is stored or unlocked in memory.
     */
    @Synchronized
    fun getOrCreateSwarmClusterKey(context: Context): ByteArray {
        val prefs = context.applicationContext.getSharedPreferences(VAULT_PREFS, Context.MODE_PRIVATE)
        val storedEnc = prefs.getString(SWARM_PSK_PREF, null)
        if (!storedEnc.isNullOrBlank()) {
            val decoded = decryptSecret(context, storedEnc)
            if (decoded.isNotBlank()) {
                return runCatching { Base64.decode(decoded, Base64.NO_WRAP) }.getOrNull() ?: generateNewPsk(context, prefs)
            }
        }
        return generateNewPsk(context, prefs)
    }

    private fun generateNewPsk(context: Context, prefs: android.content.SharedPreferences): ByteArray {
        val raw = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val enc = encryptSecret(context, Base64.encodeToString(raw, Base64.NO_WRAP))
        prefs.edit().putString(SWARM_PSK_PREF, enc).apply()
        return raw
    }

    fun computeHmacSha256(key: ByteArray, data: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data)
    }

    // --- PERSISTENT MONOTONIC PIN RATE-LIMITER (H4) ---

    data class PinLockoutState(
        val failedAttempts: Int = 0,
        val lockoutRemainingMs: Long = 0L
    )

    @Synchronized
    fun getPinLockoutState(context: Context): PinLockoutState {
        val prefs = context.applicationContext.getSharedPreferences(VAULT_PREFS, Context.MODE_PRIVATE)
        val enc = prefs.getString(PIN_STATE_PREF, null) ?: return PinLockoutState()
        val raw = decryptSecret(context, enc)
        if (raw.isBlank()) return PinLockoutState()
        val parts = raw.split("|")
        if (parts.size != 3) return PinLockoutState()
        val attempts = parts[0].toIntOrNull() ?: 0
        val deadlineElapsed = parts[1].toLongOrNull() ?: 0L
        val lastRecordedElapsed = parts[2].toLongOrNull() ?: 0L
        val nowElapsed = SystemClock.elapsedRealtime()

        // If device rebooted (elapsedRealtime < lastRecordedElapsed), preserve remaining penalty window safely
        val effectiveRemaining = if (deadlineElapsed <= 0L) {
            0L
        } else if (nowElapsed < lastRecordedElapsed) {
            // Reboot detected: keep remaining portion from last recorded elapsed time
            (deadlineElapsed - lastRecordedElapsed).coerceAtLeast(0L)
        } else {
            (deadlineElapsed - nowElapsed).coerceAtLeast(0L)
        }

        if (nowElapsed < lastRecordedElapsed && effectiveRemaining > 0L) {
            // Re-anchor deadline post-reboot
            persistPinState(context, prefs, attempts, nowElapsed + effectiveRemaining, nowElapsed)
        }

        return PinLockoutState(failedAttempts = attempts, lockoutRemainingMs = effectiveRemaining)
    }

    @Synchronized
    fun recordFailedPinAttempt(context: Context): PinLockoutState {
        val prefs = context.applicationContext.getSharedPreferences(VAULT_PREFS, Context.MODE_PRIVATE)
        val current = getPinLockoutState(context)
        val newAttempts = current.failedAttempts + 1
        val nowElapsed = SystemClock.elapsedRealtime()
        val penaltyMs = if (newAttempts >= MAX_FREE_ATTEMPTS) {
            val exponent = (newAttempts - MAX_FREE_ATTEMPTS).coerceAtMost(4)
            30_000L shl exponent // 30s, 60s, 120s, 240s, 480s exponential backoff
        } else {
            0L
        }
        val deadline = if (penaltyMs > 0L) nowElapsed + penaltyMs else 0L
        persistPinState(context, prefs, newAttempts, deadline, nowElapsed)
        return PinLockoutState(failedAttempts = newAttempts, lockoutRemainingMs = penaltyMs)
    }

    @Synchronized
    fun resetPinLockout(context: Context) {
        val prefs = context.applicationContext.getSharedPreferences(VAULT_PREFS, Context.MODE_PRIVATE)
        prefs.edit().remove(PIN_STATE_PREF).apply()
    }

    private fun persistPinState(
        context: Context,
        prefs: android.content.SharedPreferences,
        attempts: Int,
        deadlineElapsed: Long,
        nowElapsed: Long
    ) {
        val payload = "$attempts|$deadlineElapsed|$nowElapsed"
        prefs.edit().putString(PIN_STATE_PREF, encryptSecret(context, payload)).apply()
    }
}
