package com.example.musicwallpaper

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class MusicListenerService : NotificationListenerService() {

    override fun onCreate() {
        super.onCreate()
        Log.e("MUSIC", "SERVICE CREATED")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.e("MUSIC", "LISTENER CONNECTED")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        Log.e("MUSIC", "POSTED: ${sbn.packageName}")

        val n = sbn.notification
        if (n != null) {
            Log.e("MUSIC", "HAS NOTIFICATION: ${n.extras}")
        } else {
            Log.e("MUSIC", "NOTIFICATION IS NULL")
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.e("MUSIC", "REMOVED: ${sbn.packageName}")
    }
}