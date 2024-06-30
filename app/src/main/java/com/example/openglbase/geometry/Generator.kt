package com.example.openglbase.geometry

object Generator {

    fun generateScreenQuad(
        coords3D: FloatArray = defaultQuadCoords3D(),
        normals: FloatArray = defaultQuadNormals(),
        texCoords: FloatArray = defaultQuadTexCoords()
    ): Mesh {
        if (coords3D.size < 12) {
            throw IllegalArgumentException("texCoords must have at least 12 elements, but got ${coords3D.size}")
        }
        if (normals.size < 12) {
            throw IllegalArgumentException("texCoords must have at least 12 elements, but got ${normals.size}")
        }
        if (texCoords.size < 8) {
            throw IllegalArgumentException("texCoords must have at least 8 elements, but got ${texCoords.size}")
        }
        val vertices = mutableListOf<Vertex>()
        val indices = mutableListOf<VertexIndex>()

        vertices.addAll(listOf(
            Vertex.Builder().position(coords3D[0], coords3D[1], coords3D[2]).normal(normals[0], normals[1], normals[2]).texCoords(texCoords[0], texCoords[1]).build(),
            Vertex.Builder().position(coords3D[3], coords3D[4], coords3D[5]).normal(normals[3], normals[4], normals[5]).texCoords(texCoords[2], texCoords[3]).build(),
            Vertex.Builder().position(coords3D[6], coords3D[7], coords3D[8]).normal(normals[6], normals[7], normals[8]).texCoords(texCoords[4], texCoords[5]).build(),
            Vertex.Builder().position(coords3D[9], coords3D[10], coords3D[11]).normal(normals[9], normals[10], normals[11]).texCoords(texCoords[6], texCoords[7]).build()
        ))
        indices.addAll(listOf(
            0, 1, 2,
            0, 2, 3
        ))

        val mesh = Mesh()
        mesh.load(vertices, indices)
        return mesh
    }

    private fun defaultQuadCoords3D(): FloatArray = floatArrayOf(
        -1.0f, -1.0f, 0.0f,
        +1.0f, -1.0f, 0.0f,
        +1.0f, +1.0f, 0.0f,
        -1.0f, +1.0f, 0.0f
    )

    private fun defaultQuadNormals(): FloatArray = floatArrayOf(
        0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f
    )

    private fun defaultQuadTexCoords(): FloatArray = floatArrayOf(
        0.0f, 0.0f,
        1.0f, 0.0f,
        1.0f, 1.0f,
        0.0f, 1.0f
    )

    fun generateCube(size: Float): Mesh {
        return generatePrism(size, size, size)
    }

    fun generatePrism(width: Float, height: Float, depth: Float): Mesh {
        val halfWidth = width / 2.0f
        val halfHeight = height / 2.0f
        val halfDepth = depth / 2.0f
        val vertices = mutableListOf<Vertex>()
        val indices = mutableListOf<VertexIndex>()

        // Front face
        vertices.addAll(listOf(
            Vertex.Builder().position(-halfWidth, -halfHeight, +halfDepth).normal(0.0f, 0.0f, -1.0f).texCoords(0.0f, 0.0f).build(),
            Vertex.Builder().position(+halfWidth, -halfHeight, +halfDepth).normal(0.0f, 0.0f, -1.0f).texCoords(1.0f, 0.0f).build(),
            Vertex.Builder().position(+halfWidth, +halfHeight, +halfDepth).normal(0.0f, 0.0f, -1.0f).texCoords(1.0f, 1.0f).build(),
            Vertex.Builder().position(-halfWidth, +halfHeight, +halfDepth).normal(0.0f, 0.0f, -1.0f).texCoords(0.0f, 1.0f).build()
        ))
        indices.addAll(listOf(
            0, 1, 2,
            0, 2, 3
        ))

        // Right face
        vertices.addAll(listOf(
            Vertex.Builder().position(+halfWidth, -halfHeight, +halfDepth).normal(1.0f, 0.0f, 0.0f).texCoords(0.0f, 0.0f).build(),
            Vertex.Builder().position(+halfWidth, -halfHeight, -halfDepth).normal(1.0f, 0.0f, 0.0f).texCoords(1.0f, 0.0f).build(),
            Vertex.Builder().position(+halfWidth, +halfHeight, -halfDepth).normal(1.0f, 0.0f, 0.0f).texCoords(1.0f, 1.0f).build(),
            Vertex.Builder().position(+halfWidth, +halfHeight, +halfDepth).normal(1.0f, 0.0f, 0.0f).texCoords(0.0f, 1.0f).build()
        ))
        indices.addAll(listOf(
            4, 5, 6,
            4, 6, 7
        ))

        // Back face
        vertices.addAll(listOf(
            Vertex.Builder().position(+halfWidth, -halfHeight, -halfDepth).normal(0.0f, 0.0f, 1.0f).texCoords(0.0f, 0.0f).build(),
            Vertex.Builder().position(-halfWidth, -halfHeight, -halfDepth).normal(0.0f, 0.0f, 1.0f).texCoords(1.0f, 0.0f).build(),
            Vertex.Builder().position(-halfWidth, +halfHeight, -halfDepth).normal(0.0f, 0.0f, 1.0f).texCoords(1.0f, 1.0f).build(),
            Vertex.Builder().position(+halfWidth, +halfHeight, -halfDepth).normal(0.0f, 0.0f, 1.0f).texCoords(0.0f, 1.0f).build()
        ))
        indices.addAll(listOf(
            8, 9, 10,
            8, 10, 11
        ))

        // Left face
        vertices.addAll(listOf(
            Vertex.Builder().position(-halfWidth, -halfHeight, -halfDepth).normal(-1.0f, 0.0f, 0.0f).texCoords(0.0f, 0.0f).build(),
            Vertex.Builder().position(-halfWidth, -halfHeight, +halfDepth).normal(-1.0f, 0.0f, 0.0f).texCoords(1.0f, 0.0f).build(),
            Vertex.Builder().position(-halfWidth, +halfHeight, +halfDepth).normal(-1.0f, 0.0f, 0.0f).texCoords(1.0f, 1.0f).build(),
            Vertex.Builder().position(-halfWidth, +halfHeight, -halfDepth).normal(-1.0f, 0.0f, 0.0f).texCoords(0.0f, 1.0f).build()
        ))
        indices.addAll(listOf(
            12, 13, 14,
            12, 14, 15
        ))

        // Top face
        vertices.addAll(listOf(
            Vertex.Builder().position(-halfWidth, +halfHeight, +halfDepth).normal(0.0f, 1.0f, 0.0f).texCoords(0.0f, 0.0f).build(),
            Vertex.Builder().position(+halfWidth, +halfHeight, +halfDepth).normal(0.0f, 1.0f, 0.0f).texCoords(1.0f, 0.0f).build(),
            Vertex.Builder().position(+halfWidth, +halfHeight, -halfDepth).normal(0.0f, 1.0f, 0.0f).texCoords(1.0f, 1.0f).build(),
            Vertex.Builder().position(-halfWidth, +halfHeight, -halfDepth).normal(0.0f, 1.0f, 0.0f).texCoords(0.0f, 1.0f).build()
        ))
        indices.addAll(listOf(
            16, 17, 18,
            16, 18, 19
        ))

        // Bottom face
        vertices.addAll(listOf(
            Vertex.Builder().position(-halfWidth, -halfHeight, -halfDepth).normal(0.0f, -1.0f, 0.0f).texCoords(0.0f, 0.0f).build(),
            Vertex.Builder().position(+halfWidth, -halfHeight, -halfDepth).normal(0.0f, -1.0f, 0.0f).texCoords(1.0f, 0.0f).build(),
            Vertex.Builder().position(+halfWidth, -halfHeight, +halfDepth).normal(0.0f, -1.0f, 0.0f).texCoords(1.0f, 1.0f).build(),
            Vertex.Builder().position(-halfWidth, -halfHeight, +halfDepth).normal(0.0f, -1.0f, 0.0f).texCoords(0.0f, 1.0f).build()
        ))
        indices.addAll(listOf(
            20, 21, 22,
            20, 22, 23
        ))

        val mesh = Mesh()
        mesh.load(vertices, indices)
        return mesh
    }
}