package com.example.samplecomposearcore.arcoreutils

import android.content.Context
import android.opengl.GLES30
import android.util.Size
import com.example.samplecomposearcore.geometry.Generator
import com.example.samplecomposearcore.opengl.GPUTexture
import com.example.samplecomposearcore.scene.Camera3D
import com.example.samplecomposearcore.utils.Logger
import com.google.ar.core.CameraConfig
import com.google.ar.core.CameraConfigFilter
import com.google.ar.core.Config
import com.google.ar.core.Coordinates2d
import com.google.ar.core.Coordinates3d
import com.google.ar.core.Frame
import com.google.ar.core.Session
import com.google.ar.core.exceptions.NotYetAvailableException
import java.util.EnumSet
import java.util.concurrent.atomic.AtomicBoolean

class ARCoreManager(context: Context, val onArCoreSessionCreated: () -> Unit) {

    private val displayHelper = DisplayHelper(context)
    private val cameraImageRenderer = CameraImageRenderer(context)
    private val depthImageRenderer = DepthImageRenderer(context)
    private val pointCloudRenderer = PointCloudRenderer(context)
    private val cameraPermissionGranted = AtomicBoolean(false)

    var arCoreSession: Session? = null
        private set
    var currentFrame: Frame? = null
        private set
    var depthApiSupported = false
        private set
    var eisSupported = false
        private set
    var eisEnabled = false
        private set

    private var forceUpdateCameraCoords2D = true

    private var arCoreSessionResumed = false
    private var firstTimeResume = true

    var depthTexture: GPUTexture? = null
        private set
    var hasDepthData = false
        private set

    fun onResume() {
        displayHelper.onResume()
    }

    fun onPause() {
        arCoreSession?.pause()
        arCoreSessionResumed = false
        displayHelper.onPause()
        hasDepthData = false
    }

    fun onClear() {
        arCoreSession?.close()
        arCoreSession = null
    }

    fun onSurfaceChanged(width: Int, height: Int) {
        displayHelper.onViewportChanged(width, height)
    }

    fun initializeGPU() {
        cameraImageRenderer.initializeGPU()
        depthImageRenderer.initializeGPU()
        pointCloudRenderer.initializeGPU()
        depthTexture = null
    }

    fun cameraPermissionWasGranted() {
        // Usually called very early or from the main thread. Not worth posting it
        // to the render thread as we might not have the thread yet where we can post to.
        cameraPermissionGranted.set(true)
    }

    fun enableEIS(enable: Boolean) {
        if (eisSupported && enable != eisEnabled) {
            arCoreSession?.let {
                val newMode = if (enable) Config.ImageStabilizationMode.EIS else Config.ImageStabilizationMode.OFF
                val config = it.config
                config.imageStabilizationMode = newMode
                it.configure(config)
            }
            eisEnabled = arCoreSession?.config?.imageStabilizationMode == Config.ImageStabilizationMode.EIS
            forceUpdateCameraCoords2D = (enable == false)
        }
    }

    fun update(context: Context, onFrameAvailable: (Frame) -> Unit) {
        ensureArCoreSessionCreated(context)
        arCoreSession?.let { arCoreSession ->
            ensureArCoreResumed()
            displayHelper.update(arCoreSession)
            arCoreSession.setCameraTextureName(cameraImageRenderer.texture.id)
            val newFrame = arCoreSession.update()
            val newTimestamp = (newFrame.timestamp != currentFrame?.timestamp)
            val geometryChanged = newFrame.hasDisplayGeometryChanged()
            if (geometryChanged || newTimestamp) {
                currentFrame = newFrame
                if (depthApiSupported) {
                    updateDepth(newFrame)
                }
                updateCameraCoords(newFrame)
                pointCloudRenderer.update(newFrame)
                onFrameAvailable(newFrame)
            }
        }
    }

    fun renderCameraImage() {
        if (arCoreSession != null) {
            cameraImageRenderer.render()
        }
    }

    fun renderDepthMap() {
        if (arCoreSession != null) {
            depthTexture?.let {
                depthImageRenderer.render(it)
            }
        }
    }

    fun renderDepthRawData() {
        if (arCoreSession != null) {
            depthTexture?.let {
                depthImageRenderer.renderRawData(it)
            }
        }
    }

    fun renderPointCloud(camera: Camera3D) {
        pointCloudRenderer.render(camera)
    }

    private fun ensureArCoreSessionCreated(context: Context) {
        if (arCoreSession == null && cameraPermissionGranted.get()) {
            Logger.LogInfo(TAG, "ARCoreManager Creating ARCore Session")
            val session = Session(context)
            session.configure(Config(session).apply {
                lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
                focusMode = Config.FocusMode.AUTO

                val depthConfig = Config.DepthMode.AUTOMATIC
                depthMode =
                    if (session.isDepthModeSupported(depthConfig)) depthConfig else Config.DepthMode.DISABLED
                depthApiSupported = depthMode != Config.DepthMode.DISABLED
                //planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL

                val cameraConfigFilter = CameraConfigFilter(session)
                cameraConfigFilter.targetFps = EnumSet.of(CameraConfig.TargetFps.TARGET_FPS_30)
                var chosenCameraConfig: CameraConfig? = null
                session.getSupportedCameraConfigs(cameraConfigFilter).forEach { cameraConfig ->
                    val textureSize = cameraConfig.textureSize
                    val currentTextureSize = chosenCameraConfig?.textureSize ?: Size(0, 0)
                    var candidate = false
                    if (textureSize.width > currentTextureSize.width || textureSize.height > currentTextureSize.height) {
                        chosenCameraConfig = cameraConfig
                        candidate = true
                    }
                    Logger.LogInfo(
                        TAG,
                        "cameraConfig[${if (candidate) "X" else " "}]: " +
                                "camId:${cameraConfig.cameraId} " +
                                "fpsRange:${cameraConfig.fpsRange} " +
                                "CpuImageSize:${cameraConfig.imageSize} " +
                                "GpuTextureSize:${cameraConfig.textureSize} "
                    )
                }
                chosenCameraConfig?.let {
                    session.cameraConfig = it
                }
            })
            eisSupported = session.isImageStabilizationModeSupported(Config.ImageStabilizationMode.EIS)
            arCoreSession = session
            onArCoreSessionCreated()
        }
    }

    private fun ensureArCoreResumed() {
        if (!arCoreSessionResumed) {
            if (!firstTimeResume) {
                arCoreSession?.resume()
            } else {
                firstTimeResume = false
                /**
                 * resume() + pause() + resume() is needed due to:
                 * https://github.com/google-ar/arcore-android-sdk/issues/1312
                 */
                arCoreSession?.resume()
                arCoreSession?.pause()
                arCoreSession?.resume()
            }
            arCoreSessionResumed = true
        }
    }

    private fun updateDepth(frame: Frame) {
        if (depthTexture == null) {
            // GL_LINEAR will help smoothen the transition from one texel to another.
            // It's helpful here because the depth image from ARCore has low resolution.
            depthTexture = GPUTexture(
                GLES30.GL_TEXTURE_2D,
                listOf(
                    GLES30.GL_TEXTURE_MIN_FILTER to GLES30.GL_LINEAR,
                    GLES30.GL_TEXTURE_MAG_FILTER to GLES30.GL_LINEAR,
                    GLES30.GL_TEXTURE_WRAP_S to GLES30.GL_CLAMP_TO_EDGE,
                    GLES30.GL_TEXTURE_WRAP_T to GLES30.GL_CLAMP_TO_EDGE,
                )
            )
        }

        try {
            val depthImage = frame.acquireDepthImage16Bits()
            depthTexture!!.uploadTextureDataToGPU(
                depthImage.width,
                depthImage.height,
                GLES30.GL_RG8,
                GLES30.GL_RG,
                GLES30.GL_UNSIGNED_BYTE,
                depthImage.planes[0].buffer
            )
            hasDepthData = true
            depthImage.close()
        } catch (_: NotYetAvailableException) {
            // Normal exception from ARCore - happens usually during the first frames.
        }
    }

    private fun updateCameraCoords(frame: Frame) {
        if (eisEnabled) {
            // When EIS is enabled, the camera coords might change every frame.
            updateCoords3D(frame)
        } else if (frame.hasDisplayGeometryChanged() || forceUpdateCameraCoords2D) {
            updateCoords2D(frame)
            forceUpdateCameraCoords2D = false
        }
    }

    private fun updateCoords3D(frame: Frame) {
        val verticesNdc = floatArrayOf(
            -1f, -1f,
            +1f, -1f,
            +1f, +1f,
            -1f, +1f
        )
        val coords3D = FloatArray(4 * 3)
        frame.transformCoordinates3d(
            Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES,
            verticesNdc,
            Coordinates3d.EIS_NORMALIZED_DEVICE_COORDINATES,
            coords3D
        )
        val texCoords3D = FloatArray(4 * 3)
        frame.transformCoordinates3d(
            Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES,
            verticesNdc,
            Coordinates3d.EIS_TEXTURE_NORMALIZED,
            texCoords3D
        )

        // Using the Normals attributes as 3D texCoords because our texCoords in [Vertex] is a Vector2.
        val mesh = Generator.generateScreenQuad(coords3D = coords3D, normals = texCoords3D)
        cameraImageRenderer.updateMesh(mesh, true)
        depthImageRenderer.updateMesh(mesh, true)
    }

    private fun updateCoords2D(frame: Frame) {
        val verticesNdc = floatArrayOf(
            -1f, -1f,
            +1f, -1f,
            +1f, +1f,
            -1f, +1f
        )
        val texCoords = FloatArray(8)
        frame.transformCoordinates2d(
            Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES,
            verticesNdc,
            Coordinates2d.TEXTURE_NORMALIZED,
            texCoords
        )
        val mesh = Generator.generateScreenQuad(texCoords = texCoords)
        cameraImageRenderer.updateMesh(mesh, false)
        depthImageRenderer.updateMesh(mesh, false)
    }

    companion object {
        private const val TAG = "ArCoreManager"
    }

}