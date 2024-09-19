package com.example.samplecomposearcore.opengl

import android.content.res.AssetManager
import android.opengl.GLES30
import com.example.samplecomposearcore.math.Matrix
import com.example.samplecomposearcore.utils.ShaderReader
import com.example.samplecomposearcore.utils.loadImage

class MaterialTextured(
    assetManager: AssetManager,
    textureFilename: String,
    invertYAxis: Boolean = true
) : Material {
    private val shader = ShaderProgram(
        ShaderReader.readShader(assetManager, "shaders/material_textured.vert"),
        ShaderReader.readShader(assetManager, "shaders/material_textured.frag")
    )

    private val texture = loadImage(assetManager, textureFilename, invertYAxis)

    private lateinit var gpuTexture: GPUTexture

    private var initialized = false

    override fun initializeGPU() {
        if (initialized) {
            return
        }
        gpuTexture = GPUTexture(
            GLES30.GL_TEXTURE_2D,
            listOf(
                GLES30.GL_TEXTURE_MIN_FILTER to GLES30.GL_NEAREST,
                GLES30.GL_TEXTURE_MAG_FILTER to GLES30.GL_NEAREST,
                GLES30.GL_TEXTURE_WRAP_S to GLES30.GL_REPEAT,
                GLES30.GL_TEXTURE_WRAP_T to GLES30.GL_REPEAT,
            )
        )
        gpuTexture.uploadBitmapToGPU(GLES30.GL_RGBA, texture)
        shader.compile()
        shader.use()
        shader.setTextureSampler2D("uColorTexture", 0)
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
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(gpuTexture.target, gpuTexture.id)
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
