package com.example.openglbase

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.openglbase.compose.TextCheckbox
import com.example.openglbase.samples.Sample1_ComposableOpenGLView
import com.example.openglbase.samples.Sample2_MultipleGLViews
import com.example.openglbase.samples.Sample3_BasicScene
import com.example.openglbase.samples.Sample4_BackgroundCamera
import com.example.openglbase.samples.Sample5_ARCore
import com.example.openglbase.ui.theme.OpenGLBaseTheme
import com.google.ar.core.ArCoreApk

private enum class NavRoute(val route: String, val arCoreDependency: Boolean, @StringRes val description: Int) {
    Sample1("Sample OpenGL setup", false, R.string.sample1_description),
    Sample2("Sample multiple GL views", false, R.string.sample2_description),
    Sample3("Sample basic scene", false, R.string.sample3_description),
    Sample4("Sample background camera", true, R.string.sample4_description),
    Sample5("Sample ARCore", true, R.string.sample5_description),
}

private enum class ArCoreInstallationState {
    UNKNOWN,
    UNINSTALLED_OR_TOO_OLD,
    INSTALLED,
}

class MainActivity : ComponentActivity() {
    private var arCoreSupportedAndUpdated by mutableStateOf(ArCoreInstallationState.UNKNOWN)
    private var showInstallArCoreButton by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OpenGLBaseTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "main"
                ) {
                    composable("main") {
                        MainScreen(
                            arCoreSupportedAndUpdated,
                            onNavigateTo = { navController.navigate(it.route) },
                            onInstallArCore = { onInstallArCore() }
                        )
                    }
                    composable(NavRoute.Sample1.route) { Sample1_ComposableOpenGLView() }
                    composable(NavRoute.Sample2.route) { Sample2_MultipleGLViews() }
                    composable(NavRoute.Sample3.route) { Sample3_BasicScene() }
                    composable(NavRoute.Sample4.route) { Sample4_BackgroundCamera() }
                    composable(NavRoute.Sample5.route) { Sample5_ARCore(this@MainActivity::runOnUiThread) }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkArCoreAvailability()
    }

    private fun checkArCoreAvailability() {
        ArCoreApk.getInstance().checkAvailabilityAsync(applicationContext) {
            if (it == ArCoreApk.Availability.SUPPORTED_INSTALLED) {
                arCoreSupportedAndUpdated = ArCoreInstallationState.INSTALLED
                showInstallArCoreButton = false
            } else {
                arCoreSupportedAndUpdated = ArCoreInstallationState.UNINSTALLED_OR_TOO_OLD
                showInstallArCoreButton = true
            }
        }
    }

    private fun onInstallArCore() {
        ArCoreApk.getInstance().requestInstall(this, true)
    }
}

@Composable
private fun MainScreen(
    arCoreSupportedAndUpdated: ArCoreInstallationState,
    onNavigateTo: (NavRoute) -> Unit,
    onInstallArCore: () -> Unit
) {
    var infoOpen by remember { mutableStateOf(false) }
    var arCoreInfoOpen by remember { mutableStateOf(true) }
    Scaffold(
        topBar = {
            TopBar {
                infoOpen = !infoOpen
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            MainScreenContent(arCoreSupportedAndUpdated, onNavigateTo, onInstallArCore)
            if (infoOpen) {
                DialogCard(onDismissRequest = { infoOpen = !infoOpen }) {
                    Text(
                        text = stringResource(R.string.project_description),
                        modifier = Modifier
                            .padding(16.dp)
                    )
                }
            }
            if ((arCoreSupportedAndUpdated == ArCoreInstallationState.UNINSTALLED_OR_TOO_OLD) && arCoreInfoOpen) {
                DialogCard(onDismissRequest = { arCoreInfoOpen = !arCoreInfoOpen }) {
                    Text(
                        text = stringResource(R.string.arcore_dependency_explanation),
                        modifier = Modifier
                            .padding(16.dp)
                    )
                    Button(onClick = onInstallArCore) {
                        Text(text = stringResource(R.string.arcore_install_button))
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogCard(onDismissRequest: () -> Unit, content: @Composable () -> Unit) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(text = stringResource(R.string.ok))
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar(onInfoClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.End
        ) {
            Image(
                painter = painterResource(id = R.drawable.baseline_info_outline_24),
                contentDescription = "Open app description",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                modifier = Modifier.clickable(onClick = onInfoClick)
            )
        }
    }
}

@Composable
private fun MainScreenContent(
    arCoreSupportedAndUpdated: ArCoreInstallationState,
    onNavigateTo: (NavRoute) -> Unit,
    onInstallArCore: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.app_title),
            fontSize = 30.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Text(
            text = stringResource(R.string.session_title),
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        if (arCoreSupportedAndUpdated == ArCoreInstallationState.UNINSTALLED_OR_TOO_OLD) {
            Button(onClick = onInstallArCore) {
                Text(text = stringResource(R.string.arcore_install_button))
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 60.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(NavRoute.entries) {
                val blockedByArcoreSupport = it.arCoreDependency && (arCoreSupportedAndUpdated !== ArCoreInstallationState.INSTALLED)
                SampleEntry(it.route, stringResource(it.description), blockedByArcoreSupport) {
                    onNavigateTo(it)
                }
            }
        }
    }
}

@Composable
private fun SampleEntry(
    name: String,
    description: String,
    blockedByArcoreSupport: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier
            .widthIn(100.dp, 300.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                if (!blockedByArcoreSupport) {
                    onClick()
                }
            },
        color = if (!blockedByArcoreSupport) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceDim
    ) {
        val expanded = remember { MutableTransitionState(false) }
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(text = name)
                val icon = if (expanded.targetState) {
                    R.drawable.baseline_info_24
                } else {
                    R.drawable.baseline_info_outline_24
                }
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = "See description",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
                    modifier = Modifier
                        .clickable { expanded.targetState = !expanded.currentState },
                )
            }
            AnimatedVisibility(visibleState = expanded) {
                Box {
                    Text(text = description)
                }
            }
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MainScreenPreview() {
    OpenGLBaseTheme {
        Surface {
            var arCoreSupportedAndUpdated by remember { mutableStateOf(ArCoreInstallationState.UNKNOWN) }
            MainScreen(
                arCoreSupportedAndUpdated,
                onNavigateTo = {},
                onInstallArCore = {}
            )
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Bottom,
            ) {
                TextCheckbox(
                    text = "ARCore supported",
                    checked = arCoreSupportedAndUpdated == ArCoreInstallationState.INSTALLED,
                    onCheckedChange = {
                        arCoreSupportedAndUpdated = when (arCoreSupportedAndUpdated) {
                            ArCoreInstallationState.UNKNOWN -> ArCoreInstallationState.UNINSTALLED_OR_TOO_OLD
                            ArCoreInstallationState.UNINSTALLED_OR_TOO_OLD -> ArCoreInstallationState.INSTALLED
                            ArCoreInstallationState.INSTALLED -> ArCoreInstallationState.UNKNOWN
                        }
                    },
                )
            }
        }
    }
}
