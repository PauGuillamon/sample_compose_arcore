package com.example.samplecomposearcore.opengl

import android.content.res.AssetManager
import com.example.samplecomposearcore.math.Matrix
import com.example.samplecomposearcore.math.Vector3
import com.example.samplecomposearcore.utils.ShaderReader

class MaterialColored(
    assetManager: AssetManager,
    private val color: Vector3
) : Material {
    private val shader = ShaderProgram(
        ShaderReader.readShader(assetManager, "shaders/material_colored.vert"),
        ShaderReader.readShader(assetManager, "shaders/material_colored.frag")
    )

    private var initialized = false

    override fun initializeGPU() {
        if (initialized) {
            return
        }
        shader.compile()
        initialized = true
    }

    override fun restoreInitializedGPU() {
        initialized = false
    }

    override fun isGPUInitialized(): Boolean {
        return initialized
    }

    override fun prepareForRender() {
        shader.use()
        shader.setVector3Uniform("uColor", color)
    }

    override fun setRenderProperties(
        modelMatrix: Matrix,
        viewMatrix: Matrix,
        projectionMatrix: Matrix
    ) {
        shader.setMatrixUniform("uModelMatrix", modelMatrix)
        shader.setMatrixUniform("uViewMatrix", viewMatrix)
        shader.setMatrixUniform("uProjectionMatrix", projectionMatrix)
    }
}
