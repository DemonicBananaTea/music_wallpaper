package com.example.musicwallpaper

import android.content.ComponentName
import android.content.Context
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.util.Log

class MediaSessionListener(private val context: Context) {

    private val mediaSessionManager =
        context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager

    private var isPlaying = false
    private var activePackage: String? = null

    fun start() {
        val component = ComponentName(context, DummyNotificationListener::class.java)

        mediaSessionManager.addOnActiveSessionsChangedListener(
            listener,
            component
        )

        Log.e("MEDIA", "STARTED")
    }

    private val listener =
        MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
            update(controllers)
        }

    private fun update(controllers: List<MediaController>?) {
        if (controllers.isNullOrEmpty()) {
            isPlaying = false
            activePackage = null
            return
        }

        val controller = controllers.firstOrNull()
        val state = controller?.playbackState?.state

        isPlaying = state == PlaybackState.STATE_PLAYING
        activePackage = controller?.packageName

        Log.e("MEDIA", "PLAYING=$isPlaying PACKAGE=$activePackage")

        val art = controller?.metadata?.description?.iconBitmap
        if (art != null) {
            ArtworkStore.bitmap = art
            Log.e("MEDIA", "ART UPDATED")
        }
    }

    fun isPlaying(): Boolean = isPlaying
}