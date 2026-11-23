package com.veil.core.transport

import java.io.InputStream

data class RemoteBlob(
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val lastModified: Long,
    val isDirectory: Boolean = false
)

data class UploadProgress(
    val bytesWritten: Long,
    val totalBytes: Long
)

interface CloudStorageProvider {
    val providerId: String
    val accountName: String

    suspend fun testConnection(): Result<Boolean>
    suspend fun list(remotePath: String): Result<List<RemoteBlob>>
    suspend fun getStream(remotePath: String, byteRange: LongRange? = null): Result<InputStream>
    suspend fun uploadStream(
        remotePath: String,
        contentLength: Long,
        stream: InputStream,
        onProgress: ((UploadProgress) -> Unit)? = null
    ): Result<Unit>
    suspend fun delete(remotePath: String): Result<Unit>
}
