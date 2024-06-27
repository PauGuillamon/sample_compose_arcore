package com.example.openglbase.arcoreutils

import android.content.Context
import android.opengl.GLES11Ext
import android.opengl.GLES30
import com.example.openglbase.geometry.Generator
import com.example.openglbase.opengl.GPUTexture
import com.example.openglbase.opengl.RenderableMesh
import com.example.openglbase.opengl.ShaderProgram
import com.example.openglbase.opengl.glHasError
import com.example.openglbase.utils.ShaderReader
import com.google.ar.core.Coordinates2d
import com.google.ar.core.Frame

class CameraImageRenderer(context: Context) {
    private val backgroundShader = ShaderProgram(
        ShaderReader.readShader(context, "shaders/external_OES_quad.vert"),
        ShaderReader.readShader(context, "shaders/external_OES_quad.frag")
    )

    private val backgroundRenderableMesh = RenderableMesh().apply {
        setMesh(Generator.generateScreenQuad())
    }

    // ARCore will output the camera image into the memory held by this texture.
    lateinit var texture: GPUTexture
        private set

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

    fun update(frame: Frame) {
        if (frame.hasDisplayGeometryChanged()) {
            val verticesNdc = floatArrayOf(
                -1f, -1f,
                +1f, -1f,
                +1f, +1f,
                -1f, +1f
            )
            val texCoords = FloatArray(8)
            frame.transformCoordinates2d(
                Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES,
                verticesNdc,
                Coordinates2d.TEXTURE_NORMALIZED,
                texCoords
            )
            backgroundRenderableMesh.updateGpuMesh(Generator.generateScreenQuad(texCoords))
        }
    }

    fun render() {
        GLES30.glDisable(GLES30.GL_DEPTH_TEST)
        backgroundShader.use()
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture.id)
        backgroundRenderableMesh.render()
    }
}