package com.veil.core.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.veil.core.crypto.VeilCrypto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * High-performance streaming image decryptor and downsampler.
 * Decrypts encrypted chunks strictly in RAM and emits Hardware/ARGB_8888 Bitmaps.
 */
object VeilImagePipeline {

    suspend fun decryptAndDecodeBitmap(
        payloadKey: ByteArray,
        fileId: ByteArray,
        encryptedInputStream: InputStream,
        targetWidth: Int = 1024,
        targetHeight: Int = 1024
    ): Bitmap? = withContext(Dispatchers.Default) {
        val decryptedByteStream = ByteArrayOutputStream()
        VeilCrypto.decryptStream(payloadKey, fileId, encryptedInputStream, decryptedByteStream)
        val plaintextBytes = decryptedByteStream.toByteArray()

        // 1. Decode bounds first to calculate inSampleSize (saves RAM)
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(plaintextBytes, 0, plaintextBytes.size, options)

        // 2. Calculate optimal inSampleSize
        options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight)
        options.inJustDecodeBounds = false
        options.inPreferredConfig = Bitmap.Config.RGB_565 // Memory-efficient for gallery grids

        BitmapFactory.decodeByteArray(plaintextBytes, 0, plaintextBytes.size, options)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize.coerceAtLeast(1)
    }
}
