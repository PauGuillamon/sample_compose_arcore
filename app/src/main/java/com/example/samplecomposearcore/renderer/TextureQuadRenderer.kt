package com.example.samplecomposearcore.renderer

import android.content.Context
import android.opengl.GLES30
import com.example.samplecomposearcore.geometry.Generator
import com.example.samplecomposearcore.opengl.GPUTexture
import com.example.samplecomposearcore.opengl.RenderableMesh
import com.example.samplecomposearcore.opengl.ShaderProgram
import com.example.samplecomposearcore.opengl.glHasError
import com.example.samplecomposearcore.utils.ShaderReader

class TextureQuadRenderer(context: Context) {
    private val shader = ShaderProgram(
        ShaderReader.readShader(context, "shaders/quad.vert"),
        ShaderReader.readShader(context, "shaders/quad.frag")
    )

    private val renderableQuad = RenderableMesh().apply {
        setMesh(Generator.generateScreenQuad())
    }

    fun initializeGPU() {
        shader.compile()
        renderableQuad.initializeGPU()
        renderableQuad.uploadToGPU()
        glHasError("TextureQuadRenderer")
    }

    fun clearGPU() {
        shader.delete()
        renderableQuad.clearGPU()
    }

    fun render(texture: GPUTexture) {
        shader.use()
        shader.setTextureSampler2D("uColorTexture", 0)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        texture.bind()
        renderableQuad.render()
        texture.unbind()
    }
}