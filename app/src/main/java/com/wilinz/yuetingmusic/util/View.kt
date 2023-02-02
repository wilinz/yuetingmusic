package com.wilinz.yuetingmusic.util

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup

fun View.getBoundsByRoot(root: ViewGroup): Rect {
    val offsetViewBounds = Rect()
    this.getDrawingRect(offsetViewBounds)
    root.offsetDescendantRectToMyCoords(this, offsetViewBounds)
    return offsetViewBounds
}