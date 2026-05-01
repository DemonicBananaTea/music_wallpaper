package com.example.musicwallpaper

import android.graphics.Bitmap
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class MusicListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {

    android.util.Log.d("MUSIC", "NOTIF from: ${sbn.packageName}")

    try {
        val extras = sbn.notification?.extras ?: return

        android.util.Log.d(
            "MUSIC",
            "has largeIcon = ${extras.containsKey("android.largeIcon")}"
        )

        val bitmap =
            extras.getParcelable("android.largeIcon")
                ?: extras.getParcelable("android.picture")
                ?: sbn.notification?.getLargeIcon()
                    ?.loadDrawable(this)
                    ?.let { Utils.drawableToBitmap(it) }

        android.util.Log.d(
            "MUSIC",
            "bitmap created = ${bitmap != null}"
        )

        if (bitmap != null && bitmap.width > 50 && bitmap.height > 50) {
            ArtworkStore.currentBitmap = bitmap
            ArtworkStore.lastUpdateTime = System.currentTimeMillis()

            android.util.Log.d("MUSIC", "STORE UPDATED")
        }

    } catch (e: Exception) {
        android.util.Log.e("MUSIC", "ERROR", e)
    }
}

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // нічого не робимо — чиста логіка
    }
}