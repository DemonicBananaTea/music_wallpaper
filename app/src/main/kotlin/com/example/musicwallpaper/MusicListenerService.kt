package com.example.musicwallpaper

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.graphics.Bitmap

class MusicListenerService : NotificationListenerService() {
override fun onNotificationPosted(sbn: StatusBarNotification) {

    val extras = sbn.notification?.extras ?: return

    val bitmap =
        extras.getParcelable<Bitmap>("android.largeIcon")
            ?: extras.getParcelable("android.largeIcon")
            ?: sbn.notification?.getLargeIcon()
                ?.loadDrawable(this)
                ?.let { Utils.drawableToBitmap(it) }

    if (bitmap != null) {
        ArtworkStore.currentBitmap = bitmap
        ArtworkStore.lastUpdateTime = System.currentTimeMillis()
    }
}
}