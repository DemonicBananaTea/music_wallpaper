package com.example.musicwallpaper

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppSelectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_select)

        val recycler = findViewById<RecyclerView>(R.id.recycler)
        recycler.layoutManager = LinearLayoutManager(this)

        val pm = packageManager

        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val apps = pm.queryIntentActivities(intent, 0)

        val saved = Settings.getAllowedApps(this)

        val items = apps.map {
            val pkg = it.activityInfo.packageName

            AppItem(
                label = it.loadLabel(pm).toString(),
                packageName = pkg,
                selected = saved.contains(pkg)
            )
        }.sortedBy { it.label.lowercase() }

        recycler.adapter = AppAdapter(items) { updated ->
            val selected = updated
                .filter { it.selected }
                .map { it.packageName }
                .toSet()

            Settings.setAllowedApps(this, selected)
        }
    }
}