package com.veil.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.veil.core.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("SELECT * FROM media_items WHERE vaultId = :vaultId AND isTrashed = 0 ORDER BY captureTimestamp DESC")
    fun getTimeline(vaultId: String): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE vaultId = :vaultId AND isFavorite = 1 AND isTrashed = 0 ORDER BY captureTimestamp DESC")
    fun getFavorites(vaultId: String): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE id = :id")
    suspend fun getById(id: String): MediaEntity?

    @Query("SELECT * FROM media_items WHERE dedupToken = :token LIMIT 1")
    suspend fun getByDedupToken(token: String): MediaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(item: MediaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MediaEntity>)

    @Query("UPDATE media_items SET isFavorite = :isFav WHERE id = :id")
    suspend fun setFavorite(id: String, isFav: Boolean)

    @Query("UPDATE media_items SET syncStatus = :status, remotePath = :remotePath WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: SyncStatus, remotePath: String?)

    @Query("UPDATE media_items SET isTrashed = 1 WHERE id = :id")
    suspend fun moveToTrash(id: String)

    @Query("SELECT COUNT(*) FROM media_items WHERE vaultId = :vaultId AND isTrashed = 0")
    fun getMediaCount(vaultId: String): Flow<Int>
}

@Dao
interface VaultDao {
    @Query("SELECT * FROM vaults LIMIT 1")
    fun getActiveVault(): Flow<VaultEntity?>

    @Query("SELECT * FROM vaults WHERE id = :id")
    suspend fun getVaultById(id: String): VaultEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVault(vault: VaultEntity)

    @Query("UPDATE vaults SET biometricIv = :iv, biometricMasterKey = :key WHERE id = :id")
    suspend fun updateBiometricKey(id: String, iv: ByteArray, key: ByteArray)
}

@Dao
interface BackupQueueDao {
    @Query("SELECT * FROM backup_queue WHERE state = 'QUEUED' ORDER BY enqueuedAt ASC")
    suspend fun getPendingItems(): List<BackupQueueEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(item: BackupQueueEntity)

    @Query("DELETE FROM backup_queue WHERE queueId = :queueId")
    suspend fun dequeue(queueId: String)

    @Query("UPDATE backup_queue SET attempts = attempts + 1, lastErrorCode = :errorCode, state = :state WHERE queueId = :queueId")
    suspend fun recordAttempt(queueId: String, errorCode: Int, state: String)
}

@Dao
interface AlbumDao {
    @Query("SELECT * FROM albums WHERE vaultId = :vaultId ORDER BY createdAt DESC")
    fun getAlbums(vaultId: String): Flow<List<AlbumEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: AlbumEntity)
}
