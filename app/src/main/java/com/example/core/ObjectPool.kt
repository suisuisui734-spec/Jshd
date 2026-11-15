package com.example.core

/**
 * Lightweight pre-allocated object pool to eliminate per-frame GC churn
 * for particles, hit sparks, shockwave instances, and damage events.
 */
class ObjectPool<T>(
  private val maxCapacity: Int,
  private val factory: () -> T,
  private val reset: (T) -> Unit,
) {
  private val available = ArrayDeque<T>(maxCapacity)

  fun prewarm(count: Int) {
    val target = minOf(count, maxCapacity)
    repeat(target) {
      if (available.size < maxCapacity) {
        available.addLast(factory())
      }
    }
  }

  fun acquire(): T {
    val item = if (available.isNotEmpty()) available.removeLast() else factory()
    reset(item)
    return item
  }

  fun release(item: T) {
    if (available.size < maxCapacity) {
      reset(item)
      available.addLast(item)
    }
  }
}
