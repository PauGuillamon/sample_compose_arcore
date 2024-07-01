package com.example.samplecomposearcore.geometry

import com.example.samplecomposearcore.math.Vector2
import com.example.samplecomposearcore.math.Vector3

typealias VertexIndex = Int

class Vertex(val position: Vector3, val normal: Vector3, val texCoords: Vector2) {
    class Builder {
        private var position = Vector3(0.0f, 0.0f, 0.0f)
        private var normal = Vector3(0.0f, 0.0f, 0.0f)
        private var texCoords = Vector2(0.0f, 0.0f)

        fun position(position: Vector3) = apply { this.position = position }
        fun position(x: Float, y: Float, z: Float) = apply { this.position = Vector3(x, y, z) }

        fun normal(normal: Vector3) = apply { this.normal = normal }
        fun normal(x: Float, y: Float, z: Float) = apply { this.normal = Vector3(x, y, z) }

        fun texCoords(texCoords: Vector2) = apply { this.texCoords = texCoords }
        fun texCoords(u: Float, v: Float) = apply { this.texCoords = Vector2(u, v) }

        fun build(): Vertex {
            return Vertex(position, normal, texCoords)
        }
    }

    companion object {
        const val SIZE_BYTES = (Vector3.SIZE_BYTES * 2) + Vector2.SIZE_BYTES
    }
}