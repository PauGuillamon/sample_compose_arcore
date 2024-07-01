package com.example.samplecomposearcore.scene

class DeltaTime {
    private var lastUpdateTime = -1L

    var deltaTimeSeconds: Float = 0f
        private set

    fun pause() {
        lastUpdateTime = -1L
    }

    fun update() {
        val now = System.nanoTime()
        deltaTimeSeconds =
            if (lastUpdateTime != -1L) (now - lastUpdateTime) / 1_000_000_000.0f
            else 0f
        lastUpdateTime = now
    }
}