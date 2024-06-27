package com.example.openglbase.utils

import android.util.Log

object Logger {
    fun LogError(tag: String, msg: String) {
        Log.e(tag, msg)
    }

    fun LogWarning(tag: String, msg: String) {
        Log.w(tag, msg)
    }

    fun LogInfo(tag: String, msg: String) {
        Log.i(tag, msg)
    }

    fun LogVerbose(tag: String, msg: String) {
        Log.v(tag, msg)
    }

    fun LogDebug(tag: String, msg: String) {
        Log.d(tag, msg)
    }
}