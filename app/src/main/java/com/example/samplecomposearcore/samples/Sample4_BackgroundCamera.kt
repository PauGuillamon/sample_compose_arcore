package com.example.samplecomposearcore.samples

import android.content.Context
import android.opengl.GLES30
import android.view.MotionEvent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.samplecomposearcore.arcoreutils.ARCoreManager
import com.example.samplecomposearcore.compose.CameraPermission
import com.example.samplecomposearcore.compose.GLThreadedRenderer
import com.example.samplecomposearcore.compose.OpenGLView
import com.example.samplecomposearcore.geometry.Generator
import com.example.samplecomposearcore.math.Vector3
import com.example.samplecomposearcore.opengl.MaterialTextured
import com.example.samplecomposearcore.opengl.RenderableMesh
import com.example.samplecomposearcore.opengl.RenderableModel
import com.example.samplecomposearcore.opengl.glHasError
import com.example.samplecomposearcore.scene.Camera3D
import com.example.samplecomposearcore.scene.DeltaTime
import com.google.ar.core.Frame



@Composable
fun Sample4_BackgroundCamera() {
    val context = LocalContext.current
    val renderer = remember { Sample4Renderer(context) }
    Box {
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_STOP) {
                    renderer.onClear()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
        OpenGLView(
            glThreadedRenderer = renderer,
            modifier = Modifier.fillMaxSize()
        )
        CameraPermission {
            renderer.cameraPermissionWasGranted()
        }
    }
}





private class Sample4Renderer(context: Context) : GLThreadedRenderer() {
    override val listenToTouchEvents: Boolean = false

    private val deltaTime = DeltaTime()
    private val arCoreManager = ARCoreManager(context, onArCoreSessionCreated = {})

    private val cuteCatCubeZoya: RenderableModel = RenderableModel(
        RenderableMesh().apply {
            setMesh(Generator.generateCube(1.0f))
        },
        MaterialTextured(context.assets, "textures/Zoya.png")
    )

    private val zoyaCubeNode = RotatingNode().apply {
        localPosition = Vector3.forward().scaled(1.0f)
        localScale = Vector3.one().scaled(0.2f)
    }

    private val camera3D = Camera3D().apply {
        setPosition(Vector3(0.0f, 0.0f, 0.0f))
    }

    fun onClear() {
        arCoreManager.onClear()
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
        cuteCatCubeZoya.initializeGPU()
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
        // Might be called multiple times due to the limitations of CameraPermission.
        arCoreManager.cameraPermissionWasGranted()
    }

    private fun update() {
        val view = view ?: return
        deltaTime.update()
        arCoreManager.update(view.context) { frame ->
            updateCameraPosition(frame)
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
        cuteCatCubeZoya.renderNode(zoyaCubeNode, camera3D)
    }

    private fun updateCameraPosition(frame: Frame) {
        val arCoreCamera = frame.camera
        arCoreCamera.getViewMatrix(camera3D.viewMatrix.data, 0)
        arCoreCamera.getProjectionMatrix(camera3D.projectionMatrix.data, 0, camera3D.nearPlane, camera3D.farPlane)
    }
}
