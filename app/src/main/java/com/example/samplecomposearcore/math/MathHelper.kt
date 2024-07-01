package com.example.samplecomposearcore.math

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Based on Sceneform:
 * https://github.com/google-ar/sceneform-android-sdk/blob/master/sceneformsrc/sceneform/src/main/java/com/google/ar/sceneform/math/MathHelper.java
 *
 * Static functions for common math operations.
 */
object MathHelper {
    const val FLT_EPSILON = 1.19209290E-07f
    const val MAX_DELTA = 1.0E-10f

    /**
     * Returns true if two floats are equal within a tolerance. Useful for comparing floating point
     * numbers while accounting for the limitations in floating point precision.
     *
     * https://randomascii.wordpress.com/2012/02/25/comparing-floating-point-numbers-2012-edition/
     */
    fun almostEqualRelativeAndAbs(
        a: Float,
        b: Float,
    ): Boolean {
        // Check if the numbers are really close -- needed
        // when comparing numbers near zero.
        var mutableA = a
        var mutableB = b
        val diff = abs(mutableA - mutableB)
        if (diff <= MAX_DELTA) {
            return true
        }
        mutableA = abs(mutableA)
        mutableB = abs(mutableB)
        val largest = max(mutableA, mutableB)
        return diff <= largest * FLT_EPSILON
    }

    /** Clamps a value between a minimum and maximum range.  */
    fun clamp(
        value: Float,
        min: Float,
        max: Float,
    ): Float {
        return min(max, max(min, value))
    }

    /** Clamps a value between a range of 0 and 1.  */
    fun clamp01(value: Float): Float {
        return clamp(value, 0.0f, 1.0f)
    }

    /**
     * Linearly interpolates between a and b by a ratio.
     *
     * @param a the beginning value
     * @param b the ending value
     * @param t ratio between the two floats
     * @return interpolated value between the two floats
     */
    fun lerp(
        a: Float,
        b: Float,
        t: Float,
    ): Float {
        return a + t * (b - a)
    }
}