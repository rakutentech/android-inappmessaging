package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Config Response class for GSON.
 */
@JsonClass(generateAdapter = true)
internal data class ConfigResponse(
    @Json(name = "data") val data: ConfigResponseData? = null
)
