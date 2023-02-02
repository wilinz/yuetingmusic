package com.wilinz.yuetingmusic.util

import android.util.Log
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette

const val TAG = "Palette.kt"

fun Palette.isDark(): Boolean? {
    var mostPopularSwatch: Palette.Swatch? = null
    for (swatch in this.swatches) {
        if (mostPopularSwatch == null
            || swatch.population > mostPopularSwatch.population
        ) {
            mostPopularSwatch = swatch
        }
    }
    Log.d(TAG, "mostPopularSwatch: ${mostPopularSwatch}")
    return mostPopularSwatch?.let { swatch ->
        val m = ColorUtils.calculateLuminance(swatch.rgb)
        Log.d(TAG, "calculateLuminance: ${m}")
        m < 0.5
    }
}
