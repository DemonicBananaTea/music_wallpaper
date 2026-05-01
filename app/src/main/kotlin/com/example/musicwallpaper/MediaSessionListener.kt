package com.example.musicwallpaper

import android.content.ComponentName
import android.content.Context
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.util.Log

class MediaSessionListener(private val context: Context) {

    private val manager =
        context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager

    fun start() {
        try {
            val component = ComponentName(context, DummyNotificationListener::class.java)

            manager.addOnActiveSessionsChangedListener(
                { controllers -> safeUpdate(controllers) },
                component
            )

            Log.e("MEDIA", "STARTED")

        } catch (e: Exception) {
            Log.e("MEDIA", "FAILED", e)
        }
    }

    private fun safeUpdate(controllers: List<MediaController>?) {
        try {
            val c = controllers?.firstOrNull() ?: return

            val state = c.playbackState?.state ?: return
            val playing = state == PlaybackState.STATE_PLAYING

            Log.e("MEDIA", "PLAYING=$playing")

            val bmp = c.metadata?.description?.iconBitmap

            if (playing && bmp != null) {
                ArtworkStore.set(bmp)
            }

        } catch (e: Exception) {
            Log.e("MEDIA", "UPDATE ERROR", e)
        }
    }
}