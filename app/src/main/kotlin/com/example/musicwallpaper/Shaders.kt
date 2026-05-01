package com.example.musicwallpaper

const val VERTEX_SHADER = """
attribute vec4 position;
attribute vec2 texCoord;
varying vec2 vTexCoord;

void main() {
    gl_Position = position;
    vTexCoord = texCoord;
}
"""

const val FRAGMENT_SHADER = """
precision mediump float;

uniform sampler2D uTexture;
varying vec2 vTexCoord;

void main() {
    float blur = 0.01;

    vec4 color = vec4(0.0);

    color += texture2D(uTexture, vTexCoord + vec2(-blur, -blur)) * 0.111;
    color += texture2D(uTexture, vTexCoord + vec2(0.0, -blur)) * 0.111;
    color += texture2D(uTexture, vTexCoord + vec2(blur, -blur)) * 0.111;

    color += texture2D(uTexture, vTexCoord + vec2(-blur, 0.0)) * 0.111;
    color += texture2D(uTexture, vTexCoord) * 0.111;
    color += texture2D(uTexture, vTexCoord + vec2(blur, 0.0)) * 0.111;

    color += texture2D(uTexture, vTexCoord + vec2(-blur, blur)) * 0.111;
    color += texture2D(uTexture, vTexCoord + vec2(0.0, blur)) * 0.111;
    color += texture2D(uTexture, vTexCoord + vec2(blur, blur)) * 0.111;

    gl_FragColor = color;
}
"""