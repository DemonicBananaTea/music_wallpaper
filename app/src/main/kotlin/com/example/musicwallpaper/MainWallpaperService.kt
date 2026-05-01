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
        private val frameRunnable = object : Runnable {
            override fun run() {
                if (visible) {
                    reader.update()
                    drawFrame()
                    handler.postDelayed(this, 1000L / 30L) // ~30 FPS
                }
            }
        }

        override fun onVisibilityChanged(isVisible: Boolean) {
            visible = isVisible

            if (isVisible) {
                handler.post(frameRunnable)
            } else {
                handler.removeCallbacks(frameRunnable)
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
            handler.removeCallbacks(frameRunnable)
        }

        private fun drawFrame() {
            val holder = surfaceHolder
            val canvas: Canvas = holder.lockCanvas() ?: return

            try {
                val bmp = ArtworkStore.currentBitmap

                if (bmp == null) {
                    canvas.drawColor(Color.BLACK)
                } else {
                    canvas.drawColor(Color.BLACK) // фон під картинкою
                    canvas.drawBitmap(bmp, 0f, 0f, null)
                }

            } finally {
                holder.unlockCanvasAndPost(canvas)
            }
        }
    }
}