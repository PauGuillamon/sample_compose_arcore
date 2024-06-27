package com.example.openglbase.samples

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import com.example.openglbase.GLThreadedRenderer
import com.example.openglbase.SceneOcclusionQuadRenderer
import com.example.openglbase.ZoyaCubeNode
import com.example.openglbase.arcoreutils.ARCoreManager
import com.example.openglbase.arcoreutils.AnchorNode
import com.example.openglbase.geometry.Generator
import com.example.openglbase.math.Vector3
import com.example.openglbase.opengl.Framebuffer
import com.example.openglbase.opengl.GPUTexture
import com.example.openglbase.opengl.RenderableMesh
import com.example.openglbase.opengl.ShaderProgram
import com.example.openglbase.opengl.glHasError
import com.example.openglbase.scene.Camera3D
import com.example.openglbase.scene.DeltaTime
import com.example.openglbase.scene.Node
import com.example.openglbase.utils.Logger
import com.example.openglbase.utils.ShaderReader
import com.example.openglbase.utils.loadImage
import com.google.ar.core.DepthPoint
import com.google.ar.core.Plane
import com.google.ar.core.Point
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import java.util.ArrayDeque

class Sample5Renderer(context: Context, onArCoreSessionCreated: () -> Unit) : GLThreadedRenderer() {
    override val listenToTouchEvents: Boolean = true

    var onFrameFinished: () -> Unit = {}

    val arCoreManager = ARCoreManager(context, onArCoreSessionCreated)

    private val camera3D = Camera3D()
    private val deltaTime = DeltaTime()
    private val sceneOcclusionQuadRenderer = SceneOcclusionQuadRenderer(context)

    private var viewportWidth = 0
    private var viewportHeight = 0

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

    private val zoyaCubeRenderable = RenderableMesh().apply {
        setMesh(Generator.generateCube(0.5f))
    }
    private val zoyaCubeShader = ShaderProgram(
        ShaderReader.readShader(context, "shaders/sample.vert"),
        ShaderReader.readShader(context, "shaders/sample.frag")
    )
    private val zoyaCubeBitmap = loadImage(context.assets, "textures/Zoya.png", true)
    private lateinit var zoyaCubeGpuTexture: GPUTexture
    private val zoyaNode = ZoyaCubeNode().apply {
        localPosition = Vector3.forward().scaled(1f)
        localScale = Vector3.one().scaled(0.3f)

        val childrenScale = 0.5f
        addChild(ZoyaCubeNode().apply {
            localPosition = Vector3.right().scaled(1f)
            localScale = Vector3(childrenScale)
        })
    }

    private val rootNode = Node().apply {
        addChild(zoyaNode)
    }

    private var addedNodes = ArrayDeque<Node>()

    private val gestureDetector = GestureDetector(context, object: SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            Logger.LogError(TAG, "PGJ GestureDetector onSingleTapUp at ${e.x}x${e.y}")
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
                        Logger.LogError(TAG, "PGJ hitResult using trackableType:${trackable::class.simpleName}")
                        val anchorNode = AnchorNode(hitResult.createAnchor())
                        anchorNode.addChild(ZoyaCubeNode().apply {
                            localScale = Vector3(0.5f)
                        })
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

    // TODO PGJ called too many times because of recompositions?
    fun cameraPermissionWasGranted() {
        arCoreManager.cameraPermissionWasGranted()
    }

    override fun onThreadedInitializeGPUData() {
        Logger.LogInfo(TAG, "MyRenderer onThreadedInitializeGPUData")
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
        renderFrame()
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
    }

    private fun renderFrame() {
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
            depthTexture
        )

        glHasError("end onDrawFrame")
    }

    private fun renderScene3D() {
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glDepthMask(true)
        // Simple & naive render pass. Might cause overdraw,
        // transparencies won't work correctly.
        rootNode.traverseEnabledTree {
            if (it is ZoyaCubeNode) {
                renderZoyaCube(it)
            }
        }
        // By rendering the pointCloud after the virtual objects,
        // the GPU can discard pixels sooner in the depth test.
        // This works as long as none of the objects needed transparency,
        // in which case the object's render pass would already fail.
        arCoreManager.renderPointCloud(camera3D)
    }

    private fun renderZoyaCube(zoyaCubeNode: ZoyaCubeNode) {
        zoyaCubeShader.use()
        zoyaCubeShader.setMatrixUniform("uModelMatrix", zoyaCubeNode.worldModelMatrix)
        zoyaCubeShader.setMatrixUniform("uViewMatrix", camera3D.viewMatrix)
        zoyaCubeShader.setMatrixUniform("uProjectionMatrix", camera3D.projectionMatrix)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(zoyaCubeGpuTexture.target, zoyaCubeGpuTexture.id)
        zoyaCubeRenderable.render()
    }

    companion object {
        private const val TAG = "MyRenderer"
    }

}