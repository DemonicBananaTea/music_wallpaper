package com.example.musicwallpaper

import android.graphics.Canvas
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder

class MainWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return MusicEngine()
    }

    inner class MusicEngine : Engine() {

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            drawFrame(holder)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) {
                drawFrame(surfaceHolder)
            }
        }

        override fun onSurfaceChanged(
            holder: SurfaceHolder,
            format: Int,
            width: Int,
            height: Int
        ) {
            super.onSurfaceChanged(holder, format, width, height)
            drawFrame(holder)
        }

        private fun drawFrame(holder: SurfaceHolder) {
            val canvas: Canvas = holder.lockCanvas() ?: return

            try {
                val bmp = ArtworkStore.currentBitmap

                if (bmp == null) {
                    canvas.drawColor(android.graphics.Color.BLACK)
                } else {
                    canvas.drawBitmap(bmp, 0f, 0f, null)
                }

            } finally {
                holder.unlockCanvasAndPost(canvas)
            }
        }
    }
}