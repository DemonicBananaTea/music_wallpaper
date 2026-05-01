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
        val allowed = Settings.getAllowedApps(context)

        val sessions = manager.getActiveSessions(component)

        val controller = sessions
            .filter { allowed.contains(it.packageName) }
            .firstOrNull()

        if (controller == null) {
            ArtworkStore.currentBitmap = null
            ArtworkStore.lastUpdateTime = System.currentTimeMillis()
            return
        }

        val bmp = controller.metadata?.description?.iconBitmap

        if (bmp != null) {
            ArtworkStore.currentBitmap = bmp
            ArtworkStore.lastUpdateTime = System.currentTimeMillis()
        }

    } catch (_: Exception) {}
}
}