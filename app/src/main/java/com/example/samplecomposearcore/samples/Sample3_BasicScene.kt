package com.example.samplecomposearcore.samples

import android.content.Context
import android.opengl.GLES30
import android.view.MotionEvent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.samplecomposearcore.R
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
import com.example.samplecomposearcore.scene.Node
import com.example.samplecomposearcore.ui.theme.SampleComposeARCoreTheme



@Composable
fun Sample3_BasicScene() {
    val context = LocalContext.current
    val renderer = remember { Sample3Renderer(context) }
    OpenGLView(
        glThreadedRenderer = renderer,
        modifier = Modifier.fillMaxSize()
    )
    ArrowControls(renderer)
}





/**
 * Very simple arrow controls to control the position of the camera.
 */
@Composable
private fun ArrowControls(renderer: Sample3Renderer) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ImageButton(R.drawable.baseline_keyboard_double_arrow_up_24, renderer::moveCameraUp)
            ImageButton(R.drawable.baseline_keyboard_arrow_up_24, renderer::moveCameraForward)
            ImageButton(R.drawable.baseline_keyboard_double_arrow_down_24, renderer::moveCameraDown)
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ImageButton(R.drawable.baseline_keyboard_arrow_left_24, renderer::moveCameraLeft)
            ImageButton(R.drawable.baseline_keyboard_arrow_down_24, renderer::moveCameraBackward)
            ImageButton(R.drawable.baseline_keyboard_arrow_right_24, renderer::moveCameraRight)
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun ImageButton(@DrawableRes id: Int, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.padding(end = 4.dp)) {
        Image(
            painter = painterResource(id = id),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
            contentDescription = "ImageButton:$id"
        )
    }
}






private class Sample3Renderer(context: Context) : GLThreadedRenderer() {
    override val listenToTouchEvents: Boolean = false

    private val deltaTime = DeltaTime()

    private val cuteCatCubeZoya = RenderableModel(
        RenderableMesh().apply {
            setMesh(Generator.generateCube(1.0f))
        },
        MaterialTextured(context.assets, "textures/Zoya.png")
    )
    private val cuteCatCubeMerlot = RenderableModel(
        RenderableMesh().apply {
            setMesh(Generator.generateCube(1.0f))
        },
        MaterialTextured(context.assets, "textures/Merlot.jpg")
    )

    private val nodes = listOf(
        createNode(Vector3( 0.0f, 0.0f, -1.0f)),
        createNode(Vector3( 0.0f, 0.0f, -2.0f)),
        createNode(Vector3( 0.0f, 0.0f, -4.0f)),
        createNode(Vector3(-1.0f, 0.0f, -2.0f)),
        createNode(Vector3(-1.0f, 0.0f, -4.0f)),
        createNode(Vector3(+1.0f, 0.0f, -2.0f)),
        createNode(Vector3(+1.0f, 0.0f, -4.0f)),
        createNode(Vector3( 0.0f, 2.0f, -2.0f)),
        createNode(Vector3( 0.0f, 2.0f, -4.0f)),
        createNode(Vector3(-1.0f, 2.0f, -2.0f)),
        createNode(Vector3(-1.0f, 2.0f, -4.0f)),
        createNode(Vector3(+1.0f, 2.0f, -2.0f)),
        createNode(Vector3(+1.0f, 2.0f, -4.0f)),
    )

    private fun createNode(position: Vector3): Node {
        return RotatingNode().apply {
            localPosition = position
            localScale = Vector3.one().scaled(0.2f)
        }
    }

    private val camera3D = Camera3D().apply {
        setPosition(Vector3(0.0f, 0.0f, 0.0f))
    }
    private val cameraMovementStep = 0.2f

    override fun onViewLifecycleResume() {
        // Empty
    }

    override fun onViewLifecyclePause() {
        cuteCatCubeZoya.restoreInitializedGPU()
        cuteCatCubeMerlot.restoreInitializedGPU()
    }

    override fun onThreadedInitializeGPUData() {
        glHasError("initializeGPUData start")
        cuteCatCubeZoya.initializeGPU()
        cuteCatCubeMerlot.initializeGPU()
        glHasError("initializeGPUData end")
    }

    override fun onThreadedSurfaceChanged(width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        camera3D.setViewport(width, height)
    }

    override fun onThreadedTouchEvent(event: MotionEvent) {
        // Empty
    }

    override fun onThreadedDrawFrame() {
        update()
        render()
    }

    private fun update() {
        deltaTime.update()
        nodes.forEach {
            it.update(deltaTime.deltaTimeSeconds)
        }
    }

    private fun render() {
        GLES30.glClearColor(0.05f, 0.05f, 0.05f, 1.0f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        GLES30.glEnable(GLES30.GL_CULL_FACE)
        GLES30.glFrontFace(GLES30.GL_CCW)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glDepthMask(true)

        var count = 0
        nodes.forEach {
            // Rendering each node with a different model
            val renderableModel = if (count % 2 == 0) {
                cuteCatCubeZoya
            } else {
                cuteCatCubeMerlot
            }
            renderableModel.renderNode(it, camera3D)
            count++
        }
    }

    fun moveCameraUp() {
        moveCamera(Vector3.up().scaled(cameraMovementStep))
    }

    fun moveCameraDown() {
        moveCamera(Vector3.down().scaled(cameraMovementStep))
    }

    fun moveCameraLeft() {
        moveCamera(Vector3.left().scaled(cameraMovementStep))
    }

    fun moveCameraRight() {
        moveCamera(Vector3.right().scaled(cameraMovementStep))
    }

    fun moveCameraForward() {
        moveCamera(Vector3.forward().scaled(cameraMovementStep))
    }

    fun moveCameraBackward() {
        moveCamera(Vector3.back().scaled(cameraMovementStep))
    }

    private fun moveCamera(direction: Vector3) {
        postOnRenderThread {
            val newPosition = Vector3.add(camera3D.worldPosition, direction)
            camera3D.setPosition(newPosition)
        }
    }
}

@Preview
@Composable
private fun Droid2Preview() {
    SampleComposeARCoreTheme {
        Sample3_BasicScene()
    }
}
