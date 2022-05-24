package com.rakuten.tech.mobile.inappmessaging.runtime.data.models.rat

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * This class contains just name and value attributes.
 * Specially made for broadcasting RAT events.
 */
internal data class RatAttribute(
    @Json(name = "name") val name: String,
    @Json(name = "value") val value: Any
)
