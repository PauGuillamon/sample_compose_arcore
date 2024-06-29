package com.example.openglbase.opengl

import android.opengl.GLES30
import android.util.Log
import com.example.openglbase.math.Matrix
import com.example.openglbase.utils.Logger

class ShaderProgram(private val vertexShaderCode: String, private val fragmentShaderCode: String) {

    private var id = 0

    fun compile() {
        val vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode)

        id = GLES30.glCreateProgram()
        GLES30.glAttachShader(id, vertexShader)
        GLES30.glAttachShader(id, fragmentShader)
        GLES30.glLinkProgram(id)

        val value = IntArray(1)
        GLES30.glGetProgramiv(id, GLES30.GL_LINK_STATUS, value, 0)
        if (value[0] != GLES30.GL_TRUE) {
            val infoLog = GLES30.glGetProgramInfoLog(id)
            Log.e("ShaderProgram", "ERROR compiling shader: $infoLog")
            id = 0
        }
        GLES30.glDeleteShader(vertexShader)
        GLES30.glDeleteShader(fragmentShader)

        if (glHasError("ShaderProgram compile")) {
            throw IllegalStateException("Shader failed to compile.")
        }
    }

    fun delete() {
        if (id != 0) {
            GLES30.glDeleteProgram(id)
            id = 0
        }
    }

    fun use() {
        if (id == 0) {
            throw IllegalStateException("Shader is not ready to be used. It was probably not compiled.")
        }
        GLES30.glUseProgram(id)
    }

    fun setTextureSampler2D(samplerName: String, textureUnit: Int) {
        val samplerLocation = GLES30.glGetUniformLocation(id, samplerName)
        if (samplerLocation >= 0) {
            GLES30.glUniform1i(samplerLocation, textureUnit)
        }
    }

    fun setMatrixUniform(uniformName: String, matrix: Matrix) {
        val uniformLocation = GLES30.glGetUniformLocation(id, uniformName)
        if (uniformLocation >= 0) {
            GLES30.glUniformMatrix4fv(uniformLocation, 1, false, matrix.data, 0)
        }
    }

    fun setIntUniform(uniformName: String, value: Int) {
        val uniformLocation = GLES30.glGetUniformLocation(id, uniformName)
        if (uniformLocation >= 0) {
            GLES30.glUniform1i(uniformLocation, value)
        }
    }

    fun setBoolUniform(uniformName: String, value: Boolean) {
        setIntUniform(uniformName, if (value) 1 else 0)
    }

    fun setFloatUniform(uniformName: String, value: Float) {
        val uniformLocation = GLES30.glGetUniformLocation(id, uniformName)
        if (uniformLocation >= 0) {
            GLES30.glUniform1f(uniformLocation, value)
        }
    }

    private fun loadShader(shaderType: Int, shaderCode: String): Int {
        var shaderId = GLES30.glCreateShader(shaderType)
        GLES30.glShaderSource(shaderId, shaderCode)
        GLES30.glCompileShader(shaderId)

        val value = IntArray(1)
        GLES30.glGetShaderiv(shaderId, GLES30.GL_COMPILE_STATUS, value, 0)
        if (value[0] != GLES30.GL_TRUE) {
            val infoLog = GLES30.glGetShaderInfoLog(shaderId)
            Logger.LogError("ShaderProgram", "ERROR loading shader ${shaderType.shaderTypeName()}: $infoLog")
            shaderId = 0
        }
        return shaderId
    }

    private fun Int.shaderTypeName(): String {
        return when (this) {
            GLES30.GL_VERTEX_SHADER -> "VERTEX_SHADER"
            GLES30.GL_FRAGMENT_SHADER -> "FRAGMENT_SHADER"
            else -> "Unknown"
        }
    }

}