package com.example.musicwallpaper

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity

class AppSelectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this)
        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL

        scroll.addView(container)
        setContentView(scroll)

        val pm = packageManager

        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .sortedBy { pm.getApplicationLabel(it).toString() }

        val selected = Settings.getAllowedApps(this).toMutableSet()

        for (app in apps) {
            val label = pm.getApplicationLabel(app).toString()
            val pkg = app.packageName

            val cb = CheckBox(this)
            cb.text = label
            cb.isChecked = selected.contains(pkg)

            cb.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) selected.add(pkg)
                else selected.remove(pkg)

                Settings.setAllowedApps(this, selected)
            }

            container.addView(cb)
        }
    }
}