package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Endpoints object.
 * Part of ConfigResponse from Config Service.
 */
@JsonClass(generateAdapter = true)
internal data class ConfigResponseEndpoints(
    @Json(name = "ping") val ping: String? = null,
    @Json(name = "impression") val impression: String? = null,
    @Json(name = "displayPermission") val displayPermission: String? = null
)
