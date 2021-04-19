package com.rakuten.tech.mobile.inappmessaging.runtime.data.requests

import com.google.gson.annotations.SerializedName
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants

/**
 * This class represents the request body for config request.
 */
internal data class ConfigRequest(
    @SerializedName("appId")
    private val appId: String?,
    @SerializedName("locale")
    private val locale: String?,
    @SerializedName("appVersion")
    private val appVersion: String?,
    @SerializedName("sdkVersion")
    private val sdkVersion: String?
) {

    @SerializedName("platform")
    private val platform = InAppMessagingConstants.ANDROID_PLATFORM_ENUM

    val queryParam = mutableMapOf(
            "platform" to platform,
            "appId" to appId,
            "sdkVersion" to sdkVersion,
            "appVersion" to appVersion,
            "locale" to locale)
}
