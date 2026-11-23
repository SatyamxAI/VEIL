package com.veil

import android.app.Application
import com.veil.core.backup.MediaStoreWatcher
import net.sqlcipher.database.SQLiteDatabase

class VeilApplication : Application() {

    private lateinit var mediaStoreWatcher: MediaStoreWatcher

    override fun onCreate() {
        super.onCreate()

        // 1. Initialize SQLCipher native cryptographic library
        SQLiteDatabase.loadLibs(this)

        // 2. Start media store watcher for automatic encrypted backup
        mediaStoreWatcher = MediaStoreWatcher(this)
        try {
            mediaStoreWatcher.startWatching()
        } catch (e: Exception) {
            // Handled when permissions are not yet granted on first run
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        if (::mediaStoreWatcher.isInitialized) {
            mediaStoreWatcher.stopWatching()
        }
    }
}
