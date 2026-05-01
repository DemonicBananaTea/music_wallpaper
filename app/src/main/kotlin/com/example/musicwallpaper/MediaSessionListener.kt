package com.example.musicwallpaper

import android.content.ComponentName
import android.content.Context
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState

class MediaSessionListener(private val context: Context) {

    private val mediaSessionManager =
        context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager

    private var isPlaying = false
    private var activePackage: String? = null

    private val listener =
        MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
            updateState(controllers)
        }

    fun start() {
        val component = ComponentName(
            context,
            MediaNotificationListener::class.java
        )

        mediaSessionManager.addOnActiveSessionsChangedListener(
            listener,
            component
        )
    }

    private fun updateState(controllers: List<MediaController>?) {
        if (controllers.isNullOrEmpty()) {
            isPlaying = false
            activePackage = null
            return
        }

        val controller = controllers.firstOrNull()
        val state = controller?.playbackState?.state

        isPlaying = state == PlaybackState.STATE_PLAYING
        activePackage = controller?.packageName
    }

    fun isMusicPlaying(): Boolean = isPlaying

    fun getActivePackage(): String? = activePackage
}