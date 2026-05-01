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

    val allowedApps = Settings.getAllowedApps(context)


   fun update() {
    try {
        val sessions = manager.getActiveSessions(component)

        val allowed = Settings.getAllowedApps(context)

        val controller = sessions.firstOrNull { session ->
            allowed.contains(session.packageName)
        } ?: run {
            ArtworkStore.bitmap = null
            return
        }

        val bmp = controller.metadata?.description?.iconBitmap

        if (bmp != null) {
            ArtworkStore.bitmap = bmp
        }

    } catch (_: Exception) {}
}
}