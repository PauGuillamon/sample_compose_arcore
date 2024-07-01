package com.example.samplecomposearcore.math


class Vector2(var x: Float, var y: Float) {

    companion object {
        const val SIZE_BYTES = Float.SIZE_BYTES * 2
    }
}

/**
 * Based on Sceneform:
 * https://github.com/google-ar/sceneform-android-sdk/blob/master/sceneformsrc/sceneform/src/main/java/com/google/ar/sceneform/math/Vector3.java
 */
class Vector3 {
    var x = 0f
    var y = 0f
    var z = 0f

    /** Construct a Vector3 and assign zero to all values  */
    constructor() {
        x = 0f
        y = 0f
        z = 0f
    }

    /** Construct a Vector3 and assign the same value to all values */
    constructor(value: Float) {
        x = value
        y = value
        z = value
    }

    /** Construct a Vector3 and assign each value  */
    constructor(x: Float, y: Float, z: Float) {
        this.x = x
        this.y = y
        this.z = z
    }

    /** Construct a Vector3 and copy the values  */
    constructor(v: Vector3) {
        set(v)
    }

    /** Copy the values from another Vector3 to this Vector3  */
    fun set(v: Vector3) {
        x = v.x
        y = v.y
        z = v.z
    }

    /** Set each value  */
    fun set(
        vx: Float,
        vy: Float,
        vz: Float,
    ) {
        x = vx
        y = vy
        z = vz
    }

    /** Set all values of the Vector3 to this value */
    fun setAll(value: Float) {
        x = value
        y = value
        z = value
    }

    /** Set each value to zero  */
    fun setZero() {
        set(0f, 0f, 0f)
    }

    /** Set each value to one  */
    fun setOne() {
        set(1f, 1f, 1f)
    }

    /** Forward into the screen is the negative Z direction  */
    fun setForward() {
        set(0f, 0f, -1f)
    }

    /** Back out of the screen is the positive Z direction  */
    fun setBack() {
        set(0f, 0f, 1f)
    }

    /** Up is the positive Y direction  */
    fun setUp() {
        set(0f, 1f, 0f)
    }

    /** Down is the negative Y direction  */
    fun setDown() {
        set(0f, -1f, 0f)
    }

    /** Right is the positive X direction  */
    fun setRight() {
        set(1f, 0f, 0f)
    }

    /** Left is the negative X direction  */
    fun setLeft() {
        set(-1f, 0f, 0f)
    }

    fun lengthSquared(): Float {
        return x * x + y * y + z * z
    }

    fun length(): Float {
        return Math.sqrt(lengthSquared().toDouble()).toFloat()
    }

    override fun toString(): String {
        return "[x=$x, y=$y, z=$z]"
    }

    /** Scales the Vector3 to the unit length  */
    fun normalized(): Vector3 {
        val result = Vector3(this)
        val normSquared = dot(this, this)
        if (MathHelper.almostEqualRelativeAndAbs(normSquared, 0.0f)) {
            result.setZero()
        } else if (normSquared != 1f) {
            val norm = (1.0 / Math.sqrt(normSquared.toDouble())).toFloat()
            result.set(scaled(norm))
        }
        return result
    }

    /**
     * Uniformly scales a Vector3
     *
     * @return a Vector3 multiplied by a scalar amount
     */
    fun scaled(a: Float): Vector3 {
        return Vector3(x * a, y * a, z * a)
    }

    /**
     * Negates a Vector3
     *
     * @return A Vector3 with opposite direction
     */
    fun negated(): Vector3 {
        return Vector3(-x, -y, -z)
    }

    /** Calculates the distance to another Vector3 */
    fun distanceTo(other: Vector3): Float {
        val difference = subtract(this, other)
        return difference.length()
    }

    /**
     * Returns true if the other object is a Vector3 and each component is equal within a tolerance.
     */
    override fun equals(other: Any?): Boolean {
        if (other !is Vector3) {
            return false
        }
        return if (this === other) {
            true
        } else {
            equals(this, other)
        }
    }

    /** @hide
     */
    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + java.lang.Float.floatToIntBits(x)
        result = prime * result + java.lang.Float.floatToIntBits(y)
        result = prime * result + java.lang.Float.floatToIntBits(z)
        return result
    }

    companion object {
        const val SIZE_BYTES = Float.SIZE_BYTES * 3
        /**
         * Adds two Vector3's
         *
         * @return The combined Vector3
         */
        fun add(
            lhs: Vector3,
            rhs: Vector3,
        ): Vector3 {
            return Vector3(lhs.x + rhs.x, lhs.y + rhs.y, lhs.z + rhs.z)
        }

        /**
         * Subtract two Vector3
         *
         * @return The combined Vector3
         */
        fun subtract(
            lhs: Vector3,
            rhs: Vector3,
        ): Vector3 {
            return Vector3(lhs.x - rhs.x, lhs.y - rhs.y, lhs.z - rhs.z)
        }

        /**
         * Get dot product of two Vector3's
         *
         * @return The scalar product of the Vector3's
         */
        fun dot(
            lhs: Vector3,
            rhs: Vector3,
        ): Float {
            return lhs.x * rhs.x + lhs.y * rhs.y + lhs.z * rhs.z
        }

        /**
         * Get cross product of two Vector3's
         *
         * @return A Vector3 perpendicular to Vector3's
         */
        fun cross(
            lhs: Vector3,
            rhs: Vector3,
        ): Vector3 {
            val lhsX = lhs.x
            val lhsY = lhs.y
            val lhsZ = lhs.z
            val rhsX = rhs.x
            val rhsY = rhs.y
            val rhsZ = rhs.z
            return Vector3(
                lhsY * rhsZ - lhsZ * rhsY,
                lhsZ * rhsX - lhsX * rhsZ,
                lhsX * rhsY - lhsY * rhsX,
            )
        }

        /** Get a Vector3 with each value set to the element wise minimum of two Vector3's values  */
        fun min(
            lhs: Vector3,
            rhs: Vector3,
        ): Vector3 {
            return Vector3(Math.min(lhs.x, rhs.x), Math.min(lhs.y, rhs.y), Math.min(lhs.z, rhs.z))
        }

        /** Get a Vector3 with each value set to the element wise maximum of two Vector3's values  */
        fun max(
            lhs: Vector3,
            rhs: Vector3,
        ): Vector3 {
            return Vector3(Math.max(lhs.x, rhs.x), Math.max(lhs.y, rhs.y), Math.max(lhs.z, rhs.z))
        }

        /** Get the maximum value in a single Vector3  */
        fun componentMax(a: Vector3): Float {
            return Math.max(Math.max(a.x, a.y), a.z)
        }

        /** Get the minimum value in a single Vector3  */
        fun componentMin(a: Vector3): Float {
            return Math.min(Math.min(a.x, a.y), a.z)
        }

        /**
         * Linearly interpolates between a and b.
         *
         * @param a the beginning value
         * @param b the ending value
         * @param t ratio between the two floats.
         * @return interpolated value between the two floats
         */
        fun lerp(
            a: Vector3,
            b: Vector3,
            t: Float,
        ): Vector3 {
            return Vector3(
                MathHelper.lerp(a.x, b.x, t),
                MathHelper.lerp(a.y, b.y, t),
                MathHelper.lerp(a.z, b.z, t),
            )
        }

        /**
         * Returns the shortest angle in degrees between two vectors. The result is never greater than 180
         * degrees.
         */
        fun angleBetweenVectors(
            a: Vector3,
            b: Vector3,
        ): Float {
            val lengthA = a.length()
            val lengthB = b.length()
            val combinedLength = lengthA * lengthB
            if (MathHelper.almostEqualRelativeAndAbs(combinedLength, 0.0f)) {
                return 0.0f
            }
            val dot = dot(a, b)
            var cos = dot / combinedLength
            // Clamp due to floating point precision that could cause dot to be > combinedLength.
            // Which would cause acos to return NaN.
            cos = MathHelper.clamp(cos, -1.0f, 1.0f)
            val angleRadians = Math.acos(cos.toDouble()).toFloat()
            return Math.toDegrees(angleRadians.toDouble()).toFloat()
        }

        /** Compares two Vector3's are equal if each component is equal within a tolerance.  */
        fun equals(
            lhs: Vector3,
            rhs: Vector3,
        ): Boolean {
            var result = true
            result = result and MathHelper.almostEqualRelativeAndAbs(lhs.x, rhs.x)
            result = result and MathHelper.almostEqualRelativeAndAbs(lhs.y, rhs.y)
            result = result and MathHelper.almostEqualRelativeAndAbs(lhs.z, rhs.z)
            return result
        }

        /** Gets a Vector3 with all values set to zero  */
        fun zero(): Vector3 {
            return Vector3()
        }

        /** Gets a Vector3 with all values set to one  */
        fun one(): Vector3 {
            val result = Vector3()
            result.setOne()
            return result
        }

        /** Gets a Vector3 set to (0, 0, -1)  */
        fun forward(): Vector3 {
            val result = Vector3()
            result.setForward()
            return result
        }

        /** Gets a Vector3 set to (0, 0, 1)  */
        fun back(): Vector3 {
            val result = Vector3()
            result.setBack()
            return result
        }

        /** Gets a Vector3 set to (0, 1, 0)  */
        fun up(): Vector3 {
            val result = Vector3()
            result.setUp()
            return result
        }

        /** Gets a Vector3 set to (0, -1, 0)  */
        fun down(): Vector3 {
            val result = Vector3()
            result.setDown()
            return result
        }

        /** Gets a Vector3 set to (1, 0, 0)  */
        fun right(): Vector3 {
            val result = Vector3()
            result.setRight()
            return result
        }

        /** Gets a Vector3 set to (-1, 0, 0)  */
        fun left(): Vector3 {
            val result = Vector3()
            result.setLeft()
            return result
        }
    }
}
