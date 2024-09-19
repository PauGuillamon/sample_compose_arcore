package com.example.samplecomposearcore.utils

import android.content.Context
import android.content.res.AssetManager
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

object ShaderReader {
    /**
     * Reads a Shader source file from the assets folder.
     *
     * @param filename The filename of the shader source file.
     * @return The content of the text file, or null in case of error.
     */
    @Throws(IOException::class)
    fun readShader(context: Context, filename: String): String {
        return readShader(context.assets, filename)
    }

    @Throws(IOException::class)
    fun readShader(assetManager: AssetManager, filename: String): String {
        assetManager.open(filename).use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                val sb = StringBuilder()
                var line = reader.readLine()
                while (line != null) {
                    val tokens = line.split(" ").dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (tokens.isNotEmpty() && tokens[0] == "#include") {
                        var includeFilename = tokens[1]
                        includeFilename = includeFilename.replace("\"", "")
                        if (includeFilename == filename) {
                            throw IOException("Do not include the calling file.")
                        }
                        sb.append(readShader(assetManager, includeFilename))
                    } else {
                        sb.append(line).append("\n")
                    }
                    line = reader.readLine()
                }
                return sb.toString()
            }
        }
    }
}