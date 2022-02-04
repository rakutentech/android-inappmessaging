package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import java.nio.charset.Charset

internal object CacheUtil {

    /**
     * @return 1/8th of the given memory in byte.
     */
    fun getMemoryCacheSize(memory: Int = getMaxMemory()): Int = if (memory > 0) memory / MEM_FRACTION else 1

    /**
     * @return the available VM memory in byte.
     * Get max available VM memory, exceeding this amount will throw an
     * OutOfMemory exception.
     */
    private fun getMaxMemory() = Runtime.getRuntime().maxMemory().toInt()

    private const val MEM_FRACTION = 8
}
