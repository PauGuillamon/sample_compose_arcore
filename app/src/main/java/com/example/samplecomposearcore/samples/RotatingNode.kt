package com.example.samplecomposearcore.samples

import com.example.samplecomposearcore.math.Quaternion
import com.example.samplecomposearcore.math.Vector3
import com.example.samplecomposearcore.scene.Node

class RotatingNode : Node() {
    private var cubeRotationX = 0f
    private var cubeRotationY = 0f

    override fun update(deltaTime: Float) {
        cubeRotationX += 30f * deltaTime
        cubeRotationY += 60f * deltaTime
        localRotation = Quaternion.eulerAngles(Vector3(cubeRotationX, cubeRotationY, 0.0f))
    }
}