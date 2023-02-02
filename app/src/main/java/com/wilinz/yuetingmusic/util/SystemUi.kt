package com.wilinz.yuetingmusic.util

import android.view.Window
import androidx.core.view.WindowCompat


fun setStatusBarTint(window: Window, isAppearanceLightStatusBars: Boolean) {
    WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
        isAppearanceLightStatusBars
}