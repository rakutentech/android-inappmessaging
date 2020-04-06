package com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping

import com.google.gson.annotations.SerializedName

/**
 * Class for parsing DataItem, which is a response from MessageMixer.
 */
internal data class MessageSettings(
    @SerializedName("displaySettings")
    val displaySettings: DisplaySettings?,

    @com.google.gson.annotations.SerializedName("controlSettings")
    val controlSettings: ControlSettings?
)
