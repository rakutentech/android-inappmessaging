package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Class for parsing response from MessageMixer.
 */
@JsonClass(generateAdapter = true)
internal data class MessageMixerResponse(
    @Json(name = "data")
    val data: List<DataItem>,
    // SDK's next ping to message mixer should be in nextPingMillis. Time is in millisecond.
    @Json(name = "nextPingMillis")
    val nextPingMillis: Long,
    // Current UTC time when message mixer returned response.
    @Json(name = "currentPingMillis")
    val currentPingMillis: Long
)
