package com.arcore.MixedAIAR

import android.app.Activity
import android.graphics.Bitmap
import android.text.SpannableStringBuilder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import java.io.File

/**
 * Collects Bitmaps from a coroutine source (BitmapSource for static JPG or DynamicBitmapSource for
 * a stream of changing files)
 */
class BitmapCollector(
    /**
     * If using static jpg, comment out DynamicBitmapSource, uncomment BitmapSource
     */
    //private val bitmapSource: BitmapSource?,
    private val bitmapSource: DynamicBitmapSource?,
    private val classifier: ImageClassifier?,
    private val activity: Activity
    ): ViewModel() {

        private val outputPath = activity.getExternalFilesDir(null)
        private val childDirectory = File(outputPath, "data")
        var run = false
        private var job : Job? = null
        var outputText = SpannableStringBuilder("null")

        /**
         * Stops running collector
         */
        fun pauseCollect() {
            run = false
            job?.cancel()
        }

        /**
         * Starts collection
         * Precondition: bitmapSource must be emitting a stream to collect
         */
        fun startCollect() = runBlocking <Unit>{
            run = true
            launch {
                collectStream()
            }
        }

        /**
         * launches coroutine to collect bitmap from bitmapSource, scales bitmap to
         * ImageClassifier requirements. Writes output to file.
         */
        private suspend fun collectStream() {
            childDirectory.mkdirs()
            val file = File(childDirectory, classifier?.modelName + '_' + classifier?.device + '_'+ classifier?.time +".csv")
            job = viewModelScope.launch(Dispatchers.Default) {
                bitmapSource?.bitmapStream?.collect {
                    val bitmap = Bitmap.createScaledBitmap(
                        it,
                        classifier!!.imageSizeX,
                        classifier.imageSizeY,
                        true
                    )

                    classifier.classifyFrame(bitmap, outputText)
                    file.appendText(outputText.toString())
                    delay(100)
                }
            }
        }
    }


