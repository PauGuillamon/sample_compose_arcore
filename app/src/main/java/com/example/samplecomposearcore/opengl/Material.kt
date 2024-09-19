package com.example.samplecomposearcore.opengl

import com.example.samplecomposearcore.math.Matrix

interface Material {
    fun initializeGPU()
    fun restoreInitializedGPU()
    fun isGPUInitialized(): Boolean
    fun prepareForRender()
    fun setRenderProperties(
        modelMatrix: Matrix,
        viewMatrix: Matrix,
        projectionMatrix: Matrix
    )
}
