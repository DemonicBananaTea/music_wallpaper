package com.example.musicwallpaper

import android.content.ComponentName
import android.content.Context
import android.media.session.MediaSessionManager

class MediaSessionReader(private val context: Context) {

    private val manager =
        context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager

    private val component =
        ComponentName(context, DummyNotificationListener::class.java)

    fun update() {
        try {
            val allowed = Settings.getAllowedApps(context)

            val sessions = manager.getActiveSessions(component)

            val controller = sessions.firstOrNull {
                allowed.contains(it.packageName)
            }

            if (controller == null) {
                ArtworkStore.currentBitmap = null
                return
            }

            val bmp = controller.metadata?.description?.iconBitmap

            ArtworkStore.currentBitmap = bmp

        } catch (_: Exception) {
            ArtworkStore.currentBitmap = null
        }
    }
}