package com.example.samplecomposearcore.arcoreutils

import android.content.Context
import android.opengl.GLES30
import com.example.samplecomposearcore.geometry.Mesh
import com.example.samplecomposearcore.geometry.Vertex
import com.example.samplecomposearcore.geometry.VertexIndex
import com.example.samplecomposearcore.opengl.RenderableMesh
import com.example.samplecomposearcore.opengl.ShaderProgram
import com.example.samplecomposearcore.scene.Camera
import com.example.samplecomposearcore.utils.ShaderReader
import com.google.ar.core.Frame
import com.google.ar.core.TrackingState

class PointCloudRenderer(context: Context) {
    private val shader = ShaderProgram(
        ShaderReader.readShader(context, "shaders/point_cloud.vert"),
            ShaderReader.readShader(context, "shaders/point_cloud.frag")
    )
    private val renderableMesh = RenderableMesh()

    private var readyToRender = false

    fun initializeGPU() {
        shader.compile()
        renderableMesh.initializeGPU()
        readyToRender = false
    }

    fun update(frame: Frame) {
        val vertices = mutableListOf<Vertex>()
        frame.acquirePointCloud().use { pointCloud ->
            val points = pointCloud.points
            points.rewind()
            while (points.hasRemaining()) {
                val x = points.get()
                val y = points.get()
                val z = points.get()
                val confidence = points.get()

                vertices.add(Vertex.Builder().position(x, y, z).normal(confidence, confidence, confidence).build())
            }
        }

        if (vertices.size > 0) {
            val indices = mutableListOf<VertexIndex>()
            indices.addAll(0..<vertices.size)

            val mesh = Mesh()
            mesh.load(vertices, indices)
            renderableMesh.updateGpuMesh(mesh)
            readyToRender = true
        }
        if (frame.camera.trackingState != TrackingState.TRACKING) {
            readyToRender = false
        }
    }

    fun render(camera: Camera) {
        if (readyToRender) {
            shader.use()
            shader.setMatrixUniform("uViewMatrix", camera.viewMatrix)
            shader.setMatrixUniform("uProjectionMatrix", camera.projectionMatrix)
            renderableMesh.render(GLES30.GL_POINTS)
        }
    }
}