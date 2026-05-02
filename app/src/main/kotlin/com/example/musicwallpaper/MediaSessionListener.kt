class MediaSessionReader(private val context: Context) {

    private val manager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
    private val component = ComponentName(context, DummyNotificationListener::class.java)

    // Зберігаємо ID останнього обробленого треку
    private var lastTrackId: String? = null
    private var lastPackageName: String? = null

    fun update() {
        val allowed = Settings.getAllowedApps(context)
        val sessions = manager.getActiveSessions(component)

        val controller = sessions.firstOrNull { allowed.contains(it.packageName) }

        if (controller == null) {
            if (lastPackageName != null) {
                ArtworkStore.currentBitmap = null
                lastTrackId = null
                lastPackageName = null
            }
            return
        }

        val metadata = controller.metadata
        // Отримуємо унікальний ID пісні або комбінацію назва+автор
        val currentTrackId = metadata?.getString(MediaMetadata.METADATA_KEY_MEDIA_ID) 
                             ?: metadata?.description?.title?.toString()

        // ПОРІВНЯННЯ: якщо пакет той самий і ID пісні той самий — виходимо
        if (controller.packageName == lastPackageName && currentTrackId == lastTrackId) {
            return
        }

        Log.d("MediaReader", "Оновлюємо обкладинку для: $currentTrackId")

        // Отримуємо бітмап (спробуй METADATA_KEY_ALBUM_ART для кращої якості)
        val bmp = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART) 
                  ?: metadata?.description?.iconBitmap

        ArtworkStore.currentBitmap = bmp
        
        // Оновлюємо стан
        lastTrackId = currentTrackId
        lastPackageName = controller.packageName
    }
}
