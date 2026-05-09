package com.example.livewallpaper
import android.os.Handler
import android.os.Looper
import android.graphics.*
import android.os.Build
import android.service.wallpaper.WallpaperService
import android.view.Surface
import android.view.SurfaceHolder
import android.graphics.HardwareRenderer
import android.graphics.RenderNode
import android.graphics.RenderEffect
import android.graphics.Shader
import android.util.Log
import com.example.livewallpaper.MyNotificationListener

class MyWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = MyEngine()

    inner class MyEngine : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private var isVisible = false
        private var albumArt: Bitmap? = null
        private var albumArtOld: Bitmap? = null
        private val shadowPaint = Paint().apply {
            colorFilter = PorterDuffColorFilter(Color.argb(100, 0, 0, 0), PorterDuff.Mode.SRC_ATOP)
        }
        private val destRect = RectF() // Створюємо один раз
        private var currentAlpha = 0f         // Поточна яскравість (0.0 до 1.0)
        val transitionSpeed = 0.05f    // Швидкість зміни (чим менше, тим повільніше)
        var maxBrightness = 0.6f
        private var isAnimating = false       // Чи треба продовжувати цикл перемальовування
        private var crossfadeAlpha = 0f

        init {
            albumArt = MyNotificationListener.latestBitmap
            albumArt?.let { bmp ->
                updateScaleParams(bmp)
            }
            if (MyNotificationListener.isPlaying == true) {
                drawFrame()
            }

            MyNotificationListener.onStateChanged = {
                Log.d("WallpaperLog", "State changed, calling drawFrame()")
                drawFrame() 
            }
            
            MyNotificationListener.onBitmapUpdate = { newBmp ->
                Log.d("WallpaperLogFade", "Лямбда спрацювала! Новий трек!")
                albumArtOld = albumArt
                albumArt = newBmp
                crossfadeAlpha = 0f
                updateScaleParams(newBmp)
                drawFrame() 
            }
        }
        override fun onCreate(surfaceHolder: SurfaceHolder) {
    super.onCreate(surfaceHolder)

    // МИ ПРИЗНАЧАЄМО ФУНКЦІЮ, А НЕ ВИКЛИКАЄМО ЇЇ

}

        override fun onVisibilityChanged(visible: Boolean) {
            this.isVisible = visible
            if (visible) {
                MyNotificationListener.latestBitmap?.let { bmp ->
                    this.albumArt = bmp
                }
                drawFrame() // Перемальовуємо, щоб показати актуальну обкладинку при поверненні
            }
        }

        private var renderer: HardwareRenderer? = null
        private var renderNode: RenderNode? = null
        private var renderEffect: RenderEffect? = null

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            
            val frame = holder.surfaceFrame
            val width = frame.width()
            val height = frame.height()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                renderer = HardwareRenderer().apply {
                    setSurface(holder.surface)
                }

                renderNode = RenderNode("content").apply {
            // Використовуємо отримані розміри
                    setPosition(0, 0, width, height)
                    
                    albumArt?.let { bmp ->
                        updateScaleParams(bmp)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        // Створюємо ефект
                        val blur = RenderEffect.createBlurEffect(120f, 120f, Shader.TileMode.CLAMP)
        
                        // ПРИЗНАЧАЄМО його вузлу (це метод класу RenderNode)
                    setRenderEffect(blur) 
        
                    // Якщо тобі все ще потрібна посилання в класі MyEngine:
                    this@MyEngine.renderEffect = blur
    }
                }
            }

            drawFrame()
        }
        
        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            // 💢 Ось тут ми виправляємо мій попередній закид про хардкод!
            renderNode?.setPosition(0, 0, width, height)
            albumArt?.let { bmp ->
                updateScaleParams(bmp)
            }
            drawFrame()
        }
        
        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            // КРИТИЧНО: зупиняємо рендер, щоб не було витоків
            renderer?.apply {
                stop()
                destroy()
            }
            renderer = null
        }

        override fun onDestroy() {
            super.onDestroy()
            //MyNotificationListener.isPlaying = null
            //MyNotificationListener.onBitmapUpdate = null // Звільняємо ресурси
            //MyNotificationListener.onStateChanged = null
        }

        private fun scheduleNextFrame() {
            handler.removeCallbacksAndMessages(null) 
            if (isAnimating) {
                handler.postDelayed({ drawFrame() }, 33)
            }
        }

        fun UpdateAlbumArtCrossfade(newBmp: Bitmap?){
            if (albumArt == newBmp) return // Нічого не міняємо
                albumArtOld = albumArt // Стара стає попередньою
                albumArt = newBmp       // Нова займає трон
                crossfadeAlpha = 0f               // Починаємо з повної прозорості нової
                drawFrame()
        }

        private fun updateScaleParams(bmp: Bitmap) {
            val viewWidth = renderNode?.width?.toFloat() ?: return
            val viewHeight = renderNode?.height?.toFloat() ?: return
            
            val scale = Math.max(viewWidth / bmp.width, viewHeight / bmp.height)
            val finalWidth = bmp.width * scale
            val finalHeight = bmp.height * scale
            val left = (viewWidth - finalWidth) / 2f
            val top = (viewHeight - finalHeight) / 2f
            
            destRect.set(left, top, left + finalWidth, top + finalHeight)
        }

        private fun scaleAndDraw(canvas: Canvas, bmp: Bitmap, alphaFactor: Float) {
            shadowPaint.alpha = (alphaFactor * 255).toInt()
            canvas.drawBitmap(bmp, null, destRect, shadowPaint)
        }
 
        private fun drawFrame() {
if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
    val node = renderNode ?: return
    val renderer = renderer ?: return

    // --- 1. ЛОГІКА КРОСФЕЙДУ (Зміна треку) ---
    if (crossfadeAlpha < 1f) {
        crossfadeAlpha = (crossfadeAlpha + 0.03f).coerceAtMost(1f)
        isAnimating = true
    }

    // --- 2. ЛОГІКА ЯСКРАВОСТІ (Плей / Пауза) ---
    val targetAlpha = if (MyNotificationListener.isPlaying == true) maxBrightness else 0f
    if (Math.abs(currentAlpha - targetAlpha) > 0.001f) {
        if (currentAlpha < targetAlpha) currentAlpha += transitionSpeed
        else currentAlpha -= transitionSpeed
        currentAlpha = currentAlpha.coerceIn(0f, maxBrightness)
        isAnimating = true
    } else {
        currentAlpha = targetAlpha
        // Тут ми не ставимо isAnimating = false відразу, 
        // бо може ще йти кросфейд!
    }

    // Якщо обидві анімації закінчились — зупиняємось
    if (crossfadeAlpha >= 1f && Math.abs(currentAlpha - targetAlpha) <= 0.001f) {
        isAnimating = false
    }

    val canvas = node.beginRecording()
    try {
        canvas.drawColor(Color.BLACK)

        // Множимо прозорість кросфейду на загальну яскравість
        // currentAlpha тут працює як "майстер-слайдер"

        // 1. Малюємо СТАРУ обкладинку (вона зникає)
        albumArtOld?.let { oldBmp ->
            // Стара картинка має зникати, поки нова з'являється
            val oldAlpha = (1f - crossfadeAlpha) * currentAlpha
            if (oldAlpha > 0.01f) {
                scaleAndDraw(canvas, oldBmp, oldAlpha)
            }
        }

        // 2. Малюємо НОВУ обкладинку (вона з'являється)
        albumArt?.let { newBmp ->
            val newAlpha = crossfadeAlpha * currentAlpha
            if (newAlpha > 0.01f) {
                scaleAndDraw(canvas, newBmp, newAlpha)
            }
        }

    } finally {
        node.endRecording()
    } 
    renderer.setContentRoot(node)
            renderer.createRenderRequest()
            .setVsyncTime(System.nanoTime())
            .syncAndDraw()
            if (isAnimating) {
                scheduleNextFrame()
            }
        }
    }
}
