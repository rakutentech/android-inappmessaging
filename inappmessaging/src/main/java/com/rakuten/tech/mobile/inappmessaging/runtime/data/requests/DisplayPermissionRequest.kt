package com.rakuten.tech.mobile.inappmessaging.runtime.data.requests

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.UserIdentifier
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants

/**
 * This class represents the request body for message display permission request.
 */
@SuppressWarnings("ObjectToString")
// toString() method is not needed for this class.
internal data class DisplayPermissionRequest(
    @Json(name = "campaignId")
    val campaignId: String?,
    @Json(name = "appVersion")
    val appVersion: String?,
    @Json(name = "sdkVersion")
    val sdkVersion: String?,
    @Json(name = "locale")
    val locale: String?,
    @Json(name = "lastPingInMillis")
    val lastPingInMillis: Long,
    @Json(name = "userIdentifier")
    val userIdentifier: List<UserIdentifier>
) {
    @Json(name = "platform")
    val platform: Int = InAppMessagingConstants.ANDROID_PLATFORM_ENUM
}
