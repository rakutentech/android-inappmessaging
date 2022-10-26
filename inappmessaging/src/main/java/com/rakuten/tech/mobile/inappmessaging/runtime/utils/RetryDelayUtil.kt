package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import kotlin.random.Random

internal object RetryDelayUtil {
    const val RETRY_ERROR_CODE = 429
    const val INITIAL_BACKOFF_DELAY = 60000L // in ms
    private const val RANDOM_RANGE = 60
    private const val RANDOM_MULTIPLIER = 1000

    // exponential delay + random [1...60]
    fun getNextDelay(currDelay: Long) = (currDelay * 2) + (Random.nextInt(RANDOM_RANGE) + 1) * RANDOM_MULTIPLIER
}
