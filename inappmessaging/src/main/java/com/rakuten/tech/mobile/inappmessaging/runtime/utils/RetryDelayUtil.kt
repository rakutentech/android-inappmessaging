package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import kotlin.random.Random

internal object RetryDelayUtil {
    const val RETRY_ERROR_CODE = 429
    const val INITIAL_BACKOFF_DELAY = 60000L // in ms

    // exponential delay + random [1...60]
    @SuppressWarnings("MagicNumber")
    fun getNextDelay(currDelay: Long) = (currDelay * 2) + (Random.nextInt(60) + 1) * 1000
}
