package com.example.livewallpaper

import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import android.graphics.*
import android.os.Build
import android.graphics.RenderEffect
import android.graphics.Shader

class MyWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return MyEngine()
    }

    inner class MyEngine : Engine() {

        private val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 60f
        }

        private val blurPaint = Paint()

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            drawFrame(holder)
        }

        private fun drawFrame(holder: SurfaceHolder) {
            val canvas = holder.lockCanvas()

            // 1. Створюємо bitmap як "шар"
            val width = canvas.width
            val height = canvas.height

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val offscreenCanvas = Canvas(bitmap)

            // 2. Малюємо ТУДИ (не одразу на екран)
            offscreenCanvas.drawColor(Color.BLACK)
            offscreenCanvas.drawText("HELLO WALLPAPER", 100f, 200f, textPaint)

            // 3. Накладаємо blur (API 31+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                blurPaint.setRenderEffect(
                    RenderEffect.createBlurEffect(
                        40f, 40f,
                        Shader.TileMode.CLAMP
                    )
                )
            }

            // 4. Малюємо bitmap з блюром на реальний canvas
            canvas.drawBitmap(bitmap, 0f, 0f, blurPaint)

            holder.unlockCanvasAndPost(canvas)
        }
    }
}