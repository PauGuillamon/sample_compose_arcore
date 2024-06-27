package com.example.openglbase.samples

import android.opengl.GLES30
import android.view.MotionEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.openglbase.compose.GLThreadedRenderer
import com.example.openglbase.compose.OpenGLView
import com.example.openglbase.ui.theme.OpenGLBaseTheme

@Composable
fun Sample2_MultipleGLViews() {
    /**
     * Surface has a hardcoded background color to more easily
     * showcase the colors of the GL renderers.
     */
    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sample 2",
                modifier = Modifier
                    .padding(16.dp),
                textAlign = TextAlign.Center,
            )
            // TODO PGJ showcase switching to ExternalSurface and EmbeddedExternalSurface
            OpenGLView(
                glThreadedRenderer = Sample2Renderer("top", MaterialTheme.colorScheme.inversePrimary),
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth(0.5f)
                    .clip(CutCornerShape(16.dp))
            )
            Text(
                text = "Text in between",
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center,
            )
            OpenGLView(
                glThreadedRenderer = Sample2Renderer("bottom", MaterialTheme.colorScheme.onBackground),
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth(0.7f)
                    .clip(RoundedCornerShape(16.dp))
            )
            Text(
                text = "Text at the end",
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center,
            )
        }
    }
}

private class Sample2Renderer(val name: String, backgroundColor: Color) : GLThreadedRenderer() {
    override val listenToTouchEvents: Boolean = false

    private val colorRed = backgroundColor.red
    private val colorGreen = backgroundColor.green
    private val colorBlue = backgroundColor.blue

    override fun onViewLifecycleResume() { }
    override fun onViewLifecyclePause() { }
    override fun onThreadedInitializeGPUData() { }

    override fun onThreadedSurfaceChanged(width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onThreadedTouchEvent(event: MotionEvent) { }

    override fun onThreadedDrawFrame() {
        GLES30.glClearColor(colorRed, colorGreen, colorBlue, 0.2f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
    }
}

@Preview
@Composable
private fun Sample2Preview() {
    OpenGLBaseTheme {
        Sample2_MultipleGLViews()
    }
}
