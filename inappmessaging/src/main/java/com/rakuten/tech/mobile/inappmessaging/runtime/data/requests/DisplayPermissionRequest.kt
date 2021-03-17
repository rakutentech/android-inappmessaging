package com.rakuten.tech.mobile.inappmessaging.runtime.data.requests

import com.google.gson.annotations.SerializedName
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.UserIdentifier
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants

/**
 * This class represents the request body for message display permission request.
 */
@SuppressWarnings("ObjectToString") // toString() method is not needed for this class.
internal data class DisplayPermissionRequest(
    @SerializedName("campaignId")
    val campaignId: String?,
    @SerializedName("appVersion")
    val appVersion: String?,
    @SerializedName("sdkVersion")
    val sdkVersion: String?,
    @SerializedName("locale")
    val locale: String?,
    @SerializedName("lastPingInMillis")
    val lastPingInMillis: Long,
    @SerializedName("userIdentifier")
    val userIdentifier: List<UserIdentifier>
) {
    @SerializedName("platform")
    private val platform: Int = InAppMessagingConstants.ANDROID_PLATFORM_ENUM // NOPMD
}
