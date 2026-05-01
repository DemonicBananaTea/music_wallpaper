package com.example.musicwallpaper

import android.graphics.Bitmap

object ArtworkStore {
    @Volatile var currentBitmap: Bitmap? = null
    @Volatile var lastUpdateTime: Long = 0L
}