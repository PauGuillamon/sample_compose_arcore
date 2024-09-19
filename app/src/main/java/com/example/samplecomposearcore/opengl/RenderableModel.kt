package com.example.samplecomposearcore.opengl

import com.example.samplecomposearcore.scene.Camera
import com.example.samplecomposearcore.scene.Node

class RenderableModel(
    // TODO PGJ internally reorder meshes so same materials are rendered at once
    private val meshes: List<Pair<RenderableMesh, Material>>
) {
    private var initialized = false

    constructor(renderableMesh: RenderableMesh, material: Material): this(
        listOf(
            Pair(
                renderableMesh,
                material
            )
        )
    )

    fun initializeGPU() {
        meshes.forEach {
            it.first.initializeGPU()
            it.first.uploadToGPU()
            it.second.initializeGPU()
        }
        initialized = true
    }

    fun restoreInitializedGPU() {
        meshes.forEach {
            // Not needed to restoreGPU for RenderableMesh because they are unique and
            // will be automatically clean. For Material, they might be shared among
            // multiple RenderableMesh. That's why we need to properly handle the
            // lifecycle of a Material and make sure it's only initialized once.
            it.second.restoreInitializedGPU()
        }
        initialized = false
    }

    fun isGPUInitialized(): Boolean {
        return initialized
    }

    fun renderNode(node: Node, camera: Camera) {
        meshes.forEach {
            it.second.prepareForRender()
            it.second.setRenderProperties(
                node.worldModelMatrix,
                camera.viewMatrix,
                camera.projectionMatrix
            )
            it.first.render()
        }
    }
}
