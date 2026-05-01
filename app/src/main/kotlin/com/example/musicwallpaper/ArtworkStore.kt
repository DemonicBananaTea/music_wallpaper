package com.example.musicwallpaper

import android.graphics.Bitmap

object ArtworkStore {
    @Volatile var bitmap: Bitmap? = null
    @Volatile var lastUpdateTime: Long = 0L
}