package com.example.openglbase.samples

import android.content.Context
import android.opengl.GLES30
import android.view.MotionEvent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.openglbase.arcoreutils.ARCoreManager
import com.example.openglbase.GLThreadedRenderer
import com.example.openglbase.OpenGLView
import com.example.openglbase.compose.CameraPermission
import com.example.openglbase.geometry.Generator
import com.example.openglbase.math.Vector3
import com.example.openglbase.opengl.GPUTexture
import com.example.openglbase.opengl.RenderableMesh
import com.example.openglbase.opengl.ShaderProgram
import com.example.openglbase.opengl.glHasError
import com.example.openglbase.scene.Camera3D
import com.example.openglbase.scene.DeltaTime
import com.example.openglbase.utils.ShaderReader
import com.example.openglbase.utils.loadImage
import com.google.ar.core.Frame

@Composable
fun Sample4_BackgroundCamera() {
    val context = LocalContext.current
    val renderer = remember { Sample4Renderer(context) }
    Box {
        OpenGLView(glThreadedRenderer = renderer, modifier = Modifier.fillMaxSize())
        CameraPermission {
            renderer.cameraPermissionWasGranted()
        }
    }
}

// TODO PGJ this renderer doesn't have onClear, important for ARCore
private class Sample4Renderer(context: Context) : GLThreadedRenderer() {
    override val listenToTouchEvents: Boolean = false

    private val deltaTime = DeltaTime()
    private val arCoreManager = ARCoreManager(context, onArCoreSessionCreated = {})

    private val zoyaCubeRenderable = RenderableMesh().apply {
        setMesh(Generator.generateCube(1.0f))
    }
    private val zoyaCubeShader = ShaderProgram(
        ShaderReader.readShader(context, "shaders/sample.vert"),
        ShaderReader.readShader(context, "shaders/sample.frag")
    )
    private val zoyaCubeBitmap = loadImage(context.assets, "textures/Zoya.png", true)
    private lateinit var zoyaCubeGpuTexture: GPUTexture

    private val zoyaCubeNode = RotatingNode().apply {
        localPosition = Vector3.forward().scaled(1.0f)
        localScale = Vector3.one().scaled(0.2f)
    }

    private val camera3D = Camera3D().apply {
        worldPosition.set(0.0f, 0.0f, 2.0f)
    }

    override fun onViewLifecycleResume() {
        arCoreManager.onResume()
    }

    override fun onViewLifecyclePause() {
        arCoreManager.onPause()
    }

    override fun onThreadedInitializeGPUData() {
        glHasError("initializeGPUData start")
        arCoreManager.initializeGPU()
        zoyaCubeShader.compile()
        zoyaCubeRenderable.initializeGPU()
        zoyaCubeRenderable.uploadToGPU()
        zoyaCubeGpuTexture = GPUTexture(
            GLES30.GL_TEXTURE_2D,
            listOf(
                GLES30.GL_TEXTURE_MIN_FILTER to GLES30.GL_NEAREST,
                GLES30.GL_TEXTURE_MAG_FILTER to GLES30.GL_NEAREST,
                GLES30.GL_TEXTURE_WRAP_S to GLES30.GL_CLAMP_TO_EDGE,
                GLES30.GL_TEXTURE_WRAP_T to GLES30.GL_CLAMP_TO_EDGE,
            )
        )
        zoyaCubeGpuTexture.uploadBitmapToGPU(GLES30.GL_RGBA, zoyaCubeBitmap)
        zoyaCubeShader.use()
        zoyaCubeShader.setTextureSampler2D("uColorTexture", 0)
        glHasError("initializeGPUData end")
    }

    override fun onThreadedSurfaceChanged(width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        camera3D.setViewport(width, height)
        arCoreManager.onSurfaceChanged(width, height)
    }

    override fun onThreadedTouchEvent(event: MotionEvent) {
        // Empty
    }

    override fun onThreadedDrawFrame() {
        update()
        render()
    }

    fun cameraPermissionWasGranted() {
        arCoreManager.cameraPermissionWasGranted()
    }

    private fun update() {
        val view = view ?: return
        deltaTime.update()
        arCoreManager.update(view.context) { frame ->
            // TODO PGJ remove before presentation
            // updateCameraPosition(frame)
        }
        zoyaCubeNode.update(deltaTime.deltaTimeSeconds)
    }

    private fun render() {
        // Clear the screen
        GLES30.glClearColor(0.05f, 0.05f, 0.05f, 1.0f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        // render the camera image as the background. Depth testing is disabled internally.
        arCoreManager.renderCameraImage()

        // Setup OpenGL so that virtual objects are rendered correctly
        GLES30.glEnable(GLES30.GL_CULL_FACE)
        GLES30.glFrontFace(GLES30.GL_CCW)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glDepthMask(true)

        // Render our virtual object
        zoyaCubeShader.use()
        zoyaCubeShader.setMatrixUniform("uModelMatrix", zoyaCubeNode.worldModelMatrix)
        zoyaCubeShader.setMatrixUniform("uViewMatrix", camera3D.viewMatrix)
        zoyaCubeShader.setMatrixUniform("uProjectionMatrix", camera3D.projectionMatrix)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(zoyaCubeGpuTexture.target, zoyaCubeGpuTexture.id)
        zoyaCubeRenderable.render()
    }

    private fun updateCameraPosition(frame: Frame) {
        val arCoreCamera = frame.camera
        arCoreCamera.getViewMatrix(camera3D.viewMatrix.data, 0)
        arCoreCamera.getProjectionMatrix(camera3D.projectionMatrix.data, 0, camera3D.nearPlane, camera3D.farPlane)
    }
}
