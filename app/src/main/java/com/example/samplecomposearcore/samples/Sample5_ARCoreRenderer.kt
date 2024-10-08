package com.example.samplecomposearcore.samples

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import com.example.samplecomposearcore.arcoreutils.ARCoreManager
import com.example.samplecomposearcore.arcoreutils.AnchorNode
import com.example.samplecomposearcore.compose.GLThreadedRenderer
import com.example.samplecomposearcore.geometry.Generator
import com.example.samplecomposearcore.math.Vector3
import com.example.samplecomposearcore.opengl.Framebuffer
import com.example.samplecomposearcore.opengl.GPUTexture
import com.example.samplecomposearcore.opengl.MaterialTextured
import com.example.samplecomposearcore.opengl.RenderableMesh
import com.example.samplecomposearcore.opengl.RenderableModel
import com.example.samplecomposearcore.opengl.glHasError
import com.example.samplecomposearcore.renderer.SceneOcclusionQuadRenderer
import com.example.samplecomposearcore.scene.Camera3D
import com.example.samplecomposearcore.scene.DeltaTime
import com.example.samplecomposearcore.scene.Node
import com.example.samplecomposearcore.utils.Logger
import com.google.ar.core.DepthPoint
import com.google.ar.core.Plane
import com.google.ar.core.Point
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import java.util.ArrayDeque

class Sample5_ARCoreRenderer(context: Context, onArCoreSessionCreated: () -> Unit) : GLThreadedRenderer() {
    override val listenToTouchEvents: Boolean = true

    var onFrameFinished: () -> Unit = {}

    val arCoreManager = ARCoreManager(context, onArCoreSessionCreated)

    private val camera3D = Camera3D()
    private val deltaTime = DeltaTime()
    private val sceneOcclusionQuadRenderer = SceneOcclusionQuadRenderer(context)

    private var viewportWidth = 0
    private var viewportHeight = 0
    private var showFeaturePoints = false
    private var showDepthMap = false

    /**
     * This Framebuffer will hold the virtual scene's color and depth.
     */
    private var virtualSceneFramebuffer = Framebuffer(0, 0, depthAsReadableTexture = true)

    /**
     * Not interested in the actual values of this framebuffer's depth buffer, the Depth Map
     * will end up in the color buffer instead with the full range of depth from ARCore.
     */
    private var depthMapFramebuffer = Framebuffer(
        0,
        0,
        colorBufferInternalFormat = GLES30.GL_RG16F,
        depthAsReadableTexture = false
    )

    private val cuteCatCubeZoya: RenderableModel = RenderableModel(
        RenderableMesh().apply {
            setMesh(Generator.generateCube(1.0f))
        },
        MaterialTextured(context.assets, "textures/Zoya.png")
    )
    private val cuteCatCubeMerlot: RenderableModel = RenderableModel(
        RenderableMesh().apply {
            setMesh(Generator.generateCube(1.0f))
        },
        MaterialTextured(context.assets, "textures/Merlot.jpg")
    )
    private lateinit var statueOfLiberty: RenderableModel

    private val rootNode = Node().apply {
        addChild(RotatingNode().apply {
            localPosition = Vector3.forward().scaled(1f)
            localScale = Vector3.one().scaled(0.3f)
            renderableModel = cuteCatCubeZoya

            val childrenScale = 0.5f
            addChild(RotatingNode().apply {
                localPosition = Vector3.right().scaled(1f)
                localScale = Vector3(childrenScale)
                renderableModel = cuteCatCubeMerlot
            })
        })
    }

    private var addedNodes = ArrayDeque<Node>()

    private val gestureDetector = GestureDetector(context, object: SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            Logger.LogInfo(TAG, "GestureDetector onSingleTapUp at ${e.x}x${e.y}")
            arCoreManager.arCoreSession ?: return true
            arCoreManager.currentFrame?.let { frame ->
                val hitResults = frame.hitTest(e.x, e.y)
                for (hitResult in hitResults) {
                    val trackable = hitResult.trackable
                    var useTrackable = false
                    if (trackable.trackingState == TrackingState.TRACKING) {
                        useTrackable = when (trackable) {
                            is Plane -> trackable.isPoseInExtents(hitResult.hitPose)
                            is Point -> true
                            is DepthPoint -> true
                            else -> false
                        }
                    }
                    if (useTrackable) {
                        /**
                         * Naive approach always creating a new Anchor. This might cause performance
                         * issues given that Anchors have an impact on CPU usage. A better approach
                         * could be to reuse Anchors when placing objects close together.
                         */
                        Logger.LogInfo(TAG, "HitResult using trackableType:${trackable::class.simpleName}")
                        val anchorNode = AnchorNode(hitResult.createAnchor())
                        anchorNode.addChild(createNewNodeObject())
                        addedNodes.add(anchorNode)
                        rootNode.addChild(anchorNode)
                        break
                    }
                }
            }
            return true
        }
    })

    init {
        Logger.LogInfo(TAG, "MyRenderer init")
    }

    fun setStatueOfLibertyModel(renderableModel: RenderableModel) {
        statueOfLiberty = renderableModel
    }

    override fun onViewLifecycleResume() {
        Logger.LogInfo(TAG, "MyRenderer onViewLifecycleResume")
        arCoreManager.onResume()
    }

    override fun onViewLifecyclePause() {
        Logger.LogInfo(TAG, "MyRenderer onViewLifecyclePause")
        /**
         * By calling [Session.pause] here, libEGL will log the error:
         *      "call to OpenGL ES API with no current context (logged once per thread)"
         * However, given that [GLSurfaceView] does not notify the renderer that the render thread
         * is being paused, there's no other way to pause the ARCore session.
         * Reimplementing GLSurfaceView could solve this, but it's out of scope for this project.
         */
        arCoreManager.onPause()
        deltaTime.pause()
        cuteCatCubeZoya.restoreInitializedGPU()
        cuteCatCubeMerlot.restoreInitializedGPU()
        if (::statueOfLiberty.isInitialized) {
            statueOfLiberty.restoreInitializedGPU()
        }
    }

    fun onClear() {
        Logger.LogInfo(TAG, "MyRenderer onClear")
        arCoreManager.onClear()
    }

    fun deleteLast() {
        postOnRenderThread {
            if (addedNodes.isNotEmpty()) {
                addedNodes.removeLast()?.detach()
            }
        }
    }

    fun showFeaturePoints(show: Boolean) {
        postOnRenderThread {
            showFeaturePoints = show
        }
    }

    fun showDepthMap(show: Boolean) {
        postOnRenderThread {
            showDepthMap = show
        }
    }

    fun enableEIS(enable: Boolean) {
        postOnRenderThread {
            arCoreManager.enableEIS(enable)
        }
    }

    fun cameraPermissionWasGranted() {
        // Might be called multiple times due to the limitations of CameraPermission.
        arCoreManager.cameraPermissionWasGranted()
    }

    override fun onThreadedInitializeGPUData() {
        Logger.LogInfo(TAG, "MyRenderer onThreadedInitializeGPUData")
        glHasError("initializeGPUData start")

        cuteCatCubeZoya.initializeGPU()
        cuteCatCubeMerlot.initializeGPU()
        glHasError("initializeGPUData after zoyaCube initialized")

        arCoreManager.initializeGPU()
        virtualSceneFramebuffer.clearGPU()
        virtualSceneFramebuffer.initializeGPU()
        depthMapFramebuffer.clearGPU()
        depthMapFramebuffer.initializeGPU()
        sceneOcclusionQuadRenderer.initializeGPU()

        if (glHasError(" initializeGPUData end")) {
            throw IllegalStateException("GL error")
        }
    }

    override fun onThreadedSurfaceChanged(width: Int, height: Int) {
        Logger.LogInfo(TAG, "MyRenderer onThreadedSurfaceChanged ${width}x$height")
        arCoreManager.onSurfaceChanged(width, height)
        camera3D.setViewport(width, height) // Only useful when camera permission is not granted yet.
        viewportWidth = width
        viewportHeight = height
        if (width != virtualSceneFramebuffer.width || height != virtualSceneFramebuffer.height) {
            virtualSceneFramebuffer.clearGPU()
            virtualSceneFramebuffer = Framebuffer(width, height, depthAsReadableTexture = true)
            virtualSceneFramebuffer.initializeGPU()
            depthMapFramebuffer.clearGPU()
            depthMapFramebuffer = Framebuffer(
                width,
                height,
                colorBufferInternalFormat = GLES30.GL_RG16F,
                depthAsReadableTexture = false
            )
            depthMapFramebuffer.initializeGPU()
        }
    }

    override fun onThreadedTouchEvent(event: MotionEvent) {
        gestureDetector.onTouchEvent(event)
    }

    override fun onThreadedDrawFrame() {
        update()
        if (showDepthMap && arCoreManager.depthApiSupported) {
            arCoreManager.renderDepthMap()
        } else {
            render()
        }
        onFrameFinished()
    }

    private fun update() {
        val view = view ?: return
        deltaTime.update()
        arCoreManager.update(view.context) { frame ->
            val arCoreCamera = frame.camera
            arCoreCamera.getViewMatrix(camera3D.viewMatrix.data, 0)
            arCoreCamera.getProjectionMatrix(camera3D.projectionMatrix.data, 0, camera3D.nearPlane, camera3D.farPlane)
        }
        // Updating all the nodes independently of ARCore's update.
        rootNode.traverseEnabledTree {
            it.update(deltaTime.deltaTimeSeconds)
        }
        if (::statueOfLiberty.isInitialized) {
            if (!statueOfLiberty.isGPUInitialized()) {
                statueOfLiberty.initializeGPU()
            }
        }
    }

    private fun render() {
        GLES30.glEnable(GLES30.GL_CULL_FACE)
        GLES30.glFrontFace(GLES30.GL_CCW)
        // 1. First render pass, renders the virtual scene into the virtualSceneFramebuffer.
        virtualSceneFramebuffer.bindBlock {
            GLES30.glViewport(0, 0, it.width, it.height)
            GLES30.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

            renderScene3D()
        }

        // 2. This render pass renders the depth values onto the color texture in depthMapFramebuffer.
        //    By doing this, we let arCoreManager handle the display-to-camera transformations
        //    and the virtual scene can directly sample using the same UV coordinates.
        val depthTexture: GPUTexture?
        if (arCoreManager.hasDepthData) {
            depthMapFramebuffer.bindBlock {
                GLES30.glViewport(0, 0, it.width, it.height)
                GLES30.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
                GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
                arCoreManager.renderDepthRawData()
            }
            depthTexture = depthMapFramebuffer.colorTexture
        } else {
            depthTexture = null
        }

        // 3. Last render pass, combines the virtual scene with the depth texture
        // on top of the camera image into the default framebuffer (display).
        GLES30.glViewport(0, 0, viewportWidth, viewportHeight)
        GLES30.glClearColor(0.05f, 0.05f, 0.05f, 1.0f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        arCoreManager.renderCameraImage()
        sceneOcclusionQuadRenderer.render(
            virtualSceneFramebuffer.colorTexture,
            virtualSceneFramebuffer.depthTexture!!,
            depthTexture,
            camera3D.nearPlane,
            camera3D.farPlane
        )

        glHasError("end onDrawFrame")
    }

    private fun renderScene3D() {
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glDepthMask(true)
        // Simple & naive render pass. Might cause overdraw,
        // transparencies won't work correctly.
        rootNode.traverseEnabledTree {
            it.renderableModel?.renderNode(it, camera3D)
        }
        if (showFeaturePoints) {
            // By rendering the pointCloud after the virtual objects,
            // the GPU can discard pixels sooner in the depth test.
            // This works as long as none of the objects needed transparency,
            // in which case the object's render pass would already fail.
            arCoreManager.renderPointCloud(camera3D)
        }
    }

    private enum class ObjectTypes {
        CuteCatCubeZoya,
        CuteCatCubeMerlot,
        StatueOfLiberty
    }
    private var nextObjectType = ObjectTypes.CuteCatCubeZoya

    private fun createNewNodeObject(): Node {
        val currentObjectType = nextObjectType
        nextObjectType = when (nextObjectType) {
            ObjectTypes.CuteCatCubeZoya -> ObjectTypes.CuteCatCubeMerlot
            ObjectTypes.CuteCatCubeMerlot -> ObjectTypes.StatueOfLiberty
            ObjectTypes.StatueOfLiberty -> ObjectTypes.CuteCatCubeZoya
        }
        return when (currentObjectType) {
            ObjectTypes.CuteCatCubeZoya -> RotatingNode().apply {
                localScale = Vector3(0.5f)
                renderableModel = cuteCatCubeZoya
            }
            ObjectTypes.CuteCatCubeMerlot -> RotatingNode().apply {
                localScale = Vector3(0.5f)
                renderableModel = cuteCatCubeMerlot
            }
            ObjectTypes.StatueOfLiberty -> Node().apply {
                renderableModel = statueOfLiberty
            }
        }
    }

    companion object {
        private const val TAG = "Sample5_ARCoreRenderer"
    }

}