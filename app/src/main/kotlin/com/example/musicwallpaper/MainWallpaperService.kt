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
                    drawFrame()
                    handler.postDelayed(this, 16)
                }
            }
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

        private fun drawFrame() {
            val holder = surfaceHolder
            if (!holder.surface.isValid) return

            val canvas = holder.lockCanvas() ?: return

            try {
                // базовий фон
                canvas.drawColor(Color.BLACK)

                // картинка якщо є
                ArtworkStore.bitmap?.let { bmp ->
                    val dst = Rect(0, 0, canvas.width, canvas.height)
                    canvas.drawBitmap(bmp, null, dst, Paint())
                }

            } finally {
                holder.unlockCanvasAndPost(canvas)
            }
        }
    }
}