package com.example.musicwallpaper

import android.util.Log
import android.content.ComponentName
import android.content.Context
import android.media.session.MediaSessionManager
import android.media.session.MediaController

class MediaSessionReader(private val context: Context) {

    private val manager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
    private val component = ComponentName(context, DummyNotificationListener::class.java)

    // Зберігаємо заголовок останнього треку, щоб не перемальовувати те саме
    private var lastTrackTitle: String? = null

    fun update() {
        val allowed = Settings.getAllowedApps(context)
        val sessions = manager.getActiveSessions(component)

        // Шукаємо перший дозволений контролер
        val controller = sessions.firstOrNull { allowed.contains(it.packageName) }

        if (controller == null) {
            if (lastTrackTitle != null) {
                ArtworkStore.currentBitmap = null
                lastTrackTitle = null
            }
            return
        }

        // Отримуємо опис (MediaDescription)
        val description = controller.metadata?.description
        val currentTitle = description?.title?.toString()

        // ПЕРЕВІРКА: якщо назва пісні та сама — ігноруємо оновлення
        if (currentTitle == lastTrackTitle) {
            return
        }

        Log.d("MediaReader", "Новий трек: $currentTitle. Оновлюємо картинку.")

        // Використовуємо твій робочий спосіб отримання бітмапа
        val bmp = description?.iconBitmap

        ArtworkStore.currentBitmap = Blur.fastBlur(bmp, 0.25f, 12)
        lastTrackTitle = currentTitle
    }
}
