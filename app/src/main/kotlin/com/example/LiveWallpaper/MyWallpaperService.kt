package com.example.livewallpaper

import android.graphics.*
import android.os.Build
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder

class MyWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return MyEngine()
    }

    inner class MyEngine : Engine() {

        private val paint = Paint().apply {
            color = Color.WHITE
            textSize = 60f
        }

        private var renderNode: RenderNode? = null

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                renderNode = RenderNode("blurNode").apply {
                    setPosition(0, 0, 1080, 1920)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    setRenderEffect(
                        RenderEffect.createBlurEffect(
                            40f, 40f,
                            Shader.TileMode.CLAMP
                        )
                    )
                }
            }

            drawFrame(holder)
        }

        private fun drawFrame(holder: SurfaceHolder) {
            val canvas = holder.lockCanvas()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && renderNode != null) {

                val recordingCanvas = renderNode!!.beginRecording()

                // малюємо ВНУТРІ renderNode
                recordingCanvas.drawColor(Color.BLACK)
                recordingCanvas.drawText("HELLO WALLPAPER", 100f, 200f, paint)

                renderNode!!.endRecording()

                // малюємо renderNode на екран
                canvas.drawRenderNode(renderNode!!)
            } else {
                // fallback
                canvas.drawColor(Color.BLACK)
                canvas.drawText("HELLO WALLPAPER", 100f, 200f, paint)
            }

            holder.unlockCanvasAndPost(canvas)
        }
    }
}