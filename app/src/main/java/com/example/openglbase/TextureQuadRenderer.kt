package com.example.openglbase

import android.content.Context
import android.opengl.GLES30
import com.example.openglbase.geometry.Generator
import com.example.openglbase.opengl.GPUTexture
import com.example.openglbase.opengl.RenderableMesh
import com.example.openglbase.opengl.ShaderProgram
import com.example.openglbase.opengl.glHasError
import com.example.openglbase.utils.ShaderReader

// TODO PGJ move to another package
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
    }
}