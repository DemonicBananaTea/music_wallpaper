package com.example.musicwallpaper

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.app.Notification
import android.graphics.Bitmap

class MusicListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras

        val bmp = extras.getParcelable<Bitmap>(Notification.EXTRA_LARGE_ICON_BIG)
            ?: extras.getParcelable(Notification.EXTRA_LARGE_ICON)

        if (bmp != null) {
            ArtworkStore.currentBitmap = bmp
            ArtworkStore.lastUpdateTime = System.currentTimeMillis()
        }
    }
}
