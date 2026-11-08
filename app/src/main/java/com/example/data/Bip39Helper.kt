package com.example.data

import java.security.MessageDigest

object Bip39Helper {

    /**
     * Combines user-specified 5 entropy words into a stable 256-bit (32-byte) entropy source.
     * Each word is trimmed and lowercased for consistency.
     */
    fun generateEntropyFromWords(words: List<String>): ByteArray {
        val cleanWords = words.map { it.trim().lowercase() }
        val rawInput = cleanWords.joinToString("|")
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(rawInput.toByteArray(Charsets.UTF_8))
    }

    /**
     * Combines user profile details and user password, hashing them into a stable
     * 256-bit (32-byte) entropy source.
     */
    fun generateEntropyHash(
        birthdate: String,
        firstName: String,
        lastName: String,
        password: String
    ): ByteArray {
        // Clean inputs and join them using standard delimiters
        val cleanBirthdate = birthdate.trim()
        val cleanFirst = firstName.trim().lowercase()
        val cleanLast = lastName.trim().lowercase()
        val cleanPass = password // Keep exact case for strength

        val rawInput = "$cleanBirthdate|$cleanFirst|$cleanLast|$cleanPass"
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(rawInput.toByteArray(Charsets.UTF_8))
    }

    /**
     * Generates a 24-word BIP39 compliant mnemonic using 256 bits of entropy.
     * 256 bits of entropy + 8 bits checksum = 264 bits total.
     * Divided into 24 groups of 11 bits each, which indexes the 2048 words.
     */
    fun generateMnemonic(entropy: ByteArray): List<String> {
        require(entropy.size == 32) { "Entropy must be exactly 32 bytes (256 bits) for 24 words" }

        // Compute the SHA-256 checksum of entropy
        val md = MessageDigest.getInstance("SHA-256")
        val hash = md.digest(entropy)
        val checksumByte = hash[0] // Use first 8 bits (1 byte) as checksum

        // Combine 256 bits of entropy + 8 bits of checksum into a BooleanArray of 264 bits
        val bits = BooleanArray(264)
        var bitIndex = 0

        // 1. Add entropy bits
        for (i in 0 until 32) {
            val byteValue = entropy[i].toInt() and 0xFF
            for (b in 7 downTo 0) {
                bits[bitIndex++] = ((byteValue shr b) and 1) == 1
            }
        }

        // 2. Add checksum bits (the first 8 bits of the checksum byte)
        val checksumValue = checksumByte.toInt() and 0xFF
        for (b in 7 downTo 0) {
            bits[bitIndex++] = ((checksumValue shr b) and 1) == 1
        }

        // 3. Convert 264 bits into 24 indices of 11 bits each
        val words = mutableListOf<String>()
        val wordlist = Bip39Wordlist.words

        for (group in 0 until 24) {
            var index = 0
            for (bit in 0 until 11) {
                index = index shl 1
                if (bits[group * 11 + bit]) {
                    index = index or 1
                }
            }
            // Fetch word from BIP39 word list, fallback to a safe index if out of bounds (should not happen)
            val word = wordlist.getOrElse(index) { "netrunner" }
            words.add(word)
        }

        return words
    }

    /**
     * Validates whether the given list of words contains 12 or 24 valid BIP-39 mnemonic words.
     */
    fun validateWords(words: List<String>): Boolean {
        if (words.size != 12 && words.size != 24) return false
        val wordlistSet = Bip39Wordlist.words.toSet()
        return words.all { wordlistSet.contains(it.lowercase().trim()) }
    }
}
