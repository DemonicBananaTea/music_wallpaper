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

        val allowed = Settings.getAllowedApps(context)

        val sessions = manager.getActiveSessions(component)

        // 🔥 шукаємо сесію тільки з ОБРАНИХ застосунків
        val controller = sessions.firstOrNull { session ->
            allowed.contains(session.packageName)
        }

        // ❌ якщо немає активної сесії для обраних apps
        if (controller == null) {
            ArtworkStore.currentBitmap = null
            return
        }

        // ✅ якщо є — беремо обкладинку
        ArtworkStore.currentBitmap =
            controller.metadata?.description?.iconBitmap
    }
}