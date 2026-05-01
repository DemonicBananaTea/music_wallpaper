package com.example.musicwallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.graphics.Bitmap

class MainWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return MusicEngine()
    }

    inner class MusicEngine : Engine() {

        private val handler = Handler(Looper.getMainLooper())
        private var visible = false

        private val reader by lazy { MediaSessionReader(applicationContext) }

        // 🔥 ОНОВЛЕННЯ СЕСІЙ (рідко)
        private val updateRunnable = object : Runnable {
            override fun run() {
                if (visible) {
                    reader.update()
                    handler.postDelayed(this, 1500L) // 1.5 сек
                }
            }
        }

        // 🔥 РЕНДЕР (часто)
        private val drawRunnable = object : Runnable {
            override fun run() {
                if (visible) {
                    drawFrame()
                    handler.postDelayed(this, 33L) // ~30 FPS
                }
            }
        }

        override fun onVisibilityChanged(isVisible: Boolean) {
            visible = isVisible

            handler.removeCallbacks(updateRunnable)
            handler.removeCallbacks(drawRunnable)

            if (isVisible) {
                handler.post(updateRunnable)
                handler.post(drawRunnable)
            }
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            drawFrame()
        }

        override fun onSurfaceChanged(
            holder: SurfaceHolder,
            format: Int,
            width: Int,
            height: Int
        ) {
            super.onSurfaceChanged(holder, format, width, height)
            drawFrame()
        }

        override fun onDestroy() {
            super.onDestroy()
            handler.removeCallbacks(updateRunnable)
            handler.removeCallbacks(drawRunnable)
        }
        
        fun blurBitmap(src: Bitmap, radius: Float): Bitmap {
    val bitmap = Bitmap.createBitmap(
        src.width,
        src.height,
        Bitmap.Config.ARGB_8888
    )

    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // ❗ простий варіант (без RenderEffect)
    // поки що просто малюємо як є
    canvas.drawBitmap(src, 0f, 0f, paint)

    return bitmap
}

        private fun drawFrame() {
    val holder = surfaceHolder
    val canvas = holder.lockCanvas() ?: return

    try {
        val bmp = ArtworkStore.currentBitmap

        canvas.drawColor(Color.BLACK)

        if (bmp != null) {

            val canvasW = canvas.width.toFloat()
            val canvasH = canvas.height.toFloat()

            val bmpW = bmp.width.toFloat()
            val bmpH = bmp.height.toFloat()

            val scale = maxOf(canvasW / bmpW, canvasH / bmpH)

            val scaledW = bmpW * scale
            val scaledH = bmpH * scale

            val left = (canvasW - scaledW) / 2f
            val top = (canvasH - scaledH) / 2f

            val dst = RectF(
                left,
                top,
                left + scaledW,
                top + scaledH
            )

            val displayed = blurBitmap(bmp, 40f)

            canvas.drawBitmap(displayed, null, dst, null)

            val darkPaint = Paint().apply {
                color = Color.BLACK
                alpha = (255 * 0.30f).toInt()
            }

            canvas.drawRect(0f, 0f, canvasW, canvasH, darkPaint)
        }

    } finally {
        holder.unlockCanvasAndPost(canvas)
    }
}
    }
}