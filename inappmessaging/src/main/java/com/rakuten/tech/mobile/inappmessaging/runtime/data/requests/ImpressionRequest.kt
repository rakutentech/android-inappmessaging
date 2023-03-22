package com.rakuten.tech.mobile.inappmessaging.runtime.data.requests

import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ImpressionType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.UserIdentifier

/**
 * This class represents the request body for impression.
 */
internal data class ImpressionRequest(
    private val campaignId: String?,
    private val isTest: Boolean,
    private val appVersion: String?,
    private val sdkVersion: String?,
    private val userIdentifiers: List<UserIdentifier>,
    val impressions: List<Impression>,
)

internal data class Impression(
    private val impType: ImpressionType,
    val timestamp: Long,
) {
    val type = impType.typeId
}
