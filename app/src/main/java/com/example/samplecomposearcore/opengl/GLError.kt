package com.example.samplecomposearcore.opengl

import android.opengl.GLES30
import com.example.samplecomposearcore.utils.Logger

fun glHasError(location: String = ""): Boolean {
    var errorFound = false
    var errorCode = GLES30.glGetError()
    while (errorCode != GLES30.GL_NO_ERROR) {
        errorFound = true
        logError(errorCode, location)
        errorCode = GLES30.glGetError()
    }
    return errorFound
}

private fun logError(errorCode: Int, location: String) {
    val errorName = when (errorCode) {
        GLES30.GL_INVALID_ENUM -> "INVALID_ENUM"
        GLES30.GL_INVALID_VALUE -> "GL_INVALID_VALUE"
        GLES30.GL_INVALID_OPERATION -> "GL_INVALID_OPERATION"
        GLES30.GL_OUT_OF_MEMORY -> "INVALID_ENUM"
        GLES30.GL_INVALID_FRAMEBUFFER_OPERATION -> "INVALID_ENUM"
        else -> "UNKNOWN(errorCode:$errorCode)"
    }
    val locationStr = if (location.isEmpty()) "" else " at [$location]"
    Logger.LogError("GLError", "GLError:$errorName at \"$locationStr\"")
}
