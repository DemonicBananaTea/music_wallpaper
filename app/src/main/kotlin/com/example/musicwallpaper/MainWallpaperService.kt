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

        private lateinit var reader: MediaSessionReader

        private val frame = object : Runnable {
            override fun run() {
                if (running) {
                    reader.update()
                    draw()
                    handler.postDelayed(this, 1000)
                }
            }
        }

        override fun onCreate(holder: SurfaceHolder) {
            super.onCreate(holder)
            reader = MediaSessionReader(applicationContext)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            running = visible
            if (visible) handler.post(frame)
            else handler.removeCallbacks(frame)
        }

        override fun onDestroy() {
            running = false
            handler.removeCallbacks(frame)
            super.onDestroy()
        }

        private fun draw() {
            val holder = surfaceHolder
            if (!holder.surface.isValid) return

            val canvas = holder.lockCanvas() ?: return

            try {
                canvas.drawColor(Color.BLACK)

                ArtworkStore.bitmap?.let { bmp ->
                    val dst = Rect(0, 0, canvas.width, canvas.height)
                    canvas.drawBitmap(bmp, null, dst, Paint())
                }

            } finally {
                try {
                    holder.unlockCanvasAndPost(canvas)
                } catch (_: Exception) {}
            }
        }
    }
}