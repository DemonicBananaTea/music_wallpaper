package com.example.musicwallpaper

import android.os.Bundle
import android.widget.*
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity

class AppSelectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL

        val scroll = ScrollView(this)
        scroll.addView(layout)

        setContentView(scroll)

        val pm = packageManager
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        val selected = Settings.getAllowedApps(this).toMutableSet()

        for (app in installedApps) {
            val name = pm.getApplicationLabel(app).toString()
            val pkg = app.packageName

            val checkBox = CheckBox(this)
            checkBox.text = name
            checkBox.isChecked = selected.contains(pkg)

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) selected.add(pkg)
                else selected.remove(pkg)

                Settings.setAllowedApps(this, selected)
            }

            layout.addView(checkBox)
        }
    }
}