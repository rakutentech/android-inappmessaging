package com.rakuten.tech.mobile.inappmessaging.runtime.data.requests

import com.google.gson.annotations.SerializedName
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.UserIdentifier

/**
 * This class represents the request body for ping request.
 */
internal data class PingRequest(
    @SerializedName("appVersion")
    private val appVersion: String?,
    @SerializedName("userIdentifiers")
    private val userIdentifiers: MutableList<UserIdentifier>?
)
