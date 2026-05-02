package com.example.livewallpaper

import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class MyWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return MyEngine()
    }

    inner class MyEngine : Engine() {

        private val paint = Paint().apply {
            color = Color.WHITE
            textSize = 60f
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            drawFrame(holder)
        }

        private fun drawFrame(holder: SurfaceHolder) {
            val canvas: Canvas = holder.lockCanvas()
            canvas.drawColor(Color.BLACK)
            canvas.drawText("HELLO WALLPAPER", 100f, 200f, paint)
            holder.unlockCanvasAndPost(canvas)
        }
    }
}