package com.rakuten.tech.mobile.inappmessaging.runtime.data.requests

import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants

/**
 * This class represents the request parameters for config request.
 */
internal data class ConfigQueryParamsBuilder(
    private val appId: String? = null,
    private val locale: String? = null,
    private val appVersion: String? = null,
    private val sdkVersion: String? = null,
    private val deviceId: String,
) {

    private val platform = InAppMessagingConstants.ANDROID_PLATFORM_ENUM

    val queryParams = mutableMapOf<String, Any?>(
        "platform" to platform,
        "appId" to appId,
        "sdkVersion" to sdkVersion,
        "appVersion" to appVersion,
        "locale" to locale.orEmpty(),
        "deviceId" to deviceId,
    )
}
