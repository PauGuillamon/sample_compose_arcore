package com.example.openglbase.opengl

import android.opengl.GLES30

// TODO PGJ document it's a simple framebuffer implementation
class Framebuffer(
    val width: Int,
    val height: Int,
    private val colorBufferInternalFormat: Int = GLES30.GL_RGBA,
    private val depthAsReadableTexture: Boolean = false,
) {

    private var id: Int = INVALID_ID
    private var renderBufferObject = INVALID_ID

    lateinit var colorTexture: GPUTexture
        private set
    var depthTexture: GPUTexture? = null
        private set

    @OptIn(ExperimentalStdlibApi::class)
    fun initializeGPU() {
        if (id != INVALID_ID) {
            clearGPU()
        }
        if (width == 0 || height == 0) {
            return
        }
        val values = IntArray(1)
        GLES30.glGenFramebuffers(1, values, 0)
        if (glHasError()) {
            id = INVALID_ID
            throw IllegalStateException("Failed to create framebuffer")
        }
        id = values[0]

        colorTexture = GPUTexture(
            GLES30.GL_TEXTURE_2D,
            listOf(
                GLES30.GL_TEXTURE_MIN_FILTER to GLES30.GL_LINEAR,
                GLES30.GL_TEXTURE_MAG_FILTER to GLES30.GL_LINEAR,
                GLES30.GL_TEXTURE_WRAP_S to GLES30.GL_CLAMP_TO_EDGE,
                GLES30.GL_TEXTURE_WRAP_T to GLES30.GL_CLAMP_TO_EDGE,
            )
        )
        val (format, type) = textureFormatFromInternalFormat(colorBufferInternalFormat)
        colorTexture.allocateTextureMemory(
            width,
            height,
            internalFormat = colorBufferInternalFormat,
            format = format,
            type = type
        )

        if (depthAsReadableTexture) {
            depthTexture = GPUTexture(
                GLES30.GL_TEXTURE_2D,
                listOf(
                    GLES30.GL_TEXTURE_MIN_FILTER to GLES30.GL_NEAREST,
                    GLES30.GL_TEXTURE_MAG_FILTER to GLES30.GL_NEAREST,
                    GLES30.GL_TEXTURE_WRAP_S to GLES30.GL_CLAMP_TO_EDGE,
                    GLES30.GL_TEXTURE_WRAP_T to GLES30.GL_CLAMP_TO_EDGE,
                )
            )
            depthTexture?.allocateTextureMemory(
                width,
                height,
                internalFormat = GLES30.GL_DEPTH_COMPONENT32F,
                format = GLES30.GL_DEPTH_COMPONENT,
                type = GLES30.GL_FLOAT
            )
        }

        bindBlock {
            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, colorTexture.id, 0)
            if (depthTexture != null) {
                GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GLES30.GL_TEXTURE_2D, depthTexture!!.id, 0)
            } else {
                GLES30.glGenRenderbuffers(1, values, 0)
                renderBufferObject = values[0]
                GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, renderBufferObject)
                GLES30.glRenderbufferStorage(GLES30.GL_RENDERBUFFER, GLES30.GL_DEPTH24_STENCIL8, width, height)
                GLES30.glFramebufferRenderbuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_STENCIL_ATTACHMENT, GLES30.GL_RENDERBUFFER, renderBufferObject)
                GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, 0)
            }
            val status = GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER)
            if (status != GLES30.GL_FRAMEBUFFER_COMPLETE) {
                throw IllegalStateException("Framebuffer not complete. Status: ${status.toHexString()}")
            }
        }
    }

    fun clearGPU() {
        if (id != INVALID_ID) {
            val values = IntArray(1)
            values[0] = id
            GLES30.glDeleteFramebuffers(1, values, 0)
            id = INVALID_ID

            if (renderBufferObject != INVALID_ID) {
                values[0] = renderBufferObject
                GLES30.glDeleteRenderbuffers(1, values, 0)
                renderBufferObject = INVALID_ID
            }

            colorTexture.clearGPU()
            depthTexture?.clearGPU()
            depthTexture = null
        }
    }

    fun bind() {
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, id)
    }

    fun unbind() {
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
    }

    // TODO PGJ make private? or add docs
    inline fun bindBlock(block: (it: Framebuffer) -> Unit) {
        bind()
        block(this)
        unbind()
    }

    private fun textureFormatFromInternalFormat(internalFormat: Int): Pair<Int, Int> {
        return when (internalFormat) {
            GLES30.GL_RGBA -> GLES30.GL_RGBA to GLES30.GL_UNSIGNED_BYTE
            GLES30.GL_DEPTH_COMPONENT32F -> GLES30.GL_DEPTH_COMPONENT to GLES30.GL_FLOAT
            GLES30.GL_R16F -> GLES30.GL_RED to GLES30.GL_FLOAT
            GLES30.GL_R32F -> GLES30.GL_RED to GLES30.GL_FLOAT
            GLES30.GL_RG16F -> GLES30.GL_RG to GLES30.GL_FLOAT
            GLES30.GL_RG32F -> GLES30.GL_RG to GLES30.GL_FLOAT
            else -> throw NotImplementedError("Internal format: $internalFormat not implemented yet.")
        }
    }

    companion object {
        private const val INVALID_ID = -1
    }
}