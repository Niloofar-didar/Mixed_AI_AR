package com.arcore.MixedAIAR

import android.app.Activity
import android.graphics.Bitmap
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DynamicBitmapSource(private val bitmapUpdaterApi : BitmapUpdaterApi) {

    val bitmapStream: Flow<Bitmap?> = flow {
        while(true) {
            val bitmapStream = bitmapUpdaterApi.latestBitmap
            emit(bitmapStream)
            delay(250)
        }
    }
}