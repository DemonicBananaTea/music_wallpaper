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
android.util.Log.e("MEDIA", "sessions: ${sessions.map { it.packageName }}")
android.util.Log.e("MEDIA", "allowed: $allowed")
        // 🔥 шукаємо сесію тільки серед обраних застосунків
        val controller = sessions.firstOrNull { session ->
            allowed.contains(session.packageName)
        }

        if (controller == null) {
            // ❌ нема активної сесії обраного app
            ArtworkStore.currentBitmap = null
            return
        }

        val bmp = controller.metadata?.description?.iconBitmap

        if (bmp != null) {
            ArtworkStore.currentBitmap = bmp
        } else {
            // є сесія, але нема обкладинки
            ArtworkStore.currentBitmap = null
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }
}
}