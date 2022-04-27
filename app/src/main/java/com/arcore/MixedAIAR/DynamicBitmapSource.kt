package com.arcore.MixedAIAR

import android.graphics.Bitmap
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

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
                val bitmapStream = bitmapUpdaterApi.latestBitmap
                emit(bitmapStream)
                // Delay to check for new bitmap
                delay(20) // 16.666 ms per frame
            }
        }
    }

    fun startStream() {
        run = true
        runStream()
    }

    fun pauseStream() {
        run = false
    }
}