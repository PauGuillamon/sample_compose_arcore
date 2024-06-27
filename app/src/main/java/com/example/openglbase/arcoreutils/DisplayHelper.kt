package com.example.openglbase.arcoreutils

import android.content.Context
import android.hardware.display.DisplayManager
import android.hardware.display.DisplayManager.DisplayListener
import android.os.Build
import android.view.WindowManager
import com.google.ar.core.Session

/**
 * Based on:
 * https://github.com/google-ar/arcore-android-sdk/blob/master/samples/hello_ar_kotlin/app/src/main/java/com/google/ar/core/examples/java/common/helpers/DisplayRotationHelper.java
 */
class DisplayHelper(context: Context) : DisplayListener {
    private val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    private val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        context.display!!
    } else {
        @Suppress("DEPRECATION")
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay!!
    }

    private var displayChanged = true
    private var viewportWidth = 0
    private var viewportHeight = 0

    override fun onDisplayAdded(displayId: Int) {
        // Nothing to do
    }

    override fun onDisplayRemoved(displayId: Int) {
        // Nothing to do
    }

    override fun onDisplayChanged(displayId: Int) {
        displayChanged = true
    }

    fun onResume() {
        displayManager.registerDisplayListener(this, null)
    }

    fun onPause() {
        displayManager.unregisterDisplayListener(this)
    }

    fun onViewportChanged(width: Int, height: Int) {
        displayChanged = true
        viewportWidth = width
        viewportHeight = height
    }

    fun update(arCoreSession: Session) {
        if (displayChanged) {
            displayChanged = false
            arCoreSession.setDisplayGeometry(display.rotation, viewportWidth, viewportHeight)
        }
    }
}