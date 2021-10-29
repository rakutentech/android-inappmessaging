package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping

import com.google.gson.annotations.SerializedName

/**
 * Class for parsing ControlSettings, which is a response from MessageMixer.
 */
internal data class ControlSettings(
    @SerializedName("buttons")
    val buttons: List<MessageButton>,
    @SerializedName("content")
    val content: Content? = null
)
