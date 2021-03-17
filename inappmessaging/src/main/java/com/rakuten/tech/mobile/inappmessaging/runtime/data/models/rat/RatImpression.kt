package com.rakuten.tech.mobile.inappmessaging.runtime.data.models.rat

import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ImpressionType

/**
 * This class specifically made for broadcasting impressions to RAT.
 */
internal data class RatImpression(private val impressionType: ImpressionType, private val timestamp: Long) {

    private val action = impressionType.typeId
    private val ts = timestamp
}
