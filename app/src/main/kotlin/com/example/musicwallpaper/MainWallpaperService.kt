package com.example.musicwallpaper

import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import android.graphics.*
import android.os.*
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.Canvas

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
        this.alpha = (alpha * 255).toInt()
    }

    val canvasRatio = canvas.width.toFloat() / canvas.height
    val bitmapRatio = bitmap.width.toFloat() / bitmap.height

    val srcRect: Rect
    val dstRect = Rect(0, 0, canvas.width, canvas.height)

    if (bitmapRatio > canvasRatio) {
        // bitmap ширший → обрізаємо по ширині
        val newWidth = (bitmap.height * canvasRatio).toInt()
        val xOffset = (bitmap.width - newWidth) / 2
        srcRect = Rect(xOffset, 0, xOffset + newWidth, bitmap.height)
    } else {
        // bitmap вищий → обрізаємо по висоті
        val newHeight = (bitmap.width / canvasRatio).toInt()
        val yOffset = (bitmap.height - newHeight) / 2
        srcRect = Rect(0, yOffset, bitmap.width, yOffset + newHeight)
    }

    canvas.drawBitmap(bitmap, srcRect, dstRect, paint)
}

        private fun blurAndDarkenSafe(src: Bitmap): Bitmap {
    val bitmap = src.copy(Bitmap.Config.ARGB_8888, true)

    // простий багатошаровий blur ефект
    val canvas = Canvas(bitmap)
    val paint = Paint()

    // багаторазове накладання з оффсетом = “blur”
    for (i in 1..6) {
        paint.alpha = 40
        canvas.drawBitmap(bitmap, i.toFloat(), i.toFloat(), paint)
        canvas.drawBitmap(bitmap, -i.toFloat(), i.toFloat(), paint)
        canvas.drawBitmap(bitmap, i.toFloat(), -i.toFloat(), paint)
        canvas.drawBitmap(bitmap, -i.toFloat(), -i.toFloat(), paint)
    }

    // затемнення
    val darkPaint = Paint().apply {
        color = Color.argb(140, 0, 0, 0)
    }

    canvas.drawRect(
        0f, 0f,
        bitmap.width.toFloat(),
        bitmap.height.toFloat(),
        darkPaint
    )

    return bitmap
}
    }
}
