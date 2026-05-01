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

    val tv = android.widget.TextView(this)
    tv.text = "TEST SCREEN 123"
    tv.textSize = 30f

    setContentView(tv)

    android.util.Log.e("APP_SELECT", "TEST SCREEN OPENED")
}
}