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
        
        // РЕЄСТРУЄМО СЛУХАЧА (Ось чого не вистачало!)
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

        val session = sessions.find { it.packageName == "com.spotify.music" } ?: null

        session?.metadata?.let { metadata ->
            // Витягаємо обкладинку (пробуємо спочатку велику, потім іконку)
            val rawBitmap = metadata.getBitmap(MediaMetadata.METADATA_KEY_ART)
                ?: metadata.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON)

            rawBitmap?.let { processAndPost(it) }
        }
    }

    private fun processAndPost(source: Bitmap) {
        // Конвертація в HARDWARE для HardwareRenderer
        val hardwareBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            source.copy(Bitmap.Config.HARDWARE, false)
        } else {
            source
        }

        latestBitmap = hardwareBitmap
        onBitmapUpdate?.invoke(hardwareBitmap)
    }
}
