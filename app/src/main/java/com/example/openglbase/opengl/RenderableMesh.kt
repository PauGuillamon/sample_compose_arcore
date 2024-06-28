package com.example.openglbase.opengl

import android.opengl.GLES30
import com.example.openglbase.geometry.Mesh

class RenderableMesh {
    var VAO = 0
        private set
    var VBO = 0
        private set
    var EBO = 0
        private set

    private var mesh: Mesh? = null
    private var uploaded = false

    fun setMesh(newMesh: Mesh) {
        mesh = newMesh.copy()
    }

    fun updateGpuMesh(newMesh: Mesh) {
        mesh = newMesh.copy()
        uploadToGPU()
    }

    fun clearGPU() {
        if (VAO != 0) {
            val values = IntArray(2)
            values[0] = VAO
            GLES30.glDeleteVertexArrays(1, values, 0)
            VAO = 0

            values[0] = VBO
            values[1] = EBO
            GLES30.glDeleteBuffers(2, values, 0)
            VBO = 0
            EBO = 0
        }
        uploaded = false
    }

    fun initializeGPU() {
        val intArray = IntArray(2)
        GLES30.glGenVertexArrays(1, intArray, 0)
        VAO = intArray[0]

        GLES30.glGenBuffers(2, intArray, 0)
        VBO = intArray[0]
        EBO = intArray[1]
        uploaded = false
    }

    fun uploadToGPU() {
        val mesh = mesh ?: throw IllegalStateException("Mesh was not set.")
        if (mesh.verticesBufferSize == 0 || mesh.indicesDataSize == 0) {
            throw IllegalStateException("RenderableMesh was not set with Mesh data.")
        }
        if (VAO == 0) {
            throw IllegalStateException("RenderableMesh was not initialized.")
        }

        GLES30.glBindVertexArray(VAO)

        mesh.verticesData.rewind()
        mesh.indicesData.rewind()

        // Uploads vertex data to GPU
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, VBO)
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, mesh.verticesBufferSize, mesh.verticesData, GLES30.GL_STATIC_DRAW)

        // Uploads element indices to GPU
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, EBO)
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, mesh.indicesDataSize, mesh.indicesData, GLES30.GL_STATIC_DRAW)

        val stride = 8 * Float.SIZE_BYTES
        // Sets up Buffer offsets to specific set of data
        // Vertices
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, stride, 0)
        GLES30.glEnableVertexAttribArray(0)

        // Normals
        val offsetToNormals = 3 * Float.SIZE_BYTES
        GLES30.glVertexAttribPointer(1, 3, GLES30.GL_FLOAT, false, stride, offsetToNormals)
        GLES30.glEnableVertexAttribArray(1)

        // Texture Coordinates
        val offsetToTexCoords = 6 * Float.SIZE_BYTES
        GLES30.glVertexAttribPointer(2, 2, GLES30.GL_FLOAT, false, stride, offsetToTexCoords)
        GLES30.glEnableVertexAttribArray(2)

        GLES30.glBindVertexArray(0)
        glHasError()
        uploaded = true
    }

    fun render(primitive: Int = GLES30.GL_TRIANGLES) {
        val mesh = mesh ?: throw IllegalStateException("Mesh was not set.")
        if (!uploaded) throw IllegalStateException("Mesh was uploaded to GPU.")
        GLES30.glBindVertexArray(VAO)
        GLES30.glDrawElements(primitive, mesh.indicesDataSize, GLES30.GL_UNSIGNED_INT, 0)
        GLES30.glBindVertexArray(0)
        glHasError()
    }
}