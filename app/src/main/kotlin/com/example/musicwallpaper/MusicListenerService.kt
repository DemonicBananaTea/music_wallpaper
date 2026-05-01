package com.example.musicwallpaper

import android.graphics.Bitmap
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class MusicListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        android.util.Log.d("MUSIC", "LISTENER CONNECTED")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {

        android.util.Log.d("MUSIC", "NOTIF: ${sbn.packageName}")

        try {
            val extras = sbn.notification?.extras ?: return

            val bitmap: Bitmap? =
                extras.getParcelable("android.largeIcon")
                    ?: extras.getParcelable("android.picture")
                    ?: sbn.notification?.getLargeIcon()
                        ?.loadDrawable(this)
                        ?.let { Utils.drawableToBitmap(it) }

            android.util.Log.d("MUSIC", "bitmap = ${bitmap != null}")

            if (bitmap != null && bitmap.width > 50 && bitmap.height > 50) {
                ArtworkStore.currentBitmap = bitmap
                ArtworkStore.lastUpdateTime = System.currentTimeMillis()

                android.util.Log.d("MUSIC", "STORE UPDATED")
            }

        } catch (e: Exception) {
            android.util.Log.e("MUSIC", "ERROR", e)
        }
    }
}