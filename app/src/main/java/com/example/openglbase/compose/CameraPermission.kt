package com.example.openglbase.compose

import android.Manifest
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.openglbase.R
import com.example.openglbase.ui.theme.OpenGLBaseTheme
import com.example.openglbase.utils.Logger
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

/**
 * This composable informs the user to request the camera permission. Shows the
 * rationale accordingly, even though it cannot detect whether the user had
 * already previously denied the permission.
 *
 * [contentOnPermissionGranted] is *always* called for every recomposition if
 * the permission status is granted. This is not great but it works for this
 * project.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPermission(contentOnPermissionGranted: @Composable () -> Unit) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    if (cameraPermissionState.status.isGranted) {
        contentOnPermissionGranted()
    } else {
        CameraPermissionRequest(
            cameraPermissionState.status.shouldShowRationale,
            cameraPermissionState::launchPermissionRequest
        )
    }
}

@Composable
private fun CameraPermissionRequest(
    shouldShowRationale: Boolean,
    onRequestPermissionClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val text = if (shouldShowRationale) {
                    stringResource(R.string.camera_permission_rationale)
                } else {
                    stringResource(R.string.camera_permission_please_grant)
                }
                Text(
                    text = text,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(8.dp)
                )
                Button(onClick = onRequestPermissionClick) {
                    Text(text = stringResource(R.string.camera_request_permission))
                }
            }
        }
    }
}

@ExperimentalPermissionsApi
@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewCameraPermission() {
    OpenGLBaseTheme {
        Surface(
            color = MaterialTheme.colorScheme.onBackground
        ) {
            var showRationale by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxSize()) {
                CameraPermissionRequest(
                    showRationale
                ) {
                    showRationale = true
                }
            }
        }
    }
}