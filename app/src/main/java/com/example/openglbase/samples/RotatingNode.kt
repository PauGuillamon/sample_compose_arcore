package com.example.openglbase.samples

import com.example.openglbase.math.Quaternion
import com.example.openglbase.math.Vector3
import com.example.openglbase.scene.Node

class RotatingNode : Node() {
    private var cubeRotationX = 0f
    private var cubeRotationY = 0f

    override fun update(deltaTime: Float) {
        cubeRotationX += 30f * deltaTime
        cubeRotationY += 60f * deltaTime
        localRotation = Quaternion.eulerAngles(Vector3(cubeRotationX, cubeRotationY, 0.0f))
    }
}