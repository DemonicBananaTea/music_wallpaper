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
        setContentView(R.layout.activity_app_select)

        val recycler = findViewById<RecyclerView>(R.id.recycler)
        recycler.layoutManager = LinearLayoutManager(this)

        val pm = packageManager

        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val installed = pm.queryIntentActivities(intent, 0)

        val saved = Settings.getAllowedApps(this)

        val items = installed.map {
            val pkg = it.activityInfo.packageName

            AppItem(
                label = it.loadLabel(pm).toString(),
                packageName = pkg,
                selected = saved.contains(pkg)
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

    override fun onResume() {
        super.onResume()
        Log.e("APP_SELECT", "RESUMED")
    }
}