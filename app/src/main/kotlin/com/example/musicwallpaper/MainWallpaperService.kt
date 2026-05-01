package com.example.musicwallpaper

import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import android.graphics.*
import android.os.*

class MainWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = EngineImpl()

    inner class EngineImpl : Engine() {

        private val handler = Handler(Looper.getMainLooper())
        private var running = false

        private lateinit var mediaSession: MediaSessionListener

        private var currentBitmap: Bitmap? = null
        private var previousBitmap: Bitmap? = null
        private var transitionProgress = 1f

        private val drawRunner = object : Runnable {
            override fun run() {
                if (running) {
                    drawFrame()
                    handler.postDelayed(this, 33)
                }
            }
        }

        override fun onCreate(holder: SurfaceHolder) {
            super.onCreate(holder)

            mediaSession = MediaSessionListener(applicationContext)
            mediaSession.start()

            running = true
        }

        override fun onVisibilityChanged(visible: Boolean) {
            running = visible

            if (visible) {
                handler.post(drawRunner)
            } else {
                handler.removeCallbacks(drawRunner)
            }
        }

        override fun onDestroy() {
            running = false
            handler.removeCallbacks(drawRunner)
            super.onDestroy()
        }

        private fun drawFrame() {
            val holder = surfaceHolder
            if (!holder.surface.isValid) return

            val canvas = holder.lockCanvas() ?: return

            try {
                val now = System.currentTimeMillis()

                // safe read (anti race condition)
                val newBmp = ArtworkStore.currentBitmap?.copy(Bitmap.Config.ARGB_8888, false)

                // ⏱ idle timeout (єдине джерело очищення)
                val timeout = now - ArtworkStore.lastUpdateTime > 5000

                if (timeout) {
                    currentBitmap = null
                    previousBitmap = null
                } else {
                    if (newBmp != null && newBmp != currentBitmap) {
                        previousBitmap = currentBitmap
                        currentBitmap = blurAndDarkenSafe(newBmp)
                        transitionProgress = 0f
                    }
                }

                canvas.drawColor(Color.BLACK)

                val curr = currentBitmap
                val prev = previousBitmap

                if (curr != null) {
                    if (prev != null && transitionProgress < 1f) {
                        drawBitmap(canvas, prev, 1f - transitionProgress)
                        drawBitmap(canvas, curr, transitionProgress)
                        transitionProgress += 0.05f
                    } else {
                        drawBitmap(canvas, curr, 1f)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    holder.unlockCanvasAndPost(canvas)
                } catch (_: Exception) {
                    // ignore surface crash
                }
            }
        }

        private fun drawBitmap(canvas: Canvas, bitmap: Bitmap, alpha: Float) {

            if (bitmap.isRecycled) return
            if (bitmap.width <= 0 || bitmap.height <= 0) return

            val paint = Paint().apply {
                this.alpha = (alpha * 255).toInt().coerceIn(0, 255)
            }

            val canvasRatio = canvas.width.toFloat() / canvas.height
            val bitmapRatio = bitmap.width.toFloat() / bitmap.height

            val srcRect: Rect
            val dstRect = Rect(0, 0, canvas.width, canvas.height)

            if (bitmapRatio > canvasRatio) {
                val newWidth = (bitmap.height * canvasRatio).toInt()
                val xOffset = (bitmap.width - newWidth) / 2
                srcRect = Rect(xOffset, 0, xOffset + newWidth, bitmap.height)
            } else {
                val newHeight = (bitmap.width / canvasRatio).toInt()
                val yOffset = (bitmap.height - newHeight) / 2
                srcRect = Rect(0, yOffset, bitmap.width, yOffset + newHeight)
            }

            canvas.drawBitmap(bitmap, srcRect, dstRect, paint)
        }

        private fun blurAndDarkenSafe(src: Bitmap): Bitmap {

            // downscale for safety (avoids OOM)
            val safeSrc = Bitmap.createScaledBitmap(src, 300, 300, true)

            val bitmap = safeSrc.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(bitmap)
            val paint = Paint()

            // fake blur (stable across all Android versions)
            for (i in 1..4) {
                paint.alpha = 25
                canvas.drawBitmap(bitmap, i.toFloat(), i.toFloat(), paint)
                canvas.drawBitmap(bitmap, -i.toFloat(), i.toFloat(), paint)
                canvas.drawBitmap(bitmap, i.toFloat(), -i.toFloat(), paint)
                canvas.drawBitmap(bitmap, -i.toFloat(), -i.toFloat(), paint)
            }

            val dark = Paint().apply {
                color = Color.argb(140, 0, 0, 0)
            }

            canvas.drawRect(
                0f, 0f,
                bitmap.width.toFloat(),
                bitmap.height.toFloat(),
                dark
            )

            return bitmap
        }
    }
}