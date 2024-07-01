package com.example.samplecomposearcore.samples

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
import com.example.samplecomposearcore.R
import com.example.samplecomposearcore.compose.CameraPermission
import com.example.samplecomposearcore.compose.OpenGLView
import com.example.samplecomposearcore.compose.TextCheckbox
import com.example.samplecomposearcore.ui.theme.SampleComposeARCoreTheme
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
            val toggleFeaturePointsLambda = remember { { enabled: Boolean -> viewModel.toggleFeaturePoints(enabled) } }
            val toggleDepthMapLambda = remember { { enabled: Boolean -> viewModel.toggleDepthMap(enabled) } }
            val toggleEisLambda = remember { { enabled: Boolean -> viewModel.toggleEIS(enabled) } }
            UILayer(
                stats = viewModel.arCoreStats,
                fps = viewModel.fps,
                onDeleteLast = deleteLastLambda,
                showingFeaturePoints = viewModel.showingFeaturePoints,
                onToggleFeaturePoints = toggleFeaturePointsLambda,
                showingDepthMap = viewModel.showingDepthMap,
                onToggleDepthMap = toggleDepthMapLambda,
                eisEnabled = viewModel.eisEnabled,
                onToggleEis = toggleEisLambda,
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
    showingFeaturePoints: Boolean,
    onToggleFeaturePoints: (Boolean) -> Unit,
    showingDepthMap: Boolean,
    onToggleDepthMap: (Boolean) -> Unit,
    eisEnabled: Boolean,
    onToggleEis: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        SessionStats(
            stats,
            fps,
            showingFeaturePoints,
            onToggleFeaturePoints,
            showingDepthMap,
            onToggleDepthMap,
            eisEnabled,
            onToggleEis
        )
        Toolbar(onDeleteLast)
    }
}

@Composable
private fun SessionStats(
    stats: String,
    fps: String,
    showingFeaturePoints: Boolean,
    onToggleFeaturePoints: (Boolean) -> Unit,
    showingDepthMap: Boolean,
    onToggleDepthMap: (Boolean) -> Unit,
    eisEnabled: Boolean,
    onToggleEis: (Boolean) -> Unit,
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
            TextCheckbox(stringResource(R.string.sample5_toggle_feature_points), showingFeaturePoints, onToggleFeaturePoints)
            TextCheckbox(stringResource(R.string.sample5_toggle_depth_map), showingDepthMap, onToggleDepthMap)
            TextCheckbox(stringResource(R.string.sample5_toggle_eis), eisEnabled, onToggleEis)
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
    SampleComposeARCoreTheme {
        val fps = remember { derivedStateOf { fpsCount.intValue.toString() } }
        var showingFeaturePoints by remember { mutableStateOf(true) }
        var showingDepthMap by remember { mutableStateOf(false) }
        var eisEnabled by remember { mutableStateOf(false) }
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            UILayer(
                stats = "Depth API: true",
                fps = fps.value,
                onDeleteLast = {
                    fpsCount.intValue = 0
                },
                showingFeaturePoints = showingFeaturePoints,
                onToggleFeaturePoints = {
                    showingFeaturePoints = !showingFeaturePoints
                },
                showingDepthMap = showingDepthMap,
                onToggleDepthMap = {
                    showingDepthMap = !showingDepthMap
                },
                eisEnabled = eisEnabled,
                onToggleEis = {
                    eisEnabled = !eisEnabled
                },
            )
        }
    }
}
