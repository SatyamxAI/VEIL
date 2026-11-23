package com.veil.core.crypto

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.SecureRandom

class VeilCryptoTest {

    @Test
    fun testKeyDerivationDeterministic() {
        val password = "SuperSecretMasterPassword123!".toCharArray()
        val salt = VeilCrypto.generateRandomBytes(32)

        val key1 = VeilCrypto.deriveKeyFromPassword(password, salt, iterations = 10_000)
        val key2 = VeilCrypto.deriveKeyFromPassword(password, salt, iterations = 10_000)

        assertArrayEquals("Derived keys with same password and salt must match", key1.bytes, key2.bytes)

        // Different salt must produce different key
        val differentSalt = VeilCrypto.generateRandomBytes(32)
        val key3 = VeilCrypto.deriveKeyFromPassword(password, differentSalt, iterations = 10_000)
        assertFalse("Different salts must produce distinct keys", key1.bytes.contentEquals(key3.bytes))

        key1.destroy()
        key2.destroy()
        key3.destroy()
    }

    @Test
    fun testStreamingAeadRoundtripSmallPayload() {
        val payloadKey = VeilCrypto.generateRandomBytes(32)
        val fileId = VeilCrypto.generateRandomBytes(16)
        val originalData = "VEIL: Private by design. Simple by nature.".toByteArray(Charsets.UTF_8)

        val encryptedStream = ByteArrayOutputStream()
        VeilCrypto.encryptStream(payloadKey, fileId, ByteArrayInputStream(originalData), encryptedStream)

        val encryptedBytes = encryptedStream.toByteArray()
        assertTrue("Ciphertext must be larger than plaintext due to header and AEAD tags", encryptedBytes.size > originalData.size)

        val decryptedStream = ByteArrayOutputStream()
        VeilCrypto.decryptStream(payloadKey, fileId, ByteArrayInputStream(encryptedBytes), decryptedStream)

        assertArrayEquals("Decrypted data must match original plaintext", originalData, decryptedStream.toByteArray())
    }

    @Test
    fun testStreamingAeadRoundtripMultiChunk() {
        // Multi-chunk test spanning across several 64 KiB chunks (~200 KiB)
        val payloadKey = VeilCrypto.generateRandomBytes(32)
        val fileId = VeilCrypto.generateRandomBytes(16)

        val originalData = ByteArray(180 * 1024) // 180 KiB spans across 3 chunks
        SecureRandom().nextBytes(originalData)

        val encryptedStream = ByteArrayOutputStream()
        VeilCrypto.encryptStream(payloadKey, fileId, ByteArrayInputStream(originalData), encryptedStream)

        val decryptedStream = ByteArrayOutputStream()
        VeilCrypto.decryptStream(payloadKey, fileId, ByteArrayInputStream(encryptedStream.toByteArray()), decryptedStream)

        assertArrayEquals("Multi-chunk decrypted data must match original", originalData, decryptedStream.toByteArray())
    }

    @Test
    fun testTamperDetectionFailsDecryption() {
        val payloadKey = VeilCrypto.generateRandomBytes(32)
        val fileId = VeilCrypto.generateRandomBytes(16)
        val originalData = "Sensitive client confidential photo payload".toByteArray(Charsets.UTF_8)

        val encryptedStream = ByteArrayOutputStream()
        VeilCrypto.encryptStream(payloadKey, fileId, ByteArrayInputStream(originalData), encryptedStream)

        val ciphertext = encryptedStream.toByteArray()
        // Tamper with a byte in the payload
        ciphertext[ciphertext.size - 5] = (ciphertext[ciphertext.size - 5].toInt() xor 0xFF).toByte()

        try {
            val decryptedStream = ByteArrayOutputStream()
            VeilCrypto.decryptStream(payloadKey, fileId, ByteArrayInputStream(ciphertext), decryptedStream)
            fail("Decryption must throw an authentication exception when ciphertext is tampered!")
        } catch (e: Exception) {
            // Expected cryptographic failure
            assertTrue(e.message?.contains("tampered", ignoreCase = true) == true || e is javax.crypto.AEADBadTagException)
        }
    }

    @Test
    fun testBlindDeduplicationToken() {
        val dedupKey = VeilCrypto.generateRandomBytes(32)
        val data = "Identical photo contents".toByteArray(Charsets.UTF_8)

        val token1 = VeilCrypto.computeBlindDedupToken(dedupKey, ByteArrayInputStream(data))
        val token2 = VeilCrypto.computeBlindDedupToken(dedupKey, ByteArrayInputStream(data))

        assertEquals("Same data and key must produce identical dedup token", token1, token2)

        val differentData = "Different photo contents".toByteArray(Charsets.UTF_8)
        val token3 = VeilCrypto.computeBlindDedupToken(dedupKey, ByteArrayInputStream(differentData))

        assertNotEquals("Different data must produce different token", token1, token3)
    }

    @Test
    fun testBip39RecoveryGeneration() {
        val phrase = Bip39Recovery.generateRecoveryPhrase()
        assertEquals("BIP-39 recovery phrase must contain 24 words", 24, phrase.size)
        phrase.forEach { word ->
            assertTrue("Word must not be blank", word.isNotBlank())
        }
    }
}
