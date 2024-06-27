package com.example.openglbase.samples

import android.opengl.GLES30
import android.view.MotionEvent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.openglbase.GLThreadedRenderer
import com.example.openglbase.OpenGLView
import com.example.openglbase.ui.theme.OpenGLBaseTheme

@Composable
fun Sample1() {
    OpenGLView(
        glThreadedRenderer = Sample1Renderer(),
        modifier = Modifier.fillMaxSize()
    )
}

private class Sample1Renderer : GLThreadedRenderer() {
    override val listenToTouchEvents: Boolean = false

    private var colorBlue = 0.0f

    override fun onViewLifecycleResume() { }
    override fun onViewLifecyclePause() { }
    override fun onThreadedInitializeGPUData() { }

    override fun onThreadedSurfaceChanged(width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onThreadedTouchEvent(event: MotionEvent) { }

    override fun onThreadedDrawFrame() {
        GLES30.glClearColor(0.05f, 0.05f, colorBlue, 1.0f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        colorBlue += 0.01f
        if (colorBlue > 1.0f) {
            colorBlue = 0.0f
        }
    }
}

@Preview
//@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun Sample1Preview() {
    OpenGLBaseTheme {
        Sample1()
    }
}
