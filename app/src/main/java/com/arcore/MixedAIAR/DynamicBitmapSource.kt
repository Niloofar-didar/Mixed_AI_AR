package com.arcore.MixedAIAR

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Emits latest bitmap from BitmapUpdaterApi
 */
class DynamicBitmapSource(private val bitmapUpdaterApi : BitmapUpdaterApi) {
    var run = false
    lateinit var bitmapStream : Flow<Bitmap?>

    private fun runStream() {
        bitmapStream = flow {
            while (run) {
                // get latest bitmap from BitmapUpdaterApi
//                val bitmapStream = bitmapUpdaterApi.latestBitmap
                emit(bitmapUpdaterApi.latestBitmap)
            }
        }.flowOn(Dispatchers.Default)
    }

    fun startStream() {
        run = true
        runStream()
    }

    fun pauseStream() {
        run = false
    }
}