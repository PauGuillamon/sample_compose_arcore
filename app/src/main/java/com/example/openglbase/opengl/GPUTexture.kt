package com.example.openglbase.opengl

import android.graphics.Bitmap
import android.opengl.GLES30
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * @param target: The type of texture this object will target, such as GL_TEXTURE_2D or GL_TEXTURE_CUBE_MAP
 * @param paramsInt: list of param name and Int value pairs for this Texture. Example: {GL_TEXTURE_MIN_FILTER, GL_NEAREST}
 * @param paramsFloatVec: list of param name and float vector values pairs for this Texture. Example: {GL_TEXTURE_BORDER_COLOR, {1.0f, 1.0f, 1.0f, 1.0f}}
 */
class GPUTexture(
    val target: Int,
    paramsInt: List<Pair<Int, Int>>,
    paramsFloatVec: List<Pair<Int, FloatArray>> = emptyList()
) {
    var id: Int
        private set

    var width = 0
        private set
    var height = 0
        private set

    init {
        val values = IntArray(1)
        GLES30.glGenTextures(1, values, 0)
        val textureId = values[0]
        GLES30.glBindTexture(target, textureId)
        paramsInt.forEach {
            GLES30.glTexParameteri(target, it.first, it.second)
        }
        paramsFloatVec.forEach {
            if (it.second.size < 4) {
                throw IllegalArgumentException("Float vector param requires 4 parameters.")
            }
            GLES30.glTexParameterfv(target, it.first, it.second, 0)
        }
        GLES30.glBindTexture(target, 0)

        id = if (!glHasError()) textureId else INVALID_ID
    }

    fun bind() {
        GLES30.glBindTexture(target, id)
    }

    fun unbind() {
        GLES30.glBindTexture(target, 0)
    }

    fun clearGPU() {
        if (id != INVALID_ID) {
            val values = IntArray(1)
            values[0] = id
            GLES30.glDeleteTextures(1, values, 0)
            id = INVALID_ID
        }
    }

    /**
     * See https://registry.khronos.org/OpenGL-Refpages/es3/html/glTexImage2D.xhtml
     */
    fun allocateTextureMemory(
        textureWidth: Int,
        textureHeight: Int,
        internalFormat: Int,
        format: Int,
        type: Int
    ) {
        bind()
        glTexImage2D(textureWidth, textureHeight, internalFormat, format, type, null)
        unbind()
    }

    /**
     * See https://registry.khronos.org/OpenGL-Refpages/es3/html/glTexImage2D.xhtml
     */
    fun uploadTextureDataToGPU(textureWidth: Int, textureHeight: Int, internalFormat: Int, format: Int, type: Int, data: Buffer) {
        data.rewind()
        bind()
        glTexImage2D(textureWidth, textureHeight, internalFormat, format, type, data)
        GLES30.glGenerateMipmap(target)
        unbind()
    }

    fun uploadBitmapToGPU(internalFormat: Int, bitmap: Bitmap) {
        val capacity = bitmap.width * bitmap.height * 4
        val byteBuffer = ByteBuffer.allocateDirect(capacity)
        byteBuffer.order(ByteOrder.nativeOrder())
        byteBuffer.position(0)
        bitmap.copyPixelsToBuffer(byteBuffer)
        byteBuffer.position(0)
        uploadTextureDataToGPU(bitmap.width, bitmap.height, internalFormat, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, byteBuffer)
    }

    private fun glTexImage2D(textureWidth: Int, textureHeight: Int, internalFormat: Int, format: Int, type: Int, data: Buffer?) {
        width = textureWidth
        height = textureHeight
        GLES30.glTexImage2D(target, 0, internalFormat, width, height, 0, format, type, data)
    }

    companion object {
        private const val INVALID_ID = -1
    }
}