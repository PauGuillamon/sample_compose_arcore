package com.example.samplecomposearcore.arcoreutils

import com.example.samplecomposearcore.math.Matrix
import com.example.samplecomposearcore.scene.Node
import com.google.ar.core.Anchor
import com.google.ar.core.TrackingState

class AnchorNode(private val anchor: Anchor) : Node() {

    init {
        update(0f)
    }

    override fun update(deltaTime: Float) {
        val newEnabled: Boolean
        if (anchor.trackingState == TrackingState.TRACKING) {
            val anchorMatrix = Matrix()
            anchor.pose.toMatrix(anchorMatrix.data, 0)
            setLocalModelMatrix(anchorMatrix)
            newEnabled = true
        } else {
            newEnabled = false
        }
        children.forEach {
            it.enabled = newEnabled
        }
    }

    override fun detach() {
        super.detach()
        anchor.detach()
    }
}