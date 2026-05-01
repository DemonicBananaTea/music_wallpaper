package com.example.musicwallpaper

import android.content.Context
import android.graphics.Bitmap
import android.view.SurfaceHolder
import javax.microedition.khronos.egl.*
import android.opengl.*

class GLRenderer(
    private val holder: SurfaceHolder,
    private val context: Context
) {

    private var running = false
    private var visible = false

    private lateinit var eglDisplay: EGLDisplay
    private lateinit var eglContext: EGLContext
    private lateinit var eglSurface: EGLSurface

    private var program = 0
    private var textureId = 0

    fun setVisible(v: Boolean) {
        visible = v
    }

    fun start() {
        running = true
        Thread { loop() }.start()
    }

    fun stop() {
        running = false
    }

    private fun loop() {
        initGL()

        while (running) {
            if (visible) {
                updateTexture()
                render()
            }
            Thread.sleep(16)
        }

        releaseGL()
    }

    private fun initGL() {
        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        EGL14.eglInitialize(eglDisplay, null, 0, null, 0)

        val configAttribs = intArrayOf(
            EGL14.EGL_RENDERABLE_TYPE,
            EGL14.EGL_OPENGL_ES2_BIT,
            EGL14.EGL_NONE
        )

        val configs = arrayOfNulls<EGLConfig>(1)
        val num = IntArray(1)

        EGL14.eglChooseConfig(
            eglDisplay,
            configAttribs,
            0,
            configs,
            0,
            1,
            num,
            0
        )

        val ctxAttribs = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL14.EGL_NONE
        )

        eglContext = EGL14.eglCreateContext(
            eglDisplay,
            configs[0],
            EGL14.EGL_NO_CONTEXT,
            ctxAttribs,
            0
        )

        eglSurface = EGL14.eglCreateWindowSurface(
            eglDisplay,
            configs[0],
            holder,
            intArrayOf(EGL14.EGL_NONE),
            0
        )

        EGL14.eglMakeCurrent(
            eglDisplay,
            eglSurface,
            eglSurface,
            eglContext
        )

        program = ShaderProgram.create()
    }

    private fun updateTexture() {
        val bmp = ArtworkStore.currentBitmap ?: return
        textureId = TextureHelper.loadTexture(bmp)
    }

    private fun render() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        GLES20.glUseProgram(program)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)

        Quad.draw(program)

        EGL14.eglSwapBuffers(eglDisplay, eglSurface)
    }

    private fun releaseGL() {
        EGL14.eglDestroySurface(eglDisplay, eglSurface)
        EGL14.eglDestroyContext(eglDisplay, eglContext)
        EGL14.eglTerminate(eglDisplay)
    }
}