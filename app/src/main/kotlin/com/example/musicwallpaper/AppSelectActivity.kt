package com.example.musicwallpaper

import android.content.pm.PackageManager
import android.content.Context
import android.content.Intent
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

        val intent = Intent(Intent.ACTION_MAIN, null) intent.addCategory(Intent.CATEGORY_LAUNCHER)

        val installed = pm.queryIntentActivities(intent, 0)

        val saved = Settings.getAllowedApps(this)

        val items = installed.map {
        val ai = it.activityInfo
            AppItem(
                label = it.loadLabel(pm).toString(),
                packageName = ai.packageName,
                selected = saved.contains(ai.packageName)
            )
        }

        val adapter = AppAdapter(items) { updated ->
            val selected = updated
                .filter { it.selected }
                .map { it.packageName }
                .toSet()

            Settings.setAllowedApps(this, selected)
        }

        recycler.adapter = adapter
    }
    
    private fun getUserApps(context: Context): List<AppItem> {
    val pm = context.packageManager

    val intent = Intent(Intent.ACTION_MAIN, null)
    intent.addCategory(Intent.CATEGORY_LAUNCHER)

    val apps = pm.queryIntentActivities(intent, 0)

    return apps.map {
        AppItem(
            packageName = it.activityInfo.packageName,
            label = it.loadLabel(pm).toString()
        )
    }.sortedBy { it.label }
}
    
    override fun onResume() {
    super.onResume()
    android.util.Log.e("APP_SELECT", "RESUMED")
}
}