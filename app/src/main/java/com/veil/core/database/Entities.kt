package com.veil.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.veil.core.model.MediaType
import com.veil.core.model.SyncStatus

@Entity(tableName = "media_items")
data class MediaEntity(
    @PrimaryKey val id: String,
    val vaultId: String,
    val localUri: String,
    val fileName: String,
    val remotePath: String?,
    val mimeType: String,
    val mediaType: MediaType,
    val sizeBytes: Long,
    val width: Int,
    val height: Int,
    val durationMs: Long,
    val captureTimestamp: Long,
    val blurhash: String?,
    val dedupToken: String?,
    val syncStatus: SyncStatus,
    val isFavorite: Boolean = false,
    val isTrashed: Boolean = false
)

@Entity(tableName = "vaults")
data class VaultEntity(
    @PrimaryKey val id: String,
    val name: String,
    val remoteRootPath: String,
    val salt: ByteArray,
    val encryptedMasterKey: ByteArray,
    val biometricIv: ByteArray? = null,
    val biometricMasterKey: ByteArray? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: String,
    val vaultId: String,
    val providerType: String,
    val accountName: String,
    val endpointUrl: String?,
    val credentialsBlob: ByteArray?,
    val isPrimary: Boolean,
    val lastSyncTimestamp: Long
)

@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey val id: String,
    val vaultId: String,
    val name: String,
    val isSmartAlbum: Boolean = false,
    val filterQuery: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "backup_queue")
data class BackupQueueEntity(
    @PrimaryKey val queueId: String,
    val mediaItemId: String,
    val localUri: String,
    val dedupToken: String?,
    val attempts: Int = 0,
    val lastErrorCode: Int = 0,
    val state: String = "QUEUED",
    val enqueuedAt: Long = System.currentTimeMillis()
)
