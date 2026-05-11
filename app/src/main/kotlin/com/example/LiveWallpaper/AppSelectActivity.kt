package com.example.livewallpaper

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.color.DynamicColors
import android.util.Log
import android.content.Intent

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable,
    var isSelected: Boolean = false
)

class AppSelectActivity : ComponentActivity() {
    private var selectedPackages: Set<String> = emptySet()

    override fun onCreate(savedInstanceState: Bundle?) {
        DynamicColors.applyToActivityIfAvailable(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_select)

        val prefs = getSharedPreferences("WallpaperSettings", MODE_PRIVATE)
        selectedPackages = prefs.getStringSet("selected_packages", emptySet()) ?: emptySet()

        val apps = getInstalledApps()
        val adapter = AppAdapter(apps)

        val rvApps = findViewById<RecyclerView>(R.id.rvApps)
        val btnOk = findViewById<View>(R.id.btnOk)
        
        rvApps.layoutManager = LinearLayoutManager(this)
        rvApps.adapter = adapter

        btnOk.setOnClickListener {
        selectedPackages = apps.filter { it.isSelected }.map { it.packageName }.toSet()
        
        getSharedPreferences("WallpaperSettings", MODE_PRIVATE)
            .edit()
            .putStringSet("selected_packages", selectedPackages)
            .apply()
        finish()
        val intent = Intent("com.example.ACTION_UPDATE_CONFIG")
        intent.setPackage(packageName) // Щоб сигнал не вийшов за межі твого додатка
        sendBroadcast(intent)
        }
    }

    private fun getInstalledApps(): List<AppInfo> {
        val pm = packageManager
        val intent = android.content.Intent(android.content.Intent.ACTION_MAIN, null)
        intent.addCategory(android.content.Intent.CATEGORY_LAUNCHER)
        
return pm.queryIntentActivities(intent, 0).map {
        val pkg = it.activityInfo.packageName
        AppInfo(
            it.loadLabel(pm).toString(),
            pkg,
            it.loadIcon(pm),
            isSelected = pkg in selectedPackages // <--- ОЦЕ КЛЮЧОВИЙ МОМЕНТ
        )
        }.sortedBy { it.name.lowercase() }
    }

    class AppAdapter(
        private val apps: List<AppInfo>
    ) : RecyclerView.Adapter<AppAdapter.VH>() {
        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val icon = v.findViewById<ImageView>(R.id.ivAppIcon)
            val name = v.findViewById<TextView>(R.id.tvAppName)
            val check = v.findViewById<MaterialCheckBox>(R.id.cbSelected)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = 
            VH(LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false))

        override fun onBindViewHolder(holder: VH, position: Int) {
            val app = apps[position]
            holder.name.text = app.name
            holder.icon.setImageDrawable(app.icon)
            holder.check.setOnCheckedChangeListener(null) // Важливо для переробки view
            holder.check.isChecked = app.isSelected
            holder.check.setOnCheckedChangeListener { _, isChecked -> app.isSelected = isChecked }
            holder.itemView.setOnClickListener { holder.check.toggle() }
        }

        override fun getItemCount() = apps.size
    }
}
