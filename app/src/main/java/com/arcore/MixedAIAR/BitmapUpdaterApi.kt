package com.arcore.MixedAIAR

import android.graphics.Bitmap

/**
 * Stores latest Bitmap to pass to DynamicBitmapSource
 */
class BitmapUpdaterApi {
    var latestBitmap : Bitmap? = null

    fun updateBitmap(bitmap: Bitmap) {
        latestBitmap = bitmap
    }
}