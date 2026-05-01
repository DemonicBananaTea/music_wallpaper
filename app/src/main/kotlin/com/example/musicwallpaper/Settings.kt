package com.example.musicwallpaper

import android.content.Context

object Settings {

    private const val PREF = "music_wallpaper"
    private const val KEY = "allowed_apps"

    fun getAllowedApps(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY, emptySet()) ?: emptySet()
    }

    fun setAllowedApps(context: Context, set: Set<String>) {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY, set).apply()
    }
}