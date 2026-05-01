package com.example.musicwallpaper

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable

object Utils {

    fun drawableToBitmap(drawable: Drawable): Bitmap {
        // якщо це вже BitmapDrawable — просто беремо bitmap
        if (drawable is android.graphics.drawable.BitmapDrawable) {
            drawable.bitmap?.let { return it.copy(Bitmap.Config.ARGB_8888, true) }
        }

        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 512
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 512

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }
}