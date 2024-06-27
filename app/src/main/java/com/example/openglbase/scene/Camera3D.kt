package com.example.openglbase.scene

import com.example.openglbase.math.Matrix
import com.example.openglbase.math.Vector3

class Camera3D : Camera() {
    enum class Projection {
        Perspective,
        Orthographic
    }

    private val front = Vector3.forward()

    var projection = Projection.Perspective
        private set
    var aspectRatio = 1.0f
        private set
    var nearPlane = 0.1f
        private set
    var farPlane = 100.0f
        private set
    var fieldOfView = 45.0f
        private set

    init {
        updateViewMatrix()
        updateProjectionMatrix()
    }

    override fun setViewport(width: Int, height: Int) {
        super.setViewport(width, height)
        aspectRatio = width.toFloat() / height
        updateProjectionMatrix()
    }

    fun setProjection(newProjection: Projection) {
        projection = newProjection
        updateProjectionMatrix()
    }

    fun setPosition(newPosition: Vector3) {
        worldPosition.set(newPosition)
        updateViewMatrix()
    }

    private fun updateViewMatrix() {
        val centerPoint = Vector3.add(worldPosition, front)
        val matrix = Matrix()
        android.opengl.Matrix.setLookAtM(
            matrix.data,
            0,
            worldPosition.x,
            worldPosition.y,
            worldPosition.z,
            centerPoint.x,
            centerPoint.y,
            centerPoint.z,
            upVector.x,
            upVector.y,
            upVector.z
        )
        viewMatrix.set(matrix)
    }

    private fun updateProjectionMatrix() {
        val matrix = Matrix()
        if (projection == Projection.Perspective) {
            android.opengl.Matrix.perspectiveM(
                matrix.data,
                0,
                fieldOfView,
                aspectRatio,
                nearPlane,
                farPlane
            )
        } else {
            android.opengl.Matrix.orthoM(
                matrix.data,
                0,
                -aspectRatio,
                aspectRatio,
                -1.0f,
                1.0f,
                nearPlane,
                farPlane
            )
        }
        projectionMatrix.set(matrix)
    }

}