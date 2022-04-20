package com.arcore.MixedAIAR;

/**
 * Object to hold classifier object for recyclerview content
 * */
class ItemsViewModel {
    val models = listOf("MN V1 1.0 Q 224", "MN v2 1.0 Q 224", "MN v1 1.0 Q 224", "IN V1 Q 224", "MN v1 0.25 Q 128")
    val devices = listOf("cpu", "gpu", "nnapi")
    var consumer : BitmapCollector? = null
    var classifier: ImageClassifier? = null
    var currentDevice = -1
    var currentModel = -1
    var currentNumThreads = -1

    fun setCollector(bitmapConsumer: BitmapCollector) {
        consumer = bitmapConsumer
    }
}

