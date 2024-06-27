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
class SceneOcclusionQuadRenderer(context: Context) {
    private val shader = ShaderProgram(
        ShaderReader.readShader(context, "shaders/scene_occlusion_quad.vert"),
        ShaderReader.readShader(context, "shaders/scene_occlusion_quad.frag")
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

    /**
     * Combines the scene color and depth textures into a single texture.
     * If depthMapTexture is provided, it is compared the scene depth texture
     * and the virtual scene will be occluded (hidden) if it the scene falls
     * behind the depth map.
     */
    fun render(
        sceneColorTexture: GPUTexture,
        sceneDepthTexture: GPUTexture,
        depthMapTexture: GPUTexture?
    ) {
        shader.use()
        shader.setTextureSampler2D("uSceneColorTexture", 0)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        sceneColorTexture.bind()
        shader.setTextureSampler2D("uSceneDepthTexture", 1)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
        sceneDepthTexture.bind()
        val depthMapAvailable = depthMapTexture != null
        shader.setBoolUniform("uDepthMapAvailable", depthMapAvailable)
        if (depthMapAvailable) {
            shader.setTextureSampler2D("uDepthMapTexture", 2)
            GLES30.glActiveTexture(GLES30.GL_TEXTURE2)
            depthMapTexture!!.bind()
        }
        renderableQuad.render()
    }
}