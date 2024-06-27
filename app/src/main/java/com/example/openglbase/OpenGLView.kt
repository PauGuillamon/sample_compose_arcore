package com.example.openglbase

import android.annotation.SuppressLint
import android.opengl.EGL14
import android.opengl.EGL15
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.openglbase.utils.Logger
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay
import javax.microedition.khronos.opengles.GL10

abstract class GLThreadedRenderer {
    abstract val listenToTouchEvents: Boolean

    var view: GLSurfaceView? = null
        internal set

    fun postOnRenderThread(runnable: Runnable) {
        view?.queueEvent(runnable)
    }

    abstract fun onViewLifecycleResume()
    abstract fun onViewLifecyclePause()

    abstract fun onThreadedInitializeGPUData()
    abstract fun onThreadedSurfaceChanged(width: Int, height: Int)
    abstract fun onThreadedTouchEvent(event: MotionEvent)
    abstract fun onThreadedDrawFrame()
}

private class RendererHolder(val renderer: GLThreadedRenderer) {
    lateinit var view: GLSurfaceView

    private var resumed = false

    fun onResume() {
        view.onResume()
        renderer.view = view
        Logger.LogInfo("PGJ", "PGJ OpenGLView onResume")
        renderer.onViewLifecycleResume()
        resumed = true
    }

    fun onPause() {
        if (resumed) {
            Logger.LogInfo("PGJ", "PGJ OpenGLView onPause")
            view.onPause()
            renderer.view = null
            renderer.onViewLifecyclePause()
            resumed = false
        }
    }

    fun onSurfaceCreated() {
        renderer.onThreadedInitializeGPUData()
    }
    fun onSurfaceChanged(width: Int, height: Int) {
        renderer.onThreadedSurfaceChanged(width, height)
    }

    fun onDrawFrame() {
        if (resumed) {
            renderer.onThreadedDrawFrame()
        }
    }
}

// TODO PGJ Document OpenGLView, make it explicit it's just an example of how to do it.
@SuppressLint("ClickableViewAccessibility")
@Composable
fun OpenGLView(
    glThreadedRenderer: GLThreadedRenderer,
    modifier: Modifier = Modifier
) {
    val rendererHolder by remember { mutableStateOf(RendererHolder(glThreadedRenderer)) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            Logger.LogError("PGJ", "PGJ OpenGLView event:$event")
            when (event) {
                Lifecycle.Event.ON_RESUME -> rendererHolder.onResume()
                Lifecycle.Event.ON_PAUSE -> rendererHolder.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    AndroidView(
        modifier = modifier,
        factory = {
            val view = GLSurfaceView(it)
            view.setEGLContextFactory(ContextFactory())
            view.setEGLConfigChooser(ConfigChooser())
            view.setRenderer(object: GLSurfaceView.Renderer {
                override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                    rendererHolder.onSurfaceCreated()
                }
                override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                    rendererHolder.onSurfaceChanged(width, height)
                }
                override fun onDrawFrame(gl: GL10?) {
                    rendererHolder.onDrawFrame()
                }
            })
            rendererHolder.view = view
            if (rendererHolder.renderer.listenToTouchEvents) {
                view.setOnTouchListener { v, event ->
                    /**
                     * Since we are posting a Runnable on the render thread, the
                     * original [MotionEvent] might have been converted back from
                     * local coordinates to global screen coordinates by [android.view.ViewGroup].
                     * Therefore we need to use a copy of the MotionEvent in our runnable.
                     */
                    val copiedEvent = MotionEvent.obtain(event)
                    view.queueEvent {
                        rendererHolder.renderer.onThreadedTouchEvent(copiedEvent)
                        copiedEvent.recycle()
                    }
                    true
                }
            }
            view
        },
        onRelease = {
            Logger.LogError("PGJ", "PGJ OpenGLView AndroidView onRelease")
            rendererHolder.onPause()
        }
    )
}

private fun checkEglError(
    prompt: String,
    egl: EGL10,
) {
    var error: Int
    while (egl.eglGetError().also { error = it } != EGL10.EGL_SUCCESS) {
        Logger.LogError("PGJ", String.format("%s: EGL error: 0x%x", prompt, error))
    }
}

private class ContextFactory : GLSurfaceView.EGLContextFactory {
    override fun createContext(
        egl: EGL10,
        display: EGLDisplay,
        eglConfig: EGLConfig,
    ): EGLContext {
        val attributeList = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 3, EGL10.EGL_NONE) // TODO PGJ
        val context = egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attributeList)
        checkEglError("eglCreateContext", egl)
        return context
    }

    override fun destroyContext(
        egl: EGL10,
        display: EGLDisplay,
        context: EGLContext,
    ) {
        egl.eglDestroyContext(display, context)
    }
}

private class ConfigChooser : GLSurfaceView.EGLConfigChooser {
    private val redSize = 8
    private val greenSize = 8
    private val blueSize = 8
    private val alphaSize = 8
    private val depthSize = 16
    private val stencilSize = 0
    private val configAttributeSet = intArrayOf(
        EGL10.EGL_RED_SIZE, 4,
        EGL10.EGL_GREEN_SIZE, 4,
        EGL10.EGL_BLUE_SIZE, 4,
        EGL10.EGL_ALPHA_SIZE, 4,
        EGL10.EGL_DEPTH_SIZE, 24,
        EGL10.EGL_STENCIL_SIZE, 0,
        EGL10.EGL_RENDERABLE_TYPE, EGL15.EGL_OPENGL_ES3_BIT, // TODO PGJ
        // if (openglEsClientMajorVersion == 3) EGL15.EGL_OPENGL_ES3_BIT else EGL14.EGL_OPENGL_ES2_BIT,
        EGL10.EGL_NONE,
    )

    override fun chooseConfig(
        egl: EGL10,
        display: EGLDisplay,
    ): EGLConfig? {
        val numConfig = IntArray(1)
        egl.eglChooseConfig(display, configAttributeSet, null, 0, numConfig)
        val numConfigs = numConfig[0]

        if (numConfigs <= 0) {
            throw IllegalArgumentException("No configs match configSpec")
        }

        // Allocate then read the array of minimally matching EGL configs
        val configs = arrayOfNulls<EGLConfig>(numConfigs)
        egl.eglChooseConfig(display, configAttributeSet, configs, numConfigs, numConfig)
        return chooseConfig(egl, display, configs)
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun chooseConfig(
        egl: EGL10,
        display: EGLDisplay,
        configs: Array<EGLConfig?>,
    ): EGLConfig? {
        var retval = configs[0]
        for (config in configs) {
            if (config == null) {
                continue
            }
            val depthSize = findConfigAttrib(egl, display, config, EGL10.EGL_DEPTH_SIZE, 0)
            val stencilSize = findConfigAttrib(egl, display, config, EGL10.EGL_STENCIL_SIZE, 0)
            val redSize = findConfigAttrib(egl, display, config, EGL10.EGL_RED_SIZE, 0)
            val greenSize = findConfigAttrib(egl, display, config, EGL10.EGL_GREEN_SIZE, 0)
            val blueSize = findConfigAttrib(egl, display, config, EGL10.EGL_BLUE_SIZE, 0)
            val alphaSize = findConfigAttrib(egl, display, config, EGL10.EGL_ALPHA_SIZE, 0)
            val renderableType = findConfigAttrib(egl, display, config, EGL10.EGL_RENDERABLE_TYPE, 0)

            Logger.LogInfo("PGJ", "Config " +
                    "RGBA:${redSize}x${greenSize}x${blueSize}x${alphaSize} " +
                    "depth:$depthSize stencil:$stencilSize " +
                    "EGL_RENDERABLE_TYPE:0x${renderableType.toHexString()}")

            if (depthSize < this.depthSize || stencilSize < this.stencilSize) {
                continue
            }

            if (redSize == this.redSize &&
                greenSize == this.greenSize &&
                blueSize == this.blueSize &&
                alphaSize == this.alphaSize
            ) {
                retval = config
                break
            }
        }
        return retval
    }

    private fun findConfigAttrib(
        egl: EGL10,
        display: EGLDisplay,
        config: EGLConfig?,
        attribute: Int,
        defaultValue: Int,
    ): Int {
        val value = IntArray(1)
        return if (egl.eglGetConfigAttrib(display, config, attribute, value)) value[0] else defaultValue
    }


    companion object {
        private const val EGL_OPENGL_ES2_BIT = 4 // TODO GL ES 3
    }
}

