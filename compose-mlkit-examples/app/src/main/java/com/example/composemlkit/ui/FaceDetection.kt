package com.example.composemlkit.ui

import android.content.Context
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.OnImageSavedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.composemlkit.common.AssetUtil
import com.example.composemlkit.common.FaceAnalyzer
import com.example.composemlkit.common.models.FileData
import androidx.compose.ui.tooling.preview.Preview as ComposablePreview

@Composable
fun FaceDetection(
    onImageSaved: (file: FileData) -> Unit = {}
) {
    val viewModel = hiltViewModel<FaceDetectionViewModel>()
    val uiState = viewModel.state
    val counter = viewModel.counterState

    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    LaunchedEffect(Unit) { viewModel.onViewIsInitialized() }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val executor = ContextCompat.getMainExecutor(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                    .build()

                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setImageQueueDepth(10)
                    .build()
                    .apply {
                        setAnalyzer(executor, FaceAnalyzer(viewModel))
                    }

                ProcessCameraProvider
                    .getInstance(context).addListener(
                        {
                            imageCapture = ImageCapture.Builder()
                                .build()
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageCapture,
                                imageAnalysis
                            )
                        },
                        executor
                    )
            }, executor)
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )

    when (uiState.uiState) {
        is FaceDetectionViewModel.UiState.InvalidPosition -> {
            InvalidPosition(uiState.uiState.description)
        }
        is FaceDetectionViewModel.UiState.CapturingImage -> {
            takePicture(context, imageCapture, onImageSaved, viewModel)
        }
        FaceDetectionViewModel.UiState.Initial -> {}
    }
    if (uiState.isPreparingCamera()) {
        CounterView(counter.value)
    }
}

@Composable
private fun Processing() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xBEFFFFFF))
    ) {
    }
}

@Composable
private fun takePicture(
    context: Context,
    imageCapture: ImageCapture?,
    onImageSaved: (file: FileData) -> Unit,
    viewModel: FaceDetectionViewModel
) {
    LaunchedEffect(Unit) {
        viewModel.onProcessing()
    }

    val photoFile = AssetUtil.createDefaultFile(context)
    photoFile?.let {
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture?.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    AssetUtil.addWaterMark(photoFile, context)
                    onImageSaved(FileData(photoFile.absolutePath))
                }

                override fun onError(exception: ImageCaptureException) {
                    println(exception.message)
                }
            }
        )
    }
}

@Composable
fun InvalidPosition(description: String) {
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(color = Color(0x80E98440))
        ) {
            Text(
                text = description,
                fontSize = 30.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CounterView(counter: Int) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "$counter",
            fontSize = 80.sp,
            color = Color(0xBFFAF9F9),
            fontWeight = FontWeight.Bold
        )
    }
}

@ComposablePreview
@Composable
fun CounterViewPreview() {
    CounterView(5)
}

@ComposablePreview
@Composable
fun InvalidPositionViewPreview() {
    InvalidPosition("Invalid Position")
}
