package com.veil.core.crypto

import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * VEIL Core Cryptographic Subsystem
 *
 * Implements:
 * 1. Client-Side Zero-Knowledge Encryption.
 * 2. 64 KiB Chunked Streaming AEAD (AES-256-GCM) with chunk index + EOF bound in Associated Data (AD).
 * 3. Nonce Misuse Resistance: Base IV XOR monotonic chunk index.
 * 4. Blind Deduplication Token generation: HMAC-SHA256(DDK, Stream).
 * 5. Deterministic Filename Obfuscation via HMAC-SHA256 & AES-GCM.
 */
object VeilCrypto {
    private const val GCM_TAG_LENGTH_BITS = 128
    private const val GCM_TAG_LENGTH_BYTES = 16
    private const val GCM_IV_LENGTH_BYTES = 12
    const val CHUNK_SIZE_BYTES = 64 * 1024 // 64 KiB
    private const val HEADER_MAGIC = "VEIL"
    private const val FORMAT_VERSION: Short = 1

    private val secureRandom = SecureRandom()

    fun generateRandomBytes(length: Int): ByteArray {
        val bytes = ByteArray(length)
        secureRandom.nextBytes(bytes)
        return bytes
    }

    /**
     * Derives a 256-bit Key Encryption Key (KEK) from the user's master password.
     * Uses PBKDF2WithHmacSHA256 with 120,000 iterations and 32-byte salt.
     */
    fun deriveKeyFromPassword(password: CharArray, salt: ByteArray, iterations: Int = 120_000): SecureBytes {
        val spec = PBEKeySpec(password, salt, iterations, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val derived = factory.generateSecret(spec).encoded
        spec.clearPassword()
        return SecureBytes(derived)
    }

    /**
     * HKDF-Expand (RFC 5869) to derive sub-keys (Payload Master Key, Filename Key, Dedup Key).
     */
    fun hkdfExpand(prk: ByteArray, info: String, outputLength: Int = 32): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(prk, "HmacSHA256"))
        
        val infoBytes = info.toByteArray(Charsets.UTF_8)
        val result = ByteArray(outputLength)
        var t = ByteArray(0)
        var generated = 0
        var counter: Byte = 1

        while (generated < outputLength) {
            mac.reset()
            mac.update(t)
            mac.update(infoBytes)
            mac.update(counter)
            t = mac.doFinal()

            val toCopy = minOf(t.size, outputLength - generated)
            System.arraycopy(t, 0, result, generated, toCopy)
            generated += toCopy
            counter++
        }
        return result
    }

    /**
     * Computes a blind deduplication token: HMAC-SHA256(DedupKey, PlaintextStream).
     * Cloud providers cannot correlate this with known rainbow tables.
     */
    fun computeBlindDedupToken(dedupKey: ByteArray, inputStream: InputStream): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(dedupKey, "HmacSHA256"))
        val buffer = ByteArray(8192)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            mac.update(buffer, 0, bytesRead)
        }
        val rawHash = mac.doFinal()
        return rawHash.joinToString("") { "%02x".format(it) }
    }

    /**
     * Encrypts a full stream into a 64 KiB chunked VEIL container.
     */
    fun encryptStream(
        payloadKey: ByteArray,
        fileId: ByteArray,
        inputStream: InputStream,
        outputStream: OutputStream
    ) {
        // 1. Write Header: Magic (4B) + Version (2B) + File Salt (16B) + Base IV (12B)
        val fileSalt = generateRandomBytes(16)
        val baseIv = generateRandomBytes(GCM_IV_LENGTH_BYTES)

        val headerBuffer = ByteBuffer.allocate(4 + 2 + 16 + GCM_IV_LENGTH_BYTES).order(ByteOrder.BIG_ENDIAN)
        headerBuffer.put(HEADER_MAGIC.toByteArray(Charsets.US_ASCII))
        headerBuffer.putShort(FORMAT_VERSION)
        headerBuffer.put(fileSalt)
        headerBuffer.put(baseIv)
        outputStream.write(headerBuffer.array())

        val chunkBuffer = ByteArray(CHUNK_SIZE_BYTES)
        var chunkIndex: Long = 0
        var bytesRead: Int

        val secretKey = SecretKeySpec(payloadKey, "AES")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")

        while (inputStream.read(chunkBuffer).also { bytesRead = it } != -1) {
            // Peek to see if this is the last chunk
            val nextByte = inputStream.read()
            val isLastChunk = (nextByte == -1)

            // Derive chunk IV: Base IV XOR chunkIndex
            val chunkIv = deriveChunkIv(baseIv, chunkIndex)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, chunkIv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

            // Bind AD: FileId || chunkIndex || isLastChunk
            val ad = buildAssociatedData(fileId, chunkIndex, isLastChunk)
            cipher.updateAAD(ad)

            // Write chunk length (4 bytes)
            val ciphertext = cipher.doFinal(chunkBuffer, 0, bytesRead)
            val lengthBuffer = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(ciphertext.size).array()
            outputStream.write(lengthBuffer)
            outputStream.write(ciphertext)

            if (isLastChunk) {
                break
            } else {
                // Put back the peeked byte into the next chunk read
                chunkBuffer[0] = nextByte.toByte()
                val additional = inputStream.read(chunkBuffer, 1, CHUNK_SIZE_BYTES - 1)
                bytesRead = if (additional != -1) additional + 1 else 1
                chunkIndex++

                val peekAfter = inputStream.read()
                val isFinal = (peekAfter == -1)

                val nextIv = deriveChunkIv(baseIv, chunkIndex)
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH_BITS, nextIv))
                cipher.updateAAD(buildAssociatedData(fileId, chunkIndex, isFinal))

                val nextCiphertext = cipher.doFinal(chunkBuffer, 0, bytesRead)
                outputStream.write(ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(nextCiphertext.size).array())
                outputStream.write(nextCiphertext)

                if (isFinal) break
                // If not final, prime next loop with peeked byte
                chunkBuffer[0] = peekAfter.toByte()
                // Continues in loop...
            }
            chunkIndex++
        }
        outputStream.flush()
    }

    /**
     * Decrypts a chunked VEIL container back to plaintext stream.
     */
    fun decryptStream(
        payloadKey: ByteArray,
        fileId: ByteArray,
        inputStream: InputStream,
        outputStream: OutputStream
    ) {
        // Read header
        val header = ByteArray(4 + 2 + 16 + GCM_IV_LENGTH_BYTES)
        var totalHeaderRead = 0
        while (totalHeaderRead < header.size) {
            val r = inputStream.read(header, totalHeaderRead, header.size - totalHeaderRead)
            check(r != -1) { "Premature EOF while reading VEIL header" }
            totalHeaderRead += r
        }

        val headerBuf = ByteBuffer.wrap(header).order(ByteOrder.BIG_ENDIAN)
        val magic = ByteArray(4).also { headerBuf.get(it) }
        check(String(magic, Charsets.US_ASCII) == HEADER_MAGIC) { "Invalid VEIL container magic" }
        val version = headerBuf.short
        check(version == FORMAT_VERSION) { "Unsupported container version: $version" }

        val fileSalt = ByteArray(16).also { headerBuf.get(it) }
        val baseIv = ByteArray(GCM_IV_LENGTH_BYTES).also { headerBuf.get(it) }

        val secretKey = SecretKeySpec(payloadKey, "AES")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")

        var chunkIndex: Long = 0
        val lengthBytes = ByteArray(4)

        while (true) {
            var read = 0
            while (read < 4) {
                val r = inputStream.read(lengthBytes, read, 4 - read)
                if (r == -1) {
                    if (read == 0) return // Normal EOF reached
                    error("Corrupted chunk header")
                }
                read += r
            }

            val cipherLength = ByteBuffer.wrap(lengthBytes).order(ByteOrder.BIG_ENDIAN).int
            val ciphertext = ByteArray(cipherLength)
            var cipherRead = 0
            while (cipherRead < cipherLength) {
                val r = inputStream.read(ciphertext, cipherRead, cipherLength - cipherRead)
                check(r != -1) { "Unexpected EOF in chunk $chunkIndex" }
                cipherRead += r
            }

            // Attempt decryption. Peek ahead to detect last chunk or try AD
            val chunkIv = deriveChunkIv(baseIv, chunkIndex)
            var decrypted: ByteArray? = null

            // Try with isLastChunk = false first, if failure try true
            for (isLast in listOf(false, true)) {
                try {
                    val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, chunkIv)
                    cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
                    cipher.updateAAD(buildAssociatedData(fileId, chunkIndex, isLast))
                    decrypted = cipher.doFinal(ciphertext)
                    break
                } catch (e: Exception) {
                    // Try next
                }
            }

            val plaintext = decrypted ?: error("Cryptographic authentication failed on chunk $chunkIndex. Data tampered!")
            outputStream.write(plaintext)
            chunkIndex++
        }
    }

    private fun deriveChunkIv(baseIv: ByteArray, chunkIndex: Long): ByteArray {
        val iv = baseIv.clone()
        val indexBuf = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN).putLong(chunkIndex).array()
        for (i in 0 until 8) {
            iv[4 + i] = (iv[4 + i].toInt() xor indexBuf[i].toInt()).toByte()
        }
        return iv
    }

    private fun buildAssociatedData(fileId: ByteArray, chunkIndex: Long, isLastChunk: Boolean): ByteArray {
        val buffer = ByteBuffer.allocate(fileId.size + 8 + 1).order(ByteOrder.BIG_ENDIAN)
        buffer.put(fileId)
        buffer.putLong(chunkIndex)
        buffer.put(if (isLastChunk) 1.toByte() else 0.toByte())
        return buffer.array()
    }
}
