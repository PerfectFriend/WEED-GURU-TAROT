package com.example.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.MessageDigest

class Bip39Test {

    @Test
    fun bip39_wordlist_hasExactly2048SortedWordsWithCanonicalHash() {
        val words = Bip39Wordlist.words
        assertEquals("BIP-39 wordlist must have exactly 2048 words", 2048, words.size)

        val sortedWords = words.sorted()
        assertEquals("BIP-39 wordlist must be sorted alphabetically", sortedWords, words)

        // Verify SHA-256 hash of the canonical wordlist joined by newline
        val joined = words.joinToString("\n")
        val md = MessageDigest.getInstance("SHA-256")
        val hash = md.digest(joined.toByteArray(Charsets.UTF_8))
        val hexHash = hash.joinToString("") { "%02x".format(it) }

        assertEquals(
            "187db04a869dd9bc7be80d21a86497d692c0db6abd3aa8cb6be5d618ff757fae",
            hexHash
        )
        assertEquals("First word must be abandon", "abandon", words.first())
        assertEquals("Last word must be zoo", "zoo", words.last())
        assertTrue("Must contain yellow", words.contains("yellow"))
        assertTrue("Must contain zero", words.contains("zero"))
        assertTrue("Must contain zone", words.contains("zone"))
    }

    @Test
    fun bip39_standardTestVectors_12and24Words() {
        // Test Vector 1: 128-bit all zeros
        val entropy128Zero = ByteArray(16) { 0 }
        val mnemonic12 = Bip39Helper.generateMnemonic(entropy128Zero)
        assertEquals(12, mnemonic12.size)
        assertEquals(
            "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about",
            mnemonic12.joinToString(" ")
        )
        assertTrue(Bip39Helper.validateWords(mnemonic12))

        // Test Vector 2: 256-bit all zeros
        val entropy256Zero = ByteArray(32) { 0 }
        val mnemonic24 = Bip39Helper.generateMnemonic(entropy256Zero)
        assertEquals(24, mnemonic24.size)
        assertEquals(
            "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon art",
            mnemonic24.joinToString(" ")
        )
        assertTrue(Bip39Helper.validateWords(mnemonic24))

        // Test Vector 3: 128-bit all 0x7F
        val entropy1287f = ByteArray(16) { 0x7F.toByte() }
        val mnemonic127f = Bip39Helper.generateMnemonic(entropy1287f)
        assertEquals(
            "legal winner thank year wave sausage worth useful legal winner thank yellow",
            mnemonic127f.joinToString(" ")
        )
        assertTrue(Bip39Helper.validateWords(mnemonic127f))
    }

    @Test
    fun bip39_checksumValidation_catchesCorruptedWords() {
        val validMnemonic = "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about".split(" ")
        assertTrue("Valid mnemonic must pass validation", Bip39Helper.validateWords(validMnemonic))

        // Corrupting the last word (wrong checksum)
        val corruptedChecksum = validMnemonic.toMutableList().apply { this[11] = "abandon" }
        assertFalse("Mnemonic with invalid checksum must fail validation", Bip39Helper.validateWords(corruptedChecksum))

        // Corrupting an internal word
        val corruptedWord = validMnemonic.toMutableList().apply { this[0] = "ability" }
        assertFalse("Mnemonic with altered word must fail validation", Bip39Helper.validateWords(corruptedWord))

        // Non-existent word
        val nonExistentWord = validMnemonic.toMutableList().apply { this[0] = "netrunner" }
        assertFalse("Mnemonic with word outside BIP-39 must fail", Bip39Helper.validateWords(nonExistentWord))

        // Wrong word count
        assertFalse("11 words must fail", Bip39Helper.validateWords(validMnemonic.take(11)))
        assertFalse("13 words must fail", Bip39Helper.validateWords(validMnemonic + "about"))
    }
}
