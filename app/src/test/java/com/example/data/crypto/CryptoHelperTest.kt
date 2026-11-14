package com.example.data.crypto

import android.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class CryptoHelperTest {

    @Test
    fun pbkdf2_derivesDeterministicKeyForSameInputs() {
        val passphrase = "quantum_neural_passphrase_2026".toCharArray()
        val salt = CryptoHelper.generateSalt(16)

        val key1 = CryptoHelper.deriveKeyPbkdf2(passphrase, salt, iterations = 10_000)
        val key2 = CryptoHelper.deriveKeyPbkdf2(passphrase, salt, iterations = 10_000)

        assertEquals("AES", key1.algorithm)
        assertEquals(32, key1.encoded.size) // 256 bits
        assertTrue(CryptoHelper.constantTimeEquals(key1.encoded, key2.encoded))

        // Different salt must produce completely different key
        val differentSalt = CryptoHelper.generateSalt(16)
        val key3 = CryptoHelper.deriveKeyPbkdf2(passphrase, differentSalt, iterations = 10_000)
        assertFalse(CryptoHelper.constantTimeEquals(key1.encoded, key3.encoded))
    }

    @Test
    fun aesGcm_roundTripEncryptionDecryption() {
        val passphrase = "matrix_operator_secret".toCharArray()
        val salt = CryptoHelper.generateSalt(16)
        val secretKey = CryptoHelper.deriveKeyPbkdf2(passphrase, salt, iterations = 10_000)

        val originalMessage = "⚡ CYBER_TAROT: Secret packet 0xDEADBEEF transmission through Tor node."

        // Encrypt twice - each must have a unique IV and different ciphertext
        val encrypted1 = CryptoHelper.encryptAesGcm(originalMessage, secretKey)
        val encrypted2 = CryptoHelper.encryptAesGcm(originalMessage, secretKey)

        assertNotEquals("Each encryption must have a unique IV", encrypted1.ivBase64, encrypted2.ivBase64)
        assertNotEquals("Each encryption must produce unique ciphertext", encrypted1.ciphertextBase64, encrypted2.ciphertextBase64)

        // Decrypt both
        val decrypted1 = CryptoHelper.decryptAesGcm(encrypted1.ciphertextBase64, encrypted1.ivBase64, secretKey)
        val decrypted2 = CryptoHelper.decryptAesGcm(encrypted2.ciphertextBase64, encrypted2.ivBase64, secretKey)

        assertTrue("Decryption 1 must succeed", decrypted1.isSuccess)
        assertTrue("Decryption 2 must succeed", decrypted2.isSuccess)
        assertEquals(originalMessage, decrypted1.getOrNull())
        assertEquals(originalMessage, decrypted2.getOrNull())
    }

    @Test
    fun aesGcm_tamperingDetection_failsOnModifiedCiphertext() {
        val passphrase = "secure_netrunner_channel".toCharArray()
        val salt = CryptoHelper.generateSalt(16)
        val secretKey = CryptoHelper.deriveKeyPbkdf2(passphrase, salt, iterations = 10_000)

        val message = "CLASSIFIED_CORE_DIRECTIVE"
        val encrypted = CryptoHelper.encryptAesGcm(message, secretKey)

        // Tamper with the ciphertext (flip a byte)
        val rawCiphertext = Base64.decode(encrypted.ciphertextBase64, Base64.NO_WRAP)
        rawCiphertext[0] = (rawCiphertext[0].toInt() xor 0xFF).toByte()
        val tamperedCiphertextBase64 = Base64.encodeToString(rawCiphertext, Base64.NO_WRAP)

        val tamperedResult = CryptoHelper.decryptAesGcm(tamperedCiphertextBase64, encrypted.ivBase64, secretKey)
        assertFalse("GCM must reject tampered ciphertext with authentication tag failure", tamperedResult.isSuccess)

        // Decrypting with wrong key must also fail
        val wrongKey = CryptoHelper.deriveKeyPbkdf2("wrong_passphrase".toCharArray(), salt, iterations = 10_000)
        val wrongKeyResult = CryptoHelper.decryptAesGcm(encrypted.ciphertextBase64, encrypted.ivBase64, wrongKey)
        assertFalse("GCM must reject decryption with wrong key", wrongKeyResult.isSuccess)
    }

    @Test
    fun constantTimeEquals_verifiesEquivalenceCorrectly() {
        assertTrue(CryptoHelper.constantTimeEquals("12345678", "12345678"))
        assertFalse(CryptoHelper.constantTimeEquals("12345678", "12345679"))
        assertFalse(CryptoHelper.constantTimeEquals("12345678", "1234567"))
        assertFalse(CryptoHelper.constantTimeEquals("22222222", "11111111"))
    }
}
