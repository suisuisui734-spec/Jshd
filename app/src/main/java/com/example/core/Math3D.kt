package com.example.core

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

data class Vec3(
  val x: Float = 0f,
  val y: Float = 0f,
  val z: Float = 0f,
) {
  operator fun plus(o: Vec3): Vec3 = Vec3(x + o.x, y + o.y, z + o.z)

  operator fun minus(o: Vec3): Vec3 = Vec3(x - o.x, y - o.y, z - o.z)

  operator fun times(s: Float): Vec3 = Vec3(x * s, y * s, z * s)

  fun lengthSq(): Float = x * x + y * y + z * z

  fun length(): Float = sqrt(lengthSq())

  fun lengthXZ(): Float = sqrt(x * x + z * z)

  fun normalized(): Vec3 {
    val l = length()
    return if (l > 1e-5f) Vec3(x / l, y / l, z / l) else ZERO
  }

  fun normalizedXZ(): Vec3 {
    val l = lengthXZ()
    return if (l > 1e-5f) Vec3(x / l, 0f, z / l) else FORWARD
  }

  companion object {
    val ZERO = Vec3(0f, 0f, 0f)
    val UP = Vec3(0f, 1f, 0f)
    val FORWARD = Vec3(0f, 0f, 1f)
  }
}

object Math3D {
  fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * clamp(t, 0f, 1f)

  fun lerpVec3(a: Vec3, b: Vec3, t: Float): Vec3 {
    val ct = clamp(t, 0f, 1f)
    return Vec3(lerp(a.x, b.x, ct), lerp(a.y, b.y, ct), lerp(a.z, b.z, ct))
  }

  fun clamp(v: Float, minVal: Float, maxVal: Float): Float = max(minVal, min(maxVal, v))

  fun smoothstep(edge0: Float, edge1: Float, x: Float): Float {
    val t = clamp((x - edge0) / max(1e-5f, edge1 - edge0), 0f, 1f)
    return t * t * (3f - 2f * t)
  }

  fun normalizeAngle(rad: Float): Float {
    var a = rad
    val twoPi = (2.0 * PI).toFloat()
    while (a > PI) a -= twoPi
    while (a < -PI) a += twoPi
    return a
  }

  fun lerpAngle(from: Float, to: Float, t: Float): Float {
    val diff = normalizeAngle(to - from)
    return normalizeAngle(from + diff * clamp(t, 0f, 1f))
  }

  fun yawFromDir(dx: Float, dz: Float): Float = atan2(dx, dz)

  fun forwardFromYaw(yawRad: Float): Vec3 = Vec3(sin(yawRad), 0f, cos(yawRad))

  fun rightFromYaw(yawRad: Float): Vec3 = Vec3(cos(yawRad), 0f, -sin(yawRad))

  fun distanceXZ(a: Vec3, b: Vec3): Float {
    val dx = a.x - b.x
    val dz = a.z - b.z
    return sqrt(dx * dx + dz * dz)
  }

  /** Column-major 4x4 TRS matrix for Filament TransformManager. */
  fun composeTrsColumnMajor(
    pos: Vec3,
    yawRad: Float,
    pitchRad: Float = 0f,
    rollRad: Float = 0f,
    scale: Float = 1f,
    out: FloatArray = FloatArray(16),
  ): FloatArray {
    val cy = cos(yawRad)
    val sy = sin(yawRad)
    val cp = cos(pitchRad)
    val sp = sin(pitchRad)
    val cr = cos(rollRad)
    val sr = sin(rollRad)

    // R = Ry * Rx * Rz
    val r00 = cy * cr + sy * sp * sr
    val r01 = -cy * sr + sy * sp * cr
    val r02 = sy * cp

    val r10 = cp * sr
    val r11 = cp * cr
    val r12 = -sp

    val r20 = -sy * cr + cy * sp * sr
    val r21 = sy * sr + cy * sp * cr
    val r22 = cy * cp

    out[0] = r00 * scale
    out[1] = r10 * scale
    out[2] = r20 * scale
    out[3] = 0f

    out[4] = r01 * scale
    out[5] = r11 * scale
    out[6] = r21 * scale
    out[7] = 0f

    out[8] = r02 * scale
    out[9] = r12 * scale
    out[10] = r22 * scale
    out[11] = 0f

    out[12] = pos.x
    out[13] = pos.y
    out[14] = pos.z
    out[15] = 1f
    return out
  }
}
