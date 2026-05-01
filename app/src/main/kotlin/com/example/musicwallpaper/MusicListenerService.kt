package com.example.musicwallpaper

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.graphics.Bitmap

class MusicListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        android.util.Log.e("MUSIC", "CONNECTED")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        try {
            val icon = sbn.notification?.getLargeIcon()
            val bmp = icon?.loadDrawable(this)?.let {
                Utils.drawableToBitmap(it)
            }

            if (bmp != null) {
                ArtworkStore.bitmap = bmp
                android.util.Log.e("MUSIC", "BITMAP UPDATED")
            }

        } catch (e: Exception) {
            android.util.Log.e("MUSIC", "ERROR", e)
        }
    }
}