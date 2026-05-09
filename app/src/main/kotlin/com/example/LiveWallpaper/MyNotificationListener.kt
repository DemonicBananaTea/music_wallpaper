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
    private var lastExtractedTrack: String? = null 
private var lastTrackTitle: String? = null
private var lastTrackArtist: String? = null
private var isBitmapLoadedForCurrent: Boolean = false
    private var sessionManager: MediaSessionManager? = null
    
    private var currentController: MediaController? = null
 
    private var component: ComponentName? = null

    private val sessionCallback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) {
            Log.d("WallpaperLog", "Metadata changed calling extractBitmap(it). isPlaying = $isPlaying")
            metadata?.let { extractBitmap(it) }
            }
    	
        override fun onPlaybackStateChanged(state: PlaybackState?) {
    	    super.onPlaybackStateChanged(state)
        if (state == null) return
    
        // Тепер компілятор ЗНАЄ, що state не null
        // і результат порівняння буде чистим Boolean
        val newPlayingState: Boolean = (state.state == PlaybackState.STATE_PLAYING)
    
        if (newPlayingState == isPlaying) return
            isPlaying = newPlayingState
            Log.d("WallpaperLog", "State changed, invoke. latestBitmap? ${latestBitmap == null}")
            onStateChanged?.invoke()
        }
    }

    companion object {
        var latestBitmap: Bitmap? = null
        var onBitmapUpdate: ((Bitmap) -> Unit)? = null
	    var isPlaying: Boolean? = null
        var onStateChanged: (() -> Unit)? = null
    }

    override fun onCreate() {
        super.onCreate()
        
        val sessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        val componentName = ComponentName(this, MyNotificationListener::class.java)
        
        // Отримуємо список активних сесій (Spotify, YouTube тощо)
        val controllers = sessionManager.getActiveSessions(componentName)
        
        if (controllers.isNotEmpty()) {
            // Беремо перший активний плеєр
            val primaryController = controllers[0]
            val state = primaryController.playbackState?.state
            
            // Оновлюємо нашу статичну змінну реальним значенням
            isPlaying = (state == PlaybackState.STATE_PLAYING)
            
            Log.d("WallpaperLog", "Сервіс стартонув. Реальний стан плеєра: $isPlaying")
        } else {
            isPlaying = false
            Log.d("WallpaperLog", "Активних сесій не знайдено, ставимо pause")
        }
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
    val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
    val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
    if (title != lastTrackTitle || artist != lastTrackArtist) {
        lastTrackTitle = title
        lastTrackArtist = artist
        isBitmapLoadedForCurrent = false
        Log.d("WallpaperLog", "Зафіксовано новий трек: $title. Чекаємо на арт...")
    }

    // Якщо ми вже успішно витягли бітмапу для цього треку — ігноруємо спам
    if (isBitmapLoadedForCurrent) return
    
    Log.d("WallpaperLog", "Новий трек: $title. Витягуємо арт...")   
    val art = metadata.getBitmap(MediaMetadata.METADATA_KEY_ART)
    val albumArt = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
    val icon = metadata.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON)

    Log.d("WallpaperLog", "Ключі: ART=${art != null}, ALBUM_ART=${albumArt != null}, ICON=${icon != null}")

    val result = art ?: albumArt ?: icon

    if (result != null) {
        Log.d("WallpaperLog", "Успіх! Бітмап знайдено.")
        processAndPost(result)
        isBitmapLoadedForCurrent=true
    } else {
        return
    }
}



    private fun processAndPost(source: Bitmap) {
        val hardwareBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            source.copy(Bitmap.Config.HARDWARE, false)
        } else {
            source
        }
        Log.d("WallpaperLog", "Bitmap loaded and sent. Callback null? ${onBitmapUpdate == null}")
        latestBitmap = hardwareBitmap
        onBitmapUpdate?.invoke(hardwareBitmap)
    }
}
