package com.wilinz.yuetingmusic.util

import android.graphics.*
import androidx.annotation.Px


fun zoomImg(bm: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
    val srcWidth = bm.width
    val srcHeight = bm.height
    val widthScale = targetWidth * 1.0f / srcWidth
    val heightScale = targetHeight * 1.0f / srcHeight
    val matrix = Matrix()
    matrix.postScale(widthScale, heightScale, 0F, 0F)
    // 如需要可自行设置 Bitmap.Config.RGB_8888 等等
    val bmpRet = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.RGB_565)
    val canvas = Canvas(bmpRet)
    val paint = Paint()
    canvas.drawBitmap(bm, matrix, paint)
    return bmpRet
}

fun Bitmap.isDark(@Px left: Int, @Px top: Int, @Px right: Int, @Px bottom: Int): Boolean {
    val darkThreshold = 185
    val width = right - left
    val height = bottom - top
    val pixels = IntArray(width * height)
    this.getPixels(pixels, 0, width, left, top, width, height)
    var darkPixels = 0
    for (pixel in pixels) {
        val color = Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)
        if (color <= darkThreshold) {
            darkPixels++
        }
    }
    return darkPixels / pixels.size.toDouble() >= 0.5
}