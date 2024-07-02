package com.example.openglbase.math

/**
 *  Based Sceneform:
 *  https://github.com/google-ar/sceneform-android-sdk/blob/master/sceneformsrc/sceneform/src/main/java/com/google/ar/sceneform/math/Quaternion.java
 *
 *  A Sceneform quaternion class for floats.
 *
 *
 * Quaternion operations are Hamiltonian using the right-hand-rule convention.
 */
class Quaternion {
    var x = 0f
    var y = 0f
    var z = 0f
    var w = 0f

    /** Construct Quaternion and set to Identity  */
    constructor() {
        x = 0f
        y = 0f
        z = 0f
        w = 1f
    }

    /**
     * Construct Quaternion and set each value. The Quaternion will be normalized during construction
     */
    constructor(x: Float, y: Float, z: Float, w: Float) {
        set(x, y, z, w)
    }

    /** Construct Quaternion using values from another Quaternion  */
    constructor(q: Quaternion) {
        set(q)
    }

    /**
     * Construct Quaternion using an axis/angle to define the rotation
     *
     * @param axis Sets rotation direction
     * @param angle Angle size in degrees
     */
    constructor(axis: Vector3, angle: Float) {
        set(axisAngle(axis, angle))
    }

    /**
     * Construct Quaternion based on eulerAngles.
     *
     * @see .eulerAngles
     * @param eulerAngles - the angle in degrees for each axis.
     */
    constructor(eulerAngles: Vector3) {
        set(eulerAngles(eulerAngles))
    }

    /** Copy values from another Quaternion into this one  */
    fun set(q: Quaternion) {
        x = q.x
        y = q.y
        z = q.z
        w = q.w
        normalize()
    }

    /** Update this Quaternion using an axis/angle to define the rotation  */
    operator fun set(
        axis: Vector3,
        angle: Float,
    ) {
        set(axisAngle(axis, angle))
    }

    /** Set each value and normalize the Quaternion  */
    operator fun set(
        qx: Float,
        qy: Float,
        qz: Float,
        qw: Float,
    ) {
        x = qx
        y = qy
        z = qz
        w = qw
        normalize()
    }

    /** Set the Quaternion to identity  */
    fun setIdentity() {
        x = 0f
        y = 0f
        z = 0f
        w = 1f
    }

    /**
     * Rescales the quaternion to the unit length.
     *
     *
     * If the Quaternion can not be scaled, it is set to identity and false is returned.
     *
     * @return true if the Quaternion was non-zero
     */
    fun normalize(): Boolean {
        val normSquared = dot(this, this)
        if (MathHelper.almostEqualRelativeAndAbs(normSquared, 0.0f)) {
            setIdentity()
            return false
        } else if (normSquared != 1f) {
            val norm = (1.0 / Math.sqrt(normSquared.toDouble())).toFloat()
            x *= norm
            y *= norm
            z *= norm
            w *= norm
        } else { // do nothing if normSquared is already the unit length
        }
        return true
    }

    /**
     * Get a Quaternion with a matching rotation but scaled to unit length.
     *
     * @return the quaternion scaled to the unit length, or zero if that can not be done.
     */
    fun normalized(): Quaternion {
        val result = Quaternion(this)
        result.normalize()
        return result
    }

    /**
     * Get a Quaternion with the opposite rotation
     *
     * @return the opposite rotation
     */
    fun inverted(): Quaternion {
        return Quaternion(-x, -y, -z, w)
    }

    /**
     * Flips the sign of the Quaternion, but represents the same rotation.
     *
     * @return the negated Quaternion
     */
    fun negated(): Quaternion {
        return Quaternion(-x, -y, -z, -w)
    }

    override fun toString(): String {
        return "[x=$x, y=$y, z=$z, w=$w]"
    }

    /**
     * Uniformly scales a Quaternion without normalizing
     *
     * @return a Quaternion multiplied by a scalar amount.
     */
    fun scaled(a: Float): Quaternion {
        val result = Quaternion()
        result.x = x * a
        result.y = y * a
        result.z = z * a
        result.w = w * a
        return result
    }

    /**
     * Returns true if the other object is a Quaternion and the dot product is 1.0 +/- a tolerance.
     */
    override fun equals(other: Any?): Boolean {
        if (other !is Quaternion) {
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
        result = prime * result + java.lang.Float.floatToIntBits(w)
        result = prime * result + java.lang.Float.floatToIntBits(x)
        result = prime * result + java.lang.Float.floatToIntBits(y)
        result = prime * result + java.lang.Float.floatToIntBits(z)
        return result
    }

    companion object {
        private const val SLERP_THRESHOLD = 0.9995f

        /**
         * Rotates a Vector3 by a Quaternion
         *
         * @return The rotated vector
         */
        fun rotateVector(
            q: Quaternion,
            src: Vector3,
        ): Vector3 {
            val result = Vector3()
            val w2 = q.w * q.w
            val x2 = q.x * q.x
            val y2 = q.y * q.y
            val z2 = q.z * q.z
            val zw = q.z * q.w
            val xy = q.x * q.y
            val xz = q.x * q.z
            val yw = q.y * q.w
            val yz = q.y * q.z
            val xw = q.x * q.w
            val m00 = w2 + x2 - z2 - y2
            val m01 = xy + zw + zw + xy
            val m02 = xz - yw + xz - yw
            val m10 = -zw + xy - zw + xy
            val m11 = y2 - z2 + w2 - x2
            val m12 = yz + yz + xw + xw
            val m20 = yw + xz + xz + yw
            val m21 = yz + yz - xw - xw
            val m22 = z2 - y2 - x2 + w2
            val sx = src.x
            val sy = src.y
            val sz = src.z
            result.x = m00 * sx + m10 * sy + m20 * sz
            result.y = m01 * sx + m11 * sy + m21 * sz
            result.z = m02 * sx + m12 * sy + m22 * sz
            return result
        }

        fun inverseRotateVector(
            q: Quaternion,
            src: Vector3,
        ): Vector3 {
            val result = Vector3()
            val w2 = q.w * q.w
            val x2 = -q.x * -q.x
            val y2 = -q.y * -q.y
            val z2 = -q.z * -q.z
            val zw = -q.z * q.w
            val xy = -q.x * -q.y
            val xz = -q.x * -q.z
            val yw = -q.y * q.w
            val yz = -q.y * -q.z
            val xw = -q.x * q.w
            val m00 = w2 + x2 - z2 - y2
            val m01 = xy + zw + zw + xy
            val m02 = xz - yw + xz - yw
            val m10 = -zw + xy - zw + xy
            val m11 = y2 - z2 + w2 - x2
            val m12 = yz + yz + xw + xw
            val m20 = yw + xz + xz + yw
            val m21 = yz + yz - xw - xw
            val m22 = z2 - y2 - x2 + w2
            val sx = src.x
            val sy = src.y
            val sz = src.z
            result.x = m00 * sx + m10 * sy + m20 * sz
            result.y = m01 * sx + m11 * sy + m21 * sz
            result.z = m02 * sx + m12 * sy + m22 * sz
            return result
        }

        /**
         * Create a Quaternion by combining two Quaternions multiply(lhs, rhs) is equivalent to performing
         * the rhs rotation then lhs rotation Ordering is important for this operation.
         *
         * @return The combined rotation
         */
        fun multiply(
            lhs: Quaternion,
            rhs: Quaternion,
        ): Quaternion {
            val lx = lhs.x
            val ly = lhs.y
            val lz = lhs.z
            val lw = lhs.w
            val rx = rhs.x
            val ry = rhs.y
            val rz = rhs.z
            val rw = rhs.w
            return Quaternion(
                lw * rx + lx * rw + ly * rz - lz * ry,
                lw * ry - lx * rz + ly * rw + lz * rx,
                lw * rz + lx * ry - ly * rx + lz * rw,
                lw * rw - lx * rx - ly * ry - lz * rz,
            )
        }

        /**
         * Adds two Quaternion's without normalizing
         *
         * @return The combined Quaternion
         */
        fun add(
            lhs: Quaternion,
            rhs: Quaternion,
        ): Quaternion {
            val result = Quaternion()
            result.x = lhs.x + rhs.x
            result.y = lhs.y + rhs.y
            result.z = lhs.z + rhs.z
            result.w = lhs.w + rhs.w
            return result
        }

        /** The dot product of two Quaternions.  */
        fun dot(
            lhs: Quaternion,
            rhs: Quaternion,
        ): Float {
            return lhs.x * rhs.x + lhs.y * rhs.y + lhs.z * rhs.z + lhs.w * rhs.w
        }

        /**
         * Returns the linear interpolation between two given rotations by a ratio. The ratio is clamped
         * between a range of 0 and 1.
         */
        fun lerp(
            a: Quaternion,
            b: Quaternion,
            ratio: Float,
        ): Quaternion {
            return Quaternion(
                MathHelper.lerp(a.x, b.x, ratio),
                MathHelper.lerp(a.y, b.y, ratio),
                MathHelper.lerp(a.z, b.z, ratio),
                MathHelper.lerp(a.w, b.w, ratio),
            )
        }

        /**
         * Returns the spherical linear interpolation between two given orientations.
         *
         * If t is 0 this returns a.
         * As t approaches 1 {@link #slerp} may approach either b or -b (whichever is closest
         * to a)
         * If t is above 1 or below 0 the result will be extrapolated.
         * @param start the beginning value
         * @param end the ending value
         * @param t the ratio between the two floats
         * @return interpolated value between the two floats
         */
        fun slerp(
            start: Quaternion,
            end: Quaternion,
            t: Float,
        ): Quaternion {
            val orientation0 = start.normalized()
            var orientation1 = end.normalized()
            // cosTheta0 provides the angle between the rotations at t=0
            var cosTheta0 = dot(orientation0, orientation1).toDouble()
            // Flip end rotation to get shortest path if needed
            if (cosTheta0 < 0.0f) {
                orientation1 = orientation1.negated()
                cosTheta0 = -cosTheta0
            }
            // Small rotations should just use lerp
            if (cosTheta0 > SLERP_THRESHOLD) {
                return lerp(orientation0, orientation1, t)
            }
            // Cosine function range is -1,1. Clamp larger rotations.
            cosTheta0 = Math.max(-1.0, Math.min(1.0, cosTheta0))
            val theta0 = Math.acos(cosTheta0) // Angle between orientations at t=0
            val thetaT = theta0 * t // theta0 scaled to current t
            // s0 = sin(theta0 - thetaT) / sin(theta0)
            val s0 = Math.cos(thetaT) - cosTheta0 * Math.sin(thetaT) / Math.sin(theta0)
            val s1 = Math.sin(thetaT) / Math.sin(theta0)
            // result = s0*start + s1*end
            val result = add(orientation0.scaled(s0.toFloat()), orientation1.scaled(s1.toFloat()))
            return result.normalized()
        }

        /**
         * Get a new Quaternion using an axis/angle to define the rotation
         *
         * @param axis Sets rotation direction
         * @param degrees Angle size in degrees
         */
        fun axisAngle(
            axis: Vector3,
            degrees: Float,
        ): Quaternion {
            val dest = Quaternion()
            val angle = Math.toRadians(degrees.toDouble())
            val factor = Math.sin(angle / 2.0)
            dest.x = (axis.x * factor).toFloat()
            dest.y = (axis.y * factor).toFloat()
            dest.z = (axis.z * factor).toFloat()
            dest.w = Math.cos(angle / 2.0).toFloat()
            dest.normalize()
            return dest
        }

        /**
         * Get a new Quaternion using eulerAngles to define the rotation.
         *
         *
         * The rotations are applied in Z, Y, X order. This is consistent with other graphics engines.
         * One thing to note is the coordinate systems are different between Sceneform and Unity, so the
         * same angles used here will have cause a different orientation than Unity. Carefully check your
         * parameter values to get the same effect as in other engines.
         *
         * @param eulerAngles - the angles in degrees.
         */
        fun eulerAngles(eulerAngles: Vector3): Quaternion {
            val qX = Quaternion(Vector3.right(), eulerAngles.x)
            val qY = Quaternion(Vector3.up(), eulerAngles.y)
            val qZ = Quaternion(Vector3.back(), eulerAngles.z)
            return multiply(multiply(qY, qX), qZ)
        }

        /** Get a new Quaternion representing the rotation from one vector to another.  */
        fun rotationBetweenVectors(
            start: Vector3,
            end: Vector3,
        ): Quaternion {
            var mutableStart = start
            var mutableEnd = end
            mutableStart = mutableStart.normalized()
            mutableEnd = mutableEnd.normalized()
            val cosTheta = Vector3.dot(mutableStart, mutableEnd)
            var rotationAxis: Vector3
            if (cosTheta < -1.0f + 0.001f) { // special case when vectors in opposite directions:
                // there is no "ideal" rotation axis
                // So guess one; any will do as long as it's perpendicular to start
                rotationAxis = Vector3.cross(Vector3.back(), mutableStart)
                if (rotationAxis.lengthSquared() < 0.01f) { // bad luck, they were parallel, try again!
                    rotationAxis = Vector3.cross(Vector3.right(), mutableStart)
                }
                rotationAxis = rotationAxis.normalized()
                return axisAngle(rotationAxis, 180.0f)
            }
            rotationAxis = Vector3.cross(mutableStart, mutableEnd)
            val squareLength = Math.sqrt((1.0 + cosTheta) * 2.0).toFloat()
            val inverseSquareLength = 1.0f / squareLength
            return Quaternion(
                rotationAxis.x * inverseSquareLength,
                rotationAxis.y * inverseSquareLength,
                rotationAxis.z * inverseSquareLength,
                squareLength * 0.5f,
            )
        }

        /**
         * Get a new Quaternion representing a rotation towards a specified forward direction. If
         * upInWorld is orthogonal to forwardInWorld, then the Y axis is aligned with desiredUpInWorld.
         */
        fun lookRotation(
            forwardInWorld: Vector3,
            desiredUpInWorld: Vector3,
        ): Quaternion {
            // Find the rotation between the world forward and the forward to look at.
            var mutableDesiredUpInWorld = desiredUpInWorld
            val rotateForwardToDesiredForward = rotationBetweenVectors(Vector3.forward(), forwardInWorld)
            // Recompute upwards so that it's perpendicular to the direction
            val rightInWorld = Vector3.cross(forwardInWorld, mutableDesiredUpInWorld)
            mutableDesiredUpInWorld = Vector3.cross(rightInWorld, forwardInWorld)
            // Find the rotation between the "up" of the rotated object, and the desired up
            val newUp = rotateVector(rotateForwardToDesiredForward, Vector3.up())
            val rotateNewUpToUpwards = rotationBetweenVectors(newUp, mutableDesiredUpInWorld)
            return multiply(rotateNewUpToUpwards, rotateForwardToDesiredForward)
        }

        /**
         * Compare two Quaternions
         *
         *
         * Tests for equality by calculating the dot product of lhs and rhs. lhs and -lhs will not be
         * equal according to this function.
         */
        fun equals(
            lhs: Quaternion,
            rhs: Quaternion,
        ): Boolean {
            val dot = dot(lhs, rhs)
            return MathHelper.almostEqualRelativeAndAbs(dot, 1.0f)
        }

        /** Get a Quaternion set to identity  */
        fun identity(): Quaternion {
            return Quaternion()
        }
    }
}