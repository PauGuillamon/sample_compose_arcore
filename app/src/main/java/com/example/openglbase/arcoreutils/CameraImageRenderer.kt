package com.example.openglbase.arcoreutils

import android.content.Context
import android.opengl.GLES11Ext
import android.opengl.GLES30
import com.example.openglbase.geometry.Generator
import com.example.openglbase.geometry.Mesh
import com.example.openglbase.opengl.GPUTexture
import com.example.openglbase.opengl.RenderableMesh
import com.example.openglbase.opengl.ShaderProgram
import com.example.openglbase.opengl.glHasError
import com.example.openglbase.utils.ShaderReader

class CameraImageRenderer(context: Context) {
    private val backgroundShader = ShaderProgram(
        ShaderReader.readShader(context, "shaders/camera_image.vert"),
        ShaderReader.readShader(context, "shaders/camera_image.frag")
    )

    private val backgroundRenderableMesh = RenderableMesh().apply {
        setMesh(Generator.generateScreenQuad())
    }

    // ARCore will output the camera image into the memory held by this texture.
    lateinit var texture: GPUTexture
        private set

    private var use3DCoords = false

    fun initializeGPU() {
        texture = GPUTexture(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            listOf(
                GLES30.GL_TEXTURE_MIN_FILTER to GLES30.GL_LINEAR,
                GLES30.GL_TEXTURE_MAG_FILTER to GLES30.GL_LINEAR,
                GLES30.GL_TEXTURE_WRAP_S to GLES30.GL_CLAMP_TO_EDGE,
                GLES30.GL_TEXTURE_WRAP_T to GLES30.GL_CLAMP_TO_EDGE,
            )
        )

        backgroundRenderableMesh.initializeGPU()
        backgroundRenderableMesh.uploadToGPU()
        backgroundShader.compile()
        backgroundShader.use()
        backgroundShader.setTextureSampler2D("uColorTexture", 0)
        glHasError("CameraImageRenderer initializeGPU")
    }

    fun updateMesh(mesh: Mesh, using3DCoords: Boolean) {
        use3DCoords = using3DCoords
        backgroundRenderableMesh.updateGpuMesh(mesh)
    }

    fun render() {
        GLES30.glDisable(GLES30.GL_DEPTH_TEST)
        backgroundShader.use()
        backgroundShader.setBoolUniform("uUseTexCoords3D", use3DCoords)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture.id)
        backgroundRenderableMesh.render()
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)
    }
}