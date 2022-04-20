package com.arcore.MixedAIAR

import android.graphics.Bitmap

class BitmapUpdaterApi {
    var latestBitmap : Bitmap? = null


    fun updateBitmap(bitmap: Bitmap) {
        latestBitmap = bitmap
    }

//    fun fetchLatestBitmap() : Bitmap? {
//        return latestBitmap
//    }
}