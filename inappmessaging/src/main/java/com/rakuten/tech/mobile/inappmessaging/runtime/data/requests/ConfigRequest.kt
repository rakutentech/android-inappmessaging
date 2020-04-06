package com.rakuten.tech.mobile.inappmessaging.runtime.data.requests

import com.google.gson.annotations.SerializedName
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings

/**
 * This class represents the request body for config request.
 */
internal data class ConfigRequest(
    @SerializedName("appId")
    val appId: String?,
    @SerializedName("locale")
    val locale: String?,
    @SerializedName("appVersion")
    val appVersion: String?,
    @SerializedName("sdkVersion")
    val sdkVersion: String?
) {

    @SerializedName("platform")
    @SuppressFBWarnings("SS_SHOULD_BE_STATIC")
    private val platform = InAppMessagingConstants.ANDROID_PLATFORM_ENUM
}
