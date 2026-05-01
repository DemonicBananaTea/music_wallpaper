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

        private val frame = object : Runnable {
            override fun run() {
                if (running) {
                    drawSafe()
                    handler.postDelayed(this, 16)
                }
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            running = visible
            if (visible) handler.post(frame)
            else handler.removeCallbacks(frame)
        }

        private fun drawSafe() {
            val holder = surfaceHolder
            if (!holder.surface.isValid) return

            val canvas = holder.lockCanvas() ?: return

            try {
                canvas.drawColor(Color.BLACK)

                val bmp = ArtworkStore.get()

                if (bmp != null) {
                    val dst = Rect(0, 0, canvas.width, canvas.height)
                    canvas.drawBitmap(bmp, null, dst, Paint())
                }

            } catch (e: Exception) {
                // 🔥 ніколи не даємо впасти wallpaper
            } finally {
                try {
                    holder.unlockCanvasAndPost(canvas)
                } catch (_: Exception) {}
            }
        }
    }
}