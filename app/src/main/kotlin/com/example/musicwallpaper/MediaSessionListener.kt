package com.example.musicwallpaper

import android.util.Log
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
        Log.e("MEDIA", "UPDATE CALLED")

val sessions = manager.getActiveSessions(component)
Log.e("MEDIA", "sessions = ${sessions.map { it.packageName }}")

val allowed = Settings.getAllowedApps(context)
Log.e("MEDIA", "allowed = $allowed")

val controller = sessions.firstOrNull { allowed.contains(it.packageName) }
Log.e("MEDIA", "controller = ${controller?.packageName}")

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
        
        ArtworkStore.currentBitmap = null

    } catch (e: Exception) {
        e.printStackTrace()
    }
}
}