package com.example.livewallpaper

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.livewallpaper.databinding.ActivityMainBinding
import com.google.android.material.color.DynamicColors
import android.app.WallpaperManager
import android.content.ComponentName

class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Динамічні кольори
        DynamicColors.applyToActivityIfAvailable(this)

        super.onCreate(savedInstanceState)

        // 2. ViewBinding (має бути ПЕРЕД використанням будь-яких в'юшок)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 3. Логіка вибору додатків
        binding.btnSelectApps.setOnClickListener {
            val intent = Intent(this, AppSelectActivity::class.java)
            startActivity(intent)
        }
        
        // 4. Завантаження налаштувань
        val prefs = getSharedPreferences("WallpaperSettings", MODE_PRIVATE)
        setupSettingsListeners(prefs)
        
        // Початкова перевірка дозволів
        updatePermissionUI()
    }

    override fun onResume() {
        super.onResume()
        // Оновлюємо стан картки, коли користувач повернувся
        updatePermissionUI()
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat?.contains(packageName) == true
    }

    private fun updatePermissionUI() {
        if (isNotificationServiceEnabled()) {
            // Якщо дозвіл є — ховаємо картку
            binding.permissionCard.visibility = View.GONE
        } else {
            // Якщо дозволу нема — показуємо картку і вішаємо клік на кнопку в ній
            binding.permissionCard.visibility = View.VISIBLE
            binding.btnOpenSettings.setOnClickListener {
                try {
                    startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                } catch (e: Exception) {
                    Toast.makeText(this, "Не вдалося відкрити налаштування 💢", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupSettingsListeners(prefs: android.content.SharedPreferences) {
        binding.apply {
            // Встановлюємо початкові значення
            switchBlur.isChecked = prefs.getBoolean("use_blur", true)
            sliderBlur.value = prefs.getInt("blur_radius", 120).toFloat()
            switchDark.isChecked = prefs.getBoolean("use_dark", true)
            sliderDark.value = prefs.getFloat("darken_str", 0.4f)
            
            // Слухачі змін
            switchBlur.setOnCheckedChangeListener { _, isChecked ->
                prefs.edit().putBoolean("use_blur", isChecked).apply()
            }

            sliderBlur.addOnChangeListener { _, value, fromUser ->
                if (fromUser) prefs.edit().putInt("blur_radius", value.toInt()).apply()
            }

            switchDark.setOnCheckedChangeListener { _, isChecked ->
                prefs.edit().putBoolean("use_dark", isChecked).apply()
            }

            sliderDark.addOnChangeListener { _, value, fromUser ->
                if (fromUser) prefs.edit().putFloat("darken_str", value).apply()
            }

            binding.btnApplyWallpaper.setOnClickListener {
                val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                intent.putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    // Використовуй this@MainActivity, щоб вказати на контекст Activity
                    ComponentName(this@MainActivity, MyWallpaperService::class.java) 
                )
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    val fallbackIntent = Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
                    startActivity(fallbackIntent)
                }
            }
        } //binding.apply
    }
}
