package com.example.musicwallpaper

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.widget.Button

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val btn = findViewById<Button>(R.id.openSettings)
        
        btn.setOnClickListener {
            it.post {
                startActivity(Intent(this, AppSelectActivity::class.java))
            }
        }
    }
}