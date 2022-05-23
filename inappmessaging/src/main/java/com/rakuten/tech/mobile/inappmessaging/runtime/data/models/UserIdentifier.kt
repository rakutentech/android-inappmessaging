package com.rakuten.tech.mobile.inappmessaging.runtime.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * This class represents user identification.
 */
@JsonClass(generateAdapter = true)
internal data class UserIdentifier(
    @Json(name = "id")
    val id: String,
    @Json(name = "type")
    val type: Int
)
