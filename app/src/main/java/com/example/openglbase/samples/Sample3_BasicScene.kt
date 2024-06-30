package com.example.openglbase.samples

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
import com.example.openglbase.R
import com.example.openglbase.compose.GLThreadedRenderer
import com.example.openglbase.compose.OpenGLView
import com.example.openglbase.geometry.Generator
import com.example.openglbase.math.Vector3
import com.example.openglbase.opengl.GPUTexture
import com.example.openglbase.opengl.RenderableMesh
import com.example.openglbase.opengl.ShaderProgram
import com.example.openglbase.opengl.glHasError
import com.example.openglbase.scene.Camera3D
import com.example.openglbase.scene.DeltaTime
import com.example.openglbase.scene.Node
import com.example.openglbase.ui.theme.OpenGLBaseTheme
import com.example.openglbase.utils.ShaderReader
import com.example.openglbase.utils.loadImage

@Composable
fun Sample3_BasicScene() {
    val context = LocalContext.current
    val renderer = remember { Sample3Renderer(context) }
    OpenGLView(
        glThreadedRenderer = renderer,
        modifier = Modifier
            .fillMaxSize()
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

    private val zoyaCubeRenderable = RenderableMesh().apply {
        setMesh(Generator.generateCube(1.0f))
    }
    private val zoyaCubeShader = ShaderProgram(
        ShaderReader.readShader(context, "shaders/sample.vert"),
        ShaderReader.readShader(context, "shaders/sample.frag")
    )
    private val zoyaCubeBitmap = loadImage(context.assets, "textures/Zoya.png", true)
    private lateinit var zoyaCubeGpuTexture: GPUTexture

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
        // Empty
    }

    override fun onThreadedInitializeGPUData() {
        glHasError("initializeGPUData start")
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

        zoyaCubeShader.use()
        nodes.forEach {
            zoyaCubeShader.setMatrixUniform("uModelMatrix", it.worldModelMatrix)
            zoyaCubeShader.setMatrixUniform("uViewMatrix", camera3D.viewMatrix)
            zoyaCubeShader.setMatrixUniform("uProjectionMatrix", camera3D.projectionMatrix)
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
            GLES30.glBindTexture(zoyaCubeGpuTexture.target, zoyaCubeGpuTexture.id)
            zoyaCubeRenderable.render()
            GLES30.glBindTexture(zoyaCubeGpuTexture.target, 0)
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
    OpenGLBaseTheme {
        Sample3_BasicScene()
    }
}
