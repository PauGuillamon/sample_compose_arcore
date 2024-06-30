package com.example.openglbase.arcoreutils

import android.content.Context
import android.opengl.GLES30
import com.example.openglbase.geometry.Generator
import com.example.openglbase.geometry.Mesh
import com.example.openglbase.opengl.GPUTexture
import com.example.openglbase.opengl.RenderableMesh
import com.example.openglbase.opengl.ShaderProgram
import com.example.openglbase.opengl.glHasError
import com.example.openglbase.utils.ShaderReader
import com.example.openglbase.utils.loadImage

class DepthImageRenderer(context: Context) {

    private val backgroundShader = ShaderProgram(
        ShaderReader.readShader(context, "shaders/camera_image.vert"),
        ShaderReader.readShader(context, "shaders/arcore_utils/depth_map.frag")
    )

    val backgroundRenderableMesh = RenderableMesh().apply {
        setMesh(Generator.generateScreenQuad())
    }

    private val depthColorPaletteBitmap = loadImage(context.assets, "textures/depth_color_palette.png", false)
    private lateinit var depthColorPaletteTexture: GPUTexture

    private var use3DCoords = false

    fun initializeGPU() {
        backgroundRenderableMesh.initializeGPU()
        backgroundRenderableMesh.uploadToGPU()
        backgroundShader.compile()
        backgroundShader.use()
        backgroundShader.setTextureSampler2D("uDepthTexture", 0)
        backgroundShader.setTextureSampler2D("uDepthColorPaletteTexture", 1)

        depthColorPaletteTexture = GPUTexture(
            GLES30.GL_TEXTURE_2D,
            listOf(
                GLES30.GL_TEXTURE_MIN_FILTER to GLES30.GL_NEAREST,
                GLES30.GL_TEXTURE_MAG_FILTER to GLES30.GL_NEAREST,
                GLES30.GL_TEXTURE_WRAP_S to GLES30.GL_CLAMP_TO_EDGE,
                GLES30.GL_TEXTURE_WRAP_T to GLES30.GL_CLAMP_TO_EDGE,
            )
        )
        depthColorPaletteTexture.uploadBitmapToGPU(GLES30.GL_RGBA, depthColorPaletteBitmap)
        glHasError("DepthImageRenderer initializeGPU")
    }

    fun updateMesh(mesh: Mesh, using3DCoords: Boolean) {
        use3DCoords = using3DCoords
        backgroundRenderableMesh.updateGpuMesh(mesh)
    }

    fun render(depthTexture: GPUTexture) {
        GLES30.glDisable(GLES30.GL_DEPTH_TEST)
        backgroundShader.use()
        backgroundShader.setBoolUniform("uDepthRawData", false)
        backgroundShader.setBoolUniform("uUseTexCoords3D", use3DCoords)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, depthTexture.id)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, depthColorPaletteTexture.id)
        backgroundRenderableMesh.render()
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
    }

    /**
     * Renders the depth map raw data to a full-screen quad. Camera coordinates
     * adjusted so the resulting color texture can be sampled using simple UV
     * coordinates without any further transformation.
     *
     * The currently bound color buffer is expected to have internalFormat GL_RG16F or GL_RG32F.
     */
    fun renderRawData(depthTexture: GPUTexture) {
        GLES30.glDisable(GLES30.GL_DEPTH_TEST)
        backgroundShader.use()
        backgroundShader.setBoolUniform("uDepthRawData", true)
        backgroundShader.setBoolUniform("uUseTexCoords3D", use3DCoords)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, depthTexture.id)
        backgroundRenderableMesh.render()
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
    }
}