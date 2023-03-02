package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping

import com.google.gson.annotations.SerializedName

/**
 * Class for parsing response from MessageMixer.
 */
internal data class MessageMixerResponse(
    @SerializedName("data")
    val data: List<DataItem>,
    // SDK's next ping to message mixer should be in nextPingMillis. Time is in millisecond.
    @SerializedName("nextPingMillis")
    val nextPingMillis: Long,
    // Current UTC time when message mixer returned response.
    @SerializedName("currentPingMillis")
    val currentPingMillis: Long,
)
