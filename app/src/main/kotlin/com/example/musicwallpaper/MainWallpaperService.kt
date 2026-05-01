package com.example.musicwallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import android.graphics.RenderEffect
import android.graphics.Shader
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
        
        fun blurBitmap(src: Bitmap, radius: Int): Bitmap {

    // 🔥 1. зменшуємо (це ключ до “красивого blur”)
    val smallW = src.width / 4
    val smallH = src.height / 4

    val small = Bitmap.createScaledBitmap(src, smallW, smallH, true)

    val bitmap = small.copy(Bitmap.Config.ARGB_8888, true)

    val w = bitmap.width
    val h = bitmap.height

    val pixels = IntArray(w * h)
    bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

    val div = radius.coerceIn(1, 8)

    val tmp = IntArray(pixels.size)

    for (y in 0 until h) {
        for (x in 0 until w) {

            var r = 0
            var g = 0
            var b = 0
            var count = 0

            for (dy in -div..div) {
                for (dx in -div..div) {
                    val nx = x + dx
                    val ny = y + dy

                    if (nx in 0 until w && ny in 0 until h) {
                        val idx = ny * w + nx
                        val color = pixels[idx]

                        r += (color shr 16) and 0xff
                        g += (color shr 8) and 0xff
                        b += color and 0xff
                        count++
                    }
                }
            }

            val i = y * w + x
            tmp[i] =
                (0xff shl 24) or
                ((r / count) shl 16) or
                ((g / count) shl 8) or
                (b / count)
        }
    }

    bitmap.setPixels(tmp, 0, w, 0, 0, w, h)

    // 🔥 2. повертаємо назад у великий розмір
    return Bitmap.createScaledBitmap(bitmap, src.width, src.height, true)
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

            val dst = android.graphics.RectF(
                left,
                top,
                left + scaledW,
                top + scaledH
            )

            // 🔥 РОЗМИТИЙ ФОН
            val blurred = blurBitmap(bmp, 6)

            canvas.drawBitmap(blurred, null, dst, null)

            // 🔥 ЗАТЕМНЕННЯ 30%
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