package com.example.samplecomposearcore.samples

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.samplecomposearcore.compose.GLThreadedRenderer
import com.example.samplecomposearcore.utils.Logger

class Sample5_ARCoreViewModel(context: Context, val postOnUiThread: (Runnable) -> Unit) : ViewModel() {
    private val glRenderer = Sample5_ARCoreRenderer(context) {
        postOnUiThread {
            onArCoreSessionCreated()
        }
    }
    val renderer: GLThreadedRenderer by mutableStateOf(glRenderer)

    var fps by mutableStateOf("0")
        private set

    var showingFeaturePoints by mutableStateOf(false)
        private set
    var showingDepthMap by mutableStateOf(false)
        private set
    var eisEnabled by mutableStateOf(false)
        private set

    private var depthApiSupported = false
    private var eisSupported = false
    var arCoreStats by mutableStateOf("")
        private set

    private var lastFrameTime = 0L
    private var framesCount = 0

    init {
        Logger.LogWarning(TAG, "GLSceneViewModel created")
        glRenderer.onFrameFinished = {
            postOnUiThread {
                updateFps()
            }
        }
        updateStats()
    }

    override fun onCleared() {
        super.onCleared()
        Logger.LogWarning(TAG, "GLSceneViewModel cleared")
        glRenderer.onClear()
    }

    fun deleteLast() {
        glRenderer.deleteLast()
    }

    fun toggleFeaturePoints(show: Boolean) {
        showingFeaturePoints = show
        glRenderer.showFeaturePoints(show)
    }

    fun toggleDepthMap(show: Boolean) {
        if (depthApiSupported) {
            showingDepthMap = show
            glRenderer.showDepthMap(show)
        } else {
            showingDepthMap = false
        }
    }

    fun toggleEIS(enabled: Boolean) {
        if (eisSupported) {
            eisEnabled = enabled
            glRenderer.enableEIS(enabled)
        } else {
            eisEnabled = false
        }
    }

    fun cameraPermissionWasGranted() {
        glRenderer.cameraPermissionWasGranted()
    }

    /**
     * Very simple and naive FPS counter. Might not be accurate.
     */
    private fun updateFps() {
        framesCount++
        val now = System.currentTimeMillis()
        val timeSinceLastUpdate = now - lastFrameTime
        if (timeSinceLastUpdate > 1000) {
            lastFrameTime = now
            fps = framesCount.toString()
            framesCount = 0
        }
    }

    private fun onArCoreSessionCreated() {
        depthApiSupported = glRenderer.arCoreManager.depthApiSupported
        eisSupported = glRenderer.arCoreManager.eisSupported
        updateStats()
    }

    private fun updateStats() {
        arCoreStats = StringBuilder()
            .append("ARCore features:\n")
            .append("\tDepth API: $depthApiSupported\n")
            .append("\tEIS: $eisSupported")
            .toString()
    }

    companion object {
        private const val TAG = "Sample5_ARCoreViewModel"
    }

}