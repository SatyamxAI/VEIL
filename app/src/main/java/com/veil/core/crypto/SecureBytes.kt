package com.veil.core.crypto

import java.io.Closeable
import java.util.Arrays

/**
 * Secure container for sensitive cryptographic key material.
 * Ensures zeroization upon close/garbage collection to prevent key remanence in memory.
 */
class SecureBytes(private val data: ByteArray) : Closeable {
    private var isDestroyed = false

    val bytes: ByteArray
        get() {
            check(!isDestroyed) { "Key material has already been destroyed." }
            return data
        }

    val size: Int
        get() = data.size

    fun copyBytes(): ByteArray {
        check(!isDestroyed) { "Key material has already been destroyed." }
        return data.clone()
    }

    override fun close() {
        destroy()
    }

    fun destroy() {
        if (!isDestroyed) {
            Arrays.fill(data, 0.toByte())
            isDestroyed = true
        }
    }

    protected fun finalize() {
        destroy()
    }
}
