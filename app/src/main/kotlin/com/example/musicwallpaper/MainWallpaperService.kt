package com.example.musicwallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder

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

            val scale = minOf(canvasW / bmpW, canvasH / bmpH)

            val scaledW = bmpW * scale
            val scaledH = bmpH * scale

            val left = (canvasW - scaledW) / 2f
            val top = (canvasH - scaledH) / 2f

            val dst = android.graphics.RectF(
                left,
                top,
                left + scaledW,
                top + scaledH
            )

            // 🔥 BLUR (Android 12+)
            if (android.os.Build.VERSION.SDK_INT >= 31) {
                canvas.apply {
                    renderEffect = android.graphics.RenderEffect.createBlurEffect(
                        40f, 40f,
                        android.graphics.Shader.TileMode.CLAMP
                    )
                }
            }

            canvas.drawBitmap(bmp, null, dst, null)

            // ❗ вимикаємо blur для наступних кадрів
            if (android.os.Build.VERSION.SDK_INT >= 31) {
                canvas.renderEffect = null
            }

            // 🔥 затемнення 30%
            val darkPaint = android.graphics.Paint().apply {
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