package com.example.musicwallpaper

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val btn = Button(this)
        btn.text = "Вибрати застосунки"

        btn.setOnClickListener {
            startActivity(Intent(this, AppSelectActivity::class.java))
        }

        setContentView(btn)
    }
}