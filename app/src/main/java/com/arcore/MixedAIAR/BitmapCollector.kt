package com.arcore.MixedAIAR

import android.app.Activity
import android.graphics.Bitmap
import android.text.SpannableString
import android.text.SpannableStringBuilder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import java.io.File

class BitmapCollector(
//    private val bitmapSource: BitmapSource?,
    private val bitmapSource: DynamicBitmapSource?,
    private val classifier: ImageClassifier?,
//    private val textView: TextView,
    private val activity: Activity
    ): ViewModel() {

        val path = activity.getExternalFilesDir(null)
        val directory = File(path, "data")


        var run = false
        var job : Job? = null
        var textToShow = SpannableStringBuilder("null")

        fun toggleCollect() {
            when (run) {
                true -> pauseCollect()
                false -> startCollect()
            }
        }

        fun pauseCollect() {
            run = false
            job?.cancel()
        }


        fun startCollect() = runBlocking <Unit>{
            run = true
            launch {
                collectStream()
            }
        }

        private fun collectConcurrently() = runBlocking <Unit> {
            launch {
                collectStream()
            }
        }

        private suspend fun collectStream() {
            directory.mkdirs()
            val file = File(directory, classifier?.modelName + '_' + classifier?.device + '_'+ classifier?.time +".csv")
            job = viewModelScope.launch(Dispatchers.Default) {
                    bitmapSource?.bitmapStream?.collect {
    //                    val textToShow = SpannableStringBuilder()
                        val bitmap = Bitmap.createScaledBitmap(
                            it,
                            classifier!!.imageSizeX,
                            classifier.imageSizeY,
                            true
                        )
//                        val file: File = File(directory, bitmap.toString() + ".jpeg")
//                        val fOut = FileOutputStream(file)
//                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut)
//                        fOut.flush()
//                        fOut.close()



                        classifier.classifyFrame(bitmap, textToShow)
                        if(textToShow.toString().length<0) {
                            println("Length: ${textToShow.toString().length}")
                        }
    //                    showToast(textToShow)
                        println(textToShow.toString())
                        file.appendText(textToShow.toString())

                        delay(100)
                    }

                }
            }

        private fun showToast(s: String) {
            val str1 = SpannableString(s)
            textToShow.append(str1)
            showToast(textToShow)
        }

        private fun showToast(builder: SpannableStringBuilder?) {
            if (activity != null) {
//                activity.runOnUiThread { textView.setText(builder, TextView.BufferType.SPANNABLE) }
            }
        }
    }


