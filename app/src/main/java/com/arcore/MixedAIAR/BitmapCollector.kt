package com.arcore.MixedAIAR

import android.app.Activity
import android.graphics.Bitmap
import android.os.SystemClock
import android.text.SpannableStringBuilder
import android.util.Log
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
//    private val bitmapSource: BitmapSource?,
    private val bitmapSource: DynamicBitmapSource?,
    private val classifier: ImageClassifier?,
    var index: Int, // to ensure unique filename.
    private val activity: Activity
): ViewModel() {

//    var instance: MainActivity =

    var start: Long = 0
    var end: Long = 0
    var overhead: Long = 0
    var classificationTime: Long = 0
    var responseTime: Long = 0
    var totalResponseTime: Long = 0
    var numOfTimesExecuted = 0
    //
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
        val file = File(childDirectory,
            index.toString() + '_' +
                    classifier?.modelName + '_' +
                    classifier?.device + '_'+
                    classifier?.numThreads + "T_"+
                    classifier?.time +
                    ".csv")
//        file.appendText("timestamp,response,guess3,acc3,guess2,acc2,guess1,acc1\n")
        file.appendText("overhead,classification Time,response time ")
//        end = System.nanoTime()/1000000
        job = viewModelScope.launch(Dispatchers.Default) {
            bitmapSource?.bitmapStream?.collect {

                val bitmap = Bitmap.createScaledBitmap(
                    it,
                    classifier!!.imageSizeX,
                    classifier.imageSizeY,
                    true
                )

                start = System.nanoTime()/1000000
                if(end!=0L) {
                    overhead = start-end
                }
                classifier.classifyFrame(bitmap)
                end = System.nanoTime()/1000000
                classificationTime = end-start
                responseTime=overhead+classificationTime
                numOfTimesExecuted++
                totalResponseTime+=responseTime
                // throughput = 1/avg(responseTimes for 1 model)
                // then calculate average throughputs
                Log.d("times", "${overhead},${classificationTime},${responseTime}")
                outputText.append("${overhead},${classificationTime},${responseTime}\n")
                file.appendText(outputText.toString())
//                    delay(50)
            }
        }
    }
}


