package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Class for parsing MessageSettings, which is a response from MessageMixer.
 */
@JsonClass(generateAdapter = true)
internal data class MessageSettings(
    @Json(name = "displaySettings")
    val displaySettings: DisplaySettings,

    @Json(name = "controlSettings")
    val controlSettings: ControlSettings
)
