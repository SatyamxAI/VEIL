package com.veil.core.transport

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Local Encrypted Sandbox Cloud Provider
 * Emulates remote cloud storage inside the application's isolated private storage.
 * Used for offline vaults, zero-setup onboarding, and developer testing.
 */
class LocalSandboxStorageProvider(
    private val context: Context,
    override val accountName: String = "Local Encrypted Vault"
) : CloudStorageProvider {

    override val providerId: String = "local_sandbox"

    private val rootDir: File by lazy {
        File(context.filesDir, "veil_remote_sandbox").apply {
            if (!exists()) mkdirs()
        }
    }

    override suspend fun testConnection(): Result<Boolean> = Result.success(true)

    override suspend fun list(remotePath: String): Result<List<RemoteBlob>> = withContext(Dispatchers.IO) {
        runCatching {
            val target = File(rootDir, remotePath.trimStart('/'))
            if (!target.exists()) return@runCatching emptyList<RemoteBlob>()

            target.listFiles()?.map { file ->
                RemoteBlob(
                    name = file.name,
                    path = file.relativeTo(rootDir).path.replace('\\', '/'),
                    sizeBytes = file.length(),
                    lastModified = file.lastModified(),
                    isDirectory = file.isDirectory
                )
            } ?: emptyList()
        }
    }

    override suspend fun getStream(remotePath: String, byteRange: LongRange?): Result<InputStream> = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(rootDir, remotePath.trimStart('/'))
            check(file.exists()) { "Remote blob not found: $remotePath" }

            val fis = FileInputStream(file)
            if (byteRange != null) {
                fis.skip(byteRange.first)
            }
            fis
        }
    }

    override suspend fun uploadStream(
        remotePath: String,
        contentLength: Long,
        stream: InputStream,
        onProgress: ((UploadProgress) -> Unit)?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val target = File(rootDir, remotePath.trimStart('/'))
            target.parentFile?.mkdirs()

            val tempFile = File(target.parentFile, "${target.name}.tmp")
            FileOutputStream(tempFile).use { fos ->
                val buffer = ByteArray(64 * 1024)
                var bytesWritten: Long = 0
                var read: Int
                while (stream.read(buffer).also { read = it } != -1) {
                    fos.write(buffer, 0, read)
                    bytesWritten += read
                    onProgress?.invoke(UploadProgress(bytesWritten, contentLength))
                }
                fos.flush()
            }

            // Atomic rename to commit
            if (target.exists()) target.delete()
            tempFile.renameTo(target)
            Unit
        }
    }

    override suspend fun delete(remotePath: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val target = File(rootDir, remotePath.trimStart('/'))
            if (target.exists()) target.delete()
            Unit
        }
    }
}
