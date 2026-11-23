package com.veil.core.backup

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Listens for new camera photos and videos via MediaStore ContentObserver.
 * Debounces bursts (e.g. rapid camera shots) for 15 seconds before triggering WorkManager.
 */
class MediaStoreWatcher(
    private val context: Context,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private var debounceJob: Job? = null

    private val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)
            debounceJob?.cancel()
            debounceJob = coroutineScope.launch {
                delay(15_000) // 15-second debounce
                BackupWorker.enqueueBackup(context)
            }
        }
    }

    fun startWatching() {
        context.contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            observer
        )
        context.contentResolver.registerContentObserver(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            true,
            observer
        )
    }

    fun stopWatching() {
        context.contentResolver.unregisterContentObserver(observer)
        debounceJob?.cancel()
    }
}
