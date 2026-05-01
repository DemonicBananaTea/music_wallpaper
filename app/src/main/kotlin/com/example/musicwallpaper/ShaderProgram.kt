package com.example.musicwallpaper

import android.opengl.GLES20

object ShaderProgram {

    fun create(): Int {

        val vertex = compile(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER)
        val fragment = compile(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER)

        val program = GLES20.glCreateProgram()

        GLES20.glAttachShader(program, vertex)
        GLES20.glAttachShader(program, fragment)

        GLES20.glLinkProgram(program)

        return program
    }

    private fun compile(type: Int, code: String): Int {
        val shader = GLES20.glCreateShader(type)

        GLES20.glShaderSource(shader, code)
        GLES20.glCompileShader(shader)

        return shader
    }
}