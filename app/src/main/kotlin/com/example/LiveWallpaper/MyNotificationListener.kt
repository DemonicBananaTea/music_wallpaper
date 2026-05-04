package com.example.livewallpaper

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.os.Build
import android.service.notification.NotificationListenerService
import android.util.Log


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

    Log.d("WallpaperLog", "Sessions: ${sessions.size}")

    // 1. Використовуємо ПРАВИЛЬНИЙ пакет для Spotify
    val session = sessions.firstOrNull() { it.packageName == "com.spotify.music" }

    session?.let { controller ->
        Log.d("WallpaperLog", "Using session: ${controller.packageName}")

        // 2. РЕЄСТРУЄМО КОЛБЕК (це змусить фон оновлюватися самому)
        controller.registerCallback(object : MediaController.Callback() {
            override fun onMetadataChanged(metadata: MediaMetadata?) {
                Log.d("WallpaperLog", "Metadata changed (New song!)")
                metadata?.let { extractBitmap(it) }
            }
        })

        // 3. Відразу витягуємо поточну картинку
        controller.metadata?.let { 
            Log.d("WallpaperLog", "Extracting initial metadata")
            extractBitmap(it) 
        }
    }
}


private fun extractBitmap(metadata: MediaMetadata) {
    Log.d("WallpaperLog", "--- Починаємо екстракцію бітмапа ---")
    
    // Перевіряємо всі можливі ключі по черзі
    val art = metadata.getBitmap(MediaMetadata.METADATA_KEY_ART)
    val albumArt = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
    val icon = metadata.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON)

    Log.d("WallpaperLog", "Ключі: ART=${art != null}, ALBUM_ART=${albumArt != null}, ICON=${icon != null}")

    val result = art ?: albumArt ?: icon

    if (result != null) {
        Log.d("WallpaperLog", "Успіх! Бітмап знайдено.")
        processAndPost(result)
    } else {
        Log.e("WallpaperLog", "ЖАХ! Всі ключі порожні. Spotify не віддав картинку.")
        
        // Перевіримо, чи є хоча б назва треку, щоб знати, що метадані взагалі живі
        val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
        Log.d("WallpaperLog", "Трек, для якого шукаємо арт: $title")
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
