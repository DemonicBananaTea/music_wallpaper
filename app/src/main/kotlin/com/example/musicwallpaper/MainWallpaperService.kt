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


        private fun drawFrame() {
    val holder = surfaceHolder
    val canvas = holder.lockCanvas() ?: return

    try {
        val bmp = ArtworkStore.currentBitmap

        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()

        

        if (bmp != null) {

            val scale = maxOf(w / bmp.width, h / bmp.height)

            val dst = RectF(
                (w - bmp.width * scale) / 2f,
                (h - bmp.height * scale) / 2f,
                (w + bmp.width * scale) / 2f,
                (h + bmp.height * scale) / 2f
            )

            canvas.drawBitmap(bmp, null, dst, null)

            // 🔥 затемнення 30%
            val dark = Paint().apply {
                color = Color.BLACK
                alpha = 50
            }

            canvas.drawRect(0f, 0f, w, h, dark)
        }
        canvas.drawColor(Color.BLACK)

    } finally {
        holder.unlockCanvasAndPost(canvas)
    }
}
    }
}