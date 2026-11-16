package com.example.data

import com.example.data.crypto.CryptoHelper
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.Normalizer
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * BIP-39 compliant mnemonic generator, validator, and PBKDF2-HMAC-SHA512 seed deriver.
 * Generates and validates standard BIP-39 mnemonics compatible with hardware/software wallets.
 */
object Bip39Helper {

    private val secureRandom = SecureRandom()
    private val wordlist get() = Bip39Wordlist.words
    private val wordToIndex by lazy {
        wordlist.mapIndexed { index, word -> word to index }.toMap()
    }

    /**
     * Generates cryptographically secure random entropy (default 256 bits = 32 bytes -> 24 words).
     */
    fun generateRandomEntropy(bits: Int = 256): ByteArray {
        require(bits == 128 || bits == 256) { "Supported BIP-39 entropy sizes: 128 or 256 bits" }
        val bytes = ByteArray(bits / 8)
        secureRandom.nextBytes(bytes)
        return bytes
    }

    /**
     * Combines user-specified entropy words into a 256-bit source using PBKDF2-HMAC-SHA256
     * (210,000 iterations) rather than single-pass unsalted SHA-256.
     */
    fun generateEntropyFromWords(words: List<String>): ByteArray {
        val cleanWords = words.map { it.trim().lowercase() }
        val rawInput = cleanWords.joinToString("|")
        val salt = MessageDigest.getInstance("SHA-256")
            .digest("WEEDGURU_WORDS_ENTROPY_V2".toByteArray(Charsets.UTF_8))
        return CryptoHelper.deriveKeyPbkdf2(
            passphrase = rawInput.toCharArray(),
            salt = salt,
            iterations = CryptoHelper.PBKDF2_ITERATIONS,
            keyLengthBits = 256
        ).encoded
    }

    /**
     * Key-stretched deterministic fallback entropy derivation (PBKDF2-HMAC-SHA256, 210,000 iterations).
     * Deprecated for primary wallet creation: always prefer [generateRandomEntropy].
     */
    @Deprecated("Brain-wallet derivation from PII is weak; prefer generateRandomEntropy()")
    fun generateEntropyHash(
        birthdate: String,
        firstName: String,
        lastName: String,
        password: String
    ): ByteArray {
        val cleanBirthdate = birthdate.trim()
        val cleanFirst = firstName.trim().lowercase()
        val cleanLast = lastName.trim().lowercase()
        val rawInput = "$cleanBirthdate|$cleanFirst|$cleanLast|$password"
        val salt = MessageDigest.getInstance("SHA-256")
            .digest("WEEDGURU_RECOVERY_ENTROPY_V2".toByteArray(Charsets.UTF_8))
        return CryptoHelper.deriveKeyPbkdf2(
            passphrase = rawInput.toCharArray(),
            salt = salt,
            iterations = CryptoHelper.PBKDF2_ITERATIONS,
            keyLengthBits = 256
        ).encoded
    }

    /**
     * Canonical BIP-39 Seed Derivation:
     * Derives a 64-byte (512-bit) binary seed from a mnemonic phrase + optional passphrase
     * using PBKDF2-HMAC-SHA512 with 2048 iterations and NFKD normalization.
     */
    fun mnemonicToSeed(words: List<String>, passphrase: String = ""): ByteArray {
        require(words.size == 12 || words.size == 24) { "Mnemonic must contain 12 or 24 words" }
        val normalizedMnemonic = Normalizer.normalize(
            words.joinToString(" ") { it.trim().lowercase() },
            Normalizer.Form.NFKD
        )
        val normalizedSalt = Normalizer.normalize(
            "mnemonic$passphrase",
            Normalizer.Form.NFKD
        ).toByteArray(Charsets.UTF_8)

        val spec = PBEKeySpec(normalizedMnemonic.toCharArray(), normalizedSalt, 2048, 512)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
        return try {
            factory.generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    /**
     * Generates a BIP-39 compliant mnemonic from entropy (16 bytes -> 12 words, 32 bytes -> 24 words).
     */
    fun generateMnemonic(entropy: ByteArray): List<String> {
        require(entropy.size == 16 || entropy.size == 32) {
            "Entropy must be 16 bytes (128 bits for 12 words) or 32 bytes (256 bits for 24 words)"
        }

        val entBits = entropy.size * 8
        val csBits = entBits / 32
        val totalBits = entBits + csBits
        val wordCount = totalBits / 11

        val md = MessageDigest.getInstance("SHA-256")
        val hash = md.digest(entropy)

        val bits = BooleanArray(totalBits)
        var bitIndex = 0

        for (i in entropy.indices) {
            val byteValue = entropy[i].toInt() and 0xFF
            for (b in 7 downTo 0) {
                bits[bitIndex++] = ((byteValue shr b) and 1) == 1
            }
        }

        val hashByte = hash[0].toInt() and 0xFF
        for (b in 7 downTo (8 - csBits)) {
            bits[bitIndex++] = ((hashByte shr b) and 1) == 1
        }

        val resultWords = ArrayList<String>(wordCount)
        val wordsList = wordlist

        for (group in 0 until wordCount) {
            var index = 0
            for (bit in 0 until 11) {
                index = index shl 1
                if (bits[group * 11 + bit]) {
                    index = index or 1
                }
            }
            if (index !in wordsList.indices) {
                throw IllegalStateException("BIP-39 index $index out of bounds [0..2047]")
            }
            resultWords.add(wordsList[index])
        }

        return resultWords
    }

    /**
     * Strictly validates a BIP-39 mnemonic phrase:
     * 1. Word count must be 12 or 24 words.
     * 2. Every word must exist in the canonical BIP-39 English wordlist.
     * 3. The checksum bits in the final word must match SHA-256(entropy).
     */
    fun validateWords(words: List<String>): Boolean {
        if (words.size != 12 && words.size != 24) return false

        val cleanWords = words.map { it.trim().lowercase() }
        val indices = IntArray(cleanWords.size)

        for (i in cleanWords.indices) {
            val idx = wordToIndex[cleanWords[i]] ?: return false
            indices[i] = idx
        }

        val totalBits = cleanWords.size * 11
        val csBits = totalBits / 33
        val entBits = totalBits - csBits
        val entBytesCount = entBits / 8

        val bits = BooleanArray(totalBits)
        var bitIndex = 0
        for (idx in indices) {
            for (b in 10 downTo 0) {
                bits[bitIndex++] = ((idx shr b) and 1) == 1
            }
        }

        val entropy = ByteArray(entBytesCount)
        for (i in 0 until entBytesCount) {
            var byteVal = 0
            for (b in 0 until 8) {
                byteVal = byteVal shl 1
                if (bits[i * 8 + b]) {
                    byteVal = byteVal or 1
                }
            }
            entropy[i] = byteVal.toByte()
        }

        val md = MessageDigest.getInstance("SHA-256")
        val hash = md.digest(entropy)
        val hashByte = hash[0].toInt() and 0xFF

        for (b in 0 until csBits) {
            val expectedBit = ((hashByte shr (7 - b)) and 1) == 1
            val actualBit = bits[entBits + b]
            if (expectedBit != actualBit) return false
        }

        return true
    }
}
