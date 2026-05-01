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

        val active = sessions.firstOrNull {
            it.packageName in allowed
        }

        if (active == null) {
            ArtworkStore.currentBitmap = null
            return
        }

        ArtworkStore.currentBitmap =
            active.metadata?.description?.iconBitmap
    }
}