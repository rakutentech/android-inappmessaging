package com.rakuten.tech.mobile.inappmessaging.runtime.data.models.rat

/**
 * This class specifically made for broadcasting impressions to RAT.
 */
internal data class RatImpression(private val action: Int, private val timestamp: Long)
