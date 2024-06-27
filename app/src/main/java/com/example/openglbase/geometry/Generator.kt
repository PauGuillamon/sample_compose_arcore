package com.example.openglbase.geometry

object Generator {
    fun generateScreenQuad(): Mesh {
        return generateScreenQuad(floatArrayOf(
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f
        ))
    }
    fun generateScreenQuad(texCoords: FloatArray): Mesh {
        if (texCoords.size < 8) {
            throw IllegalArgumentException("texCoords must have at least 8 elements, but got ${texCoords.size}")
        }
        val vertices = mutableListOf<Vertex>()
        val indices = mutableListOf<VertexIndex>()

        vertices.addAll(listOf(
            Vertex.Builder().position(-1.0f, -1.0f, 0.0f).normal(0.0f, 0.0f, 1.0f).texCoords(texCoords[0], texCoords[1]).build(),
            Vertex.Builder().position(+1.0f, -1.0f, 0.0f).normal(0.0f, 0.0f, 1.0f).texCoords(texCoords[2], texCoords[3]).build(),
            Vertex.Builder().position(+1.0f, +1.0f, 0.0f).normal(0.0f, 0.0f, 1.0f).texCoords(texCoords[4], texCoords[5]).build(),
            Vertex.Builder().position(-1.0f, +1.0f, 0.0f).normal(0.0f, 0.0f, 1.0f).texCoords(texCoords[6], texCoords[7]).build()
        ))
        indices.addAll(listOf(
            0, 1, 2,
            0, 2, 3
        ))

        val mesh = Mesh()
        mesh.load(vertices, indices)
        return mesh
    }

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