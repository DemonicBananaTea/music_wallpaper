package com.example.musicwallpaper

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.graphics.Bitmap

class MusicListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        val title = extras.getString("android.title")
        val artist = extras.getString("android.text")

        val largeIcon = notification.getLargeIcon()

        val bitmap: Bitmap? = largeIcon?.loadDrawable(this)?.let {
            Utils.drawableToBitmap(it)
        }

        if (bitmap != null) {
            ArtworkStore.currentBitmap = bitmap
            ArtworkStore.lastUpdateTime = System.currentTimeMillis()
        }
    }
}