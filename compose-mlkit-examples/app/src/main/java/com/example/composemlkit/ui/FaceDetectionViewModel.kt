package com.example.composemlkit.ui

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FaceDetectionViewModel @Inject constructor() : ViewModel() {

    private val allowedRange = 40.0..70.0
    private val isAppInBackground = false // add code to check when app is in background
    private var counterJob: Job? = null
    private val mutableState = mutableStateOf(State())
    val state: State
        get() = mutableState.value

    private var _counterState = mutableStateOf(0)
    val counterState: MutableState<Int>
        get() = _counterState

    private fun setState(newState: State) {
        mutableState.value = newState
    }

    fun onViewIsInitialized() {
        setInicialState()
        stopCounter()
    }

    private fun setInicialState() {
        setState(state.copy(uiState = UiState.Initial))
    }

    private fun stopCounter() {
        counterJob?.cancel()
        _counterState.value = MAX_COUNTER
    }

    private fun preparingPhoto() {
        if (!state.shouldTheCounterStart()) return
        setState(state.copy(uiState = UiState.Preparing))
        counterJob = safeLaunch {
            (MAX_COUNTER - 1 downTo 0).asFlow()
                .onEach { delay(DELAY_BETWEEN_COUNTER) }
                .collect { counter ->
                    _counterState.value = counter
                    if (counter == 0) setState(state.copy(uiState = UiState.CapturingImage))
                }
        }
    }

    private fun setInvalidState() {
        setState(state.copy(uiState = UiState.InvalidPosition(INVALID_POSITION_DESC)))
        stopCounter()
    }

    private fun getEye(faces: List<Face>, eye: Int) =
        faces.first().allContours.firstOrNull { it.faceContourType == eye }

    private fun getRightEye(faces: List<Face>) = getEye(faces, FaceContour.RIGHT_EYE)

    private fun getLeftEye(faces: List<Face>) = getEye(faces, FaceContour.LEFT_EYE)

    fun onProcessingTheImage(faces: List<Face>) {
        if (!isAppInBackground && faces.isNotEmpty()) {
            val leftEye = getLeftEye(faces) ?: return
            val rightEye = getRightEye(faces) ?: return

            val xLeftEye = leftEye.points.maxOf { it.x }
            val xRightEye = rightEye.points.minOf { it.x }

            if ((xRightEye - xLeftEye) in allowedRange) {
                preparingPhoto()
            } else {
                setInvalidState()
            }
        }
    }

    fun onProcessing() {
        setState(state.copy(uiState = UiState.Processing))
    }

    sealed class UiState {
        object Initial : UiState()
        object Preparing : UiState()
        object Processing : UiState()
        class InvalidPosition(val description: String) : UiState()
        object CapturingImage : UiState()
    }

    data class State(
        val uiState: UiState = UiState.Initial,
        val isInvalidPosition: Boolean = false
    ) {
        fun isPreparingCamera() = uiState is UiState.Preparing
        private fun isCapturingImage() = uiState is UiState.CapturingImage
        private fun isProcessing() = uiState is UiState.Processing
        fun shouldTheCounterStart() = !isPreparingCamera() && !isCapturingImage() && !isProcessing()
    }

    companion object {
        private const val MAX_COUNTER = 5
        private const val DELAY_BETWEEN_COUNTER = 1000L
        private const val INVALID_POSITION_DESC = "Invalid Position"
    }
}

fun ViewModel.safeLaunch(
    block: suspend CoroutineScope.() -> Unit
): Job {
    return viewModelScope.launch { block() }
}
