package com.veil.core.media

import android.content.Context
import java.io.File

/**
 * Encrypted LRU Media & Thumbnail Cache
 * Stored in app-private internal cache directory. No plaintext ever touches disk.
 */
class VeilMediaCache(private val context: Context) {
    private val thumbnailCacheDir = File(context.cacheDir, "veil_enc_thumbs").apply { mkdirs() }
    private val mediaCacheDir = File(context.cacheDir, "veil_enc_media").apply { mkdirs() }

    fun getThumbnailFile(id: String): File = File(thumbnailCacheDir, "$id.enc_thumb")
    fun getMediaFile(id: String): File = File(mediaCacheDir, "$id.enc_chunk")

    fun hasThumbnail(id: String): Boolean = getThumbnailFile(id).exists()
    fun hasMedia(id: String): Boolean = getMediaFile(id).exists()

    fun clearAll() {
        thumbnailCacheDir.deleteRecursively()
        mediaCacheDir.deleteRecursively()
        thumbnailCacheDir.mkdirs()
        mediaCacheDir.mkdirs()
    }

    fun getCacheSizeBytes(): Long {
        return (thumbnailCacheDir.listFiles()?.sumOf { it.length() } ?: 0L) +
               (mediaCacheDir.listFiles()?.sumOf { it.length() } ?: 0L)
    }
}
