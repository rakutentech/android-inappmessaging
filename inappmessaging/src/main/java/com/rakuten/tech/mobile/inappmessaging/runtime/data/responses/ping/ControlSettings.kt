package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Class for parsing ControlSettings, which is a response from MessageMixer.
 */
@JsonClass(generateAdapter = true)
internal data class ControlSettings(
    @Json(name = "buttons")
    val buttons: List<MessageButton>,
    @Json(name = "content")
    val content: Content? = null
)
