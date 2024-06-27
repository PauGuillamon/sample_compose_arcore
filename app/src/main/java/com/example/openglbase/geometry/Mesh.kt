package com.example.openglbase.geometry

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

class Mesh {

    var verticesBufferSize = 0
       private set
    var verticesData = FloatBuffer.allocate(0)
        private set
    var indicesDataSize = 0
        private set
    var indicesData = IntBuffer.allocate(0)
        private set

    fun load(vertices: List<Vertex>, indices: List<VertexIndex>) {
        verticesBufferSize = vertices.size * Vertex.SIZE_BYTES
        verticesData = ByteBuffer.allocateDirect(verticesBufferSize).order(ByteOrder.nativeOrder()).asFloatBuffer()
        vertices.forEach {
            verticesData.put(it.position.x)
            verticesData.put(it.position.y)
            verticesData.put(it.position.z)
            verticesData.put(it.normal.x)
            verticesData.put(it.normal.y)
            verticesData.put(it.normal.z)
            verticesData.put(it.texCoords.x)
            verticesData.put(it.texCoords.y)
        }
        indicesDataSize = indices.size * VertexIndex.SIZE_BYTES
        indicesData = ByteBuffer.allocateDirect(indicesDataSize).order(ByteOrder.nativeOrder()).asIntBuffer()
        indices.forEach {
            indicesData.put(it)
        }
    }
}