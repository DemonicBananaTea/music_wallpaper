package com.example.livewallpaper

import android.os.Handler
import android.os.Looper
import android.graphics.*
import android.os.Build
import android.service.wallpaper.WallpaperService
import android.view.Surface
import android.view.SurfaceHolder
import android.graphics.HardwareRenderer
import android.graphics.RenderNode
import android.graphics.RenderEffect
import android.graphics.Shader
import android.util.Log
import com.example.livewallpaper.MyNotificationListener

class MyWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = MyEngine()

    inner class MyEngine : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private var isVisible = false
        private var albumArt: Bitmap? = null
        private var albumArtOld: Bitmap? = null
        private val shadowPaint = Paint().apply {}
        private val destRect = RectF()
        private var currentAlpha = 0f
        val transitionSpeed = 0.05f
        var maxBrightness = 0.6f
        private var isAnimating = false
        private var crossfadeAlpha = 0f
        private val onBitmapUpdateListener = { bitmap: Bitmap ->
            UpdateAlbumArtCrossfade(bitmap)
        }
        private val onStateUpdateListener = { ->
            drawFrame()
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
        super.onCreate(surfaceHolder)
        MyNotificationListener.subscribeBitmap(onBitmapUpdateListener)
        MyNotificationListener.subscribeState(onStateUpdateListener)
        MyNotificationListener.latestBitmap?.let { bmp ->
            this.albumArt = bmp
            this.currentAlpha = if (MyNotificationListener.isPlaying == true) 1f else 0f
            this.crossfadeAlpha = 1f
        }
        }
        override fun onVisibilityChanged(visible: Boolean) {
            this.isVisible = visible
            if (visible) {
                MyNotificationListener.latestBitmap?.let { bmp ->
                    this.albumArt = bmp
                }
                updateRenderEffect() 
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
                    setPosition(0, 0, width, height)
                    albumArt?.let { bmp ->
                        updateScaleParams(bmp)
                    }
                }
            }
            updateRenderEffect()
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            renderNode?.setPosition(0, 0, width, height)
            albumArt?.let { bmp ->
                updateScaleParams(bmp)
            }
            drawFrame()
        }
        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            renderer?.apply {
                stop()
                destroy()
            }
            renderer = null
        }
        override fun onDestroy() {
            super.onDestroy()
            MyNotificationListener.unsubscribeBitmap(onBitmapUpdateListener)
            MyNotificationListener.unsubscribeState(onStateUpdateListener)
        }

private fun updateRenderEffect() {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        val prefs = getSharedPreferences("WallpaperSettings", android.content.Context.MODE_PRIVATE)
        val isBlurEnabled = prefs.getBoolean("use_blur", true)
        val radius = prefs.getInt("blur_radius", 120).toFloat().coerceAtLeast(1f)
        if (prefs.getBoolean("use_dark", true) == true) {
            maxBrightness = 1f - prefs.getFloat("darken_str", 0.4f).coerceAtMost(1f)
        }
        else {
            maxBrightness = 1f
        }
        renderNode?.let { node ->
            if (isBlurEnabled) {
                val blur = android.graphics.RenderEffect.createBlurEffect(
                    radius, radius, android.graphics.Shader.TileMode.CLAMP
                )
                node.setRenderEffect(blur)
            } else {
                node.setRenderEffect(null)
            }
        }
        drawFrame()
    }
}

        private fun scheduleNextFrame() {
            handler.removeCallbacksAndMessages(null) 
            if (isAnimating) {
                handler.postDelayed({ drawFrame() }, 33)
            }
        }
        fun UpdateAlbumArtCrossfade(newBmp: Bitmap?){
            if (albumArt == newBmp) return // Нічого не міняємо
                albumArtOld = albumArt // Стара стає попередньою
                albumArt = newBmp       // Нова займає трон
                crossfadeAlpha = 0f               // Починаємо з повної прозорості нової
                newBmp?.let { bmp ->
                    updateScaleParams(bmp)
                }
                drawFrame()
        }

        private fun updateScaleParams(bmp: Bitmap) {
            val viewWidth = renderNode?.width?.toFloat() ?: return
            val viewHeight = renderNode?.height?.toFloat() ?: return
            val scale = Math.max(viewWidth / bmp.width, viewHeight / bmp.height)
            val finalWidth = bmp.width * scale
            val finalHeight = bmp.height * scale
            val left = (viewWidth - finalWidth) / 2f
            val top = (viewHeight - finalHeight) / 2f
            destRect.set(left, top, left + finalWidth, top + finalHeight)
        }

        private fun scaleAndDraw(canvas: Canvas, bmp: Bitmap, alphaFactor: Float) {
            shadowPaint.alpha = (alphaFactor * 255).toInt()
            canvas.drawBitmap(bmp, null, destRect, shadowPaint)
        }
 
        private fun drawFrame() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
            val node = renderNode ?: return
            val renderer = renderer ?: return
            if (crossfadeAlpha < 1f) {
                crossfadeAlpha = (crossfadeAlpha + 0.03f).coerceAtMost(1f)
                isAnimating = true
            }
        
            val targetAlpha = if (MyNotificationListener.isPlaying == true) maxBrightness else 0f
            if (Math.abs(currentAlpha - targetAlpha) > 0.001f) {
                if (currentAlpha < targetAlpha) currentAlpha += transitionSpeed
                else currentAlpha -= transitionSpeed
                currentAlpha = currentAlpha.coerceIn(0f, maxBrightness)
                isAnimating = true
            } else {
                currentAlpha = targetAlpha
            }
            if (crossfadeAlpha >= 1f && Math.abs(currentAlpha - targetAlpha) <= 0.001f) {
                isAnimating = false
                albumArtOld = null
            }
            val canvas = node.beginRecording()
            try {
                canvas.drawColor(Color.BLACK)
                albumArtOld?.let { oldBmp ->
                    val oldAlpha = (1f - crossfadeAlpha) * currentAlpha
                    if (oldAlpha > 0.01f) {
                        scaleAndDraw(canvas, oldBmp, oldAlpha)
                    }
                }
                albumArt?.let { newBmp ->
                    val newAlpha = crossfadeAlpha * currentAlpha
                    if (newAlpha > 0.01f) {
                        scaleAndDraw(canvas, newBmp, newAlpha)
                    }
                }
            }
            finally {
                node.endRecording()
            } 
            renderer.setContentRoot(node)
            renderer.createRenderRequest()
            .setVsyncTime(System.nanoTime())
            .syncAndDraw()
            if (isAnimating) {
                scheduleNextFrame()
            }
        }
    }
} 
