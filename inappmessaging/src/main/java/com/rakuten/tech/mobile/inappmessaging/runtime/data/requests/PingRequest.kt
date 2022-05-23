package com.rakuten.tech.mobile.inappmessaging.runtime.data.requests

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.UserIdentifier

/**
 * This class represents the request body for ping request.
 */
@JsonClass(generateAdapter = true)
internal data class PingRequest(
    @Json(name = "appVersion")
    val appVersion: String?,
    @Json(name = "userIdentifiers")
    val userIdentifiers: MutableList<UserIdentifier>?
)
