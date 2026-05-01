package com.example.musicwallpaper

import android.graphics.Bitmap
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class MusicListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {

        try {
            val extras = sbn.notification?.extras ?: return

            val bitmap: Bitmap? =
                extras.getParcelable("android.largeIcon")
                    ?: extras.getParcelable("android.picture")
                    ?: sbn.notification?.getLargeIcon()
                        ?.loadDrawable(this)
                        ?.let { Utils.drawableToBitmap(it) }

            // 🧠 тільки базова перевірка (без фільтрів)
            if (bitmap != null &&
                bitmap.width > 50 &&
                bitmap.height > 50
            ) {
                ArtworkStore.currentBitmap = bitmap
                ArtworkStore.lastUpdateTime = System.currentTimeMillis()
            }

        } catch (e: Exception) {
            // ❗ ніколи не даємо listener'у падати
            e.printStackTrace()
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // нічого не робимо — чиста логіка
    }
}