package com.example.livewallpaper

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.os.Build
import android.service.notification.NotificationListenerService

class MyNotificationListener : NotificationListenerService(), MediaSessionManager.OnActiveSessionsChangedListener {

    private var sessionManager: MediaSessionManager? = null

    // Об'єкт для передачі даних (можеш замінити на свій механізм)
    companion object {
        var latestBitmap: Bitmap? = null
        var onBitmapUpdate: ((Bitmap) -> Unit)? = null
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        sessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        
        val component = ComponentName(this, MyNotificationListener::class.java)
        sessionManager?.addOnActiveSessionsChangedListener(this, component)
        
        fetchMetadata()
    }

    override fun onActiveSessionsChanged(controllers: MutableList<MediaController>?) {
        fetchMetadata()
    }

    private fun fetchMetadata() {
    val component = ComponentName(this, MyNotificationListener::class.java)
    val sessions = sessionManager?.getActiveSessions(component) ?: return

    // 1. Використовуємо реальний ID пакету
    val session = sessions.find { it.packageName == "com.spotify.music" } ?: sessions.firstOrNull()

    session?.let { controller ->
        // 2. РЕЄСТРУЄМО КОЛБЕК (щоб ловити перемикання треків)
        controller.registerCallback(object : MediaController.Callback() {
            override fun onMetadataChanged(metadata: MediaMetadata?) {
                metadata?.let { extractBitmap(it) }
            }
        })

        // 3. Витягуємо поточне, що вже грає
        controller.metadata?.let { extractBitmap(it) }
    }
}

private fun extractBitmap(metadata: MediaMetadata) {
    val rawBitmap = metadata.getBitmap(MediaMetadata.METADATA_KEY_ART)
        ?: metadata.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON)
    
    rawBitmap?.let { processAndPost(it) }
}

}
