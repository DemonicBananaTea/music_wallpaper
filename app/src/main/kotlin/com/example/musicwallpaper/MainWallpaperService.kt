package com.example.musicwallpaper

import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder

class MainWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return GLEngine()
    }

    inner class GLEngine : Engine() {

        private var renderer: GLRenderer? = null

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            renderer = GLRenderer(holder, applicationContext)
            renderer?.start()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            renderer?.setVisible(visible)
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            renderer?.stop()
        }
    }
}