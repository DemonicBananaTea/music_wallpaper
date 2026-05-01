package com.example.musicwallpaper

import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import android.graphics.*

class MainWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = EngineImpl()

    inner class EngineImpl : Engine() {

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            draw(holder)
        }

        private fun draw(holder: SurfaceHolder) {

            val canvas = holder.lockCanvas() ?: return

            canvas.drawColor(Color.BLUE) // 🔥 тест

            holder.unlockCanvasAndPost(canvas)
        }
    }
}