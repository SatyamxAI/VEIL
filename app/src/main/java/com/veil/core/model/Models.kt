package com.veil.core.model

enum class MediaType {
    IMAGE,
    VIDEO
}

enum class SyncStatus {
    LOCAL_ONLY,
    SYNCING,
    SYNCED,
    FAILED
}

data class MediaItem(
    val id: String,
    val vaultId: String,
    val uri: String,
    val fileName: String,
    val remotePath: String? = null,
    val mimeType: String,
    val mediaType: MediaType,
    val sizeBytes: Long,
    val width: Int = 0,
    val height: Int = 0,
    val durationMs: Long = 0,
    val captureTimestamp: Long,
    val blurhash: String? = null,
    val dedupToken: String? = null,
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,
    val isFavorite: Boolean = false,
    val isTrashed: Boolean = false
)

data class Vault(
    val id: String,
    val name: String,
    val remoteRootPath: String,
    val salt: ByteArray,
    val encryptedMasterKey: ByteArray,
    val createdAt: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Vault
        return id == other.id && name == other.name
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

enum class CloudProviderType {
    LOCAL_SANDBOX,
    GOOGLE_DRIVE,
    WEBDAV,
    S3_COMPATIBLE,
    DROPBOX,
    ONEDRIVE
}

data class CloudAccount(
    val id: String,
    val vaultId: String,
    val providerType: CloudProviderType,
    val accountName: String,
    val endpointUrl: String? = null,
    val isPrimary: Boolean = true,
    val lastSyncTimestamp: Long = 0L
)
