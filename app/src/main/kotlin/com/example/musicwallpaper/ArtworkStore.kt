package com.example.musicwallpaper

import android.graphics.Bitmap
import java.util.concurrent.atomic.AtomicReference

object ArtworkStore {
    private val ref = AtomicReference<Bitmap?>(null)

    fun set(bmp: Bitmap?) {
        ref.set(bmp)
    }

    fun get(): Bitmap? = ref.get()
}