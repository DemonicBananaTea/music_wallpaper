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
import com.example.livewallpaper.AppSelectActivity
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter

class MyNotificationListener : NotificationListenerService(), MediaSessionManager.OnActiveSessionsChangedListener {
    private var lastExtractedTrack: String? = null 
    private var lastTrackTitle: String? = null
    private var lastTrackArtist: String? = null
    private var isBitmapLoadedForCurrent: Boolean = false
    private var allowedPackages: Set<String> = emptySet()
    private var sessionManager: MediaSessionManager? = null
    
    private var currentController: MediaController? = null
 
    private var component: ComponentName? = null
    
    private val updateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.ACTION_UPDATE_CONFIG") {
                fetchMetadata() 
            }
        }
    }
    
    private val sessionCallback = object : MediaController.Callback() {

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            Log.d("WallpaperLog", "Metadata changed calling extractBitmap(it). isPlaying = $isPlaying")
            metadata?.let { extractBitmap(it) }
        }
        
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            super.onPlaybackStateChanged(state)
            if (state == null) return
    
            val newPlayingState: Boolean = (state.state == PlaybackState.STATE_PLAYING)
    
            if (newPlayingState == isPlaying) return
            isPlaying = newPlayingState
            Log.d("WallpaperLog", "State changed, invoke. latestBitmap? ${latestBitmap == null}")
            onStateUpdate()
        }
    }

    companion object {
        var latestBitmap: Bitmap? = null
        var isPlaying: Boolean? = null
        var onStateChanged: (() -> Unit)? = null
        private val callbacksBitmap = mutableSetOf<(Bitmap) -> Unit>()
        
        fun subscribeBitmap(callback: (Bitmap) -> Unit) {
            callbacksBitmap.add(callback)
        }
        
        fun unsubscribeBitmap(callback: (Bitmap) -> Unit) {
            callbacksBitmap.remove(callback)
        }
        
        fun onBitmapUpdate(bitmap: Bitmap) {
            callbacksBitmap.forEach { it.invoke(bitmap) }
        }

        private val callbacksState = mutableSetOf<() -> Unit>()
        
        fun subscribeState(callback: () -> Unit) {
            callbacksState.add(callback)
        }
        
        fun unsubscribeState(callback: () -> Unit) {
            callbacksState.remove(callback)
        }
        
        fun onStateUpdate() {
            callbacksState.forEach { it.invoke() }
        }
    }

    override fun onCreate() {
        super.onCreate()
        
        val sessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        val componentName = ComponentName(this, MyNotificationListener::class.java)
        
        val controllers = sessionManager.getActiveSessions(componentName)
        
        if (controllers.isNotEmpty()) {
            val primaryController = controllers[0]
            val state = primaryController.playbackState?.state
            
            isPlaying = (state == PlaybackState.STATE_PLAYING)
            
            Log.d("WallpaperLog", "Сервіс стартонув. Реальний стан плеєра: $isPlaying")
        } else {
            isPlaying = false
            Log.d("WallpaperLog", "Активних сесій не знайдено, ставимо pause")
        }

        val filter = IntentFilter("com.example.ACTION_UPDATE_CONFIG")
        registerReceiver(updateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
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
        val prefs = getSharedPreferences("WallpaperSettings", MODE_PRIVATE)
        allowedPackages = prefs.getStringSet("selected_packages", emptySet()) ?: emptySet()

        val prioritized = sessions
            .filter { it.packageName in allowedPackages }
            .sortedByDescending {
                when (it.playbackState?.state) {
                    PlaybackState.STATE_PLAYING -> 3
                    PlaybackState.STATE_BUFFERING -> 2
                    PlaybackState.STATE_PAUSED -> 1
                    else -> 0
                }
            }

        val newController = prioritized.firstOrNull()
        
        if (newController == null) {
            currentController = null
            isPlaying = false
        } 
        else if (currentController == null) {
            isPlaying = (newController.playbackState?.state == PlaybackState.STATE_PLAYING)
        }

        if (newController?.sessionToken == currentController?.sessionToken) {

            isPlaying =
                (newController?.playbackState?.state == PlaybackState.STATE_PLAYING)

            onStateUpdate()

            newController?.metadata?.let {
                extractBitmap(it)
            }

            return
        }

        currentController?.unregisterCallback(sessionCallback)
        
        currentController = newController
        currentController?.let { controller ->
            controller.registerCallback(sessionCallback)
            
            isPlaying = (currentController?.playbackState?.state == PlaybackState.STATE_PLAYING)
            onStateUpdate()
            controller.metadata?.let { extractBitmap(it) }
        }
    }

    private fun extractBitmap(metadata: MediaMetadata) {
        val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
        val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
        //if (title != lastTrackTitle || artist != lastTrackArtist) {
            lastTrackTitle = title
            lastTrackArtist = artist
            isBitmapLoadedForCurrent = false
            Log.d("WallpaperLog", "Зафіксовано новий трек: $title. Чекаємо на арт...")
        //}

        //if (isBitmapLoadedForCurrent) return
        
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
        latestBitmap = hardwareBitmap
        onBitmapUpdate(hardwareBitmap)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(updateReceiver)
    }
}
