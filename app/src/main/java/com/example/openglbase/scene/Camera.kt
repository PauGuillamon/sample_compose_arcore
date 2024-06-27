package com.example.openglbase.scene

import androidx.annotation.CallSuper
import com.example.openglbase.math.Matrix
import com.example.openglbase.math.Vector3

open class Camera {
    val worldPosition = Vector3.zero()
    val upVector = Vector3.up()
    val viewMatrix = Matrix()
    val projectionMatrix = Matrix()

    var viewWidth = 0
        private set
    var viewHeight = 0
        private set

    @CallSuper
    open fun setViewport(width: Int, height: Int) {
        viewWidth = width
        viewHeight = height
    }
}
