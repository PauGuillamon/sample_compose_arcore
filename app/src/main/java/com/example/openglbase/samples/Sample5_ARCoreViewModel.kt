package com.example.openglbase.samples

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.openglbase.compose.GLThreadedRenderer
import com.example.openglbase.utils.Logger

class Sample5_ARCoreViewModel(context: Context, val postOnUiThread: (Runnable) -> Unit) : ViewModel() {
    private val glRenderer = Sample5_ARCoreRenderer(context) {
        postOnUiThread {
            onArCoreSessionCreated()
        }
    }
    val renderer: GLThreadedRenderer by mutableStateOf(glRenderer)

    var fps by mutableStateOf("0")
        private set

    var showingDepthMap by mutableStateOf(false)
        private set
    var showingFeaturePoints by mutableStateOf(false)
        private set

    private var depthApiSupported = false
    var arCoreStats by mutableStateOf("")
        private set

    private var lastFrameTime = 0L
    private var framesCount = 0

    init {
        Logger.LogWarning("PGJ", "GLSceneViewModel created")
        glRenderer.onFrameFinished = {
            postOnUiThread {
                updateFps()
            }
        }
        updateStats()
    }

    override fun onCleared() {
        super.onCleared()
        Logger.LogWarning("PGJ", "GLSceneViewModel cleared")
        glRenderer.onClear()
    }

    fun deleteLast() {
        glRenderer.deleteLast()
    }

    fun toggleDepthMap(show: Boolean) {
        showingDepthMap = show
        glRenderer.showDepthMap(show)
    }

    fun toggleFeaturePoints(show: Boolean) {
        showingFeaturePoints = show
        glRenderer.showFeaturePoints(show)
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
        if (glRenderer.arCoreManager.depthApiSupported != depthApiSupported) {
            depthApiSupported = glRenderer.arCoreManager.depthApiSupported
            updateStats()
        }
    }

    private fun updateStats() {
        arCoreStats = StringBuilder()
            .append("ARCore stats:\n")
            .append("\tDepth API: $depthApiSupported")
            .toString()
    }
}