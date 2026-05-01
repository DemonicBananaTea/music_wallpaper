package com.example.musicwallpaper

import android.graphics.*
import android.os.*
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

        private val updateRunnable = object : Runnable {
            override fun run() {
                if (visible) {
                    reader.update()
                    handler.postDelayed(this, 1500L)
                }
            }
        }

        private val drawRunnable = object : Runnable {
            override fun run() {
                if (visible) {
                    drawFrame()
                    handler.postDelayed(this, 33L)
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

private fun blurBitmapFast(src: Bitmap): Bitmap {
    val small = Bitmap.createScaledBitmap(
        src,
        src.width / 6,
        src.height / 6,
        true
    )
    return Bitmap.createScaledBitmap(
        small,
        src.width,
        src.height,
        true
    )
}

        private fun drawFrame() {
    val holder = surfaceHolder
    val canvas = holder.lockCanvas() ?: return

    try {
        val bmp = ArtworkStore.currentBitmap

        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()

        canvas.drawColor(Color.BLACK)

        if (bmp != null) {

            val blurred = blurBitmapFast(bmp)

            val scale = maxOf(w / blurred.width, h / blurred.height)

            val dst = RectF(
                (w - blurred.width * scale) / 2f,
                (h - blurred.height * scale) / 2f,
                (w + blurred.width * scale) / 2f,
                (h + blurred.height * scale) / 2f
            )

            canvas.drawBitmap(blurred, null, dst, null)

            // 🔥 затемнення 30%
            val dark = Paint().apply {
                color = Color.BLACK
                alpha = 77
            }

            canvas.drawRect(0f, 0f, w, h, dark)
        }

    } finally {
        holder.unlockCanvasAndPost(canvas)
    }
}
    }
}