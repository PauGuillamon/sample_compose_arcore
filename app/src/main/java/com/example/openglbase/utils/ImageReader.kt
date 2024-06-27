package com.example.openglbase.utils

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory

fun loadImage(assetManager: AssetManager, filename: String, invertYAxis: Boolean): Bitmap {
    val inputStream = assetManager.open(filename)
    val bitmap = BitmapFactory.decodeStream(inputStream)
    inputStream.close()
    return if (invertYAxis) {
        val tmp = flipBitmap(bitmap)
        bitmap.recycle()
        return tmp
    } else {
        bitmap
    }
}

private fun flipBitmap(bitmap: Bitmap): Bitmap {
    val matrix = android.graphics.Matrix()
    matrix.preScale(1.0f, -1.0f)
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}
