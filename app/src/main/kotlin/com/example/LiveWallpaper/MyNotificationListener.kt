package com.example.livewallpaper

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Build
import android.service.notification.NotificationListenerService
import android.util.Log

class MyNotificationListener :
    NotificationListenerService(),
    MediaSessionManager.OnActiveSessionsChangedListener {
    data class TrackFingerprint(
        val title: String?,
        val artist: String?,
        val duration: Long?
    )
    private var lastFingerprint: TrackFingerprint? = null
    companion object {

        var latestBitmap: Bitmap? = null
        var isPlaying: Boolean = false

        private val callbacksBitmap =
            mutableSetOf<(Bitmap) -> Unit>()

        private val callbacksState =
            mutableSetOf<() -> Unit>()

        fun subscribeBitmap(callback: (Bitmap) -> Unit) {
            callbacksBitmap.add(callback)
        }

        fun unsubscribeBitmap(callback: (Bitmap) -> Unit) {
            callbacksBitmap.remove(callback)
        }

        fun subscribeState(callback: () -> Unit) {
            callbacksState.add(callback)
        }

        fun unsubscribeState(callback: () -> Unit) {
            callbacksState.remove(callback)
        }

        fun onBitmapUpdate(bitmap: Bitmap) {
            callbacksBitmap.forEach { it.invoke(bitmap) }
        }

        fun onStateUpdate() {
            callbacksState.forEach { it.invoke() }
        }
    }

    data class SessionHolder(
        val controller: MediaController,
        val callback: MediaController.Callback,
        var lastState: PlaybackState? = null,
        var lastMetadata: MediaMetadata? = null
    )

    private val trackedSessions =
        mutableMapOf<MediaSession.Token, SessionHolder>()

    private var activeToken: MediaSession.Token? = null

    private var sessionManager: MediaSessionManager? = null

    private var component: ComponentName? = null

    private var allowedPackages: Set<String> = emptySet()

    private val updateReceiver = object : BroadcastReceiver() {

        override fun onReceive(
            context: Context?,
            intent: Intent?
        ) {

            if (intent?.action ==
                "com.example.ACTION_UPDATE_CONFIG"
            ) {

                reloadAllowedPackages()
                refreshSessions()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        val filter =
            IntentFilter("com.example.ACTION_UPDATE_CONFIG")

        registerReceiver(
            updateReceiver,
            filter,
            Context.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onListenerConnected() {
        super.onListenerConnected()

        sessionManager =
            getSystemService(Context.MEDIA_SESSION_SERVICE)
                    as MediaSessionManager

        component =
            ComponentName(
                this,
                MyNotificationListener::class.java
            )

        sessionManager?.addOnActiveSessionsChangedListener(
            this,
            component
        )

        reloadAllowedPackages()

        refreshSessions()
    }

    override fun onActiveSessionsChanged(
        controllers: MutableList<MediaController>?
    ) {

        Log.d("Wallpaper", "Sessions changed")

        refreshSessions()
    }

    private fun reloadAllowedPackages() {

        val prefs =
            getSharedPreferences(
                "WallpaperSettings",
                MODE_PRIVATE
            )

        allowedPackages =
            prefs.getStringSet(
                "selected_packages",
                emptySet()
            ) ?: emptySet()
    }

    private fun refreshSessions() {

        val controllers =
            sessionManager?.getActiveSessions(component)
                ?: emptyList()

        val filtered =
            controllers.filter {
                it.packageName in allowedPackages
            }

        val newTokens =
            filtered.map { it.sessionToken }.toSet()

        val existingTokens =
            trackedSessions.keys.toSet()

        // remove old
        (existingTokens - newTokens).forEach {
            detachSession(it)
        }

        // add new
        filtered.forEach { controller ->

            if (controller.sessionToken !in trackedSessions) {
                attachSession(controller)
            }
        }

        evaluateActiveSession()
    }

    private fun attachSession(
        controller: MediaController
    ) {

        Log.d(
            "Wallpaper",
            "Attach ${controller.packageName}"
        )

        val callback =
            object : MediaController.Callback() {

                override fun onPlaybackStateChanged(
                    state: PlaybackState?
                ) {

                    val holder =
                        trackedSessions[controller.sessionToken]
                            ?: return

                    holder.lastState = state

                    Log.d(
                        "Wallpaper",
                        "${controller.packageName} state=${state?.state}"
                    )

                    evaluateActiveSession()
                }

                override fun onMetadataChanged(
                    metadata: MediaMetadata?
                ) {

                    val holder =
                        trackedSessions[controller.sessionToken]
                            ?: return

                    holder.lastMetadata = metadata

                    Log.d(
                        "Wallpaper",
                        "${controller.packageName} metadata changed"
                    )

                    evaluateActiveSession()
                }
            }

        controller.registerCallback(callback)

        trackedSessions[controller.sessionToken] =
            SessionHolder(
                controller = controller,
                callback = callback,
                lastState = controller.playbackState,
                lastMetadata = controller.metadata
            )
    }

    private fun detachSession(
        token: MediaSession.Token
    ) {

        val holder =
            trackedSessions.remove(token)
                ?: return

        Log.d(
            "Wallpaper",
            "Detach ${holder.controller.packageName}"
        )

        holder.controller.unregisterCallback(
            holder.callback
        )
    }

    private fun evaluateActiveSession() {

        val best =
            trackedSessions.values
                .sortedByDescending {

                    when (it.lastState?.state) {

                        PlaybackState.STATE_PLAYING -> 100

                        PlaybackState.STATE_BUFFERING -> 90

                        PlaybackState.STATE_PAUSED -> 50

                        else -> 0
                    }
                }
                .firstOrNull()

        if (best == null) {

            activeToken = null

            isPlaying = false

            onStateUpdate()

            return
        }

        activeToken =
            best.controller.sessionToken

        val playing =
            best.lastState?.state ==
                    PlaybackState.STATE_PLAYING

        isPlaying = playing

        onStateUpdate()

        val metadata =
            best.lastMetadata ?: return

        extractBitmap(metadata)
    }

    private fun extractBitmap(
        metadata: MediaMetadata
    ) {
        val fingerprint = TrackFingerprint(
            metadata.getString(MediaMetadata.METADATA_KEY_TITLE),
            metadata.getString(MediaMetadata.METADATA_KEY_ARTIST),
            metadata.getLong(MediaMetadata.METADATA_KEY_DURATION)
        )

        if (fingerprint == lastFingerprint) {
            return
        }

        val title =
            metadata.getString(
                MediaMetadata.METADATA_KEY_TITLE
            )

        Log.d(
            "Wallpaper",
            "Extract bitmap: $title"
        )

        val art =
            metadata.getBitmap(
                MediaMetadata.METADATA_KEY_ART
            )

        val albumArt =
            metadata.getBitmap(
                MediaMetadata.METADATA_KEY_ALBUM_ART
            )

        val icon =
            metadata.getBitmap(
                MediaMetadata.METADATA_KEY_DISPLAY_ICON
            )

        val result =
            art ?: albumArt ?: icon ?: return

        lastFingerprint = fingerprint
        processAndPost(result)
    }

    private fun processAndPost(
        source: Bitmap
    ) {

        val bitmap =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                source.copy(Bitmap.Config.HARDWARE, false)
            } else {
                source
            }

        latestBitmap = bitmap

        onBitmapUpdate(bitmap)
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(updateReceiver)

        trackedSessions.values.forEach {

            it.controller.unregisterCallback(
                it.callback
            )
        }

        trackedSessions.clear()
    }
}
