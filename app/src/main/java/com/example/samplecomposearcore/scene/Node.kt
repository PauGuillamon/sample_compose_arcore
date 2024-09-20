package com.example.samplecomposearcore.scene

import androidx.annotation.CallSuper
import com.example.samplecomposearcore.math.Matrix
import com.example.samplecomposearcore.math.Quaternion
import com.example.samplecomposearcore.math.Vector3
import com.example.samplecomposearcore.opengl.RenderableModel

open class Node {
    private val localModelMatrix = Matrix()

    private val _worldModelMatrix = Matrix()
    val worldModelMatrix: Matrix
        get() {
            if (dirty || parent?.dirty == true) {
                recalculateModelMatrix()
            }
            return _worldModelMatrix
        }

    var localPosition = Vector3.zero()
        set(value) {
            field = value
            markDirty()
        }
    var localRotation = Quaternion()
        set(value) {
            field = value
            markDirty()
        }
    var localScale = Vector3.one()
        set(value) {
            field = value
            markDirty()
        }

    var enabled = true

    var renderableModel: RenderableModel? = null

    protected var parent: Node? = null
    protected val children = mutableListOf<Node>()

    private var dirty = false

    fun setLocalModelMatrix(matrix: Matrix) {
        matrix.decomposeTranslation(localPosition)
        matrix.extractQuaternion(localRotation)
        matrix.decomposeScale(localScale)
        markDirty()
    }

    fun addChild(child: Node) {
        child.parent?.children?.remove(child)
        children.add(child)
        child.parent = this
        child.markDirty()
    }

    @CallSuper
    open fun detach() {
        parent?.children?.remove(this)
        parent = null
    }

    open fun update(deltaTime: Float) {
        // Empty
    }

    fun traverseAllTree(action: (Node) -> Unit) {
        action(this)
        children.forEach {
            it.traverseAllTree(action)
        }
    }

    fun traverseEnabledTree(action: (Node) -> Unit) {
        if (enabled) {
            action(this)
            children.forEach {
                it.traverseEnabledTree(action)
            }
        }
    }

    private fun markDirty() {
        traverseAllTree {
            it.dirty = true
        }
    }

    private fun recalculateModelMatrix() {
        localModelMatrix.makeTrs(
            localPosition,
            localRotation,
            localScale
        )
        val parentModelMatrix = parent?.worldModelMatrix
        if (parentModelMatrix != null) {
            Matrix.multiply(parentModelMatrix, localModelMatrix, _worldModelMatrix)
        } else {
            _worldModelMatrix.set(localModelMatrix)
        }
        dirty = false
    }
}