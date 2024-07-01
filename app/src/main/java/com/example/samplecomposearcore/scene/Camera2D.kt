package com.example.samplecomposearcore.scene

import com.example.samplecomposearcore.math.Matrix
import com.example.samplecomposearcore.math.Quaternion
import com.example.samplecomposearcore.math.Vector3

/**
 * [Camera2D] sets a 2D coordinate system with Y origin at the top-left corner and Y increasing going down.
 */
class Camera2D : Camera() {
    init {
        worldPosition.set(0f, 0f, 1f)
        val matrix = Matrix()
        val centerPoint = Vector3.forward()
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

    override fun setViewport(width: Int, height: Int) {
        super.setViewport(width, height)
        val matrix = Matrix()
        android.opengl.Matrix.orthoM(
            matrix.data,
            0,
            0f,
            viewWidth.toFloat(),
            viewHeight.toFloat(),
            0f,
            0.01f,
            100f
        )
        projectionMatrix.set(matrix)
    }

    companion object {
        /**
         * Adjusts a [matrix] to a 2D coordinate system by changing the Y axis so that Y origin
         * is at the top-left corner and Y increases going down. This function is useful to
         * adjust 3D objects to a 2D coordinate system without changing their triangle winding.
         *
         * @param [maxY] the highest Y value of an object. It's not the same as the object's height.
         */
        fun adjustToCoordinateSystem2D(matrix: Matrix, maxY: Float): Matrix {
            val adjustedMatrix = Matrix()
            adjustedMatrix.makeTrs(
                Vector3(0.0f, maxY, 0.0f),
                Quaternion.identity(),
                Vector3(1.0f, -1.0f, 1.0f)
            )
            Matrix.multiply(matrix, adjustedMatrix, adjustedMatrix)
            return adjustedMatrix
        }
    }
}