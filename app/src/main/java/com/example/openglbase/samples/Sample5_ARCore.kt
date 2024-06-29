package com.example.openglbase.samples

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.openglbase.R
import com.example.openglbase.compose.CameraPermission
import com.example.openglbase.compose.OpenGLView
import com.example.openglbase.compose.TextCheckbox
import com.example.openglbase.ui.theme.OpenGLBaseTheme
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun Sample5_ARCore(
    postOnUiThread: (Runnable) -> Unit,
    viewModel: Sample5_ARCoreViewModel = viewModel(
        factory = Sample5_ViewModelFactory(
            LocalContext.current,
            postOnUiThread
        )
    ),
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        OpenGLView(glThreadedRenderer = viewModel.renderer)
        CameraPermission {
            viewModel.cameraPermissionWasGranted()
            // Important to make the lambdas as "remember" to avoid recompositions.
            // Alternatively we can enable Compose's strong skipping mode
            val deleteLastLambda = remember { { viewModel.deleteLast() } }
            val toggleDepthMapLambda = remember { { enabled: Boolean -> viewModel.toggleDepthMap(enabled) } }
            val toggleFeaturePointsLambda = remember { { enabled: Boolean -> viewModel.toggleFeaturePoints(enabled) } }
            UILayer(
                stats = viewModel.arCoreStats,
                fps = viewModel.fps,
                onDeleteLast = deleteLastLambda,
                showingDepthMap = viewModel.showingDepthMap,
                onToggleDepthMap = toggleDepthMapLambda,
                showingFeaturePoints = viewModel.showingFeaturePoints,
                onToggleFeaturePoints = toggleFeaturePointsLambda
            )
        }
    }
}

private class Sample5_ViewModelFactory(
    private val context: Context,
    private val postOnUiThread: (Runnable) -> Unit,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return Sample5_ARCoreViewModel(context, postOnUiThread) as T
    }
}

@Composable
private fun UILayer(
    stats: String,
    fps: String,
    onDeleteLast: () -> Unit,
    showingDepthMap: Boolean,
    onToggleDepthMap: (Boolean) -> Unit,
    showingFeaturePoints: Boolean,
    onToggleFeaturePoints: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        SessionStats(
            stats,
            fps,
            showingDepthMap,
            onToggleDepthMap,
            showingFeaturePoints,
            onToggleFeaturePoints
        )
        Toolbar(onDeleteLast)
    }
}

@Composable
private fun SessionStats(
    stats: String,
    fps: String,
    showingDepthMap: Boolean,
    onToggleDepthMap: (Boolean) -> Unit,
    showingFeaturePoints: Boolean,
    onToggleFeaturePoints: (Boolean) -> Unit,
) {
    Surface(
        modifier = Modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp)),
        color = Color(0xAA4D4D4D),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
        ) {
            Text(text = stats)
            Text(text = stringResource(id = R.string.sample5_fps, fps))
            TextCheckbox(stringResource(R.string.sample5_depth_map), showingDepthMap, onToggleDepthMap)
            TextCheckbox(stringResource(R.string.sample5_feature_points), showingFeaturePoints, onToggleFeaturePoints)
        }
    }
}

@Composable
private fun Toolbar(onDeleteLast: () -> Unit) {
    Button(
        onClick = { onDeleteLast() },
        modifier = Modifier
            .padding(8.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.baseline_undo_24),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
            contentDescription = "Delete last"
        )
        Text(
            text = stringResource(id = R.string.sample5_delete_last),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview() {
    val fpsCount = remember { mutableIntStateOf(0) }
    MainScope().launch {
        while (true) {
            delay(100)
            fpsCount.intValue++
        }
    }
    OpenGLBaseTheme {
        val fps = remember { derivedStateOf { fpsCount.intValue.toString() } }
        var showingDepthMap by remember { mutableStateOf(false) }
        var showingFeaturePoints by remember { mutableStateOf(true) }
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            UILayer(
                stats = "Depth API: true",
                fps = fps.value,
                onDeleteLast = {
                    fpsCount.intValue = 0
                },
                showingDepthMap = showingDepthMap,
                onToggleDepthMap = {
                    showingDepthMap = !showingDepthMap
                },
                showingFeaturePoints = showingFeaturePoints,
                onToggleFeaturePoints = {
                    showingFeaturePoints = !showingFeaturePoints
                }
            )
        }
    }
}
