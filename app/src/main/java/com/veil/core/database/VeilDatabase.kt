package com.veil.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.veil.core.model.MediaType
import com.veil.core.model.SyncStatus
import net.sqlcipher.database.SupportFactory

class Converters {
    @TypeConverter
    fun fromMediaType(type: MediaType): String = type.name

    @TypeConverter
    fun toMediaType(value: String): MediaType = MediaType.valueOf(value)

    @TypeConverter
    fun fromSyncStatus(status: SyncStatus): String = status.name

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus = SyncStatus.valueOf(value)
}

@Database(
    entities = [
        MediaEntity::class,
        VaultEntity::class,
        AccountEntity::class,
        AlbumEntity::class,
        BackupQueueEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class VeilDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao
    abstract fun vaultDao(): VaultDao
    abstract fun backupQueueDao(): BackupQueueDao
    abstract fun albumDao(): AlbumDao

    companion object {
        @Volatile
        private var INSTANCE: VeilDatabase? = null

        fun getInstance(context: Context, passphrase: ByteArray? = null): VeilDatabase {
            return INSTANCE ?: synchronized(this) {
                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    VeilDatabase::class.java,
                    "veil_vault_metadata.db"
                )

                // If a passphrase is provided, initialize full SQLCipher encryption at rest
                if (passphrase != null && passphrase.isNotEmpty()) {
                    val factory = SupportFactory(passphrase)
                    builder.openHelperFactory(factory)
                }

                builder.fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
