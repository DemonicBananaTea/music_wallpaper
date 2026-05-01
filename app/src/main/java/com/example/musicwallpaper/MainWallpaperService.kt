package com.example.musicwallpaper

import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import android.graphics.*
import android.os.*

class MainWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = EngineImpl()

    inner class EngineImpl : Engine() {

        private val handler = Handler(Looper.getMainLooper())

        private var currentBitmap: Bitmap? = null
        private var previousBitmap: Bitmap? = null
        private var transitionProgress = 1f

        private val drawRunner = object : Runnable {
            override fun run() {
                draw()
                handler.postDelayed(this, 33)
            }
        }

        override fun onCreate(holder: SurfaceHolder) {
            super.onCreate(holder)
            handler.post(drawRunner)
        }

        private fun draw() {
            val canvas = surfaceHolder.lockCanvas() ?: return

            try {
                val now = System.currentTimeMillis()
                val newBmp = ArtworkStore.currentBitmap

                if (newBmp != null && newBmp != currentBitmap) {
                    previousBitmap = currentBitmap
                    currentBitmap = blurAndDarken(newBmp)
                    transitionProgress = 0f
                }

                if (now - ArtworkStore.lastUpdateTime > 3000) {
                    currentBitmap = null
                }

                canvas.drawColor(Color.BLACK)

                currentBitmap?.let {
                    if (previousBitmap != null && transitionProgress < 1f) {

                        drawBitmap(canvas, previousBitmap!!, 1f - transitionProgress)
                        drawBitmap(canvas, it, transitionProgress)

                        transitionProgress += 0.05f
                    } else {
                        drawBitmap(canvas, it, 1f)
                    }
                }

            } finally {
                surfaceHolder.unlockCanvasAndPost(canvas)
            }
        }

        private fun drawBitmap(canvas: Canvas, bitmap: Bitmap, alpha: Float) {
            val paint = Paint().apply {
                this.alpha = (alpha * 255).toInt()
            }

            val rect = Rect(0, 0, canvas.width, canvas.height)
            canvas.drawBitmap(bitmap, null, rect, paint)
        }

        private fun blurAndDarken(src: Bitmap): Bitmap {
            val scaled = Bitmap.createScaledBitmap(src, 100, 100, true)

            val blur = Bitmap.createScaledBitmap(
                scaled,
                src.width,
                src.height,
                true
            )

            val canvas = Canvas(blur)

            val paint = Paint()
            paint.color = Color.argb((0.3f * 255).toInt(), 0, 0, 0)

            canvas.drawRect(0f, 0f, blur.width.toFloat(), blur.height.toFloat(), paint)

            return blur
        }
    }
}
