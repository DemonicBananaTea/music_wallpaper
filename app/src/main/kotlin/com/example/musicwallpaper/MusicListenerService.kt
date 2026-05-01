package com.example.musicwallpaper

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class MusicListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        Log.e("TEST", "CONNECTED")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        Log.e("TEST", "POSTED: ${sbn.packageName}")
        ArtworkStore.lastUpdateTime = System.currentTimeMillis()
    }
}