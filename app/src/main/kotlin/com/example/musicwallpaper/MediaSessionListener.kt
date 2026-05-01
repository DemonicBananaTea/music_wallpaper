package com.example.musicwallpaper

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.media.session.MediaController
import android.media.session.MediaSessionManager

class MediaSessionReader(private val context: Context) {

    private val manager =
        context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager

    private val component =
        ComponentName(context, DummyNotificationListener::class.java)

    fun update() {
        try {
            val sessions: List<MediaController> =
                manager.getActiveSessions(component)

            val controller = sessions.firstOrNull() ?: return

            val bmp: Bitmap? =
                controller.metadata?.description?.iconBitmap

            if (bmp != null) {
                ArtworkStore.bitmap = bmp
            }

        } catch (_: Exception) {
        }
    }
}