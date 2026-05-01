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
                val newBmp = ArtworkStore.currentBitmap

                if (newBmp != null && newBmp != currentBitmap) {
                    previousBitmap = currentBitmap
                    currentBitmap = blurAndDarkenSafe(newBmp)
                    transitionProgress = 0f
                }

                if (now - ArtworkStore.lastUpdateTime > 3000) {
                    currentBitmap = null
                    previousBitmap = null
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
                // щоб не падав wallpaper через один збій
                e.printStackTrace()
            } finally {
                try {
                    holder.unlockCanvasAndPost(canvas)
                } catch (_: Exception) {
                    // ignore surface already destroyed
                }
            }
        }

        private fun drawBitmap(canvas: Canvas, bitmap: Bitmap, alpha: Float) {
            val paint = Paint().apply {
                this.alpha = (alpha * 255).toInt().coerceIn(0, 255)
            }

            val rect = Rect(0, 0, canvas.width, canvas.height)
            canvas.drawBitmap(bitmap, null, rect, paint)
        }

        private fun blurAndDarkenSafe(src: Bitmap): Bitmap {
            val safeSrc = try {
                if (src.config != null) {
                    src.copy(src.config, true)
                } else {
                    src.copy(Bitmap.Config.ARGB_8888, true)
                }
            } catch (e: Exception) {
                return src
            }

            val scaled = Bitmap.createScaledBitmap(safeSrc, 100, 100, true)

            val result = Bitmap.createScaledBitmap(
                scaled,
                src.width,
                src.height,
                true
            )

            val canvas = Canvas(result)

            val paint = Paint().apply {
                color = Color.argb((0.3f * 255).toInt(), 0, 0, 0)
            }

            canvas.drawRect(
                0f, 0f,
                result.width.toFloat(),
                result.height.toFloat(),
                paint
            )

            return result
        }
    }
}