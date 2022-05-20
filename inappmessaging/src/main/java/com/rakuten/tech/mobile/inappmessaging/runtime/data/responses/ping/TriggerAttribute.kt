package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Class for parsing TriggerAttribute, which is a response from MessageMixer.
 */
@JsonClass(generateAdapter = true)
internal data class TriggerAttribute(
    @Json(name = "name")
    val name: String,

    @Json(name = "value")
    val value: String,

    @Json(name = "type")
    val type: Int,

    @Json(name = "operator")
    val operator: Int
)
