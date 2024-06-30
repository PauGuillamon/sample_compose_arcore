package com.example.openglbase.geometry

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

class Mesh {

    var verticesBuffer = FloatBuffer.allocate(0)
        private set
    var verticesBufferSize = 0
       private set

    var indicesBuffer = IntBuffer.allocate(0)
        private set
    var indicesBufferSize = 0
        private set
    var indicesCount = 0
        private set

    fun copy(): Mesh {
        val newMesh = Mesh()
        newMesh.verticesBuffer = verticesBuffer
        newMesh.verticesBufferSize = verticesBufferSize

        newMesh.indicesBuffer = indicesBuffer
        newMesh.indicesBufferSize = indicesBufferSize
        newMesh.indicesCount = indicesCount
        return newMesh
    }

    fun load(vertices: List<Vertex>, indices: List<VertexIndex>) {
        verticesBufferSize = vertices.size * Vertex.SIZE_BYTES
        verticesBuffer = ByteBuffer.allocateDirect(verticesBufferSize).order(ByteOrder.nativeOrder()).asFloatBuffer()
        vertices.forEach {
            verticesBuffer.put(it.position.x)
            verticesBuffer.put(it.position.y)
            verticesBuffer.put(it.position.z)
            verticesBuffer.put(it.normal.x)
            verticesBuffer.put(it.normal.y)
            verticesBuffer.put(it.normal.z)
            verticesBuffer.put(it.texCoords.x)
            verticesBuffer.put(it.texCoords.y)
        }
        indicesCount = indices.size
        indicesBufferSize = indices.size * VertexIndex.SIZE_BYTES
        indicesBuffer = ByteBuffer.allocateDirect(indicesBufferSize).order(ByteOrder.nativeOrder()).asIntBuffer()
        indices.forEach {
            indicesBuffer.put(it)
        }
    }
}