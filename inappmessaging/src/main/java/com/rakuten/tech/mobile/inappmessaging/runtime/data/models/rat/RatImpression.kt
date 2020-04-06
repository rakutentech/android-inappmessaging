package com.rakuten.tech.mobile.inappmessaging.runtime.data.models.rat

import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ImpressionType
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings

/**
 * This class specifically made for broadcasting impressions to RAT.
 */
@SuppressFBWarnings("URF_UNREAD_FIELD")
internal data class RatImpression(private val impressionType: ImpressionType, private val timestamp: Long) {

    private val action = impressionType.typeId
    private val ts = timestamp
}
