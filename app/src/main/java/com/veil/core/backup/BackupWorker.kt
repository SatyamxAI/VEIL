package com.veil.core.backup

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.veil.core.crypto.VeilCrypto
import com.veil.core.database.VeilDatabase
import com.veil.core.model.SyncStatus
import com.veil.core.transport.LocalSandboxStorageProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Android WorkManager Worker that performs background encryption and cloud backup.
 * Runs under system constraints: Unmetered Network (Wi-Fi), Charging, Battery Not Low.
 */
class BackupWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val database = VeilDatabase.getInstance(context)
        val queueDao = database.backupQueueDao()
        val mediaDao = database.mediaDao()

        val pending = queueDao.getPendingItems()
        if (pending.isEmpty()) {
            return@withContext Result.success()
        }

        // Provider for backup destination (Local sandbox or user-configured provider)
        val provider = LocalSandboxStorageProvider(context)

        // Mock payload key for backup encryption pass
        val payloadKey = VeilCrypto.generateRandomBytes(32)

        for (item in pending) {
            try {
                mediaDao.updateSyncStatus(item.mediaItemId, SyncStatus.SYNCING, null)

                val localFile = File(item.localUri)
                if (!localFile.exists()) {
                    queueDao.dequeue(item.queueId)
                    mediaDao.updateSyncStatus(item.mediaItemId, SyncStatus.FAILED, null)
                    continue
                }

                // 1. Blind Deduplication check
                val dedupKey = VeilCrypto.hkdfExpand(payloadKey, "veil-dedup-token-v1")
                val dedupToken = FileInputStream(localFile).use {
                    VeilCrypto.computeBlindDedupToken(dedupKey, it)
                }

                // 2. Encrypt locally to temporary chunked container
                val tempEncryptedFile = File(context.cacheDir, "backup_${item.mediaItemId}.veil.tmp")
                val fileId = VeilCrypto.generateRandomBytes(16)

                FileInputStream(localFile).use { input ->
                    FileOutputStream(tempEncryptedFile).use { output ->
                        VeilCrypto.encryptStream(payloadKey, fileId, input, output)
                    }
                }

                // 3. Upload encrypted stream to cloud provider
                val remotePath = "d/${item.mediaItemId.take(2)}/${item.mediaItemId}.veil"
                FileInputStream(tempEncryptedFile).use { uploadStream ->
                    provider.uploadStream(
                        remotePath = remotePath,
                        contentLength = tempEncryptedFile.length(),
                        stream = uploadStream
                    ).getOrThrow()
                }

                // 4. Cleanup temp file and update database status
                tempEncryptedFile.delete()
                queueDao.dequeue(item.queueId)
                mediaDao.updateSyncStatus(item.mediaItemId, SyncStatus.SYNCED, remotePath)

            } catch (e: Exception) {
                queueDao.recordAttempt(item.queueId, errorCode = 1, state = "FAILED")
                mediaDao.updateSyncStatus(item.mediaItemId, SyncStatus.FAILED, null)
            }
        }

        Result.success()
    }

    companion object {
        fun enqueueBackup(context: Context, wifiOnly: Boolean = true, requiresCharging: Boolean = false) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
                .setRequiresCharging(requiresCharging)
                .setRequiresBatteryNotLow(true)
                .build()

            val request = OneTimeWorkRequestBuilder<BackupWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
