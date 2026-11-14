package com.example.data.security

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.TarotRepository
import com.example.data.crypto.CryptoHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class EncryptedMessageSecurityTest {

    private lateinit var app: Application
    private lateinit var repository: TarotRepository

    @Before
    fun setup() {
        app = ApplicationProvider.getApplicationContext()
        repository = TarotRepository.getInstance(app)
    }

    @Test
    fun peerMessage_endToEndAesGcm_encryptsAndDecryptsProperly() = runTest {
        // Add a test contact
        repository.addPeerContact(
            name = "CipherGhost",
            onionAddress = "cipherghost7x2y.onion",
            publicKey = "0x04bf12a34892cfa7"
        )
        val contacts = repository.peerContacts.first()
        val contact = contacts.first { it.name == "CipherGhost" }

        val originalSecret = "TOP_SECRET: Quantum neural bridge operational."
        repository.sendEncryptedMessage(contact.id, "Netrunner", originalSecret)

        // Read back messages
        val messages = repository.getEncryptedMessages(contact.id).first()
        assertTrue("Messages list must contain at least 1 message", messages.isNotEmpty())

        val sentMessage = messages.first { !it.isIncoming }
        // Verify ciphertext is encrypted (not plain text)
        assertNotEquals(originalSecret, sentMessage.ciphertext)
        assertTrue("IV must be present", sentMessage.iv.isNotBlank())
        // Verify in-memory decrypted plaintext matches
        assertEquals(originalSecret, sentMessage.plaintext)
    }

    @Test
    fun peerMessage_tamperedCiphertext_failsAuthenticationTagCheck() = runTest {
        val masterSeed = "correct horse battery staple test seed"
        val contactId = 42
        val contactPubKey = "0x9999ffff"

        val key = CryptoHelper.deriveChannelKey(masterSeed, contactId, contactPubKey)
        val plain = "CLASSIFIED_PACKET_BURST"

        val encResult = CryptoHelper.encryptAesGcm(plain, key)

        // Corrupt 1 byte of ciphertext
        val rawCipher = android.util.Base64.decode(encResult.ciphertextBase64, android.util.Base64.NO_WRAP)
        rawCipher[2] = (rawCipher[2].toInt() xor 0xAA).toByte()
        val tamperedCipher = android.util.Base64.encodeToString(rawCipher, android.util.Base64.NO_WRAP)

        val decryptAttempt = CryptoHelper.decryptAesGcm(tamperedCipher, encResult.ivBase64, key)
        assertFalse("Decryption of tampered ciphertext must fail authentication check", decryptAttempt.isSuccess)
    }
}
