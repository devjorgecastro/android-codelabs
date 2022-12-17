package com.example.composemlkit.common

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.composemlkit.ui.FaceDetectionViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection

class FaceAnalyzer(private val viewModel: FaceDetectionViewModel) : ImageAnalysis.Analyzer {

    private val detector =
        FaceDetection.getClient(FaceDetectorOptionsUtil.getFaceDetectorOptions())

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        mediaImage?.let { image ->
            val inputImage =
                InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)

            detector.process(inputImage)
                .addOnSuccessListener { faces ->
                    viewModel.onProcessingTheImage(faces)
                    imageProxy.close()
                }
                .addOnFailureListener { imageProxy.close() }
                .addOnCompleteListener { imageProxy.close() }
        }
    }
}
