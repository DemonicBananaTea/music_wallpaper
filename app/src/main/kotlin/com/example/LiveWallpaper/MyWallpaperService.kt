package com.example.livewallpaper

import android.graphics.*
import android.os.Build
import android.service.wallpaper.WallpaperService
import android.view.Surface
import android.view.SurfaceHolder
import android.graphics.HardwareRenderer
import android.graphics.RenderNode
import android.graphics.RenderEffect
import android.graphics.Shader

class MyWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = MyEngine()

    inner class MyEngine : Engine() {

        private val paint = Paint().apply {
            color = Color.WHITE
            textSize = 60f
        }

        private var renderer: HardwareRenderer? = null
        private var renderNode: RenderNode? = null
        private var renderEffect: RenderEffect? = null

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                renderer = HardwareRenderer().apply {
                    setSurface(holder.surface)
                }

                renderNode = RenderNode("content").apply {
                    setPosition(0, 0, 1080, 1920)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        // 👇 ОЦЕ ПРАЦЮЄ
                        renderEffect = RenderEffect.createBlurEffect(
                            40f, 40f, Shader.TileMode.CLAMP
                        )
                    }
                }
            }

            drawFrame()
        }

        private fun drawFrame() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return

            val node = renderNode ?: return
            val renderer = renderer ?: return

            val canvas = node.beginRecording()

            // малюєш як хочеш
            canvas.drawColor(Color.BLACK)
            canvas.drawText("HELLO WALLPAPER", 100f, 200f, paint)

            node.endRecording()

            renderer.setContentRoot(node)
            renderer.createRenderRequest()
                .setVsyncTime(System.nanoTime())
                .syncAndDraw()
        }
    }
}