package com.rakuten.tech.mobile.inappmessaging.runtime.data.requests

import com.google.gson.annotations.SerializedName
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.UserIdentifier

/**
 * This class represents the request body for ping request.
 */
internal data class PingRequest(
    private val appVersion: String?,
    private val userIdentifiers: List<UserIdentifier>?,
    @SerializedName("supportedCampaignTypes") private val supportedTypes: List<Int>,
    private val rmcSdkVersion: String?,
)
