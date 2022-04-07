package com.arcore.MixedAIAR;

class ItemsViewModel {
    val models = listOf("MobileNet Float", "MobileNet Quant")
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

