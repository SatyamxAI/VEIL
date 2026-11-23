package com.veil.core.media

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.datasource.BaseDataSource
import androidx.media3.datasource.DataSpec
import com.veil.core.crypto.VeilCrypto
import com.veil.core.transport.CloudStorageProvider
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * Custom Media3 / ExoPlayer DataSource for streaming on-demand decrypted video.
 * Intercepts range requests, fetches the required 64 KiB chunks, decrypts them in RAM,
 * and feeds plaintext bytes directly to the video decoder without touching disk.
 */
class VeilMediaDataSource(
    private val payloadKey: ByteArray,
    private val fileId: ByteArray,
    private val remotePath: String,
    private val provider: CloudStorageProvider
) : BaseDataSource(/* isNetwork = */ true) {

    private var dataSpec: DataSpec? = null
    private var bytesRemaining: Long = 0
    private var currentStream: InputStream? = null

    override fun open(dataSpec: DataSpec): Long {
        this.dataSpec = dataSpec
        transferInitializing(dataSpec)

        val startPosition = dataSpec.position
        val length = dataSpec.length

        // Fetch encrypted stream from provider for the range
        // In full implementation, maps startPosition to 64 KiB chunk offset
        val result = kotlinx.coroutines.runBlocking {
            provider.getStream(remotePath, if (length != C.LENGTH_UNSET.toLong()) startPosition..(startPosition + length) else null)
        }

        val rawStream = result.getOrThrow()
        
        // Decrypt stream in memory
        val decryptedOut = ByteArrayOutputStream()
        VeilCrypto.decryptStream(payloadKey, fileId, rawStream, decryptedOut)
        val decryptedBytes = decryptedOut.toByteArray()

        currentStream = decryptedBytes.inputStream()
        bytesRemaining = if (length != C.LENGTH_UNSET.toLong()) length else decryptedBytes.size.toLong()

        transferStarted(dataSpec)
        return bytesRemaining
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (length == 0) return 0
        if (bytesRemaining == 0L) return C.RESULT_END_OF_INPUT

        val bytesToRead = minOf(length.toLong(), bytesRemaining).toInt()
        val bytesRead = currentStream?.read(buffer, offset, bytesToRead) ?: -1
        if (bytesRead == -1) return C.RESULT_END_OF_INPUT

        bytesRemaining -= bytesRead
        bytesTransferred(bytesRead)
        return bytesRead
    }

    override fun getUri(): Uri? = dataSpec?.uri

    override fun close() {
        currentStream?.close()
        currentStream = null
        if (dataSpec != null) {
            transferEnded()
            dataSpec = null
        }
    }
}
