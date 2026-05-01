package com.example.musicwallpaper

import android.graphics.Canvas
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder

class MusicWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return Engine()
    }

    inner class Engine : WallpaperService.Engine() {

        override fun onDraw(surfaceHolder: SurfaceHolder) {
            val canvas: Canvas = surfaceHolder.lockCanvas() ?: return

            try {
                val bmp = ArtworkStore.currentBitmap

                if (bmp == null) {
                    canvas.drawColor(android.graphics.Color.BLACK)
                } else {
                    canvas.drawBitmap(bmp, 0f, 0f, null)
                }

            } finally {
                surfaceHolder.unlockCanvasAndPost(canvas)
            }
        }
    }
}