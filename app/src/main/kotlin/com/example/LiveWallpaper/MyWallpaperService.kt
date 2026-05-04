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

        private var isVisible = false
        private var albumArt: Bitmap? = null
        
        init {
            MyNotificationListener.onBitmapUpdate = { bmp ->
                this.albumArt = bmp
                if (isVisible) {
                    drawFrame() 
                }
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.isVisible = visible
            if (visible) {
                drawFrame() // Перемальовуємо, щоб показати актуальну обкладинку при поверненні
            }
        }

        private var renderer: HardwareRenderer? = null
        private var renderNode: RenderNode? = null
        private var renderEffect: RenderEffect? = null

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            
            val frame = holder.surfaceFrame
            val width = frame.width()
            val height = frame.height()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                renderer = HardwareRenderer().apply {
                    setSurface(holder.surface)
                }

                renderNode = RenderNode("content").apply {
            // Використовуємо отримані розміри
                    setPosition(0, 0, width, height)
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        // Створюємо ефект
                        val blur = RenderEffect.createBlurEffect(120f, 120f, Shader.TileMode.CLAMP)
        
                        // ПРИЗНАЧАЄМО його вузлу (це метод класу RenderNode)
                    setRenderEffect(blur) 
        
                    // Якщо тобі все ще потрібна посилання в класі MyEngine:
                    this@MyEngine.renderEffect = blur
    }
                }
            }

            drawFrame()
        }
        
        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            // 💢 Ось тут ми виправляємо мій попередній закид про хардкод!
            renderNode?.setPosition(0, 0, width, height)
            drawFrame()
        }
        
        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            // КРИТИЧНО: зупиняємо рендер, щоб не було витоків
            renderer?.apply {
                stop()
                destroy()
            }
            renderer = null
        }

        private fun drawFrame() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return

            val node = renderNode ?: return
            val renderer = renderer ?: return

            val canvas = node.beginRecording()
            
            try {
            canvas.drawColor(Color.BLACK)
            
            albumArt?.let { bmp ->
            val viewWidth = node.width.toFloat()
            val viewHeight = node.height.toFloat()
            val bmpWidth = bmp.width.toFloat()
            val bmpHeight = bmp.height.toFloat()

            // 1. Обчислюємо коефіцієнт масштабування (щоб заповнити весь екран)
            val scale = Math.max(viewWidth / bmpWidth, viewHeight / bmpHeight)

            // 2. Визначаємо фінальні розміри після масштабування
            val finalWidth = bmpWidth * scale
            val finalHeight = bmpHeight * scale

            // 3. Центруємо: зміщуємо вліво/вгору на половину зайвого простору
            val left = (viewWidth - finalWidth) / 2f
            val top = (viewHeight - finalHeight) / 2f

            // 4. Створюємо RectF для малювання (RectF працює з Float)
            val destRect = RectF(left, top, left + finalWidth, top + finalHeight)

            // Малюємо з фільтрацією, щоб не було "пікселів" при розтягуванні
            canvas.drawBitmap(bmp, null, destRect, null)
        }
    }
            finally {
            node.endRecording()
        }
            renderer.setContentRoot(node)
            renderer.createRenderRequest()
                .setVsyncTime(System.nanoTime())
                .syncAndDraw()
        }
    }
}