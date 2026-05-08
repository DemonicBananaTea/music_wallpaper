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
import android.media.session.PlaybackState
class MyNotificationListener : NotificationListenerService(), MediaSessionManager.OnActiveSessionsChangedListener {

    private var sessionManager: MediaSessionManager? = null
    
    private var currentController: MediaController? = null
    
    private var component: ComponentName? = null

    private val sessionCallback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) {
            Log.d("WallpaperLog", "Metadata changed inside Callback")

            metadata?.let { extractBitmap(it) }
        }
override fun onPlaybackStateChanged(state: PlaybackState?) {
    super.onPlaybackStateChanged(state)
    isPlaying = state?.state == PlaybackState.STATE_PLAYING
    Log.d("WallpaperLog", "Is playing: $isPlaying")
}
    }

    // Об'єкт для передачі даних (можеш замінити на свій механізм)
    companion object {
        var latestBitmap: Bitmap? = null
        var onBitmapUpdate: ((Bitmap) -> Unit)? = null
	var isPlaying: Boolean = false

    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        sessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        
        component = ComponentName(this, MyNotificationListener::class.java)
        sessionManager?.addOnActiveSessionsChangedListener(this, component)
        
        fetchMetadata()
    }

    override fun onActiveSessionsChanged(controllers: MutableList<MediaController>?) {
        fetchMetadata()
    }



    private fun fetchMetadata() {
    
    val sessions = sessionManager?.getActiveSessions(component) ?: return
    
    if (sessions.isNullOrEmpty()) {
        currentController?.unregisterCallback(sessionCallback)
        currentController = null
        return
    }
    
    val newController = sessions.find { it.packageName == "com.spotify.music" } ?: sessions.firstOrNull()

    // Якщо це той самий контролер, що вже слухаємо — нічого не робимо
    if (newController?.sessionToken == currentController?.sessionToken) return

    // Відписуємося від старого
    currentController?.unregisterCallback(sessionCallback)
    
    // Підписуємося на новий
    currentController = newController
    currentController?.let { controller ->
Log.d("WallpaperLog", "Subscribing to new session: ${controller.packageName}")
        controller.registerCallback(sessionCallback)
        
        // Отримуємо поточний стан відтворення
        controller.metadata?.let { extractBitmap(it) }
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
