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

        val recycler = RecyclerView(this)
        recycler.layoutManager = LinearLayoutManager(this)
        setContentView(recycler)

        val pm = packageManager

        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val apps = pm.queryIntentActivities(intent, 0)

        val items = apps.map {
            AppItem(
                label = it.loadLabel(pm).toString(),
                packageName = it.activityInfo.packageName,
                selected = false
            )
        }.sortedBy { it.label.lowercase() }

        Log.e("APPS", "COUNT = ${items.size}")

        recycler.adapter = AppAdapter(items) {}
    }
}