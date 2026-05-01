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

            if (visible) handler.post(drawRunner)
            else handler.removeCallbacks(drawRunner)
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
                val newBmp = ArtworkStore.currentBitmap?.copy(Bitmap.Config.ARGB_8888, false)

                val timeout = now - ArtworkStore.lastUpdateTime > 5000

                if (timeout) {
                    currentBitmap = null
                    previousBitmap = null
                } else {
                    if (newBmp != null && newBmp != currentBitmap) {
                        previousBitmap = currentBitmap
                        currentBitmap = newBmp
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

            } finally {
                try {
                    holder.unlockCanvasAndPost(canvas)
                } catch (_: Exception) {}
            }
        }

        private fun drawBitmap(canvas: Canvas, bitmap: Bitmap, alpha: Float) {

            if (bitmap.isRecycled) return
            if (bitmap.width <= 0 || bitmap.height <= 0) return

            val paint = Paint().apply {
                this.alpha = (alpha * 255).toInt().coerceIn(0, 255)
            }

            val dst = Rect(0, 0, canvas.width, canvas.height)

            canvas.drawBitmap(bitmap, null, dst, paint)
        }
    }
}