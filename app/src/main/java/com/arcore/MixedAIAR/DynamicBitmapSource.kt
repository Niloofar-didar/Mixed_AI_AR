package com.arcore.MixedAIAR

import android.app.Activity
import android.graphics.Bitmap
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DynamicBitmapSource(private val bitmapUpdaterApi : BitmapUpdaterApi) {
    var run = false
    lateinit var bitmapStream : Flow<Bitmap?>


    private fun runStream() {
        bitmapStream = flow {
            while (run) {
                val bitmapStream = bitmapUpdaterApi.latestBitmap
                emit(bitmapStream)
                delay(10)
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