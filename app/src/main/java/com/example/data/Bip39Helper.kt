package com.example.data

import java.security.MessageDigest

/**
 * BIP-39 compliant mnemonic generator and validator.
 * Generates and validates standard BIP-39 mnemonics compatible with industry hardware
 * and software cryptocurrency wallets.
 */
object Bip39Helper {

    private val wordlist get() = Bip39Wordlist.words
    private val wordToIndex by lazy {
        wordlist.mapIndexed { index, word -> word to index }.toMap()
    }

    /**
     * Combines user-specified entropy words into a stable 256-bit (32-byte) entropy source.
     * Each word is trimmed and lowercased for consistency.
     */
    fun generateEntropyFromWords(words: List<String>): ByteArray {
        val cleanWords = words.map { it.trim().lowercase() }
        val rawInput = cleanWords.joinToString("|")
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(rawInput.toByteArray(Charsets.UTF_8))
    }

    /**
     * Hashes user details into a stable 256-bit (32-byte) entropy source.
     */
    fun generateEntropyHash(
        birthdate: String,
        firstName: String,
        lastName: String,
        password: String
    ): ByteArray {
        val cleanBirthdate = birthdate.trim()
        val cleanFirst = firstName.trim().lowercase()
        val cleanLast = lastName.trim().lowercase()
        val cleanPass = password

        val rawInput = "$cleanBirthdate|$cleanFirst|$cleanLast|$cleanPass"
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(rawInput.toByteArray(Charsets.UTF_8))
    }

    /**
     * Generates a BIP-39 compliant mnemonic from entropy.
     * Supported entropy lengths:
     * - 16 bytes (128 bits) -> 12 words (128 bits entropy + 4 bits checksum = 132 bits)
     * - 32 bytes (256 bits) -> 24 words (256 bits entropy + 8 bits checksum = 264 bits)
     */
    fun generateMnemonic(entropy: ByteArray): List<String> {
        require(entropy.size == 16 || entropy.size == 32) {
            "Entropy must be 16 bytes (128 bits for 12 words) or 32 bytes (256 bits for 24 words)"
        }

        val entBits = entropy.size * 8
        val csBits = entBits / 32
        val totalBits = entBits + csBits
        val wordCount = totalBits / 11

        // Checksum is the first csBits of SHA-256(entropy)
        val md = MessageDigest.getInstance("SHA-256")
        val hash = md.digest(entropy)

        // Construct bit array
        val bits = BooleanArray(totalBits)
        var bitIndex = 0

        // 1. Add entropy bits
        for (i in entropy.indices) {
            val byteValue = entropy[i].toInt() and 0xFF
            for (b in 7 downTo 0) {
                bits[bitIndex++] = ((byteValue shr b) and 1) == 1
            }
        }

        // 2. Add checksum bits from the first hash byte(s)
        val hashByte = hash[0].toInt() and 0xFF
        for (b in 7 downTo (8 - csBits)) {
            bits[bitIndex++] = ((hashByte shr b) and 1) == 1
        }

        // 3. Convert bit groups of 11 into wordlist indices
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
        val csBits = totalBits / 33 // 4 bits for 12 words, 8 bits for 24 words
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
