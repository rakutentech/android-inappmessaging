package com.rakuten.tech.mobile.inappmessaging.runtime.data.requests

import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.UserIdentifier
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants

/**
 * This class represents the request body for message display permission request.
 */
internal data class DisplayPermissionRequest(
    val campaignId: String?,
    val appVersion: String?,
    val sdkVersion: String?,
    val locale: String?,
    val lastPingInMillis: Long,
    val userIdentifier: List<UserIdentifier>,
    val deviceId: String,
) {
    private val platform: Int = InAppMessagingConstants.ANDROID_PLATFORM_ENUM
}
