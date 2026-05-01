package com.example.musicwallpaper

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

        val installed = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        val saved = Settings.getAllowedApps(this)

        val items = installed.map {
            AppItem(
                label = pm.getApplicationLabel(it).toString(),
                packageName = it.packageName,
                selected = saved.contains(it.packageName)
            )
        }.sortedBy { it.label.lowercase() }

        val adapter = AppAdapter(items) { updated ->
            val selected = updated
                .filter { it.selected }
                .map { it.packageName }
                .toSet()

            Settings.setAllowedApps(this, selected)
        }

        recycler.adapter = adapter
    }
}