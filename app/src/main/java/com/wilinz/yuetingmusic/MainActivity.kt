package com.wilinz.yuetingmusic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.wilinz.yuetingmusic.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val binding = ActivityMainBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)
    }
}