package com.example.musicwallpaper

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppSelectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val recycler = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@AppSelectActivity)
        }

        setContentView(recycler)

        val pm = packageManager

        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val apps = pm.queryIntentActivities(intent, 0)

        val saved = Settings.getAllowedApps(this)

        val items = apps.map {
            val pkg = it.activityInfo.packageName

            AppItem(
                label = it.loadLabel(pm).toString(),
                packageName = pkg,
                selected = saved.contains(pkg)   // 🔥 ВАЖЛИВО
            )
        }.sortedBy { it.label.lowercase() }

        Log.e("APPS", "COUNT = ${items.size}")
        Log.e("APPS", "SAVED = $saved")

        recycler.adapter = AppAdapter(items) { updated ->

            val selected = updated
                .filter { it.selected }
                .map { it.packageName }
                .toSet()

            Log.e("APPS", "NEW SAVED = $selected")

            Settings.setAllowedApps(this, selected)
        }
    }
}